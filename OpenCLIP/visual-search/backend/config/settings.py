from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    mysql_host: str = "mysql"
    mysql_port: int = 3306
    mysql_user: str = "vs_user"
    mysql_password: str = "vs_pass123"
    mysql_db: str = "visual_search"
    
    milvus_host: str = "milvus"
    milvus_port: int = 19530
    
    openclip_model: str = "ViT-B-32"
    openclip_pretrained: str = "laion2b_s34b_b79k"
    
    rembg_api_url: str = "http://rembg:7000"
    
    minio_endpoint: str = "minio:9000"
    minio_access_key: str = "minioadmin"
    minio_secret_key: str = "minioadmin"
    minio_secure: bool = False
    minio_bucket: str = "product-images"
    use_minio: bool = True
    
    enable_transaction: bool = True

    class Config:
        env_file = ".env"
        extra = "ignore"


settings = Settings()
