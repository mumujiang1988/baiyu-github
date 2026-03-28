-- 将静态字典改为动态字典类型
-- 执行时间：2026-03-28
-- 修改：orderStatus 和 documentStatus 改为从 sys_dict_data 表动态获取

-- 步骤 1：备份当前配置
CREATE TABLE IF NOT EXISTS erp_page_config_backup_20260328_dynamic AS 
SELECT * FROM erp_page_config WHERE module_code = 'saleorder';

-- 步骤 2：删除旧的静态字典定义
UPDATE erp_page_config 
SET dict_config = JSON_REMOVE(dict_config, '$.dictionaries.orderStatus')
WHERE module_code = 'saleorder';

UPDATE erp_page_config 
SET dict_config = JSON_REMOVE(dict_config, '$.dictionaries.documentStatus')
WHERE module_code = 'saleorder';

-- 步骤 3：添加动态字典定义
UPDATE erp_page_config 
SET dict_config = JSON_SET(
  dict_config,
  '$.dictionaries.orderStatus',
  CAST('{"type":"dynamic","config":{"api":"/erp/engine/dict/union/order_status","ttl":600000,"labelField":"dict_label","valueField":"dict_value","useGlobalCache":true}}' AS JSON)
)
WHERE module_code = 'saleorder';

UPDATE erp_page_config 
SET dict_config = JSON_SET(
  dict_config,
  '$.dictionaries.documentStatus',
  CAST('{"type":"dynamic","config":{"api":"/erp/engine/dict/union/f_document_status","ttl":600000,"labelField":"dict_label","valueField":"dict_value","useGlobalCache":true}}' AS JSON)
)
WHERE module_code = 'saleorder';

-- 步骤 4：验证更新结果
SELECT 
  module_code,
  JSON_EXTRACT(dict_config, '$.dictionaries.orderStatus') as orderStatus,
  JSON_EXTRACT(dict_config, '$.dictionaries.documentStatus') as documentStatus
FROM erp_page_config 
WHERE module_code = 'saleorder';
