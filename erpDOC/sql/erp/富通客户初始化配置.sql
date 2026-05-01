-- ============================================
-- ERP 配置 JSON 强制拆分方案 - 富通客户模块导入 SQL
-- 版本：v1.0 (9字段拆分 + API配置版)
-- 日期：2026-04-15
-- 说明：导入富通客户页面配置数据（9 字段强制拆分 + API配置）
--   - page_config: 页面基础配置
--   - form_config: 表单配置
--   - table_config: 表格列配置
--   - search_config: 搜索区域配置
--   - action_config: 按钮操作配置
--   - api_config: API接口配置
--   - dict_config: 字典数据源配置
--   - business_config: 业务规则配置
--   - detail_config: 详情页配置
-- ============================================

USE test;

-- ============================================
-- 第一步：清理旧数据（级联删除）
-- ============================================

SET FOREIGN_KEY_CHECKS = 0;

-- 级联删除富通客户相关的所有配置
DELETE FROM erp_page_config_history WHERE module_code = 'ftcustomer';
DELETE FROM erp_approval_history WHERE module_code = 'ftcustomer';
DELETE FROM erp_approval_flow WHERE module_code = 'ftcustomer';
DELETE FROM erp_push_relation WHERE source_module = 'ftcustomer' OR target_module = 'ftcustomer';
DELETE FROM erp_page_config WHERE module_code = 'ftcustomer';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第二步：插入富通客户页面配置数据
-- ============================================

-- 富通客户页面配置（9 字段强制拆分，包含 api_config、search_config 和 action_config）
INSERT INTO `erp_page_config` (
  `module_code`,
  `config_name`,
  `config_type`,
  `page_config`,
  `form_config`,
  `table_config`,
  `search_config`,
  `action_config`,
  `api_config`,
  `dict_config`,
  `business_config`,
  `detail_config`,
  `version`,
  `status`,
  `is_public`,
  `create_by`,
  `remark`
) VALUES (
  'ftcustomer',
  '富通客户管理',
  'PAGE', 
  '{
    "pageId": "ftcustomer",
    "title": "{entityName}管理",
    "permission": "erp:ftcustomer:query",
    "layout": "standard",
    "apiPrefix": "/erp/engine",
    "tableName": "bd_ft_customer",
    "billNoField": "code"
  }', 
  '{
    "formConfig": {
      "dialogWidth": "1400px",
      "labelWidth": "120px",
      "layout": "horizontal"
    },
    "fields": [
      {
        "field": "ft_id",
        "label": "富通ID",
        "component": "input",
        "span": 6,
        "required": true,
        "props": {"maxlength": 100, "clearable": true}
      },
      {
        "field": "code",
        "label": "客户编码",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 100, "clearable": true}
      },
      {
        "field": "type",
        "label": "客户类型",
        "component": "select",
        "span": 6,
        "dictionary": "customer_type",
        "props": {"placeholder": "请选择客户类型", "filterable": true, "clearable": true}
      },
      {
        "field": "short_name",
        "label": "客户简称",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 100, "clearable": true}
      },
      {
        "field": "name",
        "label": "客户名称",
        "component": "input",
        "span": 6,
        "required": true,
        "props": {"maxlength": 100, "clearable": true}
      },
      {
        "field": "region",
        "label": "国家",
        "component": "select",
        "span": 6,
        "dictionary": "nation",
        "props": {"placeholder": "请选择国家", "clearable": true, "filterable": true}
      },
      {
        "field": "main_product",
        "label": "主营产品",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 100, "clearable": true}
      },
      {
        "field": "grade",
        "label": "客户等级",
        "component": "select",
        "span": 6,
        "dictionary": "customer_grade",
        "props": {"placeholder": "请选择客户等级", "clearable": true}
      },
      {
        "field": "source",
        "label": "客户来源",
        "component": "select",
        "span": 6,
        "dictionary": "customer_source",
        "props": {"placeholder": "请选择客户来源", "clearable": true}
      },
      {
        "field": "business_type",
        "label": "业务类型",
        "component": "select",
        "span": 6,
        "dictionary": "business_type",
        "props": {"placeholder": "请选择业务类型", "clearable": true}
      },
      {
        "field": "address",
        "label": "联系地址",
        "component": "textarea",
        "span": 12,
        "props": {"maxlength": 500, "clearable": true, "rows": 3}
      },
      {
        "field": "scale",
        "label": "规模",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 255, "clearable": true}
      },
      {
        "field": "belong",
        "label": "是否公海",
        "component": "select",
        "span": 6,
        "options": [{"label": "公海", "value": 0}, {"label": "私海", "value": 1}],
        "props": {"placeholder": "请选择", "clearable": true}
      },
      {
        "field": "operator_name",
        "label": "业务员名称",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 40, "clearable": true}
      },
      {
        "field": "ext_id",
        "label": "第三方数据ID",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 100, "clearable": true}
      }
    ]
  }', 
  '{
    "tableName": "bd_ft_customer",
    "primaryKey": "id",
    "orderBy": [
      {"field": "create_time", "direction": "DESC"}
    ],
    "columns": [
      {"type": "selection", "width": 55, "fixed": "left", "resizable": false},
      {"type": "expand", "width": 100, "fixed": "left", "resizable": false, "label": "详情"},
      {"prop": "ft_id", "label": "富通ID", "width": 150, "fixed": "left", "align": "left", "visible": true, "resizable": true},
      {"prop": "name", "label": "客户名称", "width": 180, "fixed": "left", "align": "left", "visible": true, "resizable": true},
      {"prop": "short_name", "label": "客户简称", "width": 150, "align": "left", "visible": true, "resizable": true},
      {"prop": "type", "label": "客户类型", "width": 120, "align": "center", "visible": true, "renderType": "tag", "dictionary": "customer_type"},
      {"prop": "grade", "label": "客户等级", "width": 100, "align": "center", "visible": true, "renderType": "tag", "dictionary": "customer_grade"},
      {"prop": "region", "label": "国家", "width": 120, "align": "center", "visible": true, "dictionary": "nation"},
      {"prop": "main_product", "label": "主营产品", "width": 150, "align": "left", "visible": true, "showOverflowTooltip": true},
      {"prop": "operator_name", "label": "业务员", "width": 120, "align": "left", "visible": true},
      {"prop": "source", "label": "客户来源", "width": 120, "align": "center", "visible": true, "dictionary": "customer_source"},
      {"prop": "belong", "label": "归属", "width": 100, "align": "center", "visible": true, "renderType": "tag"},
      {"prop": "status", "label": "审批状态", "width": 120, "align": "center", "visible": true, "renderType": "tag", "dictionary": "approval_status"},
      {"prop": "deal_time", "label": "成交时间", "width": 160, "align": "center", "visible": true, "renderType": "datetime", "format": "YYYY-MM-DD HH:mm:ss"},
      {"prop": "create_time", "label": "创建时间", "width": 160, "align": "center", "visible": true, "renderType": "datetime", "format": "YYYY-MM-DD HH:mm:ss"}
    ],
    "pagination": {
      "defaultPageSize": 10,
      "pageSizeOptions": [10, 20, 50, 100]
    }
  }',
  '{
    "showSearch": true,
    "defaultExpand": true,
    "fields": [
      {
        "field": "name",
        "label": "客户名称",
        "component": "input",
        "props": {
          "placeholder": "客户名称",
          "clearable": true,
          "prefixIcon": "User",
          "style": {"width": "160px"}
        },
        "queryOperator": "like"
      },
      {
        "field": "short_name",
        "label": "客户简称",
        "component": "input",
        "props": {
          "placeholder": "客户简称",
          "clearable": true,
          "style": {"width": "140px"}
        },
        "queryOperator": "like"
      },
      {
        "field": "type",
        "label": "客户类型",
        "component": "select",
        "dictionary": "customer_type",
        "props": {
          "placeholder": "客户类型",
          "clearable": true,
          "filterable": true,
          "style": {"width": "130px"}
        },
        "defaultValue": "",
        "queryOperator": "eq"
      },
      {
        "field": "region",
        "label": "国家",
        "component": "select",
        "dictionary": "nation",
        "props": {
          "placeholder": "国家",
          "clearable": true,
          "filterable": true,
          "style": {"width": "130px"}
        },
        "defaultValue": "",
        "queryOperator": "eq"
      },
      {
        "field": "grade",
        "label": "客户等级",
        "component": "select",
        "dictionary": "customer_grade",
        "props": {
          "placeholder": "客户等级",
          "clearable": true,
          "style": {"width": "120px"}
        },
        "defaultValue": "",
        "queryOperator": "eq"
      },
      {
        "field": "operator_name",
        "label": "业务员",
        "component": "input",
        "props": {
          "placeholder": "业务员",
          "clearable": true,
          "style": {"width": "130px"}
        },
        "queryOperator": "like"
      },
      {
        "field": "belong",
        "label": "归属",
        "component": "select",
        "options": [{"label": "公海", "value": 0}, {"label": "私海", "value": 1}],
        "props": {
          "placeholder": "归属",
          "clearable": true,
          "style": {"width": "110px"}
        },
        "defaultValue": "",
        "queryOperator": "eq"
      },
      {
        "field": "status",
        "label": "审批状态",
        "component": "select",
        "dictionary": "approval_status",
        "props": {
          "placeholder": "审批状态",
          "clearable": true,
          "style": {"width": "120px"}
        },
        "defaultValue": "",
        "queryOperator": "eq"
      }
    ]
  }', 
  '{
    "toolbar": [
      {"key": "add", "label": "新增", "icon": "Plus", "permission": "erp:ftcustomer:add", "type": "primary", "position": "left", "handler": "handleAdd"},
      {"key": "edit", "label": "修改", "icon": "Edit", "permission": "erp:ftcustomer:edit", "type": "success", "position": "left", "disabled": "single", "handler": "handleUpdate"},
      {"key": "delete", "label": "删除", "icon": "Delete", "permission": "erp:ftcustomer:remove", "type": "danger", "position": "left", "disabled": "multiple", "handler": "handleDelete", "confirm": "是否确认删除选中的 {count} 条数据？"},
      {"key": "audit", "label": "审核", "icon": "CircleCheck", "permission": "erp:ftcustomer:audit", "type": "success", "position": "left", "disabled": "multiple", "handler": "handleAudit", "confirm": "是否确认审核选中的 {count} 条数据？"},
      {"key": "export", "label": "导出", "icon": "Download", "permission": "erp:ftcustomer:export", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleExport"},
      {"key": "columnSetting", "label": "列设置", "icon": "Setting", "type": "info", "position": "right", "handler": "openColumnSetting"}
    ],
    "row": []
  }', 
  '{
    "baseUrl": "/api/ftcustomer",
    "methods": {
      "list": {
        "url": "/list",
        "method": "GET",
        "description": "查询富通客户列表"
      },
      "get": {
        "url": "/{id}",
        "method": "GET",
        "description": "获取富通客户详情"
      },
      "add": {
        "url": "/add",
        "method": "POST",
        "description": "新增富通客户"
      },
      "update": {
        "url": "/update",
        "method": "PUT",
        "description": "修改富通客户"
      },
      "delete": {
        "url": "/delete",
        "method": "DELETE",
        "description": "删除富通客户"
      },
      "contacts": {
        "url": "/contacts/{ftId}",
        "method": "GET",
        "description": "获取富通客户联系人列表"
      },
      "audit": {
        "url": "/audit",
        "method": "POST",
        "description": "审核富通客户"
      }
    }
  }', 
  '{
    "builder": {
      "enabled": true
    },
    "dictionaries": {
      "customer_type": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/customer_type",
          "useGlobalCache": true,
          "cacheKey": "customer_type_dict",
          "cacheTTL": 86400000
        }
      },
      "customer_grade": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/customer_grade",
          "useGlobalCache": true,
          "cacheKey": "customer_grade_dict",
          "cacheTTL": 86400000
        }
      },
      "customer_source": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/customer_source",
          "useGlobalCache": true,
          "cacheKey": "customer_source_dict",
          "cacheTTL": 86400000
        }
      },
      "business_type": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/business_type",
          "useGlobalCache": true,
          "cacheKey": "business_type_dict",
          "cacheTTL": 86400000
        }
      },
      "nation": {
        "type": "remote",
        "config": {
          "searchApi": "/erp/engine/country/search?keyword={keyword}&limit=20",
          "minKeywordLength": 1,
          "debounce": 300,
          "labelField": "name",
          "valueField": "code"
        }
      },
      "approval_status": {
        "type": "static",
        "data": [
          {"label": "草稿", "value": "0"},
          {"label": "审批完成", "value": "3"}
        ]
      }
    },
    "globalCacheSettings": {
      "enabled": true,
      "defaultTTL": 300000
    }
  }', 
  '{
    "messages": {
      "selectOne": "请选择一条数据",
      "confirmDelete": "是否确认删除选中的 {count} 条数据？",
      "confirmAudit": "是否确认审核选中的 {count} 条数据？",
      "success": {"add": "新增成功", "edit": "修改成功", "delete": "删除成功", "audit": "审核成功"},
      "error": {"load": "加载数据失败", "save": "保存失败", "delete": "删除失败", "audit": "审核失败"}
    },
    "entityName": "富通客户",
    "entityNameSingular": "客户",
    "dialogTitle": {"add": "新增{entityName}", "edit": "修改{entityName}"},
    "drawerTitle": "{entityName}详情 - {code}"
  }',
  '{
    "detail": {
      "enabled": true,
      "displayType": "drawer",
      "title": "{entityName}详情 - {code}",
      "width": "70%",
      "direction": "rtl",
      "loadStrategy": "lazy",
      "defaultActiveTab": "contacts",
      "tabs": [
        {
          "name": "contacts",
          "label": "联系人信息",
          "icon": "User",
          "type": "table",
          "dataField": "rows",
          "tableName": "bd_ft_contact",
          "relationConfig": { 
            "masterTable": "bd_ft_customer",
            "masterField": "code",
            "detailTable": "bd_ft_contact",
            "detailField": "code",
            "operator": "eq"
          },
          "queryConfig": {
            "enabled": true,
            "defaultConditions": [
              {
                "field": "code",
                "operator": "eq",
                "value": "${code}",
                "description": "按客户编码查询联系人 (ft_customer.code = ft_contact.code)"
              }
            ],
            "defaultOrderBy": [{"field": "id", "direction": "ASC"}]
          },
          "table": {
            "border": true,
            "stripe": true,
            "maxHeight": "500",
            "showOverflowTooltip": true,
            "columns": [
              {"prop": "name", "label": "姓名", "width": 100, "align": "center", "sortable": true},
              {"prop": "sex", "label": "性别", "width": 60, "align": "center", "sortable": true},
              {"prop": "job", "label": "职务", "width": 120, "align": "left", "showOverflowTooltip": true},
              {"prop": "tele_phone", "label": "电话", "width": 120, "align": "center"},
              {"prop": "mobile", "label": "手机", "width": 120, "align": "center"},
              {"prop": "email", "label": "邮箱", "width": 180, "align": "left", "showOverflowTooltip": true},
              {"prop": "wechat", "label": "微信", "width": 120, "align": "center"},
              {"prop": "qq", "label": "QQ", "width": 100, "align": "center"},
              {"prop": "whats_app", "label": "WhatsApp", "width": 120, "align": "center"},
              {"prop": "linkedin", "label": "LinkedIn", "width": 150, "align": "left", "showOverflowTooltip": true},
              {"prop": "facebook_per_main", "label": "Facebook", "width": 150, "align": "left", "showOverflowTooltip": true},
              {"prop": "twitter_per_main", "label": "Twitter", "width": 150, "align": "left", "showOverflowTooltip": true},
              {"prop": "address", "label": "联系住址", "width": 200, "align": "left", "showOverflowTooltip": true},
              {"prop": "default_contact", "label": "默认联系人", "width": 100, "align": "center", "renderType": "tag"},
              {"prop": "status", "label": "状态", "width": 80, "align": "center", "renderType": "tag"},
              {"prop": "remark", "label": "备注", "width": 150, "align": "left", "showOverflowTooltip": true}
            ]
          }
        }
      ]
    }
  }',
  
  1,
  '1',
  '0',
  'admin',
  '富通客户配置（v1.0 - 主从表结构，支持联系人明细）'
);

-- ============================================
-- 第三步：验证导入结果
-- ============================================

SELECT '========================================' AS '';
SELECT ' 富通客户配置导入成功！' AS '';
SELECT '========================================' AS '';

SELECT 
  config_id,
  module_code,
  config_name,
  version,
  status,
  JSON_LENGTH(page_config) AS page_fields,
  JSON_LENGTH(form_config) AS form_fields,
  JSON_LENGTH(search_config) AS search_fields,
  JSON_LENGTH(action_config) AS actions,
  JSON_LENGTH(table_config) AS table_columns,
  JSON_LENGTH(dict_config) AS dictionaries,
  JSON_LENGTH(business_config) AS business_rules,
  JSON_LENGTH(detail_config) AS detail_tabs
FROM erp_page_config
WHERE module_code = 'ftcustomer';

SELECT '========================================' AS '';
SELECT '📊 配置统计信息：' AS '';
SELECT '========================================' AS '';
SELECT 
  CONCAT('页面配置字段数：', JSON_LENGTH(JSON_EXTRACT(form_config, '$.fields'))) AS form_fields_count,
  CONCAT('搜索字段数：', JSON_LENGTH(JSON_EXTRACT(search_config, '$.fields'))) AS search_fields_count,
  CONCAT('工具栏按钮数：', JSON_LENGTH(JSON_EXTRACT(action_config, '$.toolbar'))) AS toolbar_buttons_count,
  CONCAT('表格列数：', JSON_LENGTH(JSON_EXTRACT(table_config, '$.columns'))) AS table_columns_count,
  CONCAT('字典数量：', JSON_LENGTH(JSON_EXTRACT(dict_config, '$.dictionaries'))) AS dicts_count,
  CONCAT('详情页签数：', JSON_LENGTH(JSON_EXTRACT(detail_config, '$.detail.tabs'))) AS detail_tabs_count
FROM erp_page_config
WHERE module_code = 'ftcustomer';

SELECT '========================================' AS '';
SELECT ' 配置说明：' AS '';
SELECT '========================================' AS '';
SELECT '   主表：bd_ft_customer (富通客户)' AS info1;
SELECT '   明细表：bd_ft_contact (富通联系人)' AS info2;
SELECT '   关联关系：bd_ft_customer.ft_id = bd_ft_contact.ft_id' AS info3;
SELECT '   详情页：包含基本信息和联系人信息两个页签' AS info4;
SELECT '   字典配置：客户类型、客户等级、客户来源、业务类型、审批状态' AS info5;
SELECT '========================================' AS '';
SELECT '下一步操作：' AS '';
SELECT '  后续可添加：' AS note1;
SELECT '    - 审批流程配置（erp_approval_flow）' AS note2;
SELECT '    - 下推关系配置（erp_push_relation）' AS note3;
SELECT '  请根据实际需求执行相应的导入脚本' AS note4;
SELECT '========================================' AS '';
SELECT '完成！' AS '';
SELECT '========================================' AS '';
