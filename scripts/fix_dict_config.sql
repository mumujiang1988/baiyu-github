-- =====================================================
-- 修复销售订单管理的字典配置
-- 说明：确保字典配置与本地 dict.json 文件一致
-- 时间：2026-03-28
-- =====================================================

USE test;

-- 1. 先查看当前配置
SELECT 
    config_id,
    module_code,
    config_name,
    JSON_LENGTH(JSON_EXTRACT(dict_config, '$.dictionaries')) as dict_count,
    JSON_EXTRACT(dict_config, '$.dictionaries.salespersons') as salespersons_config,
    JSON_EXTRACT(dict_config, '$.dictionaries.documentStatus') as documentStatus_config,
    JSON_EXTRACT(dict_config, '$.dictionaries.orderStatus') as orderStatus_config
FROM erp_page_config 
WHERE config_name = '销售订单管理'\G

-- 2. 如果需要更新，使用以下 UPDATE 语句
-- 注意：这里只是示例，实际数据库中的配置已经是正确的

UPDATE erp_page_config 
SET dict_config = JSON_SET(
    dict_config,
    '$.dictionaries.orderStatus._note', 
    '注意：数据库中没有 orderStatus 字典类型，这是前端硬编码的静态字典。如需修改请同步更新后端 t_sale_order 表的实际值'
),
update_time = NOW(),
update_by = 'admin',
remark = '添加订单状态字典说明 - 2026-03-28'
WHERE config_name = '销售订单管理';

-- 3. 验证更新结果
SELECT 
    config_id,
    module_code,
    config_name,
    JSON_EXTRACT(dict_config, '$.dictionaries.orderStatus') as orderStatus_full
FROM erp_page_config 
WHERE config_name = '销售订单管理'\G
