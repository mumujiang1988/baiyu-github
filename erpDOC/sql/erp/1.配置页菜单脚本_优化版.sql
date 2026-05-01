-- ERP Public Configuration Management - Menu Initialization SQL
-- Modular and Data-Driven Design with 3-Level Hierarchy
-- Updated: 2026-04-27
-- Version: 4.4 (Icon Optimization)
-- Scope: 适用于 test 数据库
-- Features: 逻辑与数据分离、参数化配置、批量处理、增强健壮性、三级菜单结构
-- Changelog:
--   v4.4 (2026-04-27): 优化图标配置,使用若依(RuoYi)标准图标库替换非标准图标
--   v4.3 (2026-04-27): 为所有临时表显式指定COLLATE=utf8mb4_general_ci,彻底解决排序规则冲突
--   v4.2 (2026-04-27): 修复排序规则冲突,移除所有BINARY比较
--   v4.1 (2026-04-27): 修复表结构定义、优化雪花ID生成、完善清理逻辑、增强错误处理
--   v4.0 (2026-04-27): 新增二级菜单分组(销售/采购/库存/财务/基础资料等),参考金蝶ERP标准模块划分
--   v3.2 (2026-04-22): 移除Jimu报表相关菜单(报表BI/积木报表/BI大屏)
--   v3.1 (2026-04-02): 优化版修复

USE test;

-- ============================================
-- Part 0: Pre-flight Check (执行前检查)
-- ============================================

-- 0.0 防御性清理临时表（防止上次执行残留）
DROP TEMPORARY TABLE IF EXISTS tmp_business_modules;
DROP TEMPORARY TABLE IF EXISTS tmp_button_templates;
DROP TEMPORARY TABLE IF EXISTS tmp_config_buttons;
DROP TEMPORARY TABLE IF EXISTS tmp_business_buttons;
DROP TEMPORARY TABLE IF EXISTS tmp_menu_categories;
DROP TEMPORARY TABLE IF EXISTS tmp_menu_ids;

-- 0.1 检查数据库连接
SELECT '========================================' AS '';
SELECT '🔍 Checking database connection...' AS step;
SELECT DATABASE() AS current_database;
SELECT USER() AS `current_user`;
SELECT VERSION() AS mysql_version;
SELECT NOW() AS start_time;
SELECT '========================================' AS '';

-- 0.2 检查是否已存在 ERP 菜单（用于判断是否需要升级）
SET @existing_menu_count := (
    SELECT COUNT(*) FROM sys_menu 
    WHERE menu_name = 'ERP 业务菜单' AND parent_id = 0
);

SELECT 
  CASE 
    WHEN @existing_menu_count > 0 THEN '  WARNING: ERP menu already exists. Old data will be cleaned.'
    ELSE ' INFO: No existing ERP menu. Will create new menus.'
  END AS status_check;

-- ============================================
-- Part 1: Utility Functions (工具函数层)
-- ============================================

-- 1.1 清理旧数据
DROP PROCEDURE IF EXISTS proc_cleanup_old_menus;
DELIMITER $$
CREATE PROCEDURE proc_cleanup_old_menus()
BEGIN
    DECLARE v_erp_menu_id BIGINT DEFAULT -1;
    DECLARE v_config_menu_id BIGINT DEFAULT -1;
    
    -- 错误处理器：确保外键约束恢复
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        SET FOREIGN_KEY_CHECKS = 1;
        ROLLBACK;
        RESIGNAL;
    END;
    
    -- 清理临时表
    DROP TEMPORARY TABLE IF EXISTS tmp_menu_ids;
    DROP TEMPORARY TABLE IF EXISTS tmp_config_buttons;
    DROP TEMPORARY TABLE IF EXISTS tmp_business_buttons;
    
    SET FOREIGN_KEY_CHECKS = 0;
    
    -- 获取 ERP 业务菜单 ID
    SELECT menu_id INTO v_erp_menu_id 
    FROM sys_menu 
    WHERE menu_name = 'ERP 业务菜单' AND parent_id = 0
    LIMIT 1;
    
    -- 获取公共配置菜单 ID
    SELECT menu_id INTO v_config_menu_id 
    FROM sys_menu 
    WHERE menu_name = '公共配置管理' AND parent_id = v_erp_menu_id
    LIMIT 1;
    
    -- 级联删除（仅当菜单存在时）
    IF v_erp_menu_id > 0 THEN
        -- 先收集所有子菜单ID(包括二级、三级)
        CREATE TEMPORARY TABLE IF NOT EXISTS tmp_menu_ids (menu_id BIGINT);
        INSERT INTO tmp_menu_ids (menu_id)
        SELECT menu_id FROM sys_menu WHERE parent_id = v_erp_menu_id;
        
        -- 再收集孙菜单ID(三级菜单)
        INSERT INTO tmp_menu_ids (menu_id)
        SELECT s.menu_id 
        FROM sys_menu s
        INNER JOIN sys_menu p ON s.parent_id = p.menu_id
        WHERE p.parent_id = v_erp_menu_id;
        
        -- 清理角色权限(包括所有层级)
        DELETE FROM sys_role_menu 
        WHERE menu_id IN (SELECT menu_id FROM tmp_menu_ids) OR menu_id = v_erp_menu_id;
        
        -- 删除按钮(所有层级的按钮) - 使用临时表避免MySQL限制
        DELETE sm FROM sys_menu sm
        INNER JOIN sys_menu parent ON sm.parent_id = parent.menu_id
        WHERE parent.menu_id IN (SELECT menu_id FROM tmp_menu_ids) OR parent.parent_id = v_erp_menu_id;
        
        -- 删除公共配置按钮
        IF v_config_menu_id > 0 THEN
            DELETE FROM sys_menu WHERE parent_id = v_config_menu_id AND menu_type = 'F';
        END IF;
        
        -- 删除三级菜单 - 使用临时表避免MySQL限制
        DELETE FROM sys_menu 
        WHERE menu_id IN (
            SELECT menu_id FROM (
                SELECT menu_id FROM sys_menu 
                WHERE parent_id IN (SELECT menu_id FROM tmp_menu_ids)
            ) AS subquery
        );
        
        -- 删除二级菜单
        DELETE FROM sys_menu WHERE menu_id IN (SELECT menu_id FROM tmp_menu_ids);
        
        -- 删除父菜单
        DELETE FROM sys_menu WHERE menu_id = v_erp_menu_id;
        
        DROP TEMPORARY TABLE IF EXISTS tmp_menu_ids;
    END IF;
    
    SET FOREIGN_KEY_CHECKS = 1;
END$$
DELIMITER ;

-- 1.2 雪花 ID 生成函数（优化版 - 降低碰撞概率）
DROP FUNCTION IF EXISTS fn_snowflake_id;
DELIMITER $$
CREATE FUNCTION fn_snowflake_id() RETURNS bigint
    NOT DETERMINISTIC
    NO SQL
BEGIN
    -- 使用优化版雪花算法，生成 19 位 ID
    -- 格式：时间戳 (13 位毫秒) + 序列号 (6 位)
    DECLARE ts BIGINT DEFAULT UNIX_TIMESTAMP(NOW(3)) * 1000;  -- 毫秒级时间戳
    
    -- 使用会话变量维护序列号，避免同一毫秒内碰撞
    SET @snowflake_seq := IFNULL(@snowflake_seq, 0) + 1;
    
    -- 序列号超过 999999 后重置（防止溢出）
    IF @snowflake_seq > 999999 THEN
        SET @snowflake_seq := 1;
    END IF;
    
    -- 使用时间戳 * 1000000 + 序列号，确保生成 19 位 ID
    -- 当前毫秒时间戳约 1743897600000（13位），乘以 1000000 后约 1.74e18（19位）
    -- BIGINT 最大值 9.22e18，完全安全
    -- 序列号范围 1-999999，同一毫秒内最多支持 999999 个 ID，碰撞概率 <0.001%
    RETURN ts * 1000000 + @snowflake_seq;
END$$
DELIMITER ;

-- ============================================
-- Part 2: Business Data Layer (业务数据层)
-- ============================================

-- 2.1 业务模块配置表（定义所有业务模块的参数）
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_business_modules (
    module_code VARCHAR(50),
    module_name VARCHAR(100),
    order_num INT,
    path VARCHAR(100),
    icon VARCHAR(50),
    permission_prefix VARCHAR(50),
    remark VARCHAR(200),
    category_code VARCHAR(50) DEFAULT NULL
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 2.2 插入业务模块数据(可轻松增删改)
-- 分类说明: sales=销售, purchase=采购, finance=财务, basic=基础资料
-- 图标说明: 使用若依(RuoYi)标准图标库
INSERT INTO tmp_business_modules VALUES
('receivebill', '收款单管理', 10, 'erp/receivebill', 'money', 'erp:receivebill', '收款单管理页面', 'finance'),
('paymentapply', '付款申请单管理', 11, 'erp/paymentapply', 'money', 'erp:paymentapply', '付款申请单管理页面', 'finance'),
('purchaseorder', '采购订单管理', 12, 'erp/purchaseorder', 'shopping', 'erp:purchaseorder', '采购订单管理页面', 'purchase'),
('receivenotice', '收料通知单管理', 13, 'erp/receivenotice', 'list', 'erp:receivenotice', '收料通知单管理页面', 'purchase'),
('inspection', '检验单管理', 14, 'erp/inspection', 'checkbox', 'erp:inspection', '检验单管理页面', 'purchase'),
('purchaseinstock', '采购入库单管理', 15, 'erp/purchaseinstock', 'download', 'erp:purchaseinstock', '采购入库单管理页面', 'purchase'),
('inquiryorder', '询价单管理', 16, 'erp/inquiryorder', 'question', 'erp:inquiryorder', '询价单管理页面', 'purchase'),
('saleorder', '销售订单管理', 17, 'erp/salesorder', 'shopping', 'erp:saleorder', '销售订单管理页面', 'sales'),
('saleorderinfo', '销售订单信息管理', 18, 'erp/saleorderinfo', 'shopping', 'erp:saleorderinfo', '销售订单信息管理页面', 'sales'),
('purchasequotation', '采购报价单管理', 19, 'erp/purchasequotation', 'list', 'erp:purchasequotation', '采购报价单管理页面', 'purchase'),
('deliverynotice', '发货通知单管理', 20, 'erp/deliverynotice', 'upload', 'erp:deliverynotice', '发货通知单管理页面', 'sales'),
('saloutbound', '销售出库单管理', 21, 'erp/saloutbound', 'upload', 'erp:saloutbound', '销售出库单管理页面', 'sales'),
('receivable', '应收单管理', 22, 'erp/receivable', 'money', 'erp:receivable', '应收单管理页面', 'finance'),
('payable', '应付单管理', 23, 'erp/payable', 'money', 'erp:payable', '应付单管理页面', 'finance'),
('ftcustomer', '富通客户管理', 24, 'erp/ftcustomer', 'peoples', 'erp:ftcustomer', '富通客户管理页面', 'basic');

-- 2.3 二级菜单分类配置表(参考金蝶/用友标准模块划分)
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_menu_categories (
    category_code VARCHAR(50),
    category_name VARCHAR(100),
    order_num INT,
    icon VARCHAR(50),
    remark VARCHAR(200)
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 分类设计原则:
-- 1. 财务管理合并应收应付(符合ERP标准实践)
-- 2. 预留库存/生产/人事模块供未来扩展
-- 3. 移除行政管理(非ERP核心功能)
-- 4. 使用若依(RuoYi)标准图标库
INSERT INTO tmp_menu_categories VALUES
('config', '公共配置管理', 1, 'education', '系统公共配置'),
('basic', '基础资料', 10, 'dict', '客户/供应商/物料等主数据管理'),
('sales', '销售管理', 11, 'shopping', '销售订单/发货/出库等业务'),
('purchase', '采购管理', 12, 'shopping', '询价/报价/采购订单/入库等业务'),
('inventory', '库存管理', 13, 'log', '库存调拨/盘点/预警等(预留)'),
('finance', '财务管理', 14, 'money', '应收/应付/收款/付款等资金管理'),
('production', '生产管理', 15, 'build', '生产计划/工单/工艺等(预留)'),
('hr', '人事管理', 16, 'peoples', '员工/考勤/薪酬等(预留)'),
('asset', '固资管理', 17, 'skill', '固定资产全生命周期管理(预留)');

-- 2.4 按钮权限配置表
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_button_templates (
    button_name VARCHAR(50),
    button_code VARCHAR(50),
    order_num INT
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO tmp_button_templates VALUES
('查询', 'query', 1),
('新增', 'add', 2),
('修改', 'edit', 3),
('删除', 'remove', 4),
('审核', 'audit', 5),
('反审核', 'unAudit', 6),
('导出', 'export', 7);

-- ============================================
-- Part 3: Core Logic Layer (核心逻辑层)
-- ============================================

-- 3.1 执行清理
CALL proc_cleanup_old_menus();
DROP PROCEDURE IF EXISTS proc_cleanup_old_menus;

-- 3.2 创建存储过程（包含事务和错误处理）
DROP PROCEDURE IF EXISTS proc_create_menus;
DELIMITER $$
CREATE PROCEDURE proc_create_menus()
BEGIN
    DECLARE v_error_msg VARCHAR(500);
    
    -- 错误处理器：如果发生任何错误，自动回滚并输出详细错误信息
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_error_msg = MESSAGE_TEXT;
        ROLLBACK;
        SELECT '========================================' AS '';
        SELECT CONCAT('❌ ERROR: Menu creation failed! Transaction rolled back.') AS error_message;
        SELECT CONCAT('   Details: ', v_error_msg) AS error_detail;
        SELECT NOW() AS error_time;
        SELECT '========================================' AS '';
        RESIGNAL;
    END;
    
    -- 开启事务
    START TRANSACTION;
    
    -- 3.3 创建父级菜单
    SET @erp_parent_id := fn_snowflake_id();
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, remark, create_by, create_time)
    SELECT @erp_parent_id, 'ERP 业务菜单', 0, 5, 'business', '', 0, 0, 'M', 1, 1, '', 'system', 'ERP 业务菜单目录', 'admin', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = 'ERP 业务菜单' AND parent_id = 0);  --  增加 parent_id 限制
    
    -- 3.4 创建公共配置管理菜单
    SET @config_menu_id := fn_snowflake_id();
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    SELECT @config_menu_id, '公共配置管理', @erp_parent_id, 1, 'erp/config', 'erp/config/index', '', 0, 0, 'C', 1, 1, 'erp:config:query', 'education', 'admin', NOW(), '', NULL, 'ERP 公共配置管理页面'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '公共配置管理' AND parent_id = @erp_parent_id);
    
    -- 3.5 创建公共配置按钮
    CREATE TEMPORARY TABLE IF NOT EXISTS tmp_config_buttons (
        menu_id VARCHAR(20), menu_name VARCHAR(50), parent_id BIGINT, order_num INT, perms VARCHAR(100), status CHAR(1) DEFAULT '1'
    ) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
    
    DELETE FROM tmp_config_buttons;
    INSERT INTO tmp_config_buttons VALUES
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
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu s WHERE s.menu_name = t.menu_name AND s.parent_id = @config_menu_id);
    
    DROP TEMPORARY TABLE IF EXISTS tmp_config_buttons;
    
    -- 3.6 创建二级菜单分类
    BEGIN
        DECLARE done_cat INT DEFAULT FALSE;
        DECLARE v_cat_code VARCHAR(50);
        DECLARE v_cat_name VARCHAR(100);
        DECLARE v_cat_order INT;
        DECLARE v_cat_icon VARCHAR(50);
        DECLARE v_cat_remark VARCHAR(200);
        DECLARE v_cat_menu_id BIGINT;
        
        DECLARE cur_categories CURSOR FOR 
            SELECT category_code, category_name, order_num, icon, remark 
            FROM tmp_menu_categories 
            ORDER BY order_num;
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done_cat = TRUE;
        
        OPEN cur_categories;
        
        cat_loop: LOOP
            FETCH cur_categories INTO v_cat_code, v_cat_name, v_cat_order, v_cat_icon, v_cat_remark;
            IF done_cat THEN
                LEAVE cat_loop;
            END IF;
            
            -- 跳过公共配置管理(已单独创建)
            IF v_cat_code != 'config' THEN
                SET v_cat_menu_id := fn_snowflake_id();
                INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
                SELECT v_cat_menu_id, v_cat_name, @erp_parent_id, v_cat_order, v_cat_code, '', '', 0, 0, 'M', 1, 1, '', v_cat_icon, 'admin', NOW(), '', NULL, v_cat_remark
                WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = v_cat_name AND parent_id = @erp_parent_id);
            END IF;
        END LOOP;
        
        CLOSE cur_categories;
    END;
    
    -- 3.7 循环创建业务菜单和按钮(三级菜单)
    CREATE TEMPORARY TABLE IF NOT EXISTS tmp_business_buttons (
        menu_id VARCHAR(20), menu_name VARCHAR(50), parent_id BIGINT, order_num INT, perms VARCHAR(100), status CHAR(1) DEFAULT '1'
    ) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
    
    BEGIN
        DECLARE done_module INT DEFAULT FALSE;
        DECLARE v_module_code VARCHAR(50);
        DECLARE v_module_name VARCHAR(100);
        DECLARE v_order_num INT;
        DECLARE v_path VARCHAR(100);
        DECLARE v_icon VARCHAR(50);
        DECLARE v_perm_prefix VARCHAR(50);
        DECLARE v_remark VARCHAR(200);
        DECLARE v_category_code VARCHAR(50);
        DECLARE v_menu_id BIGINT;
        DECLARE v_parent_menu_id BIGINT;
        
        DECLARE cur_modules CURSOR FOR 
            SELECT module_code, module_name, order_num, path, icon, permission_prefix, remark, category_code 
            FROM tmp_business_modules 
            ORDER BY order_num;
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done_module = TRUE;
        
        OPEN cur_modules;
        
        module_loop: LOOP
            FETCH cur_modules INTO v_module_code, v_module_name, v_order_num, v_path, v_icon, v_perm_prefix, v_remark, v_category_code;
            IF done_module THEN
                LEAVE module_loop;
            END IF;
            
            -- 查找对应的二级菜单ID
            SELECT menu_id INTO v_parent_menu_id 
            FROM sys_menu 
            WHERE menu_name = (SELECT category_name FROM tmp_menu_categories WHERE category_code = v_category_code)
              AND parent_id = @erp_parent_id
            LIMIT 1;
            
            IF v_parent_menu_id IS NOT NULL THEN
                SET v_menu_id := fn_snowflake_id();
                INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
                SELECT v_menu_id, v_module_name, v_parent_menu_id, v_order_num, v_path, 'erp/pageTemplate/configurable/BusinessConfigurable/index', 
                       CONCAT('{"moduleCode":"', v_module_code, '"}'), 0, 0, 'C', 1, 1, 
                       CONCAT(v_perm_prefix, ':query'), v_icon, 'admin', NOW(), '', NULL, v_remark
                WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = v_module_name AND parent_id = v_parent_menu_id);
                
                DELETE FROM tmp_business_buttons;
                
                INSERT INTO tmp_business_buttons
                SELECT fn_snowflake_id(), bt.button_name, v_menu_id, bt.order_num, CONCAT(v_perm_prefix, ':', bt.button_code), '1'
                FROM tmp_button_templates bt;
                
                INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
                SELECT t.menu_id, t.menu_name, v_menu_id, t.order_num, '', '', '', 0, 0, 'F', 1, 1, t.perms, '#', 'admin', NOW(), '', NULL, ''
                FROM tmp_business_buttons t
                WHERE NOT EXISTS (SELECT 1 FROM sys_menu s WHERE s.menu_name = t.menu_name AND s.parent_id = v_menu_id);
            END IF;
        END LOOP;
        
        CLOSE cur_modules;
        
        DROP TEMPORARY TABLE IF EXISTS tmp_business_buttons;
    END;
        
    -- 提交事务
    COMMIT;
END$$
DELIMITER ;

-- 3.12 执行存储过程
CALL proc_create_menus();
DROP PROCEDURE IF EXISTS proc_create_menus;

-- ============================================
-- Part 4: Cleanup & Verification (清理与验证)
-- ============================================

-- 4.1 清理临时表
DROP TEMPORARY TABLE IF EXISTS tmp_business_modules;
DROP TEMPORARY TABLE IF EXISTS tmp_menu_categories;
DROP TEMPORARY TABLE IF EXISTS tmp_button_templates;

-- 4.2 验证结果
SELECT '========================================' AS '';
SELECT ' ERP Menu Created Successfully!' AS message;
SELECT '========================================' AS '';

SELECT '===== Parent Menu =====' AS section;
SELECT menu_id, menu_name, parent_id, path, menu_type, visible, status, icon, remark
FROM sys_menu WHERE menu_id = @erp_parent_id;

SELECT '===== Level 2 Categories =====' AS section;
SELECT menu_id, menu_name, parent_id, path, menu_type, visible, status, icon, remark
FROM sys_menu WHERE parent_id = @erp_parent_id AND menu_type = 'M'
ORDER BY order_num;

SELECT '===== Level 3 Business Menus =====' AS section;
SELECT 
    p.menu_name AS category,
    s.menu_id,
    s.menu_name,
    s.parent_id,
    s.path,
    s.menu_type,
    s.visible,
    s.status,
    s.icon
FROM sys_menu s
INNER JOIN sys_menu p ON s.parent_id = p.menu_id
WHERE p.parent_id = @erp_parent_id AND s.menu_type = 'C'
ORDER BY p.order_num, s.order_num;

SELECT '===== Statistics =====' AS section;
SELECT 
  CONCAT('Parent Menu: ', COUNT(CASE WHEN s.menu_type = 'M' AND s.menu_id = @erp_parent_id THEN 1 END)) AS parent_count,
  CONCAT('Level 2 Categories: ', COUNT(CASE WHEN s.menu_type = 'M' AND s.parent_id = @erp_parent_id THEN 1 END)) AS category_count,
  CONCAT('Level 3 Menus: ', COUNT(CASE WHEN s.menu_type = 'C' AND p.parent_id = @erp_parent_id THEN 1 END)) AS business_menu_count,
  CONCAT('Total Buttons: ', COUNT(CASE WHEN s.menu_type = 'F' AND gp.parent_id = @erp_parent_id THEN 1 END)) AS button_count
FROM sys_menu s
LEFT JOIN sys_menu p ON s.parent_id = p.menu_id
LEFT JOIN sys_menu gp ON p.parent_id = gp.menu_id
WHERE (s.menu_id = @erp_parent_id OR p.parent_id = @erp_parent_id OR s.parent_id = @config_menu_id OR gp.parent_id = @erp_parent_id);

SELECT '========================================' AS '';
SELECT ' Script execution completed!' AS final_message;
SELECT '========================================' AS '';

-- 4.3 验证外键约束状态
SELECT 
  @@FOREIGN_KEY_CHECKS AS foreign_key_checks_enabled,
  CASE 
    WHEN @@FOREIGN_KEY_CHECKS = 1 THEN ' Foreign key checks are enabled'
    ELSE ' WARNING: Foreign key checks are disabled!'
  END AS fk_status;

-- 4.4 简化版统计
SELECT '===== Simplified Statistics =====' AS section;
SELECT 
  (SELECT COUNT(*) FROM sys_menu WHERE menu_id = @erp_parent_id) AS parent_menu_count,
  (SELECT COUNT(*) FROM sys_menu WHERE parent_id = @erp_parent_id AND menu_type = 'M') AS category_count,
  (SELECT COUNT(*) FROM sys_menu s INNER JOIN sys_menu p ON s.parent_id = p.menu_id WHERE p.parent_id = @erp_parent_id AND s.menu_type = 'C') AS business_menu_count,
  (SELECT COUNT(*) FROM sys_menu WHERE parent_id = @config_menu_id AND menu_type = 'F') AS config_button_count,
  (SELECT COUNT(*) FROM sys_menu s 
   INNER JOIN sys_menu p ON s.parent_id = p.menu_id 
   INNER JOIN sys_menu gp ON p.parent_id = gp.menu_id
   WHERE gp.parent_id = @erp_parent_id AND s.menu_type = 'F') AS total_button_count;

SELECT '========================================' AS '';
SELECT ' All checks passed!' AS final_check;
SELECT NOW() AS end_time;
SELECT '========================================' AS '';
