-- ============================================
-- 收款单 relationConfig 配置修复脚本
-- 日期：2026-04-01
-- 问题：缺少 relationConfig 配置导致查询失败
-- ============================================

USE test;

-- ============================================
-- 查看当前收款单配置
-- ============================================
SELECT 
  module_code,
  config_name,
  JSON_EXTRACT(detail_config, '$.detail.tabs[0].tableName') AS tab_table,
  JSON_EXTRACT(detail_config, '$.detail.tabs[0].relationConfig') AS relation_config
FROM erp_page_config
WHERE module_code = 'receivemoney';

-- ============================================
-- 修复方案：更新 detail_config，添加 relationConfig
-- ============================================
UPDATE erp_page_config
SET detail_config = JSON_SET(
  detail_config,
  '$.detail.tabs[0].relationConfig',
  '{
    "masterTable": "f_receivebill",
    "masterField": "id",
    "detailTable": "f_receivebill_entry",
    "detailField": "f_entry_id",
    "operator": "eq"
  }'
)
WHERE module_code = 'receivemoney'
  AND JSON_EXTRACT(detail_config, '$.detail.tabs[0].tableName') = 'f_receivebill_entry'
  AND JSON_EXTRACT(detail_config, '$.detail.tabs[0].relationConfig') IS NULL;

-- ============================================
-- 验证修复结果
-- ============================================
SELECT 
  module_code,
  config_name,
  JSON_EXTRACT(detail_config, '$.detail.tabs[0].relationConfig') AS relation_config
FROM erp_page_config
WHERE module_code = 'receivemoney';

SELECT '✅ 收款单 relationConfig 配置修复完成！' AS message;
