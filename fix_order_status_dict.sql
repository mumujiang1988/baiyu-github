-- 修复订单状态字典数据
-- 执行时间：2026-03-28
-- 问题：order_status 字典未在数据库中创建,导致前端无法加载

-- 步骤 1：添加订单状态字典类型
INSERT INTO sys_dict_type (dict_id, tenant_id, dict_name, dict_type, status, remark, create_by, create_time, update_by, update_time)
VALUES (
  REPLACE(UUID(), '-', ''),
  '000000',
  '订单状态',
  'order_status',
  '0',
  '订单状态：未关闭/已关闭/业务终止',
  'admin',
  NOW(),
  'admin',
  NOW()
)
ON DUPLICATE KEY UPDATE dict_name = '订单状态';

-- 步骤 2：添加订单状态字典数据
INSERT INTO sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, status, remark, create_by, create_time, update_by, update_time)
VALUES 
  (REPLACE(UUID(), '-', ''), '000000', 1, '未关闭', 'A', 'order_status', '0', '订单未关闭', 'admin', NOW(), 'admin', NOW()),
  (REPLACE(UUID(), '-', ''), '000000', 2, '已关闭', 'B', 'order_status', '0', '订单已关闭', 'admin', NOW(), 'admin', NOW()),
  (REPLACE(UUID(), '-', ''), '000000', 3, '业务终止', 'C', 'order_status', '0', '订单业务终止', 'admin', NOW(), 'admin', NOW())
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 步骤 3：验证单据状态字典数据完整性
-- 确认 f_document_status 包含所有必要的状态值
SELECT '=== 单据状态字典验证 ===' as info;
SELECT dict_code, dict_sort, dict_label, dict_value, dict_type, status 
FROM sys_dict_data 
WHERE dict_type = 'f_document_status'
ORDER BY dict_sort;

-- 步骤 4：验证订单状态字典创建结果
SELECT '=== 订单状态字典验证 ===' as info;
SELECT dict_code, dict_sort, dict_label, dict_value, dict_type, status 
FROM sys_dict_data 
WHERE dict_type = 'order_status'
ORDER BY dict_sort;

-- 步骤 5：验证字典类型创建结果
SELECT '=== 字典类型验证 ===' as info;
SELECT dict_id, dict_name, dict_type, status, remark
FROM sys_dict_type
WHERE dict_type IN ('order_status', 'f_document_status')
ORDER BY dict_type;
