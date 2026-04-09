"""
路由器模块初始化
"""
from .search import router as search_router
from .product import router as product_router
from .image import router as image_router

__all__ = ["search_router", "product_router", "image_router"]
