"""
Enhanced Product Ingest with Unified Transaction Support
Atomic ingestion with rollback (optimized order: external storage first, then database)
"""
import logging
from typing import List, Dict
from collections import defaultdict
from fastapi import UploadFile, HTTPException

from services.transaction_service import IngestTransaction
from dependencies import (
    get_clip_service,
    get_rembg_service,
    get_image_processor
)

logger = logging.getLogger(__name__)

# 常量定义
MAX_DUPLICATE_DISPLAY = 5  # 最多显示的重复图片数量


async def ingest_product_with_transaction(
    product_code: str,
    name: str,
    spec: str = None,
    category: str = None,
    files: List[UploadFile] = None,
    remove_bg: bool = False
) -> Dict:
    """
    Ingest product with transaction support (atomic + rollback)
    
    Args:
        product_code: Product code
        name: Product name
        spec: Product specification
        category: Product category
        files: List of image files
        remove_bg: Whether to remove background
    
    Returns:
        {
            "success": bool,
            "product_code": str,
            "ingested_images": int,
            "milvus_ids": List[int],
            "minio_paths": List[str]
        }
    """
    if not files:
        raise HTTPException(status_code=400, detail="At least one image file is required")
    
    # Get services
    clip_service = get_clip_service()
    rembg_service = get_rembg_service()
    image_processor = get_image_processor()
    
    # Create transaction manager (use simple mode without compensation)
    transaction = IngestTransaction(product_code, use_compensation=False)
    
    try:
        # Phase 0: Check if product already exists
        from dependencies import get_product_service
        product_service = get_product_service()
        existing_product = product_service.get_product(product_code)
        
        # Phase 1: Prepare all images - 优化版:批量查询去重
        logger.info(f"Phase 1: Preparing {len(files)} images for {product_code}")
        prepared_images = []
        duplicate_images = []  # 记录重复图片
        duplicate_product_codes = set()  # 记录重复图片所属的产品编码
        
        # Step 1: 读取所有图片并计算hash(不执行数据库查询)
        image_data_list = []
        for idx, file in enumerate(files):
            try:
                # Read image bytes
                image_bytes = await file.read()
                
                # Remove background if requested
                if remove_bg and rembg_service:
                    try:
                        image_bytes = rembg_service.remove_background(image_bytes)
                        logger.debug(f"Background removed for image {idx+1}")
                    except Exception as e:
                        logger.warning(f"Background removal failed for image {idx+1}: {str(e)}")
                
                # Extract features ONCE
                processed_image = image_processor.preprocess(image_bytes)
                embedding = clip_service.extract_features(processed_image)
                
                # Compute hash
                image_hash = image_processor.compute_hash(image_bytes)
                
                image_data_list.append({
                    'idx': idx,
                    'file': file,
                    'image_bytes': image_bytes,
                    'embedding': embedding,
                    'image_hash': image_hash
                })
                
                logger.debug(f"Image {idx+1}/{len(files)} processed: {file.filename}")
                
            except Exception as e:
                logger.error(f"Failed to process image {idx+1}: {str(e)}")
                raise
        
        # Step 2: 批量检查哪些hash已存在(只需1次SQL查询)
        if image_data_list:
            all_hashes = [data['image_hash'] for data in image_data_list]
            placeholders = ','.join(['%s'] * len(all_hashes))
            existing_records = product_service._execute_query(
                f"SELECT image_hash, product_code FROM product_image WHERE image_hash IN ({placeholders})",
                tuple(all_hashes),
                dictionary=True
            )
            
            # 构建 hash -> product_codes 映射
            hash_to_products = defaultdict(set)
            for record in existing_records:
                hash_to_products[record['image_hash']].add(record['product_code'])
            
            logger.info(f"Batch duplicate check: {len(existing_records)}/{len(all_hashes)} images are duplicates")
        else:
            hash_to_products = {}
        
        # Step 3: 根据去重结果处理每张图片
        for data in image_data_list:
            image_hash = data['image_hash']
            
            if image_hash in hash_to_products:
                # 重复图片
                duplicate_images.append(data['file'].filename)
                duplicate_product_codes.update(hash_to_products[image_hash])
                logger.warning(f"Duplicate image detected: {data['file'].filename} (belongs to: {', '.join(hash_to_products[image_hash])})")
                continue
            
            # 非重复图片,准备入库
            prepared = transaction.prepare_image(
                data['image_bytes'], 
                data['file'].filename, 
                embedding=data['embedding'],
                image_hash=image_hash
            )
            prepared_images.append(prepared)
            logger.debug(f"Image prepared for ingest: {data['file'].filename}")
        
        # 检查是否有有效图片
        if not prepared_images:
            # 构建详细的错误信息
            error_detail = f"产品 {product_code} 入库失败："
            
            # 如果产品已存在,先提示产品信息
            if existing_product:
                error_detail += f"\n\n⚠️ 注意: 产品编码 [{product_code}] 已存在"
                error_detail += f"\n   当前产品名称: {existing_product.get('name', '未知')}"
                error_detail += f"\n   当前规格: {existing_product.get('spec', '无') or '无'}"
                error_detail += f"\n   当前分类: {existing_product.get('category', '无') or '无'}"
                error_detail += f"\n   本次操作将更新产品信息并尝试添加新图片"
            
            if duplicate_images:
                total_dup = len(duplicate_images)
                dup_products = ", ".join(sorted(duplicate_product_codes)) if duplicate_product_codes else "未知产品"
                
                error_detail += f"\n\n❌ 所有 {total_dup} 张图片均为重复图片"
                error_detail += f"\n📋 重复图片列表:"
                for i, dup_img in enumerate(duplicate_images[:MAX_DUPLICATE_DISPLAY], 1):
                    error_detail += f"\n   {i}. {dup_img}"
                if len(duplicate_images) > MAX_DUPLICATE_DISPLAY:
                    error_detail += f"\n   ... 还有 {len(duplicate_images) - MAX_DUPLICATE_DISPLAY} 张"
                
                error_detail += f"\n\n💡 这些图片已存在于以下产品中: {dup_products}"
                
                # 根据产品是否存在,提供不同的解决方案
                if existing_product:
                    error_detail += f"\n\n✅ 解决方案:"
                    if product_code in duplicate_product_codes:
                        error_detail += f"\n   ⚠️ 这些图片属于当前产品 [{product_code}]"
                        error_detail += f"\n   1. 如需重新入库,请先删除产品 [{product_code}] 及其所有图片"
                        error_detail += f"\n   2. 或者使用不同的图片添加到现有产品"
                    else:
                        error_detail += f"\n   ⚠️ 这些图片属于其他产品 [{dup_products}]"
                        error_detail += f"\n   1. 删除产品 [{dup_products}] 中的这些图片"
                        error_detail += f"\n   2. 或者使用不同的图片重新上传"
                        error_detail += f"\n   3. 本次操作仍会更新产品 [{product_code}] 的名称/规格/分类信息"
                else:
                    error_detail += f"\n\n✅ 解决方案:"
                    error_detail += f"\n   1. 删除产品 [{dup_products}] 中的这些图片"
                    error_detail += f"\n   2. 或者使用不同的图片重新上传"
            else:
                error_detail += "\n\n❌ 没有有效的图片可以入库"
                error_detail += "\n\n✅ 解决方案: 请检查图片格式是否正确 (支持 JPG, PNG, WEBP)"
            
            logger.error(error_detail)
            raise HTTPException(status_code=400, detail=error_detail)
        
        # Phase 2: Execute atomic ingestion
        logger.info(f"Phase 2: Executing atomic ingestion for {len(prepared_images)} images")
        
        product_info = {
            "code": product_code,
            "name": name,
            "spec": spec,
            "category": category
        }
        
        result = transaction.execute_ingest(prepared_images, product_info)
        
        logger.info(f" Product {product_code} ingested successfully: {result['ingested_images']} images")
        
        return {
            "success": True,
            "product_code": product_code,
            "ingested_images": result["ingested_images"],
            "milvus_ids": result["milvus_ids"],
            "minio_paths": result["minio_paths"]
        }
        
    except Exception as e:
        logger.error(f"💥 Transaction failed for {product_code}: {str(e)}", exc_info=True)
        raise
