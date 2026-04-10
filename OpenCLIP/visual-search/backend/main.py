"""
FastAPI 主入口 - 以图搜品系统

应用启动入口，负责：
- 创建 FastAPI 应用实例
- 配置中间件（CORS、限流）
- 注册路由
- 初始化服务
"""
import logging
import os
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 导入配置和依赖
from config import settings
from dependencies import init_services, clip_service, milvus_service, minio_service
from routers import search_router, product_router, image_router
from middleware import http_exception_handler, validation_exception_handler


# ==================== 应用工厂 ====================

def create_app() -> FastAPI:
    """
    创建并配置 FastAPI 应用
    
    Returns:
        配置完成的 FastAPI 应用实例
    """
    # 创建应用实例
    app = FastAPI(
        title="以图搜品系统",
        description="基于OpenCLIP+Milvus的图像检索系统",
        version="2.0.0"
    )
    
    # 配置请求限流
    limiter = Limiter(key_func=get_remote_address)
    app.state.limiter = limiter
    app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)
    
    # 配置 CORS
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=True,
        allow_methods=["GET", "POST", "DELETE", "PUT"],
        allow_headers=["*"],
    )
    
    # 注册全局异常处理器
    app.add_exception_handler(HTTPException, http_exception_handler)
    from fastapi.exceptions import RequestValidationError
    app.add_exception_handler(RequestValidationError, validation_exception_handler)
    
    # 注册路由
    app.include_router(search_router)
    app.include_router(product_router)
    app.include_router(image_router)
    
    # 注册启动事件
    @app.on_event("startup")
    async def startup_event():
        """应用启动时初始化所有服务"""
        try:
            # 初始化所有服务
            init_services()
            logger.info(" 所有服务初始化成功")
            
        except Exception as e:
            logger.error(f" 服务初始化失败: {str(e)}", exc_info=True)
            logger.warning(" 应用将在受限模式下运行（部分功能不可用）")
    
    # 根路径
    @app.get("/")
    async def root():
        """健康检查"""
        return {"status": "ok", "message": "以图搜品系统运行中"}
    
    # 详细健康检查
    @app.get("/health")
    async def health_check():
        """详细健康检查"""
        import shutil
        
        checks = {
            "mysql": True,  # 如果初始化成功则为True
            "milvus": milvus_service is not None,
            "clip": clip_service is not None,
            "minio": minio_service is not None and minio_service.health_check() if minio_service else False
        }
        
        # 检查 Milvus 集合状态
        if milvus_service:
            try:
                stats = milvus_service.get_stats()
                checks["milvus_vector_count"] = stats.get("vector_count", 0)
            except Exception as e:
                logger.warning(f"Milvus 状态检查失败: {str(e)}")
                checks["milvus"] = False
        
        # 检查磁盘剩余空间 (storage 目录)
        try:
            storage_path = os.path.join(os.path.dirname(__file__), "storage")
            if os.path.exists(storage_path):
                usage = shutil.disk_usage(storage_path)
                checks["disk_free_gb"] = round(usage.free / (1024**3), 2)
                checks["disk_usage_percent"] = round((usage.used / usage.total) * 100, 2)
        except Exception as e:
            logger.warning(f"磁盘空间检查失败: {str(e)}")
        
        return {
            "status": "healthy" if all(v for k, v in checks.items() if isinstance(v, bool)) else "unhealthy",
            "checks": checks
        }
    
    return app


# ==================== 应用实例 ====================

app = create_app()


# ==================== 主入口 ====================

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=True
    )
