-- ============================================
-- ERP 配置化页面 - 清理脚本
-- 模块：销售订单管理
-- 创建时间：2026-03-25 11:41:41
-- 说明：删除由 menu-config.sql 和 page-config-data.sql 创建的数据
-- 警告：执行此脚本将删除相关菜单、按钮权限和页面配置数据
-- ============================================

-- 开始事务（如果表支持事务）
SET autocommit=0;
START TRANSACTION;

-- ============================================
-- 1. 删除按钮权限
-- ============================================
-- 直接根据权限标识符删除，不依赖父菜单 ID（更安全可靠）
DELETE FROM sys_menu 
WHERE perms IN ('k3:saleorder:query', 'k3:saleorder:add', 'k3:saleorder:edit', 'k3:saleorder:remove', 'k3:saleorder:audit', 'k3:saleorder:unAudit', 'k3:saleorder:push', 'k3:saleorder:export');

SELECT ROW_COUNT() AS '已删除按钮数量';

-- ============================================
-- 2. 删除子菜单（业务页面）
-- ============================================
-- 使用变量避免 1093 错误
SET @parent_menu_id := (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name COLLATE utf8mb4_general_ci = 'ERP 业务菜单' COLLATE utf8mb4_general_ci LIMIT 1) AS tmp);

DELETE FROM sys_menu 
WHERE menu_name COLLATE utf8mb4_general_ci = '销售订单管理' COLLATE utf8mb4_general_ci
AND parent_id = @parent_menu_id;

SELECT ROW_COUNT() AS '已删除子菜单数量';

-- ============================================
-- 3. 删除页面配置数据
-- ============================================
DELETE FROM erp_page_config 
WHERE module_code = 'saleorder';

SELECT ROW_COUNT() AS '已删除配置数量';

-- ============================================
-- 4. 删除配置历史记录（如果存在）
-- ============================================
DELETE FROM erp_page_config_history 
WHERE config_id IN (
    SELECT config_id FROM erp_page_config WHERE module_code = 'saleorder'
);

SELECT ROW_COUNT() AS '已删除配置历史数量';

-- ============================================
-- 5. 可选：删除父菜单（谨慎使用）
-- ============================================
-- 注意：只有在确认没有其他子菜单时才执行以下 SQL
-- 取消下面的注释以启用此功能

-- DELETE FROM sys_menu 
-- WHERE menu_id = '1943362205047062529'
-- AND NOT EXISTS (
--     SELECT 1 FROM sys_menu WHERE parent_id = '1943362205047062529'
-- );

-- SELECT ROW_COUNT() AS '已删除父菜单数量';

-- ============================================
-- 提交事务
-- ============================================
COMMIT;

-- ============================================
-- 验证清理结果
-- ============================================
SELECT '✅ 销售订单管理 数据清理完成！' AS message;

-- 验证菜单是否已删除
SELECT COUNT(*) AS remaining_menus 
FROM sys_menu 
WHERE menu_name COLLATE utf8mb4_general_ci = '销售订单管理' COLLATE utf8mb4_general_ci;

-- 验证配置是否已删除
SELECT COUNT(*) AS remaining_configs 
FROM erp_page_config 
WHERE module_code = 'saleorder';

-- 显示剩余的按钮权限（应该为 0）
SELECT COUNT(*) AS remaining_buttons 
FROM sys_menu 
WHERE perms IN ('k3:saleorder:query', 'k3:saleorder:add', 'k3:saleorder:edit', 'k3:saleorder:remove', 'k3:saleorder:audit', 'k3:saleorder:unAudit', 'k3:saleorder:push', 'k3:saleorder:export');

-- ============================================
-- 使用说明
-- ============================================
/*
💡 提示：
1. 此脚本用于清理测试数据或回滚模块
2. 脚本使用事务保护，要么全部成功，要么全部回滚
3. 父菜单删除默认禁用，需要手动取消注释
4. 执行前请确认模块名称和配置编码

⚠️ 警告：
1. 执行此脚本将永久删除数据
2. 建议先备份相关数据
3. 生产环境慎用

使用示例：
mysql -u root -p test < cleanup-config.sql
*/
