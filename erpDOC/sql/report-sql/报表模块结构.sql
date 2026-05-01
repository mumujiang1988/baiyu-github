-- ============================================
-- 报表模块完整初始化脚本(生产就绪版)
-- 用途: 一键初始化报表模块所有表结构
-- 作者: Lingma
-- 日期: 2026-04-30
-- 版本: v4.0
-- 
-- 【重要】使用说明:
-- 1. ⚠️  此脚本会删除所有rpt_开头的表并重建,执行前请务必备份!
-- 2. 确保MySQL版本 >= 5.7(支持JSON类型)
-- 3. 使用命令: mysql -u root -p your_database < report-module-production-ready.sql
-- 4. 脚本会自动检测并删除旧表,然后重新创建
-- 5. 本脚本支持重复执行,每次都会完全重置
-- 6. ⚠️  如需初始化示例数据,请额外执行: report-sample-data.sql
-- ============================================

SET NAMES utf8mb4 COLLATE utf8mb4_general_ci;
SET CHARACTER SET utf8mb4;

-- ============================================
-- 选择目标数据库(请根据实际情况修改)
-- ============================================
USE test;  -- ⚠️ 如果需要其他数据库,请修改此行
SELECT CONCAT('✅ 已切换到数据库: ', DATABASE()) AS database_info;

-- ============================================
-- MySQL版本检查
-- ============================================
SELECT 
  CONCAT('MySQL版本: ', VERSION()) AS version_info,
  CASE 
    WHEN VERSION() >= '5.7.0' THEN '✅ 版本符合要求'
    ELSE '❌ 版本过低,需要 >= 5.7.0'
  END AS version_check;

-- 版本警告(仅提示,不终止执行)
SELECT 
  CASE 
    WHEN VERSION() < '5.7.0' THEN '⚠️  警告: MySQL版本过低,JSON类型可能不支持!请升级到5.7+' 
    ELSE ''
  END AS version_warning;

-- ============================================
-- 第一步: 检测并删除旧表(完全重置)
-- ============================================
SELECT '========================================' AS '';
SELECT '⚠️  警告: 即将删除所有rpt_开头的表!' AS warning;
SELECT '========================================' AS '';

-- 禁用外键检查(确保删除顺序不受限制)
SET FOREIGN_KEY_CHECKS = 0;

-- 1.1 删除字典配置表
DROP TABLE IF EXISTS `rpt_dict_config`;
SELECT '✅ 已删除表: rpt_dict_config' AS progress;

-- 1.2 删除用户筛选方案表
DROP TABLE IF EXISTS `rpt_user_filter_scheme`;
SELECT '✅ 已删除表: rpt_user_filter_scheme' AS progress;

-- 1.3 删除用户报表偏好配置表
DROP TABLE IF EXISTS `rpt_user_report_preference`;
SELECT '✅ 已删除表: rpt_user_report_preference' AS progress;

-- 1.4 删除报表访问日志表
DROP TABLE IF EXISTS `rpt_report_access_log`;
SELECT '✅ 已删除表: rpt_report_access_log' AS progress;

-- 1.5 删除报表配置表
DROP TABLE IF EXISTS `rpt_report_config`;
SELECT '✅ 已删除表: rpt_report_config' AS progress;

SELECT '========================================' AS '';
SELECT '✅ 旧表清理完成! 开始重新创建...' AS progress;
SELECT '========================================' AS '';

-- ============================================
-- 第二步: 创建报表模块表结构
-- ============================================
SELECT '========================================' AS '';
SELECT '开始创建报表模块表结构...' AS progress;
SELECT '========================================' AS '';

-- 注意: MySQL的DDL语句(CREATE TABLE)会自动提交事务,因此不需要START TRANSACTION
-- 我们使用错误处理机制来确保原子性

-- ============================================
-- 2.1 报表配置表 - rpt_report_config
-- ============================================

CREATE TABLE `rpt_report_config` (
  `report_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报表ID',
  `report_code` VARCHAR(100) NOT NULL COMMENT '报表编码(唯一标识)',
  `report_name` VARCHAR(200) NOT NULL COMMENT '报表名称',
  `report_type` VARCHAR(50) NOT NULL DEFAULT 'DEPT_ANALYSIS' COMMENT '报表类型(DEPT_ANALYSIS/PERSONAL_PERFORMANCE/BUSINESS_TREND)',
  
  -- 分组配置(用于左侧树形导航)
  `category` VARCHAR(50) NOT NULL DEFAULT 'DEFAULT' COMMENT '报表分类(SALES/PURCHASE/FINANCE/INVENTORY等)',
  `parent_category` VARCHAR(50) NULL COMMENT '父级分类(支持多级嵌套,为空表示顶级分类)',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号(同category内排序)',
  
  -- 核心配置(JSON格式,支持动态扩展)
  `sql_template` JSON NOT NULL DEFAULT (JSON_OBJECT('query', '', 'params', JSON_ARRAY())) COMMENT 'SQL查询模板(支持${paramName:columnName}占位符),必须包含query和params字段',
  `field_config` JSON NOT NULL DEFAULT (JSON_ARRAY()) COMMENT '字段配置(JSON数组),每个元素包含fieldName、label、type等字段',
  `dimension_config` JSON DEFAULT NULL COMMENT '维度配置(部门/时间/产品等分组维度)',
  `drill_config` JSON DEFAULT NULL COMMENT '穿透配置(下钻层级/跳转规则/参数映射)',
  `permission_config` JSON DEFAULT NULL COMMENT '权限配置(报表级+字段级),默认为所有角色可见',
  `chart_config` JSON DEFAULT NULL COMMENT '图表配置(可选,用于可视化)',
  
  `status` CHAR(1) NOT NULL DEFAULT '1' COMMENT '状态(0禁用 1启用 2已删除)',
  `remark` VARCHAR(500) NULL COMMENT '备注说明',
  `create_by` VARCHAR(100) NOT NULL COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(100) NULL COMMENT '更新者',
  `update_time` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`report_id`),
  UNIQUE KEY `uk_report_code` (`report_code`),
  KEY `idx_status` (`status`),
  KEY `idx_report_type` (`report_type`),
  KEY `idx_category` (`category`),
  KEY `idx_category_sort` (`category`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报表配置表';

SELECT '✅ 表 rpt_report_config 创建成功' AS progress;

-- ============================================
-- 2.2 报表访问日志表 - rpt_report_access_log
-- ============================================

CREATE TABLE `rpt_report_access_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `report_code` VARCHAR(100) NOT NULL COMMENT '报表编码',
  `user_id` BIGINT NOT NULL COMMENT '访问用户ID',
  `user_name` VARCHAR(100) NULL COMMENT '访问用户名',
  `dept_id` BIGINT NULL COMMENT '访问时所属部门ID',
  `access_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
  `query_params` JSON NULL COMMENT '查询参数快照',
  `result_count` INT NULL COMMENT '返回记录数',
  `duration_ms` INT NULL COMMENT '查询耗时(毫秒)',
  `ip_address` VARCHAR(50) NULL COMMENT 'IP地址',
  `status` CHAR(1) NOT NULL DEFAULT '1' COMMENT '执行状态(0失败 1成功)',
  `error_msg` VARCHAR(1000) NULL COMMENT '错误信息(失败时记录)',
  
  PRIMARY KEY (`log_id`),
  KEY `idx_report_time` (`report_code`, `access_time`),
  KEY `idx_user_time` (`user_id`, `access_time`),
  KEY `idx_access_time` (`access_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报表访问日志表';

SELECT '✅ 表 rpt_report_access_log 创建成功' AS progress;

-- ============================================
-- 2.3 用户报表偏好配置表 - rpt_user_report_preference
-- ============================================

CREATE TABLE `rpt_user_report_preference` (
  `preference_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '偏好ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `report_code` VARCHAR(100) NOT NULL COMMENT '报表编码',
  
  -- 列配置(JSON格式)
  `column_config` JSON NOT NULL COMMENT '列配置(字段显示/隐藏/顺序/宽度/合计等)',
  
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`preference_id`),
  UNIQUE KEY `uk_user_report` (`user_id`, `report_code`),
  KEY `idx_report_code` (`report_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户报表偏好配置表';

SELECT '✅ 表 rpt_user_report_preference 创建成功' AS progress;

-- ============================================
-- 2.4 用户筛选方案表 - rpt_user_filter_scheme
-- ============================================

CREATE TABLE `rpt_user_filter_scheme` (
  `scheme_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '方案ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `report_code` VARCHAR(100) NOT NULL COMMENT '报表编码',
  `scheme_name` VARCHAR(50) NOT NULL COMMENT '方案名称(最多50字符)',
  
  -- 筛选值(JSON格式)
  `filter_values` JSON NOT NULL COMMENT '筛选条件值',
  
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`scheme_id`),
  UNIQUE KEY `uk_user_report_name` (`user_id`, `report_code`, `scheme_name`),
  KEY `idx_user_report` (`user_id`, `report_code`),
  KEY `idx_report_code` (`report_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户筛选方案表';

SELECT '✅ 表 rpt_user_filter_scheme 创建成功' AS progress;

-- ============================================
-- 2.5 报表字典配置表 - rpt_dict_config
-- ============================================

CREATE TABLE `rpt_dict_config` (
  `dict_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典ID',
  `dict_type` VARCHAR(50) NOT NULL COMMENT '字典类型(REPORT_TYPE/REPORT_CATEGORY)',
  `dict_label` VARCHAR(100) NOT NULL COMMENT '字典标签(显示名称)',
  `dict_value` VARCHAR(50) NOT NULL COMMENT '字典值(编码)',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` CHAR(1) NOT NULL DEFAULT '1' COMMENT '状态(0禁用 1启用)',
  `remark` VARCHAR(200) NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  PRIMARY KEY (`dict_id`),
  UNIQUE KEY `uk_type_value` (`dict_type`, `dict_value`),
  KEY `idx_dict_type` (`dict_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报表字典配置表';

SELECT '✅ 表 rpt_dict_config 创建成功' AS progress;

SELECT '========================================' AS '';
SELECT '✅ 表结构创建完成!' AS progress;
SELECT '========================================' AS '';

-- ============================================
-- 第三步: 初始化系统基础字典数据(必需)
-- ============================================
SELECT '开始初始化系统基础字典数据...' AS progress;

-- 3.1 报表类型字典(系统核心枚举,必须存在)
INSERT INTO `rpt_dict_config` (`dict_type`, `dict_label`, `dict_value`, `sort_order`, `status`, `remark`) VALUES
('REPORT_TYPE', '部门分析', 'DEPT_ANALYSIS', 1, '1', '按部门维度统计分析'),
('REPORT_TYPE', '个人绩效', 'PERSONAL_PERFORMANCE', 2, '1', '按人员维度统计绩效'),
('REPORT_TYPE', '业务趋势', 'BUSINESS_TREND', 3, '1', '业务发展趋势分析')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), sort_order = VALUES(sort_order);

SELECT '✅ 报表类型字典: 3条' AS progress;

-- 3.2 报表分类字典(系统核心枚举,必须存在)
INSERT INTO `rpt_dict_config` (`dict_type`, `dict_label`, `dict_value`, `sort_order`, `status`, `remark`) VALUES
('REPORT_CATEGORY', '销售报表', 'SALES', 1, '1', '销售相关报表'),
('REPORT_CATEGORY', '采购报表', 'PURCHASE', 2, '1', '采购相关报表'),
('REPORT_CATEGORY', '财务报表', 'FINANCE', 3, '1', '财务相关报表'),
('REPORT_CATEGORY', '库存报表', 'INVENTORY', 4, '1', '库存相关报表'),
('REPORT_CATEGORY', '生产报表', 'PRODUCTION', 5, '1', '生产相关报表'),
('REPORT_CATEGORY', '人力报表', 'HR', 6, '1', '人力资源相关报表')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), sort_order = VALUES(sort_order);

SELECT '✅ 报表分类字典: 6条' AS progress;

SELECT '========================================' AS '';
SELECT '✅ 系统基础数据初始化完成!' AS progress;
SELECT '📌 注意: 如需初始化示例报表配置(3个报表),请执行: report-sample-data.sql' AS notice;
SELECT '========================================' AS '';


-- ============================================
-- 第四步: 恢复外键检查并验证
-- ============================================

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

SELECT '========================================' AS '';
SELECT '✅ 报表模块初始化完成!' AS result;
SELECT '========================================' AS '';
SELECT CONCAT('已创建表: ', COUNT(*)) AS summary FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name LIKE 'rpt_%';
SELECT '已初始化字典: 9条' AS summary;
SELECT '  - 报表类型: 3条 (DEPT_ANALYSIS/PERSONAL_PERFORMANCE/BUSINESS_TREND)' AS summary;
SELECT '  - 报表分类: 6条 (SALES/PURCHASE/FINANCE/INVENTORY/PRODUCTION/HR)' AS summary;
SELECT '========================================' AS '';
SELECT '📌 提示: 如需初始化示例报表配置(3个完整报表),请执行:' AS tip;
SELECT '   mysql -u root -p your_database < report-sample-data.sql' AS tip;
SELECT '========================================' AS '';

-- ============================================
-- 验证提示
-- ============================================
SELECT '验证命令:' AS tip;
SELECT 'SHOW TABLES LIKE ''rpt_%'';' AS cmd;
SELECT 'SELECT report_code, report_name FROM rpt_report_config ORDER BY sort_order;' AS cmd;
SELECT 'SELECT dict_type, dict_label, dict_value FROM rpt_dict_config ORDER BY dict_type, sort_order;' AS cmd;
