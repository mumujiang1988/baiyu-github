"""
MySQL 产品业务服务
"""
import mysql.connector
from mysql.connector import pooling
from typing import List, Dict, Optional
from collections import defaultdict


class ProductService:
    """MySQL 产品业务服务"""

    def __init__(self, host: str, port: int, user: str, password: str, database: str, pool_size: int = 5):
        """初始化 MySQL 连接池"""
        print(f"🔌 连接 MySQL: {host}:{port}/{database}")

        dbconfig = {
            "host": host, "port": port, "user": user, "password": password,
            "database": database, "charset": 'utf8mb4', "collation": 'utf8mb4_unicode_ci'
        }

        try:
            self.connection_pool = pooling.MySQLConnectionPool(pool_name="mysql_pool", pool_size=pool_size, **dbconfig)
        except AttributeError:
            self.connection_pool = pooling.PooledMySQLConnection(pool_name="mysql_pool", pool_size=pool_size, **dbconfig)

        print("✅ MySQL 连接池创建成功")

    def _get_connection(self):
        """获取数据库连接"""
        return self.connection_pool.get_connection()

    def _execute_query(self, sql: str, params: tuple = None, dictionary: bool = False) -> List[Dict]:
        """执行查询"""
        conn = self._get_connection()
        cursor = conn.cursor(dictionary=dictionary)
        try:
            cursor.execute(sql, params or ())
            return cursor.fetchall()
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

    # ==================== 产品操作 ====================

    def upsert_product(self, product_code: str, name: str, spec: Optional[str] = None, category: Optional[str] = None):
        """创建或更新产品"""
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
            print(f"✅ 产品 {product_code} 彻底删除成功")
        finally:
            cursor.close()
            conn.close()

    # ==================== 图片操作 ====================

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

    # ==================== 检索结果聚合 ====================

    def aggregate_results(self, raw_results: List[Dict], aggregation: str = "max", top_k: int = 10) -> List[Dict]:
        """聚合检索结果（按产品编码）"""
        product_results = defaultdict(list)
        for result in raw_results:
            product_results[result["product_code"]].append(result)

        aggregated = []
        for product_code, results in product_results.items():
            if aggregation == "max":
                similarity = max(results, key=lambda x: x["similarity"])["similarity"]
            elif aggregation == "avg":
                similarity = sum(r["similarity"] for r in results) / len(results)
            else:
                similarity = results[0]["similarity"]

            image_paths = [img["image_path"] for img in self.get_product_images(product_code)[:3]]

            aggregated.append({
                "product_code": product_code,
                "similarity": similarity,
                "image_count": len(results),
                "image_paths": image_paths
            })

        aggregated.sort(key=lambda x: x["similarity"], reverse=True)
        return aggregated[:top_k]

    # ==================== 日志操作 ====================

    def log_search(self, query_hash: str, top_product_code: str, similarity: float, search_time_ms: int, result_count: int):
        """记录检索日志"""
        sql = "INSERT INTO search_log (query_image_hash, top_product_code, similarity_score, search_time_ms, result_count) VALUES (%s, %s, %s, %s, %s)"
        try:
            self._execute_update(sql, (query_hash, top_product_code, similarity, search_time_ms, result_count))
        except Exception as e:
            print(f"记录检索日志失败: {e}")

    def log_ingest(self, product_code: str, image_count: int, success_count: int, fail_count: int, ingest_time_ms: int, error_msg: str = None):
        """记录入库日志"""
        sql = "INSERT INTO ingest_log (product_code, image_count, success_count, fail_count, ingest_time_ms, error_msg) VALUES (%s, %s, %s, %s, %s, %s)"
        try:
            self._execute_update(sql, (product_code, image_count, success_count, fail_count, ingest_time_ms, error_msg))
        except Exception as e:
            print(f"记录入库日志失败: {e}")

    def count_searches(self) -> int:
        """统计检索次数"""
        results = self._execute_query("SELECT COUNT(*) as count FROM search_log")
        return results[0]["count"] if results else 0
