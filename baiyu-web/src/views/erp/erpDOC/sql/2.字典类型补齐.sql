-- =====================================================
-- 补全 sys_dict_type 字典类型表
-- 基于 bymaterial_dictionary 表中的业务字典分类
-- 生成时间：2026-03-27
-- 使用动态雪花算法 ID (19 位数字)
-- ID 结构：毫秒级时间戳 (13 位) + 随机数 (6 位)
-- =====================================================

USE test;

-- ============================================
-- Part 0: Pre-flight Check
-- ============================================

-- 清理临时表
DROP TEMPORARY TABLE IF EXISTS tmp_dict_types;

-- 检查数据库连接
SELECT '========================================' AS '';
SELECT '🔍 Checking database connection...' AS step;
SELECT DATABASE() AS current_database;
SELECT USER() AS `current_user`;
SELECT VERSION() AS mysql_version;
SELECT '========================================' AS '';

-- ============================================
-- Part 1: Utility Functions
-- ============================================

-- 雪花 ID 生成函数（与菜单脚本保持一致）
DROP FUNCTION IF EXISTS fn_snowflake_id;
DELIMITER $$
CREATE FUNCTION fn_snowflake_id() RETURNS bigint
    NOT DETERMINISTIC
    NO SQL
BEGIN
    -- 使用简化版的雪花算法，生成 19 位 ID
    -- 格式：时间戳 (13 位毫秒) + 随机数 (6 位)
    DECLARE ts BIGINT DEFAULT UNIX_TIMESTAMP(NOW(3)) * 1000;  -- 毫秒级时间戳
    DECLARE random_part INT DEFAULT FLOOR(RAND() * 999999) + 1;
    
    -- 使用时间戳 * 1000000 + 随机数，确保生成 19 位 ID
    -- 当前毫秒时间戳约 1743897600000（13位），乘以 1000000 后约 1.74e18（19位）
    -- BIGINT 最大值 9.22e18，完全安全
    -- 注意：随机数范围 1-999999，理论上同一毫秒内生成 <100 个 ID 时碰撞概率 <0.5%
    RETURN ts * 1000000 + random_part;
END$$
DELIMITER ;

-- ============================================
-- Part 2: Business Data Layer
-- ============================================

-- 创建临时表存储字典类型数据
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_dict_types (
    dict_name VARCHAR(50),
    dict_type VARCHAR(50),
    remark VARCHAR(200),
    order_num INT
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;

-- 插入字典类型数据（按业务逻辑排序）
INSERT INTO tmp_dict_types VALUES
('币种', 'currency', '币种列表', 1),
('付款条款', 'payment_clause', '付款条款列表', 2),
('贸易方式', 'trade_way', '贸易方式列表', 3),
('产品分类', 'product_category', '产品分类列表', 4),
('客户分类', 'customer_category', '客户分类列表', 5),
('客户分组', 'customer_grouping', '客户分组列表', 6),
('客户来源', 'customer_source', '客户来源列表', 7),
('单据类型', 'document_type', '单据类型列表', 8),
('库存状态', 'inventory_status', '库存状态列表', 9),
('发票类型', 'invoice_type', '发票类型列表', 10),
('价格类型', 'price_type', '价格类型列表', 11),
('产品类型', 'product_type', '产品类型列表', 12),
('供应商分类', 'supplier_classification', '供应商分类列表', 13),
('供应类别', 'supply_category', '供应类别列表', 14),
('包装方式', 'manner_packing', '包装方式列表', 15),
('汇率类型', 'exchange_type', '汇率类型列表', 16),
('收款条款', 'collection_terms', '收款条款列表', 17),
('订单产品分类', 'order_product_category', '订单产品分类列表', 18),
('关税名称', 'tariff_nomenclature', '关税名称列表', 19),
('ERP 分类属性', 'erpClsId_property', 'ERP 分类属性列表', 20),
('销售人员', 'salespersons', '销售人员列表（包含部门、角色信息）', 21),
('订单状态', 'order_status', '订单状态：未关闭/已关闭/业务终止', 22),
('单据状态', 'f_document_status', '单据状态：创建/审核中/已审核/重新审核/暂存', 23),
('客户列表', 'customers', '客户信息列表（来自 bd_customer 表）', 24),
('物料列表', 'materials', '物料信息列表（来自 by_material 表）', 25),
('用户列表', 'users', '用户信息列表（来自 sys_user 表）', 26),
('供应商列表', 'suppliers', '供应商信息列表（来自 supplier 表）', 27),
('部门列表', 'departments', '部门信息列表（来自 sys_dept 表）', 28),
('税率列表', 'tax_rates', '税率信息列表（来自 tax_rate 表）', 29);

-- ============================================
-- Part 3: Core Logic Layer
-- ============================================

-- 开启事务
START TRANSACTION;

-- 批量插入字典类型（使用动态生成的雪花 ID）
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time)
SELECT 
    fn_snowflake_id(),
    t.dict_name,
    t.dict_type,
    '1',
    t.remark,
    'admin',
    NOW()
FROM tmp_dict_types t
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 提交事务
COMMIT;

-- ============================================
-- Part 4: Cleanup & Verification
-- ============================================

-- 清理临时表和函数
DROP TEMPORARY TABLE IF EXISTS tmp_dict_types;
DROP FUNCTION IF EXISTS fn_snowflake_id;

-- 验证结果
SELECT '========================================' AS '';
SELECT ' Dictionary Types Created Successfully!' AS message;
SELECT '========================================' AS '';

SELECT '===== Statistics =====' AS section;
SELECT 
    COUNT(*) AS total_count,
    SUM(CASE WHEN status = '1' THEN 1 ELSE 0 END) AS active_count,
    GROUP_CONCAT(dict_type ORDER BY dict_id SEPARATOR ', ') AS dict_types
FROM sys_dict_type
WHERE dict_type IN (
    'currency', 'payment_clause', 'trade_way', 'product_category',
    'customer_category', 'customer_grouping', 'customer_source',
    'document_type', 'inventory_status', 'invoice_type', 'price_type',
    'product_type', 'supplier_classification', 'supply_category',
    'manner_packing', 'exchange_type', 'collection_terms',
    'order_product_category', 'tariff_nomenclature', 'erpClsId_property',
    'salespersons', 'order_status', 'f_document_status',
    'customers', 'materials', 'users', 'suppliers', 'departments', 'tax_rates'
);

SELECT '========================================' AS '';
SELECT '🎉 Script execution completed!' AS final_message;
SELECT '========================================' AS '';

