-- ERP Public Configuration Management - Menu Initialization SQL
-- Manual Maintenance Script
-- Updated: 2026-04-02
-- Description: ERP 菜单初始化脚本（包含完整的旧数据清理机制）
-- Version: 2.0 (生产环境优化版)
-- Scope: 适用于 test 数据库

USE test;

-- ============================================
-- 第一步：清理旧数据（级联删除 ERP 业务菜单下的所有子菜单和按钮）
-- ============================================

-- 先清理所有可能的临时表（防止脚本失败后残留）
DROP TEMPORARY TABLE IF EXISTS tmp_menu_ids;
DROP TEMPORARY TABLE IF EXISTS tmp_config_buttons;
DROP TEMPORARY TABLE IF EXISTS tmp_receivebill_buttons;
DROP TEMPORARY TABLE IF EXISTS tmp_paymentapply_buttons;
DROP TEMPORARY TABLE IF EXISTS tmp_purchaseorder_buttons;
DROP TEMPORARY TABLE IF EXISTS tmp_receivenotice_buttons;
DROP TEMPORARY TABLE IF EXISTS tmp_inspection_buttons;
DROP TEMPORARY TABLE IF EXISTS tmp_purchaseinstock_buttons;
DROP TEMPORARY TABLE IF EXISTS tmp_salesorder_buttons;
DROP TEMPORARY TABLE IF EXISTS tmp_purchasequotation_buttons;
DROP TEMPORARY TABLE IF EXISTS tmp_deliverynotice_buttons;
DROP TEMPORARY TABLE IF EXISTS tmp_saloutbound_buttons;

SET FOREIGN_KEY_CHECKS = 0;

-- 使用变量避免重复子查询
SET @erp_menu_id := (SELECT menu_id FROM sys_menu WHERE BINARY menu_name = 'ERP 业务菜单' LIMIT 1);

-- 1. 删除所有子菜单的按钮（使用 JOIN，如果@erp_menu_id 为 NULL 则不删除任何数据）
DELETE sm FROM sys_menu sm
INNER JOIN sys_menu parent ON sm.parent_id = parent.menu_id
WHERE parent.menu_id = IFNULL(@erp_menu_id, -1);

-- 2. 收集子菜单 ID 到临时表
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_menu_ids (
  menu_id BIGINT
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;

INSERT INTO tmp_menu_ids (menu_id)
SELECT menu_id FROM sys_menu WHERE parent_id = IFNULL(@erp_menu_id, -1);

-- 3. 清理角色权限关联
DELETE FROM sys_role_menu 
WHERE menu_id IN (SELECT menu_id FROM tmp_menu_ids)
   OR menu_id = IFNULL(@erp_menu_id, -1);

-- 4. 删除子菜单本身
DELETE FROM sys_menu WHERE parent_id = IFNULL(@erp_menu_id, -1);

-- 5. 删除 ERP 业务菜单本身
DELETE FROM sys_menu WHERE menu_id = IFNULL(@erp_menu_id, -1);

-- 6. 清空并删除临时表
DELETE FROM tmp_menu_ids;
DROP TEMPORARY TABLE IF EXISTS tmp_menu_ids;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第二步：开启事务（确保原子性）
-- ============================================
START TRANSACTION;

-- ============================================
-- 第三步：创建或更新雪花 ID 生成函数
DROP FUNCTION IF EXISTS fn_snowflake_id;
DELIMITER $$
CREATE FUNCTION fn_snowflake_id() RETURNS bigint
    NOT DETERMINISTIC
    READS SQL DATA
BEGIN
    -- 获取当前时间戳 (毫秒级，13 位)
    DECLARE ts BIGINT DEFAULT UNIX_TIMESTAMP(NOW()) * 1000 + FLOOR(MICROSECOND(NOW()) / 1000);
    
    -- 机器 ID (使用固定值 0，避免随机性导致的主从复制问题)
    DECLARE machine_id INT DEFAULT 0;
    
    -- 序列号 (使用更大的随机值，范围 0-4095)
    DECLARE sequence INT DEFAULT FLOOR(RAND() * 4096);
    
    -- 添加额外的随机因子 (1-9999)
    DECLARE random_factor INT DEFAULT FLOOR(RAND() * 9999) + 1;
    
    -- 组合雪花 ID: 时间戳左移 22 位 + 机器 ID 左移 12 位 + 序列号
    -- 由于 MySQL 不支持位移运算，使用数学计算模拟
    -- snowflake = (ts << 22) | (machine_id << 12) | sequence
    -- 简化版本：直接拼接字符串，加入随机因子
    DECLARE sf_id BIGINT;
    SET sf_id = CAST(CONCAT(SUBSTRING(ts, 1, 10), LPAD(machine_id, 5, '0'), LPAD(sequence, 4, '0')) AS UNSIGNED);
    
    -- 应用随机因子，使 ID 变化更大
    SET sf_id = sf_id + random_factor;
    
    RETURN sf_id;
END$$
DELIMITER ;

-- ============================================
-- 第四步：创建 ERP 业务菜单父级
-- ============================================

-- Create parent menu (ERP Business Menu)
SET @erp_parent_id := fn_snowflake_id();
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, remark, create_by, create_time)
SELECT @erp_parent_id, 'ERP 业务菜单', 
       0, 5, 'business', '', 0, 0, 'M', 1, 1, '', 'system', 
       'ERP 业务菜单目录', 'admin', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE BINARY menu_name = 'ERP 业务菜单'
);

-- ============================================
-- 第五步：创建公共配置管理菜单及按钮权限
-- ============================================

-- Create config management menu
SET @config_menu_id := fn_snowflake_id();
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @config_menu_id, '公共配置管理', @erp_parent_id, 2, 'erp/config', 
        'erp/config/index', '', 0, 0, 'C', 1, 1, 
        'erp:config:query', 'dict', 'admin', NOW(), '', NULL, 'ERP 公共配置管理页面'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu 
    WHERE BINARY menu_name = '公共配置管理'
    AND parent_id = @erp_parent_id
);

-- Create button permissions using temporary table
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_config_buttons (
    menu_id VARCHAR(20),
    menu_name VARCHAR(50),
    parent_id BIGINT,
    order_num INT,
    perms VARCHAR(100),
    status CHAR(1) DEFAULT '1'
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;

DELETE FROM tmp_config_buttons;

INSERT INTO tmp_config_buttons (menu_id, menu_name, parent_id, order_num, perms, status) VALUES
(fn_snowflake_id(), '查询', @config_menu_id, 1, 'erp:config:query', '1'),
(fn_snowflake_id(), '新增', @config_menu_id, 2, 'erp:config:add', '1'),
(fn_snowflake_id(), '修改', @config_menu_id, 3, 'erp:config:edit', '1'),
(fn_snowflake_id(), '删除', @config_menu_id, 4, 'erp:config:remove', '1'),
(fn_snowflake_id(), '导出', @config_menu_id, 5, 'erp:config:export', '1'),
(fn_snowflake_id(), '导入', @config_menu_id, 6, 'erp:config:import', '1'),
(fn_snowflake_id(), '复制', @config_menu_id, 7, 'erp:config:copy', '1'),
(fn_snowflake_id(), '历史版本', @config_menu_id, 8, 'erp:config:history', '1'),
(fn_snowflake_id(), '回滚', @config_menu_id, 9, 'erp:config:rollback', '1'),
(fn_snowflake_id(), '状态管理', @config_menu_id, 10, 'erp:config:status', '1');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT t.menu_id, t.menu_name, @config_menu_id, t.order_num, '', '', '', 0, 0, 'F', 1, 1, t.perms, '#', 'admin', NOW(), '', NULL, ''
FROM tmp_config_buttons t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu s 
    WHERE BINARY s.menu_name = BINARY t.menu_name
    AND s.parent_id = @config_menu_id
);

DROP TEMPORARY TABLE IF EXISTS tmp_config_buttons;

-- ============================================
-- 第六步：创建 ERP 单据页面菜单配置
-- ============================================

-- 1. 收款单菜单
SET @receivebill_menu_id := fn_snowflake_id();
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @receivebill_menu_id, '收款单管理', @erp_parent_id, 10, 'erp/receivebill', 
        'erp/pageTemplate/configurable/BusinessConfigurable/index', '{"moduleCode":"receivebill"}', 0, 0, 'C', 1, 1, 
        'k3:receivebill:query', 'money', 'admin', NOW(), '', NULL, '收款单管理页面'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu 
    WHERE BINARY menu_name = '收款单管理'
);

-- 收款单按钮权限
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_receivebill_buttons (
    menu_id VARCHAR(20),
    menu_name VARCHAR(50),
    parent_id BIGINT,
    order_num INT,
    perms VARCHAR(100),
    status CHAR(1) DEFAULT '1'
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;

DELETE FROM tmp_receivebill_buttons;

INSERT INTO tmp_receivebill_buttons (menu_id, menu_name, parent_id, order_num, perms, status) VALUES
(fn_snowflake_id(), '查询', @receivebill_menu_id, 1, 'k3:receivebill:query', '1'),
(fn_snowflake_id(), '新增', @receivebill_menu_id, 2, 'k3:receivebill:add', '1'),
(fn_snowflake_id(), '修改', @receivebill_menu_id, 3, 'k3:receivebill:edit', '1'),
(fn_snowflake_id(), '删除', @receivebill_menu_id, 4, 'k3:receivebill:remove', '1'),
(fn_snowflake_id(), '审核', @receivebill_menu_id, 5, 'k3:receivebill:audit', '1'),
(fn_snowflake_id(), '反审核', @receivebill_menu_id, 6, 'k3:receivebill:unAudit', '1'),
(fn_snowflake_id(), '导出', @receivebill_menu_id, 7, 'k3:receivebill:export', '1');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT t.menu_id, t.menu_name, @receivebill_menu_id, t.order_num, '', '', '', 0, 0, 'F', 1, 1, t.perms, '#', 'admin', NOW(), '', NULL, ''
FROM tmp_receivebill_buttons t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu s 
    WHERE BINARY s.menu_name = BINARY t.menu_name
    AND s.parent_id = @receivebill_menu_id
);

DROP TEMPORARY TABLE IF EXISTS tmp_receivebill_buttons;

-- 2. 付款申请单菜单
SET @paymentapply_menu_id := fn_snowflake_id();
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @paymentapply_menu_id, '付款申请单管理', @erp_parent_id, 11, 'erp/paymentapply', 
        'erp/pageTemplate/configurable/BusinessConfigurable/index', '{"moduleCode":"paymentapply"}', 0, 0, 'C', 1, 1, 
        'k3:paymentapply:query', 'money', 'admin', NOW(), '', NULL, '付款申请单管理页面'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu 
    WHERE BINARY menu_name = '付款申请单管理'
);

-- 付款申请单按钮权限
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_paymentapply_buttons (
    menu_id VARCHAR(20),
    menu_name VARCHAR(50),
    parent_id BIGINT,
    order_num INT,
    perms VARCHAR(100),
    status CHAR(1) DEFAULT '1'
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;

DELETE FROM tmp_paymentapply_buttons;

INSERT INTO tmp_paymentapply_buttons (menu_id, menu_name, parent_id, order_num, perms, status) VALUES
(fn_snowflake_id(), '查询', @paymentapply_menu_id, 1, 'k3:paymentapply:query', '1'),
(fn_snowflake_id(), '新增', @paymentapply_menu_id, 2, 'k3:paymentapply:add', '1'),
(fn_snowflake_id(), '修改', @paymentapply_menu_id, 3, 'k3:paymentapply:edit', '1'),
(fn_snowflake_id(), '删除', @paymentapply_menu_id, 4, 'k3:paymentapply:remove', '1'),
(fn_snowflake_id(), '审核', @paymentapply_menu_id, 5, 'k3:paymentapply:audit', '1'),
(fn_snowflake_id(), '反审核', @paymentapply_menu_id, 6, 'k3:paymentapply:unAudit', '1'),
(fn_snowflake_id(), '导出', @paymentapply_menu_id, 7, 'k3:paymentapply:export', '1');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT t.menu_id, t.menu_name, @paymentapply_menu_id, t.order_num, '', '', '', 0, 0, 'F', 1, 1, t.perms, '#', 'admin', NOW(), '', NULL, ''
FROM tmp_paymentapply_buttons t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu s 
    WHERE BINARY s.menu_name = BINARY t.menu_name
    AND s.parent_id = @paymentapply_menu_id
);

DROP TEMPORARY TABLE IF EXISTS tmp_paymentapply_buttons;

-- 3. 采购订单菜单
SET @purchaseorder_menu_id := fn_snowflake_id();
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @purchaseorder_menu_id, '采购订单管理', @erp_parent_id, 12, 'erp/purchaseorder', 
        'erp/pageTemplate/configurable/BusinessConfigurable/index', '{"moduleCode":"purchaseorder"}', 0, 0, 'C', 1, 1, 
        'k3:purchaseorder:query', 'shopping', 'admin', NOW(), '', NULL, '采购订单管理页面'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu 
    WHERE BINARY menu_name = '采购订单管理'
    AND parent_id = @erp_parent_id
);

-- 采购订单按钮权限
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_purchaseorder_buttons (
    menu_id VARCHAR(20),
    menu_name VARCHAR(50),
    parent_id BIGINT,
    order_num INT,
    perms VARCHAR(100),
    status CHAR(1) DEFAULT '1'
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;

DELETE FROM tmp_purchaseorder_buttons;

INSERT INTO tmp_purchaseorder_buttons (menu_id, menu_name, parent_id, order_num, perms, status) VALUES
(fn_snowflake_id(), '查询', @purchaseorder_menu_id, 1, 'k3:purchaseorder:query', '1'),
(fn_snowflake_id(), '新增', @purchaseorder_menu_id, 2, 'k3:purchaseorder:add', '1'),
(fn_snowflake_id(), '修改', @purchaseorder_menu_id, 3, 'k3:purchaseorder:edit', '1'),
(fn_snowflake_id(), '删除', @purchaseorder_menu_id, 4, 'k3:purchaseorder:remove', '1'),
(fn_snowflake_id(), '审核', @purchaseorder_menu_id, 5, 'k3:purchaseorder:audit', '1'),
(fn_snowflake_id(), '反审核', @purchaseorder_menu_id, 6, 'k3:purchaseorder:unAudit', '1'),
(fn_snowflake_id(), '导出', @purchaseorder_menu_id, 7, 'k3:purchaseorder:export', '1');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT t.menu_id, t.menu_name, @purchaseorder_menu_id, t.order_num, '', '', '', 0, 0, 'F', 1, 1, t.perms, '#', 'admin', NOW(), '', NULL, ''
FROM tmp_purchaseorder_buttons t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu s 
    WHERE BINARY s.menu_name = BINARY t.menu_name
    AND s.parent_id = @purchaseorder_menu_id
);

DROP TEMPORARY TABLE IF EXISTS tmp_purchaseorder_buttons;

-- 4. 收料通知单菜单
SET @receivenotice_menu_id := fn_snowflake_id();
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @receivenotice_menu_id, '收料通知单管理', @erp_parent_id, 13, 'erp/receivenotice', 
        'erp/pageTemplate/configurable/BusinessConfigurable/index', '{"moduleCode":"receivenotice"}', 0, 0, 'C', 1, 1, 
        'k3:receivenotice:query', 'list', 'admin', NOW(), '', NULL, '收料通知单管理页面'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu 
    WHERE BINARY menu_name = '收料通知单管理'
);

-- 收料通知单按钮权限
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_receivenotice_buttons (
    menu_id VARCHAR(20),
    menu_name VARCHAR(50),
    parent_id BIGINT,
    order_num INT,
    perms VARCHAR(100),
    status CHAR(1) DEFAULT '1'
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;

DELETE FROM tmp_receivenotice_buttons;

INSERT INTO tmp_receivenotice_buttons (menu_id, menu_name, parent_id, order_num, perms, status) VALUES
(fn_snowflake_id(), '查询', @receivenotice_menu_id, 1, 'k3:receivenotice:query', '1'),
(fn_snowflake_id(), '新增', @receivenotice_menu_id, 2, 'k3:receivenotice:add', '1'),
(fn_snowflake_id(), '修改', @receivenotice_menu_id, 3, 'k3:receivenotice:edit', '1'),
(fn_snowflake_id(), '删除', @receivenotice_menu_id, 4, 'k3:receivenotice:remove', '1'),
(fn_snowflake_id(), '审核', @receivenotice_menu_id, 5, 'k3:receivenotice:audit', '1'),
(fn_snowflake_id(), '反审核', @receivenotice_menu_id, 6, 'k3:receivenotice:unAudit', '1'),
(fn_snowflake_id(), '导出', @receivenotice_menu_id, 7, 'k3:receivenotice:export', '1');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT t.menu_id, t.menu_name, @receivenotice_menu_id, t.order_num, '', '', '', 0, 0, 'F', 1, 1, t.perms, '#', 'admin', NOW(), '', NULL, ''
FROM tmp_receivenotice_buttons t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu s 
    WHERE BINARY s.menu_name = BINARY t.menu_name
    AND s.parent_id = @receivenotice_menu_id
);

DROP TEMPORARY TABLE IF EXISTS tmp_receivenotice_buttons;

-- 5. 检验单菜单
SET @inspection_menu_id := fn_snowflake_id();
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @inspection_menu_id, '检验单管理', @erp_parent_id, 14, 'erp/inspection', 
        'erp/pageTemplate/configurable/BusinessConfigurable/index', '{"moduleCode":"inspection"}', 0, 0, 'C', 1, 1, 
        'k3:inspection:query', 'clipboard', 'admin', NOW(), '', NULL, '检验单管理页面'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu 
    WHERE BINARY menu_name = '检验单管理'
);

-- 检验单按钮权限
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_inspection_buttons (
    menu_id VARCHAR(20),
    menu_name VARCHAR(50),
    parent_id BIGINT,
    order_num INT,
    perms VARCHAR(100),
    status CHAR(1) DEFAULT '1'
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;

DELETE FROM tmp_inspection_buttons;

INSERT INTO tmp_inspection_buttons (menu_id, menu_name, parent_id, order_num, perms, status) VALUES
(fn_snowflake_id(), '查询', @inspection_menu_id, 1, 'k3:inspection:query', '1'),
(fn_snowflake_id(), '新增', @inspection_menu_id, 2, 'k3:inspection:add', '1'),
(fn_snowflake_id(), '修改', @inspection_menu_id, 3, 'k3:inspection:edit', '1'),
(fn_snowflake_id(), '删除', @inspection_menu_id, 4, 'k3:inspection:remove', '1'),
(fn_snowflake_id(), '审核', @inspection_menu_id, 5, 'k3:inspection:audit', '1'),
(fn_snowflake_id(), '反审核', @inspection_menu_id, 6, 'k3:inspection:unAudit', '1'),
(fn_snowflake_id(), '导出', @inspection_menu_id, 7, 'k3:inspection:export', '1');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT t.menu_id, t.menu_name, @inspection_menu_id, t.order_num, '', '', '', 0, 0, 'F', 1, 1, t.perms, '#', 'admin', NOW(), '', NULL, ''
FROM tmp_inspection_buttons t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu s 
    WHERE BINARY s.menu_name = BINARY t.menu_name
    AND s.parent_id = @inspection_menu_id
);

DROP TEMPORARY TABLE IF EXISTS tmp_inspection_buttons;

-- 6. 采购入库单菜单
SET @purchaseinstock_menu_id := fn_snowflake_id();
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @purchaseinstock_menu_id, '采购入库单管理', @erp_parent_id, 15, 'erp/purchaseinstock', 
        'erp/pageTemplate/configurable/BusinessConfigurable/index', '{"moduleCode":"purchaseinstock"}', 0, 0, 'C', 1, 1, 
        'k3:purchaseinstock:query', 'form', 'admin', NOW(), '', NULL, '采购入库单管理页面'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu 
    WHERE BINARY menu_name = '采购入库单管理'
);

-- 采购入库单按钮权限
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_purchaseinstock_buttons (
    menu_id VARCHAR(20),
    menu_name VARCHAR(50),
    parent_id BIGINT,
    order_num INT,
    perms VARCHAR(100),
    status CHAR(1) DEFAULT '1'
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;

DELETE FROM tmp_purchaseinstock_buttons;

INSERT INTO tmp_purchaseinstock_buttons (menu_id, menu_name, parent_id, order_num, perms, status) VALUES
(fn_snowflake_id(), '查询', @purchaseinstock_menu_id, 1, 'k3:purchaseinstock:query', '1'),
(fn_snowflake_id(), '新增', @purchaseinstock_menu_id, 2, 'k3:purchaseinstock:add', '1'),
(fn_snowflake_id(), '修改', @purchaseinstock_menu_id, 3, 'k3:purchaseinstock:edit', '1'),
(fn_snowflake_id(), '删除', @purchaseinstock_menu_id, 4, 'k3:purchaseinstock:remove', '1'),
(fn_snowflake_id(), '审核', @purchaseinstock_menu_id, 5, 'k3:purchaseinstock:audit', '1'),
(fn_snowflake_id(), '反审核', @purchaseinstock_menu_id, 6, 'k3:purchaseinstock:unAudit', '1'),
(fn_snowflake_id(), '导出', @purchaseinstock_menu_id, 7, 'k3:purchaseinstock:export', '1');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT t.menu_id, t.menu_name, @purchaseinstock_menu_id, t.order_num, '', '', '', 0, 0, 'F', 1, 1, t.perms, '#', 'admin', NOW(), '', NULL, ''
FROM tmp_purchaseinstock_buttons t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu s 
    WHERE BINARY s.menu_name = BINARY t.menu_name
    AND s.parent_id = @purchaseinstock_menu_id
);

DROP TEMPORARY TABLE IF EXISTS tmp_purchaseinstock_buttons;

-- 7. 销售订单菜单（注意：path 保持 salesorder 以兼容历史数据）
SET @salesorder_menu_id := fn_snowflake_id();
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @salesorder_menu_id, '销售订单管理', @erp_parent_id, 17, 'erp/salesorder', 
        'erp/pageTemplate/configurable/BusinessConfigurable/index', '{"moduleCode":"saleorder"}', 0, 0, 'C', 1, 1, 
        'k3:saleorder:query', 'shopping', 'admin', NOW(), '', NULL, '销售订单管理页面'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu 
    WHERE menu_name = '销售订单管理'
    AND parent_id = @erp_parent_id
);

-- 销售订单按钮权限
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_salesorder_buttons (
    menu_id VARCHAR(20),
    menu_name VARCHAR(50),
    parent_id BIGINT,
    order_num INT,
    perms VARCHAR(100),
    status CHAR(1) DEFAULT '1'
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;

DELETE FROM tmp_salesorder_buttons;

INSERT INTO tmp_salesorder_buttons (menu_id, menu_name, parent_id, order_num, perms, status) VALUES
(fn_snowflake_id(), '查询', @salesorder_menu_id, 1, 'k3:saleorder:query', '1'),
(fn_snowflake_id(), '新增', @salesorder_menu_id, 2, 'k3:saleorder:add', '1'),
(fn_snowflake_id(), '修改', @salesorder_menu_id, 3, 'k3:saleorder:edit', '1'),
(fn_snowflake_id(), '删除', @salesorder_menu_id, 4, 'k3:saleorder:remove', '1'),
(fn_snowflake_id(), '审核', @salesorder_menu_id, 5, 'k3:saleorder:audit', '1'),
(fn_snowflake_id(), '反审核', @salesorder_menu_id, 6, 'k3:saleorder:unAudit', '1'),
(fn_snowflake_id(), '导出', @salesorder_menu_id, 7, 'k3:saleorder:export', '1');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT t.menu_id, t.menu_name, @salesorder_menu_id, t.order_num, '', '', '', 0, 0, 'F', 1, 1, t.perms, '#', 'admin', NOW(), '', NULL, ''
FROM tmp_salesorder_buttons t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu s 
    WHERE BINARY s.menu_name = BINARY t.menu_name
    AND s.parent_id = @salesorder_menu_id
);

DROP TEMPORARY TABLE IF EXISTS tmp_salesorder_buttons;

-- 8. 采购报价单菜单
SET @purchasequotation_menu_id := fn_snowflake_id();
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @purchasequotation_menu_id, '采购报价单管理', @erp_parent_id, 18, 'erp/purchasequotation', 
        'erp/pageTemplate/configurable/BusinessConfigurable/index', '{"moduleCode":"purchasequotation"}', 0, 0, 'C', 1, 1, 
        'k3:purchasequotation:query', 'list', 'admin', NOW(), '', NULL, '采购报价单管理页面'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu 
    WHERE menu_name = '采购报价单管理'
);

-- 采购报价单按钮权限
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_purchasequotation_buttons (
    menu_id VARCHAR(20),
    menu_name VARCHAR(50),
    parent_id BIGINT,
    order_num INT,
    perms VARCHAR(100),
    status CHAR(1) DEFAULT '1'
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;

DELETE FROM tmp_purchasequotation_buttons;

INSERT INTO tmp_purchasequotation_buttons (menu_id, menu_name, parent_id, order_num, perms, status) VALUES
(fn_snowflake_id(), '查询', @purchasequotation_menu_id, 1, 'k3:purchasequotation:query', '1'),
(fn_snowflake_id(), '新增', @purchasequotation_menu_id, 2, 'k3:purchasequotation:add', '1'),
(fn_snowflake_id(), '修改', @purchasequotation_menu_id, 3, 'k3:purchasequotation:edit', '1'),
(fn_snowflake_id(), '删除', @purchasequotation_menu_id, 4, 'k3:purchasequotation:remove', '1'),
(fn_snowflake_id(), '审核', @purchasequotation_menu_id, 5, 'k3:purchasequotation:audit', '1'),
(fn_snowflake_id(), '反审核', @purchasequotation_menu_id, 6, 'k3:purchasequotation:unAudit', '1'),
(fn_snowflake_id(), '导出', @purchasequotation_menu_id, 7, 'k3:purchasequotation:export', '1');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT t.menu_id, t.menu_name, @purchasequotation_menu_id, t.order_num, '', '', '', 0, 0, 'F', 1, 1, t.perms, '#', 'admin', NOW(), '', NULL, ''
FROM tmp_purchasequotation_buttons t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu s 
    WHERE BINARY s.menu_name = BINARY t.menu_name
    AND s.parent_id = @purchasequotation_menu_id
);

DROP TEMPORARY TABLE IF EXISTS tmp_purchasequotation_buttons;

-- 9. 发货通知单菜单
SET @deliverynotice_menu_id := fn_snowflake_id();
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @deliverynotice_menu_id, '发货通知单管理', @erp_parent_id, 19, 'erp/deliverynotice', 
        'erp/pageTemplate/configurable/BusinessConfigurable/index', '{"moduleCode":"deliverynotice"}', 0, 0, 'C', 1, 1, 
        'k3:deliverynotice:query', 'postcard', 'admin', NOW(), '', NULL, '发货通知单管理页面'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu 
    WHERE BINARY menu_name = '发货通知单管理'
);

-- 发货通知单按钮权限
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_deliverynotice_buttons (
    menu_id VARCHAR(20),
    menu_name VARCHAR(50),
    parent_id BIGINT,
    order_num INT,
    perms VARCHAR(100),
    status CHAR(1) DEFAULT '1'
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;

DELETE FROM tmp_deliverynotice_buttons;

INSERT INTO tmp_deliverynotice_buttons (menu_id, menu_name, parent_id, order_num, perms, status) VALUES
(fn_snowflake_id(), '查询', @deliverynotice_menu_id, 1, 'k3:deliverynotice:query', '1'),
(fn_snowflake_id(), '新增', @deliverynotice_menu_id, 2, 'k3:deliverynotice:add', '1'),
(fn_snowflake_id(), '修改', @deliverynotice_menu_id, 3, 'k3:deliverynotice:edit', '1'),
(fn_snowflake_id(), '删除', @deliverynotice_menu_id, 4, 'k3:deliverynotice:remove', '1'),
(fn_snowflake_id(), '审核', @deliverynotice_menu_id, 5, 'k3:deliverynotice:audit', '1'),
(fn_snowflake_id(), '反审核', @deliverynotice_menu_id, 6, 'k3:deliverynotice:unAudit', '1'),
(fn_snowflake_id(), '导出', @deliverynotice_menu_id, 7, 'k3:deliverynotice:export', '1');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT t.menu_id, t.menu_name, @deliverynotice_menu_id, t.order_num, '', '', '', 0, 0, 'F', 1, 1, t.perms, '#', 'admin', NOW(), '', NULL, ''
FROM tmp_deliverynotice_buttons t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu s 
    WHERE BINARY s.menu_name = BINARY t.menu_name
    AND s.parent_id = @deliverynotice_menu_id
);

DROP TEMPORARY TABLE IF EXISTS tmp_deliverynotice_buttons;

-- 10. 销售出库单菜单
SET @saloutbound_menu_id := fn_snowflake_id();
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @saloutbound_menu_id, '销售出库单管理', @erp_parent_id, 20, 'erp/saloutbound', 
        'erp/pageTemplate/configurable/BusinessConfigurable/index', '{"moduleCode":"saloutbound"}', 0, 0, 'C', 1, 1, 
        'k3:saloutbound:query', 'list', 'admin', NOW(), '', NULL, '销售出库单管理页面'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu 
    WHERE BINARY menu_name = '销售出库单管理'
);

-- 销售出库单按钮权限
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_saloutbound_buttons (
    menu_id VARCHAR(20),
    menu_name VARCHAR(50),
    parent_id BIGINT,
    order_num INT,
    perms VARCHAR(100),
    status CHAR(1) DEFAULT '1'
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;

DELETE FROM tmp_saloutbound_buttons;

INSERT INTO tmp_saloutbound_buttons (menu_id, menu_name, parent_id, order_num, perms, status) VALUES
(fn_snowflake_id(), '查询', @saloutbound_menu_id, 1, 'k3:saloutbound:query', '1'),
(fn_snowflake_id(), '新增', @saloutbound_menu_id, 2, 'k3:saloutbound:add', '1'),
(fn_snowflake_id(), '修改', @saloutbound_menu_id, 3, 'k3:saloutbound:edit', '1'),
(fn_snowflake_id(), '删除', @saloutbound_menu_id, 4, 'k3:saloutbound:remove', '1'),
(fn_snowflake_id(), '审核', @saloutbound_menu_id, 5, 'k3:saloutbound:audit', '1'),
(fn_snowflake_id(), '反审核', @saloutbound_menu_id, 6, 'k3:saloutbound:unAudit', '1'),
(fn_snowflake_id(), '导出', @saloutbound_menu_id, 7, 'k3:saloutbound:export', '1');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT t.menu_id, t.menu_name, @saloutbound_menu_id, t.order_num, '', '', '', 0, 0, 'F', 1, 1, t.perms, '#', 'admin', NOW(), '', NULL, ''
FROM tmp_saloutbound_buttons t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu s 
    WHERE BINARY s.menu_name = BINARY t.menu_name
    AND s.parent_id = @saloutbound_menu_id
);

DROP TEMPORARY TABLE IF EXISTS tmp_saloutbound_buttons;

-- ============================================
-- 第七步：提交事务
-- ============================================
COMMIT;

-- ============================================
-- 第八步：验证导入结果
-- ============================================

-- Verification queries
SELECT '========================================' AS '';
SELECT '✅ ERP Menu Created Successfully!' AS message;
SELECT '========================================' AS '';

SELECT '===== Parent Menu =====' AS section;
SELECT menu_id, menu_name, parent_id, path, menu_type, visible, status, icon, remark
FROM sys_menu WHERE menu_id = @erp_parent_id;

SELECT '===== Sub Menus =====' AS section;
SELECT menu_id, menu_name, parent_id, path, menu_type, visible, status, icon
FROM sys_menu WHERE parent_id = @erp_parent_id AND menu_type IN ('C', 'M')
ORDER BY order_num;

SELECT '===== Config Buttons =====' AS section;
SELECT menu_id, menu_name, parent_id, perms, status
FROM sys_menu WHERE menu_type = 'F' AND parent_id = @config_menu_id 
ORDER BY order_num;

SELECT '========================================' AS '';
SELECT '📊 Statistics:' AS summary;
SELECT '========================================' AS '';

SELECT 
  CONCAT('Parent Menu: ', COUNT(CASE WHEN menu_type = 'M' THEN 1 END)) AS parent_count,
  CONCAT('Sub Menus: ', COUNT(CASE WHEN menu_type IN ('C', 'M') AND parent_id = @erp_parent_id THEN 1 END)) AS sub_menu_count,
  CONCAT('Buttons: ', COUNT(CASE WHEN menu_type = 'F' AND parent_id = @config_menu_id THEN 1 END)) AS button_count
FROM sys_menu
WHERE menu_id = @erp_parent_id OR parent_id = @erp_parent_id OR parent_id = @config_menu_id;

SELECT '========================================' AS '';
SELECT '✨ All menus and buttons have been cleaned and recreated!' AS note;
SELECT '========================================' AS '';

-- ============================================
-- 执行完成提示
-- ============================================
SELECT '🎉 Script execution completed successfully!' AS final_message;
SELECT '========================================' AS '';
SELECT 'Next Steps:' AS guide;
SELECT '1. Check the statistics above' AS step1;
SELECT '2. Verify menus in RuoYi admin panel' AS step2;
SELECT '3. Test button permissions with different roles' AS step3;
SELECT '========================================' AS '';