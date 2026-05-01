-- ============================================
-- ERP 配置 JSON 强制拆分方案 - 销售订单信息模块导入 SQL
-- 版本：v1.0 (9字段拆分 + API配置版)
-- 日期：2026-04-14
-- 说明：导入销售订单信息页面配置数据（9 字段强制拆分 + API配置 + 统一字典 API）
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

-- 级联删除销售订单信息相关的所有配置
DELETE FROM erp_page_config_history WHERE module_code = 'saleorderinfo';
DELETE FROM erp_approval_history WHERE module_code = 'saleorderinfo';
DELETE FROM erp_approval_flow WHERE module_code = 'saleorderinfo';
DELETE FROM erp_push_relation WHERE source_module = 'saleorderinfo' OR target_module = 'saleorderinfo';
DELETE FROM erp_page_config WHERE module_code = 'saleorderinfo';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第二步：插入销售订单信息页面配置数据
-- ============================================

-- 销售订单信息页面配置（9 字段强制拆分，包含 api_config、search_config 和 action_config）
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
  'saleorderinfo',
  '销售订单信息管理',
  'PAGE', 
  '{
    "pageId": "saleorderinfo",
    "title": "{entityName}管理",
    "permission": "erp:saleorderinfo:query",
    "layout": "standard",
    "apiPrefix": "/erp/engine",
    "tableName": "t_sale_xsddxx",
    "billNoField": "f_bill_no"
  }', 
  '{
    "formConfig": {
      "dialogWidth": "1400px",
      "labelWidth": "120px",
      "layout": "horizontal"
    },
    "fields": [
      {
        "field": "f_bill_no",
        "label": "单据编号",
        "component": "input",
        "span": 6,
        "required": true,
        "props": {"maxlength": 100, "clearable": true}
      },
      {
        "field": "f_date",
        "label": "日期",
        "component": "date-picker",
        "span": 6,
        "required": true,
        "props": {"placeholder": "选择日期", "valueFormat": "YYYY-MM-DD"}
      },
      {
        "field": "f_kh",
        "label": "客户",
        "component": "select",
        "span": 6,
        "dictionary": "customers",
        "required": true,
        "props": {"placeholder": "请选择客户", "filterable": true, "clearable": true}
      },
      {
        "field": "f_bb",
        "label": "币别",
        "component": "select",
        "span": 6,
        "dictionary": "currency",
        "required": true,
        "props": {"placeholder": "请选择币别", "filterable": true, "clearable": true},
        "defaultValue": "1"
      },
      {
        "field": "f_xsbm",
        "label": "销售部门",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 40, "clearable": true}
      },
      {
        "field": "f_xsy",
        "label": "销售员",
        "component": "select",
        "span": 6,
        "dictionary": "salespersons",
        "props": {"placeholder": "请选择销售员", "filterable": true, "clearable": true}
      },
      {
        "field": "f_xsy1",
        "label": "销售员1",
        "component": "select",
        "span": 6,
        "dictionary": "salespersons",
        "props": {"placeholder": "请选择销售员1", "filterable": true, "clearable": true}
      },
      {
        "field": "f_sktj",
        "label": "收款条件",
        "component": "select",
        "span": 6,
        "dictionary": "payment_clause",
        "props": {"placeholder": "请选择收款条件", "filterable": true, "clearable": true}
      },
      {
        "field": "f_hl",
        "label": "汇率",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 6, "step": 0.000001, "controlsPosition": "right"},
        "defaultValue": 1
      },
      {
        "field": "f_document_status",
        "label": "数据状态",
        "component": "select",
        "span": 6,
        "dictionary": "f_document_status",
        "props": {"placeholder": "请选择数据状态", "clearable": true},
        "defaultValue": "A"
      },
      {
        "field": "f_shr",
        "label": "审核人",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 40, "clearable": true, "readonly": true}
      },
      {
        "field": "f_date_sh",
        "label": "审核日期",
        "component": "date-picker",
        "span": 6,
        "props": {"placeholder": "选择审核日期", "valueFormat": "YYYY-MM-DD HH:mm:ss", "readonly": true}
      },
      {
        "field": "f_identification",
        "label": "后置单据标识",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 100, "clearable": true}
      }
    ]
  }', 
  '{
    "tableName": "t_sale_xsddxx",
    "primaryKey": "id",
    "orderBy": [
      {"field": "f_date", "direction": "DESC"}
    ],
    "columns": [
      {"type": "selection", "width": 55, "fixed": "left", "resizable": false},
      {"type": "expand", "width": 100, "fixed": "left", "resizable": false, "label": "详情"},
      {"prop": "f_bill_no", "label": "单据编号", "width": 150, "fixed": "left", "align": "left", "visible": true, "resizable": true, "renderType": "text"},
      {"prop": "f_kh", "label": "客户", "width": 150, "fixed": "left", "align": "left", "visible": true, "resizable": true, "renderType": "text", "dictionary": "customers"},
      {"prop": "f_document_status", "label": "数据状态", "width": 120, "align": "center", "visible": true, "renderType": "tag", "dictionary": "f_document_status"},
      {"prop": "f_date", "label": "日期", "width": 120, "align": "center", "visible": true, "renderType": "date", "format": "YYYY-MM-DD"},
      {"prop": "f_bb", "label": "币别", "width": 100, "align": "center", "visible": true, "renderType": "text", "dictionary": "currency"},
      {"prop": "f_xsbm", "label": "销售部门", "width": 120, "align": "left", "visible": true, "renderType": "text"},
      {"prop": "f_xsy", "label": "销售员", "width": 120, "align": "left", "visible": true, "renderType": "text", "dictionary": "salespersons"},
      {"prop": "f_xsy1", "label": "销售员1", "width": 120, "align": "left", "visible": true, "renderType": "text", "dictionary": "salespersons"},
      {"prop": "f_sktj", "label": "收款条件", "width": 140, "align": "center", "visible": true, "renderType": "text", "dictionary": "payment_clause"},
      {"prop": "f_hl", "label": "汇率", "width": 100, "align": "right", "visible": true, "renderType": "number", "precision": 6},
      {"prop": "f_shr", "label": "审核人", "width": 100, "align": "left", "visible": true, "renderType": "text"},
      {"prop": "f_date_sh", "label": "审核日期", "width": 160, "align": "center", "visible": true, "renderType": "datetime", "format": "YYYY-MM-DD HH:mm:ss"},
      {"prop": "create_time", "label": "创建时间", "width": 160, "align": "center", "visible": true, "renderType": "datetime", "format": "YYYY-MM-DD HH:mm:ss"},
      {"prop": "create_by", "label": "创建人", "width": 100, "align": "left", "visible": true, "renderType": "text", "dictionary": "users"}
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
        "field": "f_date",
        "label": "订单日期",
        "component": "daterange",
        "props": {
          "startPlaceholder": "开始日期",
          "endPlaceholder": "结束日期",
          "valueFormat": "YYYY-MM-DD",
          "style": {"width": "220px"}
        },
        "defaultValue": ["2010-01-01", "today"],
        "queryOperator": "between"
      },
      {
        "field": "f_bill_no",
        "label": "单据编号",
        "component": "input",
        "props": {
          "placeholder": "单据编号",
          "clearable": true,
          "prefixIcon": "Search",
          "style": {"width": "160px"}
        },
        "queryOperator": "right_like"
      },
      {
        "field": "f_kh",
        "label": "客户",
        "component": "select",
        "dictionary": "customers",
        "props": {
          "placeholder": "客户",
          "clearable": true,
          "filterable": true,
          "style": {"width": "130px"}
        },
        "queryOperator": "eq"
      },
      {
        "field": "f_xsy",
        "label": "销售员",
        "component": "select",
        "dictionary": "salespersons",
        "props": {
          "placeholder": "销售员",
          "clearable": true,
          "filterable": true,
          "style": {"width": "120px"}
        },
        "defaultValue": "",
        "queryOperator": "eq"
      },
      {
        "field": "f_document_status",
        "label": "数据状态",
        "component": "select",
        "dictionary": "f_document_status",
        "props": {
          "placeholder": "数据状态",
          "clearable": true,
          "filterable": true,
          "style": {"width": "120px"}
        },
        "defaultValue": "",
        "queryOperator": "eq"
      }
    ]
  }', 
  '{
    "toolbar": [
      {"key": "add", "label": "新增", "icon": "Plus", "permission": "erp:saleorderinfo:add", "type": "primary", "position": "left", "handler": "handleAdd"},
      {"key": "edit", "label": "修改", "icon": "Edit", "permission": "erp:saleorderinfo:edit", "type": "success", "position": "left", "disabled": "single", "handler": "handleUpdate"},
      {"key": "delete", "label": "删除", "icon": "Delete", "permission": "erp:saleorderinfo:remove", "type": "danger", "position": "left", "disabled": "multiple", "handler": "handleDelete", "confirm": "是否确认删除选中的 {count} 条数据？"},
      {"key": "audit", "label": "审核", "icon": "CircleCheck", "permission": "erp:saleorderinfo:audit", "type": "success", "position": "left", "disabled": "multiple", "handler": "handleAudit", "confirm": "是否确认审核选中的 {count} 条数据？"},
      {"key": "unAudit", "label": "反审核", "icon": "Close", "permission": "erp:saleorderinfo:unAudit", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleUnAudit", "confirm": "是否确认反审核选中的 {count} 条数据？"},
      {"key": "push", "label": "下推", "icon": "Download", "permission": "erp:saleorderinfo:push", "type": "info", "position": "left", "disabled": "single", "handler": "handleOpenPushDialog"},
      {"key": "export", "label": "导出", "icon": "Download", "permission": "erp:saleorderinfo:export", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleExport"},
      {"key": "columnSetting", "label": "列设置", "icon": "Setting", "type": "info", "position": "right", "handler": "openColumnSetting"}
    ],
    "row": []
  }', 
  '{
    "baseUrl": "/api/saleorderinfo",
    "methods": {
      "list": {
        "url": "/list",
        "method": "GET",
        "description": "查询销售订单信息列表"
      },
      "get": {
        "url": "/{id}",
        "method": "GET",
        "description": "获取销售订单信息详情"
      },
      "add": {
        "url": "/add",
        "method": "POST",
        "description": "新增销售订单信息"
      },
      "update": {
        "url": "/update",
        "method": "PUT",
        "description": "修改销售订单信息"
      },
      "delete": {
        "url": "/delete",
        "method": "DELETE",
        "description": "删除销售订单信息"
      },
      "entry": {
        "url": "/entry/{billNo}",
        "method": "GET",
        "description": "获取销售订单信息明细"
      },
      "audit": {
        "url": "/audit",
        "method": "POST",
        "description": "审核销售订单信息"
      },
      "unAudit": {
        "url": "/unAudit",
        "method": "POST",
        "description": "反审核销售订单信息"
      }
    }
  }', 
  '{
    "builder": {
      "enabled": true
    },
    "dictionaries": {
      "salespersons": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/salespersons",
          "useGlobalCache": true,
          "cacheKey": "salespersons_dict",
          "cacheTTL": 86400000
        }
      },
      "customers": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/customers",
          "useGlobalCache": true,
          "cacheKey": "customers_dict",
          "cacheTTL": 86400000
        }
      },
      "currency": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/currency",
          "useGlobalCache": true,
          "cacheKey": "currency_dict",
          "cacheTTL": 86400000
        }
      },
      "payment_clause": {
        "type": "api",
        "config": {
          "api": "/erp/engine/bizdict/payment_clause",
          "useGlobalCache": true,
          "cacheKey": "payment_clause_dict",
          "cacheTTL": 86400000
        }
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
      "confirmUnAudit": "是否确认反审核选中的 {count} 条数据？",
      "success": {"add": "新增成功", "edit": "修改成功", "delete": "删除成功", "audit": "审核成功", "unAudit": "反审核成功"},
      "error": {"load": "加载数据失败", "save": "保存失败", "delete": "删除失败", "audit": "审核失败", "unAudit": "反审核失败"}
    },
    "entityName": "销售订单信息",
    "entityNameSingular": "订单信息",
    "dialogTitle": {"add": "新增{entityName}", "edit": "修改{entityName}"},
    "drawerTitle": "{entityName}详情 - {billNo}"
  }',
  '{
    "detail": {
      "enabled": true,
      "displayType": "drawer",
      "title": "{entityName}详情 - {billNo}",
      "width": "70%",
      "direction": "rtl",
      "loadStrategy": "lazy",
      "tabs": [
        {
          "name": "entry",
          "label": "销售订单信息明细",
          "icon": "Document",
          "type": "table",
          "dataField": "entryList",
          "tableName": "t_sale_xsddxx_entry",
          "relationConfig": { 
            "masterTable": "t_sale_xsddxx",
            "masterField": "id",
            "detailTable": "t_sale_xsddxx_entry",
            "detailField": "f_entry_id",
            "operator": "eq"
          },
          "queryConfig": {
            "enabled": true,
            "defaultConditions": [
              {
                "field": "f_entry_id",
                "operator": "eq",
                "value": "${id}",
                "description": "按主表ID查询明细 (t_sale_xsddxx.id = t_sale_xsddxx_entry.f_entry_id)"
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
              {"prop": "f_cpdm", "label": "产品代码", "width": 120, "align": "center", "sortable": true},
              {"prop": "f_ora_base_property", "label": "产品名称", "width": 180, "align": "left", "showOverflowTooltip": true, "sortable": true},
              {"prop": "f_ora_base_property1", "label": "规格型号", "width": 180, "align": "left", "showOverflowTooltip": true, "sortable": true},
              {"prop": "f_szsl", "label": "销售数量", "width": 100, "align": "right", "renderType": "number", "sortable": true, "precision": 2},
              {"prop": "F_price", "label": "单价", "width": 100, "align": "right", "renderType": "currency", "precision": 2, "sortable": true},
              {"prop": "F_taxprice", "label": "含税单价", "width": 100, "align": "right", "renderType": "currency", "precision": 2, "sortable": true},
              {"prop": "f_amount", "label": "金额", "width": 120, "align": "right", "renderType": "currency", "precision": 2, "sortable": true},
              {"prop": "f_all_amount", "label": "价税合计", "width": 120, "align": "right", "renderType": "currency", "precision": 2, "sortable": true},
              {"prop": "f_ora_unit_id", "label": "计量单位", "width": 100, "align": "center", "sortable": true},
              {"prop": "f_date_jh", "label": "交货日期", "width": 160, "align": "center", "renderType": "datetime", "format": "YYYY-MM-DD HH:mm:ss", "sortable": true},
              {"prop": "f_gls", "label": "关联数", "width": 100, "align": "right", "renderType": "number", "precision": 2, "sortable": true},
              {"prop": "f_bill_status", "label": "关联状态", "width": 100, "align": "center", "renderType": "tag"},
              {"prop": "f_khhh", "label": "客户货号", "width": 120, "align": "center"},
              {"prop": "f_ora_source_bill_type", "label": "源单类型", "width": 120, "align": "center"},
              {"prop": "f_ora_source_bill_no", "label": "源单编号", "width": 140, "align": "left", "showOverflowTooltip": true}
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
  '销售订单信息配置（v1.0 - 基于 t_sale_xsddxx 表结构）'
);

-- ============================================
-- 第三步：验证导入结果
-- ============================================

SELECT '========================================' AS '';
SELECT ' 销售订单信息配置导入成功！' AS '';
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
WHERE module_code = 'saleorderinfo';

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
WHERE module_code = 'saleorderinfo';

SELECT '========================================' AS '';
SELECT ' 配置说明：' AS '';
SELECT '========================================' AS '';
SELECT '   主表：t_sale_xsddxx（销售订单信息主表）' AS info1;
SELECT '   明细表：t_sale_xsddxx_entry（销售订单信息明细表）' AS info2;
SELECT '   关联字段：f_bill_no（单据编号）' AS info3;
SELECT '   字典配置：customers, salespersons, currency, payment_clause, f_document_status' AS info4;
SELECT '   权限前缀：erp:saleorderinfo:*' AS info5;
SELECT '========================================' AS '';
SELECT '下一步操作：' AS '';
SELECT '  后续将添加：' AS note1;
SELECT '    - 审批流程配置（erp_approval_flow）' AS note2;
SELECT '    - 下推关系配置（erp_push_relation）' AS note3;
SELECT '  请继续执行相应的导入脚本' AS note4;
SELECT '========================================' AS '';
SELECT '完成！' AS '';
SELECT '========================================' AS '';
