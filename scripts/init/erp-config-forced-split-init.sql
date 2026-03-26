-- ============================================
-- ERP配置化 JSON 强制拆分方案 - 数据库初始化脚本
-- 版本：v2.0 (强制拆分版)
-- 日期：2026-03-26
-- 说明：
--   - 强制拆分 JSON 配置为 5 个独立字段
--   - 不包含历史数据兼容
--   - 不包含旧实现清理逻辑
--   - 行业标准做法，支持复用、审计、高性能
-- ============================================

USE test;

-- ============================================
-- 警告：执行前请确认
-- ============================================
-- 1. 此脚本会删除所有旧数据并重建表结构
-- 2. 不再兼容旧的 config_content 单字段设计
-- 3. 采用全新的 5 字段强制拆分架构
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

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第二步：创建核心表结构（强制拆分版）
-- ============================================

-- 1. 页面配置表（核心表 - 5 字段强制拆分）
CREATE TABLE `erp_page_config` (
  `config_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置 ID',
  `module_code` VARCHAR(100) NOT NULL COMMENT '模块编码（唯一标识）',
  `config_name` VARCHAR(200) NOT NULL COMMENT '配置名称',
  `config_type` VARCHAR(50) NOT NULL DEFAULT 'PAGE' COMMENT '配置类型（PAGE/FORM/TABLE/DICT/BUSINESS）',
  
  -- ========== 强制拆分的 5 个 JSON 字段 ==========
  `page_config` JSON NOT NULL COMMENT '页面基础配置 (page.json)',
  `form_config` JSON COMMENT '表单 UI 组件配置 (form.json)',
  `table_config` JSON COMMENT '表格查询配置 (table.json)',
  `dict_config` JSON COMMENT '字典数据源配置 (dict.json)',
  `business_config` JSON COMMENT '业务规则配置 (config.json)',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='ERP 页面配置表（强制拆分版）';

-- 2. 配置历史表（版本控制）
CREATE TABLE `erp_page_config_history` (
  `history_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史 ID',
  `config_id` BIGINT NOT NULL COMMENT '配置 ID',
  `module_code` VARCHAR(100) NOT NULL COMMENT '模块编码',
  `version` INT NOT NULL COMMENT '版本号',
  
  -- 历史快照（同样拆分 5 个字段）
  `page_config` JSON COMMENT '页面配置快照',
  `form_config` JSON COMMENT '表单配置快照',
  `table_config` JSON COMMENT '表格配置快照',
  `dict_config` JSON COMMENT '字典配置快照',
  `business_config` JSON COMMENT '业务配置快照',
  
  `change_reason` VARCHAR(500) NULL COMMENT '变更原因',
  `change_type` VARCHAR(20) NOT NULL COMMENT '变更类型（CREATE/UPDATE/DELETE/ROLLBACK）',
  `create_by` VARCHAR(100) NOT NULL COMMENT '操作人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  
  PRIMARY KEY (`history_id`),
  KEY `idx_config_id` (`config_id`),
  KEY `idx_module_version` (`module_code`, `version`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='ERP配置历史表';

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
-- 第三步：插入示例数据（销售订单模块）
-- ============================================

-- 1. 销售订单页面配置（5 字段强制拆分）
INSERT INTO `erp_page_config` (
  `module_code`,
  `config_name`,
  `config_type`,
  `page_config`,
  `form_config`,
  `table_config`,
  `dict_config`,
  `business_config`,
  `version`,
  `status`,
  `is_public`,
  `create_by`,
  `remark`
) VALUES (
  'saleorder',
  '销售订单管理',
  'PAGE',
  
  -- page_config: 页面基础配置
  '{
    "pageId": "saleorder",
    "pageName": "销售订单管理",
    "permission": "k3:saleorder:query",
    "layout": "standard",
    "apiPrefix": "/erp/engine"
  }',
  
  -- form_config: 表单 UI 组件配置
  '{
    "formConfig": {
      "dialogWidth": "900px",
      "labelWidth": "120px",
      "layout": "horizontal"
    },
    "fields": [
      {
        "label": "订单编号",
        "prop": "order_no",
        "type": "input",
        "required": false,
        "readonly": true
      },
      {
        "label": "客户简称",
        "prop": "customer_name",
        "type": "select",
        "dict": "customer_dict",
        "required": true,
        "rules": [
          {
            "required": true,
            "message": "请选择客户",
            "trigger": "change"
          }
        ]
      }
    ]
  }',
  
  -- table_config: 表格查询配置
  '{
    "tableName": "t_sale_order",
    "primaryKey": "id",
    "queryBuilder": {
      "enabled": true,
      "fields": [
        {
          "prop": "order_no",
          "label": "订单编号",
          "op": "like",
          "component": "input"
        },
        {
          "prop": "customer_name",
          "label": "客户简称",
          "op": "like",
          "component": "input"
        }
      ],
      "defaultOrderBy": [
        {
          "field": "create_time",
          "direction": "DESC"
        }
      ]
    },
    "columns": [
      {
        "prop": "order_no",
        "label": "订单编号",
        "width": 150,
        "fixed": "left"
      },
      {
        "prop": "customer_name",
        "label": "客户简称",
        "width": 120
      },
      {
        "prop": "amount",
        "label": "金额",
        "width": 120,
        "align": "right",
        "renderType": "currency"
      }
    ],
    "pagination": {
      "defaultPageSize": 10,
      "pageSizeOptions": [10, 20, 50, 100]
    }
  }',
  
  -- dict_config: 字典数据源配置
  '{
    "dicts": [
      {
        "dictKey": "customer_dict",
        "dictType": "dynamic",
        "table": "t_customer",
        "conditions": [
          {
            "field": "deleted",
            "operator": "isNull"
          }
        ],
        "fieldMapping": {
          "valueField": "customer_id",
          "labelField": "customer_name"
        },
        "orderBy": [
          {
            "field": "customer_name",
            "direction": "ASC"
          }
        ],
        "cacheable": true,
        "cacheTTL": 3600
      }
    ],
    "globalCacheSettings": {
      "enabled": true,
      "defaultTTL": 3600
    }
  }',
  
  -- business_config: 业务规则配置
  '{
    "buttons": [
      {
        "key": "add",
        "label": "新增",
        "icon": "Plus",
        "permission": "k3:saleorder:add"
      },
      {
        "key": "edit",
        "label": "编辑",
        "icon": "Edit",
        "permission": "k3:saleorder:edit"
      },
      {
        "key": "delete",
        "label": "删除",
        "icon": "Delete",
        "permission": "k3:saleorder:remove",
        "confirm": "是否确认删除？"
      },
      {
        "key": "audit",
        "label": "审核",
        "icon": "Check",
        "permission": "k3:saleorder:audit",
        "confirm": "是否确认审核？"
      }
    ],
    "messages": {
      "confirmDelete": "是否确认删除选中的 {count} 条数据？",
      "confirmAudit": "是否确认审核选中的 {count} 条数据？",
      "success": {
        "add": "新增成功",
        "edit": "修改成功",
        "delete": "删除成功",
        "audit": "审核成功"
      }
    }
  }',
  
  1,
  '1',
  '0',
  'admin',
  '销售订单配置（强制拆分版）'
);

-- 2. 插入审批流程配置
INSERT INTO `erp_approval_flow` (
  `flow_name`,
  `module_code`,
  `flow_config`,
  `status`,
  `create_by`,
  `remark`
) VALUES (
  '销售订单审批流程',
  'saleorder',
  '{
    "steps": [
      {
        "stepNo": 1,
        "stepName": "提交",
        "action": "SUBMIT",
        "condition": {
          "field": "status",
          "operator": "=",
          "value": "Z"
        },
        "nextStep": 2
      },
      {
        "stepNo": 2,
        "stepName": "部门经理审批",
        "action": "APPROVE",
        "condition": {
          "field": "status",
          "operator": "=",
          "value": "A"
        },
        "approverType": "role",
        "approverValue": "dept_manager",
        "nextStep": 3
      },
      {
        "stepNo": 3,
        "stepName": "财务审批",
        "action": "APPROVE",
        "condition": {
          "field": "status",
          "operator": "=",
          "value": "B"
        },
        "approverType": "role",
        "approverValue": "finance_manager",
        "nextStep": 4
      },
      {
        "stepNo": 4,
        "stepName": "完成",
        "action": "COMPLETE",
        "condition": {
          "field": "status",
          "operator": "=",
          "value": "C"
        }
      }
    ]
  }',
  '1',
  'admin',
  '销售订单审批流程配置'
);

-- 3. 插入下推关系配置
INSERT INTO `erp_push_relation` (
  `source_module`,
  `target_module`,
  `relation_name`,
  `mapping_config`,
  `status`,
  `create_by`,
  `remark`
) VALUES (
  'saleorder',
  'deliveryOrder',
  '销售订单下推发货通知单',
  '{
    "fieldMapping": [
      {
        "source": "order_no",
        "target": "source_bill_no"
      },
      {
        "source": "customer_name",
        "target": "customer_name"
      }
    ],
    "entryMapping": [
      {
        "source": "material_id",
        "target": "material_id"
      },
      {
        "source": "qty",
        "target": "qty"
      },
      {
        "source": "price",
        "target": "price"
      }
    ],
    "triggerCondition": {
      "field": "status",
      "operator": "=",
      "value": "approved"
    }
  }',
  '1',
  'admin',
  '销售订单下推发货通知单配置'
);

-- ============================================
-- 第四步：创建菜单和权限配置
-- ============================================

-- 1. 创建 ERP 业务菜单目录
INSERT INTO `sys_menu` (
  `menu_id`,
  `menu_name`,
  `parent_id`,
  `order_num`,
  `path`,
  `component`,
  `is_frame`,
  `is_cache`,
  `menu_type`,
  `visible`,
  `status`,
  `perms`,
  `icon`,
  `remark`,
  `create_by`,
  `create_time`
) SELECT 
  '1943362205047062529',
  'ERP 业务菜单',
  0,
  5,
  'business',
  '',
  0,
  0,
  'M',
  1,
  1,
  '',
  'document',
  'ERP 业务菜单目录',
  'admin',
  NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE menu_id = '1943362205047062529'
);

-- 2. 创建销售订单管理菜单
INSERT INTO `sys_menu` (
  `menu_id`,
  `menu_name`,
  `parent_id`,
  `order_num`,
  `path`,
  `component`,
  `query_param`,
  `is_frame`,
  `is_cache`,
  `menu_type`,
  `visible`,
  `status`,
  `perms`,
  `icon`,
  `create_by`,
  `create_time`,
  `remark`
) SELECT 
  '1943362205181280258',
  '销售订单管理',
  '1943362205047062529',
  1,
  'saleorder',
  'erp/pageTemplate/configurable/BusinessConfigurable',
  '{"moduleCode":"saleorder"}',
  0,
  0,
  'C',
  1,
  1,
  'k3:saleorder:query',
  'document',
  'admin',
  NOW(),
  '销售订单管理 配置化页面'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE menu_id = '1943362205181280258'
);

-- 3. 创建按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '1943362205315497989', '查询', '1943362205181280258', 1, '', '', '', 0, 0, 'F', 1, 1, 'k3:saleorder:query', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '1943362205315497989');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '1943362205449715718', '新增', '1943362205181280258', 2, '', '', '', 0, 0, 'F', 1, 1, 'k3:saleorder:add', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '1943362205449715718');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '1943362205583933447', '修改', '1943362205181280258', 3, '', '', '', 0, 0, 'F', 1, 1, 'k3:saleorder:edit', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '1943362205583933447');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '1943362205718151176', '删除', '1943362205181280258', 4, '', '', '', 0, 0, 'F', 1, 1, 'k3:saleorder:remove', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '1943362205718151176');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '1943362205852368905', '审核', '1943362205181280258', 5, '', '', '', 0, 0, 'F', 1, 1, 'k3:saleorder:audit', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '1943362205852368905');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '1943362205986586634', '反审核', '1943362205181280258', 6, '', '', '', 0, 0, 'F', 1, 1, 'k3:saleorder:unAudit', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '1943362205986586634');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '1943362206120804363', '下推', '1943362205181280258', 7, '', '', '', 0, 0, 'F', 1, 1, 'k3:saleorder:push', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '1943362206120804363');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '1943362206255022092', '导出', '1943362205181280258', 8, '', '', '', 0, 0, 'F', 1, 1, 'k3:saleorder:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '1943362206255022092');

-- ============================================
-- 第五步：添加性能优化索引
-- ============================================

-- 1. 复合索引（优化高频查询）
ALTER TABLE `erp_page_config`
ADD INDEX `idx_module_status_version` (`module_code`, `status`, `version`);

ALTER TABLE `erp_approval_history`
ADD INDEX `idx_module_bill` (`module_code`, `bill_id`);

ALTER TABLE `erp_approval_history`
ADD INDEX `idx_bill_action` (`bill_id`, `approval_action`);

ALTER TABLE `erp_push_relation`
ADD INDEX `idx_source_status` (`source_module`, `status`);

-- ============================================
-- 第六步：创建触发器（自动记录历史）
-- ============================================

DELIMITER $$

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
    dict_config,
    business_config,
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
    NEW.dict_config,
    NEW.business_config,
    CONCAT('版本更新：', NEW.remark),
    'UPDATE',
    NEW.update_by
  );
END$$

DELIMITER ;

-- ============================================
-- 第七步：验证初始化结果
-- ============================================

-- 查看表结构
SELECT '========================================' AS '';
SELECT '✅ 表结构验证' AS '';
SELECT '========================================' AS '';
SHOW TABLES LIKE 'erp_%';

-- 查看配置数据
SELECT '========================================' AS '';
SELECT '✅ 配置数据验证' AS '';
SELECT '========================================' AS '';
SELECT 
  config_id,
  module_code,
  config_name,
  version,
  status,
  JSON_LENGTH(page_config) AS page_fields,
  JSON_LENGTH(form_config) AS form_fields,
  JSON_LENGTH(table_config) AS table_columns,
  JSON_LENGTH(dict_config) AS dictionaries,
  JSON_LENGTH(business_config) AS buttons
FROM erp_page_config;

-- 查看菜单数据
SELECT '========================================' AS '';
SELECT '✅ 菜单权限验证' AS '';
SELECT '========================================' AS '';
SELECT 
  menu_id,
  menu_name,
  parent_id,
  menu_type,
  perms
FROM sys_menu 
WHERE menu_id IN (
  '1943362205047062529',
  '1943362205181280258',
  '1943362205315497989',
  '1943362205449715718',
  '1943362205583933447',
  '1943362205718151176',
  '1943362205852368905',
  '1943362205986586634',
  '1943362206120804363',
  '1943362206255022092'
);

-- ============================================
-- 执行完成提示
-- ============================================

SELECT '========================================' AS '';
SELECT '🎉 ERP配置化 JSON 强制拆分方案初始化完成！' AS '';
SELECT '========================================' AS '';
SELECT '已创建：' AS summary;
SELECT '  ✅ 5 个核心表（强制拆分版）' AS tables;
SELECT '  ✅ 5 个 JSON 字段（page/form/table/dict/business）' AS fields;
SELECT '  ✅ 1 个页面配置（销售订单示例）' AS configs;
SELECT '  ✅ 1 个审批流程配置' AS flows;
SELECT '  ✅ 1 个下推关系配置' AS relations;
SELECT '  ✅ 10 个菜单权限' AS menus;
SELECT '  ✅ 自动历史触发器' AS triggers;
SELECT '========================================' AS '';
SELECT '下一步操作：' AS next_steps;
SELECT '  1. 编译后端：mvn clean install -DskipTests' AS step1;
SELECT '  2. 启动服务：java -jar ruoyi-admin-wms.jar' AS step2;
SELECT '  3. 访问页面：http://localhost/erp/pageTemplate/configurable/BusinessConfigurable?moduleCode=saleorder' AS step3;
SELECT '========================================' AS '';
