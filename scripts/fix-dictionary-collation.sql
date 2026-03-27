-- ============================================
-- 修复字典表字符集冲突问题
-- 问题：sys_dict_data 和 bymaterial_dictionary 的字符集不一致
-- 错误：Illegal mix of collations for operation 'UNION'
-- ============================================

-- 1. 查看当前字符集设置
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CHARACTER_SET_NAME,
    COLLATION_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'test'
  AND TABLE_NAME IN ('sys_dict_data', 'bymaterial_dictionary')
  AND COLUMN_NAME IN ('dict_label', 'dict_value', 'name', 'kingdee');

-- 2. 方案 1：修改 bymaterial_dictionary 表的字符集与 sys_dict_data 一致（推荐）
-- 将 name 和 kingdee 字段转换为 utf8mb4_general_ci
ALTER TABLE bymaterial_dictionary
MODIFY COLUMN name VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '名称',
MODIFY COLUMN kingdee VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '金蝶代码';

-- 3. 或者方案 2：在查询时使用 COLLATE 强制转换（临时方案，不需要修改表结构）
-- 修改后的 SQL 应该是：
-- SELECT dict_label COLLATE utf8mb4_general_ci AS label, dict_value COLLATE utf8mb4_general_ci AS value, dict_type AS type
-- FROM sys_dict_data WHERE dict_type = ?
-- UNION ALL
-- SELECT name COLLATE utf8mb4_general_ci AS label, kingdee COLLATE utf8mb4_general_ci AS value, category AS type
-- FROM bymaterial_dictionary WHERE category = ?;

-- 4. 验证字符集是否已统一
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CHARACTER_SET_NAME,
    COLLATION_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'test'
  AND TABLE_NAME IN ('sys_dict_data', 'bymaterial_dictionary')
  AND COLUMN_NAME IN ('dict_label', 'dict_value', 'name', 'kingdee');
