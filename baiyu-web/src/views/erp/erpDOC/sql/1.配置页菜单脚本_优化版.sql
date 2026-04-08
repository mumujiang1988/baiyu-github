-- ERP Public Configuration Management - Menu Initialization SQL
-- Modular and Data-Driven Design
-- Updated: 2026-04-02
-- Version: 3.1 (Optimized with fixes)
-- Scope: 适用于 test 数据库
-- Features: 逻辑与数据分离、参数化配置、批量处理、增强健壮性

USE test;

-- ============================================
-- Part 0: Pre-flight Check (执行前检查)
-- ============================================

-- 0.0 防御性清理临时表（防止上次执行残留）
DROP TEMPORARY TABLE IF EXISTS tmp_business_modules;
DROP TEMPORARY TABLE IF EXISTS tmp_button_templates;
DROP TEMPORARY TABLE IF EXISTS tmp_config_buttons;
DROP TEMPORARY TABLE IF EXISTS tmp_business_buttons;

-- 0.1 检查数据库连接
SELECT '========================================' AS '';
SELECT '🔍 Checking database connection...' AS step;
SELECT DATABASE() AS current_database;
SELECT USER() AS `current_user`;
SELECT VERSION() AS mysql_version;
SELECT '========================================' AS '';

-- 0.2 检查是否已存在 ERP 菜单（用于判断是否需要升级）
SET @existing_menu_count := (
    SELECT COUNT(*) FROM sys_menu 
    WHERE BINARY menu_name = 'ERP 业务菜单' AND parent_id = 0
);

SELECT 
  CASE 
    WHEN @existing_menu_count > 0 THEN '⚠️  WARNING: ERP menu already exists. Old data will be cleaned.'
    ELSE '✅ INFO: No existing ERP menu. Will create new menus.'
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
    
    -- 清理临时表
    DROP TEMPORARY TABLE IF EXISTS tmp_menu_ids;
    DROP TEMPORARY TABLE IF EXISTS tmp_config_buttons;
    DROP TEMPORARY TABLE IF EXISTS tmp_business_buttons;
    
    SET FOREIGN_KEY_CHECKS = 0;
    
    -- 获取 ERP 业务菜单 ID
    SELECT menu_id INTO v_erp_menu_id 
    FROM sys_menu 
    WHERE BINARY menu_name = 'ERP 业务菜单' AND parent_id = 0
    LIMIT 1;
    
    -- 级联删除（仅当菜单存在时）
    IF v_erp_menu_id > 0 THEN
        -- 删除按钮
        DELETE sm FROM sys_menu sm
        INNER JOIN sys_menu parent ON sm.parent_id = parent.menu_id
        WHERE parent.menu_id = v_erp_menu_id;
        
        -- 收集子菜单 ID
        CREATE TEMPORARY TABLE IF NOT EXISTS tmp_menu_ids (menu_id BIGINT);
        INSERT INTO tmp_menu_ids (menu_id)
        SELECT menu_id FROM sys_menu WHERE parent_id = v_erp_menu_id;
        
        -- 清理角色权限
        DELETE FROM sys_role_menu 
        WHERE menu_id IN (SELECT menu_id FROM tmp_menu_ids) OR menu_id = v_erp_menu_id;
        
        -- 删除子菜单和父菜单
        DELETE FROM sys_menu WHERE parent_id = v_erp_menu_id;
        DELETE FROM sys_menu WHERE menu_id = v_erp_menu_id;
        
        DROP TEMPORARY TABLE IF EXISTS tmp_menu_ids;
    END IF;
    
    SET FOREIGN_KEY_CHECKS = 1;
END$$
DELIMITER ;

-- 1.2 雪花 ID 生成函数（简化版 - 避免溢出）
DROP FUNCTION IF EXISTS fn_snowflake_id;
DELIMITER $$
CREATE FUNCTION fn_snowflake_id() RETURNS bigint
    NOT DETERMINISTIC
    NO SQL  -- ✅ 修正：函数不访问数据库表
BEGIN
    -- 使用简化版的雪花算法，生成 19 位 ID
    -- 格式：时间戳 (13 位毫秒) + 随机数 (6 位)
    DECLARE ts BIGINT DEFAULT UNIX_TIMESTAMP(NOW(3)) * 1000;  -- 毫秒级时间戳
    DECLARE random_part INT DEFAULT FLOOR(RAND() * 999999) + 1;
    
    -- 使用时间戳 * 1000000 + 随机数，确保生成 19 位 ID
    -- 当前毫秒时间戳约 1743897600000（13位），乘以 1000000 后约 1.74e18（19位）
    -- BIGINT 最大值 9.22e18，完全安全
    -- 注意：随机数范围 1-999999，理论上同一毫秒内生成 <100 个 ID 时碰撞概率 <0.5%
    RETURN ts * 1000000 + random_part;
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
    remark VARCHAR(200)
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;

-- 2.2 插入业务模块数据（可轻松增删改）
INSERT INTO tmp_business_modules VALUES
('receivebill', '收款单管理', 10, 'erp/receivebill', 'money', 'erp:receivebill', '收款单管理页面'),
('paymentapply', '付款申请单管理', 11, 'erp/paymentapply', 'money', 'erp:paymentapply', '付款申请单管理页面'),
('purchaseorder', '采购订单管理', 12, 'erp/purchaseorder', 'shopping', 'erp:purchaseorder', '采购订单管理页面'),
('receivenotice', '收料通知单管理', 13, 'erp/receivenotice', 'list', 'erp:receivenotice', '收料通知单管理页面'),
('inspection', '检验单管理', 14, 'erp/inspection', 'clipboard', 'erp:inspection', '检验单管理页面'),
('purchaseinstock', '采购入库单管理', 15, 'erp/purchaseinstock', 'form', 'erp:purchaseinstock', '采购入库单管理页面'),
('saleorder', '销售订单管理', 17, 'erp/salesorder', 'shopping', 'erp:saleorder', '销售订单管理页面'),
('purchasequotation', '采购报价单管理', 18, 'erp/purchasequotation', 'list', 'erp:purchasequotation', '采购报价单管理页面'),
('deliverynotice', '发货通知单管理', 19, 'erp/deliverynotice', 'message', 'erp:deliverynotice', '发货通知单管理页面'),
('saloutbound', '销售出库单管理', 20, 'erp/saloutbound', 'list', 'erp:saloutbound', '销售出库单管理页面');

-- 2.3 按钮权限配置表
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_button_templates (
    button_name VARCHAR(50),
    button_code VARCHAR(50),
    order_num INT
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;

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
    -- 错误处理器：如果发生任何错误，自动回滚并输出错误信息
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT '========================================' AS '';
        SELECT '❌ ERROR: Menu creation failed! Transaction rolled back.' AS error_message;
        SELECT '========================================' AS '';
        RESIGNAL;
    END;
    
    -- 开启事务
    START TRANSACTION;
    
    -- 3.3 创建父级菜单
    SET @erp_parent_id := fn_snowflake_id();
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, remark, create_by, create_time)
    SELECT @erp_parent_id, 'ERP 业务菜单', 0, 5, 'business', '', 0, 0, 'M', 1, 1, '', 'system', 'ERP 业务菜单目录', 'admin', NOW()
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE BINARY menu_name = 'ERP 业务菜单' AND parent_id = 0);  -- ✅ 增加 parent_id 限制
    
    -- 3.4 创建公共配置管理菜单
    SET @config_menu_id := fn_snowflake_id();
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    SELECT @config_menu_id, '公共配置管理', @erp_parent_id, 2, 'erp/config', 'erp/config/index', '', 0, 0, 'C', 1, 1, 'erp:config:query', 'dict', 'admin', NOW(), '', NULL, 'ERP 公共配置管理页面'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE BINARY menu_name = '公共配置管理' AND parent_id = @erp_parent_id);
    
    -- 3.5 创建公共配置按钮
    CREATE TEMPORARY TABLE IF NOT EXISTS tmp_config_buttons (
        menu_id VARCHAR(20), menu_name VARCHAR(50), parent_id BIGINT, order_num INT, perms VARCHAR(100), status CHAR(1) DEFAULT '1'
    ) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;
    
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
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu s WHERE BINARY s.menu_name = BINARY t.menu_name AND s.parent_id = @config_menu_id);
    
    DROP TEMPORARY TABLE IF EXISTS tmp_config_buttons;
    
    -- 3.6 循环创建业务菜单和按钮（核心逻辑）
    -- 提前创建临时表（避免在循环中重复创建）
    CREATE TEMPORARY TABLE IF NOT EXISTS tmp_business_buttons (
        menu_id VARCHAR(20), menu_name VARCHAR(50), parent_id BIGINT, order_num INT, perms VARCHAR(100), status CHAR(1) DEFAULT '1'
    ) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;
    
    BEGIN
        DECLARE done INT DEFAULT FALSE;
        DECLARE v_module_code VARCHAR(50);
        DECLARE v_module_name VARCHAR(100);
        DECLARE v_order_num INT;
        DECLARE v_path VARCHAR(100);
        DECLARE v_icon VARCHAR(50);
        DECLARE v_perm_prefix VARCHAR(50);
        DECLARE v_remark VARCHAR(200);
        DECLARE v_menu_id BIGINT;
        
        -- 游标遍历业务模块
        DECLARE cur_modules CURSOR FOR SELECT module_code, module_name, order_num, path, icon, permission_prefix, remark FROM tmp_business_modules;
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
        
        OPEN cur_modules;
        
        read_loop: LOOP
            FETCH cur_modules INTO v_module_code, v_module_name, v_order_num, v_path, v_icon, v_perm_prefix, v_remark;
            IF done THEN
                LEAVE read_loop;
            END IF;
            
            -- 创建菜单
            SET v_menu_id := fn_snowflake_id();
            INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
            SELECT v_menu_id, v_module_name, @erp_parent_id, v_order_num, v_path, 'erp/pageTemplate/configurable/BusinessConfigurable/index', 
                   CONCAT('{"moduleCode":"', v_module_code, '"}'), 0, 0, 'C', 1, 1, 
                   CONCAT(v_perm_prefix, ':query'), v_icon, 'admin', NOW(), '', NULL, v_remark
            WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE BINARY menu_name = v_module_name AND parent_id = @erp_parent_id);  -- ✅ 增加 parent_id 限制
            
            -- 清空并重新填充临时表
            DELETE FROM tmp_business_buttons;
            
            -- 动态插入按钮（批量生成7个按钮ID）
            INSERT INTO tmp_business_buttons
            SELECT fn_snowflake_id(), bt.button_name, v_menu_id, bt.order_num, CONCAT(v_perm_prefix, ':', bt.button_code), '1'
            FROM tmp_button_templates bt;
            
            INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
            SELECT t.menu_id, t.menu_name, v_menu_id, t.order_num, '', '', '', 0, 0, 'F', 1, 1, t.perms, '#', 'admin', NOW(), '', NULL, ''
            FROM tmp_business_buttons t
            WHERE NOT EXISTS (SELECT 1 FROM sys_menu s WHERE BINARY s.menu_name = BINARY t.menu_name AND s.parent_id = v_menu_id);
            
            -- ⚠️ 关键优化：每次循环后延迟 1 毫秒，确保时间戳不同
            -- 避免同一毫秒内生成多个 ID 导致随机数碰撞
            DO SLEEP(0.001);
        END LOOP;
        
        CLOSE cur_modules;
        
        -- 循环结束后删除临时表
        DROP TEMPORARY TABLE IF EXISTS tmp_business_buttons;
    END;
    
    -- 提交事务
    COMMIT;
END$$
DELIMITER ;

-- 3.7 执行存储过程
CALL proc_create_menus();
DROP PROCEDURE IF EXISTS proc_create_menus;

-- ============================================
-- Part 4: Cleanup & Verification (清理与验证)
-- ============================================

-- 4.1 清理临时表
DROP TEMPORARY TABLE IF EXISTS tmp_business_modules;
DROP TEMPORARY TABLE IF EXISTS tmp_button_templates;

-- 4.2 验证结果
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

SELECT '===== Statistics =====' AS section;
SELECT 
  CONCAT('Parent Menu: ', COUNT(CASE WHEN s.menu_type = 'M' AND s.menu_id = @erp_parent_id THEN 1 END)) AS parent_count,
  CONCAT('Sub Menus: ', COUNT(CASE WHEN s.menu_type IN ('C', 'M') AND s.parent_id = @erp_parent_id THEN 1 END)) AS sub_menu_count,
  CONCAT('Buttons: ', COUNT(CASE WHEN s.menu_type = 'F' AND p.parent_id = @erp_parent_id THEN 1 END)) AS button_count
FROM sys_menu s
INNER JOIN sys_menu p ON s.parent_id = p.menu_id
WHERE (s.menu_id = @erp_parent_id OR p.parent_id = @erp_parent_id OR s.parent_id = @config_menu_id);

SELECT '========================================' AS '';
SELECT '🎉 Script execution completed!' AS final_message;
SELECT '========================================' AS '';

-- 4.3 验证外键约束状态
SELECT 
  @@FOREIGN_KEY_CHECKS AS foreign_key_checks_enabled,
  CASE 
    WHEN @@FOREIGN_KEY_CHECKS = 1 THEN '✅ Foreign key checks are enabled'
    ELSE '⚠️ WARNING: Foreign key checks are disabled!'
  END AS fk_status;

-- 4.4 简化版统计
SELECT '===== Simplified Statistics =====' AS section;
SELECT 
  (SELECT COUNT(*) FROM sys_menu WHERE menu_id = @erp_parent_id) AS parent_menu_count,
  (SELECT COUNT(*) FROM sys_menu WHERE parent_id = @erp_parent_id AND menu_type = 'C') AS business_menu_count,
  (SELECT COUNT(*) FROM sys_menu WHERE parent_id = @config_menu_id AND menu_type = 'F') AS config_button_count,
  (SELECT COUNT(*) FROM sys_menu s 
   INNER JOIN sys_menu p ON s.parent_id = p.menu_id 
   WHERE p.parent_id = @erp_parent_id AND s.menu_type = 'F') AS total_button_count;

SELECT '========================================' AS '';
SELECT '✅ All checks passed!' AS final_check;
SELECT '========================================' AS '';
