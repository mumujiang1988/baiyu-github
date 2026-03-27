-- ============================================
-- 更新字典配置为新格式 (v3.1)
-- 日期：2026-03-27
-- 说明：将 dict_config 从旧格式 (dicts 数组) 更新为新格式 (dictionaries 对象)
-- ============================================

-- 更新销售订单模块的字典配置
UPDATE erp_page_config 
SET dict_config = '{
  "builder": {
    "enabled": true
  },
  "dictionaries": {
    "salespersons": {
      "type": "dynamic",
      "table": "sys_user",
      "config": {
        "api": "/erp/engine/dict/union/salespersons"
      }
    },
    "currency": {
      "type": "dynamic",
      "table": "bymaterial_dictionary",
      "config": {
        "api": "/erp/engine/dict/union/currency"
      }
    },
    "paymentTerms": {
      "type": "dynamic",
      "table": "bymaterial_dictionary",
      "config": {
        "api": "/erp/engine/dict/union/paymentTerms"
      }
    },
    "nation": {
      "type": "remote",
      "config": {
        "searchApi": "/erp/engine/country/search?keyword={keyword}&limit=20",
        "minKeywordLength": 1,
        "debounce": 300
      }
    },
    "tradeType": {
      "type": "dynamic",
      "table": "bymaterial_dictionary",
      "config": {
        "api": "/erp/engine/dict/union/tradeType"
      }
    },
    "customers": {
      "type": "dynamic",
      "table": "bd_customer",
      "config": {
        "api": "/erp/engine/dict/union/customers"
      }
    },
    "materials": {
      "type": "dynamic",
      "table": "by_material",
      "config": {
        "api": "/erp/engine/dict/union/materials"
      }
    },
    "productCategory": {
      "type": "dynamic",
      "table": "bymaterial_dictionary",
      "config": {
        "api": "/erp/engine/dict/union/productCategory"
      }
    },
    "orderStatus": {
      "type": "static",
      "data": [
        { "label": "未关闭", "value": "A", "type": "success" },
        { "label": "已关闭", "value": "B", "type": "info" },
        { "label": "业务终止", "value": "C", "type": "danger" }
      ]
    },
    "documentStatus": {
      "type": "static",
      "data": [
        { "label": "暂存", "value": "Z", "type": "info" },
        { "label": "创建", "value": "A", "type": "success" },
        { "label": "审核中", "value": "B", "type": "warning" },
        { "label": "已审核", "value": "C", "type": "success" },
        { "label": "重新审核", "value": "D", "type": "primary" }
      ]
    }
  },
  "globalCacheSettings": {
    "enabled": true,
    "defaultTTL": 300000
  }
}'
WHERE module_code = 'saleorder';

-- 验证更新结果
SELECT 
  config_id,
  module_code,
  config_name,
  JSON_EXTRACT(dict_config, '$.builder.enabled') as builder_enabled,
  JSON_LENGTH(JSON_EXTRACT(dict_config, '$.dictionaries')) as dict_count,
  JSON_KEYS(JSON_EXTRACT(dict_config, '$.dictionaries')) as dict_keys
FROM erp_page_config
WHERE module_code = 'saleorder';
