-- ============================================
-- ERP 配置 JSON 强制拆分 - 数据库迁移脚本
-- ============================================
-- 功能：将大 JSON 配置拆分成独立的 5 个 JSON 字段
-- 作者：ERP Development Team
-- 日期：2026-03-26
-- ============================================

USE your_database_name;

-- ============================================
-- Step 1: 添加新的 JSON 字段
-- ============================================

ALTER TABLE `erp_page_config` 
ADD COLUMN `page_config` JSON COMMENT '页面基础配置 (page.json)' AFTER `config_content`,
ADD COLUMN `form_config` JSON COMMENT '表单 UI 组件配置 (form.json)' AFTER `page_config`,
ADD COLUMN `table_config` JSON COMMENT '表格查询配置 (table.json)' AFTER `form_config`,
ADD COLUMN `dict_config` JSON COMMENT '字典数据源配置 (dict.json)' AFTER `table_config`,
ADD COLUMN `business_config` JSON COMMENT '业务规则配置 (config.json)' AFTER `dict_config`;

-- ============================================
-- Step 2: 从 config_content 迁移数据到各个字段
-- ============================================

-- 迁移 page_config
UPDATE erp_page_config
SET page_config = JSON_EXTRACT(config_content, '$.pageConfig')
WHERE config_content IS NOT NULL 
  AND JSON_EXTRACT(config_content, '$.pageConfig') IS NOT NULL;

-- 迁移 form_config
UPDATE erp_page_config
SET form_config = JSON_EXTRACT(config_content, '$.formConfig')
WHERE config_content IS NOT NULL 
  AND JSON_EXTRACT(config_content, '$.formConfig') IS NOT NULL;

-- 迁移 table_config（包含 subTableQueryConfigs）
UPDATE erp_page_config
SET table_config = JSON_MERGE_PATCH(
  JSON_OBJECT('tableName', JSON_UNQUOTE(JSON_EXTRACT(config_content, '$.pageConfig.tableName'))),
  JSON_OBJECT('primaryKey', COALESCE(
    JSON_EXTRACT(config_content, '$.pageConfig.primaryKey'),
    '"id"'
  )),
  JSON_OBJECT('queryBuilder', COALESCE(
    JSON_EXTRACT(config_content, '$.subTableQueryConfigs.main'),
    JSON_EXTRACT(config_content, '$.queryConfig')
  )),
  JSON_OBJECT('columns', COALESCE(
    JSON_EXTRACT(config_content, '$.tableConfig.columns'),
    '[]'
  ))
)
WHERE config_content IS NOT NULL;

-- 迁移 dict_config
UPDATE erp_page_config
SET dict_config = JSON_EXTRACT(config_content, '$.dictionaryConfig')
WHERE config_content IS NOT NULL 
  AND JSON_EXTRACT(config_content, '$.dictionaryConfig') IS NOT NULL;

-- 迁移 business_config
UPDATE erp_page_config
SET business_config = JSON_MERGE_PATCH(
  COALESCE(JSON_EXTRACT(config_content, '$.businessConfig'), '{}'),
  JSON_OBJECT('apiConfig', COALESCE(JSON_EXTRACT(config_content, '$.apiConfig'), '{}')),
  JSON_OBJECT('messages', COALESCE(JSON_EXTRACT(config_content, '$.businessConfig.messages'), '{}'))
)
WHERE config_content IS NOT NULL;

-- ============================================
-- Step 3: 验证迁移结果
-- ============================================

SELECT 
  config_id,
  module_code,
  config_name,
  CASE 
    WHEN page_config IS NOT NULL THEN '✓' 
    ELSE '✗' 
  END AS page_config_ok,
  CASE 
    WHEN form_config IS NOT NULL THEN '✓' 
    ELSE '✗' 
  END AS form_config_ok,
  CASE 
    WHEN table_config IS NOT NULL THEN '✓' 
    ELSE '✗' 
  END AS table_config_ok,
  CASE 
    WHEN dict_config IS NOT NULL THEN '✓' 
    ELSE '✗' 
  END AS dict_config_ok,
  CASE 
    WHEN business_config IS NOT NULL THEN '✓' 
    ELSE '✗' 
  END AS business_config_ok,
  LENGTH(config_content) AS original_size,
  LENGTH(COALESCE(page_config, '')) + 
  LENGTH(COALESCE(form_config, '')) + 
  LENGTH(COALESCE(table_config, '')) + 
  LENGTH(COALESCE(dict_config, '')) + 
  LENGTH(COALESCE(business_config, '')) AS splitted_size
FROM erp_page_config
ORDER BY config_id;

-- ============================================
-- Step 4: 示例数据（销售订单模块）
-- ============================================

-- 如果 erp_page_config 表中没有数据，插入示例配置

INSERT INTO `erp_page_config` (
  `module_code`,
  `config_name`,
  `config_type`,
  `config_content`,
  `page_config`,
  `form_config`,
  `table_config`,
  `dict_config`,
  `business_config`,
  `version`,
  `status`,
  `is_public`,
  `create_by`,
  `create_time`,
  `remark`
) VALUES (
  'saleorder',
  '销售订单管理',
  'PAGE',
  -- config_content: 完整 JSON（向后兼容）
  '{
    "pageConfig": {
      "title": "销售订单管理",
      "moduleCode": "saleorder",
      "tableName": "t_sale_order"
    },
    "formConfig": {},
    "subTableQueryConfigs": {},
    "dictionaryConfig": {},
    "businessConfig": {}
  }',
  
  -- page_config: 页面基础配置
  '{
    "pageId": "saleorder",
    "pageName": "销售订单管理",
    "permission": "k3:saleorder:query",
    "layout": "standard"
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
        "required": true
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
        { "field": "create_time", "direction": "DESC" }
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
      }
    ]
  }',
  
  -- dict_config: 字典数据源配置
  '{
    "dicts": [
      {
        "dictKey": "customer_dict",
        "dictType": "dynamic",
        "table": "t_customer",
        "conditions": [
          { "field": "deleted", "operator": "isNull" }
        ],
        "fieldMapping": {
          "valueField": "customer_id",
          "labelField": "customer_name"
        },
        "orderBy": [
          { "field": "customer_name", "direction": "ASC" }
        ],
        "cacheable": true,
        "cacheTTL": 3600
      }
    ]
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
      }
    ],
    "messages": {
      "confirmDelete": "是否确认删除选中的 {count} 条数据？",
      "success": {
        "add": "新增成功",
        "edit": "修改成功"
      }
    }
  }',
  
  1,
  '1',
  '0',
  'admin',
  NOW(),
  'JSON 强制拆分示例配置'
)
ON DUPLICATE KEY UPDATE
  page_config = VALUES(page_config),
  form_config = VALUES(form_config),
  table_config = VALUES(table_config),
  dict_config = VALUES(dict_config),
  business_config = VALUES(business_config);

-- ============================================
-- Step 5: 创建历史表（用于版本控制）
-- ============================================

CREATE TABLE IF NOT EXISTS `erp_page_config_history` (
  `history_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史记录 ID',
  `config_id` BIGINT NOT NULL COMMENT '配置 ID',
  `module_code` VARCHAR(100) NOT NULL COMMENT '模块编码',
  `version` INT NOT NULL COMMENT '版本号',
  `page_config` JSON COMMENT '页面配置',
  `form_config` JSON COMMENT '表单配置',
  `table_config` JSON COMMENT '表格配置',
  `dict_config` JSON COMMENT '字典配置',
  `business_config` JSON COMMENT '业务配置',
  `change_reason` VARCHAR(500) COMMENT '变更原因',
  `create_by` VARCHAR(100) COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`history_id`),
  INDEX `idx_config_id` (`config_id`),
  INDEX `idx_module_code` (`module_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 配置历史表';

-- ============================================
-- Step 6: 创建触发器（自动记录历史）
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
    NEW.update_by
  );
END$$

DELIMITER ;

-- ============================================
-- Step 7: 查询视图（方便查看配置）
-- ============================================

CREATE OR REPLACE VIEW v_erp_config_summary AS
SELECT 
  config_id,
  module_code,
  config_name,
  config_type,
  version,
  status,
  JSON_LENGTH(page_config) AS page_fields_count,
  JSON_LENGTH(form_config) AS form_fields_count,
  JSON_LENGTH(table_config) AS table_columns_count,
  JSON_LENGTH(dict_config) AS dicts_count,
  JSON_LENGTH(business_config) AS buttons_count,
  create_time,
  update_time
FROM erp_page_config
ORDER BY create_time DESC;

-- ============================================
-- Step 8: 测试查询
-- ============================================

-- 查看所有配置的摘要信息
SELECT * FROM v_erp_config_summary;

-- 查看特定模块的详细配置
SELECT 
  module_code,
  JSON_UNQUOTE(JSON_EXTRACT(page_config, '$.pageName')) AS page_name,
  JSON_UNQUOTE(JSON_EXTRACT(form_config, '$.formConfig.dialogWidth')) AS dialog_width,
  JSON_LENGTH(JSON_EXTRACT(table_config, '$.columns')) AS table_columns,
  JSON_LENGTH(JSON_EXTRACT(dict_config, '$.dicts')) AS dictionaries,
  JSON_LENGTH(JSON_EXTRACT(business_config, '$.buttons')) AS buttons
FROM erp_page_config
WHERE module_code = 'saleorder';

-- ============================================
-- End of Migration Script
-- ============================================
