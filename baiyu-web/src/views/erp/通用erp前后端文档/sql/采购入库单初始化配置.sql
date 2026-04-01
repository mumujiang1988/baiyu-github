-- ============================================
-- ERP 配置 JSON 强制拆分方案 - 采购入库单模块导入 SQL（字典重构版）
-- 版本：v4.0 (9 字段拆分 + API 配置版)
-- 日期：2026-04-01
-- 说明：导入采购入库单页面配置数据（9 字段强制拆分 + API 配置 + 统一字典 API）
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

-- 级联删除采购入库单相关的所有配置
DELETE FROM erp_page_config_history WHERE module_code = 'purchaseinstock';
DELETE FROM erp_approval_history WHERE module_code = 'purchaseinstock';
DELETE FROM erp_approval_flow WHERE module_code = 'purchaseinstock';
DELETE FROM erp_push_relation WHERE source_module = 'purchaseinstock' OR target_module = 'purchaseinstock';
DELETE FROM erp_page_config WHERE module_code = 'purchaseinstock';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第二步：插入采购入库单页面配置数据
-- ============================================

-- 采购入库单页面配置（9 字段强制拆分，包含 api_config、search_config 和 action_config）
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
  'purchaseinstock',
  '采购入库单管理',
  'PAGE', 
  '{
    "pageId": "purchaseinstock",
    "title": "{entityName}管理",
    "permission": "k3:purchaseinstock:query",
    "layout": "standard",
    "apiPrefix": "/erp/engine",
    "tableName": "purchase_instock",
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
        "label": "入库日期",
        "component": "date",
        "span": 6,
        "required": true,
        "rules": [{"required": true, "message": "入库日期不能为空", "trigger": "change"}],
        "props": {"placeholder": "选择日期", "valueFormat": "YYYY-MM-DD"}
      },
      {
        "field": "f_supplier_id",
        "label": "供应商编码",
        "component": "select",
        "span": 6,
        "dictionary": "suppliers",
        "required": true,
        "props": {"placeholder": "请选择供应商", "filterable": true, "clearable": true}
      },
      {
        "field": "f_settle_org_id",
        "label": "采购组织",
        "component": "select",
        "span": 6,
        "dictionary": "stock_org",
        "required": true,
        "props": {"placeholder": "请选择采购组织", "clearable": true, "filterable": true}
      },
      {
        "field": "f_purchaser_id",
        "label": "采购员",
        "component": "select",
        "span": 6,
        "dictionary": "salesman",
        "required": false,
        "props": {"placeholder": "请选择采购员", "clearable": true, "filterable": true}
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
        "field": "f_supply_address",
        "label": "供货方地址",
        "component": "input",
        "span": 18,
        "props": {"type": "textarea", "rows": 3, "maxlength": 500, "placeholder": "请输入备注"}
      }
    ]
  }', 
  '{
    "tableName": "purchase_instock",
    "primaryKey": "id",
    "columns": [
      {"type": "selection", "width": 55, "fixed": "left", "resizable": false},
      {"type": "expand", "width": 100, "fixed": "left", "resizable": false, "label": "详情"},
      {"prop": "f_bill_no", "label": "单据编号", "width": 150, "fixed": "left", "align": "left", "visible": true, "resizable": true, "renderType": "text"},
      {"prop": "f_date", "label": "入库日期", "width": 140, "align": "center", "visible": true, "renderType": "date", "format": "YYYY-MM-DD"},
      {"prop": "f_supplier_id", "label": "供应商编码", "width": 120, "align": "left", "visible": true, "renderType": "text", "dictionary": "suppliers"},
      {"prop": "f_settle_org_id", "label": "采购组织", "width": 120, "align": "left", "visible": true, "renderType": "text", "dictionary": "stock_org"},
      {"prop": "f_purchaser_id", "label": "采购员", "width": 100, "align": "left", "visible": true, "renderType": "text", "dictionary": "salesman"},
      {"prop": "f_document_status", "label": "单据状态", "width": 100, "align": "center", "visible": true, "renderType": "tag", "dictionary": "f_document_status"},
      {"prop": "f_create_date", "label": "创建时间", "width": 160, "align": "center", "visible": true, "renderType": "datetime", "format": "YYYY-MM-DD HH:mm:ss"},
      {"prop": "f_creator_id", "label": "创建人", "width": 100, "align": "left", "visible": true, "renderType": "text"}
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
        "label": "入库日期",
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
        "field": "f_supplier_id",
        "label": "供应商编码",
        "component": "select",
        "dictionary": "suppliers",
        "props": {
          "placeholder": "供应商编码",
          "clearable": true,
          "filterable": true,
          "style": {"width": "130px"}
        },
        "defaultValue": "",
        "queryOperator": "eq"
      },
      {
        "field": "f_settle_org_id",
        "label": "采购组织",
        "component": "select",
        "dictionary": "stock_org",
        "props": {
          "placeholder": "采购组织",
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
      {"key": "add", "label": "新增", "icon": "Plus", "permission": "k3:purchaseinstock:add", "type": "primary", "position": "left", "handler": "handleAdd"},
      {"key": "edit", "label": "修改", "icon": "Edit", "permission": "k3:purchaseinstock:edit", "type": "success", "position": "left", "disabled": "single", "handler": "handleUpdate"},
      {"key": "delete", "label": "删除", "icon": "Delete", "permission": "k3:purchaseinstock:remove", "type": "danger", "position": "left", "disabled": "multiple", "handler": "handleDelete", "confirm": "是否确认删除选中的 {count} 条数据？"},
      {"key": "audit", "label": "审核", "icon": "CircleCheck", "permission": "k3:purchaseinstock:audit", "type": "success", "position": "left", "disabled": "multiple", "handler": "handleAudit", "confirm": "是否确认审核选中的 {count} 条数据？"},
      {"key": "unAudit", "label": "反审核", "icon": "Close", "permission": "k3:purchaseinstock:unAudit", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleUnAudit", "confirm": "是否确认反审核选中的 {count} 条数据？"},
      {"key": "export", "label": "导出", "icon": "Download", "permission": "k3:purchaseinstock:export", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleExport"},
      {"key": "columnSetting", "label": "列设置", "icon": "Setting", "type": "info", "position": "right", "handler": "openColumnSetting"}
    ],
    "row": []
  }', 
  '{
    "baseUrl": "/api/purchaseinstock",
    "methods": {
      "list": {
        "url": "/list",
        "method": "GET",
        "description": "查询采购入库单列表"
      },
      "get": {
        "url": "/{id}",
        "method": "GET",
        "description": "获取采购入库单详情"
      },
      "add": {
        "url": "/add",
        "method": "POST",
        "description": "新增采购入库单"
      },
      "update": {
        "url": "/update",
        "method": "PUT",
        "description": "修改采购入库单"
      },
      "delete": {
        "url": "/delete",
        "method": "DELETE",
        "description": "删除采购入库单"
      },
      "entry": {
        "url": "/entry/{billNo}",
        "method": "GET",
        "description": "获取采购入库单明细"
      },
      "audit": {
        "url": "/audit",
        "method": "POST",
        "description": "审核采购入库单"
      },
      "unAudit": {
        "url": "/unAudit",
        "method": "POST",
        "description": "反审核采购入库单"
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
      "stock": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/stock",
          "useGlobalCache": true,
          "cacheKey": "stock_dict",
          "cacheTTL": 86400000
        }
      },
      "stock_org": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/stock_org",
          "useGlobalCache": true,
          "cacheKey": "stock_org_dict",
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
      "salesman": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/salesman",
          "useGlobalCache": true,
          "cacheKey": "salesman_dict",
          "cacheTTL": 86400000
        }
      },
      "audit_status": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/audit_status",
          "useGlobalCache": true,
          "cacheKey": "audit_status_dict",
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
    "entityName": "采购入库单",
    "entityNameSingular": "采购入库单",
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
          "label": "入库明细",
          "icon": "Document",
          "type": "table",
          "dataField": "entryList",
          "tableName": "purchase_instock_entry",
          "relationConfig": {
            "enabled": true,
            "masterTable": "purchase_instock",
            "masterField": "f_bill_no",
            "detailTable": "purchase_instock_entry",
            "detailField": "fbillno",
            "operator": "eq"
          },
          "queryConfig": {
            "enabled": true,
            "defaultConditions": [
              {
                "field": "fbillno",
                "operator": "eq",
                "value": "${f_bill_no}",
                "description": "按单据编号查询明细"
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
              {"prop": "fmaterialId", "label": "物料编码", "width": 120, "align": "left", "showOverflowTooltip": true},
              {"prop": "fmaterialname", "label": "物料名称", "width": 150, "align": "left", "showOverflowTooltip": true},
              {"prop": "frealqty", "label": "实收数量", "width": 100, "align": "right", "renderType": "number", "precision": 2, "sortable": true},
              {"prop": "ftaxprice", "label": "含税单价", "width": 100, "align": "right", "renderType": "currency", "precision": 4},
              {"prop": "fallamount", "label": "价税合计", "width": 120, "align": "right", "renderType": "currency", "precision": 2, "sortable": true},
              {"prop": "funitid", "label": "单位", "width": 80, "align": "center"},
              {"prop": "fstockid", "label": "仓库", "width": 100, "align": "left", "showOverflowTooltip": true},
              {"prop": "fstocklocid", "label": "仓位", "width": 100, "align": "left", "showOverflowTooltip": true},
              {"prop": "fnote", "label": "备注", "width": 200, "align": "left", "showOverflowTooltip": true}
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
  '采购入库单页面配置 - 初始版本'
);

-- ============================================
-- 配置导入完成
-- ============================================
SELECT '采购入库单页面配置导入完成' AS message;
