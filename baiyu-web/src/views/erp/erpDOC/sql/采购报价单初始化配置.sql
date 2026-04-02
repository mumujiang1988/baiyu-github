-- ============================================
-- ERP 配置 JSON 强制拆分方案 - 采购报价单模块导入 SQL（字典重构版）
-- 版本：v4.0 (9 字段拆分 + API 配置版)
-- 日期：2026-04-01
-- 说明：导入采购报价单页面配置数据（9 字段强制拆分 + API 配置 + 统一字典 API）
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

-- 级联删除采购报价单相关的所有配置
DELETE FROM erp_page_config_history WHERE module_code = 'purchasequotation';
DELETE FROM erp_approval_history WHERE module_code = 'purchasequotation';
DELETE FROM erp_approval_flow WHERE module_code = 'purchasequotation';
DELETE FROM erp_push_relation WHERE source_module = 'purchasequotation' OR target_module = 'purchasequotation';
DELETE FROM erp_page_config WHERE module_code = 'purchasequotation';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第二步：插入采购报价单页面配置数据
-- ============================================

-- 采购报价单页面配置（9 字段强制拆分，包含 api_config、search_config 和 action_config）
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
  'purchasequotation',
  '采购报价单管理',
  'PAGE', 
  '{
    "pageId": "purchasequotation",
    "title": "{entityName}管理",
    "permission": "k3:purchasequotation:query",
    "layout": "standard",
    "apiPrefix": "/erp/engine",
    "tableName": "purchase_quotation",
    "billNoField": "fbillno"
  }', 
  '{
    "formConfig": {
      "dialogWidth": "1200px",
      "labelWidth": "120px",
      "layout": "horizontal"
    },
    "fields": [
      {
        "field": "fbillno",
        "label": "单据编号",
        "component": "input",
        "span": 6,
        "required": true,
        "rules": [{"required": true, "message": "单据编号不能为空", "trigger": "blur"}],
        "props": {"maxlength": 100, "clearable": true}
      },
      {
        "field": "fdate",
        "label": "报价日期",
        "component": "date",
        "span": 6,
        "required": true,
        "rules": [{"required": true, "message": "报价日期不能为空", "trigger": "change"}],
        "props": {"placeholder": "选择日期", "valueFormat": "YYYY-MM-DD"}
      },
      {
        "field": "f_khjc",
        "label": "客户简称",
        "component": "select",
        "span": 6,
        "dictionary": "customers",
        "required": true,
        "props": {"placeholder": "请选择客户", "filterable": true, "clearable": true}
      },
      {
        "field": "f_xsy",
        "label": "销售员",
        "component": "select",
        "span": 6,
        "dictionary": "salesman",
        "required": false,
        "props": {"placeholder": "请选择销售员", "clearable": true, "filterable": true}
      },
      {
        "field": "fdocumentstatus",
        "label": "数据状态",
        "component": "select",
        "span": 6,
        "dictionary": "f_document_status",
        "props": {"placeholder": "请选择数据状态", "clearable": true},
        "defaultValue": "0"
      },
      {
        "field": "f_fktj",
        "label": "付款条件",
        "component": "input",
        "span": 12,
        "props": {"maxlength": 200, "clearable": true, "placeholder": "请输入付款条件"}
      }
    ]
  }', 
  '{
    "tableName": "purchase_quotation",
    "primaryKey": "id",
    "columns": [
      {"type": "selection", "width": 55, "fixed": "left", "resizable": false},
      {"type": "expand", "width": 100, "fixed": "left", "resizable": false, "label": "详情"},
      {"prop": "fbillno", "label": "单据编号", "width": 150, "fixed": "left", "align": "left", "visible": true, "resizable": true, "renderType": "text"},
      {"prop": "fdate", "label": "报价日期", "width": 140, "align": "center", "visible": true, "renderType": "date", "format": "YYYY-MM-DD"},
      {"prop": "f_khjc", "label": "客户简称", "width": 120, "align": "left", "visible": true, "renderType": "text", "dictionary": "customers"},
      {"prop": "f_xsy", "label": "销售员", "width": 100, "align": "left", "visible": true, "renderType": "text", "dictionary": "salesman"},
      {"prop": "fdocumentstatus", "label": "数据状态", "width": 100, "align": "center", "visible": true, "renderType": "tag", "dictionary": "f_document_status"},
      {"prop": "f_fktj", "label": "付款条件", "width": 150, "align": "left", "visible": true, "renderType": "text"},
      {"prop": "fcreatedate", "label": "创建时间", "width": 160, "align": "center", "visible": true, "renderType": "datetime", "format": "YYYY-MM-DD HH:mm:ss"},
      {"prop": "fcreatorid", "label": "创建人", "width": 100, "align": "left", "visible": true, "renderType": "text", "dictionary": "users"}
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
        "label": "报价日期",
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
        "field": "fbillno",
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
        "field": "f_khjc",
        "label": "客户简称",
        "component": "select",
        "dictionary": "customers",
        "props": {
          "placeholder": "客户简称",
          "clearable": true,
          "filterable": true,
          "style": {"width": "130px"}
        },
        "defaultValue": "",
        "queryOperator": "eq"
      },
      {
        "field": "f_xsy",
        "label": "销售员",
        "component": "select",
        "dictionary": "salesman",
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
        "field": "fdocumentstatus",
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
      {"key": "add", "label": "新增", "icon": "Plus", "permission": "k3:purchasequotation:add", "type": "primary", "position": "left", "handler": "handleAdd"},
      {"key": "edit", "label": "修改", "icon": "Edit", "permission": "k3:purchasequotation:edit", "type": "success", "position": "left", "disabled": "single", "handler": "handleUpdate"},
      {"key": "delete", "label": "删除", "icon": "Delete", "permission": "k3:purchasequotation:remove", "type": "danger", "position": "left", "disabled": "multiple", "handler": "handleDelete", "confirm": "是否确认删除选中的 {count} 条数据？"},
      {"key": "audit", "label": "审核", "icon": "CircleCheck", "permission": "k3:purchasequotation:audit", "type": "success", "position": "left", "disabled": "multiple", "handler": "handleAudit", "confirm": "是否确认审核选中的 {count} 条数据？"},
      {"key": "unAudit", "label": "反审核", "icon": "Close", "permission": "k3:purchasequotation:unAudit", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleUnAudit", "confirm": "是否确认反审核选中的 {count} 条数据？"},
      {"key": "export", "label": "导出", "icon": "Download", "permission": "k3:purchasequotation:export", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleExport"},
      {"key": "columnSetting", "label": "列设置", "icon": "Setting", "type": "info", "position": "right", "handler": "openColumnSetting"}
    ],
    "row": []
  }', 
  '{
    "baseUrl": "/api/purchasequotation",
    "methods": {
      "list": {
        "url": "/list",
        "method": "GET",
        "description": "查询采购报价单列表"
      },
      "get": {
        "url": "/{id}",
        "method": "GET",
        "description": "获取采购报价单详情"
      },
      "add": {
        "url": "/add",
        "method": "POST",
        "description": "新增采购报价单"
      },
      "update": {
        "url": "/update",
        "method": "PUT",
        "description": "修改采购报价单"
      },
      "delete": {
        "url": "/delete",
        "method": "DELETE",
        "description": "删除采购报价单"
      },
      "entry": {
        "url": "/entry/{billNo}",
        "method": "GET",
        "description": "获取采购报价单明细"
      },
      "audit": {
        "url": "/audit",
        "method": "POST",
        "description": "审核采购报价单"
      },
      "unAudit": {
        "url": "/unAudit",
        "method": "POST",
        "description": "反审核采购报价单"
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
      "salesman": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/salesman",
          "useGlobalCache": true,
          "cacheKey": "salesman_dict",
          "cacheTTL": 86400000
        }
      },
      "bill_status": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/bill_status",
          "useGlobalCache": true,
          "cacheKey": "bill_status_dict",
          "cacheTTL": 86400000
        }
      },
      "suppliers": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/suppliers",
          "useGlobalCache": true,
          "cacheKey": "suppliers_dict",
          "cacheTTL": 86400000
        }
      },
      "purchase_org": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/purchase_org",
          "useGlobalCache": true,
          "cacheKey": "purchase_org_dict",
          "cacheTTL": 86400000
        }
      },
      "pay_org": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/pay_org",
          "useGlobalCache": true,
          "cacheKey": "pay_org_dict",
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
      "quote_type": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/quote_type",
          "useGlobalCache": true,
          "cacheKey": "quote_type_dict",
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
    "entityName": "采购报价单",
    "entityNameSingular": "采购报价单",
    "dialogTitle": {"add": "新增{entityName}", "edit": "修改{entityName}"},
    "drawerTitle": "{entityName}详情 - {fbillno}"
  }',
  '{
    "detail": {
      "enabled": true,
      "displayType": "drawer",
      "title": "{entityName}详情 - {fbillno}",
      "width": "60%",
      "direction": "rtl",
      "loadStrategy": "lazy",
      "tabs": [
        {
          "name": "entry",
          "label": "报价明细",
          "icon": "Document",
          "type": "table",
          "dataField": "entryList",
          "tableName": "purchase_quotation_entry",
          "relationConfig": { 
            "masterTable": "purchase_quotation",
            "masterField": "fbillno",
            "detailTable": "purchase_quotation_entry",
            "detailField": "fbillno",
            "operator": "eq"
          },
          "queryConfig": {
            "enabled": true,
            "defaultConditions": [
              {
                "field": "fid",
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
              {"prop": "f_cpms1", "label": "产品名称", "width": 150, "align": "left", "showOverflowTooltip": true},
              {"prop": "f_cgjg", "label": "采购价", "width": 100, "align": "right", "renderType": "currency", "precision": 4, "sortable": true},
              {"prop": "f_zxqdl", "label": "起订量", "width": 100, "align": "right", "renderType": "number", "precision": 2, "sortable": true},
              {"prop": "f_xdl", "label": "下单量", "width": 100, "align": "right", "renderType": "number", "precision": 2},
              {"prop": "f_ora_unitid", "label": "计量单位", "width": 80, "align": "center"},
              {"prop": "f_jgyxq", "label": "价格有效期", "width": 120, "align": "center", "renderType": "date", "format": "YYYY-MM-DD"},
              {"prop": "f_gys", "label": "供应商", "width": 120, "align": "left", "showOverflowTooltip": true},
              {"prop": "f_bz", "label": "产品优势", "width": 200, "align": "left", "showOverflowTooltip": true}
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
  '采购报价单页面配置 - 初始版本'
);

-- ============================================
-- 配置导入完成
-- ============================================
SELECT '采购报价单页面配置导入完成' AS message;
