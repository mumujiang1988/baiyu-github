"""
应用配置模块
"""
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """应用配置"""
    
    # MySQL 配置
    mysql_host: str = "localhost"
    mysql_port: int = 3306
    mysql_user: str = "vs_user"
    mysql_password: str = "vs_pass123"
    mysql_db: str = "visual_search"
    
    # Milvus 配置
    milvus_host: str = "localhost"
    milvus_port: int = 19530
    
    # OpenCLIP 配置
    openclip_model: str = "ViT-B-32"
    openclip_pretrained: str = "laion2b_s34b_b79k"
    
    # Rembg 配置
    rembg_api_url: str = "http://rembg:5000"
    
    # MinIO 配置
    minio_endpoint: str = "minio:9000"
    minio_access_key: str = "minioadmin"
    minio_secret_key: str = "minioadmin"
    minio_secure: bool = False
    minio_bucket: str = "product-images"
    use_minio: bool = True
    
    # Transaction Management
    enable_transaction: bool = True  # Enable transactional ingestion

    class Config:
        env_file = ".env"
        extra = "ignore"  # 忽略 .env 中未定义的字段


# 全局配置实例
settings = Settings()
