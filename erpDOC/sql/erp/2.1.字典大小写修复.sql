-- =====================================================
-- 修复 bymaterial_dictionary 表 category 大小写不一致
-- 补充缺失的 sys_dict_type 类型定义
-- 生成时间：2026-05-01
-- 依赖：需要先创建 fn_snowflake_id 函数（见 2.字典类型补齐.sql）
-- =====================================================

USE test;

-- ============================================
-- Part 1: 预检查
-- ============================================
SELECT '>>> 修复前检查' AS step;

SELECT 'Customer_grouping -> customer_grouping' AS 异常项, COUNT(*) AS 条数
FROM bymaterial_dictionary WHERE category = 'Customer_grouping'
UNION ALL
SELECT 'Invoice_type -> invoice_type', COUNT(*)
FROM bymaterial_dictionary WHERE category = 'Invoice_type'
UNION ALL
SELECT 'Order_product_category -> order_product_category', COUNT(*)
FROM bymaterial_dictionary WHERE category = 'Order_product_category'
UNION ALL
SELECT 'Supply_category -> supply_category', COUNT(*)
FROM bymaterial_dictionary WHERE category = 'Supply_category'
UNION ALL
SELECT 'FACCTYPE -> facctype', COUNT(*)
FROM bymaterial_dictionary WHERE category = 'FACCTYPE';

-- 检查缺失的 sys_dict_type
SELECT dict_type, COUNT(*) AS 已存在
FROM sys_dict_type
WHERE dict_type IN ('facctype', 'tj_reason', 'unit', 'warehouse')
GROUP BY dict_type;

-- ============================================
-- Part 2: 执行修复 (事务保护)
-- ============================================
START TRANSACTION;

-- 2.1 修复 bymaterial_dictionary category 大小写
UPDATE bymaterial_dictionary SET category = 'customer_grouping'       WHERE category = 'Customer_grouping';
UPDATE bymaterial_dictionary SET category = 'invoice_type'            WHERE category = 'Invoice_type';
UPDATE bymaterial_dictionary SET category = 'order_product_category'  WHERE category = 'Order_product_category';
UPDATE bymaterial_dictionary SET category = 'supply_category'         WHERE category = 'Supply_category';
UPDATE bymaterial_dictionary SET category = 'facctype'               WHERE category = 'FACCTYPE';

-- 2.2 补充缺失的 sys_dict_type 类型定义（使用雪花ID，跳过已存在的）
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
SELECT fn_snowflake_id(), t.dict_name, t.dict_type, '1', 'admin', NOW(), t.remark
FROM (
    SELECT '科目类型' AS dict_name, 'facctype' AS dict_type, '科目类型(来自bymaterial_dictionary)' AS remark
    UNION ALL SELECT '调价原因', 'tj_reason', '调价原因(来自bymaterial_dictionary)'
    UNION ALL SELECT '单位', 'unit', '单位(来自bymaterial_dictionary)'
    UNION ALL SELECT '仓库', 'warehouse', '仓库(来自bymaterial_dictionary)'
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type st WHERE st.dict_type = t.dict_type);

COMMIT;

-- ============================================
-- Part 3: 修复后验证
-- ============================================
SELECT '>>> 修复后验证' AS step;

-- 3.1 确认无残留异常大写
SELECT COUNT(*) AS 残留异常
FROM bymaterial_dictionary
WHERE category IN ('Customer_grouping', 'Invoice_type', 'Order_product_category', 'Supply_category', 'FACCTYPE');

-- 3.2 确认修复后记录数一致
SELECT category, COUNT(*) AS 条数 FROM bymaterial_dictionary
WHERE category IN ('customer_grouping', 'invoice_type', 'order_product_category', 'supply_category', 'facctype')
GROUP BY category ORDER BY category;

-- 3.3 确认 sys_dict_type 补充成功
SELECT dict_id, dict_name, dict_type FROM sys_dict_type
WHERE dict_type IN ('facctype', 'tj_reason', 'unit', 'warehouse');

SELECT '>>> 字典大小写修复完成' AS done;
