"""
MySQL 产品业务服务
"""
import mysql.connector
from mysql.connector import pooling
from typing import List, Dict, Optional
from collections import defaultdict


class ProductService:
    """MySQL 产品业务服务"""
    
    def __init__(
        self,
        host: str = "localhost",
        port: int = 3306,
        user: str = "vs_user",
        password: str = "vs_pass123",
        database: str = "visual_search",
        pool_size: int = 5
    ):
        """
        初始化 MySQL 连接池
        
        Args:
            host: MySQL 主机
            port: MySQL 端口
            user: 用户名
            password: 密码
            database: 数据库名
            pool_size: 连接池大小
        """
        print(f"🔌 连接 MySQL: {host}:{port}/{database}")

        # 创建连接池配置
        dbconfig = {
            "host": host,
            "port": port,
            "user": user,
            "password": password,
            "database": database,
            "charset": 'utf8mb4',
            "collation": 'utf8mb4_unicode_ci'
        }

        try:
            # 尝试使用新的API
            self.connection_pool = pooling.MySQLConnectionPool(
                pool_name="mysql_pool",
                pool_size=pool_size,
                **dbconfig
            )
        except AttributeError:
            # 如果新API不可用，使用旧API
            self.connection_pool = pooling.PooledMySQLConnection(
                pool_name="mysql_pool",
                pool_size=pool_size,
                **dbconfig
            )
        
        print("✅ MySQL 连接池创建成功")
    
    def _get_connection(self):
        """获取数据库连接"""
        return self.connection_pool.get_connection()
    
    # ==================== 产品操作 ====================
    
    def upsert_product(
        self,
        product_code: str,
        name: str,
        spec: Optional[str] = None,
        category: Optional[str] = None
    ):
        """
        创建或更新产品
        
        Args:
            product_code: 产品编码
            name: 产品名称
            spec: 规格
            category: 分类
        """
        conn = self._get_connection()
        cursor = conn.cursor()
        
        try:
            sql = """
            INSERT INTO product (product_code, name, spec, category)
            VALUES (%s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
                name = VALUES(name),
                spec = VALUES(spec),
                category = VALUES(category),
                updated_at = NOW()
            """
            cursor.execute(sql, (product_code, name, spec, category))
            conn.commit()
        finally:
            cursor.close()
            conn.close()
    
    def get_product(self, product_code: str) -> Optional[Dict]:
        """
        获取产品信息
        
        Args:
            product_code: 产品编码
        
        Returns:
            产品信息字典
        """
        conn = self._get_connection()
        cursor = conn.cursor(dictionary=True)

        try:
            # 由于现在是彻底删除，不需要检查 status
            sql = "SELECT * FROM product WHERE product_code = %s"
            cursor.execute(sql, (product_code,))
            return cursor.fetchone()
        finally:
            cursor.close()
            conn.close()
    
    def list_products(
        self,
        category: Optional[str] = None,
        page: int = 1,
        page_size: int = 20
    ) -> List[Dict]:
        """
        查询产品列表
        
        Args:
            category: 分类筛选
            page: 页码
            page_size: 每页数量
        
        Returns:
            产品列表
        """
        conn = self._get_connection()
        cursor = conn.cursor(dictionary=True)
        
        try:
            offset = (page - 1) * page_size

            if category:
                sql = """
                SELECT * FROM product
                WHERE category = %s
                ORDER BY created_at DESC
                LIMIT %s OFFSET %s
                """
                cursor.execute(sql, (category, page_size, offset))
            else:
                sql = """
                SELECT * FROM product
                ORDER BY created_at DESC
                LIMIT %s OFFSET %s
                """
                cursor.execute(sql, (page_size, offset))
            
            return cursor.fetchall()
        finally:
            cursor.close()
            conn.close()
    
    def count_products(self, category: Optional[str] = None) -> int:
        """统计产品数量"""
        conn = self._get_connection()
        cursor = conn.cursor()

        try:
            if category:
                sql = "SELECT COUNT(*) FROM product WHERE category = %s"
                cursor.execute(sql, (category,))
            else:
                sql = "SELECT COUNT(*) FROM product"
                cursor.execute(sql)

            return cursor.fetchone()[0]
        finally:
            cursor.close()
            conn.close()
    
    def delete_product(self, product_code: str):
        """
        删除产品（彻底删除）

        Args:
            product_code: 产品编码
        """
        conn = self._get_connection()
        cursor = conn.cursor()

        try:
            # 先删除关联图片记录
            sql = "DELETE FROM product_image WHERE product_code = %s"
            cursor.execute(sql, (product_code,))

            # 再删除产品记录
            sql = "DELETE FROM product WHERE product_code = %s"
            cursor.execute(sql, (product_code,))

            conn.commit()
            print(f"✅ 产品 {product_code} 彻底删除成功")
        finally:
            cursor.close()
            conn.close()
    
    # ==================== 图片操作 ====================
    
    def insert_image(
        self,
        product_code: str,
        image_path: str,
        image_hash: str,
        milvus_id: int,
        image_size: int = None
    ):
        """
        插入图片记录
        
        Args:
            product_code: 产品编码
            image_path: 图片路径
            image_hash: 图片哈希
            milvus_id: Milvus ID
            image_size: 图片大小
        """
        conn = self._get_connection()
        cursor = conn.cursor()
        
        try:
            sql = """
            INSERT INTO product_image (product_code, image_path, image_hash, milvus_id, image_size)
            VALUES (%s, %s, %s, %s, %s)
            """
            cursor.execute(sql, (product_code, image_path, image_hash, milvus_id, image_size))
            conn.commit()
            
            return cursor.lastrowid
        finally:
            cursor.close()
            conn.close()
    
    def image_exists(self, image_hash: str) -> bool:
        """检查图片是否已存在"""
        conn = self._get_connection()
        cursor = conn.cursor()

        try:
            # 由于现在是彻底删除，不需要检查 status，直接检查 image_hash 是否存在
            sql = "SELECT COUNT(*) FROM product_image WHERE image_hash = %s"
            cursor.execute(sql, (image_hash,))
            return cursor.fetchone()[0] > 0
        finally:
            cursor.close()
            conn.close()
    
    def get_product_images(self, product_code: str) -> List[Dict]:
        """
        获取产品的所有图片
        
        Args:
            product_code: 产品编码
        
        Returns:
            图片列表
        """
        conn = self._get_connection()
        cursor = conn.cursor(dictionary=True)
        
        try:
            # 由于现在是彻底删除，不需要检查 status
            sql = """
            SELECT * FROM product_image
            WHERE product_code = %s
            ORDER BY created_at
            """
            cursor.execute(sql, (product_code,))
            return cursor.fetchall()
        finally:
            cursor.close()
            conn.close()
    
    def count_images(self) -> int:
        """统计图片数量"""
        conn = self._get_connection()
        cursor = conn.cursor()

        try:
            # 由于现在是彻底删除，不需要检查 status
            sql = "SELECT COUNT(*) FROM product_image"
            cursor.execute(sql)
            return cursor.fetchone()[0]
        finally:
            cursor.close()
            conn.close()
    
    # ==================== 检索结果聚合 ====================
    
    def aggregate_results(
        self,
        raw_results: List[Dict],
        aggregation: str = "max",
        top_k: int = 10
    ) -> List[Dict]:
        """
        聚合检索结果（按产品编码）
        
        Args:
            raw_results: 原始检索结果
            aggregation: 聚合策略 (max/avg)
            top_k: 返回结果数量
        
        Returns:
            聚合后的结果
        """
        # 按产品编码分组
        product_results = defaultdict(list)
        for result in raw_results:
            product_code = result["product_code"]
            product_results[product_code].append(result)
        
        # 聚合计算
        aggregated = []
        for product_code, results in product_results.items():
            if aggregation == "max":
                # 取最大相似度
                best = max(results, key=lambda x: x["similarity"])
                similarity = best["similarity"]
            elif aggregation == "avg":
                # 取平均相似度
                similarity = sum(r["similarity"] for r in results) / len(results)
            else:
                similarity = results[0]["similarity"]
            
            # 收集图片路径
            image_paths = self.get_product_images(product_code)
            image_paths = [img["image_path"] for img in image_paths[:3]]  # 最多3张
            
            aggregated.append({
                "product_code": product_code,
                "similarity": similarity,
                "image_count": len(results),
                "image_paths": image_paths
            })
        
        # 按相似度排序
        aggregated.sort(key=lambda x: x["similarity"], reverse=True)
        
        return aggregated[:top_k]
    
    # ==================== 日志操作 ====================
    
    def log_search(
        self,
        query_hash: str,
        top_product_code: str,
        similarity: float,
        search_time_ms: int,
        result_count: int
    ):
        """记录检索日志"""
        conn = self._get_connection()
        cursor = conn.cursor()
        
        try:
            sql = """
            INSERT INTO search_log (query_image_hash, top_product_code, similarity_score, search_time_ms, result_count)
            VALUES (%s, %s, %s, %s, %s)
            """
            cursor.execute(sql, (query_hash, top_product_code, similarity, search_time_ms, result_count))
            conn.commit()
        except Exception as e:
            print(f"记录检索日志失败: {e}")
        finally:
            cursor.close()
            conn.close()
    
    def log_ingest(
        self,
        product_code: str,
        image_count: int,
        success_count: int,
        fail_count: int,
        ingest_time_ms: int,
        error_msg: str = None
    ):
        """记录入库日志"""
        conn = self._get_connection()
        cursor = conn.cursor()
        
        try:
            sql = """
            INSERT INTO ingest_log (product_code, image_count, success_count, fail_count, ingest_time_ms, error_msg)
            VALUES (%s, %s, %s, %s, %s, %s)
            """
            cursor.execute(sql, (product_code, image_count, success_count, fail_count, ingest_time_ms, error_msg))
            conn.commit()
        except Exception as e:
            print(f"记录入库日志失败: {e}")
        finally:
            cursor.close()
            conn.close()
    
    def count_searches(self) -> int:
        """统计检索次数"""
        conn = self._get_connection()
        cursor = conn.cursor()
        
        try:
            sql = "SELECT COUNT(*) FROM search_log"
            cursor.execute(sql)
            return cursor.fetchone()[0]
        finally:
            cursor.close()
            conn.close()
