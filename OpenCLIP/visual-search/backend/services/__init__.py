"""
服务模块初始化
"""
from .clip_service import ClipService
from .milvus_service import MilvusService
from .product_service import ProductService
from .image_processor import ImageProcessor

__all__ = [
    "ClipService",
    "MilvusService",
    "ProductService",
    "ImageProcessor"
]
