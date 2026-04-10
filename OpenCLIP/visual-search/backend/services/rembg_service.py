"""
Rembg 抠图服务
调用独立的 rembg Docker 容器进行背景移除
"""
import os
import io
import logging
import requests
from typing import Optional
from PIL import Image

logger = logging.getLogger(__name__)


class RembgService:
    """Rembg 抠图服务"""
    
    def __init__(self, api_url: str = "http://rembg:5000"):
        """
        初始化 Rembg 服务
        
        Args:
            api_url: Rembg API 地址
        """
        self.api_url = api_url
        logger.info(f"Rembg 服务初始化完成，API 地址: {api_url}")
    
    def remove_background(self, image_bytes: bytes) -> bytes:
        """
        移除图片背景
        
        Args:
            image_bytes: 原始图片字节数据
            
        Returns:
            抠图后的图片字节数据（PNG 格式，带透明通道）
        """
        try:
            logger.info(f"开始调用 Rembg API: {self.api_url}/api/remove")
            
            # 调用 rembg API
            response = requests.post(
                f"{self.api_url}/api/remove",
                files={"file": ("image.png", image_bytes, "image/png")},
                timeout=30  # 30秒超时
            )
            
            # 检查响应状态
            if response.status_code != 200:
                error_msg = f"Rembg API 返回错误状态码: {response.status_code}"
                logger.error(error_msg)
                raise Exception(error_msg)
            
            # 获取抠图后的图片数据
            result_bytes = response.content
            logger.info(f"Rembg 抠图成功，返回数据大小: {len(result_bytes)} bytes")
            
            return result_bytes
            
        except requests.exceptions.ConnectionError as e:
            error_msg = f"无法连接到 Rembg 服务 ({self.api_url}): {str(e)}"
            logger.error(error_msg)
            raise Exception(error_msg)
        except requests.exceptions.Timeout as e:
            error_msg = f"Rembg 服务响应超时: {str(e)}"
            logger.error(error_msg)
            raise Exception(error_msg)
        except Exception as e:
            error_msg = f"Rembg 抠图失败: {str(e)}"
            logger.error(error_msg, exc_info=True)
            raise
    
    def remove_background_and_save(
        self, 
        image_bytes: bytes, 
        product_code: str, 
        original_filename: str
    ) -> str:
        """
        移除背景并保存图片到 MinIO
        
        Args:
            image_bytes: 原始图片字节数据
            product_code: 产品编码
            original_filename: 原始文件名
            
        Returns:
            MinIO object_name (格式: product_code/filename)
        """
        try:
            # 1. 调用 Rembg 抠图
            transparent_bytes = self.remove_background(image_bytes)
            
            # 2. 生成新文件名（添加 _nobg 后缀）
            name_without_ext = os.path.splitext(original_filename)[0]
            new_filename = f"{name_without_ext}_nobg.png"  # 统一使用 PNG 格式保留透明通道
            
            # 3. 使用 ImageProcessor 保存到 MinIO
            from services.image_processor import ImageProcessor
            processor = ImageProcessor()
            
            # 调用 save_image 方法，会自动使用 MinIO 或本地存储
            object_name = processor.save_image(
                image_bytes=transparent_bytes,
                product_code=product_code,
                filename=new_filename
            )
            
            logger.info(f"抠图后图片已保存: {object_name}")
            
            return object_name
            
        except Exception as e:
            logger.error(f"移除背景并保存失败: {str(e)}", exc_info=True)
            raise
    
    def health_check(self) -> bool:
        """
        检查 Rembg 服务健康状态
        
        Returns:
            服务是否可用
        """
        try:
            response = requests.get(f"{self.api_url}/", timeout=5)
            return response.status_code == 200
        except Exception as e:
            logger.warning(f"Rembg 服务健康检查失败: {str(e)}")
            return False
