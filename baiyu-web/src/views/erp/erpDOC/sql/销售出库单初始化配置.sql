-- ============================================
-- ERP 配置 JSON 强制拆分方案 - 销售出库单模块导入 SQL
-- 版本：v1.0 (9 字段拆分 + API 配置版)
-- 日期：2026-04-01
-- 说明：导入销售出库单页面配置数据（完全参考销售订单结构）
--   - page_config: 页面基础配置
--   - form_config: 表单配置
--   - table_config: 表格列配置
--   - search_config: 搜索区域配置
--   - action_config: 按钮操作配置
--   - api_config: API 接口配置
--   - dict_config: 字典数据源配置
--   - business_config: 业务规则配置
--   - detail_config: 详情页配置
-- ============================================

USE test;

-- ============================================
-- 第一步：清理旧数据（级联删除）
-- ============================================

SET FOREIGN_KEY_CHECKS = 0;

-- 级联删除销售出库单相关的所有配置
DELETE FROM erp_page_config_history WHERE module_code = 'saloutbound';
DELETE FROM erp_approval_history WHERE module_code = 'saloutbound';
DELETE FROM erp_approval_flow WHERE module_code = 'saloutbound';
DELETE FROM erp_push_relation WHERE source_module = 'saloutbound' OR target_module = 'saloutbound';
DELETE FROM erp_page_config WHERE module_code = 'saloutbound';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第二步：插入销售出库单页面配置数据
-- ============================================

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
  'saloutbound',
  '销售出库单管理',
  'PAGE', 
  '{
    "pageId": "saloutbound",
    "title": "{entityName}管理",
    "permission": "erp:saloutbound:query",
    "layout": "standard",
    "apiPrefix": "/erp/engine",
    "tableName": "f_sal_outbound",
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
        "field": "f_document_status",
        "label": "单据状态",
        "component": "select",
        "span": 6,
        "dictionary": "f_document_status",
        "props": {"placeholder": "请选择单据状态", "clearable": true},
        "defaultValue": ""
      },
      {
        "field": "f_saleorg_id",
        "label": "销售组织",
        "component": "select",
        "span": 6,
        "dictionary": "organizations",
        "required": true,
        "props": {"placeholder": "请选择销售组织", "filterable": true, "clearable": true}
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
        "field": "f_stockorg_id",
        "label": "发货组织",
        "component": "select",
        "span": 6,
        "dictionary": "organizations",
        "required": true,
        "props": {"placeholder": "请选择发货组织", "filterable": true, "clearable": true}
      },
      {
        "field": "f_customer_id",
        "label": "客户",
        "component": "select",
        "span": 6,
        "dictionary": "customers",
        "required": true,
        "props": {"placeholder": "请选择客户", "filterable": true, "clearable": true}
      },
      {
        "field": "f_deliverydept_id",
        "label": "发货部门",
        "component": "select",
        "span": 6,
        "dictionary": "departments",
        "props": {"placeholder": "请选择发货部门", "filterable": true, "clearable": true}
      },
      {
        "field": "f_saledept_id",
        "label": "销售部门",
        "component": "select",
        "span": 6,
        "dictionary": "departments",
        "props": {"placeholder": "请选择销售部门", "filterable": true, "clearable": true}
      },
      {
        "field": "f_stockergroup_id",
        "label": "库存组",
        "component": "select",
        "span": 6,
        "dictionary": "stock_group",
        "props": {"placeholder": "请选择库存组", "filterable": true, "clearable": true}
      },
      {
        "field": "f_stocker_id",
        "label": "仓管员",
        "component": "select",
        "span": 6,
        "dictionary": "users",
        "props": {"placeholder": "请选择仓管员", "filterable": true, "clearable": true}
      },
      {
        "field": "f_salesgroup_id",
        "label": "销售组",
        "component": "select",
        "span": 6,
        "dictionary": "sales_group",
        "props": {"placeholder": "请选择销售组", "filterable": true, "clearable": true}
      },
      {
        "field": "f_salesman_id",
        "label": "销售员",
        "component": "select",
        "span": 6,
        "dictionary": "salespersons",
        "props": {"placeholder": "请选择销售员", "filterable": true, "clearable": true}
      },
      {
        "field": "f_carrier_id",
        "label": "承运商",
        "component": "select",
        "span": 6,
        "dictionary": "suppliers",
        "required": true,
        "props": {"placeholder": "请选择承运商", "filterable": true, "clearable": true}
      },
      {
        "field": "f_carriage_no",
        "label": "运输单号",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 100, "clearable": true}
      },
      {
        "field": "f_receiver_id",
        "label": "收货方",
        "component": "select",
        "span": 6,
        "dictionary": "customers",
        "props": {"placeholder": "请选择收货方", "filterable": true, "clearable": true}
      },
      {
        "field": "f_settle_id",
        "label": "结算方",
        "component": "select",
        "span": 6,
        "dictionary": "customers",
        "props": {"placeholder": "请选择结算方", "filterable": true, "clearable": true}
      },
      {
        "field": "f_payer_id",
        "label": "付款方",
        "component": "select",
        "span": 6,
        "dictionary": "customers",
        "props": {"placeholder": "请选择付款方", "filterable": true, "clearable": true}
      },
      {
        "field": "f_billtype_id",
        "label": "单据类型",
        "component": "select",
        "span": 6,
        "dictionary": "bill_type",
        "required": true,
        "props": {"placeholder": "请选择单据类型", "filterable": true, "clearable": true}
      },
      {
        "field": "f_owner_typeid_head",
        "label": "货主类型",
        "component": "select",
        "span": 6,
        "dictionary": "owner_type",
        "props": {"placeholder": "请选择货主类型", "clearable": true}
      },
      {
        "field": "f_ownerid_head",
        "label": "货主",
        "component": "select",
        "span": 6,
        "dictionary": "owner",
        "props": {"placeholder": "请选择货主", "filterable": true, "clearable": true}
      },
      {
        "field": "f_bussiness_type",
        "label": "业务类型",
        "component": "select",
        "span": 6,
        "dictionary": "business_type",
        "props": {"placeholder": "请选择业务类型", "clearable": true}
      },
      {
        "field": "f_receive_address",
        "label": "收货方地址",
        "component": "input",
        "span": 12,
        "props": {"maxlength": 255, "clearable": true}
      },
      {
        "field": "f_headlocation_id",
        "label": "交货地点",
        "component": "select",
        "span": 6,
        "dictionary": "location",
        "props": {"placeholder": "请选择交货地点", "filterable": true, "clearable": true}
      },
      {
        "field": "f_cydh",
        "label": "出运单号",
        "component": "input",
        "span": 6,
        "required": true,
        "props": {"maxlength": 100, "clearable": true}
      },
      {
        "field": "f_ora_baseproperty",
        "label": "客户简称",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 200, "clearable": true}
      },
      {
        "field": "f_mysy",
        "label": "贸易术语",
        "component": "select",
        "span": 6,
        "dictionary": "trade_terms",
        "props": {"placeholder": "请选择贸易术语", "clearable": true}
      },
      {
        "field": "f_gkmc",
        "label": "港口名称",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 200, "clearable": true}
      },
      {
        "field": "f_qyg",
        "label": "启运港",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 200, "clearable": true}
      },
      {
        "field": "f_khqc",
        "label": "客户全称",
        "component": "input",
        "span": 12,
        "props": {"maxlength": 200, "clearable": true}
      },
      {
        "field": "f_djbgzt",
        "label": "单据报关状态",
        "component": "select",
        "span": 6,
        "dictionary": "customs_status",
        "props": {"placeholder": "请选择报关状态", "clearable": true}
      },
      {
        "field": "f_tcbl",
        "label": "提成比例",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "max": 100, "precision": 4, "step": 0.0001, "controlsPosition": "right"}
      },
      {
        "field": "f_tssl",
        "label": "退税率%",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "max": 100, "precision": 4, "step": 0.0001, "controlsPosition": "right"}
      },
      {
        "field": "f_bcfy",
        "label": "包材费用",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"}
      },
      {
        "field": "f_cty_decimal1",
        "label": "包装费",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"}
      },
      {
        "field": "f_tpf",
        "label": "托盘费",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"}
      },
      {
        "field": "f_note",
        "label": "备注",
        "component": "input",
        "span": 12,
        "props": {"maxlength": 500, "clearable": true, "type": "textarea", "rows": 3}
      }
    ]
  }', 
  '{
    "tableName": "f_sal_outbound",
    "primaryKey": "id",
    "orderBy": [
      {"field": "f_date", "direction": "DESC"}
    ],
    "columns": [
      {"type": "selection", "width": 55, "fixed": "left", "resizable": false},
      {"type": "expand", "width": 100, "fixed": "left", "resizable": false, "label": "详情"},
      {"prop": "f_bill_no", "label": "单据编号", "width": 150, "fixed": "left", "align": "left", "visible": true, "resizable": true},
      {"prop": "f_document_status", "label": "单据状态", "width": 120, "align": "center", "visible": true, "renderType": "tag", "dictionary": "f_document_status"},
      {"prop": "f_date", "label": "日期", "width": 140, "align": "center", "visible": true, "renderType": "date", "format": "YYYY-MM-DD"},
      {"prop": "f_customer_id", "label": "客户", "width": 150, "align": "left", "visible": true, "renderType": "text", "dictionary": "customers"},
      {"prop": "f_ora_baseproperty", "label": "客户简称", "width": 150, "align": "left", "visible": true},
      {"prop": "f_salesman_id", "label": "销售员", "width": 120, "align": "left", "visible": true, "renderType": "text", "dictionary": "salespersons"},
      {"prop": "f_carrier_id", "label": "承运商", "width": 120, "align": "left", "visible": true, "renderType": "text", "dictionary": "suppliers"},
      {"prop": "f_cydh", "label": "出运单号", "width": 150, "align": "left", "visible": true},
      {"prop": "f_djbgzt", "label": "报关状态", "width": 100, "align": "center", "visible": true, "renderType": "tag", "dictionary": "customs_status"},
      {"prop": "f_create_time", "label": "创建时间", "width": 160, "align": "center", "visible": true, "renderType": "datetime", "format": "YYYY-MM-DD HH:mm:ss"},
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
        "label": "日期",
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
        "field": "f_customer_id",
        "label": "客户",
        "component": "select",
        "dictionary": "customers",
        "props": {
          "placeholder": "客户",
          "clearable": true,
          "filterable": true,
          "style": {"width": "130px"}
        },
        "defaultValue": "",
        "queryOperator": "eq"
      },
      {
        "field": "f_salesman_id",
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
      },
      {
        "field": "f_djbgzt",
        "label": "报关状态",
        "component": "select",
        "dictionary": "customs_status",
        "props": {
          "placeholder": "报关状态",
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
      {"key": "add", "label": "新增", "icon": "Plus", "permission": "erp:saloutbound:add", "type": "primary", "position": "left", "handler": "handleAdd"},
      {"key": "edit", "label": "修改", "icon": "Edit", "permission": "erp:saloutbound:edit", "type": "success", "position": "left", "disabled": "single", "handler": "handleUpdate"},
      {"key": "delete", "label": "删除", "icon": "Delete", "permission": "erp:saloutbound:remove", "type": "danger", "position": "left", "disabled": "multiple", "handler": "handleDelete", "confirm": "是否确认删除选中的 {count} 条数据？"},
      {"key": "audit", "label": "审核", "icon": "CircleCheck", "permission": "erp:saloutbound:audit", "type": "success", "position": "left", "disabled": "multiple", "handler": "handleAudit", "confirm": "是否确认审核选中的 {count} 条数据？"},
      {"key": "unAudit", "label": "反审核", "icon": "Close", "permission": "erp:saloutbound:unAudit", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleUnAudit", "confirm": "是否确认反审核选中的 {count} 条数据？"},
      {"key": "push", "label": "下推", "icon": "Download", "permission": "erp:saloutbound:push", "type": "info", "position": "left", "disabled": "single", "handler": "handleOpenPushDialog"},
      {"key": "export", "label": "导出", "icon": "Download", "permission": "erp:saloutbound:export", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleExport"},
      {"key": "columnSetting", "label": "列设置", "icon": "Setting", "type": "info", "position": "right", "handler": "openColumnSetting"}
    ],
    "row": []
  }', 
  '{
    "baseUrl": "/api/saloutbound",
    "methods": {
      "list": {
        "url": "/list",
        "method": "GET",
        "description": "查询销售出库单列表"
      },
      "get": {
        "url": "/{id}",
        "method": "GET",
        "description": "获取销售出库单详情"
      },
      "add": {
        "url": "/add",
        "method": "POST",
        "description": "新增销售出库单"
      },
      "update": {
        "url": "/update",
        "method": "PUT",
        "description": "修改销售出库单"
      },
      "delete": {
        "url": "/delete",
        "method": "DELETE",
        "description": "删除销售出库单"
      },
      "entry": {
        "url": "/entry/{id}",
        "method": "GET",
        "description": "获取销售出库单明细"
      },
      "audit": {
        "url": "/audit",
        "method": "POST",
        "description": "审核销售出库单"
      },
      "unAudit": {
        "url": "/unAudit",
        "method": "POST",
        "description": "反审核销售出库单"
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
      "salespersons": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/salespersons",
          "useGlobalCache": true,
          "cacheKey": "salespersons_dict",
          "cacheTTL": 86400000
        }
      },
      "organizations": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/organizations",
          "useGlobalCache": true,
          "cacheKey": "organizations_dict",
          "cacheTTL": 86400000
        }
      },
      "departments": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/departments",
          "useGlobalCache": true,
          "cacheKey": "departments_dict",
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
      "bill_type": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/bill_type",
          "useGlobalCache": true,
          "cacheKey": "bill_type_dict",
          "cacheTTL": 86400000
        }
      },
      "stock_group": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/stock_group",
          "useGlobalCache": true,
          "cacheKey": "stock_group_dict",
          "cacheTTL": 86400000
        }
      },
      "sales_group": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/sales_group",
          "useGlobalCache": true,
          "cacheKey": "sales_group_dict",
          "cacheTTL": 86400000
        }
      },
      "owner_type": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/owner_type",
          "useGlobalCache": true,
          "cacheKey": "owner_type_dict",
          "cacheTTL": 86400000
        }
      },
      "owner": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/owner",
          "useGlobalCache": true,
          "cacheKey": "owner_dict",
          "cacheTTL": 86400000
        }
      },
      "location": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/location",
          "useGlobalCache": true,
          "cacheKey": "location_dict",
          "cacheTTL": 86400000
        }
      },
      "trade_terms": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/trade_terms",
          "useGlobalCache": true,
          "cacheKey": "trade_terms_dict",
          "cacheTTL": 86400000
        }
      },
      "customs_status": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/customs_status",
          "useGlobalCache": true,
          "cacheKey": "customs_status_dict",
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
    "entityName": "销售出库单",
    "entityNameSingular": "出库单",
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
          "label": "销售出库单明细",
          "icon": "Document",
          "type": "table",
          "dataField": "entryList",
          "tableName": "f_sal_outbound_details",
          "relationConfig": {
            "masterTable": "f_sal_outbound",
            "masterField": "id",
            "detailTable": "f_sal_outbound_details",
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
                "description": "按实体主键 ID 查询明细 (f_sal_outbound.id = f_sal_outbound_details.f_entry_id)"
              }
            ],
            "defaultOrderBy": [{"field": "f_material_id", "direction": "ASC"}]
          },
          "table": {
            "border": true,
            "stripe": true,
            "maxHeight": "500",
            "showOverflowTooltip": true,
            "columns": [
              {"prop": "f_material_id", "label": "物料编码", "width": 120, "align": "center", "sortable": true},
              {"prop": "f_material_name", "label": "物料名称", "width": 180, "align": "left", "showOverflowTooltip": true, "sortable": true},
              {"prop": "f_materia_model", "label": "规格型号", "width": 150, "align": "left", "showOverflowTooltip": true},
              {"prop": "f_materia_type", "label": "物料类别", "width": 100, "align": "center"},
              {"prop": "f_unit_id", "label": "库存单位", "width": 80, "align": "center"},
              {"prop": "f_must_qty", "label": "应发数量", "width": 100, "align": "right", "renderType": "number", "sortable": true, "precision": 6},
              {"prop": "f_real_qty", "label": "实发数量", "width": 100, "align": "right", "renderType": "number", "precision": 6},
              {"prop": "f_stock_id", "label": "仓库", "width": 120, "align": "left"},
              {"prop": "f_stocks_status_id", "label": "库存状态", "width": 100, "align": "center"},
              {"prop": "f_owner_id", "label": "货主", "width": 120, "align": "left"},
              {"prop": "f_lot", "label": "批号", "width": 120, "align": "center"},
              {"prop": "f_is_free", "label": "是否赠品", "width": 80, "align": "center", "renderType": "tag"},
              {"prop": "f_price", "label": "单价", "width": 100, "align": "right", "renderType": "currency", "precision": 6, "sortable": true},
              {"prop": "f_tax_price", "label": "含税单价", "width": 100, "align": "right", "renderType": "currency", "precision": 6},
              {"prop": "f_amount", "label": "金额", "width": 120, "align": "right", "renderType": "currency", "precision": 6},
              {"prop": "f_all_amount", "label": "价税合计", "width": 120, "align": "right", "renderType": "currency", "precision": 6},
              {"prop": "f_soor_derno", "label": "销售订单单号", "width": 150, "align": "left"},
              {"prop": "f_srcbill_no", "label": "源单编号", "width": 150, "align": "left"},
              {"prop": "f_produce_date", "label": "生产日期", "width": 120, "align": "center", "renderType": "date"},
              {"prop": "f_expiry_date", "label": "有效期至", "width": 120, "align": "center", "renderType": "date"},
              {"prop": "f_mz", "label": "毛重", "width": 80, "align": "right", "renderType": "number", "precision": 2},
              {"prop": "f_jz", "label": "净重", "width": 80, "align": "right", "renderType": "number", "precision": 2},
              {"prop": "f_xs", "label": "箱数", "width": 80, "align": "right", "renderType": "number", "precision": 2},
              {"prop": "f_zwbgpm", "label": "中文报关品名", "width": 150, "align": "left"},
              {"prop": "f_ywbgpm", "label": "英文报关品名", "width": 150, "align": "left"},
              {"prop": "f_hsbm", "label": "HS 编码", "width": 100, "align": "center"},
              {"prop": "f_sfbg", "label": "是否报关", "width": 80, "align": "center", "renderType": "tag"},
              {"prop": "f_ddhsdj", "label": "订单含税单价", "width": 100, "align": "right", "renderType": "currency", "precision": 6},
              {"prop": "f_tsl", "label": "退税率%", "width": 80, "align": "right", "renderType": "percent", "precision": 2},
              {"prop": "f_tcblnew", "label": "提成比例", "width": 80, "align": "right", "renderType": "percent", "precision": 2},
              {"prop": "f_bcfynew", "label": "包材费用", "width": 100, "align": "right", "renderType": "currency", "precision": 2}
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
  '销售出库单配置（v1.0 初始版本）'
);

-- ============================================
-- 第三步：验证导入结果
-- ============================================

SELECT '========================================' AS '';
SELECT ' 销售出库单配置导入成功！' AS '';
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
WHERE module_code = 'saloutbound';

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
WHERE module_code = 'saloutbound';

SELECT '========================================' AS '';
SELECT '下一步操作：' AS '';
SELECT '========================================' AS '';
SELECT '  后续将添加：' AS note1;
SELECT '    - 审批流程配置（erp_approval_flow）' AS note2;
SELECT '    - 下推关系配置（erp_push_relation）' AS note3;
SELECT '  请继续执行相应的导入脚本' AS note4;
SELECT '========================================' AS '';
SELECT '完成！' AS '';
SELECT '========================================' AS '';
