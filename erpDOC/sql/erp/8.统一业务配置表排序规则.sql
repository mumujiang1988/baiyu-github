-- =============================================
-- 统一业务配置表和单据关系图表的排序规则
-- 将所有相关表统一为 utf8mb4_0900_ai_ci（MySQL 8.0推荐）
-- 日期: 2026-04-14
-- =============================================

USE test;

-- =============================================
-- 第一步：检查当前状态
-- =============================================
SELECT '========================================' AS '';
SELECT '📊 修复前 - 需要统一的表当前排序规则' AS title;
SELECT '========================================' AS '';

SELECT 
    TABLE_NAME,
    TABLE_COLLATION,
    CASE 
        WHEN TABLE_COLLATION != 'utf8mb4_0900_ai_ci' THEN '需要修复'
        ELSE '已正确'
    END AS status
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_TYPE = 'BASE TABLE'
  AND TABLE_NAME IN (
    -- 单据关系图相关表
    't_identifier_dictionary',
    'erp_bill_relation_config',
    -- 业务配置表
    'saleorder',
    'purchasequotation',
    'purchaseorder',
    'deliverynotice',
    'paymentapply',
    'receivebill',
    'receivenotice',
    'saloutbound',
    'payable',
    'receivable',
    'purchaseinstock',
    'inspection',
    'saleorderinfo'
  )
ORDER BY 
    CASE WHEN TABLE_COLLATION != 'utf8mb4_0900_ai_ci' THEN 0 ELSE 1 END,
    TABLE_NAME;

-- =============================================
-- 第二步：执行统一排序规则
-- =============================================
SELECT '========================================' AS '';
SELECT '🔧 开始统一排序规则...' AS title;
SELECT '========================================' AS '';

-- 单据关系图相关表
ALTER TABLE t_identifier_dictionary 
CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE erp_bill_relation_config 
CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 业务配置表
ALTER TABLE saleorder 
CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE purchasequotation 
CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE purchaseorder 
CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE deliverynotice 
CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE paymentapply 
CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE receivebill 
CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE receivenotice 
CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE saloutbound 
CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE payable 
CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE receivable 
CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE purchaseinstock 
CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE inspection 
CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE saleorderinfo 
CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

SELECT '所有表的排序规则已统一为 utf8mb4_0900_ai_ci' AS message;

-- =============================================
-- 第三步：验证修复结果
-- =============================================
SELECT '========================================' AS '';
SELECT '修复后 - 验证结果' AS title;
SELECT '========================================' AS '';

SELECT 
    TABLE_NAME,
    TABLE_COLLATION,
    ENGINE,
    TABLE_ROWS,
    '已统一' AS status
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_TYPE = 'BASE TABLE'
  AND TABLE_NAME IN (
    't_identifier_dictionary',
    'erp_bill_relation_config',
    'saleorder',
    'purchasequotation',
    'purchaseorder',
    'deliverynotice',
    'paymentapply',
    'receivebill',
    'receivenotice',
    'saloutbound',
    'payable',
    'receivable',
    'purchaseinstock',
    'inspection',
    'saleorderinfo'
  )
ORDER BY TABLE_NAME;

SELECT '========================================' AS '';
SELECT ' 完成！共统一 15 个表的排序规则' AS summary;
SELECT '========================================' AS '';
