-- 修复销售订单配置：添加缺失的静态字典（orderStatus、documentStatus）
-- 执行时间：2026-03-28
-- 问题：数据库 erp_page_config 表中的 dict_config 缺少 orderStatus 和 documentStatus 静态定义

-- 步骤 1：备份当前配置
CREATE TABLE IF NOT EXISTS erp_page_config_backup_20260328_static AS 
SELECT * FROM erp_page_config WHERE module_code = 'saleorder';

-- 步骤 2：更新 dict_config，添加缺失的静态字典
UPDATE erp_page_config 
SET dict_config = JSON_SET(
  dict_config,
  '$.dictionaries.orderStatus',
  CAST('{"type":"static","data":[{"label":"未关闭","value":"A","type":"success"},{"label":"已关闭","value":"B","type":"info"},{"label":"业务终止","value":"C","type":"danger"}]}' AS JSON)
)
WHERE module_code = 'saleorder';

UPDATE erp_page_config 
SET dict_config = JSON_SET(
  dict_config,
  '$.dictionaries.documentStatus',
  CAST('{"type":"static","data":[{"label":"暂存","value":"Z","type":"info"},{"label":"创建","value":"A","type":"success"},{"label":"审核中","value":"B","type":"warning"},{"label":"已审核","value":"C","type":"success"},{"label":"重新审核","value":"D","type":"primary"}]}' AS JSON)
)
WHERE module_code = 'saleorder';

-- 步骤 3：验证更新结果
SELECT 
  module_code,
  JSON_EXTRACT(dict_config, '$.dictionaries.orderStatus') as orderStatus,
  JSON_EXTRACT(dict_config, '$.dictionaries.documentStatus') as documentStatus
FROM erp_page_config 
WHERE module_code = 'saleorder';
