"""
配置管理模块
使用Pydantic进行配置验证和管理
"""
from pydantic_settings import BaseSettings
from pydantic import validator
from typing import Optional
import os

class Settings(BaseSettings):
    """应用配置"""
    
    # 应用配置
    APP_NAME: str = "企业产品以图搜系统"
    APP_VERSION: str = "1.0.0"
    DEBUG: bool = False
    API_HOST: str = "0.0.0.0"
    API_PORT: int = 8000
    
    # 数据库配置
    DB_HOST: str
    DB_PORT: int = 3306
    DB_USER: str
    DB_PASSWORD: str
    DB_NAME: str
    
    @validator('DB_PASSWORD')
    def validate_db_password(cls, v):
        if not v or v == "your_password":
            raise ValueError("DB_PASSWORD must be set in environment variables")
        return v
    
    @property
    def DATABASE_URL(self) -> str:
        """构建数据库连接URL"""
        return f"mysql+pymysql://{self.DB_USER}:{self.DB_PASSWORD}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}?charset=utf8mb4"
    
    # CLIP模型配置
    CLIP_MODEL: str = "openai/clip-vit-base-patch32"
    CLIP_CACHE_DIR: Optional[str] = None
    
    # FAISS配置
    FAISS_INDEX_PATH: str = "product_index.faiss"
    VECTOR_DIM: int = 512
    FAISS_SAVE_INTERVAL: int = 60  # 索引保存间隔（秒）
    
    # 文件上传配置
    UPLOAD_DIR: str = "uploads"
    MAX_FILE_SIZE: int = 10 * 1024 * 1024  # 10MB
    ALLOWED_EXTENSIONS: set = {"jpg", "jpeg", "png", "webp"}
    
    # CORS配置
    CORS_ORIGINS: list = ["http://localhost:5173", "http://127.0.0.1:5173"]
    CORS_ALLOW_CREDENTIALS: bool = True
    CORS_ALLOW_METHODS: list = ["*"]
    CORS_ALLOW_HEADERS: list = ["*"]
    
    # 日志配置
    LOG_LEVEL: str = "INFO"
    LOG_FILE: Optional[str] = "logs/app.log"
    LOG_MAX_SIZE: int = 10 * 1024 * 1024  # 10MB
    LOG_BACKUP_COUNT: int = 5
    
    # 性能配置
    DB_POOL_SIZE: int = 20
    DB_MAX_OVERFLOW: int = 40
    DB_POOL_RECYCLE: int = 3600
    
    # 安全配置
    SECRET_KEY: str = "your-secret-key-change-in-production"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30
    
    @validator('SECRET_KEY')
    def validate_secret_key(cls, v):
        if v == "your-secret-key-change-in-production":
            import warnings
            warnings.warn("Using default SECRET_KEY, please change in production!")
        return v
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = True

# 创建全局配置实例
try:
    settings = Settings()
except Exception as e:
    print(f"配置加载失败: {str(e)}")
    print("请检查.env文件配置是否正确")
    raise
