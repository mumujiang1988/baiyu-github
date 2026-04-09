"""
Milvus 向量数据库服务
"""
from pymilvus import (
    connections,
    Collection,
    FieldSchema,
    CollectionSchema,
    DataType,
    utility
)
import numpy as np
from typing import List, Dict, Optional
import logging
import time

logger = logging.getLogger(__name__)


class MilvusService:
    """Milvus 向量数据库服务"""
    
    def __init__(
        self,
        host: str = "localhost",
        port: int = 19530,
        collection_name: str = "product_images",
        embedding_dim: int = 512
    ):
        """
        初始化 Milvus 连接
        
        Args:
            host: Milvus 主机地址
            port: Milvus 端口
            collection_name: 集合名称
            embedding_dim: 向量维度
        """
        self.host = host
        self.port = port
        self.collection_name = collection_name
        self.embedding_dim = embedding_dim
        self.collection = None
        
        # 连接 Milvus（带重试机制）
        logger.info(f"🔌 连接 Milvus: {host}:{port}")
        max_retries = 5
        retry_delay = 3
        
        for attempt in range(max_retries):
            try:
                connections.connect(
                    alias="default",
                    host=host,
                    port=port
                )
                logger.info("✅ Milvus 连接成功")
                break
            except Exception as e:
                if attempt < max_retries - 1:
                    logger.warning(f"⚠️ Milvus 连接失败，{retry_delay}秒后重试 ({attempt + 1}/{max_retries})")
                    time.sleep(retry_delay)
                else:
                    raise Exception(f"Milvus 连接失败: {str(e)}")
        
        # 创建或加载集合
        self.create_collection()
    
    def create_collection(self):
        """创建集合（如果不存在）"""
        # 检查集合是否存在
        if utility.has_collection(self.collection_name):
            logger.info(f"📦 集合 {self.collection_name} 已存在")
            self.collection = Collection(self.collection_name)
            self.collection.load()
            return
        
        logger.info(f"📦 创建集合: {self.collection_name}")
        
        # 定义字段
        fields = [
            FieldSchema(
                name="id",
                dtype=DataType.INT64,
                is_primary=True,
                auto_id=True
            ),
            FieldSchema(
                name="product_code",
                dtype=DataType.VARCHAR,
                max_length=50
            ),
            FieldSchema(
                name="image_id",
                dtype=DataType.INT64
            ),
            FieldSchema(
                name="embedding",
                dtype=DataType.FLOAT_VECTOR,
                dim=self.embedding_dim
            )
        ]
        
        # 创建集合
        schema = CollectionSchema(
            fields=fields,
            description="产品图片向量集合"
        )
        self.collection = Collection(
            name=self.collection_name,
            schema=schema
        )
        
        # 创建索引
        logger.info("🔍 创建向量索引...")
        index_params = {
            "metric_type": "COSINE",
            "index_type": "IVF_FLAT",
            "params": {"nlist": 1024}
        }
        self.collection.create_index(
            field_name="embedding",
            index_params=index_params
        )
        
        # 加载集合到内存
        self.collection.load()
        logger.info("✅ 集合创建完成")
    
    def insert(
        self,
        product_code: str,
        embedding: np.ndarray,
        image_id: int = 0
    ) -> int:
        """
        插入向量
        
        Args:
            product_code: 产品编码
            embedding: 特征向量
            image_id: 图片ID
        
        Returns:
            Milvus ID
        """
        # 检查 collection 是否初始化
        if self.collection is None:
            raise Exception("Milvus collection 未初始化，请检查服务启动日志")
        
        # 准备数据
        data = [
            [product_code],
            [image_id],
            [embedding.tolist()]
        ]
        
        # 插入
        result = self.collection.insert(data)
        
        # 刷新以确保数据可见
        self.collection.flush()
        
        # 返回插入的ID
        return result.primary_keys[0]
    
    def insert_batch(
        self,
        product_codes: List[str],
        embeddings: np.ndarray,
        image_ids: List[int] = None
    ) -> List[int]:
        """
        批量插入向量
        
        Args:
            product_codes: 产品编码列表
            embeddings: 特征向量矩阵 (N x D)
            image_ids: 图片ID列表
        
        Returns:
            Milvus ID 列表
        """
        # 检查 collection 是否初始化
        if self.collection is None:
            raise Exception("Milvus collection 未初始化，请检查服务启动日志")
        
        if image_ids is None:
            image_ids = [0] * len(product_codes)
        
        # 准备数据
        data = [
            product_codes,
            image_ids,
            embeddings.tolist()
        ]
        
        # 插入
        result = self.collection.insert(data)
        self.collection.flush()
        
        return result.primary_keys
    
    def search(
        self,
        query_vector: np.ndarray,
        top_k: int = 10,
        product_code: str = None
    ) -> List[Dict]:
        """
        向量检索
        
        Args:
            query_vector: 查询向量
            top_k: 返回结果数量
            product_code: 限定产品编码（可选）
        
        Returns:
            检索结果列表
        """
        # 准备搜索参数
        search_params = {
            "metric_type": "COSINE",
            "params": {"nprobe": 10}
        }
        
        # 构建过滤表达式
        expr = None
        if product_code:
            expr = f'product_code == "{product_code}"'
        
        # 执行搜索
        results = self.collection.search(
            data=[query_vector.tolist()],
            anns_field="embedding",
            param=search_params,
            limit=top_k,
            expr=expr,
            output_fields=["product_code", "image_id"]
        )
        
        # 解析结果
        search_results = []
        for hits in results:
            for hit in hits:
                search_results.append({
                    "milvus_id": hit.id,
                    "product_code": hit.entity.get("product_code"),
                    "image_id": hit.entity.get("image_id"),
                    "similarity": hit.score  # COSINE 相似度
                })
        
        return search_results
    
    def delete(self, ids: List[int]):
        """
        删除向量
        
        Args:
            ids: Milvus ID 列表
        """
        # 检查 collection 是否初始化
        if self.collection is None:
            raise Exception("Milvus collection 未初始化")
        
        # 检查 ids 是否为空
        if not ids:
            logger.warning("⚠️ 没有需要删除的向量 ID")
            return
        
        expr = f"id in {ids}"
        self.collection.delete(expr)
        self.collection.flush()
        logger.info(f"✅ 成功删除 {len(ids)} 个向量")
    
    def delete_by_product(self, product_code: str):
        """
        删除指定产品的所有向量
        
        Args:
            product_code: 产品编码
        """
        # 检查 collection 是否初始化
        if self.collection is None:
            raise Exception("Milvus collection 未初始化")
        
        expr = f'product_code == "{product_code}"'
        self.collection.delete(expr)
        self.collection.flush()
        logger.info(f"✅ 成功删除产品 {product_code} 的所有向量")
    
    def count(self) -> int:
        """获取向量数量"""
        if self.collection is None:
            return 0
        return self.collection.num_entities
    
    def get_stats(self) -> Dict:
        """获取统计信息"""
        return {
            "collection_name": self.collection_name,
            "vector_count": self.count(),
            "embedding_dim": self.embedding_dim
        }
    
    def close(self):
        """关闭连接"""
        connections.disconnect("default")
