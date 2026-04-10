"""
依赖注入模块 - 管理服务实例
"""
import logging
from services.clip_service import ClipService
from services.milvus_service import MilvusService
from services.product_service import ProductService
from services.image_processor import ImageProcessor
from services.rembg_service import RembgService
from services.minio_service import MinioService
from services.image_optimizer import ImageOptimizer
from config.settings import settings

logger = logging.getLogger(__name__)


# 全局服务实例
clip_service = None
milvus_service = None
product_service = None
image_processor = None
rembg_service = None
minio_service = None
image_optimizer = None


def init_services():
    """初始化所有服务"""
    global clip_service, milvus_service, product_service, image_processor
    global rembg_service, minio_service, image_optimizer

    logger.info("🚀 正在初始化服务...")

    try:
        # 初始化 MinIO 服务（如果启用）
        if settings.use_minio:
            try:
                minio_service = MinioService(
                    endpoint=settings.minio_endpoint,
                    access_key=settings.minio_access_key,
                    secret_key=settings.minio_secret_key,
                    secure=settings.minio_secure,
                    bucket_name=settings.minio_bucket
                )
                logger.info(" MinIO 连接成功")
            except Exception as e:
                logger.warning(f" MinIO 连接失败，将使用本地存储: {str(e)}")
                minio_service = None
        else:
            logger.info("ℹ️ MinIO 未启用，将使用本地存储")
            minio_service = None

        # 初始化 OpenCLIP 服务
        clip_service = ClipService(
            model_name=settings.openclip_model,
            pretrained=settings.openclip_pretrained
        )
        logger.info(" OpenCLIP 模型加载成功")

        # 初始化 Milvus 服务
        milvus_service = MilvusService(
            host=settings.milvus_host,
            port=settings.milvus_port
        )
        logger.info(" Milvus 连接成功")

        # 初始化 MySQL 服务
        product_service = ProductService(
            host=settings.mysql_host,
            port=settings.mysql_port,
            user=settings.mysql_user,
            password=settings.mysql_password,
            database=settings.mysql_db
        )
        logger.info(" MySQL 连接成功")

        # 初始化图片处理器（传入 MinIO 服务）
        image_processor = ImageProcessor(
            use_minio=(minio_service is not None),
            minio_service=minio_service
        )
        storage_type = "MinIO 对象存储" if minio_service else "本地文件系统"
        logger.info(f" 图片处理器初始化完成（使用 {storage_type}）")

        # 初始化 Rembg 服务
        rembg_service = RembgService(api_url=settings.rembg_api_url)
        if rembg_service.health_check():
            logger.info(" Rembg 抠图服务连接成功")
        else:
            logger.warning(" Rembg 抠图服务不可用，将使用原始图片")
            rembg_service = None

        # 初始化图片优化器
        image_optimizer = ImageOptimizer()
        logger.info(" 图片优化器初始化完成（支持压缩、缩略图、WebP）")

        logger.info("🎉 所有服务初始化完成！")

    except Exception as e:
        logger.error(f" 服务初始化失败: {str(e)}")
        raise


def get_clip_service():
    """获取 CLIP 服务实例"""
    return clip_service


def get_milvus_service():
    """获取 Milvus 服务实例"""
    return milvus_service


def get_product_service():
    """获取产品服务实例"""
    return product_service


def get_image_processor():
    """获取图片处理器实例"""
    return image_processor


def get_rembg_service():
    """获取 Rembg 服务实例"""
    return rembg_service


def get_minio_service():
    """获取 MinIO 服务实例"""
    return minio_service


def get_image_optimizer():
    """获取图片优化器实例"""
    return image_optimizer
