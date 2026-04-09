"""
图片相关路由 - 图片访问、背景移除
"""
import os
import logging
from fastapi import APIRouter, UploadFile, File, HTTPException, Response, Request
from slowapi import Limiter
from slowapi.util import get_remote_address

from dependencies import (
    get_image_processor,
    get_rembg_service,
    get_minio_service
)

logger = logging.getLogger(__name__)

# 创建路由器
router = APIRouter(prefix="/api/v1", tags=["图片"])

# 请求限流
limiter = Limiter(key_func=get_remote_address)


# ==================== 图片访问 ====================

@router.get("/images/{image_path:path}")
async def get_image(image_path: str):
    """
    获取图片（支持 MinIO 和本地存储）
    
    Args:
        image_path: 图片路径，如 "P001/abc123.webp"
    
    Returns:
        图片二进制数据
    """
    minio_service = get_minio_service()
    image_processor = get_image_processor()
    
    try:
        if minio_service:
            # 从 MinIO 获取图片
            image_bytes = minio_service.download_image(image_path)
            
            # 根据扩展名确定 content_type
            ext = os.path.splitext(image_path)[1].lower()
            content_types = {
                '.jpg': 'image/jpeg',
                '.jpeg': 'image/jpeg',
                '.png': 'image/png',
                '.gif': 'image/gif',
                '.webp': 'image/webp'
            }
            media_type = content_types.get(ext, 'image/jpeg')
            
            return Response(content=image_bytes, media_type=media_type)
        else:
            # 从本地文件系统获取
            file_path = os.path.join("storage", "images", image_path)
            if not os.path.exists(file_path):
                raise HTTPException(status_code=404, detail="图片不存在")
            
            ext = os.path.splitext(image_path)[1].lower()
            content_types = {
                '.jpg': 'image/jpeg',
                '.jpeg': 'image/jpeg',
                '.png': 'image/png',
                '.gif': 'image/gif',
                '.webp': 'image/webp'
            }
            media_type = content_types.get(ext, 'image/jpeg')
            
            with open(file_path, 'rb') as f:
                image_bytes = f.read()
            
            return Response(content=image_bytes, media_type=media_type)
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"获取图片失败: {image_path}, 错误: {str(e)}")
        raise HTTPException(status_code=500, detail=f"获取图片失败: {str(e)}")


# ==================== 背景移除 ====================

@router.post("/rembg/remove")
@limiter.limit("5/minute")
async def remove_background(request: Request, file: UploadFile = File(...)):
    """
    移除图片背景（单独调用）
    
    Args:
        file: 原始图片
    
    Returns:
        抠图后的透明背景图片（PNG格式）
    """
    rembg_service = get_rembg_service()
    
    if not rembg_service:
        raise HTTPException(status_code=503, detail="Rembg 服务不可用")
    
    try:
        image_bytes = await file.read()
        logger.info(f"接收背景移除请求: {file.filename}, 大小: {len(image_bytes)} bytes")
        
        # 调用 rembg 服务
        transparent_bytes = rembg_service.remove_background(image_bytes)
        
        logger.info(f"背景移除成功，返回大小: {len(transparent_bytes)} bytes")
        
        # 返回抠图后的图片
        return Response(
            content=transparent_bytes,
            media_type="image/png",
            headers={
                "Content-Disposition": f"attachment; filename={os.path.splitext(file.filename)[0]}_nobg.png"
            }
        )
        
    except Exception as e:
        logger.error(f"背景移除失败: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"背景移除失败: {str(e)}")
