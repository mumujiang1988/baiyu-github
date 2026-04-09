"""
MinIO 对象存储服务
"""
import os
import logging
from minio import Minio
from minio.error import S3Error
from typing import Optional

logger = logging.getLogger(__name__)


class MinioService:
    """MinIO 对象存储服务"""
    
    def __init__(
        self,
        endpoint: str = "minio:9000",
        access_key: str = "minioadmin",
        secret_key: str = "minioadmin",
        secure: bool = False,
        bucket_name: str = "product-images"
    ):
        """
        初始化 MinIO 客户端
        
        Args:
            endpoint: MinIO 服务端点
            access_key: 访问密钥
            secret_key: 秘密密钥
            secure: 是否使用 HTTPS
            bucket_name: 默认存储桶名称
        """
        self.endpoint = endpoint
        self.bucket_name = bucket_name
        
        # 创建 MinIO 客户端
        self.client = Minio(
            endpoint,
            access_key=access_key,
            secret_key=secret_key,
            secure=secure
        )
        
        # 确保存储桶存在
        self._ensure_bucket_exists(bucket_name)
        
        logger.info(f"MinIO 服务初始化完成: {endpoint}/{bucket_name}")
    
    def _ensure_bucket_exists(self, bucket_name: str):
        """确保存储桶存在，不存在则创建"""
        try:
            if not self.client.bucket_exists(bucket_name):
                self.client.make_bucket(bucket_name)
                logger.info(f"创建存储桶: {bucket_name}")
            else:
                logger.info(f"存储桶已存在: {bucket_name}")
        except S3Error as e:
            logger.error(f"检查/创建存储桶失败: {str(e)}")
            raise
    
    def upload_image(
        self,
        file_data: bytes,
        object_name: str,
        content_type: str = "image/jpeg"
    ) -> str:
        """
        上传图片到 MinIO
        
        Args:
            file_data: 图片字节数据
            object_name: 对象名称（路径）
            content_type: 内容类型
            
        Returns:
            对象名称
        """
        try:
            from io import BytesIO
            
            # 上传文件
            self.client.put_object(
                self.bucket_name,
                object_name,
                BytesIO(file_data),
                length=len(file_data),
                content_type=content_type
            )
            
            logger.info(f"图片上传成功: {object_name}")
            return object_name
            
        except S3Error as e:
            logger.error(f"图片上传失败: {str(e)}")
            raise
    
    def download_image(self, object_name: str) -> bytes:
        """
        从 MinIO 下载图片
        
        Args:
            object_name: 对象名称
            
        Returns:
            图片字节数据
        """
        try:
            response = self.client.get_object(self.bucket_name, object_name)
            data = response.read()
            response.close()
            response.release_conn()
            
            logger.debug(f"图片下载成功: {object_name}")
            return data
            
        except S3Error as e:
            logger.error(f"图片下载失败: {str(e)}")
            raise
    
    def delete_image(self, object_name: str):
        """
        删除 MinIO 中的图片
        
        Args:
            object_name: 对象名称
        """
        try:
            self.client.remove_object(self.bucket_name, object_name)
            logger.info(f"图片删除成功: {object_name}")
        except S3Error as e:
            logger.error(f"图片删除失败: {str(e)}")
            raise
    
    def image_exists(self, object_name: str) -> bool:
        """
        检查图片是否存在
        
        Args:
            object_name: 对象名称
            
        Returns:
            是否存在
        """
        try:
            self.client.stat_object(self.bucket_name, object_name)
            return True
        except S3Error:
            return False
    
    def get_presigned_url(
        self,
        object_name: str,
        expires: int = 3600
    ) -> str:
        """
        获取预签名 URL（用于临时访问）
        
        Args:
            object_name: 对象名称
            expires: 过期时间（秒）
            
        Returns:
            预签名 URL
        """
        try:
            url = self.client.presigned_get_object(
                self.bucket_name,
                object_name,
                expires=expires
            )
            return url
        except S3Error as e:
            logger.error(f"生成预签名 URL 失败: {str(e)}")
            raise
    
    def health_check(self) -> bool:
        """
        检查 MinIO 服务健康状态
        
        Returns:
            服务是否可用
        """
        try:
            self.client.bucket_exists(self.bucket_name)
            return True
        except Exception as e:
            logger.warning(f"MinIO 健康检查失败: {str(e)}")
            return False
