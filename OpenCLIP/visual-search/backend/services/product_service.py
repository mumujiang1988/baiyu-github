"""
MySQL 产品业务服务
"""
import mysql.connector
from mysql.connector import pooling
from typing import List, Dict, Optional
from collections import defaultdict
import logging

logger = logging.getLogger(__name__)


class ProductService:
    """MySQL 产品业务服务"""

    def __init__(self, host: str, port: int, user: str, password: str, database: str, pool_size: int = 5):
        """初始化 MySQL 连接池"""
        logger.info(f"🔌 连接 MySQL: {host}:{port}/{database}")

        dbconfig = {
            "host": host, "port": port, "user": user, "password": password,
            "database": database, "charset": 'utf8mb4', "collation": 'utf8mb4_unicode_ci'
        }

        try:
            self.connection_pool = pooling.MySQLConnectionPool(pool_name="mysql_pool", pool_size=pool_size, **dbconfig)
        except AttributeError:
            self.connection_pool = pooling.PooledMySQLConnection(pool_name="mysql_pool", pool_size=pool_size, **dbconfig)

        logger.info("✅ MySQL 连接池创建成功")

    def _get_connection(self):
        """获取数据库连接"""
        return self.connection_pool.get_connection()

    def _execute_query(self, sql: str, params: tuple = None, dictionary: bool = False) -> List[Dict]:
        """执行查询"""
        conn = self._get_connection()
        cursor = conn.cursor(dictionary=dictionary)
        try:
            cursor.execute(sql, params or ())
            results = cursor.fetchall()
            if dictionary:
                return results
            else:
                return [{"count": row[0]} for row in results]
        finally:
            cursor.close()
            conn.close()

    def _execute_update(self, sql: str, params: tuple = None) -> int:
        """执行更新"""
        conn = self._get_connection()
        cursor = conn.cursor()
        try:
            cursor.execute(sql, params or ())
            conn.commit()
            return cursor.lastrowid
        finally:
            cursor.close()
            conn.close()

    # ==================== Product Operations ====================

    def upsert_product(self, product_code: str, name: Optional[str] = None, spec: Optional[str] = None, category: Optional[str] = None):
        """创建或更新产品"""
        # 如果 name 为空，使用 product_code 作为默认名称
        if not name:
            name = product_code
        
        sql = """
        INSERT INTO product (product_code, name, spec, category)
        VALUES (%s, %s, %s, %s)
        ON DUPLICATE KEY UPDATE
            name = VALUES(name),
            spec = VALUES(spec),
            category = VALUES(category),
            updated_at = NOW()
        """
        self._execute_update(sql, (product_code, name, spec, category))

    def update_product(self, product_code: str, **kwargs):
        """
        更新产品信息（只更新提供的字段）
        
        Args:
            product_code: 产品编码
            **kwargs: 要更新的字段 (name, spec, category)
        """
        if not kwargs:
            return
        
        # 构建动态 SQL
        set_clauses = []
        values = []
        
        for field in ['name', 'spec', 'category']:
            if field in kwargs:
                set_clauses.append(f"{field} = %s")
                values.append(kwargs[field])
        
        if not set_clauses:
            return
        
        # 添加 updated_at
        set_clauses.append("updated_at = NOW()")
        
        # 添加 WHERE 条件的值
        values.append(product_code)
        
        sql = f"UPDATE product SET {', '.join(set_clauses)} WHERE product_code = %s"
        self._execute_update(sql, tuple(values))

    def get_product(self, product_code: str) -> Optional[Dict]:
        """获取产品信息"""
        results = self._execute_query("SELECT * FROM product WHERE product_code = %s", (product_code,), dictionary=True)
        return results[0] if results else None

    def list_products(self, category: Optional[str] = None, page: int = 1, page_size: int = 20) -> List[Dict]:
        """查询产品列表"""
        offset = (page - 1) * page_size

        if category:
            sql = "SELECT * FROM product WHERE category = %s ORDER BY created_at DESC LIMIT %s OFFSET %s"
            params = (category, page_size, offset)
        else:
            sql = "SELECT * FROM product ORDER BY created_at DESC LIMIT %s OFFSET %s"
            params = (page_size, offset)

        return self._execute_query(sql, params, dictionary=True)

    def count_products(self, category: Optional[str] = None) -> int:
        """统计产品数量"""
        if category:
            results = self._execute_query("SELECT COUNT(*) as count FROM product WHERE category = %s", (category,))
        else:
            results = self._execute_query("SELECT COUNT(*) as count FROM product")
        return results[0]["count"] if results else 0

    def delete_product(self, product_code: str):
        """删除产品（彻底删除）"""
        sql1 = "DELETE FROM product_image WHERE product_code = %s"
        sql2 = "DELETE FROM product WHERE product_code = %s"

        conn = self._get_connection()
        cursor = conn.cursor()
        try:
            cursor.execute(sql1, (product_code,))
            cursor.execute(sql2, (product_code,))
            conn.commit()
            logger.info(f"✅ 产品 {product_code} 彻底删除成功")
        finally:
            cursor.close()
            conn.close()

    # ==================== Image Operations ====================

    def insert_image(self, product_code: str, image_path: str, image_hash: str, milvus_id: int, image_size: int = None):
        """插入图片记录"""
        sql = "INSERT INTO product_image (product_code, image_path, image_hash, milvus_id, image_size) VALUES (%s, %s, %s, %s, %s)"
        self._execute_update(sql, (product_code, image_path, image_hash, milvus_id, image_size))

    def image_exists(self, image_hash: str) -> bool:
        """检查图片是否已存在"""
        results = self._execute_query("SELECT COUNT(*) as count FROM product_image WHERE image_hash = %s", (image_hash,))
        return results[0]["count"] > 0

    def get_product_images(self, product_code: str) -> List[Dict]:
        """获取产品的所有图片"""
        sql = "SELECT * FROM product_image WHERE product_code = %s ORDER BY created_at"
        return self._execute_query(sql, (product_code,), dictionary=True)

    def count_images(self) -> int:
        """统计图片数量"""
        results = self._execute_query("SELECT COUNT(*) as count FROM product_image")
        return results[0]["count"] if results else 0

    # ==================== Search Result Aggregation ====================

    def aggregate_results(self, raw_results: List[Dict], aggregation: str = "max", top_k: int = 10) -> List[Dict]:
        """聚合检索结果（按产品编码）- 优化版：批量查询避免 N+1 问题"""
        from collections import defaultdict
        
        # 1. 按产品编码分组
        product_results = defaultdict(list)
        for result in raw_results:
            product_results[result["product_code"]].append(result)

        # 2. 收集所有产品编码
        product_codes = list(product_results.keys())
        
        # 3. 批量查询所有产品的图片（1次 SQL 替代 N 次）
        if product_codes:
            placeholders = ','.join(['%s'] * len(product_codes))
            sql = f"SELECT * FROM product_image WHERE product_code IN ({placeholders}) ORDER BY created_at"
            all_images = self._execute_query(sql, tuple(product_codes), dictionary=True)
            
            # 构建产品编码 -> 图片列表的映射
            images_map = defaultdict(list)
            for img in all_images:
                images_map[img['product_code']].append(img)
        else:
            images_map = {}

        # 4. 聚合结果
        aggregated = []
        for product_code, results in product_results.items():
            # 计算相似度
            if aggregation == "max":
                similarity = max(results, key=lambda x: x["similarity"])["similarity"]
            elif aggregation == "avg":
                similarity = sum(r["similarity"] for r in results) / len(results)
            else:
                similarity = results[0]["similarity"]

            # 从缓存中获取图片路径
            product_images = images_map.get(product_code, [])
            image_paths = [img["image_path"] for img in product_images]

            aggregated.append({
                "product_code": product_code,
                "similarity": similarity,
                "image_count": len(results),
                "image_paths": image_paths
            })

        # 5. 按相似度排序并返回 top_k
        aggregated.sort(key=lambda x: x["similarity"], reverse=True)
        return aggregated[:top_k]

    # ==================== Log Operations ====================

    def log_search(self, query_hash: str, top_product_code: str, similarity: float, search_time_ms: int, result_count: int):
        """记录检索日志"""
        sql = "INSERT INTO search_log (query_image_hash, top_product_code, similarity_score, search_time_ms, result_count) VALUES (%s, %s, %s, %s, %s)"
        try:
            self._execute_update(sql, (query_hash, top_product_code, similarity, search_time_ms, result_count))
        except Exception as e:
            logger.error(f"记录检索日志失败: {e}")

    def log_ingest(self, product_code: str, image_count: int, success_count: int, fail_count: int, ingest_time_ms: int, error_msg: str = None):
        """记录入库日志"""
        sql = "INSERT INTO ingest_log (product_code, image_count, success_count, fail_count, ingest_time_ms, error_msg) VALUES (%s, %s, %s, %s, %s, %s)"
        try:
            self._execute_update(sql, (product_code, image_count, success_count, fail_count, ingest_time_ms, error_msg))
        except Exception as e:
            logger.error(f"记录入库日志失败: {e}")

    def count_searches(self) -> int:
        """统计检索次数"""
        results = self._execute_query("SELECT COUNT(*) as count FROM search_log")
        return results[0]["count"] if results else 0
