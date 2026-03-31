-- ============================================
-- ERP 配置 JSON 强制拆分方案 - 收料通知单模块导入 SQL（字典重构版）
-- 版本：v4.0 (9字段拆分 + API配置版)
-- 日期：2026-03-31
-- 说明：导入收料通知单页面配置数据（9 字段强制拆分 + API配置 + 统一字典 API）
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

-- 级联删除收料通知单相关的所有配置
DELETE FROM erp_page_config_history WHERE module_code = 'receivenotice';
DELETE FROM erp_approval_history WHERE module_code = 'receivenotice';
DELETE FROM erp_approval_flow WHERE module_code = 'receivenotice';
DELETE FROM erp_push_relation WHERE source_module = 'receivenotice' OR target_module = 'receivenotice';
DELETE FROM erp_page_config WHERE module_code = 'receivenotice';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第二步：插入收料通知单页面配置数据
-- ============================================

-- 收料通知单页面配置（9 字段强制拆分，包含 api_config、search_config 和 action_config）
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
  'receivenotice',
  '收料通知单管理',
  'PAGE', 
  '{
    "pageId": "receivenotice",
    "title": "{entityName}管理",
    "permission": "k3:receivenotice:query",
    "layout": "standard",
    "apiPrefix": "/erp/engine",
    "tableName": "receive_notice"
  }', 
  '{
    "formConfig": {
      "dialogWidth": "1200px",
      "labelWidth": "120px",
      "layout": "horizontal"
    },
    "fields": [
      {
        "field": "FBillNo",
        "label": "单据编号",
        "component": "input",
        "span": 6,
        "required": true,
        "rules": [{"required": true, "message": "单据编号不能为空", "trigger": "blur"}],
        "props": {"maxlength": 100, "clearable": true}
      },
      {
        "field": "FDate",
        "label": "收料日期",
        "component": "date",
        "span": 6,
        "required": true,
        "rules": [{"required": true, "message": "收料日期不能为空", "trigger": "change"}],
        "props": {"placeholder": "选择日期", "valueFormat": "YYYY-MM-DD"}
      },
      {
        "field": "FSupplierId",
        "label": "供应商",
        "component": "select",
        "span": 6,
        "dictionary": "suppliers",
        "required": true,
        "props": {"placeholder": "请选择供应商", "filterable": true, "clearable": true}
      },
      {
        "field": "FPurchaseOrgId",
        "label": "采购组织",
        "component": "select",
        "span": 6,
        "dictionary": "purchase_org",
        "required": true,
        "props": {"placeholder": "请选择采购组织", "clearable": true, "filterable": true}
      },
      {
        "field": "FStockId",
        "label": "收料仓库",
        "component": "select",
        "span": 6,
        "dictionary": "stock",
        "required": true,
        "props": {"placeholder": "请选择收料仓库", "clearable": true, "filterable": true}
      },
      {
        "field": "FDocumentStatus",
        "label": "单据状态",
        "component": "select",
        "span": 6,
        "dictionary": "bill_status",
        "props": {"placeholder": "请选择单据状态", "clearable": true},
        "defaultValue": "0"
      }
    ]
  }', 
  '{
    "tableName": "receive_notice",
    "primaryKey": "FID",
    "columns": [
      {"type": "selection", "width": 55, "fixed": "left", "resizable": false},
      {"type": "expand", "width": 100, "fixed": "left", "resizable": false, "label": "详情"},
      {"prop": "FBillNo", "label": "单据编号", "width": 150, "fixed": "left", "align": "left", "visible": true, "resizable": true, "renderType": "text"},
      {"prop": "FDate", "label": "收料日期", "width": 140, "align": "center", "visible": true, "renderType": "date", "format": "YYYY-MM-DD"},
      {"prop": "FSupplierId", "label": "供应商", "width": 120, "align": "left", "visible": true, "renderType": "text", "dictionary": "suppliers"},
      {"prop": "FPurchaseOrgId", "label": "采购组织", "width": 120, "align": "left", "visible": true, "renderType": "text", "dictionary": "purchase_org"},
      {"prop": "FStockId", "label": "收料仓库", "width": 120, "align": "left", "visible": true, "renderType": "text", "dictionary": "stock"},
      {"prop": "FDocumentStatus", "label": "单据状态", "width": 100, "align": "center", "visible": true, "renderType": "tag", "dictionary": "bill_status"},
      {"prop": "FCreateDate", "label": "创建时间", "width": 160, "align": "center", "visible": true, "renderType": "datetime", "format": "YYYY-MM-DD HH:mm:ss"},
      {"prop": "FCreatorId", "label": "创建人", "width": 100, "align": "left", "visible": true, "renderType": "text"}
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
        "field": "FDate",
        "label": "收料日期",
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
        "field": "FBillNo",
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
        "field": "FSupplierId",
        "label": "供应商",
        "component": "select",
        "dictionary": "suppliers",
        "props": {
          "placeholder": "供应商",
          "clearable": true,
          "filterable": true,
          "style": {"width": "130px"}
        },
        "defaultValue": "",
        "queryOperator": "eq"
      },
      {
        "field": "FPurchaseOrgId",
        "label": "采购组织",
        "component": "select",
        "dictionary": "purchase_org",
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
        "field": "FDocumentStatus",
        "label": "单据状态",
        "component": "select",
        "dictionary": "bill_status",
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
      {"key": "add", "label": "新增", "icon": "Plus", "permission": "k3:receivenotice:add", "type": "primary", "position": "left", "handler": "handleAdd"},
      {"key": "edit", "label": "修改", "icon": "Edit", "permission": "k3:receivenotice:edit", "type": "success", "position": "left", "disabled": "single", "handler": "handleUpdate"},
      {"key": "delete", "label": "删除", "icon": "Delete", "permission": "k3:receivenotice:remove", "type": "danger", "position": "left", "disabled": "multiple", "handler": "handleDelete", "confirm": "是否确认删除选中的 {count} 条数据？"},
      {"key": "audit", "label": "审核", "icon": "CircleCheck", "permission": "k3:receivenotice:audit", "type": "success", "position": "left", "disabled": "multiple", "handler": "handleAudit", "confirm": "是否确认审核选中的 {count} 条数据？"},
      {"key": "unAudit", "label": "反审核", "icon": "Close", "permission": "k3:receivenotice:unAudit", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleUnAudit", "confirm": "是否确认反审核选中的 {count} 条数据？"},
      {"key": "export", "label": "导出", "icon": "Download", "permission": "k3:receivenotice:export", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleExport"},
      {"key": "columnSetting", "label": "列设置", "icon": "Setting", "type": "info", "position": "right", "handler": "openColumnSetting"}
    ],
    "row": []
  }', 
  '{
    "baseUrl": "/api/receivenotice",
    "methods": {
      "list": {
        "url": "/list",
        "method": "GET",
        "description": "查询收料通知单列表"
      },
      "get": {
        "url": "/{id}",
        "method": "GET",
        "description": "获取收料通知单详情"
      },
      "add": {
        "url": "/add",
        "method": "POST",
        "description": "新增收料通知单"
      },
      "update": {
        "url": "/update",
        "method": "PUT",
        "description": "修改收料通知单"
      },
      "delete": {
        "url": "/delete",
        "method": "DELETE",
        "description": "删除收料通知单"
      },
      "entry": {
        "url": "/entry/{billNo}",
        "method": "GET",
        "description": "获取收料通知单明细"
      },
      "audit": {
        "url": "/audit",
        "method": "POST",
        "description": "审核收料通知单"
      },
      "unAudit": {
        "url": "/unAudit",
        "method": "POST",
        "description": "反审核收料通知单"
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
      "purchase_org": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/purchase_org",
          "useGlobalCache": true,
          "cacheKey": "purchase_org_dict",
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
    "entityName": "收料通知单",
    "entityNameSingular": "收料通知单",
    "dialogTitle": {"add": "新增{entityName}", "edit": "修改{entityName}"},
    "drawerTitle": "{entityName}详情 - {billNo}"
  }',
  '{
    "detail": {
      "enabled": true,
      "displayType": "drawer",
      "title": "{entityName}详情 - {billNo}",
      "width": "60%",
      "direction": "rtl",
      "loadStrategy": "lazy",
      "tabs": [
        {
          "name": "entry",
          "label": "收料通知单明细",
          "icon": "Document",
          "type": "table",
          "dataField": "entryList",
          "tableName": "receive_notice_entry",
          "queryConfig": {
            "enabled": true,
            "defaultConditions": [
              {
                "field": "FBillNo",
                "operator": "eq",
                "value": "${billNo}",
                "description": "按单据编号查询明细"
              }
            ],
            "defaultOrderBy": [{"field": "FEntryId", "direction": "ASC"}]
          },
          "table": {
            "border": true,
            "stripe": true,
            "maxHeight": "500",
            "showOverflowTooltip": true,
            "columns": [
              {"prop": "FEntryId", "label": "明细ID", "width": 100, "align": "center", "sortable": true},
              {"prop": "FMaterialId", "label": "物料编码", "width": 120, "align": "left", "showOverflowTooltip": true},
              {"prop": "FMaterialName", "label": "物料名称", "width": 150, "align": "left", "showOverflowTooltip": true},
              {"prop": "FQty", "label": "收料数量", "width": 100, "align": "right", "renderType": "number", "precision": 2, "sortable": true},
              {"prop": "FUnitId", "label": "单位", "width": 80, "align": "center"},
              {"prop": "FStockId", "label": "仓库", "width": 100, "align": "left", "showOverflowTooltip": true},
              {"prop": "FPosition", "label": "仓位", "width": 100, "align": "left", "showOverflowTooltip": true},
              {"prop": "FRemark", "label": "备注", "width": 200, "align": "left", "showOverflowTooltip": true}
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
  '收料通知单页面配置 - 初始版本'
);

-- ============================================
-- 配置导入完成
-- ============================================
SELECT '收料通知单页面配置导入完成' AS message;
