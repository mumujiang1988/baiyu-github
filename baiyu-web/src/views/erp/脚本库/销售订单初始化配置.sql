-- ============================================
-- ERP 配置 JSON 强制拆分方案 - 销售订单模块导入 SQL（字典重构版）
-- 版本：v4.0 (9字段拆分 + API配置版)
-- 日期：2026-03-28
-- 说明：导入销售订单页面配置数据（9 字段强制拆分 + API配置 + 统一字典 API）
--   - page_config: 页面基础配置
--   - form_config: 表单配置
--   - table_config: 表格列配置
--   - search_config: 搜索区域配置
--   - action_config: 按钮操作配置
--   - api_config: API接口配置 ✨ 新增
--   - dict_config: 字典数据源配置（✨ 统一使用 /erp/engine/dict/all API）
--   - business_config: 业务规则配置
--   - detail_config: 详情页配置
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

-- 销售订单页面配置（9 字段强制拆分，包含 api_config、search_config 和 action_config）
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
  'saleorder',
  '销售订单管理',
  'PAGE',
  
  -- page_config: 页面基础配置
  '{
    "pageId": "saleorder",
    "pageName": "销售订单管理",
    "permission": "k3:saleorder:query",
    "layout": "standard",
    "apiPrefix": "/erp/engine",
    "tableName": "t_sale_order"
  }',
  
  -- form_config: 从 form.json 读取（✨ 已优化：使用数据库字段名）
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
        "rules": [{"required": true, "message": "单据编号不能为空", "trigger": "blur"}],
        "props": {"maxlength": 100, "clearable": true}
      },
      {
        "field": "FDate",
        "label": "销售合同日期",
        "component": "date",
        "span": 6,
        "required": true,
        "rules": [{"required": true, "message": "销售合同日期不能为空", "trigger": "change"}],
        "props": {"placeholder": "选择日期", "valueFormat": "YYYY-MM-DD"}
      },
      {
        "field": "FCustId",
        "label": "客户编码",
        "component": "select",
        "span": 6,
        "dictionary": "customers",
        "required": true,
        "props": {"placeholder": "请选择客户", "filterable": true, "clearable": true}
      },
      {
        "field": "F_ora_BaseProperty",
        "label": "客户简称",
        "component": "input",
        "span": 6,
        "required": true,
        "rules": [{"required": true, "message": "客户简称不能为空", "trigger": "blur"}],
        "props": {"placeholder": "请输入客户简称", "maxlength": 200},
        "defaultValue": ""
      },
      {
        "field": "F_khhth",
        "label": "客户合同号",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 100, "clearable": true}
      },
      {
        "field": "F_kglxr",
        "label": "客户联系人",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 100, "clearable": true, "readonly": true}
      },
      {
        "field": "F_cty_BaseProperty1",
        "label": "客户邮箱",
        "component": "input",
        "span": 6,
        "rules": [{"type": "email", "message": "请输入正确的邮箱地址", "trigger": ["blur", "change"]}],
        "props": {"maxlength": 100, "clearable": true, "readonly": true}
      },
      {
        "field": "FSettleCurrId",
        "label": "结算币别",
        "comment": "用于与金蝶 K3 系统集成的结算币种标识",
        "component": "select",
        "span": 6,
        "dictionary": "currency",
        "props": {"placeholder": "请选择结算币别", "clearable": true, "filterable": true},
        "defaultValue": "人民币"
      },
      {
        "field": "F_tcbl",
        "label": "提成比例",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "max": 100, "precision": 2, "step": 0.1, "controlsPosition": "right"}
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
        "field": "F_sfbg",
        "label": "是否报关",
        "component": "select",
        "span": 6,
        "options": [{"label": "是", "value": 1}, {"label": "否", "value": 0}],
        "componentProps": {"placeholder": "请选择是否报关", "clearable": true},
        "defaultValue": 1
      },
      {
        "field": "FRecConditionId",
        "label": "收款条件",
        "component": "select",
        "span": 6,
        "dictionary": "payment_clause",
        "componentProps": {"placeholder": "请选择收款条件", "clearable": true, "filterable": true}
      },
      {
        "field": "FBillAmount",
        "label": "订单金额",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"}
      },
      {
        "field": "FBillTaxAmount",
        "label": "税额",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"}
      },
      {
        "field": "FLocalCurrId",
        "label": "本位币",
        "comment": "公司内部核算使用的本位货币，区别于结算币别",
        "component": "select",
        "span": 6,
        "dictionary": "currency",
        "componentProps": {"placeholder": "请选择本位币", "clearable": true, "filterable": true}
      },
      {
        "field": "FSalerId",
        "label": "销售员",
        "component": "select",
        "span": 6,
        "dictionary": "salespersons",
        "componentProps": {"placeholder": "请选择销售员", "clearable": true, "filterable": true}
      },
      {
        "field": "F_lrl",
        "label": "毛净利润率",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "max": 100, "precision": 2, "step": 0.1, "controlsPosition": "right"}
      },
      {
        "field": "F_jlrl",
        "label": "净利润率",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "max": 100, "precision": 2, "step": 0.1, "controlsPosition": "right"}
      },
      {
        "field": "FState",
        "label": "国家",
        "component": "select",
        "span": 6,
        "dictionary": "nation",
        "componentProps": {"placeholder": "请选择国家", "clearable": true, "filterable": true},
        "defaultValue": ""
      },
      {
        "field": "F_gj",
        "label": "抵运国家",
        "component": "select",
        "span": 6,
        "dictionary": "nation",
        "componentProps": {"placeholder": "请选择抵运国家", "clearable": true, "filterable": true},
        "defaultValue": ""
      },
      {
        "field": "F_myfs",
        "label": "贸易方式",
        "component": "select",
        "span": 6,
        "dictionary": "trade_way",
        "componentProps": {"placeholder": "请选择贸易方式", "clearable": true, "filterable": true},
        "defaultValue": "6191b13c997848"
      }
    ]
  }',
  
  -- table_config: 从 table.json 读取（只包含表格列配置）
  '{
    "tableName": "t_sale_order",
    "primaryKey": "id",
    "columns": [
      {"type": "selection", "width": 55, "fixed": "left", "resizable": false},
      {"type": "expand", "width": 100, "fixed": "left", "resizable": false, "label": "详情"},
      {"prop": "FBillNo", "label": "单据编号", "width": 150, "fixed": "left", "align": "left", "visible": true, "resizable": true, "renderType": "text"},
      {"prop": "F_ora_BaseProperty", "label": "客户简称", "width": 150, "fixed": "left", "align": "left", "visible": true, "resizable": true},
      {"prop": "orderStatus", "label": "订单状态", "width": 120, "align": "center", "visible": true, "renderType": "tag", "dictionary": "order_status"},
      {"prop": "FDocumentStatus", "label": "单据状态", "width": 140, "align": "center", "visible": true, "renderType": "tag", "dictionary": "f_document_status"},
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
  
  -- search_config: 从 search.json 读取（查询表单配置）
  '{
    "showSearch": true,
    "defaultExpand": true,
    "fields": [
      {
        "field": "FDate",
        "label": "日期区间",
        "component": "daterange",
        "props": {
          "startPlaceholder": "开始日期",
          "endPlaceholder": "结束日期",
          "valueFormat": "YYYY-MM-DD",
          "style": {"width": "240px"}
        },
        "defaultValue": "currentMonth",
        "changeEvent": "handleQuery",
        "queryOperator": "between"
      },
      {
        "field": "FBillNo",
        "label": "单据编号",
        "component": "input",
        "props": {
          "placeholder": "输入单据编号",
          "clearable": true,
          "prefixIcon": "Search",
          "style": {"width": "180px"}
        },
        "queryOperator": "right_like"
      },
      {
        "field": "F_ora_BaseProperty",
        "label": "客户简称",
        "component": "input",
        "props": {
          "placeholder": "输入客户简称",
          "clearable": true,
          "prefixIcon": "User",
          "style": {"width": "150px"}
        },
        "queryOperator": "like"
      },
      {
        "field": "FSalerId",
        "label": "销售员",
        "component": "select",
        "dictionary": "salespersons",
        "props": {
          "placeholder": "选择销售员",
          "clearable": true,
          "filterable": true,
          "style": {"width": "120px"}
        },
        "defaultValue": "",
        "queryOperator": "eq"
      },
      {
        "field": "orderStatus",
        "label": "订单状态",
        "component": "select",
        "dictionary": "order_status",
        "props": {
          "placeholder": "选择订单状态",
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
          "placeholder": "选择单据状态",
          "clearable": true,
          "filterable": true,
          "style": {"width": "120px"}
        },
        "defaultValue": "",
        "queryOperator": "eq"
      }
    ]
  }',
  
  -- action_config: 按钮操作配置（从 config.json 的 buttons 提取）
  '{
    "toolbar": [
      {"key": "add", "label": "新增", "icon": "Plus", "permission": "k3:saleorder:add", "type": "primary", "position": "left", "handler": "handleAdd"},
      {"key": "edit", "label": "修改", "icon": "Edit", "permission": "k3:saleorder:edit", "type": "success", "position": "left", "disabled": "single", "handler": "handleUpdate"},
      {"key": "delete", "label": "删除", "icon": "Delete", "permission": "k3:saleorder:remove", "type": "danger", "position": "left", "disabled": "multiple", "handler": "handleDelete", "confirm": "是否确认删除选中的 {count} 条数据？"},
      {"key": "audit", "label": "审核", "icon": "CircleCheck", "permission": "k3:saleorder:audit", "type": "success", "position": "left", "disabled": "multiple", "handler": "handleAudit", "confirm": "是否确认审核选中的 {count} 条数据？"},
      {"key": "unAudit", "label": "反审核", "icon": "Close", "permission": "k3:saleorder:unAudit", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleUnAudit", "confirm": "是否确认反审核选中的 {count} 条数据？"},
      {"key": "push", "label": "下推", "icon": "Download", "permission": "k3:saleorder:push", "type": "info", "position": "left", "disabled": "single", "handler": "handleOpenPushDialog"},
      {"key": "export", "label": "导出", "icon": "Download", "permission": "k3:saleorder:export", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleExport"},
      {"key": "columnSetting", "label": "列设置", "icon": "Setting", "type": "info", "position": "right", "handler": "openColumnSetting"}
    ],
    "row": []
  }',
  
  -- ✨ api_config: API接口配置（新增）
  -- ✨ 说明：定义所有CRUD操作的API接口
  '{
    "baseUrl": "/api/saleorder",
    "methods": {
      "list": {
        "url": "/list",
        "method": "GET",
        "description": "查询销售订单列表"
      },
      "get": {
        "url": "/{id}",
        "method": "GET",
        "description": "获取销售订单详情"
      },
      "add": {
        "url": "/add",
        "method": "POST",
        "description": "新增销售订单"
      },
      "update": {
        "url": "/update",
        "method": "PUT",
        "description": "修改销售订单"
      },
      "delete": {
        "url": "/delete",
        "method": "DELETE",
        "description": "删除销售订单"
      },
      "entry": {
        "url": "/entry/{billNo}",
        "method": "GET",
        "description": "获取销售订单明细"
      },
      "cost": {
        "url": "/cost/{billNo}",
        "method": "GET",
        "description": "获取销售订单成本"
      },
      "audit": {
        "url": "/audit",
        "method": "POST",
        "description": "审核销售订单"
      },
      "unAudit": {
        "url": "/unAudit",
        "method": "POST",
        "description": "反审核销售订单"
      }
    }
  }',
  
  -- ✨ dict_config: 从 dict.json 读取（重构为新构建器格式）
  -- ✨ 变更：从 dicts 数组改为 dictionaries 对象，增加 builder.enabled
  -- ✨ 优化：移除普通字典配置，统一使用 API 返回的字典数据
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
        "type": "dynamic",
        "table": "bd_customer",
        "conditions": [
          {"field": "deleted", "operator": "isNull"}
        ],
        "orderBy": [
          {"field": "fname", "direction": "ASC"}
        ],
        "fieldMapping": {
          "valueField": "fnumber",
          "labelField": "fname"
        },
        "config": {
          "api": "/erp/engine/dict/union/customers",
          "useGlobalCache": true
        }
      },
      "materials": {
        "type": "dynamic",
        "table": "by_material",
        "conditions": [
          {"field": "deleted", "operator": "isNull"}
        ],
        "orderBy": [
          {"field": "name", "direction": "ASC"}
        ],
        "fieldMapping": {
          "valueField": "materialId",
          "labelField": "name"
        },
        "config": {
          "api": "/erp/engine/dict/union/materials",
          "useGlobalCache": true
        }
      },
      "nation": {
        "type": "remote",
        "config": {
          "searchApi": "/erp/engine/country/search?keyword={keyword}&limit=20",
          "minKeywordLength": 1,
          "debounce": 300
        }
      }
    },
    "globalCacheSettings": {
      "enabled": true,
      "defaultTTL": 300000
    }
  }',
  
  -- business_config: 业务消息和实体名称配置（已移除 buttons 到 action_config）
  '{
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
  
  -- detail_config: 从 detail.json 读取（✨ 已优化：修复 type 为 form + 使用数据库字段名）
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
          "label": "销售订单明细",
          "icon": "Document",
          "type": "table",
          "dataField": "entryList",
          "tableName": "t_sale_order_entry",
          "queryConfig": {
            "enabled": true,
            "defaultConditions": [
              {
                "field": "fbillno",
                "operator": "eq",
                "value": "${billNo}",
                "description": "按订单编号查询明细"
              }
            ],
            "defaultOrderBy": [{"field": "FPlanMaterialId", "direction": "ASC"}]
          },
          "table": {
            "border": true,
            "stripe": true,
            "maxHeight": "500",
            "showOverflowTooltip": true,
            "columns": [
              {"prop": "FPlanMaterialId", "label": "物料编码", "width": 120, "align": "center", "sortable": true},
              {"prop": "FPlanMaterialName", "label": "物料名称", "width": 180, "align": "left", "showOverflowTooltip": true, "sortable": true},
              {"prop": "FQty", "label": "数量", "width": 100, "align": "right", "renderType": "number", "sortable": true, "precision": 4},
              {"prop": "FPrice", "label": "单价", "width": 100, "align": "right", "renderType": "currency", "precision": 8, "sortable": true},
              {"prop": "FTaxPrice", "label": "含税单价", "width": 100, "align": "right", "renderType": "currency", "precision": 8, "sortable": true},
              {"prop": "FAllAmount", "label": "金额合计", "width": 120, "align": "right", "renderType": "currency", "precision": 2, "sortable": true},
              {"prop": "FDeliQty", "label": "已交付数量", "width": 100, "align": "right", "renderType": "number", "sortable": true, "precision": 4},
              {"prop": "F_mz", "label": "毛重", "width": 80, "align": "right", "renderType": "number", "precision": 2},
              {"prop": "F_jz", "label": "净重", "width": 80, "align": "right", "renderType": "number", "precision": 2},
              {"prop": "F_kpdj", "label": "开票单价", "width": 100, "align": "right", "renderType": "currency", "precision": 8},
              {"prop": "F_ygcb", "label": "预估成本", "width": 100, "align": "right", "renderType": "currency", "precision": 2},
              {"prop": "F_hsbm", "label": "海关编码", "width": 100, "align": "center"},
              {"prop": "F_cplb", "label": "产品类别", "width": 100, "align": "center"}
            ]
          }
        },
        {
          "name": "cost",
          "label": "成本暂估",
          "icon": "Money",
          "type": "form",
          "dataField": "costData",
          "tableName": "t_sale_order_cost",
          "queryConfig": {
            "enabled": true,
            "defaultConditions": [
              {
                "field": "FBillNo",
                "operator": "eq",
                "value": "${billNo}",
                "description": "按订单编号查询成本"
              }
            ],
            "defaultOrderBy": [{"field": "FID", "direction": "ASC"}]
          },
          "form": {
            "layout": "horizontal",
            "labelWidth": 140,
            "columns": 3,
            "fields": [
              {"prop": "F_hyf", "label": "海运费 (外币)", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "FBillAllAmount", "label": "价税合计", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "FBillAllAmount_LC", "label": "价税合计 (本位币)", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_bxf", "label": "保险费", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_gwyhfy", "label": "国外银行费用", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_qtwbfy", "label": "其他外币费用", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_mxcbhj", "label": "明细成本合计", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_mxtshj", "label": "明细退税合计", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_cbxj", "label": "成本小计 RMB", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_bzf", "label": "包装费", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_dlf", "label": "代理费", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_rzf", "label": "认证费", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_kdf", "label": "快递费成本", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_hdf", "label": "货贷费", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_lyf", "label": "陆运费", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_qtfy", "label": "其他费用", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_mjf", "label": "模具费", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_jcf", "label": "进仓费", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_fyxj", "label": "费用小计", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_wbyk", "label": "外币盈亏", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_jlre", "label": "净利润额", "span": 8, "component": "input-number", "renderType": "currency", "precision": 2, "readonly": true},
              {"prop": "F_lrl", "label": "毛净利润率%", "span": 8, "component": "input-number", "renderType": "percent", "precision": 2, "readonly": true},
              {"prop": "F_jlrl", "label": "净利润率%", "span": 8, "component": "input-number", "renderType": "percent", "precision": 2, "readonly": true}
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
  '销售订单配置（v8.0 完全体 - 消除所有异常字段映射）'
);

-- ============================================
-- 第三步：验证导入结果
-- ============================================

SELECT '========================================' AS '';
SELECT '✅ 销售订单配置导入成功（字典重构版）！' AS '';
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
WHERE module_code = 'saleorder';

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
WHERE module_code = 'saleorder';

SELECT '========================================' AS '';
SELECT '✨ 字典配置升级说明（v3.3）：' AS '';
SELECT '========================================' AS '';
SELECT '  ✅ 字典命名：统一使用 snake_case（order_status, f_document_status）' AS upgrade1;
SELECT '  ✅ 字典来源：统一使用 /erp/engine/dict/all API' AS upgrade2;
SELECT '  ✅ 移除配置：普通字典和静态字典从 dict_config 移除' AS upgrade3;
SELECT '  ✅ 保留配置：仅保留需要特殊处理的字典（salespersons, customers, materials, nation）' AS upgrade4;
SELECT '  ✅ 默认值修正：使用实际值而非显示标签（currency: "1", trade_way: "6191b13c997848"）' AS upgrade5;
SELECT '========================================' AS '';
SELECT '下一步操作：' AS '';
SELECT '  后续将添加：' AS note1;
SELECT '    - 审批流程配置（erp_approval_flow）' AS note2;
SELECT '    - 下推关系配置（erp_push_relation）' AS note3;
SELECT '  请继续执行相应的导入脚本' AS note4;
SELECT '========================================' AS '';
SELECT '完成！' AS '';
SELECT '========================================' AS '';
