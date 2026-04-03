-- ============================================
-- ERP 配置 JSON 强制拆分方案 - 付款申请单模块导入 SQL（字典重构版）
-- 版本：v4.0 (9字段拆分 + API配置版)
-- 日期：2026-03-31
-- 说明：导入付款申请单页面配置数据（9 字段强制拆分 + API配置 + 统一字典 API）
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

-- 级联删除付款申请单相关的所有配置
DELETE FROM erp_page_config_history WHERE module_code = 'paymentapply';
DELETE FROM erp_approval_history WHERE module_code = 'paymentapply';
DELETE FROM erp_approval_flow WHERE module_code = 'paymentapply';
DELETE FROM erp_push_relation WHERE source_module = 'paymentapply' OR target_module = 'paymentapply';
DELETE FROM erp_page_config WHERE module_code = 'paymentapply';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第二步：插入付款申请单页面配置数据
-- ============================================

-- 付款申请单页面配置（9 字段强制拆分，包含 api_config、search_config 和 action_config）
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
  'paymentapply',
  '付款申请单管理',
  'PAGE', 
  '{
    "pageId": "paymentapply",
    "title": "{entityName}管理",
    "permission": "k3:paymentapply:query",
    "layout": "standard",
    "apiPrefix": "/erp/engine",
    "tableName": "f_rectunit_type",
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
        "field": "f_date",
        "label": "申请日期",
        "component": "date",
        "span": 6,
        "required": true,
        "rules": [{"required": true, "message": "申请日期不能为空", "trigger": "change"}],
        "props": {"placeholder": "选择日期", "valueFormat": "YYYY-MM-DD"}
      },
      {
        "field": "f_rectunit",
        "label": "收款单位",
        "component": "select",
        "span": 6,
        "dictionary": "suppliers",
        "required": true,
        "props": {"placeholder": "请选择收款单位", "filterable": true, "clearable": true}
      },
      {
        "field": "f_settlecur",
        "label": "结算币别",
        "component": "select",
        "span": 6,
        "dictionary": "currency",
        "required": true,
        "props": {"placeholder": "请选择结算币别", "clearable": true, "filterable": true},
        "defaultValue": "人民币"
      },
      {
        "field": "f_applyamountfor_h",
        "label": "申请付款金额",
        "component": "input-number",
        "span": 6,
        "required": true,
        "rules": [{"required": true, "message": "申请金额不能为空", "trigger": "blur"}],
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"}
      },
      {
        "field": "f_document_status",
        "label": "单据状态",
        "component": "select",
        "span": 6,
        "dictionary": "f_document_status",
        "props": {"placeholder": "请选择单据状态", "clearable": true, "filterable": true},
        "defaultValue": "0"
      },
      {
        "field": "f_btbz",
        "label": "备注",
        "component": "input",
        "span": 18,
        "props": {"type": "textarea", "rows": 3, "maxlength": 500, "placeholder": "请输入备注"}
      }
    ]
  }', 
  '{
    "tableName": "f_rectunit_type",
    "primaryKey": "id",
    "orderBy": [
      {"field": "f_date", "direction": "DESC"}
    ],
    "columns": [
      {"type": "selection", "width": 55, "fixed": "left", "resizable": false},
      {"type": "expand", "width": 100, "fixed": "left", "resizable": false, "label": "详情"},
      {"prop": "f_bill_no", "label": "单据编号", "width": 150, "fixed": "left", "align": "left", "visible": true, "resizable": true, "renderType": "text"},
      {"prop": "f_date", "label": "申请日期", "width": 140, "align": "center", "visible": true, "renderType": "date", "format": "YYYY-MM-DD"},
      {"prop": "f_rectunit", "label": "收款单位", "width": 180, "align": "left", "visible": true, "renderType": "text", "dictionary": "suppliers"},
      {"prop": "f_applyamountfor_h", "label": "申请付款金额", "width": 140, "align": "right", "visible": true, "renderType": "currency", "precision": 2},
      {"prop": "f_settlecur", "label": "结算币别", "width": 120, "align": "center", "visible": true, "renderType": "text", "dictionary": "currency"},
      {"prop": "f_document_status", "label": "单据状态", "width": 100, "align": "center", "visible": true, "renderType": "tag", "dictionary": "f_document_status"},
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
        "label": "申请日期",
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
        "field": "f_rectunit",
        "label": "收款单位",
        "component": "select",
        "dictionary": "suppliers",
        "props": {
          "placeholder": "收款单位",
          "clearable": true,
          "filterable": true,
          "style": {"width": "150px"}
        },
        "defaultValue": "",
        "queryOperator": "eq"
      },
      {
        "field": "f_settlecur",
        "label": "结算币别",
        "component": "select",
        "dictionary": "currency",
        "props": {
          "placeholder": "结算币别",
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
      {"key": "add", "label": "新增", "icon": "Plus", "permission": "k3:paymentapply:add", "type": "primary", "position": "left", "handler": "handleAdd"},
      {"key": "edit", "label": "修改", "icon": "Edit", "permission": "k3:paymentapply:edit", "type": "success", "position": "left", "disabled": "single", "handler": "handleUpdate"},
      {"key": "delete", "label": "删除", "icon": "Delete", "permission": "k3:paymentapply:remove", "type": "danger", "position": "left", "disabled": "multiple", "handler": "handleDelete", "confirm": "是否确认删除选中的 {count} 条数据？"},
      {"key": "audit", "label": "审核", "icon": "CircleCheck", "permission": "k3:paymentapply:audit", "type": "success", "position": "left", "disabled": "multiple", "handler": "handleAudit", "confirm": "是否确认审核选中的 {count} 条数据？"},
      {"key": "unAudit", "label": "反审核", "icon": "Close", "permission": "k3:paymentapply:unAudit", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleUnAudit", "confirm": "是否确认反审核选中的 {count} 条数据？"},
      {"key": "export", "label": "导出", "icon": "Download", "permission": "k3:paymentapply:export", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleExport"},
      {"key": "columnSetting", "label": "列设置", "icon": "Setting", "type": "info", "position": "right", "handler": "openColumnSetting"}
    ],
    "row": []
  }', 
  '{
    "baseUrl": "/api/paymentapply",
    "methods": {
      "list": {
        "url": "/list",
        "method": "GET",
        "description": "查询付款申请单列表"
      },
      "get": {
        "url": "/{id}",
        "method": "GET",
        "description": "获取付款申请单详情"
      },
      "add": {
        "url": "/add",
        "method": "POST",
        "description": "新增付款申请单"
      },
      "update": {
        "url": "/update",
        "method": "PUT",
        "description": "修改付款申请单"
      },
      "delete": {
        "url": "/delete",
        "method": "DELETE",
        "description": "删除付款申请单"
      },
      "entry": {
        "url": "/entry/{billNo}",
        "method": "GET",
        "description": "获取付款申请单明细"
      },
      "audit": {
        "url": "/audit",
        "method": "POST",
        "description": "审核付款申请单"
      },
      "unAudit": {
        "url": "/unAudit",
        "method": "POST",
        "description": "反审核付款申请单"
      }
    }
  }', 
  '{
    "builder": {
      "enabled": true
    },
    "dictionaries": {
      "suppliers": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/suppliers",
          "useGlobalCache": true,
          "cacheKey": "suppliers_dict",
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
    "entityName": "付款申请单",
    "entityNameSingular": "付款申请单",
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
          "label": "付款申请明细",
          "icon": "Document",
          "type": "table",
          "dataField": "entryList",
          "tableName": "f_rectunit_detail",
          "relationConfig": {
            "enabled": true,
            "masterTable": "f_rectunit_type",
            "masterField": "id",
            "detailTable": "f_rectunit_detail",
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
                "description": "按主键 ID 查询明细"
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
              {"prop": "f_settletype_id", "label": "结算方式", "width": 120, "align": "left", "showOverflowTooltip": true},
              {"prop": "f_applyamount_for", "label": "申请付款金额", "width": 140, "align": "right", "renderType": "currency", "precision": 2, "sortable": true},
              {"prop": "f_eachbank_name", "label": "对方开户行", "width": 150, "align": "left", "showOverflowTooltip": true},
              {"prop": "f_eachbankaccount", "label": "对方银行账号", "width": 150, "align": "left", "showOverflowTooltip": true},
              {"prop": "f_eachccount_name", "label": "对方账户名称", "width": 150, "align": "left", "showOverflowTooltip": true},
              {"prop": "f_expectpay_date", "label": "期望付款日期", "width": 120, "align": "center", "renderType": "date", "format": "YYYY-MM-DD"},
              {"prop": "f_description", "label": "备注", "width": 200, "align": "left", "showOverflowTooltip": true}
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
  '付款申请单页面配置 - 初始版本'
);

-- ============================================
-- 配置导入完成
-- ============================================
SELECT '付款申请单页面配置导入完成' AS message;
