-- 修复销售订单字典配置
-- 日期: 2026-03-28
-- 问题: 字典名称与后端返回的数据不匹配

-- 1. 备份当前配置
CREATE TABLE IF NOT EXISTS erp_page_config_backup_20260328 AS 
SELECT * FROM erp_page_config WHERE module_code = 'saleorder';

-- 2. 修复字典名称映射
UPDATE erp_page_config 
SET dict_config = REPLACE(dict_config, '"paymentTerms"', '"payment_clause"')
WHERE module_code = 'saleorder';

UPDATE erp_page_config 
SET dict_config = REPLACE(dict_config, '"tradeType"', '"trade_way"')
WHERE module_code = 'saleorder';

UPDATE erp_page_config 
SET dict_config = REPLACE(dict_config, '"productCategory"', '"product_category"')
WHERE module_code = 'saleorder';

-- 3. 修复API路径
UPDATE erp_page_config 
SET dict_config = REPLACE(dict_config, '/erp/engine/dict/union/paymentTerms', '/erp/engine/dict/union/payment_clause')
WHERE module_code = 'saleorder';

UPDATE erp_page_config 
SET dict_config = REPLACE(dict_config, '/erp/engine/dict/union/tradeType', '/erp/engine/dict/union/trade_way')
WHERE module_code = 'saleorder';

UPDATE erp_page_config 
SET dict_config = REPLACE(dict_config, '/erp/engine/dict/union/productCategory', '/erp/engine/dict/union/product_category')
WHERE module_code = 'saleorder';

-- 4. 验证修复结果
SELECT module_code, 
       JSON_EXTRACT(dict_config, '$.dictionaries.payment_clause') IS NOT NULL AS payment_clause_fixed,
       JSON_EXTRACT(dict_config, '$.dictionaries.trade_way') IS NOT NULL AS trade_way_fixed,
       JSON_EXTRACT(dict_config, '$.dictionaries.product_category') IS NOT NULL AS product_category_fixed
FROM erp_page_config 
WHERE module_code = 'saleorder';
