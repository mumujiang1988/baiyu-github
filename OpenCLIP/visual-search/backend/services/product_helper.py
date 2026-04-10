"""
产品入库辅助函数 - 处理单个产品的图片入库逻辑
"""
import logging
from typing import List, Optional
from fastapi import UploadFile

from dependencies import (
    get_clip_service,
    get_milvus_service,
    get_product_service,
    get_image_processor,
    get_rembg_service,
    get_image_optimizer
)

logger = logging.getLogger(__name__)


async def process_single_product_images(
    product_code: str,
    files: List[UploadFile],
    remove_bg: bool = False
) -> dict:
    """
    处理单个产品的多张图片入库
    
    Args:
        product_code: 产品编码
        files: 图片文件列表
        remove_bg: 是否移除背景
    
    Returns:
        {
            "success_count": 成功数量,
            "fail_count": 失败数量,
            "errors": 错误列表
        }
    """
    clip_service = get_clip_service()
    milvus_service = get_milvus_service()
    product_service = get_product_service()
    image_processor = get_image_processor()
    rembg_service = get_rembg_service()
    image_optimizer = get_image_optimizer()
    
    # 清理旧数据（与单个入库保持一致）
    existing_images = product_service.get_product_images(product_code)
    if existing_images:
        logger.info(f"检测到产品 {product_code} 已存在，正在清理旧数据...")
        
        # 1. 清理 Milvus（必须成功）
        milvus_ids = [img["milvus_id"] for img in existing_images if img["milvus_id"]]
        if milvus_ids:
            try:
                milvus_service.delete(milvus_ids)
                logger.info(f" 清理 Milvus 向量成功: {len(milvus_ids)} 个")
            except Exception as e:
                logger.error(f" 清理 Milvus 向量失败: {str(e)}")
                raise Exception(f"清理旧数据失败: {str(e)}")
        
        # 2. 清理 MinIO（警告但不中断）
        for img in existing_images:
            try:
                image_processor.delete_image(img["image_path"])
            except Exception as e:
                logger.warning(f" 清理图片文件失败: {img['image_path']}: {str(e)}")
        
        # 3. 清理 MySQL（必须成功）
        try:
            product_service._execute_update(
                "DELETE FROM product_image WHERE product_code = %s", 
                (product_code,)
            )
            logger.info(" 清理 MySQL 图片记录成功")
        except Exception as e:
            logger.error(f" 清理 MySQL 记录失败: {str(e)}")
            raise Exception(f"清理数据库记录失败: {str(e)}")
    
    success_count = 0
    fail_count = 0
    errors = []
    
    for file in files:
        try:
            image_bytes = await file.read()
            
            # 移除背景
            if remove_bg and rembg_service:
                try:
                    transparent_bytes = rembg_service.remove_background(image_bytes)
                    image_bytes = transparent_bytes
                except Exception as e:
                    logger.warning(f"背景移除失败: {str(e)}")
            
            processed_image = image_processor.preprocess(image_bytes)
            image_hash = image_processor.compute_hash(image_bytes)
            
            # 检查重复
            if product_service.image_exists(image_hash):
                logger.debug(f"图片已存在，跳过: {file.filename}")
                continue
            
            # 提取特征
            embedding = clip_service.extract_features(processed_image)
            milvus_id = milvus_service.insert(product_code=product_code, embedding=embedding)
            
            # 优化并保存图片
            if image_optimizer:
                optimize_result = image_optimizer.optimize_and_save(
                    image_bytes=image_bytes,
                    product_code=product_code,
                    filename=file.filename,
                    image_processor=image_processor,
                    generate_thumb=True
                )
                image_path = optimize_result['main_path']
                compressed_size = optimize_result['compressed_size']
                logger.info(f"图片优化完成: {file.filename}, 压缩率: {(1 - compressed_size/len(image_bytes))*100:.1f}%")
            else:
                image_path = image_processor.save_image(
                    image_bytes=image_bytes, product_code=product_code, filename=file.filename
                )
                compressed_size = len(image_bytes)
            
            # 记录
            product_service.insert_image(product_code, image_path, image_hash, milvus_id, compressed_size)
            success_count += 1
            logger.debug(f"图片入库成功: {file.filename}")
            
        except Exception as e:
            error_msg = f"{file.filename}: {str(e)}"
            errors.append(error_msg)
            fail_count += 1
            logger.error(f"图片入库失败: {error_msg}")
    
    return {
        "success_count": success_count,
        "fail_count": fail_count,
        "errors": errors
    }
