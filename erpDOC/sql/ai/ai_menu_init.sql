-- AI Module Menu Initialization SQL
-- Modular and Data-Driven Design
-- Updated: 2026-04-29
-- Version: v3.1 (Chat Workspace & Persistence)
-- Scope: 适用于 test 数据库
-- Features: 逻辑与数据分离、参数化配置、批量处理、增强健壮性
-- Description: AI模块菜单初始化(包含提示词管理)

USE test;

-- ============================================
-- Part 0: Pre-flight Check (执行前检查)
-- ============================================

-- 0.0 防御性清理临时表（防止上次执行残留）
DROP TEMPORARY TABLE IF EXISTS tmp_menu_ids;

-- 0.1 检查数据库连接
SELECT '========================================' AS '';
SELECT '🔍 Checking database connection...' AS step;
SELECT DATABASE() AS current_database;
SELECT USER() AS `current_user`;
SELECT VERSION() AS mysql_version;
SELECT NOW() AS start_time;
SELECT '========================================' AS '';

-- 0.2 检查是否已存在AI菜单（用于判断是否需要升级）
SET @existing_menu_count := (
    SELECT COUNT(*) FROM sys_menu 
    WHERE BINARY menu_name = 'AI智能工具' AND parent_id = 0
);

SELECT 
  CASE 
    WHEN @existing_menu_count > 0 THEN '⚠️  WARNING: AI menu already exists. Old data will be cleaned.'
    ELSE '✅ INFO: No existing AI menu. Will create new menus.'
  END AS status_check;

-- ============================================
-- Part 1: Utility Functions (工具函数层)
-- ============================================

-- 1.1 清理旧数据
DROP PROCEDURE IF EXISTS proc_cleanup_ai_menus;
DELIMITER $$
CREATE PROCEDURE proc_cleanup_ai_menus()
BEGIN
    DECLARE v_ai_menu_id BIGINT DEFAULT -1;
    
    -- 清理临时表
    DROP TEMPORARY TABLE IF EXISTS tmp_menu_ids;
    
    SET FOREIGN_KEY_CHECKS = 0;
    
    -- 获取AI智能工具菜单 ID
    SELECT menu_id INTO v_ai_menu_id 
    FROM sys_menu 
    WHERE BINARY menu_name = 'AI智能工具' AND parent_id = 0
    LIMIT 1;
    
    -- 级联删除（仅当菜单存在时）
    IF v_ai_menu_id > 0 THEN
        -- 先收集所有子菜单ID(包括二级、三级)
        CREATE TEMPORARY TABLE IF NOT EXISTS tmp_menu_ids (menu_id BIGINT);
        INSERT INTO tmp_menu_ids (menu_id)
        SELECT menu_id FROM sys_menu WHERE parent_id = v_ai_menu_id;
        
        -- 再收集孙菜单ID(三级菜单)
        INSERT INTO tmp_menu_ids (menu_id)
        SELECT s.menu_id 
        FROM sys_menu s
        INNER JOIN sys_menu p ON s.parent_id = p.menu_id
        WHERE p.parent_id = v_ai_menu_id;
        
        -- 清理角色权限(包括所有层级)
        DELETE FROM sys_role_menu 
        WHERE menu_id IN (SELECT menu_id FROM tmp_menu_ids) OR menu_id = v_ai_menu_id;
        
        -- 删除按钮(三级菜单的按钮)
        DELETE sm FROM sys_menu sm
        INNER JOIN sys_menu parent ON sm.parent_id = parent.menu_id
        INNER JOIN sys_menu grandparent ON parent.parent_id = grandparent.menu_id
        WHERE grandparent.parent_id = v_ai_menu_id;
        
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
        DELETE FROM sys_menu WHERE menu_id = v_ai_menu_id;
        
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
-- Part 2: Core Logic Layer (核心逻辑层)
-- ============================================

-- 2.1 执行清理
CALL proc_cleanup_ai_menus();
DROP PROCEDURE IF EXISTS proc_cleanup_ai_menus;

-- 2.2 创建存储过程（包含事务和错误处理）
DROP PROCEDURE IF EXISTS proc_create_ai_menus;
DELIMITER $$
CREATE PROCEDURE proc_create_ai_menus()
BEGIN
    -- 错误处理器：如果发生任何错误，自动回滚并输出错误信息
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT '========================================' AS '';
        SELECT '❌ ERROR: AI menu creation failed! Transaction rolled back.' AS error_message;
        SELECT '========================================' AS '';
        RESIGNAL;
    END;
    
    -- 开启事务
    START TRANSACTION;
    
    -- 2.3 创建父级菜单 - AI智能工具
    SET @ai_parent_id := fn_snowflake_id();
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, remark, create_by, create_time)
    SELECT @ai_parent_id, 'AI智能工具', 0, 7, 'ai', '', 0, 0, 'M', 1, 1, '', 'tool', 'AI智能工具目录', 'admin', NOW()
    WHERE NOT EXISTS (
        SELECT 1 FROM sys_menu 
        WHERE BINARY menu_name = 'AI智能工具' AND parent_id = 0
    )
    AND NOT EXISTS (
        SELECT 1 FROM sys_menu 
        WHERE path = 'ai' AND parent_id = 0  -- ✅ P1-2: 路径冲突检测
    );
    
    -- 2.4 创建二级菜单 - AI聊天工作台 [Updated v3.1]
    SET @ai_tools_id := fn_snowflake_id();
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    SELECT @ai_tools_id, 'AI聊天工作台', @ai_parent_id, 1, 'workspace', 'erp/ai/index', '', 0, 0, 'C', 1, 1, 'ai:workspace:view', 'input', 'admin', NOW(), '', NULL, 'AI统一聊天工作台(支持多Skill切换与会话持久化)'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE BINARY menu_name = 'AI聊天工作台' AND parent_id = @ai_parent_id);
    
    -- 2.5 创建AI聊天工作台按钮(6个)
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES
    (fn_snowflake_id(), '发送消息', @ai_tools_id, 1, '', '', '', 0, 0, 'F', 1, 1, 'ai:chat:send', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '新建会话', @ai_tools_id, 2, '', '', '', 0, 0, 'F', 1, 1, 'ai:session:create', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '切换模型', @ai_tools_id, 3, '', '', '', 0, 0, 'F', 1, 1, 'ai:model:switch', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '刷新', @ai_tools_id, 4, '', '', '', 0, 0, 'F', 1, 1, 'ai:workspace:refresh', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '导出记录', @ai_tools_id, 5, '', '', '', 0, 0, 'F', 1, 1, 'ai:chat:export', '#', 'admin', NOW(), '', NULL, '');
    
    -- 2.6 创建二级菜单 - 使用日志
    SET @ai_log_id := fn_snowflake_id();
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    SELECT @ai_log_id, '使用日志', @ai_parent_id, 2, 'log', 'erp/ai/AiUsageLog', '', 0, 0, 'C', 1, 1, 'ai:log:view', 'log', 'admin', NOW(), '', NULL, 'AI使用日志查询页面'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE BINARY menu_name = '使用日志' AND parent_id = @ai_parent_id);
    
    -- 2.7 创建使用日志按钮(4个)
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES
    (fn_snowflake_id(), '查询', @ai_log_id, 1, '', '', '', 0, 0, 'F', 1, 1, 'ai:log:query', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '导出', @ai_log_id, 2, '', '', '', 0, 0, 'F', 1, 1, 'ai:log:export', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '删除', @ai_log_id, 3, '', '', '', 0, 0, 'F', 1, 1, 'ai:log:remove', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '清空', @ai_log_id, 4, '', '', '', 0, 0, 'F', 1, 1, 'ai:log:clear', '#', 'admin', NOW(), '', NULL, '');
    
    -- 2.8 创建二级菜单 - 模型配置
    SET @ai_model_config_id := fn_snowflake_id();
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    SELECT @ai_model_config_id, '模型配置', @ai_parent_id, 3, 'model-config', 'erp/ai/ModelConfig', '', 0, 0, 'C', 1, 1, 'ai:config:view', 'system', 'admin', NOW(), '', NULL, 'AI模型配置管理页面'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE BINARY menu_name = '模型配置' AND parent_id = @ai_parent_id);
    
    -- 2.9 创建模型配置按钮(5个)
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES
    (fn_snowflake_id(), '查询', @ai_model_config_id, 1, '', '', '', 0, 0, 'F', 1, 1, 'ai:config:query', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '新增', @ai_model_config_id, 2, '', '', '', 0, 0, 'F', 1, 1, 'ai:config:add', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '修改', @ai_model_config_id, 3, '', '', '', 0, 0, 'F', 1, 1, 'ai:config:edit', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '删除', @ai_model_config_id, 4, '', '', '', 0, 0, 'F', 1, 1, 'ai:config:remove', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '测试连接', @ai_model_config_id, 5, '', '', '', 0, 0, 'F', 1, 1, 'ai:config:test', '#', 'admin', NOW(), '', NULL, '');
    
    -- 2.10 创建二级菜单 - Skill市场 [新增]
    SET @ai_skill_id := fn_snowflake_id();
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    SELECT @ai_skill_id, 'Skill市场', @ai_parent_id, 4, 'skill', 'erp/ai/components/SkillMarket', '', 0, 0, 'C', 1, 1, 'ai:skill:view', 'star', 'admin', NOW(), '', NULL, 'AI Skill市场管理页面'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE BINARY menu_name = 'Skill市场' AND parent_id = @ai_parent_id);
    
    -- 2.11 创建Skill市场按钮(4个)
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES
    (fn_snowflake_id(), '查询', @ai_skill_id, 1, '', '', '', 0, 0, 'F', 1, 1, 'ai:skill:query', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '执行', @ai_skill_id, 2, '', '', '', 0, 0, 'F', 1, 1, 'ai:skill:execute', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '分类管理', @ai_skill_id, 3, '', '', '', 0, 0, 'F', 1, 1, 'ai:skill:categoryManage', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '模板管理', @ai_skill_id, 4, '', '', '', 0, 0, 'F', 1, 1, 'ai:skill:templateManage', '#', 'admin', NOW(), '', NULL, '');
    
    -- 2.12 创建二级菜单 - 提示词管理 [新增 v2.2]
    SET @ai_prompt_id := fn_snowflake_id();
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    SELECT @ai_prompt_id, '提示词管理', @ai_parent_id, 5, 'prompt', 'erp/ai/prompt/index', '', 0, 0, 'C', 1, 1, 'ai:prompt:list', 'edit', 'admin', NOW(), '', NULL, 'AI提示词管理页面'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE BINARY menu_name = '提示词管理' AND parent_id = @ai_parent_id);
    
    -- 2.13 创建提示词管理按钮(5个)
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES
    (fn_snowflake_id(), '查询', @ai_prompt_id, 1, '', '', '', 0, 0, 'F', 1, 1, 'ai:prompt:query', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '新增', @ai_prompt_id, 2, '', '', '', 0, 0, 'F', 1, 1, 'ai:prompt:add', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '修改', @ai_prompt_id, 3, '', '', '', 0, 0, 'F', 1, 1, 'ai:prompt:edit', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '删除', @ai_prompt_id, 4, '', '', '', 0, 0, 'F', 1, 1, 'ai:prompt:remove', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '复制', @ai_prompt_id, 5, '', '', '', 0, 0, 'F', 1, 1, 'ai:prompt:copy', '#', 'admin', NOW(), '', NULL, '');
    
    -- 2.14 创建二级菜单 - AI系统配置 [新增 v2.3]
    SET @ai_system_config_id := fn_snowflake_id();
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    SELECT @ai_system_config_id, 'AI系统配置', @ai_parent_id, 6, 'system-config', 'erp/ai/systemConfig/index', '', 0, 0, 'C', 1, 1, 'ai:system-config:view', 'system', 'admin', NOW(), '', NULL, 'AI系统配置管理页面(API Key动态配置)'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE BINARY menu_name = 'AI系统配置' AND parent_id = @ai_parent_id);
    
    -- 2.15 创建AI系统配置按钮(3个)
    INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES
    (fn_snowflake_id(), '查询', @ai_system_config_id, 1, '', '', '', 0, 0, 'F', 1, 1, 'ai:system-config:query', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '修改', @ai_system_config_id, 2, '', '', '', 0, 0, 'F', 1, 1, 'ai:system-config:edit', '#', 'admin', NOW(), '', NULL, ''),
    (fn_snowflake_id(), '测试', @ai_system_config_id, 3, '', '', '', 0, 0, 'F', 1, 1, 'ai:system-config:test', '#', 'admin', NOW(), '', NULL, '');
    
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
            WHERE menu_id >= @ai_parent_id;  -- 雪花ID递增,所有新菜单ID都大于父菜单ID
            
            SELECT CONCAT('✅ Permissions assigned to admin role (ID: ', v_admin_role_id, ')') AS permission_result;
        ELSE
            SELECT '⚠️ WARNING: Admin role not found. Please assign permissions manually.' AS permission_result;
        END IF;
    END;
END$$
DELIMITER ;

-- 2.14 执行存储过程
CALL proc_create_ai_menus();
DROP PROCEDURE IF EXISTS proc_create_ai_menus;

-- ============================================
-- Part 3: Cleanup & Verification (清理与验证)
-- ============================================

-- 3.1 清理所有临时表
DROP TEMPORARY TABLE IF EXISTS tmp_menu_ids;

-- 3.2 验证结果
SELECT '========================================' AS '';
SELECT '✅ AI Menu Created Successfully!' AS message;
SELECT '========================================' AS '';

SELECT '===== Parent Menu =====' AS section;
SELECT menu_id, menu_name, parent_id, path, menu_type, visible, status, icon, remark
FROM sys_menu WHERE menu_id = @ai_parent_id;

SELECT '===== Sub Menus =====' AS section;
SELECT menu_id, menu_name, parent_id, path, menu_type, visible, status, icon
FROM sys_menu WHERE parent_id = @ai_parent_id AND menu_type IN ('C', 'M')
ORDER BY order_num;

SELECT '===== Statistics =====' AS section;
SELECT 
  CONCAT('一级菜单: ', (SELECT COUNT(*) FROM sys_menu WHERE menu_id = @ai_parent_id AND menu_type = 'M')) AS level_1_count,
  CONCAT('二级菜单: ', (SELECT COUNT(*) FROM sys_menu WHERE parent_id = @ai_parent_id AND menu_type = 'C')) AS level_2_count,
  CONCAT('按钮权限: ', (
    SELECT COUNT(*) FROM sys_menu s
    INNER JOIN sys_menu p ON s.parent_id = p.menu_id
    WHERE p.parent_id = @ai_parent_id AND s.menu_type = 'F'
  )) AS button_count;

SELECT '========================================' AS '';
SELECT '预期结果:' AS tip;
SELECT '  一级菜单: 1个 (AI智能工具)' AS expected;
SELECT '  二级菜单: 6个 (AI聊天工作台、使用日志、模型配置、Skill市场、提示词管理、AI系统配置)' AS expected;
SELECT '  按钮权限: 25个 (5+4+5+4+5+3)' AS expected;
SELECT '========================================' AS '';

SELECT '========================================' AS '';
SELECT '✅ Script execution completed!' AS final_message;
SELECT NOW() AS end_time;
SELECT '========================================' AS '';
