-- ============================================
-- ERP 配置 JSON 强制拆分方案 - 询价单模块导入 SQL
-- 版本：v1.0 (基于 inquiry_order 表结构)
-- 日期：2026-04-20
-- 说明：导入询价单页面配置数据（9 字段强制拆分 + API配置）
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

-- 级联删除询价单相关的所有配置
DELETE FROM erp_page_config_history WHERE module_code = 'inquiryorder';
DELETE FROM erp_approval_history WHERE module_code = 'inquiryorder';
DELETE FROM erp_approval_flow WHERE module_code = 'inquiryorder';
DELETE FROM erp_push_relation WHERE source_module = 'inquiryorder' OR target_module = 'inquiryorder';
DELETE FROM erp_page_config WHERE module_code = 'inquiryorder';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第二步：插入询价单页面配置数据
-- ============================================

-- 询价单页面配置（9 字段强制拆分，包含 api_config、search_config 和 action_config）
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
  'inquiryorder',
  '询价单管理',
  'PAGE', 
  '{
    "pageId": "inquiryorder",
    "title": "{entityName}管理",
    "permission": "erp:inquiryorder:query",
    "layout": "standard",
    "apiPrefix": "/erp/engine",
    "tableName": "inquiry_order",
    "billNoField": "fbillno"
  }', 
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
        "props": {"maxlength": 50, "clearable": true}
      },
      {
        "field": "fdate",
        "label": "单据日期",
        "component": "date-picker",
        "span": 6,
        "required": true,
        "props": {"placeholder": "选择日期", "valueFormat": "YYYY-MM-DD"}
      },
      {
        "field": "f_khbm",
        "label": "客户编码",
        "component": "select",
        "span": 6,
        "dictionary": "customers",
        "required": true,
        "props": {"placeholder": "请选择客户", "filterable": true, "clearable": true}
      },
      {
        "field": "f_cty_baseproperty",
        "label": "客户名称",
        "component": "input",
        "span": 6,
        "required": true,
        "props": {"placeholder": "请输入客户名称", "maxlength": 255},
        "defaultValue": ""
      },
      {
        "field": "f_cty_baseproperty1",
        "label": "客户简称",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 255, "clearable": true}
      },
      {
        "field": "f_xsy1",
        "label": "销售员",
        "component": "select",
        "span": 6,
        "dictionary": "salespersons",
        "props": {"placeholder": "请选择销售员", "clearable": true, "filterable": true}
      },
      {
        "field": "f_ywy",
        "label": "业务员",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 50, "clearable": true}
      },
      {
        "field": "f_bjbb",
        "label": "币别",
        "component": "select",
        "span": 6,
        "dictionary": "currency",
        "props": {"placeholder": "请选择币别", "clearable": true, "filterable": true},
        "defaultValue": "人民币"
      },
      {
        "field": "f_gdhl",
        "label": "固定汇率",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 6, "step": 0.000001, "controlsPosition": "right"}
      },
      {
        "field": "f_mll",
        "label": "毛净利率%",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "max": 100, "precision": 4, "step": 0.01, "controlsPosition": "right"}
      },
      {
        "field": "fkhxq",
        "label": "客户需求",
        "component": "textarea",
        "span": 12,
        "props": {"placeholder": "请输入客户需求", "maxlength": 500, "rows": 3}
      },
      {
        "field": "f_qyd",
        "label": "shipping to",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 100, "clearable": true}
      },
      {
        "field": "f_fkfs",
        "label": "payment terms",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 100, "clearable": true}
      },
      {
        "field": "f_cty_baseproperty4",
        "label": "客户分组",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 100, "clearable": true}
      },
      {
        "field": "f_ckgj",
        "label": "出口国家",
        "component": "select",
        "span": 6,
        "dictionary": "nation",
        "props": {"placeholder": "请选择出口国家", "clearable": true, "filterable": true}
      },
      {
        "field": "f_khly",
        "label": "客户来源",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 50, "clearable": true}
      },
      {
        "field": "f_khlx1",
        "label": "客户类型",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 50, "clearable": true}
      },
      {
        "field": "f_btjshj",
        "label": "表头价税合计",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right", "readonly": true}
      },
      {
        "field": "f_jshjbwb",
        "label": "价税合计本位币",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right", "readonly": true}
      },
      {
        "field": "f_btcbhj",
        "label": "表头成本合计",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right", "readonly": true}
      },
      {
        "field": "fmjlr",
        "label": "毛净利润",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right", "readonly": true}
      },
      {
        "field": "f_email",
        "label": "邮箱",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 100, "clearable": true}
      },
      {
        "field": "f_mob",
        "label": "手机",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 50, "clearable": true}
      },
      {
        "field": "f_tel",
        "label": "电话",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 50, "clearable": true}
      },
      {
        "field": "f_fax",
        "label": "传真",
        "component": "input",
        "span": 6,
        "props": {"maxlength": 50, "clearable": true}
      },
      {
        "field": "f_cgbjrq",
        "label": "采购报价日期",
        "component": "date-picker",
        "span": 6,
        "props": {"placeholder": "选择日期", "valueFormat": "YYYY-MM-DD"}
      },
      {
        "field": "f_bzf",
        "label": "包装费",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"}
      },
      {
        "field": "f_hdf",
        "label": "货代费",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"}
      },
      {
        "field": "f_lyf",
        "label": "陆运费",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"}
      },
      {
        "field": "f_qtfy",
        "label": "其他费用",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right"}
      },
      {
        "field": "f_fyxj",
        "label": "费用小计",
        "component": "input-number",
        "span": 6,
        "componentProps": {"min": 0, "precision": 2, "step": 0.01, "controlsPosition": "right", "readonly": true}
      },
      {
        "field": "f_hqbjfk",
        "label": "后勤报价反馈",
        "component": "textarea",
        "span": 12,
        "props": {"placeholder": "请输入后勤报价反馈", "maxlength": 500, "rows": 3}
      }
    ]
  }', 
  '{
    "tableName": "inquiry_order",
    "primaryKey": "fid",
    "orderBy": [
      {"field": "fdate", "direction": "DESC"}
    ],
    "columns": [
      {"type": "selection", "width": 55, "fixed": "left", "resizable": false},
      {"type": "expand", "width": 100, "fixed": "left", "resizable": false, "label": "详情"},
      {"prop": "fbillno", "label": "单据编号", "width": 150, "fixed": "left", "align": "left", "visible": true, "resizable": true, "renderType": "text"},
      {"prop": "f_cty_baseproperty", "label": "客户名称", "width": 180, "fixed": "left", "align": "left", "visible": true, "resizable": true},
      {"prop": "f_cty_baseproperty1", "label": "客户简称", "width": 150, "align": "left", "visible": true, "resizable": true},
      {"prop": "fdocumentstatus", "label": "数据状态", "width": 120, "align": "center", "visible": true, "renderType": "tag", "dictionary": "f_document_status"},
      {"prop": "fdate", "label": "单据日期", "width": 140, "align": "center", "visible": true, "renderType": "date", "format": "YYYY-MM-DD"},
      {"prop": "f_xsy1", "label": "销售员", "width": 120, "align": "left", "visible": true, "renderType": "text", "dictionary": "salespersons"},
      {"prop": "f_ywy", "label": "业务员", "width": 120, "align": "left", "visible": true, "renderType": "text"},
      {"prop": "f_btjshj", "label": "价税合计", "width": 140, "align": "right", "visible": true, "renderType": "currency", "precision": 2},
      {"prop": "f_jshjbwb", "label": "价税合计(本位币)", "width": 160, "align": "right", "visible": true, "renderType": "currency", "precision": 2},
      {"prop": "f_btcbhj", "label": "成本合计", "width": 140, "align": "right", "visible": true, "renderType": "currency", "precision": 2},
      {"prop": "fmjlr", "label": "毛净利润", "width": 120, "align": "right", "visible": true, "renderType": "currency", "precision": 2},
      {"prop": "f_bjbb", "label": "币别", "width": 100, "align": "center", "visible": true, "renderType": "text", "dictionary": "currency"},
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
        "label": "单据日期",
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
        "field": "f_cty_baseproperty",
        "label": "客户名称",
        "component": "input",
        "props": {
          "placeholder": "客户名称",
          "clearable": true,
          "prefixIcon": "User",
          "style": {"width": "160px"}
        },
        "queryOperator": "like"
      },
      {
        "field": "f_xsy1",
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
        "field": "f_ywy",
        "label": "业务员",
        "component": "input",
        "props": {
          "placeholder": "业务员",
          "clearable": true,
          "style": {"width": "120px"}
        },
        "queryOperator": "like"
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
      {"key": "add", "label": "新增", "icon": "Plus", "permission": "erp:inquiryorder:add", "type": "primary", "position": "left", "handler": "handleAdd"},
      {"key": "edit", "label": "修改", "icon": "Edit", "permission": "erp:inquiryorder:edit", "type": "success", "position": "left", "disabled": "single", "handler": "handleUpdate"},
      {"key": "delete", "label": "删除", "icon": "Delete", "permission": "erp:inquiryorder:remove", "type": "danger", "position": "left", "disabled": "multiple", "handler": "handleDelete", "confirm": "是否确认删除选中的 {count} 条数据？"},
      {"key": "audit", "label": "审核", "icon": "CircleCheck", "permission": "erp:inquiryorder:audit", "type": "success", "position": "left", "disabled": "multiple", "handler": "handleAudit", "confirm": "是否确认审核选中的 {count} 条数据？"},
      {"key": "unAudit", "label": "反审核", "icon": "Close", "permission": "erp:inquiryorder:unAudit", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleUnAudit", "confirm": "是否确认反审核选中的 {count} 条数据？"},
      {"key": "push", "label": "下推", "icon": "Download", "permission": "erp:inquiryorder:push", "type": "info", "position": "left", "disabled": "single", "handler": "handleOpenPushDialog"},
      {"key": "export", "label": "导出", "icon": "Download", "permission": "erp:inquiryorder:export", "type": "warning", "position": "left", "disabled": "multiple", "handler": "handleExport"},
      {"key": "columnSetting", "label": "列设置", "icon": "Setting", "type": "info", "position": "right", "handler": "openColumnSetting"}
    ],
    "row": []
  }', 
  '{
    "baseUrl": "/api/inquiryorder",
    "methods": {
      "list": {
        "url": "/list",
        "method": "GET",
        "description": "查询询价单列表"
      },
      "get": {
        "url": "/{id}",
        "method": "GET",
        "description": "获取询价单详情"
      },
      "add": {
        "url": "/add",
        "method": "POST",
        "description": "新增询价单"
      },
      "update": {
        "url": "/update",
        "method": "PUT",
        "description": "修改询价单"
      },
      "delete": {
        "url": "/delete",
        "method": "DELETE",
        "description": "删除询价单"
      },
      "entry": {
        "url": "/entry/{billNo}",
        "method": "GET",
        "description": "获取询价单明细"
      },
      "audit": {
        "url": "/audit",
        "method": "POST",
        "description": "审核询价单"
      },
      "unAudit": {
        "url": "/unAudit",
        "method": "POST",
        "description": "反审核询价单"
      }
    }
  }', 
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
        "type": "api",
        "config": {
          "api": "/erp/engine/dict/union/customers",
          "useGlobalCache": true,
          "cacheKey": "customers_dict",
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
      "nation": {
        "type": "remote",
        "config": {
          "searchApi": "/erp/engine/country/search?keyword={keyword}&limit=20",
          "minKeywordLength": 1,
          "debounce": 300,
          "labelField": "name",
          "valueField": "code"
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
      "success": {"add": "新增成功", "edit": "修改成功", "delete": "删除成功", "audit": "审核成功", "unAudit": "反审核成功"},
      "error": {"load": "加载数据失败", "save": "保存失败", "delete": "删除失败", "audit": "审核失败", "unAudit": "反审核失败"}
    },
    "entityName": "询价单",
    "entityNameSingular": "询价单",
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
          "label": "询价单明细",
          "icon": "Document",
          "type": "table",
          "dataField": "entryList",
          "tableName": "inquiry_order_entry",
          "relationConfig": { 
            "masterTable": "inquiry_order",
            "masterField": "fid",
            "detailTable": "inquiry_order_entry",
            "detailField": "fid",
            "operator": "eq"
          },
          "queryConfig": {
            "enabled": true,
            "defaultConditions": [
              {
                "field": "fid",
                "operator": "eq",
                "value": "${fid}",
                "description": "按主表fid查询明细 (inquiry_order.fid = inquiry_order_entry.fid)"
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
              {"prop": "f_wldm", "label": "产品代码", "width": 120, "align": "center", "sortable": true},
              {"prop": "f_cty_baseproperty2", "label": "产品名称", "width": 180, "align": "left", "showOverflowTooltip": true, "sortable": true},
              {"prop": "f_cty_baseproperty3", "label": "规格型号", "width": 150, "align": "left", "showOverflowTooltip": true, "sortable": true},
              {"prop": "f_cplb", "label": "产品类别", "width": 100, "align": "center", "sortable": true},
              {"prop": "f_cty_baseproperty5", "label": "产品类型", "width": 100, "align": "center"},
              {"prop": "f_xdl", "label": "下单量", "width": 100, "align": "right", "renderType": "number", "sortable": true, "precision": 2},
              {"prop": "f_qdl", "label": "起订量", "width": 100, "align": "right", "renderType": "number", "precision": 2},
              {"prop": "f_hsdj", "label": "含税单价", "width": 100, "align": "right", "renderType": "currency", "precision": 4, "sortable": true},
              {"prop": "f_hscgj", "label": "成本价含税", "width": 100, "align": "right", "renderType": "currency", "precision": 4, "sortable": true},
              {"prop": "f_jshj", "label": "价税合计", "width": 120, "align": "right", "renderType": "currency", "precision": 2, "sortable": true},
              {"prop": "f_cbhj", "label": "成本合计", "width": 120, "align": "right", "renderType": "currency", "precision": 2, "sortable": true},
              {"prop": "f_yjjq", "label": "预计交期", "width": 120, "align": "center", "renderType": "date", "format": "YYYY-MM-DD"},
              {"prop": "f_jgsxrq", "label": "价格失效日期", "width": 120, "align": "center", "renderType": "date", "format": "YYYY-MM-DD"},
              {"prop": "f_jqfx", "label": "交期风险", "width": 100, "align": "center"},
              {"prop": "f_xsddgls", "label": "销售订单关联数", "width": 120, "align": "right", "renderType": "number", "precision": 2},
              {"prop": "f_xsddxtzt", "label": "销售订单下推状态", "width": 140, "align": "center"},
              {"prop": "f_khbzyq", "label": "客户包装要求", "width": 150, "align": "left", "showOverflowTooltip": true},
              {"prop": "f_cgbzfs", "label": "采购包装方式", "width": 120, "align": "center"},
              {"prop": "f_cgxpms", "label": "采购新品描述", "width": 180, "align": "left", "showOverflowTooltip": true},
              {"prop": "fgys", "label": "供应商", "width": 120, "align": "center"},
              {"prop": "ftpr", "label": "图纸人员", "width": 100, "align": "center"},
              {"prop": "fbjr", "label": "报价人", "width": 100, "align": "center"},
              {"prop": "f_ywms", "label": "英文描述", "width": 180, "align": "left", "showOverflowTooltip": true},
              {"prop": "fcpys", "label": "产品颜色", "width": 120, "align": "center"},
              {"prop": "f_mz", "label": "毛重", "width": 80, "align": "right", "renderType": "number", "precision": 2},
              {"prop": "f_jz", "label": "净重", "width": 80, "align": "right", "renderType": "number", "precision": 2},
              {"prop": "f_c", "label": "长", "width": 80, "align": "right", "renderType": "number", "precision": 2},
              {"prop": "f_k", "label": "宽", "width": 80, "align": "right", "renderType": "number", "precision": 2},
              {"prop": "f_g", "label": "高", "width": 80, "align": "right", "renderType": "number", "precision": 2},
              {"prop": "f_glsl", "label": "关联数量", "width": 100, "align": "right", "renderType": "number", "precision": 2}
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
  '询价单配置（v1.0 - 基于 inquiry_order 和 inquiry_order_entry 表结构）'
);

-- ============================================
-- 第三步：验证导入结果
-- ============================================

SELECT '========================================' AS '';
SELECT ' 询价单配置导入成功！' AS '';
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
WHERE module_code = 'inquiryorder';

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
WHERE module_code = 'inquiryorder';

SELECT '========================================' AS '';
SELECT ' 配置说明：' AS '';
SELECT '========================================' AS '';
SELECT '   主表：inquiry_order (fid 为主键)' AS info1;
SELECT '   明细表：inquiry_order_entry (通过 fid 关联)' AS info2;
SELECT '   字典配置：使用统一 API (/erp/engine/dict/union/*)' AS info3;
SELECT '   字段映射：严格按照需求文档中的字段定义' AS info4;
SELECT '========================================' AS '';
SELECT '下一步操作：' AS '';
SELECT '  后续将添加：' AS note1;
SELECT '    - 审批流程配置（erp_approval_flow）' AS note2;
SELECT '    - 下推关系配置（erp_push_relation）' AS note3;
SELECT '  请继续执行相应的导入脚本' AS note4;
SELECT '========================================' AS '';
SELECT '完成！' AS '';
SELECT '========================================' AS '';
