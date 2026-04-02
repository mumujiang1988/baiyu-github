-- ========================================
-- 企业产品以图搜系统 - 数据库初始化脚本
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS product_search DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE product_search;

-- ========================================
-- 1. 产品信息表
-- ========================================
DROP TABLE IF EXISTS products;

CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '产品唯一ID',
    product_name VARCHAR(255) NOT NULL COMMENT '产品名称',
    product_model VARCHAR(100) COMMENT '产品型号',
    price DECIMAL(10,2) COMMENT '产品价格',
    stock INT DEFAULT 0 COMMENT '库存数量',
    product_image VARCHAR(500) COMMENT '产品图片路径',
    faiss_index INT COMMENT '对应FAISS向量索引ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_faiss (faiss_index),
    INDEX idx_name (product_name),
    INDEX idx_model (product_model)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品信息表';

-- ========================================
-- 2. 向量索引映射表
-- ========================================
DROP TABLE IF EXISTS vector_mapping;

CREATE TABLE vector_mapping (
    faiss_index INT PRIMARY KEY AUTO_INCREMENT COMMENT 'FAISS向量索引ID',
    product_id INT NOT NULL COMMENT '产品ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY uk_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='向量索引映射表';

-- ========================================
-- 3. 操作日志表（可选）
-- ========================================
DROP TABLE IF EXISTS operation_logs;

CREATE TABLE operation_logs (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型（search/add/update/delete）',
    user_id INT COMMENT '用户ID',
    product_id INT COMMENT '产品ID',
    search_result_count INT COMMENT '搜索结果数量',
    similarity_score DECIMAL(5,2) COMMENT '相似度分数',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    
    INDEX idx_operation (operation_type),
    INDEX idx_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ========================================
-- 4. 用户表（可选，用于权限管理）
-- ========================================
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    role VARCHAR(20) DEFAULT 'user' COMMENT '角色（admin/user）',
    status TINYINT DEFAULT 1 COMMENT '状态（1启用 0禁用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ========================================
-- 5. 插入测试数据
-- ========================================

-- 插入测试用户
INSERT INTO users (username, password, real_name, role) VALUES
('admin', 'admin123', '系统管理员', 'admin'),
('sales01', 'sales123', '业务员张三', 'user');

-- ========================================
-- 6. 创建视图（方便查询）
-- ========================================

-- 产品完整信息视图
DROP VIEW IF EXISTS v_product_full;

CREATE VIEW v_product_full AS
SELECT 
    p.id,
    p.product_name,
    p.product_model,
    p.price,
    p.stock,
    p.product_image,
    p.faiss_index,
    p.create_time,
    p.update_time
FROM products p;

-- ========================================
-- 7. 创建存储过程
-- ========================================

-- 根据FAISS索引批量查询产品
DROP PROCEDURE IF EXISTS sp_get_products_by_faiss_indices;

DELIMITER //
CREATE PROCEDURE sp_get_products_by_faiss_indices(
    IN faiss_indices TEXT
)
BEGIN
    -- 动态SQL查询
    SET @sql = CONCAT(
        'SELECT * FROM products WHERE faiss_index IN (', 
        faiss_indices, 
        ')'
    );
    
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END //
DELIMITER ;

-- 记录搜索日志
DROP PROCEDURE IF EXISTS sp_log_search;

DELIMITER //
CREATE PROCEDURE sp_log_search(
    IN p_user_id INT,
    IN p_result_count INT,
    IN p_similarity DECIMAL(5,2),
    IN p_ip_address VARCHAR(50)
)
BEGIN
    INSERT INTO operation_logs (
        operation_type,
        user_id,
        search_result_count,
        similarity_score,
        ip_address
    ) VALUES (
        'search',
        p_user_id,
        p_result_count,
        p_similarity,
        p_ip_address
    );
END //
DELIMITER ;

-- ========================================
-- 8. 数据库初始化完成提示
-- ========================================

SELECT '数据库初始化完成！' AS message;
SELECT COUNT(*) AS table_count FROM information_schema.tables 
WHERE table_schema = 'product_search';
