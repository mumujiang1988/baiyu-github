-- ============================================
-- ERP 配置 JSON 强制拆分方案 - 应付单模块导入 SQL
-- 版本：v1.0 (9字段拆分 + API配置版)
-- 日期：2026-04-13
-- 说明：导入应付单页面配置数据（9 字段强制拆分 + API配置）
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

-- 级联删除应付单相关的所有配置
DELETE FROM erp_page_config_history WHERE module_code = 'payable';
DELETE FROM erp_approval_history WHERE module_code = 'payable';
DELETE FROM erp_approval_flow WHERE module_code = 'payable';
DELETE FROM erp_push_relation WHERE source_module = 'payable' OR target_module = 'payable';
DELETE FROM erp_page_config WHERE module_code = 'payable';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第二步：插入应付单页面配置数据
-- ============================================

-- 应付单页面配置（9 字段强制拆分，包含 api_config、search_config 和 action_config）
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
  'payable',
  '应付单管理',
  'PAGE', 
  '{
    "pageId": "payable",
    "title": "{entityName}管理",
    "permission": "erp:payable:query",
    "layout": "standard",
    "apiPrefix": "/erp/engine",
    "tableName": "f_scp_payable",
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
        "props": {"maxlength": 40, "clearable": true}
      },
      {
        "field": "f_date",
        "label": "业务日期",
        "component": "date-picker",
        "span": 6,
        "required": true,
        "props": {"placeholder": "选择日期", "valueFormat": "YYYY-MM-DD"}
      },
      {
        "field": "f_supplier_id",
        "label": "供应商",
        "component": "select",
        "span": 6,
        "dictionary": "suppliers",
        "required": true,
        "props": {"placeholder": "请选择供应商", "filterable": true, "clearable": true}
      },
      {
        "field": "f_settleorg_id",
        "label": "结算组织",
        "component": "select",
        "span": 6,
        "dictionary": "organizations",
        "props": {"placeholder": "请选择结算组织", "filterable": true, "clearable": true}
      },
      {
        "field": "f_purchaseorg_id",
        "label": "采购组织",
        "component": "select",
        "span": 6,
        "dictionary": "organizations",
        "props": {"placeholder": "请选择采购组织", "filterable": true, "clearable": true}
      },
      {
        "field": "f_payorg_id",
        "label": "付款组织",
        "component": "select",
        "span": 6,
        "dictionary": "organizations",
        "props": {"placeholder": "请选择付款组织", "filterable": true, "clearable": true}
      },
      {
        "field": "f_currency_id",
        "label": "币别",
        "component": "select",
        "span": 6,
        "dictionary": "currency",
        "props": {"placeholder": "请选择币别", "clearable": true, "filterable": true},
        "defaultValue": "1"
      },
      {
        "field": "f_allamountfor",
        "label": "价税合计",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"},
        "readonly": true
      },
      {
        "field": "f_enddate_h",
        "label": "到期日",
        "component": "date-picker",
        "span": 6,
        "props": {"placeholder": "选择到期日", "valueFormat": "YYYY-MM-DD"}
      },
      {
        "field": "f_pay_conditon",
        "label": "付款条件",
        "component": "select",
        "span": 6,
        "dictionary": "payment_clause",
        "componentProps": {"placeholder": "请选择付款条件", "clearable": true, "filterable": true}
      },
      {
        "field": "f_set_account_type",
        "label": "立账类型",
        "component": "select",
        "span": 6,
        "options": [{"label": "暂估", "value": "1"}, {"label": "正式", "value": "2"}],
        "componentProps": {"placeholder": "请选择立账类型", "clearable": true}
      },
      {
        "field": "f_ispriceexcludetax",
        "label": "价外税",
        "component": "select",
        "span": 6,
        "options": [{"label": "是", "value": "1"}, {"label": "否", "value": "0"}],
        "componentProps": {"placeholder": "请选择是否价外税", "clearable": true},
        "defaultValue": "0"
      },
      {
        "field": "f_istax",
        "label": "按含税单价录入",
        "component": "select",
        "span": 6,
        "options": [{"label": "是", "value": "1"}, {"label": "否", "value": "0"}],
        "componentProps": {"placeholder": "请选择", "clearable": true},
        "defaultValue": "1"
      },
      {
        "field": "f_rchaser_id",
        "label": "采购员",
        "component": "select",
        "span": 6,
        "dictionary": "purchasers",
        "componentProps": {"placeholder": "请选择采购员", "clearable": true, "filterable": true}
      },
      {
        "field": "f_purchasedept_id",
        "label": "采购部门",
        "component": "select",
        "span": 6,
        "dictionary": "departments",
        "componentProps": {"placeholder": "请选择采购部门", "clearable": true, "filterable": true}
      },
      {
        "field": "f_notwrittenoffmountfor",
        "label": "未付款核销金额",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"},
        "readonly": true
      },
      {
        "field": "f_scpconfirm_status",
        "label": "确认状态",
        "component": "select",
        "span": 6,
        "dictionary": "confirm_status",
        "componentProps": {"placeholder": "请选择确认状态", "clearable": true}
      },
      {
        "field": "f_ap_remark",
        "label": "备注",
        "component": "textarea",
        "span": 12,
        "props": {"maxlength": 255, "rows": 3, "placeholder": "请输入备注"}
      }
    ]
  }', 
  '{
    "tableName": "f_scp_payable",
    "primaryKey": "f_id",
    "orderBy": [
      {"field": "f_date", "direction": "DESC"}
    ],
    "columns": [
      {"type": "selection", "width": 55, "fixed": "left", "resizable": false},
      {"type": "expand", "width": 100, "fixed": "left", "resizable": false, "label": "详情"},
      {"prop": "f_bill_no", "label": "单据编号", "width": 150, "fixed": "left", "align": "left", "visible": true, "resizable": true, "renderType": "text"},
      {"prop": "f_supplier_id", "label": "供应商", "width": 150, "align": "left", "visible": true, "resizable": true, "dictionary": "suppliers"},
      {"prop": "f_date", "label": "业务日期", "width": 140, "align": "center", "visible": true, "renderType": "date", "format": "YYYY-MM-DD"},
      {"prop": "f_allamountfor", "label": "价税合计", "width": 140, "align": "right", "visible": true, "renderType": "currency", "precision": 2},
      {"prop": "f_currency_id", "label": "币别", "width": 100, "align": "center", "visible": true, "renderType": "text", "dictionary": "currency"},
      {"prop": "f_enddate_h", "label": "到期日", "width": 140, "align": "center", "visible": true, "renderType": "date", "format": "YYYY-MM-DD"},
      {"prop": "f_scpconfirm_status", "label": "确认状态", "width": 120, "align": "center", "visible": true, "renderType": "tag", "dictionary": "confirm_status"},
      {"prop": "f_pay_conditon", "label": "付款条件", "width": 120, "align": "center", "visible": true, "renderType": "text", "dictionary": "payment_clause"},
      {"prop": "f_rchaser_id", "label": "采购员", "width": 120, "align": "left", "visible": true, "renderType": "text", "dictionary": "purchasers"},
      {"prop": "f_notwrittenoffmountfor", "label": "未核销金额", "width": 140, "align": "right", "visible": true, "renderType": "currency", "precision": 2},
      {"prop": "f_create_date", "label": "创建时间", "width": 160, "align": "center", "visible": true, "renderType": "datetime", "format": "YYYY-MM-DD HH:mm:ss"},
      {"prop": "f_creator_id", "label": "创建人", "width": 100, "align": "left", "visible": true, "renderType": "text", "dictionary": "users"}
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
        "label": "业务日期",
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
        "field": "f_supplier_id",
        "label": "供应商",
        "component": "select",
        "dictionary": "suppliers",
        "props": {
          "placeholder": "供应商",
          "clearable": true,
          "filterable": true,
          "style": {"width": "150px"}
        },
        "defaultValue": "",
        "queryOperator": "eq"
      },
      {
        "field": "f_scpconfirm_status",
        "label": "确认状态",
        "component": "select",
        "dictionary": "confirm_status",
        "props": {
          "placeholder": "确认状态",
          "clearable": true,
          "filterable": true,
          "style": {"width": "120px"}
        },
        "defaultValue": "",
        "queryOperator": "eq"
      },
      {
        "field": "f_rchaser_id",
        "label": "采购员",
        "component": "select",
        "dictionary": "purchasers",
        "props": {
          "placeholder": "采购员",
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
      {"key": "add", "label": "新增", "icon": "Plus", "permission": "erp:payable:add", "type": "primary", "position": "left", "handler": "handleAdd"},
      {"key": "edit", "label": "修改", "icon": "Edit", "permission": "erp:payable:edit", "type": "success", "position": "left", "disabled": "single", "handler": "handleUpdate"},
      {"key": "delete", "label": "删除", "icon": "Delete", "permission": "erp:payable:remove", "type": "danger", "position": "left", "disabled": "multiple", "handler": "handleDelete", "confirm": "是否确认删除选中的 {count} 条数据？"},
      {"key": "audit", "label": "审核", "icon": "CircleCheck", "permission": "erp:payable:audit", "type": "success", "position": "left", "disabled": "multiple", "handler": "handleAudit", "confirm": "是否确认审核选中的 {count} 条数据？"},
      {"key": "unAudit", "label": "反审核", "icon": "Close", "permission": "erp:payable:unAudit", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleUnAudit", "confirm": "是否确认反审核选中的 {count} 条数据？"},
      {"key": "confirm", "label": "确认", "icon": "Check", "permission": "erp:payable:confirm", "type": "success", "position": "left", "disabled": "multiple", "handler": "handleConfirm", "confirm": "是否确认选中的 {count} 条数据？"},
      {"key": "push", "label": "下推", "icon": "Download", "permission": "erp:payable:push", "type": "info", "position": "left", "disabled": "single", "handler": "handleOpenPushDialog"},
      {"key": "export", "label": "导出", "icon": "Download", "permission": "erp:payable:export", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleExport"},
      {"key": "columnSetting", "label": "列设置", "icon": "Setting", "type": "info", "position": "right", "handler": "openColumnSetting"}
    ],
    "row": []
  }', 
  '{
    "baseUrl": "/api/payable",
    "methods": {
      "list": {
        "url": "/list",
        "method": "GET",
        "description": "查询应付单列表"
      },
      "get": {
        "url": "/{id}",
        "method": "GET",
        "description": "获取应付单详情"
      },
      "add": {
        "url": "/add",
        "method": "POST",
        "description": "新增应付单"
      },
      "update": {
        "url": "/update",
        "method": "PUT",
        "description": "修改应付单"
      },
      "delete": {
        "url": "/delete",
        "method": "DELETE",
        "description": "删除应付单"
      },
      "entry": {
        "url": "/entry/{billNo}",
        "method": "GET",
        "description": "获取应付单明细"
      },
      "audit": {
        "url": "/audit",
        "method": "POST",
        "description": "审核应付单"
      },
      "unAudit": {
        "url": "/unAudit",
        "method": "POST",
        "description": "反审核应付单"
      },
      "confirm": {
        "url": "/confirm",
        "method": "POST",
        "description": "确认应付单"
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
      "purchasers": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/purchasers",
          "useGlobalCache": true,
          "cacheKey": "purchasers_dict",
          "cacheTTL": 86400000
        }
      },
      "materials": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/materials",
          "useGlobalCache": true,
          "cacheKey": "materials_dict",
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
      "currency": {
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/currency",
          "useGlobalCache": true,
          "cacheKey": "currency_dict",
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
      "confirmConfirm": "是否确认选中的 {count} 条数据？",
      "success": {"add": "新增成功", "edit": "修改成功", "delete": "删除成功", "audit": "审核成功", "unAudit": "反审核成功", "confirm": "确认成功"},
      "error": {"load": "加载数据失败", "save": "保存失败", "delete": "删除失败", "audit": "审核失败", "unAudit": "反审核失败", "confirm": "确认失败"}
    },
    "entityName": "应付单",
    "entityNameSingular": "应付单",
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
          "label": "应付单明细",
          "icon": "Document",
          "type": "table",
          "dataField": "entryList",
          "tableName": "f_scp_payable_detail",
          "relationConfig": { 
            "masterTable": "f_scp_payable",
            "masterField": "f_id",
            "detailTable": "f_scp_payable_detail",
            "detailField": "f_Entry_id",
            "operator": "eq"
          },
          "table": {
            "border": true,
            "stripe": true,
            "maxHeight": "500",
            "showOverflowTooltip": true,
            "columns": [
              {"prop": "f_material_id", "label": "物料编码", "width": 120, "align": "center", "sortable": true},
              {"prop": "f_material_name", "label": "物料名称", "width": 180, "align": "left", "showOverflowTooltip": true, "sortable": true},
              {"prop": "f_priceunit_id", "label": "计价单位", "width": 100, "align": "center", "sortable": true},
              {"prop": "f_basicunitqty", "label": "计价基本数量", "width": 120, "align": "right", "renderType": "number", "sortable": true, "precision": 4},
              {"prop": "f_stock_qty", "label": "库存数量", "width": 100, "align": "right", "renderType": "number", "sortable": true, "precision": 4},
              {"prop": "f_price", "label": "单价", "width": 100, "align": "right", "renderType": "currency", "precision": 2, "sortable": true},
              {"prop": "f_ftax_price", "label": "含税单价", "width": 100, "align": "right", "renderType": "currency", "precision": 2, "sortable": true},
              {"prop": "F_entrytax_rate", "label": "税率(%)", "width": 80, "align": "center", "sortable": true},
              {"prop": "f_entry_discount_rate", "label": "折扣率(%)", "width": 100, "align": "center", "sortable": true},
              {"prop": "f_duscountamountfor", "label": "折扣额", "width": 100, "align": "right", "renderType": "currency", "precision": 2},
              {"prop": "f_notaxamountfor_d", "label": "不含税金额", "width": 120, "align": "right", "renderType": "currency", "precision": 2, "sortable": true},
              {"prop": "f_taxamountfor_d", "label": "税额", "width": 100, "align": "right", "renderType": "currency", "precision": 2, "sortable": true},
              {"prop": "f_allamountfor_d", "label": "价税合计", "width": 120, "align": "right", "renderType": "currency", "precision": 2, "sortable": true},
              {"prop": "f_source_type", "label": "源单类型", "width": 100, "align": "center"},
              {"prop": "f_sourcebill_no", "label": "源单编号", "width": 150, "align": "left", "showOverflowTooltip": true},
              {"prop": "f_comment", "label": "备注", "width": 150, "align": "left", "showOverflowTooltip": true}
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
  '应付单配置（v1.0 - 基于数据库结构严格构建）'
);

-- ============================================
-- 第三步：验证导入结果
-- ============================================

SELECT '========================================' AS '';
SELECT ' 应付单配置导入成功！' AS '';
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
WHERE module_code = 'payable';

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
WHERE module_code = 'payable';

SELECT '========================================' AS '';
SELECT '✨ 配置说明：' AS '';
SELECT '========================================' AS '';
SELECT '   主表：f_scp_payable（应付单表头）' AS info1;
SELECT '   明细表：f_scp_payable_detail（应付单明细）' AS info2;
SELECT '   关联字段：主表 f_id -> 明细表 f_Entry_id' AS info3;
SELECT '   字典配置：使用统一 API /erp/engine/dict/union/*' AS info4;
SELECT '   严格遵循数据库字段定义，无虚构字段' AS info5;
SELECT '========================================' AS '';
SELECT '下一步操作：' AS '';
SELECT '  后续将添加：' AS note1;
SELECT '    - 审批流程配置（erp_approval_flow）' AS note2;
SELECT '    - 下推关系配置（erp_push_relation）' AS note3;
SELECT '  请继续执行相应的导入脚本' AS note4;
SELECT '========================================' AS '';
SELECT '完成！' AS '';
SELECT '========================================' AS '';
