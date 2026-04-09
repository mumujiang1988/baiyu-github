"""
搜索相关路由 - 图像检索、文本搜索、组合搜索
"""
import time
import logging
from typing import List, Optional
from fastapi import APIRouter, UploadFile, File, Form, HTTPException, Request
from pydantic import BaseModel
from slowapi import Limiter
from slowapi.util import get_remote_address

from dependencies import (
    get_clip_service,
    get_milvus_service,
    get_product_service,
    get_image_processor
)
from utils.error_handler import ErrorHandler

logger = logging.getLogger(__name__)

# 创建路由器
router = APIRouter(prefix="/api/v1", tags=["搜索"])

# 请求限流
limiter = Limiter(key_func=get_remote_address)


# ==================== 数据模型 ====================

class SearchResult(BaseModel):
    """搜索结果"""
    product_code: str
    product_name: str
    spec: Optional[str]
    category: Optional[str]
    similarity: float
    image_paths: List[str]


class SearchResponse(BaseModel):
    """搜索响应"""
    success: bool
    message: str
    results: List[SearchResult]
    search_time_ms: int


# ==================== 图像检索 ====================

@router.post("/search", response_model=SearchResponse)
@limiter.limit("10/minute")
async def search_image(
    request: Request,
    file: UploadFile = File(...),
    top_k: int = 10,
    aggregation: str = "max"
):
    """
    图像检索接口
    
    Args:
        file: 查询图片
        top_k: 返回数量 (1-100)
        aggregation: 聚合策略 (max/avg)
    
    Returns:
        相似产品列表
    """
    # 参数验证
    if not (1 <= top_k <= 100):
        raise HTTPException(status_code=400, detail="top_k 必须在 1-100 之间")
    if aggregation not in ["max", "avg"]:
        raise HTTPException(status_code=400, detail="aggregation 必须是 max 或 avg")

    clip_service = get_clip_service()
    milvus_service = get_milvus_service()
    product_service = get_product_service()
    image_processor = get_image_processor()
    
    start_time = time.time()

    try:
        image_bytes = await file.read()
        logger.info(f"检索请求: 文件大小 {len(image_bytes)} bytes")

        processed_image = image_processor.preprocess(image_bytes)
        embedding = clip_service.extract_features(processed_image)

        raw_results = milvus_service.search(query_vector=embedding, top_k=top_k * 3)
        aggregated_results = product_service.aggregate_results(
            raw_results=raw_results, aggregation=aggregation, top_k=top_k
        )

        results = []
        for item in aggregated_results:
            product_info = product_service.get_product(item["product_code"])
            if product_info:
                results.append(SearchResult(
                    product_code=item["product_code"],
                    product_name=product_info["name"],
                    spec=product_info.get("spec"),
                    category=product_info.get("category"),
                    similarity=item["similarity"],
                    image_paths=item["image_paths"]
                ))

        search_time = int((time.time() - start_time) * 1000)
        logger.info(f"检索完成: 找到 {len(results)} 个结果, 耗时 {search_time}ms")

        if results:
            image_hash = image_processor.compute_hash(image_bytes)
            product_service.log_search(
                query_hash=image_hash,
                top_product_code=results[0].product_code,
                similarity=results[0].similarity,
                search_time_ms=search_time,
                result_count=len(results)
            )

        return SearchResponse(
            success=True,
            message=f"检索完成，找到 {len(results)} 个相似产品",
            results=results,
            search_time_ms=search_time
        )

    except Exception as e:
        search_time = int((time.time() - start_time) * 1000)
        logger.error(f"检索失败: {str(e)}", exc_info=True)
        return SearchResponse(
            success=False,
            message=ErrorHandler.format_error(error=e, context="图片检索"),
            results=[],
            search_time_ms=search_time
        )


# ==================== 文本搜索 ====================

@router.post("/search/text")
@limiter.limit("20/minute")
async def search_by_text(
    request: Request,
    keyword: str = Form(...),
    category: Optional[str] = Form(None),
    top_k: int = Form(10)
):
    """
    文本关键词搜索
    
    使用jieba分词，支持中文模糊匹配。
    可选择按分类筛选。
    
    Args:
        keyword: 搜索关键词（必填）
        category: 分类筛选（可选）
        top_k: 返回数量（默认10）
    
    Returns:
        产品列表，按相关性排序
    """
    import jieba
    
    product_service = get_product_service()
    
    start_time = time.time()
    
    try:
        if not keyword or len(keyword.strip()) == 0:
            raise HTTPException(status_code=400, detail="请提供搜索关键词")
        
        # 分词
        keywords = list(jieba.cut(keyword.strip()))
        keywords = [kw.strip() for kw in keywords if kw.strip() and len(kw) > 1]
        
        if not keywords:
            raise HTTPException(status_code=400, detail="关键词太短")
        
        logger.info(f"文本搜索: keyword={keyword}, keywords={keywords}, category={category}")
        
        # 构建 SQL 查询
        if category:
            sql = """
                SELECT DISTINCT p.* FROM product p
                WHERE p.status = 1 AND p.category = %s
                AND (p.name LIKE %s OR p.spec LIKE %s OR p.product_code LIKE %s)
                ORDER BY p.created_at DESC
                LIMIT %s
            """
            like_pattern = f"%{keywords[0]}%"
            params = (category, like_pattern, like_pattern, like_pattern, top_k)
        else:
            sql = """
                SELECT DISTINCT p.* FROM product p
                WHERE p.status = 1
                AND (p.name LIKE %s OR p.spec LIKE %s OR p.product_code LIKE %s)
                ORDER BY p.created_at DESC
                LIMIT %s
            """
            like_pattern = f"%{keywords[0]}%"
            params = (like_pattern, like_pattern, like_pattern, top_k)
        
        # 执行查询
        products = product_service._execute_query(sql, params, dictionary=True)
        
        # 为每个产品获取图片
        results = []
        for product in products:
            images = product_service.get_product_images(product['product_code'])
            results.append({
                "product_code": product['product_code'],
                "product_name": product['name'],
                "spec": product.get('spec'),
                "category": product.get('category'),
                "similarity": 1.0,  # 文本搜索无相似度
                "image_paths": [img['image_path'] for img in images]
            })
        
        search_time = int((time.time() - start_time) * 1000)
        
        # 记录日志
        if results:
            product_service.log_search(
                query_hash=f"text:{keyword}",
                top_product_code=results[0]['product_code'],
                similarity=1.0,
                search_time_ms=search_time,
                result_count=len(results)
            )
        
        return {
            "success": True,
            "message": f"找到 {len(results)} 个产品",
            "results": results,
            "search_time_ms": search_time,
            "keyword": keyword,
            "keywords": keywords
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"文本搜索失败: {str(e)}", exc_info=True)
        return {
            "success": False,
            "message": ErrorHandler.format_error(error=e, context="文本搜索")
        }


# ==================== 组合搜索 ====================

@router.post("/search/hybrid")
@limiter.limit("10/minute")
async def hybrid_search(
    request: Request,
    file: Optional[UploadFile] = File(None),
    keyword: Optional[str] = Form(None),
    category: Optional[str] = Form(None),
    top_k: int = Form(10),
    image_weight: float = Form(0.7),  # 图像权重
    text_weight: float = Form(0.3)    # 文本权重
):
    """
    图像+文本组合搜索
    
    Args:
        file: 查询图片（可选）
        keyword: 搜索关键词（可选）
        category: 分类筛选
        top_k: 返回数量
        image_weight: 图像相似度权重 (0-1)
        text_weight: 文本匹配权重 (0-1)
    """
    import jieba
    
    clip_service = get_clip_service()
    milvus_service = get_milvus_service()
    product_service = get_product_service()
    image_processor = get_image_processor()
    
    start_time = time.time()
    
    try:
        if not file and not keyword:
            raise HTTPException(status_code=400, detail="请提供图片或关键词至少一项")
        
        image_results = []
        text_results = []
        
        # 1. 图像搜索
        if file:
            image_bytes = await file.read()
            processed_image = image_processor.preprocess(image_bytes)
            embedding = clip_service.extract_features(processed_image)
            
            raw_results = milvus_service.search(embedding=embedding, top_k=top_k * 2)
            
            if raw_results:
                aggregated = product_service.aggregate_results(raw_results, aggregation="max", top_k=top_k)
                image_results = aggregated
        
        # 2. 文本搜索
        if keyword:
            keywords = list(jieba.cut(keyword.strip()))
            keywords = [kw.strip() for kw in keywords if kw.strip() and len(kw) > 1]
            
            if keywords:
                like_pattern = f"%{keywords[0]}%"
                if category:
                    sql = """
                        SELECT DISTINCT p.product_code, p.name, p.spec, p.category
                        FROM product p
                        WHERE p.status = 1 AND p.category = %s
                        AND (p.name LIKE %s OR p.spec LIKE %s OR p.product_code LIKE %s)
                        LIMIT %s
                    """
                    params = (category, like_pattern, like_pattern, like_pattern, top_k * 2)
                else:
                    sql = """
                        SELECT DISTINCT p.product_code, p.name, p.spec, p.category
                        FROM product p
                        WHERE p.status = 1
                        AND (p.name LIKE %s OR p.spec LIKE %s OR p.product_code LIKE %s)
                        LIMIT %s
                    """
                    params = (like_pattern, like_pattern, like_pattern, top_k * 2)
                
                products = product_service._execute_query(sql, params, dictionary=True)
                
                for product in products:
                    text_results.append({
                        "product_code": product['product_code'],
                        "product_name": product['name'],
                        "spec": product.get('spec'),
                        "category": product.get('category'),
                        "similarity": 1.0,
                        "image_paths": []
                    })
        
        # 3. 合并结果（加权融合）
        merged_results = {}
        
        # 添加图像结果
        for result in image_results:
            code = result['product_code']
            merged_results[code] = {
                **result,
                'image_score': result['similarity'],
                'text_score': 0.0,
                'final_score': result['similarity'] * image_weight
            }
        
        # 添加/更新文本结果
        for result in text_results:
            code = result['product_code']
            if code in merged_results:
                # 已存在，更新分数
                merged_results[code]['text_score'] = 1.0
                merged_results[code]['final_score'] = (
                    merged_results[code]['image_score'] * image_weight +
                    1.0 * text_weight
                )
            else:
                # 新增
                merged_results[code] = {
                    **result,
                    'image_score': 0.0,
                    'text_score': 1.0,
                    'final_score': 1.0 * text_weight
                }
                # 获取图片
                images = product_service.get_product_images(code)
                merged_results[code]['image_paths'] = [img['image_path'] for img in images]
        
        # 按最终分数排序
        sorted_results = sorted(
            merged_results.values(),
            key=lambda x: x['final_score'],
            reverse=True
        )[:top_k]
        
        # 格式化输出
        final_results = []
        for result in sorted_results:
            final_results.append({
                "product_code": result['product_code'],
                "product_name": result['product_name'],
                "spec": result.get('spec'),
                "category": result.get('category'),
                "similarity": result['final_score'],
                "image_score": result['image_score'],
                "text_score": result['text_score'],
                "image_paths": result.get('image_paths', [])
            })
        
        search_time = int((time.time() - start_time) * 1000)
        
        return {
            "success": True,
            "message": f"组合搜索完成: 找到 {len(final_results)} 个产品",
            "results": final_results,
            "search_time_ms": search_time,
            "has_image": file is not None,
            "has_keyword": keyword is not None,
            "weights": {
                "image": image_weight,
                "text": text_weight
            }
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"组合搜索失败: {str(e)}", exc_info=True)
        return {
            "success": False,
            "message": ErrorHandler.format_error(error=e, context="组合搜索")
        }
