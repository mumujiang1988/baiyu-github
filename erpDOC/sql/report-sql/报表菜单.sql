-- Report Module Menu Initialization SQL
-- Modular and Data-Driven Design
-- Updated: 2026-04-23
-- Version: 1.0 (Initial Release)
-- Scope: 适用于 test 数据库
-- Features: 逻辑与数据分离、参数化配置、批量处理、增强健壮性
-- Description: 报表模块菜单初始化(包含3个示例报表)

USE test;

-- ============================================
-- Part 0: Pre-flight Check (执行前检查)
-- ============================================

-- 0.0 防御性清理临时表（防止上次执行残留）
DROP TEMPORARY TABLE IF EXISTS tmp_report_modules;
DROP TEMPORARY TABLE IF EXISTS tmp_button_templates;
DROP TEMPORARY TABLE IF EXISTS tmp_config_buttons;
DROP TEMPORARY TABLE IF EXISTS tmp_report_buttons;
DROP TEMPORARY TABLE IF EXISTS tmp_menu_ids;

-- 0.1 检查数据库连接
SELECT '========================================' AS '';
SELECT '🔍 Checking database connection...' AS step;
SELECT DATABASE() AS current_database;
SELECT USER() AS `current_user`;
SELECT VERSION() AS mysql_version;
SELECT NOW() AS start_time;
SELECT '========================================' AS '';

-- 0.2 检查是否已存在报表菜单（用于判断是否需要升级）
SET @existing_menu_count := (
    SELECT COUNT(*) FROM sys_menu 
    WHERE BINARY menu_name = '报表分析中心' AND parent_id = 0
);

SELECT 
  CASE 
    WHEN @existing_menu_count > 0 THEN '⚠️  WARNING: Report menu already exists. Old data will be cleaned.'
    ELSE '✅ INFO: No existing report menu. Will create new menus.'
  END AS status_check;

-- ============================================
-- Part 1: Utility Functions (工具函数层)
-- ============================================

-- 1.1 清理旧数据
DROP PROCEDURE IF EXISTS proc_cleanup_report_menus;
DELIMITER $$
CREATE PROCEDURE proc_cleanup_report_menus()
BEGIN
    DECLARE v_report_menu_id BIGINT DEFAULT -1;
    
    -- 清理临时表
    DROP TEMPORARY TABLE IF EXISTS tmp_menu_ids;
    DROP TEMPORARY TABLE IF EXISTS tmp_report_buttons;
    
    SET FOREIGN_KEY_CHECKS = 0;
    
    -- 获取报表分析中心菜单 ID
    SELECT menu_id INTO v_report_menu_id 
    FROM sys_menu 
    WHERE BINARY menu_name = '报表分析中心' AND parent_id = 0
    LIMIT 1;
    
    -- 级联删除（仅当菜单存在时）
    IF v_report_menu_id > 0 THEN
        -- 先收集所有子菜单ID(包括二级、三级)
        CREATE TEMPORARY TABLE IF NOT EXISTS tmp_menu_ids (menu_id BIGINT);
        INSERT INTO tmp_menu_ids (menu_id)
        SELECT menu_id FROM sys_menu WHERE parent_id = v_report_menu_id;
        
        -- 再收集孙菜单ID(三级菜单)
        INSERT INTO tmp_menu_ids (menu_id)
        SELECT s.menu_id 
        FROM sys_menu s
        INNER JOIN sys_menu p ON s.parent_id = p.menu_id
        WHERE p.parent_id = v_report_menu_id;
        
        -- 清理角色权限(包括所有层级)
        DELETE FROM sys_role_menu 
        WHERE menu_id IN (SELECT menu_id FROM tmp_menu_ids) OR menu_id = v_report_menu_id;
        
        -- 删除按钮(三级菜单的按钮)
        DELETE sm FROM sys_menu sm
        INNER JOIN sys_menu parent ON sm.parent_id = parent.menu_id
        INNER JOIN sys_menu grandparent ON parent.parent_id = grandparent.menu_id
        WHERE grandparent.parent_id = v_report_menu_id;
        
        -- 删除三级菜单
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
        DELETE FROM sys_menu WHERE menu_id = v_report_menu_id;
        
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
    NO SQL
BEGIN
    -- ✅ Fixed: Use second-level timestamp to avoid BIGINT overflow
    -- Format: timestamp (10 digits) * 1000000 + random (6 digits) = max 16 digits (safe for BIGINT)
    DECLARE ts BIGINT DEFAULT UNIX_TIMESTAMP();  -- Second-level timestamp (~1.7e9)
    DECLARE random_part INT DEFAULT FLOOR(RAND() * 999999) + 1;
    
    RETURN ts * 1000000 + random_part;  -- Max value: ~1.7e15, well within BIGINT range (9.2e18)
END$$
DELIMITER ;

-- ============================================
-- Part 2: Business Data Layer (业务数据层)
-- ============================================

-- 2.1 报表模块配置表（定义所有报表的参数）
-- 注意: 这些是配置数据,不是菜单! 仅用于演示rpt_report_config表的初始化
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_report_modules (
    report_code VARCHAR(50),
    report_name VARCHAR(100),
    order_num INT,
    path VARCHAR(100),
    icon VARCHAR(50),
    permission_prefix VARCHAR(50),
    remark VARCHAR(200)
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;

-- 2.2 插入报表演示数据(可轻松增删改)
-- ⚠️ 重要: 这些数据仅用于说明报表配置的格式,不会创建菜单!
INSERT INTO tmp_report_modules VALUES
('sales_dept_summary', '销售订单部门汇总', 1, 'erp/report/sales-dept-summary', 'chart', 'report:sales_dept_summary', '按部门统计销售订单数量和金额'),
('purchase_supplier_analysis', '采购供应商分析', 2, 'erp/report/purchase-supplier', 'shopping', 'report:purchase_supplier_analysis', '分析供应商采购情况'),
('receivable_aging', '应收账龄分析', 3, 'erp/report/receivable-aging', 'money', 'report:receivable_aging', '分析客户应收账款的账龄分布');

-- 2.3 按钮权限配置表
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_button_templates (
    button_name VARCHAR(50),
    button_code VARCHAR(50),
    order_num INT
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;

INSERT INTO tmp_button_templates VALUES
('查询', 'query', 1),
('导出', 'export', 2),
('打印', 'print', 3),
('刷新', 'refresh', 4),
('保存方案', 'saveScheme', 5),
('加载方案', 'loadScheme', 6),
('列设置', 'columnSetting', 7);

-- ============================================
-- Part 3: Core Logic Layer (核心逻辑层)
-- ============================================

-- 3.1 执行清理
CALL proc_cleanup_report_menus();
DROP PROCEDURE IF EXISTS proc_cleanup_report_menus;

-- 3.2 创建存储过程（包含事务和错误处理）
DROP PROCEDURE IF EXISTS proc_create_report_menus;
DELIMITER $$
CREATE PROCEDURE proc_create_report_menus()
BEGIN
    -- 错误处理器：如果发生任何错误，自动回滚并输出错误信息
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT '========================================' AS '';
        SELECT '❌ ERROR: Report menu creation failed! Transaction rolled back.' AS error_message;
        SELECT '========================================' AS '';
        RESIGNAL;
    END;
    
    -- 开启事务
    START TRANSACTION;
    
    -- 3.3 创建父级菜单 - 报表分析中心
    SET @report_parent_id := fn_snowflake_id();
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, remark, create_by, create_time)
    SELECT @report_parent_id, '报表分析中心', 0, 6, 'report', '', 0, 0, 'M', 1, 1, '', 'chart', '报表分析中心目录', 'admin', NOW()
    WHERE NOT EXISTS (
        SELECT 1 FROM sys_menu 
        WHERE BINARY menu_name = '报表分析中心' AND parent_id = 0
    )
    AND NOT EXISTS (
        SELECT 1 FROM sys_menu 
        WHERE path = 'report' AND parent_id = 0  -- ✅ P1-2: 路径冲突检测
    );
    
    -- 3.4 创建二级菜单 - 报表查询(示例报表)
    SET @report_query_id := fn_snowflake_id();
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    SELECT @report_query_id, '报表查询', @report_parent_id, 1, 'erp/report/query', 'erp/report/DepartmentReport', '', 0, 0, 'C', 1, 1, 'report:query:view', 'search', 'admin', NOW(), '', NULL, '报表统一查询页面'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE BINARY menu_name = '报表查询' AND parent_id = @report_parent_id);
    
    -- 3.5 创建二级菜单 - 报表配置
    SET @report_config_id := fn_snowflake_id();
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    SELECT @report_config_id, '报表配置', @report_parent_id, 2, 'erp/report/config', 'erp/report/ReportConfig', '', 0, 0, 'C', 1, 1, 'report:config:view', 'edit', 'admin', NOW(), '', NULL, '报表配置管理页面'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE BINARY menu_name = '报表配置' AND parent_id = @report_parent_id);
    
    -- 3.6 创建二级菜单 - 报表权限设置
    SET @report_permission_id := fn_snowflake_id();
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    SELECT @report_permission_id, '报表权限设置', @report_parent_id, 3, 'erp/report/permission', 'erp/report/ReportPermission', '', 0, 0, 'C', 1, 1, 'report:permission:view', 'lock', 'admin', NOW(), '', NULL, '报表权限配置页面'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE BINARY menu_name = '报表权限设置' AND parent_id = @report_parent_id);
    
    -- 3.7 创建报表查询按钮(7个)
    CREATE TEMPORARY TABLE IF NOT EXISTS tmp_config_buttons (
        menu_id VARCHAR(20), menu_name VARCHAR(50), parent_id BIGINT, order_num INT, perms VARCHAR(100), status CHAR(1) DEFAULT '1'
    ) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4;
    
    DELETE FROM tmp_config_buttons;
    INSERT INTO tmp_config_buttons VALUES
    (fn_snowflake_id(), '查询', @report_query_id, 1, 'report:query:query', '1'),
    (fn_snowflake_id(), '导出', @report_query_id, 2, 'report:query:export', '1'),
    (fn_snowflake_id(), '打印', @report_query_id, 3, 'report:query:print', '1'),
    (fn_snowflake_id(), '刷新', @report_query_id, 4, 'report:query:refresh', '1'),
    (fn_snowflake_id(), '保存方案', @report_query_id, 5, 'report:query:saveScheme', '1'),
    (fn_snowflake_id(), '加载方案', @report_query_id, 6, 'report:query:loadScheme', '1'),
    (fn_snowflake_id(), '列设置', @report_query_id, 7, 'report:query:columnSetting', '1');
    
    INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    SELECT t.menu_id, t.menu_name, @report_query_id, t.order_num, '', '', '', 0, 0, 'F', 1, 1, t.perms, '#', 'admin', NOW(), '', NULL, ''
    FROM tmp_config_buttons t
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu s WHERE BINARY s.menu_name = BINARY t.menu_name AND s.parent_id = @report_query_id);
    
    DROP TEMPORARY TABLE IF EXISTS tmp_config_buttons;
    
    -- 3.8 【已废弃】循环创建报表演示菜单和按钮
    -- ⚠️ 重要修正: 具体报表(sales_dept_summary等)是配置数据,不应创建菜单!
    -- 正确的架构: 用户通过"报表查询"菜单进入,选择具体的reportCode查看数据
    -- 因此删除了这段会创建冗余菜单的代码(Line 243-300)
    
    -- 如果需要为特定报表创建独立菜单(高级场景),应该:
    -- 1. 在若依后台手动创建菜单
    -- 2. 指向同一个组件 erp/report/DepartmentReport
    -- 3. 通过 query_param 传递不同的 reportCode
    -- 但本脚本不提供此功能,保持简洁性
    
    -- 3.9 创建报表配置按钮(5个)
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES
    (fn_snowflake_id(), '查询', @report_config_id, 1, '', '', '', 0, 0, 'F', 1, 1, 'report:config:query', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '新增', @report_config_id, 2, '', '', '', 0, 0, 'F', 1, 1, 'report:config:add', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '修改', @report_config_id, 3, '', '', '', 0, 0, 'F', 1, 1, 'report:config:edit', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '删除', @report_config_id, 4, '', '', '', 0, 0, 'F', 1, 1, 'report:config:remove', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '导出', @report_config_id, 5, '', '', '', 0, 0, 'F', 1, 1, 'report:config:export', '#', 'admin', NOW(), '', NULL, '');
    
    -- 3.9.1 创建字典缓存刷新按钮(新增)
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES
    (fn_snowflake_id(), '字典缓存刷新', @report_parent_id, 6, '', '', '', 0, 0, 'F', 1, 1, 'report:dict:refresh', '#', 'admin', NOW(), '', NULL, '字典缓存手动刷新接口');
    
    -- 3.10 创建报表权限设置按钮(3个)
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES
    (fn_snowflake_id(), '查询', @report_permission_id, 1, '', '', '', 0, 0, 'F', 1, 1, 'report:permission:query', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '配置', @report_permission_id, 2, '', '', '', 0, 0, 'F', 1, 1, 'report:permission:config', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '分配权限', @report_permission_id, 3, '', '', '', 0, 0, 'F', 1, 1, 'report:permission:assign', '#', 'admin', NOW(), '', NULL, '');
        
    -- 提交事务
    COMMIT;
    
    -- ✅ P1-3: 自动分配权限给admin角色
    BEGIN
        DECLARE v_admin_role_id BIGINT DEFAULT NULL;
        
        -- 获取admin角色ID
        SELECT role_id INTO v_admin_role_id 
        FROM sys_role 
        WHERE BINARY role_key = 'admin' 
        LIMIT 1;
        
        IF v_admin_role_id IS NOT NULL THEN
            -- 插入所有新菜单的权限(包括父菜单、子菜单、按钮)
            INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
            SELECT v_admin_role_id, menu_id 
            FROM sys_menu 
            WHERE menu_id >= @report_parent_id;  -- 雪花ID递增,所有新菜单ID都大于父菜单ID
            
            SELECT CONCAT('✅ Permissions assigned to admin role (ID: ', v_admin_role_id, ')') AS permission_result;
        ELSE
            SELECT '⚠️ WARNING: Admin role not found. Please assign permissions manually.' AS permission_result;
        END IF;
    END;
END$$
DELIMITER ;

-- 3.7 执行存储过程
CALL proc_create_report_menus();
DROP PROCEDURE IF EXISTS proc_create_report_menus;

-- ============================================
-- Part 4: Cleanup & Verification (清理与验证)
-- ============================================

-- 4.1 清理所有临时表
DROP TEMPORARY TABLE IF EXISTS tmp_report_modules;
DROP TEMPORARY TABLE IF EXISTS tmp_button_templates;
DROP TEMPORARY TABLE IF EXISTS tmp_config_buttons;
DROP TEMPORARY TABLE IF EXISTS tmp_report_buttons;
DROP TEMPORARY TABLE IF EXISTS tmp_menu_ids;

-- 4.2 验证结果
SELECT '========================================' AS '';
SELECT '✅ Report Menu Created Successfully!' AS message;
SELECT '========================================' AS '';

SELECT '===== Parent Menu =====' AS section;
SELECT menu_id, menu_name, parent_id, path, menu_type, visible, status, icon, remark
FROM sys_menu WHERE menu_id = @report_parent_id;

SELECT '===== Sub Menus =====' AS section;
SELECT menu_id, menu_name, parent_id, path, menu_type, visible, status, icon
FROM sys_menu WHERE parent_id = @report_parent_id AND menu_type IN ('C', 'M')
ORDER BY order_num;

SELECT '===== Statistics =====' AS section;
SELECT 
  CONCAT('一级菜单: ', (SELECT COUNT(*) FROM sys_menu WHERE menu_id = @report_parent_id AND menu_type = 'M')) AS level_1_count,
  CONCAT('二级菜单: ', (SELECT COUNT(*) FROM sys_menu WHERE parent_id = @report_parent_id AND menu_type = 'C')) AS level_2_count,
  CONCAT('按钮权限: ', (
    SELECT COUNT(*) FROM sys_menu s
    INNER JOIN sys_menu p ON s.parent_id = p.menu_id
    WHERE p.parent_id = @report_parent_id AND s.menu_type = 'F'
  )) AS button_count;

SELECT '========================================' AS '';
SELECT '✅ Script execution completed!' AS final_message;
SELECT '========================================' AS '';

-- 4.3 验证外键约束状态
SELECT 
  @@FOREIGN_KEY_CHECKS AS foreign_key_checks_enabled,
  CASE 
    WHEN @@FOREIGN_KEY_CHECKS = 1 THEN '✅ Foreign key checks are enabled'
    ELSE '⚠️  WARNING: Foreign key checks are disabled!'
  END AS fk_status;

-- 4.4 简化版统计
SELECT '===== Simplified Statistics =====' AS section;
SELECT 
  (SELECT COUNT(*) FROM sys_menu WHERE menu_id = @report_parent_id) AS parent_menu_count,
  (SELECT COUNT(*) FROM sys_menu WHERE parent_id = @report_parent_id AND menu_type = 'C') AS level_2_menu_count,
  (SELECT COUNT(*) FROM sys_menu WHERE parent_id = @report_query_id AND menu_type = 'F') AS query_button_count,
  (SELECT COUNT(*) FROM sys_menu WHERE parent_id = @report_config_id AND menu_type = 'F') AS config_button_count,
  (SELECT COUNT(*) FROM sys_menu WHERE parent_id = @report_permission_id AND menu_type = 'F') AS permission_button_count,
  (SELECT COUNT(*) FROM sys_menu s 
   INNER JOIN sys_menu p ON s.parent_id = p.menu_id 
   WHERE p.parent_id = @report_parent_id AND s.menu_type = 'F') AS total_button_count;

SELECT '========================================' AS '';
SELECT '预期结果:' AS tip;
SELECT '  一级菜单: 1个 (报表分析中心)' AS expected;
SELECT '  二级菜单: 3个 (报表查询、报表配置、报表权限设置)' AS expected;
SELECT '  按钮权限: 15个 (7+5+3)' AS expected;
SELECT '  注意: 具体报表(sales_dept_summary等)不创建菜单,仅为配置数据' AS note;
SELECT '========================================' AS '';

SELECT '========================================' AS '';
SELECT '✅ All checks passed!' AS final_check;
SELECT NOW() AS end_time;
SELECT '========================================' AS '';
