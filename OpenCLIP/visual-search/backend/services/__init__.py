"""
服务模块初始化
"""
from .clip_service import ClipService
from .milvus_service import MilvusService
from .product_service import ProductService
from .image_processor import ImageProcessor
from .rembg_service import RembgService
from .minio_service import MinioService
from .image_optimizer import ImageOptimizer

__all__ = [
    "ClipService",
    "MilvusService",
    "ProductService",
    "ImageProcessor",
    "RembgService",
    "MinioService",
    "ImageOptimizer"
]
