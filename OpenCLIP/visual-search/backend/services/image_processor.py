"""
图片预处理服务
"""
import os
import hashlib
import io
from PIL import Image
import numpy as np
from typing import Tuple, Optional
from .minio_service import MinioService


class ImageProcessor:
    """图片预处理服务"""
    
    def __init__(
        self,
        target_size: Tuple[int, int] = (224, 224),
        min_size: int = 50,
        max_size: int = 4096,
        supported_formats: tuple = ('JPEG', 'PNG', 'BMP', 'GIF', 'WEBP'),
        use_minio: bool = True,
        minio_service: Optional[MinioService] = None
    ):
        """
        初始化图片处理器
        
        Args:
            target_size: 目标尺寸 (OpenCLIP 默认 224x224)
            min_size: 最小尺寸限制
            max_size: 最大尺寸限制
            supported_formats: 支持的图片格式
            use_minio: 是否使用 MinIO 存储
            minio_service: MinIO 服务实例
        """
        self.target_size = target_size
        self.min_size = min_size
        self.max_size = max_size
        self.supported_formats = supported_formats
        self.use_minio = use_minio
        self.minio_service = minio_service
        
        # 本地存储根目录（仅在不使用 MinIO 时使用）
        if not use_minio:
            self.storage_root = os.path.join(os.getcwd(), "storage", "images")
            os.makedirs(self.storage_root, exist_ok=True)
    
    def preprocess(
        self,
        image_bytes: bytes,
        enhance: bool = True
    ) -> Image.Image:
        """
        Image preprocessing
        
        Args:
            image_bytes: Image byte data
            enhance: Whether to enhance
        
        Returns:
            Processed PIL Image
        """
        # 1. Load image
        image = Image.open(io.BytesIO(image_bytes))
        
        # 2. Convert to RGB
        if image.mode != 'RGB':
            image = image.convert('RGB')
        
        # 3. Size check
        width, height = image.size
        if width < self.min_size or height < self.min_size:
            raise ValueError(f"Image size too small: {width}x{height}, minimum required {self.min_size}x{self.min_size}")
        
        if width > self.max_size or height > self.max_size:
            # Proportional scaling
            ratio = min(self.max_size / width, self.max_size / height)
            new_size = (int(width * ratio), int(height * ratio))
            image = image.resize(new_size, Image.LANCZOS)
        
        # 4. Optional enhancement
        if enhance:
            image = self._enhance_image(image)
        
        return image
    
    def _enhance_image(self, image: Image.Image) -> Image.Image:
        """
        Image enhancement
        
        Args:
            image: PIL Image
        
        Returns:
            Enhanced image
        """
        from PIL import ImageEnhance
        
        # Contrast enhancement
        enhancer = ImageEnhance.Contrast(image)
        image = enhancer.enhance(1.1)
        
        # Sharpening
        enhancer = ImageEnhance.Sharpness(image)
        image = enhancer.enhance(1.2)
        
        return image
    
    def compute_hash(self, image_bytes: bytes) -> str:
        """
        计算图片 MD5 哈希
        
        Args:
            image_bytes: 图片字节数据
        
        Returns:
            MD5 哈希字符串
        """
        return hashlib.md5(image_bytes).hexdigest()
    
    def compute_phash(self, image: Image.Image, hash_size: int = 8) -> str:
        """
        计算感知哈希（用于相似图片去重）
        
        Args:
            image: PIL Image
            hash_size: 哈希尺寸
        
        Returns:
            感知哈希字符串
        """ 
        image = image.convert('L').resize((hash_size + 1, hash_size), Image.LANCZOS)
         
        pixels = np.array(image) 
        diff = pixels[:, 1:] > pixels[:, :-1] 
        hash_str = ''.join(str(int(b)) for b in diff.flatten())
        
        return hash_str
    
    def save_image(
        self,
        image_bytes: bytes,
        product_code: str,
        filename: str
    ) -> str:
        """
        保存图片

        Args:
            image_bytes: 图片字节数据
            product_code: 产品编码
            filename: 文件名

        Returns:
            图片存储路径（MinIO object_name 或本地相对路径）
        """
        import logging
        logger = logging.getLogger(__name__)

        try:
            # 计算图片哈希
            image_hash = self.compute_hash(image_bytes)
            ext = os.path.splitext(filename)[1] or '.jpg'
            new_filename = f"{image_hash}{ext}"
            
            # 构建对象名称/文件路径
            object_name = f"{product_code}/{new_filename}"

            if self.use_minio and self.minio_service:
                # 使用 MinIO 存储
                content_type = f"image/{ext.lstrip('.').lower()}"
                if content_type == "image/jpg":
                    content_type = "image/jpeg"
                
                self.minio_service.upload_image(
                    file_data=image_bytes,
                    object_name=object_name,
                    content_type=content_type
                )
                logger.info(f"图片上传到 MinIO 成功: {object_name}")
            else:
                # 使用本地文件系统存储
                product_dir = os.path.join(self.storage_root, product_code)
                logger.info(f"创建产品目录: {product_dir}")
                os.makedirs(product_dir, exist_ok=True)
 
                if not os.access(product_dir, os.W_OK):
                    logger.error(f"目录不可写: {product_dir}")
                    raise PermissionError(f"目录不可写: {product_dir}")
 
                save_path = os.path.join(product_dir, new_filename)
                logger.info(f"准备保存图片到: {save_path}")
 
                with open(save_path, 'wb') as f:
                    f.write(image_bytes)
 
                if not os.path.exists(save_path):
                    logger.error(f"文件保存失败: {save_path}")
                    raise IOError(f"文件保存失败: {save_path}")

                file_size = os.path.getsize(save_path)
                logger.info(f"图片保存成功: {save_path}, 大小: {file_size} bytes")
            
            logger.info(f"返回存储路径: {object_name}")
            return object_name

        except Exception as e:
            logger.error(f"保存图片失败: {str(e)}", exc_info=True)
            raise
    
    def delete_image(self, image_path: str):
        """
        删除图片文件
        
        Args:
            image_path: 图片路径（MinIO object_name 或本地相对路径）
        """
        if self.use_minio and self.minio_service:
            # 从 MinIO 删除
            self.minio_service.delete_image(image_path)
        else:
            # 从本地文件系统删除
            full_path = os.path.join(self.storage_root, image_path)
            if os.path.exists(full_path):
                os.remove(full_path)
    
    def load_image(self, image_path: str) -> Image.Image:
        """
        加载图片
        
        Args:
            image_path: 图片路径（MinIO object_name 或本地相对路径）
        
        Returns:
            PIL Image
        """
        if self.use_minio and self.minio_service:
            # 从 MinIO 下载
            image_bytes = self.minio_service.download_image(image_path)
            return Image.open(io.BytesIO(image_bytes))
        else:
            # 从本地文件系统加载
            full_path = os.path.join(self.storage_root, image_path)
            return Image.open(full_path)
    
    def validate_image(self, image_bytes: bytes) -> Tuple[bool, str]:
        """
        验证图片
        
        Args:
            image_bytes: 图片字节数据
        
        Returns:
            (是否有效, 错误信息)
        """
        try:
            image = Image.open(io.BytesIO(image_bytes))
            
            # 检查格式
            if image.format not in self.supported_formats:
                return False, f"不支持的图片格式: {image.format}"
            
            # 检查尺寸
            width, height = image.size
            if width < self.min_size or height < self.min_size:
                return False, f"图片尺寸过小: {width}x{height}"
            
            # 检查是否损坏
            image.verify()
            
            return True, "OK"
            
        except Exception as e:
            return False, str(e)
    
    def get_image_info(self, image_bytes: bytes) -> dict:
        """
        获取图片信息
        
        Args:
            image_bytes: 图片字节数据
        
        Returns:
            图片信息字典
        """
        image = Image.open(io.BytesIO(image_bytes))
        
        return {
            "format": image.format,
            "mode": image.mode,
            "width": image.width,
            "height": image.height,
            "size": len(image_bytes),
            "hash": self.compute_hash(image_bytes)
        }
