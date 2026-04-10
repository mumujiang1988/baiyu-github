"""
统一事务管理服务 - 提供产品入库的原子性保障
合并了简单事务和完整事务功能
"""
import os
import json
import logging
import time
from typing import List, Dict, Optional
from datetime import datetime
import numpy as np
from concurrent.futures import ThreadPoolExecutor, as_completed

from config.constants import MAX_CONCURRENT_INGEST
from dependencies import (
    get_milvus_service,
    get_product_service,
    get_image_processor
)
from services.retry_utils import (
    delete_milvus_with_retry,
    delete_minio_with_retry
)

logger = logging.getLogger(__name__)


class IngestTransaction:
    """统一的产品入库事务管理器（支持简单模式和完整模式）"""
    
    def __init__(self, product_code: str, use_compensation: bool = False):
        """
        初始化事务管理器
        
        Args:
            product_code: 产品编码
            use_compensation: 是否启用补偿任务（默认False，使用简单回滚）
        """
        self.product_code = product_code
        self.use_compensation = use_compensation
        self.milvus_ids: List[int] = []
        self.minio_paths: List[str] = []
        self.mysql_records: List[Dict] = []
        self.transaction_id = f"{product_code}_{int(datetime.now().timestamp())}"
        
    def prepare_image(self, image_bytes: bytes, filename: str, embedding: np.ndarray = None, image_hash: str = None) -> Dict:
        """
        准备图片数据（第一阶段）
        
        Args:
            image_bytes: 图片字节数据
            filename: 文件名
            embedding: 特征向量（可选，如果已提取则传入避免重复计算）
            image_hash: 图片哈希（可选，如果已计算则传入）
        
        Returns:
            {
                "embedding": np.ndarray (如果传入),
                "image_hash": str,
                "temp_path": str,
                "filename": str
            }
        """
        from services.clip_service import ClipService
        from services.image_processor import ImageProcessor
        import numpy as np
        
        # 如果未提供 embedding，则提取特征
        if embedding is None:
            clip_service = ClipService()
            embedding = clip_service.extract_features(image_bytes)
        
        # 如果未提供 image_hash，则计算哈希
        if image_hash is None:
            image_processor = ImageProcessor()
            image_hash = image_processor.compute_hash(image_bytes)
        
        logger.debug(f" 准备图片完成: {filename}, hash={image_hash[:8]}...")
        
        result = {
            "image_hash": image_hash,
            "image_bytes": image_bytes,  # 直接保存 bytes，不需要临时文件
            "filename": filename
        }
        
        # 如果传入了 embedding，也返回
        if embedding is not None:
            result["embedding"] = embedding
        
        return result
    
    def execute_ingest(self, prepared_images: List[Dict], 
                      product_info: Dict) -> Dict:
        """
        执行入库事务（第二阶段）
        
        Args:
            prepared_images: 准备好的图片数据列表
            product_info: 产品信息 {code, name, spec, category}
        
        Returns:
            {
                "success": bool,
                "product_code": str,
                "ingested_images": int,
                "milvus_ids": List[int],
                "minio_paths": List[str]
            }
        """
        milvus_service = get_milvus_service()
        product_service = get_product_service()
        image_processor = get_image_processor()
        
        result = {
            "success": False,
            "product_code": self.product_code,
            "ingested_images": 0,
            "milvus_ids": [],
            "minio_paths": []
        }
        
        try:
            # 1. 保存产品信息
            product_service.upsert_product(
                product_code=product_info["code"],
                name=product_info["name"],
                spec=product_info.get("spec"),
                category=product_info.get("category")
            )
            logger.info(f" 产品信息保存成功: {self.product_code}")
            
            # 2. 并行处理所有图片（Milvus + MinIO + MySQL）
            def ingest_single_image(idx, prepared):
                """处理单张图片入库"""
                try:
                    # a. 插入 Milvus 向量
                    milvus_id = milvus_service.insert(
                        product_code=self.product_code,
                        embedding=prepared["embedding"],
                        image_id=0
                    )
                    
                    # b. 上传 MinIO 图片
                    image_bytes = prepared["image_bytes"]
                    object_name = f"{self.product_code}/{prepared['filename']}"
                    
                    if image_processor.minio_service:
                        from io import BytesIO
                        image_processor.minio_service.client.put_object(
                            bucket_name=image_processor.minio_service.bucket_name,
                            object_name=object_name,
                            data=BytesIO(image_bytes),
                            length=len(image_bytes),
                            content_type="image/jpeg"
                        )
                        minio_path = f"minio://{image_processor.minio_service.bucket_name}/{object_name}"
                    else:
                        raise Exception("MinIO service not available")
                    
                    # c. 插入 MySQL 图片记录
                    product_service.insert_image(
                        product_code=self.product_code,
                        image_path=minio_path,
                        milvus_id=milvus_id,
                        image_hash=prepared["image_hash"],
                        image_size=prepared.get("image_size")
                    )
                    
                    logger.debug(f"   图片 {idx+1}/{len(prepared_images)} 入库成功: {prepared['filename']}")
                    
                    return {
                        "success": True,
                        "idx": idx,
                        "milvus_id": milvus_id,
                        "minio_path": minio_path,
                        "mysql_record": {
                            "product_code": self.product_code,
                            "image_path": minio_path,
                            "milvus_id": milvus_id
                        }
                    }
                    
                except Exception as e:
                    logger.error(f" 图片 {idx+1} 入库失败: {str(e)}")
                    return {
                        "success": False,
                        "idx": idx,
                        "error": str(e)
                    }
            
            # 使用线程池并行处理
            max_workers = min(MAX_CONCURRENT_INGEST, len(prepared_images))
            logger.info(f"🚀 开始并行入库 {len(prepared_images)} 张图片 (并发数: {max_workers})")
            
            with ThreadPoolExecutor(max_workers=max_workers) as executor:
                # 提交所有任务
                future_to_idx = {
                    executor.submit(ingest_single_image, idx, prepared): idx
                    for idx, prepared in enumerate(prepared_images)
                }
                
                # 收集结果
                for future in as_completed(future_to_idx):
                    result_data = future.result()
                    
                    if result_data["success"]:
                        self.milvus_ids.append(result_data["milvus_id"])
                        self.minio_paths.append(result_data["minio_path"])
                        self.mysql_records.append(result_data["mysql_record"])
                        result["ingested_images"] += 1
                    else:
                        # 有任何一张图片失败，立即回滚
                        logger.error(f"💥 图片 {result_data['idx']+1} 失败，开始回滚...")
                        self.rollback(milvus_service, product_service, image_processor)
                        raise Exception(f"图片 {result_data['idx']+1} 入库失败: {result_data['error']}")
            
            # 3. 全部成功
            result["success"] = True
            result["milvus_ids"] = self.milvus_ids
            result["minio_paths"] = self.minio_paths
            
            logger.info(f"🎉 产品 {self.product_code} 入库成功: {result['ingested_images']} 张图片")
            
            return result
            
        except Exception as e:
            logger.error(f"💥 入库事务失败: {str(e)}", exc_info=True)
            
            # 回滚已在上面执行
            # TODO: 实现补偿任务系统（使用 Redis/Celery）
            # if self.milvus_ids or self.minio_paths or self.mysql_records:
            #     self.create_compensation_task("rollback_failed", str(e))
            
            raise
    
    def rollback(self, milvus_service, product_service, image_processor):
        """
        回滚已写入的数据（优化顺序：先外部存储，后数据库）
        
        Args:
            milvus_service: Milvus 服务
            product_service: 产品服务
            image_processor: 图片处理器
        """
        logger.warning(f" 开始回滚事务: {self.transaction_id}")
        
        rollback_errors = []
        
        # 1. 先删除 Milvus 向量（带重试）
        try:
            if self.milvus_ids:
                delete_milvus_with_retry(milvus_service, self.milvus_ids)
                logger.info(f"   回滚 Milvus 向量成功: {len(self.milvus_ids)} 个")
        except Exception as e:
            error_msg = f"Milvus 回滚失败: {str(e)}"
            logger.error(f"   {error_msg}")
            rollback_errors.append(error_msg)
        
        # 2. 再删除 MinIO 图片（带重试）
        try:
            for path in self.minio_paths:
                # 从 minio://bucket/path 提取 object_name
                object_name = path.replace(f"minio://{image_processor.bucket_name}/", "")
                delete_minio_with_retry(image_processor, object_name)
            logger.info(f"   回滚 MinIO 图片成功: {len(self.minio_paths)} 张")
        except Exception as e:
            error_msg = f"MinIO 回滚失败: {str(e)}"
            logger.error(f"   {error_msg}")
            rollback_errors.append(error_msg)
        
        # 3. 最后删除 MySQL 记录（不可逆操作放最后）
        try:
            if self.mysql_records:
                product_service._execute_update(
                    "DELETE FROM product_image WHERE product_code = %s",
                    (self.product_code,)
                )
                logger.info(f"   回滚 MySQL 记录成功")
        except Exception as e:
            error_msg = f"MySQL 回滚失败: {str(e)}"
            logger.error(f"   {error_msg}")
            rollback_errors.append(error_msg)
        
        if rollback_errors:
            logger.error(f" 部分回滚失败: {'; '.join(rollback_errors)}")
            # TODO: 实现补偿任务系统
            # if self.use_compensation:
            #     self.create_compensation_task("rollback_partial", '; '.join(rollback_errors))
        else:
            logger.info(f" 事务回滚成功: {self.transaction_id}")
    
    def create_compensation_task(self, task_type: str, error_msg: str):
        """
        创建补偿任务
        
        Args:
            task_type: 任务类型 (rollback_failed, rollback_partial)
            error_msg: 错误信息
        """
        try:
            from services.compensation_service import CompensationService
            
            task_data = {
                "transaction_id": self.transaction_id,
                "product_code": self.product_code,
                "milvus_ids": self.milvus_ids,
                "minio_paths": self.minio_paths,
                "mysql_records": self.mysql_records,
                "error_msg": error_msg
            }
            
            compensation_service = CompensationService()
            compensation_service.create_task(task_type, task_data)
            
            logger.warning(f" 已创建补偿任务: {task_type}")
            
        except Exception as e:
            logger.error(f" 创建补偿任务失败: {str(e)}")


# 为了向后兼容，提供 SimpleIngestTransaction 别名
SimpleIngestTransaction = IngestTransaction
