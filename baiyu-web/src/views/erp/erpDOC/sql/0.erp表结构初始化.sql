-- ============================================
-- ERP 配置化 JSON 强制拆分方案 - 数据库初始化脚本
-- 版本：v4.0 (完全拆分版 - 9 字段)
-- 日期：2026-03-28
-- 说明：
--   - 强制拆分 JSON 配置为 9 个独立字段
--   - 新增 api_config（API接口配置）
--   - 新增 search_config（搜索区域配置）
--   - 新增 action_config（按钮操作配置）
--   - 不包含历史数据兼容
--   - 行业标准做法，支持复用、审计、高性能
-- ============================================

USE test;

-- ============================================
-- 警告：执行前请确认
-- ============================================
-- 1. 此脚本会删除所有旧数据并重建表结构
-- 2. 不再兼容旧的 config_content 单字段设计
-- 3. 采用全新的 9 字段强制拆分架构
--    (page/form/table/search/action/api/dict/business/detail)
-- ============================================

-- ============================================
-- 第一步：清理旧数据（如果存在）
-- ============================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `erp_page_config_history`;
DROP TABLE IF EXISTS `erp_approval_history`;
DROP TABLE IF EXISTS `erp_push_relation`;
DROP TABLE IF EXISTS `erp_approval_flow`;
DROP TABLE IF EXISTS `erp_page_config`;

-- 删除旧触发器（防止重复创建）
DROP TRIGGER IF EXISTS `trg_erp_config_history`;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第二步：创建核心表结构（强制拆分版 - 9 字段）
-- ============================================

-- 1. 页面配置表（核心表 - 9 字段强制拆分）
CREATE TABLE `erp_page_config` (
  `config_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置 ID',
  `module_code` VARCHAR(100) NOT NULL COMMENT '模块编码（唯一标识）',
  `config_name` VARCHAR(200) NOT NULL COMMENT '配置名称',
  `config_type` VARCHAR(50) NOT NULL DEFAULT 'PAGE' COMMENT '配置类型（PAGE/FORM/TABLE/DICT/BUSINESS）',
  
  -- ========== 强制拆分的 9 个 JSON 字段 ==========
  `page_config` JSON NOT NULL COMMENT '页面基础配置 (page.json)',
  `form_config` JSON COMMENT '表单 UI 组件配置 (form.json)',
  `table_config` JSON COMMENT '表格列配置 (table.json)',
  `search_config` JSON COMMENT '查询表单配置 (search.json)',
  `action_config` JSON COMMENT '按钮操作配置 (action.json)',
  `api_config` JSON COMMENT 'API接口配置 (api.json)',
  `dict_config` JSON COMMENT '字典数据源配置 (dict.json)',
  `business_config` JSON COMMENT '业务规则配置 (config.json)',
  `detail_config` JSON COMMENT '详情页配置 (detail.json)',
  -- ===========================================
  
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
  `status` CHAR(1) NOT NULL DEFAULT '1' COMMENT '状态（0 禁用 1 启用）',
  `is_public` CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否公共配置（1 是 0 否）',
  `parent_config_id` BIGINT NULL COMMENT '父配置 ID（用于配置继承）',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_by` VARCHAR(100) NOT NULL COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(100) NULL COMMENT '更新者',
  `update_time` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_module_code` (`module_code`),
  KEY `idx_status` (`status`),
  KEY `idx_parent` (`parent_config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='ERP 页面配置表（强制拆分版 - 9 字段）';

-- 2. 配置历史表（版本控制）
CREATE TABLE `erp_page_config_history` (
  `history_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史 ID',
  `config_id` BIGINT NOT NULL COMMENT '配置 ID',
  `module_code` VARCHAR(100) NOT NULL COMMENT '模块编码',
  `version` INT NOT NULL COMMENT '版本号',
  
  -- 历史快照（同样拆分 9 个字段）
  `page_config` JSON COMMENT '页面配置快照',
  `form_config` JSON COMMENT '表单配置快照',
  `table_config` JSON COMMENT '表格配置快照',
  `search_config` JSON COMMENT '搜索配置快照',
  `action_config` JSON COMMENT '按钮操作配置快照',
  `api_config` JSON COMMENT 'API配置快照',
  `dict_config` JSON COMMENT '字典配置快照',
  `business_config` JSON COMMENT '业务配置快照',
  `detail_config` JSON COMMENT '详情配置快照',
  
  `change_reason` VARCHAR(500) NULL COMMENT '变更原因',
  `change_type` VARCHAR(20) NOT NULL COMMENT '变更类型（CREATE/UPDATE/DELETE/ROLLBACK）',
  `create_by` VARCHAR(100) NOT NULL COMMENT '操作人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  
  PRIMARY KEY (`history_id`),
  KEY `idx_config_id` (`config_id`),
  KEY `idx_module_version` (`module_code`, `version`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='ERP 配置历史表';

-- 3. 审批流程配置表
CREATE TABLE `erp_approval_flow` (
  `flow_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流程 ID',
  `flow_name` VARCHAR(200) NOT NULL COMMENT '流程名称',
  `module_code` VARCHAR(100) NOT NULL COMMENT '模块编码',
  `flow_config` JSON NOT NULL COMMENT '流程配置（JSON 格式）',
  `status` CHAR(1) NOT NULL DEFAULT '1' COMMENT '状态（0 禁用 1 启用）',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_by` VARCHAR(100) NOT NULL COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(100) NULL COMMENT '更新者',
  `update_time` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`flow_id`),
  UNIQUE KEY `uk_module` (`module_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='ERP 审批流程配置表';

-- 4. 审批历史记录表
CREATE TABLE `erp_approval_history` (
  `history_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史 ID',
  `flow_id` BIGINT NULL COMMENT '流程 ID',
  `module_code` VARCHAR(100) NOT NULL COMMENT '模块编码',
  `bill_id` BIGINT NOT NULL COMMENT '单据 ID',
  `bill_no` VARCHAR(100) NULL COMMENT '单据编号',
  `approval_action` VARCHAR(50) NOT NULL COMMENT '审批动作（SUBMIT/APPROVE/REJECT/WITHDRAW）',
  `approval_step` INT NULL COMMENT '审批步骤',
  `approval_user_id` BIGINT NOT NULL COMMENT '审批人 ID',
  `approval_user_name` VARCHAR(100) NULL COMMENT '审批人姓名',
  `approval_opinion` VARCHAR(500) NULL COMMENT '审批意见',
  `approval_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
  `before_status` VARCHAR(50) NULL COMMENT '审批前状态',
  `after_status` VARCHAR(50) NULL COMMENT '审批后状态',
  
  PRIMARY KEY (`history_id`),
  KEY `idx_flow_id` (`flow_id`),
  KEY `idx_module_bill` (`module_code`, `bill_id`),
  KEY `idx_bill_action` (`bill_id`, `approval_action`),
  KEY `idx_approval_time` (`approval_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='ERP 审批历史记录表';

-- 5. 下推关系配置表
CREATE TABLE `erp_push_relation` (
  `relation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关系 ID',
  `source_module` VARCHAR(100) NOT NULL COMMENT '源模块编码',
  `target_module` VARCHAR(100) NOT NULL COMMENT '目标模块编码',
  `relation_name` VARCHAR(200) NOT NULL COMMENT '关系名称',
  `mapping_config` JSON NOT NULL COMMENT '字段映射配置（JSON 格式）',
  `condition_config` JSON NULL COMMENT '下推条件配置（JSON 格式）',
  `status` CHAR(1) NOT NULL DEFAULT '1' COMMENT '状态（0 禁用 1 启用）',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_by` VARCHAR(100) NOT NULL COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(100) NULL COMMENT '更新者',
  `update_time` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`relation_id`),
  KEY `idx_source_target` (`source_module`, `target_module`),
  KEY `idx_source_status` (`source_module`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='ERP 下推关系配置表';

-- ============================================
-- 第三步：配置数据说明
-- ============================================

-- 注意：本节已移除示例配置数据，仅保留表结构初始化
-- 如需添加配置数据，请执行：销售订单初始化配置.sql
-- 该脚本将导入销售订单模块的完整配置（9 字段强制拆分，含 api_config、search_config 和 action_config）

-- 1. 销售订单页面配置（9 字段强制拆分）- 已移除
-- 2. 插入审批流程配置 - 已移除
-- 3. 插入下推关系配置 - 已移除
-- 说明：配置数据已移至【销售订单初始化配置.sql】，包含完整的 9 字段配置

-- ============================================
-- 第四步：验证表结构
-- ============================================

-- 查看已创建的表
SELECT '========================================' AS '';
SELECT '✅ ERP Table Structure Created!' AS message;
SELECT '========================================' AS '';
SHOW TABLES LIKE 'erp_%';

-- ============================================
-- 第五步：创建触发器（自动记录历史 - 支持 9 字段）
-- ============================================

DELIMITER $$

-- Drop existing trigger (idempotency)
DROP TRIGGER IF EXISTS `trg_erp_config_history`$$

CREATE TRIGGER `trg_erp_config_history`
AFTER UPDATE ON `erp_page_config`
FOR EACH ROW
BEGIN
  INSERT INTO erp_page_config_history (
    config_id,
    module_code,
    version,
    page_config,
    form_config,
    table_config,
    search_config,
    action_config,
    api_config,
    dict_config,
    business_config,
    detail_config,
    change_reason,
    change_type,
    create_by
  ) VALUES (
    NEW.config_id,
    NEW.module_code,
    NEW.version,
    NEW.page_config,
    NEW.form_config,
    NEW.table_config,
    NEW.search_config,
    NEW.action_config,
    NEW.api_config,
    NEW.dict_config,
    NEW.business_config,
    NEW.detail_config,
    CONCAT('Version update: ', NEW.remark),
    'UPDATE',
    NEW.update_by
  );
END$$

DELIMITER ;

-- ============================================
-- 第六步：验证初始化结果
-- ============================================

-- 查看表结构
SELECT '========================================' AS '';
SELECT '✅ ERP Table Structure Verified!' AS message;
SELECT '========================================' AS '';
SHOW TABLES LIKE 'erp_%';

SELECT '========================================' AS '';
SELECT '📋 Next Steps:' AS next_steps;
SELECT '========================================' AS '';
SELECT '  1. Execute: 1.配置页菜单脚本_优化版.sql (Menu initialization)' AS step1;
SELECT '  2. Execute: 2.字典类型补齐.sql (Dictionary types)' AS step2;
SELECT '  3. Execute: Business module init scripts (e.g., 销售订单初始化配置.sql)' AS step3;
SELECT '========================================' AS '';

-- ============================================
-- 执行完成提示
-- ============================================

SELECT '========================================' AS '';
SELECT '🎉 ERP Table Structure Initialization Complete!' AS message;
SELECT '========================================' AS '';
SELECT 'Created:' AS summary;
SELECT '  ✅ 5 core tables (9-field split architecture)' AS tables;
SELECT '  ✅ Auto-history trigger (supports 9 fields)' AS triggers;
SELECT '========================================' AS '';
SELECT 'Note: Menu data should be initialized by separate scripts.' AS note;
SELECT '========================================' AS '';
