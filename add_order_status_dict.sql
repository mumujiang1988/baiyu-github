-- 添加订单状态字典到数据库
-- 执行时间：2026-03-28

-- 步骤 1：添加字典类型
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

-- 步骤 2：添加字典数据
INSERT INTO sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, status, remark, create_by, create_time, update_by, update_time)
VALUES 
  (REPLACE(UUID(), '-', ''), '000000', 1, '未关闭', 'A', 'order_status', '0', '订单未关闭', 'admin', NOW(), 'admin', NOW()),
  (REPLACE(UUID(), '-', ''), '000000', 2, '已关闭', 'B', 'order_status', '0', '订单已关闭', 'admin', NOW(), 'admin', NOW()),
  (REPLACE(UUID(), '-', ''), '000000', 3, '业务终止', 'C', 'order_status', '0', '订单业务终止', 'admin', NOW(), 'admin', NOW())
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 步骤 3：验证添加结果
SELECT dict_code, dict_label, dict_value, dict_type FROM sys_dict_data WHERE dict_type = 'order_status';
