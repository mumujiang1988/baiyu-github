-- ============================================
-- ERP 配置 JSON 强制拆分方案 - 销售订单模块导入 SQL
-- 版本：v2.0 (生产交付标准版)
-- 日期：2026-03-26
-- 说明：导入销售订单页面配置数据（5 字段强制拆分）
-- 适用范围：生产环境部署
-- ============================================

USE test;

-- ============================================
-- 第一步：清理旧数据（级联删除）
-- ============================================

SET FOREIGN_KEY_CHECKS = 0;

-- 级联删除销售订单相关的所有配置
DELETE FROM erp_page_config_history WHERE module_code = 'saleorder';
DELETE FROM erp_approval_history WHERE module_code = 'saleorder';
DELETE FROM erp_approval_flow WHERE module_code = 'saleorder';
DELETE FROM erp_push_relation WHERE source_module = 'saleorder' OR target_module = 'saleorder';
DELETE FROM erp_page_config WHERE module_code = 'saleorder';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第二步：插入销售订单页面配置数据
-- ============================================

-- 销售订单页面配置（5 字段强制拆分）
INSERT INTO `erp_page_config` (
  `module_code`,
  `config_name`,
  `config_type`,
  `page_config`,
  `form_config`,
  `table_config`,
  `dict_config`,
  `business_config`,
  `version`,
  `status`,
  `is_public`,
  `create_by`,
  `remark`
) VALUES (
  'saleorder',
  '销售订单管理',
  'PAGE',
  
  -- page_config: 页面基础配置 (统一为 JSON 字符串格式)
  '{
    "pageId": "saleorder",
    "pageName": "销售订单管理",
    "permission": "k3:saleorder:query",
    "layout": "standard",
    "apiPrefix": "/erp/engine"
  }',
  
  -- form_config: 从 form.json 读取
  '{
    "formConfig": {
      "dialogWidth": "1400px",
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
        "label": "销售合同日期",
        "component": "date",
        "span": 6,
        "required": true,
        "rules": [{"required": true, "message": "销售合同日期不能为空", "trigger": "change"}],
        "props": {"placeholder": "选择日期", "valueFormat": "YYYY-MM-DD"}
      },
      {
        "field": "fcustid",
        "label": "客户编码",
        "component": "select",
        "span": 6,
        "dictionary": "customers",
        "required": true,
        "props": {"placeholder": "请选择客户", "filterable": true, "clearable": true}
      },
      {
        "field": "f_ora_baseproperty",
        "label": "客户简称",
        "component": "input",
        "span": 6,
        "required": true,
        "rules": [{"required": true, "message": "客户简称不能为空", "trigger": "blur"}],
        "props": {"placeholder": "请输入客户简称", "maxlength": 200},
        "defaultValue": ""
      },
      {
        "field": "f_khhth",
        "label": "客户合同号",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 100, "clearable": true}
      },
      {
        "field": "f_kglxr",
        "label": "客户联系人",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 100, "clearable": true, "readonly": true}
      },
      {
        "field": "f_cty_baseproperty1",
        "label": "客户邮箱",
        "component": "input",
        "span": 6,
        "rules": [{"type": "email", "message": "请输入正确的邮箱地址", "trigger": ["blur", "change"]}],
        "props": {"maxlength": 100, "clearable": true, "readonly": true}
      },
      {
        "field": "fsettlecurrid",
        "label": "结算币别",
        "comment": "用于与金蝶 K3 系统集成的结算币种标识",
        "component": "select",
        "span": 6,
        "dictionary": "currency",
        "props": {"placeholder": "请选择结算币别", "clearable": true, "filterable": true},
        "defaultValue": "人民币"
      },
      {
        "field": "f_tcbl",
        "label": "提成比例",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "max": 100, "precision": 2, "step": 0.1, "controlsPosition": "right"}
      },
      {
        "field": "fisincludedtax",
        "label": "是否含税",
        "component": "select",
        "span": 6,
        "options": [{"label": "是", "value": 1}, {"label": "否", "value": 0}],
        "componentProps": {"placeholder": "请选择是否含税", "clearable": true},
        "defaultValue": 1
      },
      {
        "field": "f_sfbg",
        "label": "是否报关",
        "component": "select",
        "span": 6,
        "options": [{"label": "是", "value": 1}, {"label": "否", "value": 0}],
        "componentProps": {"placeholder": "请选择是否报关", "clearable": true},
        "defaultValue": 1
      },
      {
        "field": "frecconditionid",
        "label": "收款条件",
        "component": "select",
        "span": 6,
        "dictionary": "paymentTerms",
        "componentProps": {"placeholder": "请选择收款条件", "clearable": true, "filterable": true}
      },
      {
        "field": "fbillamount",
        "label": "订单金额",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"}
      },
      {
        "field": "fbilltaxamount",
        "label": "税额",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"}
      },
      {
        "field": "flocalcurrid",
        "label": "本位币",
        "comment": "公司内部核算使用的本位货币，区别于结算币别",
        "component": "select",
        "span": 6,
        "dictionary": "currency",
        "componentProps": {"placeholder": "请选择本位币", "clearable": true, "filterable": true}
      },
      {
        "field": "fsalerid",
        "label": "销售员",
        "component": "select",
        "span": 6,
        "dictionary": "salespersons",
        "componentProps": {"placeholder": "请选择销售员", "clearable": true, "filterable": true}
      },
      {
        "field": "f_lrl",
        "label": "毛净利润率",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "max": 100, "precision": 2, "step": 0.1, "controlsPosition": "right"}
      },
      {
        "field": "f_jlrl",
        "label": "净利润率",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "max": 100, "precision": 2, "step": 0.1, "controlsPosition": "right"}
      },
      {
        "field": "fstate",
        "label": "国家",
        "component": "select",
        "span": 6,
        "dictionary": "nation",
        "componentProps": {"placeholder": "请选择国家", "clearable": true, "filterable": true},
        "defaultValue": ""
      },
      {
        "field": "f_gj",
        "label": "抵运国家",
        "component": "select",
        "span": 6,
        "dictionary": "nation",
        "componentProps": {"placeholder": "请选择抵运国家", "clearable": true, "filterable": true},
        "defaultValue": ""
      },
      {
        "field": "f_myfs",
        "label": "贸易方式",
        "component": "select",
        "span": 6,
        "dictionary": "tradeType",
        "componentProps": {"placeholder": "请选择贸易方式", "clearable": true, "filterable": true},
        "defaultValue": "一般贸易"
      }
    ]
  }',
  
  -- table_config: 从 table.json 读取
  '{
    "tableName": "t_sale_order",
    "primaryKey": "id",
    "queryBuilder": {
      "enabled": true,
      "fields": [
        {
          "field": "FDate",
          "label": "日期区间",
          "component": "daterange",
          "op": "between",
          "props": {
            "startPlaceholder": "开始日期",
            "endPlaceholder": "结束日期",
            "valueFormat": "YYYY-MM-DD",
            "style": {"width": "240px"}
          },
          "defaultValue": "currentMonth"
        },
        {
          "field": "FBillNo",
          "label": "单据编号",
          "component": "input",
          "op": "right_like",
          "props": {
            "placeholder": "输入单据编号",
            "clearable": true,
            "prefixIcon": "Search",
            "style": {"width": "180px"}
          }
        },
        {
          "field": "F_ora_BaseProperty",
          "label": "客户简称",
          "component": "input",
          "op": "like",
          "props": {
            "placeholder": "输入客户简称",
            "clearable": true,
            "prefixIcon": "User",
            "style": {"width": "150px"}
          }
        },
        {
          "field": "FSalerId",
          "label": "销售员",
          "component": "select",
          "op": "eq",
          "dictionary": "salespersons",
          "props": {
            "placeholder": "选择销售员",
            "clearable": true,
            "filterable": true,
            "style": {"width": "120px"}
          }
        },
        {
          "field": "orderStatus",
          "label": "订单状态",
          "component": "select",
          "op": "eq",
          "dictionary": "orderStatus",
          "props": {
            "placeholder": "选择状态",
            "clearable": true,
            "style": {"width": "100px"}
          }
        },
        {
          "field": "FDocumentStatus",
          "label": "单据状态",
          "component": "select",
          "op": "eq",
          "dictionary": "documentStatus",
          "props": {
            "placeholder": "选择单据状态",
            "clearable": true,
            "filterable": true,
            "style": {"width": "120px"}
          }
        }
      ],
      "defaultConditions": [
        {"field": "FDate", "operator": "between", "value": ["${startDate}", "${endDate}"], "description": "日期范围查询"},
        {"field": "FBillNo", "operator": "right_like", "value": "${billNo}", "description": "单据编号右模糊查询"},
        {"field": "F_ora_BaseProperty", "operator": "like", "value": "${customerName}", "description": "客户简称模糊查询"},
        {"field": "FSalerId", "operator": "eq", "value": "${salerId}", "description": "销售员精确匹配"},
        {"field": "orderStatus", "operator": "eq", "value": "${status}", "description": "订单状态精确匹配"},
        {"field": "FDocumentStatus", "operator": "eq", "value": "${docStatus}", "description": "单据状态精确匹配"}
      ],
      "defaultOrderBy": [{"field": "FCreateDate", "direction": "DESC"}]
    },
    "columns": [
      {"type": "selection", "width": 55, "fixed": "left", "resizable": false},
      {"type": "expand", "width": 100, "fixed": "left", "resizable": false, "label": "详情"},
      {"prop": "FBillNo", "label": "单据编号", "width": 150, "fixed": "left", "align": "left", "visible": true, "resizable": true, "renderType": "text"},
      {"prop": "F_ora_BaseProperty", "label": "客户简称", "width": 150, "fixed": "left", "align": "left", "visible": true, "resizable": true},
      {"prop": "orderStatus", "label": "订单状态", "width": 120, "align": "center", "visible": true, "renderType": "tag", "dictionary": "orderStatus"},
      {"prop": "FDocumentStatus", "label": "单据状态", "width": 140, "align": "center", "visible": true, "renderType": "tag", "dictionary": "documentStatus"},
      {"prop": "FDate", "label": "销售合同日期", "width": 140, "align": "center", "visible": true, "renderType": "date", "format": "YYYY-MM-DD"},
      {"prop": "FSalerId", "label": "销售员", "width": 120, "align": "left", "visible": true, "renderType": "text", "dictionary": "salespersons"},
      {"prop": "FBillAmount", "label": "订单金额", "width": 140, "align": "right", "visible": true, "renderType": "currency", "precision": 2},
      {"prop": "FBillTaxAmount", "label": "税额", "width": 100, "align": "right", "visible": true, "renderType": "currency", "precision": 2},
      {"prop": "FSettleCurrId", "label": "结算币别", "width": 140, "align": "center", "visible": true, "renderType": "text", "dictionary": "currency"},
      {"prop": "FCreateDate", "label": "创建时间", "width": 160, "align": "center", "visible": true, "renderType": "datetime", "format": "YYYY-MM-DD HH:mm:ss"},
      {"prop": "FCreatorId", "label": "创建人", "width": 100, "align": "left", "visible": true, "renderType": "text"}
    ],
    "pagination": {
      "defaultPageSize": 10,
      "pageSizeOptions": [10, 20, 50, 100]
    }
  }',
  
  -- dict_config: 从 dict.json 读取
  '{
    "dicts": [
      {
        "dictKey": "salespersons",
        "dictType": "dynamic",
        "table": "sys_user",
        "conditions": [{"field": "deleted", "operator": "isNull"}],
        "orderBy": [{"field": "nick_name", "direction": "ASC"}],
        "fieldMapping": {"valueField": "user_id", "labelField": "nick_name"},
        "config": {"api": "/erp/engine/dictionary/salespersons/data?moduleCode={moduleCode}", "labelField": "nickName", "valueField": "fseller", "ttl": 600000},
        "cacheable": true,
        "cacheTTL": 600000
      },
      {
        "dictKey": "currency",
        "dictType": "dynamic",
        "table": "bymaterial_dictionary",
        "conditions": [{"field": "category", "operator": "eq", "value": "currency"}, {"field": "deleted", "operator": "isNull"}],
        "orderBy": [{"field": "name", "direction": "ASC"}],
        "fieldMapping": {"valueField": "kingdee", "labelField": "name"},
        "config": {"api": "/erp/engine/dictionary/currency/data?moduleCode={moduleCode}", "labelField": "name", "valueField": "kingdee", "ttl": 600000},
        "cacheable": true,
        "cacheTTL": 600000
      },
      {
        "dictKey": "paymentTerms",
        "dictType": "dynamic",
        "table": "bymaterial_dictionary",
        "conditions": [{"field": "category", "operator": "eq", "value": "payment_clause"}, {"field": "deleted", "operator": "isNull"}],
        "orderBy": [{"field": "name", "direction": "ASC"}],
        "fieldMapping": {"valueField": "kingdee", "labelField": "name"},
        "config": {"api": "/erp/engine/dictionary/paymentTerms/data?moduleCode={moduleCode}", "labelField": "name", "valueField": "kingdee", "ttl": 600000},
        "cacheable": true,
        "cacheTTL": 600000
      },
      {
        "dictKey": "nation",
        "dictType": "remote",
        "config": {"searchApi": "/erp/engine/dictionary/search/nation?keyword={keyword}&moduleCode={moduleCode}", "minKeywordLength": 1, "debounce": 300}
      },
      {
        "dictKey": "tradeType",
        "dictType": "dynamic",
        "table": "bymaterial_dictionary",
        "conditions": [{"field": "category", "operator": "eq", "value": "trade_way"}, {"field": "deleted", "operator": "isNull"}],
        "orderBy": [{"field": "name", "direction": "ASC"}],
        "fieldMapping": {"valueField": "kingdee", "labelField": "name"},
        "config": {"api": "/erp/engine/dictionary/tradeType/data?moduleCode={moduleCode}", "labelField": "name", "valueField": "kingdee", "ttl": 600000},
        "cacheable": true,
        "cacheTTL": 600000
      },
      {
        "dictKey": "customers",
        "dictType": "dynamic",
        "table": "bd_customer",
        "conditions": [{"field": "deleted", "operator": "isNull"}],
        "orderBy": [{"field": "fname", "direction": "ASC"}],
        "fieldMapping": {"valueField": "fnumber", "labelField": "fname"},
        "config": {"api": "/erp/engine/dictionary/customers/data?moduleCode={moduleCode}", "labelField": "fname", "valueField": "fnumber", "ttl": 300000},
        "cacheable": true,
        "cacheTTL": 300000
      },
      {
        "dictKey": "materials",
        "dictType": "dynamic",
        "table": "by_material",
        "conditions": [{"field": "deleted", "operator": "isNull"}],
        "orderBy": [{"field": "name", "direction": "ASC"}],
        "fieldMapping": {"valueField": "materialId", "labelField": "name"},
        "config": {"api": "/erp/engine/dictionary/materials/data?moduleCode={moduleCode}", "labelField": "materialName", "valueField": "materialId", "ttl": 300000},
        "cacheable": true,
        "cacheTTL": 300000
      },
      {
        "dictKey": "productCategory",
        "dictType": "dynamic",
        "table": "bymaterial_dictionary",
        "conditions": [{"field": "category", "operator": "eq", "value": "product_category"}, {"field": "deleted", "operator": "isNull"}],
        "orderBy": [{"field": "name", "direction": "ASC"}],
        "fieldMapping": {"valueField": "kingdee", "labelField": "name"},
        "config": {"api": "/erp/engine/dictionary/productCategory/data?moduleCode={moduleCode}", "labelField": "name", "valueField": "kingdee", "ttl": 600000},
        "cacheable": true,
        "cacheTTL": 600000
      },
      {
        "dictKey": "orderStatus",
        "dictType": "static",
        "data": [{"label": "未关闭", "value": "A", "type": "success"}, {"label": "已关闭", "value": "B", "type": "info"}, {"label": "业务终止", "value": "C", "type": "danger"}]
      },
      {
        "dictKey": "documentStatus",
        "dictType": "static",
        "data": [{"label": "暂存", "value": "Z", "type": "info"}, {"label": "创建", "value": "A", "type": "success"}, {"label": "审核中", "value": "B", "type": "warning"}, {"label": "已审核", "value": "C", "type": "success"}, {"label": "重新审核", "value": "D", "type": "primary"}]
      }
    ],
    "globalCacheSettings": {
      "enabled": true,
      "defaultTTL": 300000
    }
  }',
  
  -- business_config: 从 config.json 读取
  '{
    "buttons": [
      {"key": "add", "label": "新增", "icon": "Plus", "permission": "k3:saleorder:add", "type": "primary", "position": "left"},
      {"key": "edit", "label": "修改", "icon": "Edit", "permission": "k3:saleorder:edit", "type": "success", "position": "left", "disabled": "single"},
      {"key": "delete", "label": "删除", "icon": "Delete", "permission": "k3:saleorder:remove", "type": "danger", "position": "left", "disabled": "multiple", "confirm": "是否确认删除选中的 {count} 条数据？"},
      {"key": "audit", "label": "审核", "icon": "CircleCheck", "permission": "k3:saleorder:audit", "type": "success", "position": "left", "disabled": "multiple", "confirm": "是否确认审核选中的 {count} 条数据？"},
      {"key": "unAudit", "label": "反审核", "icon": "Close", "permission": "k3:saleorder:unAudit", "type": "warning", "position": "left", "disabled": "multiple", "confirm": "是否确认反审核选中的 {count} 条数据？"},
      {"key": "push", "label": "下推", "icon": "Download", "permission": "k3:saleorder:push", "type": "info", "position": "left", "disabled": "single"},
      {"key": "export", "label": "导出", "icon": "Download", "permission": "k3:saleorder:export", "type": "warning", "position": "left", "disabled": "multiple"},
      {"key": "columnSetting", "label": "列设置", "icon": "Setting", "type": "info", "position": "right"}
    ],
    "messages": {
      "selectOne": "请选择一条数据",
      "confirmDelete": "是否确认删除选中的 {count} 条数据？",
      "confirmAudit": "是否确认审核选中的 {count} 条数据？",
      "confirmUnAudit": "是否确认反审核选中的 {count} 条数据？",
      "success": {"add": "新增成功", "edit": "修改成功", "delete": "删除成功", "audit": "审核成功", "unAudit": "反审核成功"},
      "error": {"load": "加载数据失败", "save": "保存失败", "delete": "删除失败", "audit": "审核失败", "unAudit": "反审核失败"}
    },
    "entityName": "销售订单",
    "entityNameSingular": "订单",
    "dialogTitle": {"add": "新增{entityName}", "edit": "修改{entityName}"},
    "drawerTitle": "{entityName}详情 - {billNo}"
  }',
  
  1,
  '1',
  '0',
  'admin',
  '销售订单配置（JSON 强制拆分版）'
);

-- ============================================
-- 第三步：验证导入结果
-- ============================================

SELECT '========================================' AS '';
SELECT '✅ 销售订单配置导入成功！' AS '';
SELECT '========================================' AS '';

SELECT 
  config_id,
  module_code,
  config_name,
  version,
  status,
  JSON_LENGTH(page_config) AS page_fields,
  JSON_LENGTH(form_config) AS form_fields,
  JSON_LENGTH(table_config) AS table_columns,
  JSON_LENGTH(dict_config) AS dictionaries,
  JSON_LENGTH(business_config) AS buttons
FROM erp_page_config
WHERE module_code = 'saleorder';

SELECT '========================================' AS '';
SELECT '📊 配置统计信息：' AS '';
SELECT '========================================' AS '';
SELECT 
  CONCAT('页面配置字段数：', JSON_LENGTH(JSON_EXTRACT(form_config, '$.fields'))) AS form_fields_count,
  CONCAT('表格列数：', JSON_LENGTH(JSON_EXTRACT(table_config, '$.columns'))) AS table_columns_count,
  CONCAT('字典数量：', JSON_LENGTH(JSON_EXTRACT(dict_config, '$.dicts'))) AS dicts_count,
  CONCAT('按钮数量：', JSON_LENGTH(JSON_EXTRACT(business_config, '$.buttons'))) AS buttons_count
FROM erp_page_config
WHERE module_code = 'saleorder';

SELECT '========================================' AS '';
SELECT '下一步操作：' AS '';
SELECT '  后续将添加：' AS note1;
SELECT '    - 审批流程配置（erp_approval_flow）' AS note2;
SELECT '    - 下推关系配置（erp_push_relation）' AS note3;
SELECT '  请继续执行相应的导入脚本' AS note4;
SELECT '========================================' AS '';
SELECT '完成！' AS '';
SELECT '========================================' AS '';
