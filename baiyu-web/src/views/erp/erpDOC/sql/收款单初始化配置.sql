-- ============================================
-- ERP 配置 JSON 强制拆分方案 - 收款单模块导入 SQL（字典重构版）
-- 版本：v4.0 (9字段拆分 + API配置版)
-- 日期：2026-03-31
-- 说明：导入收款单页面配置数据（9 字段强制拆分 + API配置 + 统一字典 API）
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

-- 级联删除收款单相关的所有配置
DELETE FROM erp_page_config_history WHERE module_code = 'receivebill';
DELETE FROM erp_approval_history WHERE module_code = 'receivebill';
DELETE FROM erp_approval_flow WHERE module_code = 'receivebill';
DELETE FROM erp_push_relation WHERE source_module = 'receivebill' OR target_module = 'receivebill';
DELETE FROM erp_page_config WHERE module_code = 'receivebill';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第二步：插入收款单页面配置数据
-- ============================================

-- 收款单页面配置（9 字段强制拆分，包含 api_config、search_config 和 action_config）
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
  'receivebill',
  '收款单管理',
  'PAGE', 
  '{
    "pageId": "receivebill",
    "title": "{entityName}管理",
    "permission": "erp:receivebill:query",
    "layout": "standard",
    "apiPrefix": "/erp/engine",
    "tableName": "f_receivebill",
    "billNoField": "f_bill_no"
  }', 
  '{
    "formConfig": {
      "dialogWidth": "1200px",
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
        "rules": [{"required": true, "message": "单据编号不能为空", "trigger": "blur"}],
        "props": {"maxlength": 100, "clearable": true}
      },
      {
        "field": "fdate",
        "label": "业务日期",
        "component": "date",
        "span": 6,
        "required": true,
        "rules": [{"required": true, "message": "业务日期不能为空", "trigger": "change"}],
        "props": {"placeholder": "选择日期", "valueFormat": "YYYY-MM-DD"}
      },
      {
        "field": "f_contactunit",
        "label": "往来单位",
        "component": "select",
        "span": 6,
        "dictionary": "customers",
        "required": true,
        "props": {"placeholder": "请选择客户", "filterable": true, "clearable": true}
      },
      {
        "field": "f_bill_type_id",
        "label": "单据类型",
        "component": "select",
        "span": 6,
        "dictionary": "document_type",
        "required": true,
        "props": {"placeholder": "请选择单据类型", "clearable": true, "filterable": true}
      },
      {
        "field": "f_currency_id",
        "label": "币别",
        "component": "select",
        "span": 6,
        "dictionary": "currency",
        "required": true,
        "props": {"placeholder": "请选择结算币别", "clearable": true, "filterable": true},
        "defaultValue": "人民币"
      },
      {
        "field": "f_recamountfor",
        "label": "收款金额",
        "component": "input-number",
        "span": 6,
        "required": true,
        "rules": [{"required": true, "message": "收款金额不能为空", "trigger": "blur"}],
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"}
      },
      {
        "field": "f_document_status",
        "label": "单据状态",
        "component": "select",
        "span": 6,
        "dictionary": "f_document_status",
        "props": {"placeholder": "请选择单据状态", "clearable": true},
        "defaultValue": "0"
      },
      {
        "field": "f_remark",
        "label": "备注",
        "component": "input",
        "span": 18,
        "props": {"type": "textarea", "rows": 3, "maxlength": 500, "placeholder": "请输入备注"}
      }
    ]
  }', 
  '{
    "tableName": "f_receivebill",
    "primaryKey": "id",
    "orderBy": [
      {"field": "fdate", "direction": "DESC"}
    ],
    "columns": [
      {"type": "selection", "width": 55, "fixed": "left", "resizable": false},
      {"type": "expand", "width": 100, "fixed": "left", "resizable": false, "label": "详情"},
      {"prop": "f_bill_no", "label": "单据编号", "width": 150, "fixed": "left", "align": "left", "visible": true, "resizable": true, "renderType": "text"},
      {"prop": "fdate", "label": "业务日期", "width": 140, "align": "center", "visible": true, "renderType": "date", "format": "YYYY-MM-DD"},
      {
        "prop": "f_contactunit",
        "label": "往来单位",
        "width": 120,
        "align": "left",
        "visible": true,
        "resizable": true,
        "renderType": "text",
        "dictionary": "customers"
      },
      {
        "prop": "f_bill_type_id",
        "label": "单据类型",
        "width": 120,
        "align": "center",
        "visible": true,
        "resizable": true,
        "renderType": "tag",
        "dictionary": "document_type"
      },
      {"prop": "f_recamountfor", "label": "收款金额", "width": 140, "align": "right", "visible": true, "renderType": "currency", "precision": 2},
      {"prop": "f_currency_id", "label": "币别", "width": 120, "align": "center", "visible": true, "renderType": "text", "dictionary": "currency"},
      {
        "prop": "f_document_status",
        "label": "单据状态",
        "width": 100,
        "align": "center",
        "visible": true,
        "resizable": true,
        "renderType": "tag",
        "dictionary": "f_document_status"
      },
      {"prop": "create_time", "label": "创建时间", "width": 160, "align": "center", "visible": true, "renderType": "datetime", "format": "YYYY-MM-DD HH:mm:ss"},
      {
        "prop": "create_by",
        "label": "创建人",
        "width": 100,
        "align": "left",
        "visible": true,
        "resizable": true,
        "renderType": "text",
        "dictionary": "users"
      }
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
        "field": "fdate",
        "label": "业务日期",
        "component": "daterange",
        "props": {
          "startPlaceholder": "开始日期",
          "endPlaceholder": "结束日期",
          "valueFormat": "YYYY-MM-DD",
          "style": {"width": "240px"}
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
        "field": "f_contactunit",
        "label": "往来单位",
        "component": "select",
        "dictionary": "customers",
        "props": {
          "placeholder": "往来单位",
          "clearable": true,
          "filterable": true,
          "style": {"width": "130px"}
        },
        "defaultValue": "",
        "queryOperator": "eq"
      },
      {
        "field": "f_bill_type_id",
        "label": "单据类型",
        "component": "select",
        "dictionary": "document_type",
        "props": {
          "placeholder": "单据类型",
          "clearable": true,
          "filterable": true,
          "style": {"width": "120px"}
        },
        "defaultValue": "",
        "queryOperator": "eq"
      },
      {
        "field": "f_document_status",
        "label": "单据状态",
        "component": "select",
        "dictionary": "f_document_status",
        "props": {
          "placeholder": "单据状态",
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
      {"key": "add", "label": "新增", "icon": "Plus", "permission": "erp:receivebill:add", "type": "primary", "position": "left", "handler": "handleAdd"},
      {"key": "edit", "label": "修改", "icon": "Edit", "permission": "erp:receivebill:edit", "type": "success", "position": "left", "disabled": "single", "handler": "handleUpdate"},
      {"key": "delete", "label": "删除", "icon": "Delete", "permission": "erp:receivebill:remove", "type": "danger", "position": "left", "disabled": "multiple", "handler": "handleDelete", "confirm": "是否确认删除选中的 {count} 条数据？"},
      {"key": "audit", "label": "审核", "icon": "CircleCheck", "permission": "erp:receivebill:audit", "type": "success", "position": "left", "disabled": "multiple", "handler": "handleAudit", "confirm": "是否确认审核选中的 {count} 条数据？"},
      {"key": "unAudit", "label": "反审核", "icon": "Close", "permission": "erp:receivebill:unAudit", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleUnAudit", "confirm": "是否确认反审核选中的 {count} 条数据？"},
      {"key": "export", "label": "导出", "icon": "Download", "permission": "erp:receivebill:export", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleExport"},
      {"key": "columnSetting", "label": "列设置", "icon": "Setting", "type": "info", "position": "right", "handler": "openColumnSetting"}
    ],
    "row": []
  }', 
  '{
    "baseUrl": "/api/receivebill",
    "methods": {
      "list": {
        "url": "/list",
        "method": "GET",
        "description": "查询收款单列表"
      },
      "get": {
        "url": "/{id}",
        "method": "GET",
        "description": "获取收款单详情"
      },
      "add": {
        "url": "/add",
        "method": "POST",
        "description": "新增收款单"
      },
      "update": {
        "url": "/update",
        "method": "PUT",
        "description": "修改收款单"
      },
      "delete": {
        "url": "/delete",
        "method": "DELETE",
        "description": "删除收款单"
      },
      "entry": {
        "url": "/entry/{billNo}",
        "method": "GET",
        "description": "获取收款单明细"
      },
      "audit": {
        "url": "/audit",
        "method": "POST",
        "description": "审核收款单"
      },
      "unAudit": {
        "url": "/unAudit",
        "method": "POST",
        "description": "反审核收款单"
      }
    }
  }', 
  '{
    "builder": {
      "enabled": true
    },
    "dictionaries": {
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
      "document_type": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/document_type",
          "useGlobalCache": true,
          "cacheKey": "document_type_dict",
          "cacheTTL": 86400000
        }
      },
      "f_document_status": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/f_document_status",
          "useGlobalCache": true,
          "cacheKey": "f_document_status_dict",
          "cacheTTL": 86400000
        }
      },
      "users": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/users",
          "useGlobalCache": true,
          "cacheKey": "users_dict",
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
    "entityName": "收款单",
    "entityNameSingular": "收款单",
    "dialogTitle": {"add": "新增{entityName}", "edit": "修改{entityName}"},
    "drawerTitle": "{entityName}详情 - {f_bill_no}"
  }',
  '{
    "detail": {
      "enabled": true,
      "displayType": "drawer",
      "title": "{entityName}详情 - {f_bill_no}",
      "width": "60%",
      "direction": "rtl",
      "loadStrategy": "lazy",
      "tabs": [
        {
          "name": "entry",
          "label": "收款单明细",
          "icon": "Document",
          "type": "table",
          "dataField": "entryList",
          "tableName": "f_receivebill_entry",
          "relationConfig": {
            "enabled": true,
            "masterTable": "f_receivebill",
            "masterField": "id",
            "detailTable": "f_receivebill_entry",
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
                "description": "按收款单 ID 查询明细 (f_receivebill.id = f_receivebill_entry.f_entry_id)"
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
              {"prop": "id", "label": "明细 ID", "width": 100, "align": "center", "sortable": true},
              {"prop": "f_settle_type_id", "label": "结算方式", "width": 120, "align": "center", "showOverflowTooltip": true},
              {"prop": "f_rectotalamountfor", "label": "应收金额", "width": 120, "align": "right", "renderType": "currency", "precision": 2, "sortable": true},
              {"prop": "f_realrecamountfor_d", "label": "实收金额", "width": 120, "align": "right", "renderType": "currency", "precision": 2, "sortable": true},
              {"prop": "f_oppositebank_name", "label": "对方开户行", "width": 150, "align": "left", "showOverflowTooltip": true},
              {"prop": "f_oppositebankaccount", "label": "对方银行账号", "width": 150, "align": "left", "showOverflowTooltip": true},
              {"prop": "f_oppositeccount_name", "label": "对方账户名称", "width": 150, "align": "left", "showOverflowTooltip": true},
              {"prop": "f_comment", "label": "备注", "width": 200, "align": "left", "showOverflowTooltip": true}
            ]
          }
        }
      ]
    }
  }',
  1,
  '1',
  1,
  'admin',
  '收款单页面配置 - 初始版本'
);

-- ============================================
-- 配置导入完成
-- ============================================
SELECT '收款单页面配置导入完成' AS message;
