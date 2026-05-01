-- ============================================
-- ERP 配置 JSON 强制拆分方案 - 发货通知单模块导入 SQL
-- 版本：v1.0 (9 字段拆分 + API 配置版)
-- 日期：2026-04-01
-- 说明：导入发货通知单页面配置数据（完全参考销售订单结构）
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

-- 级联删除发货通知单相关的所有配置
DELETE FROM erp_page_config_history WHERE module_code = 'deliverynotice';
DELETE FROM erp_approval_history WHERE module_code = 'deliverynotice';
DELETE FROM erp_approval_flow WHERE module_code = 'deliverynotice';
DELETE FROM erp_push_relation WHERE source_module = 'deliverynotice' OR target_module = 'deliverynotice';
DELETE FROM erp_page_config WHERE module_code = 'deliverynotice';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第二步：插入发货通知单页面配置数据
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
  'deliverynotice',
  '发货通知单管理',
  'PAGE', 
  '{
    "pageId": "deliverynotice",
    "title": "{entityName}管理",
    "permission": "erp:deliverynotice:query",
    "layout": "standard",
    "apiPrefix": "/erp/engine",
    "tableName": "delivery_notice",
    "billNoField": "FBillNo"
  }', 
  '{
    "formConfig": {
      "dialogWidth": "1400px",
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
        "props": {"maxlength": 50, "clearable": true}
      },
      {
        "field": "FDocumentStatus",
        "label": "单据状态",
        "component": "select",
        "span": 6,
        "dictionary": "f_document_status",
        "props": {"placeholder": "请选择单据状态", "clearable": true},
        "defaultValue": ""
      },
      {
        "field": "FDate",
        "label": "日期",
        "component": "date-picker",
        "span": 6,
        "required": true,
        "props": {"placeholder": "选择日期", "valueFormat": "YYYY-MM-DD"}
      },
      {
        "field": "FCustomerID",
        "label": "客户",
        "component": "select",
        "span": 6,
        "dictionary": "customers",
        "required": true,
        "props": {"placeholder": "请选择客户", "filterable": true, "clearable": true}
      },
      {
        "field": "FSalesManID",
        "label": "销售员",
        "component": "select",
        "span": 6,
        "dictionary": "salespersons",
        "props": {"placeholder": "请选择销售员", "filterable": true, "clearable": true}
      },
      {
        "field": "FCarrierID",
        "label": "承运商",
        "component": "select",
        "span": 6,
        "dictionary": "suppliers",
        "props": {"placeholder": "请选择承运商", "filterable": true, "clearable": true}
      },
      {
        "field": "FReceiverID",
        "label": "收货方",
        "component": "select",
        "span": 6,
        "dictionary": "customers",
        "props": {"placeholder": "请选择收货方", "filterable": true, "clearable": true}
      },
      {
        "field": "FSettleID",
        "label": "结算方",
        "component": "select",
        "span": 6,
        "dictionary": "customers",
        "props": {"placeholder": "请选择结算方", "filterable": true, "clearable": true}
      },
      {
        "field": "FPayerID",
        "label": "付款方",
        "component": "select",
        "span": 6,
        "dictionary": "customers",
        "props": {"placeholder": "请选择付款方", "filterable": true, "clearable": true}
      },
      {
        "field": "FBillTypeID",
        "label": "单据类型",
        "component": "select",
        "span": 6,
        "dictionary": "bill_type",
        "required": true,
        "props": {"placeholder": "请选择单据类型", "filterable": true, "clearable": true}
      },
      {
        "field": "FRECEIPTCONDITIONID",
        "label": "收款条件",
        "component": "select",
        "span": 6,
        "dictionary": "payment_clause",
        "props": {"placeholder": "请选择收款条件", "filterable": true, "clearable": true}
      },
      {
        "field": "FHeadDeliveryWay",
        "label": "交货方式",
        "component": "select",
        "span": 6,
        "dictionary": "delivery_way",
        "props": {"placeholder": "请选择交货方式", "clearable": true}
      },
      {
        "field": "FReceiveAddress",
        "label": "收货方地址",
        "component": "input",
        "span": 12,
        "props": {"maxlength": 255, "clearable": true}
      },
      {
        "field": "FHeadLocId",
        "label": "交货地点",
        "component": "select",
        "span": 6,
        "dictionary": "location",
        "props": {"placeholder": "请选择交货地点", "filterable": true, "clearable": true}
      },
      {
        "field": "FLocalCurrID",
        "label": "本位币",
        "component": "select",
        "span": 6,
        "dictionary": "currency",
        "props": {"placeholder": "请选择本位币", "clearable": true, "filterable": true}
      },
      {
        "field": "FExchangeTypeID",
        "label": "汇率类型",
        "component": "select",
        "span": 6,
        "dictionary": "exchange_type",
        "props": {"placeholder": "请选择汇率类型", "clearable": true}
      },
      {
        "field": "FExchangeRate",
        "label": "汇率",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 6, "step": 0.000001, "controlsPosition": "right"}
      },
      {
        "field": "FSettleTypeID",
        "label": "结算方式",
        "component": "select",
        "span": 6,
        "dictionary": "settle_type",
        "props": {"placeholder": "请选择结算方式", "clearable": true}
      },
      {
        "field": "FSettleCurrID",
        "label": "结算币别",
        "component": "select",
        "span": 6,
        "dictionary": "currency",
        "required": true,
        "props": {"placeholder": "请选择结算币别", "clearable": true, "filterable": true}
      },
      {
        "field": "FBillTaxAmount",
        "label": "税额",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"},
        "readonly": true
      },
      {
        "field": "FBillAmount",
        "label": "金额",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"},
        "readonly": true
      },
      {
        "field": "FBillAllAmount",
        "label": "价税合计",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"},
        "readonly": true
      },
      {
        "field": "FIsIncludedTax",
        "label": "是否含税",
        "component": "select",
        "span": 6,
        "options": [{"label": "是", "value": 1}, {"label": "否", "value": 0}],
        "componentProps": {"placeholder": "请选择是否含税", "clearable": true},
        "defaultValue": 1
      },
      {
        "field": "FAllDisCount",
        "label": "整单折扣额",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"}
      }
    ]
  }', 
  '{
    "tableName": "delivery_notice",
    "primaryKey": "id",
    "orderBy": [
      {"field": "FDate", "direction": "DESC"}
    ],
    "columns": [
      {"type": "selection", "width": 55, "fixed": "left", "resizable": false},
      {"type": "expand", "width": 100, "fixed": "left", "resizable": false, "label": "详情"},
      {"prop": "FBillNo", "label": "单据编号", "width": 150, "fixed": "left", "align": "left", "visible": true, "resizable": true},
      {"prop": "FDocumentStatus", "label": "单据状态", "width": 120, "align": "center", "visible": true, "renderType": "tag", "dictionary": "f_document_status"},
      {"prop": "FDate", "label": "日期", "width": 140, "align": "center", "visible": true, "renderType": "date", "format": "YYYY-MM-DD"},
      {"prop": "FCustomerID", "label": "客户", "width": 150, "align": "left", "visible": true, "renderType": "text", "dictionary": "customers"},
      {"prop": "FSalesManID", "label": "销售员", "width": 120, "align": "left", "visible": true, "renderType": "text", "dictionary": "salespersons"},
      {"prop": "FCarrierID", "label": "承运商", "width": 120, "align": "left", "visible": true, "renderType": "text", "dictionary": "suppliers"},
      {"prop": "FBillAmount", "label": "金额", "width": 120, "align": "right", "visible": true, "renderType": "currency", "precision": 2},
      {"prop": "FBillTaxAmount", "label": "税额", "width": 100, "align": "right", "visible": true, "renderType": "currency", "precision": 2},
      {"prop": "FBillAllAmount", "label": "价税合计", "width": 120, "align": "right", "visible": true, "renderType": "currency", "precision": 2},
      {"prop": "FCreateDate", "label": "创建时间", "width": 160, "align": "center", "visible": true, "renderType": "datetime", "format": "YYYY-MM-DD HH:mm:ss"},
      {"prop": "FCreatorId", "label": "创建人", "width": 100, "align": "left", "visible": true, "renderType": "text", "dictionary": "users"}
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
        "field": "FCustomerID",
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
        "field": "FSalesManID",
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
        "field": "FDocumentStatus",
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
      {"key": "add", "label": "新增", "icon": "Plus", "permission": "erp:deliverynotice:add", "type": "primary", "position": "left", "handler": "handleAdd"},
      {"key": "edit", "label": "修改", "icon": "Edit", "permission": "erp:deliverynotice:edit", "type": "success", "position": "left", "disabled": "single", "handler": "handleUpdate"},
      {"key": "delete", "label": "删除", "icon": "Delete", "permission": "erp:deliverynotice:remove", "type": "danger", "position": "left", "disabled": "multiple", "handler": "handleDelete", "confirm": "是否确认删除选中的 {count} 条数据？"},
      {"key": "audit", "label": "审核", "icon": "CircleCheck", "permission": "erp:deliverynotice:audit", "type": "success", "position": "left", "disabled": "multiple", "handler": "handleAudit", "confirm": "是否确认审核选中的 {count} 条数据？"},
      {"key": "unAudit", "label": "反审核", "icon": "Close", "permission": "erp:deliverynotice:unAudit", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleUnAudit", "confirm": "是否确认反审核选中的 {count} 条数据？"},
      {"key": "push", "label": "下推", "icon": "Download", "permission": "erp:deliverynotice:push", "type": "info", "position": "left", "disabled": "single", "handler": "handleOpenPushDialog"},
      {"key": "export", "label": "导出", "icon": "Download", "permission": "erp:deliverynotice:export", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleExport"},
      {"key": "columnSetting", "label": "列设置", "icon": "Setting", "type": "info", "position": "right", "handler": "openColumnSetting"}
    ],
    "row": []
  }', 
  '{
    "baseUrl": "/api/deliverynotice",
    "methods": {
      "list": {
        "url": "/list",
        "method": "GET",
        "description": "查询发货通知单列表"
      },
      "get": {
        "url": "/{id}",
        "method": "GET",
        "description": "获取发货通知单详情"
      },
      "add": {
        "url": "/add",
        "method": "POST",
        "description": "新增发货通知单"
      },
      "update": {
        "url": "/update",
        "method": "PUT",
        "description": "修改发货通知单"
      },
      "delete": {
        "url": "/delete",
        "method": "DELETE",
        "description": "删除发货通知单"
      },
      "entry": {
        "url": "/entry/{billNo}",
        "method": "GET",
        "description": "获取发货通知单明细"
      },
      "audit": {
        "url": "/audit",
        "method": "POST",
        "description": "审核发货通知单"
      },
      "unAudit": {
        "url": "/unAudit",
        "method": "POST",
        "description": "反审核发货通知单"
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
      "salespersons": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/salespersons",
          "useGlobalCache": true,
          "cacheKey": "salespersons_dict",
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
      "bill_type": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/bill_type",
          "useGlobalCache": true,
          "cacheKey": "bill_type_dict",
          "cacheTTL": 86400000
        }
      },
      "payment_clause": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/payment_clause",
          "useGlobalCache": true,
          "cacheKey": "payment_clause_dict",
          "cacheTTL": 86400000
        }
      },
      "delivery_way": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/delivery_way",
          "useGlobalCache": true,
          "cacheKey": "delivery_way_dict",
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
      "exchange_type": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/exchange_type",
          "useGlobalCache": true,
          "cacheKey": "exchange_type_dict",
          "cacheTTL": 86400000
        }
      },
      "settle_type": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/settle_type",
          "useGlobalCache": true,
          "cacheKey": "settle_type_dict",
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
    "entityName": "发货通知单",
    "entityNameSingular": "通知单",
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
          "label": "发货通知单明细",
          "icon": "Document",
          "type": "table",
          "dataField": "entryList",
          "tableName": "delivery_notice_entry",
          "relationConfig": {
            "masterTable": "delivery_notice",
            "masterField": "FBillNo",
            "detailTable": "delivery_notice_entry",
            "detailField": "delivery_notice_no",
            "operator": "eq"
          },
          "queryConfig": {
            "enabled": true,
            "defaultConditions": [
              {
                "field": "delivery_notice_no",
                "operator": "eq",
                "value": "${FBillNo}",
                "description": "按单据编号查询明细 (delivery_notice.FBillNo = delivery_notice_entry.delivery_notice_no)"
              }
            ],
            "defaultOrderBy": [{"field": "FMaterialID", "direction": "ASC"}]
          },
          "table": {
            "border": true,
            "stripe": true,
            "maxHeight": "500",
            "showOverflowTooltip": true,
            "columns": [
              {"prop": "FMaterialID", "label": "物料编码", "width": 120, "align": "center", "sortable": true},
              {"prop": "FMaterialName", "label": "物料名称", "width": 180, "align": "left", "showOverflowTooltip": true, "sortable": true},
              {"prop": "FMateriaModel", "label": "规格型号", "width": 150, "align": "left", "showOverflowTooltip": true},
              {"prop": "FMateriaType", "label": "物料类别", "width": 100, "align": "center"},
              {"prop": "FUnitID", "label": "销售单位", "width": 80, "align": "center"},
              {"prop": "FQty", "label": "销售数量", "width": 100, "align": "right", "renderType": "number", "sortable": true, "precision": 4},
              {"prop": "FStockID", "label": "出货仓库", "width": 120, "align": "left"},
              {"prop": "FBaseUnitID", "label": "基本单位", "width": 80, "align": "center"},
              {"prop": "FBaseUnitQty", "label": "销售基本数量", "width": 100, "align": "right", "renderType": "number", "precision": 4},
              {"prop": "FDeliveryLoc", "label": "交货地点", "width": 120, "align": "left"},
              {"prop": "FOrderNo", "label": "订单单号", "width": 150, "align": "left"},
              {"prop": "FLot", "label": "批号", "width": 120, "align": "center"},
              {"prop": "FIsFree", "label": "是否赠品", "width": 80, "align": "center", "renderType": "tag"},
              {"prop": "FPrice", "label": "单价", "width": 100, "align": "right", "renderType": "currency", "precision": 6, "sortable": true},
              {"prop": "FTaxPrice", "label": "含税单价", "width": 100, "align": "right", "renderType": "currency", "precision": 6},
              {"prop": "F_mz", "label": "毛重", "width": 80, "align": "right", "renderType": "number", "precision": 2},
              {"prop": "F_jz", "label": "净重", "width": 80, "align": "right", "renderType": "number", "precision": 2},
              {"prop": "F_kpdj", "label": "开票单价", "width": 100, "align": "right", "renderType": "currency", "precision": 6},
              {"prop": "F_zwbgpm", "label": "中文报关品名", "width": 150, "align": "left"},
              {"prop": "F_ywbgpm", "label": "英文报关品名", "width": 150, "align": "left"},
              {"prop": "F_sfbg", "label": "是否报关", "width": 80, "align": "center", "renderType": "tag"},
              {"prop": "F_tcblNEW", "label": "提成比例", "width": 80, "align": "right", "renderType": "percent", "precision": 2}
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
  '发货通知单配置（v1.0 初始版本）'
);

-- ============================================
-- 第三步：验证导入结果
-- ============================================

SELECT '========================================' AS '';
SELECT ' 发货通知单配置导入成功！' AS '';
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
WHERE module_code = 'deliverynotice';

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
WHERE module_code = 'deliverynotice';

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
