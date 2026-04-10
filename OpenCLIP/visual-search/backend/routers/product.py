"""
产品相关路由 - 单个/批量入库、查询、删除
"""
import time
import logging
from typing import List, Optional
from fastapi import APIRouter, UploadFile, File, Form, HTTPException, Request
from slowapi import Limiter
from slowapi.util import get_remote_address

from dependencies import (
    get_clip_service,
    get_milvus_service,
    get_product_service,
    get_image_processor,
    get_rembg_service,
    get_image_optimizer,
    get_minio_service
)
from services.product_helper import process_single_product_images
from services.precheck_service import precheck_ingest
from services.retry_utils import delete_milvus_with_retry, delete_minio_with_retry
from services.transaction_service import IngestTransaction
from services.enhanced_ingest import ingest_product_with_transaction
from services.cache_service import consistency_check_cache, product_list_cache
from utils.error_handler import ErrorHandler
from utils.response import build_success_response, build_error_response, paginated_response
from config.constants import (
    MAX_BATCH_INGEST_SIZE, 
    MAX_BATCH_DELETE_SIZE,
    RATE_LIMIT_PRODUCT_INGEST,
    RATE_LIMIT_BATCH_INGEST,
    MINIO_PATH_PREFIX
)

logger = logging.getLogger(__name__)

# 创建路由器
router = APIRouter(prefix="/api/v1", tags=["产品"])

# 请求限流
limiter = Limiter(key_func=get_remote_address)


# ==================== 单个产品入库 ====================

@router.post("/product/ingest")
@limiter.limit(RATE_LIMIT_PRODUCT_INGEST)
async def ingest_product(
    request: Request,
    product_code: str = Form(...),
    name: Optional[str] = Form(None),
    spec: Optional[str] = Form(None),
    category: Optional[str] = Form(None),
    files: List[UploadFile] = File(...),
    remove_bg: bool = Form(False)
):
    """
    单个产品入库接口
    
    Args:
        product_code: 产品编码
        name: 产品名称
        spec: 规格（可选）
        category: 分类（可选）
        files: 图片文件列表
        remove_bg: 是否移除背景
    
    Returns:
        入库结果统计
    """
    start_time = time.time()

    try:
        if not files:
            raise HTTPException(status_code=400, detail="至少需要上传一张图片")

        # 入库前预检查
        precheck_result = precheck_ingest(product_code, files)
        if not precheck_result["passed"]:
            raise HTTPException(
                status_code=503,
                detail={
                    "message": "系统预检查失败，无法执行入库操作",
                    "checks": precheck_result
                }
            )

        logger.info(f"产品入库请求: product_code={product_code}, name={name}, spec={spec}, category={category}, 图片数量={len(files)}, 移除背景={remove_bg}")
        
        # Transactional ingestion (atomic with rollback)
        result = await ingest_product_with_transaction(
            product_code=product_code,
            name=name,
            spec=spec,
            category=category,
            files=files,
            remove_bg=remove_bg
        )
        
        elapsed = time.time() - start_time
        return build_success_response(
            message=f"产品 {product_code} 入库成功",
            product_code=product_code,
            ingested_images=result["ingested_images"],
            elapsed_seconds=round(elapsed, 2)
        )

    except HTTPException:
        raise  # 让全局异常处理器处理


# ==================== 批量产品入库 ====================

@router.post("/products/batch-ingest")
@limiter.limit(RATE_LIMIT_BATCH_INGEST)
async def batch_ingest_products(
    request: Request,
    products_json: str = Form(...),
    files_map: str = Form(...),
    files: List[UploadFile] = File(...),
    remove_bg: bool = Form(False)
):
    """
    批量产品入库接口
    
    Args:
        products_json: JSON 数组，如 [{"code":"P001","name":"产品1","spec":"规格1","category":"分类1"},...]
        files_map: JSON 对象，如 {"P001":[0,1],"P002":[2]} 表示 P001 对应 files[0]和files[1]
        files: 所有图片文件列表
        remove_bg: 是否移除背景
    """
    import json
    
    clip_service = get_clip_service()
    milvus_service = get_milvus_service()
    product_service = get_product_service()
    image_processor = get_image_processor()
    rembg_service = get_rembg_service()
    image_optimizer = get_image_optimizer()
    
    start_time = time.time()
    
    try:
        # 解析参数
        products = json.loads(products_json)
        files_mapping = json.loads(files_map)
        
        if not isinstance(products, list) or len(products) == 0:
            raise HTTPException(status_code=400, detail="products_json 必须是非空数组")
        
        if len(products) > MAX_BATCH_INGEST_SIZE:
            raise HTTPException(status_code=400, detail=f"单次最多导入{MAX_BATCH_INGEST_SIZE}个产品")
        
        # 入库前预检查（检查所有产品的文件）
        total_files = len(files)
        precheck_result = precheck_ingest("batch_import", files)
        if not precheck_result["passed"]:
            raise HTTPException(
                status_code=503,
                detail={
                    "message": "系统预检查失败，无法执行批量入库操作",
                    "checks": precheck_result
                }
            )
        
        results = {
            "success": [],
            "failed": [],
            "total_images": 0,
            "total_success_images": 0,
            "total_failed_images": 0
        }
        
        # 逐个处理产品（复用单个产品入库逻辑）
        for product_info in products:
            code = product_info.get("code") or product_info.get("product_code")
            name = product_info.get("name")
            spec = product_info.get("spec", "")
            category = product_info.get("category", "")
            
            if not code or not name:
                results["failed"].append({
                    "product_code": code,
                    "error": "缺少必填字段 code 或 name"
                })
                continue
            
            # 获取该产品的文件索引
            file_indices = files_mapping.get(code, [])
            if not file_indices:
                results["failed"].append({
                    "product_code": code,
                    "error": "未找到对应的图片文件"
                })
                continue
            
            # 提取该产品的文件
            product_files = [files[idx] for idx in file_indices if idx < len(files)]
            
            if not product_files:
                results["failed"].append({
                    "product_code": code,
                    "error": "有效的图片文件为空"
                })
                continue
            
            # 保存产品信息（关键步骤，不可省略）
            try:
                product_service.upsert_product(
                    product_code=code,
                    name=name,
                    spec=spec,
                    category=category
                )
            except Exception as e:
                results["failed"].append({
                    "product_code": code,
                    "error": f"保存产品信息失败: {str(e)}"
                })
                logger.error(f"批量入库保存产品信息失败 - {code}: {str(e)}")
                continue
            
            # 调用辅助函数处理图片
            result = await process_single_product_images(code, product_files, remove_bg)
            
            if result["success_count"] > 0:
                results["success"].append({
                    "product_code": code,
                    "success_count": result["success_count"],
                    "fail_count": result["fail_count"]
                })
                results["total_success_images"] += result["success_count"]
                results["total_failed_images"] += result["fail_count"]
            else:
                results["failed"].append({
                    "product_code": code,
                    "error": "所有图片入库失败",
                    "details": result["errors"]
                })
            
            results["total_images"] += len(product_files)
        
        total_time = int((time.time() - start_time) * 1000)
        
        return build_success_response(
            message=f"批量入库完成: 成功 {len(results['success'])} 个产品, 失败 {len(results['failed'])} 个",
            results=results,
            total_time_ms=total_time
        )
        
    except json.JSONDecodeError:
        raise HTTPException(status_code=400, detail="JSON 格式错误")
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"批量入库异常: {str(e)}", exc_info=True)
        return build_error_response(
            message=ErrorHandler.format_error(error=e, context="批量产品入库"),
            error_code="BATCH_INGEST_FAILED"
        )


# ==================== 产品查询 ====================

@router.get("/product/{product_code}")
async def get_product(product_code: str):
    """获取产品详情"""
    product_service = get_product_service()
    
    product = product_service.get_product(product_code)
    if not product:
        raise HTTPException(status_code=404, detail="产品不存在")

    images = product_service.get_product_images(product_code)
    return build_success_response(
        message="获取产品详情成功",
        product=product,
        images=images
    )


@router.put("/product/{product_code}")
async def update_product(
    product_code: str,
    name: Optional[str] = Form(None),
    spec: Optional[str] = Form(None),
    category: Optional[str] = Form(None)
):
    """
    更新产品信息
    
    Args:
        product_code: 产品编码（路径参数）
        name: 产品名称（可选）
        spec: 规格（可选）
        category: 分类（可选）
    
    Returns:
        更新结果
    """
    product_service = get_product_service()
    
    # 验证产品存在
    product = product_service.get_product(product_code)
    if not product:
        raise HTTPException(status_code=404, detail=f"产品 {product_code} 不存在")
    
    # 构建更新字段
    updates = {}
    if name is not None:
        updates['name'] = name
    if spec is not None:
        updates['spec'] = spec
    if category is not None:
        updates['category'] = category
    
    if not updates:
        raise HTTPException(
            status_code=400, 
            detail="请提供至少一个要更新的字段（name, spec, category）"
        )
    
    # 执行更新
    try:
        product_service.update_product(product_code, **updates)
        
        return build_success_response(
            message=f"产品 {product_code} 信息更新成功",
            product_code=product_code,
            updated_fields=list(updates.keys())
        )
    except Exception as e:
        logger.error(f"更新产品信息失败: {str(e)}", exc_info=True)
        return build_error_response(
            message=f"更新失败: {str(e)}",
            error_code="UPDATE_PRODUCT_FAILED"
        )


@router.get("/products")
async def list_products(category: Optional[str] = None, page: int = 1, page_size: int = 20):
    """Product list query with full database information"""
    product_service = get_product_service()
    
    if page < 1:
        raise HTTPException(status_code=400, detail="page 必须大于 0")
    if not (1 <= page_size <= 100):
        raise HTTPException(status_code=400, detail="page_size 必须在 1-100 之间")

    products = product_service.list_products(category=category, page=page, page_size=page_size)
    total = product_service.count_products(category=category)
    
    # Enrich each product with additional info
    enriched_products = []
    for product in products:
        product_code = product['product_code']
        
        # Get image count from MySQL
        images = product_service.get_product_images(product_code)
        image_count = len(images)
        
        # Get Milvus IDs
        milvus_ids = [img['milvus_id'] for img in images if img.get('milvus_id')]
        
        # Get MinIO files
        minio_files = [img['image_path'] for img in images if img.get('image_path') and img['image_path'].startswith('minio://')]
        
        # Determine data status
        has_mysql = image_count > 0
        has_milvus = len(milvus_ids) > 0
        has_minio = len(minio_files) > 0
        
        if has_mysql and has_milvus and has_minio:
            data_status = 'complete'
        elif has_mysql or has_milvus or has_minio:
            data_status = 'incomplete'
        else:
            data_status = 'unknown'
        
        enriched_products.append({
            **product,
            'image_count': image_count,
            'milvus_ids': milvus_ids,
            'minio_files': minio_files,
            'data_status': data_status
        })

    return paginated_response(
        items=enriched_products,
        total=total,
        page=page,
        page_size=page_size,
        message="查询产品列表成功"
    )


@router.get("/stats")
async def get_stats():
    """获取系统统计信息"""
    product_service = get_product_service()
    
    stats = {
        "product_count": product_service.count_products(),
        "image_count": product_service.count_images(),
        "search_count": product_service.count_searches()
    }
    return build_success_response(
        message="获取系统统计成功",
        stats=stats
    )


@router.get("/products/data-consistency")
async def check_data_consistency():
    """
    检查数据一致性，找出 MySQL、MinIO、Milvus 中的残缺数据
    
    Returns:
        {
            "mysql_only": [...],  # 只在 MySQL 中存在的产品
            "milvus_only": [...], # 只在 Milvus 中存在的向量
            "minio_orphans": [...], # MinIO 中的孤儿文件
            "complete_products": [...] # 完整的产品（三个存储都有）
        }
    """
    # 尝试从缓存获取
    cache_key = "data_consistency_check"
    cached_result = consistency_check_cache.get(cache_key)
    if cached_result is not None:
        logger.info(" 使用缓存的一致性检查结果")
        return cached_result
    
    product_service = get_product_service()
    milvus_service = get_milvus_service()
    minio_service = get_minio_service()
    
    try:
        # 1. 获取 MySQL 中的所有产品和图片
        mysql_products = product_service._execute_query(
            "SELECT DISTINCT product_code FROM product",
            dictionary=True
        )
        mysql_product_codes = set([p['product_code'] for p in mysql_products])
        
        mysql_images = product_service._execute_query(
            "SELECT product_code, image_path, milvus_id FROM product_image",
            dictionary=True
        )
        mysql_image_paths = set([img['image_path'] for img in mysql_images])
        mysql_milvus_ids = set([img['milvus_id'] for img in mysql_images if img['milvus_id']])
        
        # 2. 获取 Milvus 中的所有向量
        milvus_stats = milvus_service.get_stats()
        milvus_vector_count = milvus_stats.get('vector_count', 0)
        
        # 查询 Milvus 中所有的 product_code
        milvus_product_codes = set()
        if milvus_vector_count > 0 and milvus_service.collection:
            try:
                # 加载集合
                milvus_service.collection.load()
                
                # 查询所有不同的 product_code
                results = milvus_service.collection.query(
                    expr="id >= 0",
                    output_fields=["product_code"],
                    limit=milvus_vector_count
                )
                milvus_product_codes = set([r['product_code'] for r in results])
            except Exception as e:
                logger.warning(f"查询 Milvus product_code 失败: {str(e)}")
        
        # 3. 获取 MinIO 中的所有对象
        minio_objects = []
        minio_product_codes = set()
        if minio_service:
            try:
                objects = list(minio_service.client.list_objects(
                    minio_service.bucket_name,
                    recursive=True
                ))
                for obj in objects:
                    minio_objects.append(obj.object_name)
                    # 从路径提取 product_code (格式: product_code/filename)
                    parts = obj.object_name.split('/')
                    if len(parts) >= 2:
                        minio_product_codes.add(parts[0])
            except Exception as e:
                logger.warning(f"查询 MinIO 对象失败: {str(e)}")
        
        # 4. 分析数据一致性
        # MySQL 中有但 Milvus 中没有的产品
        mysql_only_products = mysql_product_codes - milvus_product_codes
        
        # Milvus 中有但 MySQL 中没有的产品
        milvus_only_products = milvus_product_codes - mysql_product_codes
        
        # MinIO 中有但 MySQL 中没有的产品
        minio_only_products = minio_product_codes - mysql_product_codes
        
        # 完整的产品（三个存储都有）
        complete_products = mysql_product_codes & milvus_product_codes & minio_product_codes
        
        # 5. 构建详细报告
        result = {
            "summary": {
                "total_mysql_products": len(mysql_product_codes),
                "total_milvus_products": len(milvus_product_codes),
                "total_minio_products": len(minio_product_codes),
                "complete_products": len(complete_products),
                "mysql_only_count": len(mysql_only_products),
                "milvus_only_count": len(milvus_only_products),
                "minio_only_count": len(minio_only_products)
            },
            "mysql_only": [
                {
                    "product_code": code,
                    "issue": "产品在 MySQL 中存在，但 Milvus 和 MinIO 中缺失",
                    "severity": "high"
                }
                for code in mysql_only_products
            ],
            "milvus_only": [
                {
                    "product_code": code,
                    "issue": "向量在 Milvus 中存在，但 MySQL 和 MinIO 中缺失",
                    "severity": "high"
                }
                for code in milvus_only_products
            ],
            "minio_only": [
                {
                    "product_code": code,
                    "issue": "图片在 MinIO 中存在，但 MySQL 和 Milvus 中缺失",
                    "severity": "medium"
                }
                for code in minio_only_products
            ],
            "complete_products": [
                {
                    "product_code": code,
                    "status": "complete"
                }
                for code in complete_products
            ]
        }
        
        # 缓存结果（5分钟）
        consistency_check_cache.set(cache_key, result, ttl=300)
        logger.info(f" 一致性检查结果已缓存，TTL=300s")
        
        return build_success_response(
            message="数据一致性检查完成",
            **result  # 展开 result 字典
        )
        
    except Exception as e:
        logger.error(f"数据一致性检查失败: {str(e)}", exc_info=True)
        return build_error_response(
            message=f"检查失败: {str(e)}",
            error_code="CONSISTENCY_CHECK_FAILED"
        )


@router.get("/products/orphan-data")
async def query_orphan_data():
    """
    Query orphan data across all storage systems
    
    Returns:
        {
            "mysql_orphans": count,
            "milvus_orphans": count,
            "minio_orphans": count,
            "mysql_orphan_details": [...],
            "milvus_orphan_details": [...],
            "minio_orphan_details": [...]
        }
    """
    # 尝试从缓存获取
    cache_key = "orphan_data_query"
    cached_result = consistency_check_cache.get(cache_key)
    if cached_result is not None:
        logger.info(" 使用缓存的孤儿数据查询结果")
        return cached_result
    
    product_service = get_product_service()
    milvus_service = get_milvus_service()
    minio_service = get_minio_service()
    
    try:
        # 1. Get all MySQL products
        mysql_products = product_service._execute_query(
            "SELECT DISTINCT product_code FROM product",
            dictionary=True
        )
        mysql_product_codes = set([p['product_code'] for p in mysql_products])
        
        # Get MySQL images with details
        mysql_images = product_service._execute_query(
            "SELECT pi.*, p.name FROM product_image pi LEFT JOIN product p ON pi.product_code = p.product_code",
            dictionary=True
        )
        
        # 2. Get Milvus vectors
        milvus_stats = milvus_service.get_stats()
        milvus_vector_count = milvus_stats.get('vector_count', 0)
        
        milvus_vectors = []
        if milvus_vector_count > 0 and milvus_service.collection:
            try:
                milvus_service.collection.load()
                results = milvus_service.collection.query(
                    expr="id >= 0",
                    output_fields=["id", "product_code"],
                    limit=milvus_vector_count
                )
                milvus_vectors = results
            except Exception as e:
                logger.warning(f"Failed to query Milvus vectors: {str(e)}")
        
        milvus_product_codes = set([v['product_code'] for v in milvus_vectors])
        milvus_ids_in_db = set([img['milvus_id'] for img in mysql_images if img['milvus_id']])
        milvus_ids_in_milvus = set([v['id'] for v in milvus_vectors])
        
        # 3. Get MinIO objects
        minio_objects = []
        if minio_service:
            try:
                objects = list(minio_service.client.list_objects(
                    minio_service.bucket_name,
                    recursive=True
                ))
                for obj in objects:
                    minio_objects.append({
                        'object_name': obj.object_name,
                        'size': obj.size
                    })
            except Exception as e:
                logger.warning(f"Failed to query MinIO objects: {str(e)}")
        
        mysql_image_paths = set([img['image_path'].replace(MINIO_PATH_PREFIX, '') 
                                 for img in mysql_images if img['image_path']])
        
        # 4. Find orphans
        # MySQL orphans: images referencing non-existent products
        mysql_orphan_details = [
            {
                "product_code": img['product_code'],
                "name": img['name'] or 'Unknown',
                "image_path": img['image_path'],
                "reason": "Product not found in product table"
            }
            for img in mysql_images
            if img['product_code'] not in mysql_product_codes
        ]
        
        # Milvus orphans: vectors not referenced by any MySQL record
        milvus_orphan_details = [
            {
                "product_code": v['product_code'],
                "milvus_id": v['id'],
                "reason": "No corresponding MySQL record"
            }
            for v in milvus_vectors
            if v['id'] not in milvus_ids_in_db
        ]
        
        # MinIO orphans: files not referenced by any MySQL record
        minio_orphan_details = [
            {
                "object_name": obj['object_name'],
                "size": obj['size'],
                "reason": "No corresponding MySQL record"
            }
            for obj in minio_objects
            if obj['object_name'] not in mysql_image_paths
        ]
        
        result = {
            "mysql_orphans": len(mysql_orphan_details),
            "milvus_orphans": len(milvus_orphan_details),
            "minio_orphans": len(minio_orphan_details),
            "mysql_orphan_details": mysql_orphan_details,
            "milvus_orphan_details": milvus_orphan_details,
            "minio_orphan_details": minio_orphan_details
        }
        
        # 缓存结果（5分钟）
        consistency_check_cache.set(cache_key, result, ttl=300)
        logger.info(f" 孤儿数据查询结果已缓存，TTL=300s")
        
        return build_success_response(
            message="孤儿数据查询完成",
            mysql_orphans=result["mysql_orphans"],
            milvus_orphans=result["milvus_orphans"],
            minio_orphans=result["minio_orphans"],
            mysql_orphan_details=result["mysql_orphan_details"],
            milvus_orphan_details=result["milvus_orphan_details"],
            minio_orphan_details=result["minio_orphan_details"]
        )
        
    except Exception as e:
        logger.error(f"孤儿数据查询失败: {str(e)}", exc_info=True)
        import traceback
        logger.error(f"详细堆栈信息:\n{traceback.format_exc()}")
        return build_error_response(
            message=f"查询失败: {str(e)}",
            error_code="ORPHAN_QUERY_FAILED"
        )


@router.post("/products/clean-orphans")
async def clean_orphan_data(confirm: bool = Form(False)):
    """
    Clean all orphan data across MySQL, Milvus, and MinIO
    
    Two-step confirmation mechanism:
    1. First call (confirm=false): Returns preview of what will be deleted
    2. Second call (confirm=true): Executes the cleanup
    
    Returns:
        Step 1: { requires_confirmation: true, orphan_summary: {...} }
        Step 2: { success: true, message: "...", cleaned: {...} }
    """
    product_service = get_product_service()
    milvus_service = get_milvus_service()
    minio_service = get_minio_service()
    image_processor = get_image_processor()
    
    # Step 1: Return preview if not confirmed
    if not confirm:
        try:
            # Query orphan data (reuse logic from query_orphan_data)
            mysql_products = product_service._execute_query(
                "SELECT DISTINCT product_code FROM product",
                dictionary=True
            )
            mysql_product_codes = set([p['product_code'] for p in mysql_products])
            
            mysql_images = product_service._execute_query(
                "SELECT * FROM product_image",
                dictionary=True
            )
            
            # Count MySQL orphans
            mysql_orphan_codes = set([img['product_code'] for img in mysql_images 
                                      if img['product_code'] not in mysql_product_codes])
            
            # Count Milvus orphans
            milvus_stats = milvus_service.get_stats()
            milvus_vector_count = milvus_stats.get('vector_count', 0)
            milvus_orphan_count = 0
            
            if milvus_vector_count > 0 and milvus_service.collection:
                try:
                    milvus_service.collection.load()
                    results = milvus_service.collection.query(
                        expr="id >= 0",
                        output_fields=["id", "product_code"],
                        limit=milvus_vector_count
                    )
                    
                    valid_product_codes = set([img['product_code'] for img in mysql_images])
                    milvus_orphan_count = len([v for v in results 
                                              if v['product_code'] not in valid_product_codes])
                except Exception:
                    pass
            
            # Count MinIO orphans
            minio_orphan_count = 0
            if minio_service:
                try:
                    objects = list(minio_service.client.list_objects(
                        minio_service.bucket_name,
                        recursive=True
                    ))
                    
                    valid_paths = set([img['image_path'].replace('minio://visual-search/', '') 
                                      for img in mysql_images if img['image_path']])
                    
                    minio_orphan_count = len([obj for obj in objects 
                                             if obj.object_name not in valid_paths])
                except Exception:
                    pass
            
            return build_success_response(
                message="请确认清理操作",
                requires_confirmation=True,
                orphan_summary={
                    "mysql_count": len(mysql_orphan_codes),
                    "milvus_count": milvus_orphan_count,
                    "minio_count": minio_orphan_count,
                    "total": len(mysql_orphan_codes) + milvus_orphan_count + minio_orphan_count
                }
            )
        except Exception as e:
            logger.error(f"Failed to query orphan preview: {str(e)}", exc_info=True)
            return build_error_response(
                message=f"查询预览失败: {str(e)}",
                error_code="ORPHAN_PREVIEW_FAILED"
            )
    
    # Step 2: Execute cleanup (original logic)
    cleaned = {
        "mysql_count": 0,
        "milvus_count": 0,
        "minio_count": 0
    }
    
    try:
        # 1. Get all MySQL products
        mysql_products = product_service._execute_query(
            "SELECT DISTINCT product_code FROM product",
            dictionary=True
        )
        mysql_product_codes = set([p['product_code'] for p in mysql_products])
        
        # Get MySQL images
        mysql_images = product_service._execute_query(
            "SELECT * FROM product_image",
            dictionary=True
        )
        
        # 2. Clean MySQL orphans (images referencing non-existent products)
        mysql_orphan_codes = set([img['product_code'] for img in mysql_images 
                                  if img['product_code'] not in mysql_product_codes])
        
        for code in mysql_orphan_codes:
            try:
                product_service._execute_query(
                    "DELETE FROM product_image WHERE product_code = %s",
                    (code,)
                )
                cleaned["mysql_count"] += 1
                logger.info(f" Deleted MySQL orphan: {code}")
            except Exception as e:
                logger.error(f" Failed to delete MySQL orphan {code}: {str(e)}")
        
        # 3. Clean Milvus orphans
        milvus_stats = milvus_service.get_stats()
        milvus_vector_count = milvus_stats.get('vector_count', 0)
        
        if milvus_vector_count > 0 and milvus_service.collection:
            try:
                milvus_service.collection.load()
                
                # Get all vectors
                results = milvus_service.collection.query(
                    expr="id >= 0",
                    output_fields=["id", "product_code"],
                    limit=milvus_vector_count
                )
                
                # Get valid product codes from MySQL images
                valid_product_codes = set([img['product_code'] for img in mysql_images])
                
                # Find orphan vectors
                orphan_ids = [v['id'] for v in results 
                             if v['product_code'] not in valid_product_codes]
                
                if orphan_ids:
                    # Delete in batches
                    batch_size = 100
                    for i in range(0, len(orphan_ids), batch_size):
                        batch_ids = orphan_ids[i:i + batch_size]
                        expr = f"id in {batch_ids}"
                        milvus_service.collection.delete(expr)
                        cleaned["milvus_count"] += len(batch_ids)
                    
                    milvus_service.collection.flush()
                    logger.info(f" Deleted {len(orphan_ids)} Milvus orphan vectors")
                    
            except Exception as e:
                logger.error(f" Failed to clean Milvus orphans: {str(e)}")
        
        # 4. Clean MinIO orphans
        if minio_service:
            try:
                # Get all MinIO objects
                objects = list(minio_service.client.list_objects(
                    minio_service.bucket_name,
                    recursive=True
                ))
                
                # Get valid image paths from MySQL
                valid_paths = set([img['image_path'].replace(MINIO_PATH_PREFIX, '') 
                                  for img in mysql_images if img['image_path']])
                
                # Find and delete orphan objects
                for obj in objects:
                    if obj.object_name not in valid_paths:
                        try:
                            minio_service.client.remove_object(
                                minio_service.bucket_name,
                                obj.object_name
                            )
                            cleaned["minio_count"] += 1
                            logger.info(f" Deleted MinIO orphan: {obj.object_name}")
                        except Exception as e:
                            logger.error(f" Failed to delete MinIO orphan {obj.object_name}: {str(e)}")
                            
            except Exception as e:
                logger.error(f" Failed to clean MinIO orphans: {str(e)}")
        
        total_cleaned = sum(cleaned.values())
        message = f"清理完成: MySQL {cleaned['mysql_count']} 条, Milvus {cleaned['milvus_count']} 条, MinIO {cleaned['minio_count']} 条"
        
        logger.info(f"🎉 Orphan data cleanup completed: {message}")
        
        # 清除相关缓存
        consistency_check_cache.delete("orphan_data_query")
        consistency_check_cache.delete("data_consistency_check")
        logger.info(" 已清除孤儿数据和相关缓存")
        
        return build_success_response(
            message=message,
            cleaned=cleaned
        )
        
    except Exception as e:
        logger.error(f"Orphan data cleanup failed: {str(e)}", exc_info=True)
        return build_error_response(
            message=ErrorHandler.format_error(error=e, context="清理孤儿数据"),
            error_code="CLEAN_ORPHANS_FAILED"
        )


# ==================== 单个孤儿数据删除 ====================

@router.delete("/products/orphan/{orphan_type}/{identifier}")
async def delete_single_orphan(orphan_type: str, identifier: str):
    """
    Delete a single orphan record
    
    Args:
        orphan_type: Type of orphan (mysql, milvus, minio)
        identifier: Identifier (product_code for mysql/milvus, object_name for minio)
    """
    product_service = get_product_service()
    milvus_service = get_milvus_service()
    image_processor = get_image_processor()
    
    try:
        if orphan_type == "mysql":
            # Delete MySQL orphan image records
            product_service._execute_query(
                "DELETE FROM product_image WHERE product_code = %s",
                (identifier,)
            )
            logger.info(f" Deleted MySQL orphan: {identifier}")
            return build_success_response(
                message=f"已删除 MySQL 孤儿记录: {identifier}",
                type="mysql",
                identifier=identifier
            )
            
        elif orphan_type == "milvus":
            # Delete Milvus orphan vector by ID
            milvus_id = int(identifier)
            if milvus_service.collection:
                expr = f"id == {milvus_id}"
                milvus_service.collection.delete(expr)
                milvus_service.collection.flush()
                logger.info(f" Deleted Milvus orphan vector: {milvus_id}")
                return build_success_response(
                    message=f"已删除 Milvus 孤儿向量: {milvus_id}",
                    type="milvus",
                    identifier=milvus_id
                )
            else:
                raise HTTPException(status_code=500, detail="Milvus collection not initialized")
                
        elif orphan_type == "minio":
            # Delete MinIO orphan file
            if image_processor.minio_service:
                # identifier is the full object name
                object_name = identifier
                image_processor.minio_service.client.remove_object(
                    image_processor.minio_service.bucket_name,
                    object_name
                )
                logger.info(f" Deleted MinIO orphan file: {object_name}")
                return build_success_response(
                    message=f"已删除 MinIO 孤儿文件: {object_name}",
                    type="minio",
                    identifier=object_name
                )
            else:
                raise HTTPException(status_code=500, detail="MinIO service not available")
        else:
            raise HTTPException(status_code=400, detail=f"Invalid orphan type: {orphan_type}")
            
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Failed to delete orphan {orphan_type}/{identifier}: {str(e)}", exc_info=True)
        return build_error_response(
            message=ErrorHandler.format_error(error=e, context=f"删除孤儿数据 ({orphan_type})"),
            error_code="DELETE_ORPHAN_FAILED"
        )


# ==================== 产品删除 ====================

@router.delete("/product/{product_code}")
async def delete_product(product_code: str):
    """删除产品及其所有图片（优化顺序：Milvus → MinIO → MySQL）"""
    milvus_service = get_milvus_service()
    product_service = get_product_service()
    image_processor = get_image_processor()
    
    # 前置检查：验证产品是否存在
    product = product_service.get_product(product_code)
    if not product:
        raise HTTPException(
            status_code=404,
            detail=f"产品 {product_code} 不存在"
        )
    
    cleanup_log = {"milvus": False, "storage": False, "mysql": False}
    try:
        images = product_service.get_product_images(product_code)
        milvus_ids = [img["milvus_id"] for img in images if img["milvus_id"]]
        
        # 1. 先删除 Milvus 向量（带重试，可恢复）
        if milvus_ids:
            try:
                delete_milvus_with_retry(milvus_service, milvus_ids)
                cleanup_log["milvus"] = True
                logger.info(f" Milvus 向量删除成功: {len(milvus_ids)} 个")
            except Exception as e:
                logger.error(f" Milvus 清理失败（已重试3次）: {str(e)}", exc_info=True)
                raise
        else:
            # 没有向量数据，也标记为成功
            cleanup_log["milvus"] = True
            logger.info(f"ℹ️ 产品 {product_code} 没有 Milvus 向量数据，跳过清理")

        # 2. 再删除 MinIO 图片（带重试，可恢复）
        storage_errors = []
        for img in images:
            try:
                delete_minio_with_retry(image_processor, img["image_path"])
            except Exception as e:
                storage_errors.append(f"{img['image_path']}: {str(e)}")
        
        if not storage_errors:
            cleanup_log["storage"] = True
            logger.info(" MinIO 图片删除成功")
        else:
            logger.warning(f" 部分存储文件清理失败: {'; '.join(storage_errors)}")
            raise Exception(f"MinIO 删除失败: {'; '.join(storage_errors)}")

        # 3. 最后删除 MySQL 记录（不可逆操作放最后）
        try:
            product_service.delete_product(product_code)
            cleanup_log["mysql"] = True
            logger.info(f" MySQL 记录删除成功: {product_code}")
        except Exception as e:
            logger.error(f" MySQL 删除失败: {str(e)}")
            raise

        # 检查最终状态
        all_cleaned = all(cleanup_log.values())
        message = f"产品 {product_code} 删除成功" if all_cleaned else f"产品 {product_code} 部分删除成功，请检查日志"
        
        return build_success_response(
            message=message,
            deleted_images=len(images),
            cleanup_status=cleanup_log
        )

    except Exception as e:
        logger.error(f"删除产品异常: {str(e)}", exc_info=True)
        return build_error_response(
            message=ErrorHandler.format_error(error=e, context="删除产品"),
            error_code="DELETE_PRODUCT_FAILED"
        )


@router.delete("/products/batch")
async def batch_delete_products(product_codes: str = Form(...)):
    """
    批量删除产品
    
    Args:
        product_codes: 逗号分隔的产品编码列表，如 "P001,P002,P003"
    """
    milvus_service = get_milvus_service()
    product_service = get_product_service()
    image_processor = get_image_processor()
    
    try:
        codes = [code.strip() for code in product_codes.split(",") if code.strip()]
        
        if not codes:
            raise HTTPException(status_code=400, detail="请提供至少一个产品编码")
        
        if len(codes) > MAX_BATCH_DELETE_SIZE:
            raise HTTPException(status_code=400, detail=f"单次最多删除{MAX_BATCH_DELETE_SIZE}个产品")
        
        results = {
            "success": [],
            "failed": [],
            "total_deleted_images": 0
        }
        
        for code in codes:
            try:
                # 获取产品信息
                images = product_service.get_product_images(code)
                milvus_ids = [img["milvus_id"] for img in images if img["milvus_id"]]
                
                # 1. 先删除 Milvus 向量（带重试）
                if milvus_ids:
                    delete_milvus_with_retry(milvus_service, milvus_ids)
                
                # 2. 再删除 MinIO 图片（带重试）
                for img in images:
                    delete_minio_with_retry(image_processor, img["image_path"])
                
                # 3. 最后删除 MySQL 记录（不可逆操作放最后）
                product_service.delete_product(code)
                
                results["success"].append(code)
                results["total_deleted_images"] += len(images)
                
            except Exception as e:
                results["failed"].append({
                    "product_code": code,
                    "error": str(e)
                })
                logger.error(f"批量删除失败 - {code}: {str(e)}")
        
        return build_success_response(
            message=f"批量删除完成: 成功 {len(results['success'])} 个, 失败 {len(results['failed'])} 个",
            **results  # 展开 results 字典
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"批量删除异常: {str(e)}", exc_info=True)
        return build_error_response(
            message=ErrorHandler.format_error(error=e, context="批量删除产品"),
            error_code="BATCH_DELETE_FAILED"
        )


# ==================== 重试接口 ====================

@router.post("/product/{product_code}/cleanup-failed")
async def cleanup_failed_product(product_code: str):
    """
    清理失败产品的残留数据
    
    当产品入库失败时，可能产生孤儿数据。此接口用于清理这些残留数据，
    以便用户可以重新上传该产品。
    
    Args:
        product_code: 产品编码
    
    Returns:
        清理结果和下一步操作提示
    """
    try:
        logger.info(f"🧹 清理失败产品的残留数据: {product_code}")
        
        # 1. 检查是否有残留数据
        existing_images = product_service.get_product_images(product_code)
        if not existing_images:
            return build_success_response(
                message=f"产品 {product_code} 没有残留数据",
                product_code=product_code,
                has_orphan_data=False,
                next_step="re-upload"
            )
        
        # 2. 清理 Milvus 向量
        milvus_ids = [img["milvus_id"] for img in existing_images if img["milvus_id"]]
        if milvus_ids:
            try:
                delete_milvus_with_retry(milvus_service, milvus_ids)
                logger.info(f" 已清理 Milvus 向量: {len(milvus_ids)} 个")
            except Exception as e:
                logger.warning(f" 清理 Milvus 失败: {str(e)}")
        
        # 3. 清理 MinIO 图片
        minio_cleaned = 0
        for img in existing_images:
            try:
                image_processor.delete_image(img["image_path"])
                minio_cleaned += 1
            except Exception as e:
                logger.warning(f" 清理 MinIO 失败: {str(e)}")
        
        # 4. 清理 MySQL 记录
        try:
            product_service._execute_update(
                "DELETE FROM product_image WHERE product_code = %s",
                (product_code,)
            )
            logger.info(f" 已清理 MySQL 记录: {len(existing_images)} 条")
        except Exception as e:
            logger.error(f" 清理 MySQL 失败: {str(e)}")
            raise HTTPException(
                status_code=500,
                detail=f"清理数据库记录失败: {str(e)}"
            )
        
        # 5. 返回清理结果
        return build_success_response(
            message=f"产品 {product_code} 的残留数据已清理完成",
            product_code=product_code,
            has_orphan_data=True,
            cleaned_summary={
                "milvus_count": len(milvus_ids),
                "minio_count": minio_cleaned,
                "mysql_count": len(existing_images)
            },
            next_step="re-upload",
            next_step_message="请重新上传该产品的图片"
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"清理失败产品数据异常: {str(e)}", exc_info=True)
        return build_error_response(
            message=f"清理失败: {str(e)}",
            error_code="CLEANUP_FAILED"
        )
