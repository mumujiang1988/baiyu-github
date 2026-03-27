# ERP 配置化架构文档 v3.1

## 📋 文档信息

- **版本**: v3.1 (字典重构版)
- **日期**: 2026-03-27
- **适用范围**: RuoYi-WMS ERP 低代码系统
- **核心特性**: JSON 强制拆分 + 字典构建器格式

---

## 🎯 架构概述

### 核心设计理念

采用**JSON 强制拆分架构**,将完整的页面配置拆分为 8 个独立的 JSON 字段，每个字段职责单一、可复用、易维护。

```
完整页面配置 = page_config + form_config + table_config + search_config + action_config + dict_config + business_config + detail_config
```

### 架构演进历程

| 版本 | 架构特点 | 字段数量 | 说明 |
|------|----------|---------|------|
| v1.0 | 单 JSON 字段 | 1 | `config_content` 包含所有配置 |
| v2.0 | 六字段拆分 | 6 | page/form/table/dict/business/detail |
| **v3.0** | **八字段强制拆分** | **8** | **新增 search_config + action_config** |
| **v3.1** | **字典构建器格式** | **8** | **dicts 数组 → dictionaries 对象 + builder.enabled** |

---

## 🏗️ 数据库表结构设计

### erp_page_config 表结构（核心表）

```sql
CREATE TABLE `erp_page_config` (
  `config_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置 ID',
  `module_code` VARCHAR(100) NOT NULL COMMENT '模块编码（唯一标识）',
  `config_name` VARCHAR(200) NOT NULL COMMENT '配置名称',
  `config_type` VARCHAR(50) NOT NULL DEFAULT 'PAGE' COMMENT '配置类型（PAGE/FORM/TABLE/DICT/BUSINESS）',
  
  -- ========== 强制拆分的 8 个 JSON 字段 ==========
  `page_config` JSON NOT NULL COMMENT '页面基础配置 (page.json)',
  `form_config` JSON COMMENT '表单 UI 组件配置 (form.json)',
  `table_config` JSON COMMENT '表格列配置 (table.json)',
  `search_config` JSON COMMENT '查询表单配置 (search.json)',
  `action_config` JSON COMMENT '按钮操作配置 (action.json)',
  `dict_config` JSON COMMENT '字典数据源配置 (dict.json)',
  `business_config` JSON COMMENT '业务规则配置 (config.json)',
  `detail_config` JSON COMMENT '详情页配置 (detail.json)',
  -- ===========================================
  
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
  `status` CHAR(1) NOT NULL DEFAULT '1' COMMENT '状态（0 禁用 1 启用）',
  `is_public` CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否公共配置（1 是 0 否）',
  `parent_config_id` BIGINT NULL COMMENT '父配置 ID（用于配置继承）',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_by` VARCHAR(100) NOT NULL COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(100) NULL COMMENT '更新者',
  `update_time` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_module_code` (`module_code`),
  KEY `idx_status` (`status`),
  KEY `idx_parent` (`parent_config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

### 关键字段说明

#### 1. page_config - 页面基础配置

**作用**: 定义页面的基本属性、API 前缀、权限等

**示例**:
```json
{
  "pageId": "saleorder",
  "pageName": "销售订单管理",
  "permission": "k3:saleorder:query",
  "layout": "standard",
  "apiPrefix": "/erp/engine",
  "tableName": "t_sale_order"
}
```

#### 2. form_config - 表单配置

**作用**: 定义新增/编辑弹窗的表单字段、布局、验证规则

**示例**:
```json
{
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
    }
  ]
}
```

#### 3. table_config - 表格列配置

**作用**: 定义列表页的表格列、渲染类型、排序等

**示例**:
```json
{
  "tableName": "t_sale_order",
  "primaryKey": "id",
  "columns": [
    {"type": "selection", "width": 55, "fixed": "left", "resizable": false},
    {"type": "expand", "width": 100, "fixed": "left", "resizable": false, "label": "详情"},
    {"prop": "FBillNo", "label": "单据编号", "width": 150, "fixed": "left", "align": "left", "visible": true, "resizable": true, "renderType": "text"}
  ],
  "pagination": {
    "defaultPageSize": 10,
    "pageSizeOptions": [10, 20, 50, 100]
  }
}
```

#### 4. search_config - 搜索区域配置 ⭐ v3.0 新增

**作用**: 定义顶部搜索区域的查询字段、组件、查询运算符

**示例**:
```json
{
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
    }
  ]
}
```

**查询运算符说明**:
- `eq`: 等于
- `ne`: 不等于
- `like`: 模糊匹配（前后都加%）
- `left_like`: 左模糊（前面加%）
- `right_like`: 右模糊（后面加%）
- `gt`: 大于
- `lt`: 小于
- `gte`: 大于等于
- `lte`: 小于等于
- `between`: 区间查询
- `in`: IN 查询
- `isNull`: IS NULL 查询

#### 5. action_config - 按钮操作配置 ⭐ v3.0 新增

**作用**: 定义工具栏按钮和行操作按钮的权限、图标、处理函数

**示例**:
```json
{
  "toolbar": [
    {
      "key": "add",
      "label": "新增",
      "icon": "Plus",
      "permission": "k3:saleorder:add",
      "type": "primary",
      "position": "left",
      "handler": "handleAdd"
    },
    {
      "key": "edit",
      "label": "修改",
      "icon": "Edit",
      "permission": "k3:saleorder:edit",
      "type": "success",
      "position": "left",
      "disabled": "single",
      "handler": "handleUpdate"
    },
    {
      "key": "delete",
      "label": "删除",
      "icon": "Delete",
      "permission": "k3:saleorder:remove",
      "type": "danger",
      "position": "left",
      "disabled": "multiple",
      "handler": "handleDelete",
      "confirm": "是否确认删除选中的 {count} 条数据？"
    }
  ],
  "row": []
}
```

**按钮位置**:
- `left`: 工具栏左侧
- `right`: 工具栏右侧（如列设置）

**禁用策略**:
- `single`: 单选时禁用
- `multiple`: 多选时禁用
- 不填：始终可用

#### 6. dict_config - 字典数据源配置 ✨ v3.1 重构

**作用**: 定义页面使用的所有字典数据源，支持静态字典和动态字典

**✨ v3.1 重大变更**:
- **旧格式**: `dicts` 数组
- **新格式**: `dictionaries` 对象 + `builder.enabled`

**新格式示例**:
```json
{
  "builder": {
    "enabled": true
  },
  "dictionaries": {
    "salespersons": {
      "type": "dynamic",
      "table": "sys_user",
      "conditions": [
        {"field": "deleted", "operator": "isNull"}
      ],
      "orderBy": [
        {"field": "nick_name", "direction": "ASC"}
      ],
      "fieldMapping": {
        "valueField": "user_id",
        "labelField": "nick_name"
      },
      "config": {
        "api": "/erp/engine/dictionary/salespersons/data?moduleCode={moduleCode}",
        "labelField": "nickName",
        "valueField": "fseller",
        "ttl": 600000
      },
      "cacheable": true,
      "cacheTTL": 600000
    },
    "currency": {
      "type": "dynamic",
      "table": "bymaterial_dictionary",
      "conditions": [
        {"field": "category", "operator": "eq", "value": "currency"},
        {"field": "deleted", "operator": "isNull"}
      ],
      "orderBy": [
        {"field": "name", "direction": "ASC"}
      ],
      "fieldMapping": {
        "valueField": "kingdee",
        "labelField": "name"
      },
      "config": {
        "api": "/erp/engine/dictionary/currency/data?moduleCode={moduleCode}",
        "labelField": "name",
        "valueField": "kingdee",
        "ttl": 600000
      },
      "cacheable": true,
      "cacheTTL": 600000
    },
    "nation": {
      "type": "remote",
      "config": {
        "searchApi": "/erp/engine/country/search?keyword={keyword}&limit=20",
        "minKeywordLength": 1,
        "debounce": 300
      }
    },
    "orderStatus": {
      "type": "static",
      "data": [
        {"label": "未关闭", "value": "A", "type": "success"},
        {"label": "已关闭", "value": "B", "type": "info"},
        {"label": "业务终止", "value": "C", "type": "danger"}
      ]
    }
  },
  "globalCacheSettings": {
    "enabled": true,
    "defaultTTL": 300000
  }
}
```

**字典类型说明**:

| 类型 | 说明 | 数据来源 | 适用场景 |
|------|------|----------|----------|
| `static` | 静态字典 | JSON 中直接定义 | 固定的枚举值（如状态） |
| `dynamic` | 动态字典 | 数据库表查询 | 用户、物料、客户等业务数据 |
| `remote` | 远程字典 | 远程 API 搜索 | 大数据量、需要搜索的场景（如国家） |

**✨ 为什么引入 builder.enabled?**

这是为了与后端的 `DictionaryBuilder`和`DictionaryLoader` 类兼容。当`builder.enabled = true`时，前端会调用后端构建器接口获取字典数据，而不是直接从配置中读取。

#### 7. business_config - 业务规则配置

**作用**: 定义消息提示、实体名称、对话框标题等业务规则

**示例**:
```json
{
  "messages": {
    "selectOne": "请选择一条数据",
    "confirmDelete": "是否确认删除选中的 {count} 条数据？",
    "confirmAudit": "是否确认审核选中的 {count} 条数据？",
    "success": {"add": "新增成功", "edit": "修改成功", "delete": "删除成功"},
    "error": {"load": "加载数据失败", "save": "保存失败"}
  },
  "entityName": "销售订单",
  "entityNameSingular": "订单",
  "dialogTitle": {"add": "新增{entityName}", "edit": "修改{entityName}"},
  "drawerTitle": "{entityName}详情 - {billNo}"
}
```

#### 8. detail_config - 详情页配置

**作用**: 定义侧边抽屉式详情页的签页、表格、字段

**示例**:
```json
{
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
              "value": "${FBillNo}",
              "description": "按订单编号查询明细"
            }
          ],
          "defaultOrderBy": [{"field": "fPlanMaterialId", "direction": "ASC"}]
        },
        "table": {
          "border": true,
          "stripe": true,
          "maxHeight": "500",
          "showOverflowTooltip": true,
          "columns": [
            {"prop": "fplanmaterialid", "label": "物料编码", "width": 120, "align": "center", "sortable": true},
            {"prop": "fplanmaterialname", "label": "物料名称", "width": 180, "align": "left", "showOverflowTooltip": true, "sortable": true}
          ]
        }
      },
      {
        "name": "cost",
        "label": "成本暂估",
        "icon": "Money",
        "type": "descriptions",
        "dataField": "costData",
        "tableName": "t_sale_order_cost",
        "queryConfig": {
          "enabled": true,
          "defaultConditions": [
            {
              "field": "fbillno",
              "operator": "eq",
              "value": "${FBillNo}",
              "description": "按订单编号查询成本"
            }
          ]
        },
        "columns": 3,
        "fields": [
          {"prop": "F_hyf", "label": "海运费 (外币)", "renderType": "currency", "precision": 2},
          {"prop": "FBillAllAmount", "label": "价税合计", "renderType": "currency", "precision": 2}
        ]
      }
    ]
  }
}
```

---

## 🔄 配套表结构

### erp_page_config_history - 配置历史表

**作用**: 记录配置变更历史，支持版本回滚

```sql
CREATE TABLE `erp_page_config_history` (
  `history_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史 ID',
  `config_id` BIGINT NOT NULL COMMENT '配置 ID',
  `module_code` VARCHAR(100) NOT NULL COMMENT '模块编码',
  `version` INT NOT NULL COMMENT '版本号',
  
  -- 历史快照（同样拆分 8 个字段）
  `page_config` JSON COMMENT '页面配置快照',
  `form_config` JSON COMMENT '表单配置快照',
  `table_config` JSON COMMENT '表格配置快照',
  `search_config` JSON COMMENT '搜索配置快照',
  `action_config` JSON COMMENT '按钮操作配置快照',
  `dict_config` JSON COMMENT '字典配置快照',
  `business_config` JSON COMMENT '业务配置快照',
  `detail_config` JSON COMMENT '详情配置快照',
  
  `change_reason` VARCHAR(500) NULL COMMENT '变更原因',
  `change_type` VARCHAR(20) NOT NULL COMMENT '变更类型（CREATE/UPDATE/DELETE/ROLLBACK）',
  `create_by` VARCHAR(100) NOT NULL COMMENT '操作人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  
  PRIMARY KEY (`history_id`),
  KEY `idx_config_id` (`config_id`),
  KEY `idx_module_version` (`module_code`, `version`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

### erp_approval_flow - 审批流程配置表

```sql
CREATE TABLE `erp_approval_flow` (
  `flow_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流程 ID',
  `flow_name` VARCHAR(200) NOT NULL COMMENT '流程名称',
  `module_code` VARCHAR(100) NOT NULL COMMENT '模块编码',
  `flow_config` JSON NOT NULL COMMENT '流程配置（JSON 格式）',
  `status` CHAR(1) NOT NULL DEFAULT '1' COMMENT '状态（0 禁用 1 启用）',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_by` VARCHAR(100) NOT NULL COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(100) NULL COMMENT '更新者',
  `update_time` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`flow_id`),
  UNIQUE KEY `uk_module` (`module_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

### erp_approval_history - 审批历史记录表

```sql
CREATE TABLE `erp_approval_history` (
  `history_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史 ID',
  `flow_id` BIGINT NULL COMMENT '流程 ID',
  `module_code` VARCHAR(100) NOT NULL COMMENT '模块编码',
  `bill_id` BIGINT NOT NULL COMMENT '单据 ID',
  `bill_no` VARCHAR(100) NULL COMMENT '单据编号',
  `approval_action` VARCHAR(50) NOT NULL COMMENT '审批动作（SUBMIT/APPROVE/REJECT/WITHDRAW）',
  `approval_step` INT NULL COMMENT '审批步骤',
  `approval_user_id` BIGINT NOT NULL COMMENT '审批人 ID',
  `approval_user_name` VARCHAR(100) NULL COMMENT '审批人姓名',
  `approval_opinion` VARCHAR(500) NULL COMMENT '审批意见',
  `approval_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
  `before_status` VARCHAR(50) NULL COMMENT '审批前状态',
  `after_status` VARCHAR(50) NULL COMMENT '审批后状态',
  
  PRIMARY KEY (`history_id`),
  KEY `idx_flow_id` (`flow_id`),
  KEY `idx_module_bill` (`module_code`, `bill_id`),
  KEY `idx_bill_action` (`bill_id`, `approval_action`),
  KEY `idx_approval_time` (`approval_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

### erp_push_relation - 下推关系配置表

```sql
CREATE TABLE `erp_push_relation` (
  `relation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关系 ID',
  `source_module` VARCHAR(100) NOT NULL COMMENT '源模块编码',
  `target_module` VARCHAR(100) NOT NULL COMMENT '目标模块编码',
  `relation_name` VARCHAR(200) NOT NULL COMMENT '关系名称',
  `mapping_config` JSON NOT NULL COMMENT '字段映射配置（JSON 格式）',
  `condition_config` JSON NULL COMMENT '下推条件配置（JSON 格式）',
  `status` CHAR(1) NOT NULL DEFAULT '1' COMMENT '状态（0 禁用 1 启用）',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_by` VARCHAR(100) NOT NULL COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(100) NULL COMMENT '更新者',
  `update_time` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`relation_id`),
  KEY `idx_source_target` (`source_module`, `target_module`),
  KEY `idx_source_status` (`source_module`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

---

## 🔧 自动触发器

### trg_erp_config_history - 自动记录配置历史

```sql
DELIMITER $$

DROP TRIGGER IF EXISTS `trg_erp_config_history`$$

CREATE TRIGGER `trg_erp_config_history`
AFTER UPDATE ON `erp_page_config`
FOR EACH ROW
BEGIN
  INSERT INTO erp_page_config_history (
    config_id,
    module_code,
    version,
    page_config,
    form_config,
    table_config,
    search_config,
    action_config,
    dict_config,
    business_config,
    detail_config,
    change_reason,
    change_type,
    create_by
  ) VALUES (
    NEW.config_id,
    NEW.module_code,
    NEW.version,
    NEW.page_config,
    NEW.form_config,
    NEW.table_config,
    NEW.search_config,
    NEW.action_config,
    NEW.dict_config,
    NEW.business_config,
    NEW.detail_config,
    CONCAT('版本更新：', NEW.remark),
    'UPDATE',
    NEW.update_by
  );
END$$

DELIMITER ;
```

**作用**: 每次更新 `erp_page_config` 表时，自动将旧版本记录到历史表

---

## 📦 初始化脚本说明

### erp 模块初始化.sql

**作用**: 初始化数据库表结构、菜单权限、触发器

**执行内容**:
1. 删除旧表（幂等性保证）
2. 创建 5 个核心表（8 字段强制拆分）
3. 插入 ERP 业务菜单目录
4. 插入销售订单管理菜单及按钮权限
5. 创建自动历史触发器

**执行结果**:
- ✅ 5 个核心表
- ✅ 10 个菜单权限
- ✅ 自动历史触发器

### 拆分 json 导入-v3.1-字典重构版.sql

**作用**: 导入销售订单模块的完整配置数据（8 字段 + 新字典格式）

**执行内容**:
1. 清理旧的 saleorder 配置数据
2. 插入新的销售订单配置（8 字段强制拆分）
   - page_config: 页面基础配置
   - form_config: 21 个表单字段
   - table_config: 13 个表格列
   - search_config: 6 个搜索字段
   - action_config: 8 个工具栏按钮
   - dict_config: 9 个字典（新构建器格式）
   - business_config: 消息提示 + 实体名称
   - detail_config: 2 个详情页签（明细 + 成本）

**执行结果验证**:
```sql
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
```

---

## 🚀 快速上手指南

### 第一步：执行初始化脚本

```bash
# 1. 执行表结构初始化
mysql -u root -p test < erp 模块初始化.sql

# 2. 执行配置数据导入
mysql -u root -p test < 拆分 json 导入-v3.1-字典重构版.sql
```

### 第二步：编译后端

```bash
cd baiyu-ruoyi
mvn clean package -DskipTests
```

### 第三步：启动服务

```bash
cd baiyu-ruoyi/ruoyi-admin-wms/target
java -jar ruoyi-admin-wms.jar
```

### 第四步：访问页面

1. 登录系统：http://localhost:8080
2. 进入菜单：**ERP 业务菜单 > 销售订单管理**
3. 查看配置化页面效果

---

## 📊 配置统计信息（以销售订单为例）

| 配置项 | 数量 | 说明 |
|--------|------|------|
| 表单字段数 | 21 | 包含 input/select/date/input-number 等组件 |
| 搜索字段数 | 6 | 支持日期范围/模糊搜索/下拉选择 |
| 工具栏按钮数 | 8 | 新增/修改/删除/审核/反审核/下推/导出/列设置 |
| 表格列数 | 13 | 包含展开列/选择列/数据列 |
| 字典数量 | 9 | salespersons/currency/paymentTerms/nation 等 |
| 详情页签数 | 2 | 销售订单明细 + 成本暂估 |

---

## 🎯 核心优势

### 1. 职责分离

每个 JSON 字段都有明确的职责边界：
- `page_config`: 页面基础属性
- `form_config`: 表单 UI 组件
- `table_config`: 表格列定义
- `search_config`: 搜索区域
- `action_config`: 按钮操作
- `dict_config`: 数据源
- `business_config`: 业务规则
- `detail_config`: 详情页

### 2. 高度复用

可以单独复用某个配置片段：
- 只复用 `form_config` 到其他页面
- 只复用 `table_config` 的列定义
- 只复用 `dict_config` 的字典配置

### 3. 易于维护

- 修改表单字段只需改 `form_config`
- 修改搜索条件只需改 `search_config`
- 修改按钮权限只需改 `action_config`

### 4. 性能优化

- 按需加载配置片段
- 字典数据支持缓存（cacheable + cacheTTL）
- 远程字典支持防抖（debounce）

### 5. 版本控制

- 自动记录历史版本（触发器）
- 支持版本回滚
- 完整的变更审计日志

---

## 🔮 后续扩展

### 待添加的配置模块

1. **审批流程配置** (erp_approval_flow)
   - 多级审批流程
   - 条件分支
   - 会签/或签

2. **下推关系配置** (erp_push_relation)
   - 销售订单 → 发货通知单
   - 发货通知单 → 出库单
   - 字段映射规则

3. **打印模板配置**
   - 单据打印模板
   - 快递单打印模板

4. **报表配置**
   - 统计报表
   - 图表配置

---

## 📝 最佳实践

### 1. 命名规范

- `module_code`: 使用小写字母 + 数字，如 `saleorder`
- `field`: 使用小写字母 + 下划线，如 `fbillno`
- `dictionary`: 使用驼峰命名，如 `orderStatus`

### 2. 字典配置优先级

```
远程 API 搜索 (remote) > 动态字典 (dynamic) > 静态字典 (static)
```

优先使用静态字典，大数据量时使用动态字典或远程搜索。

### 3. 缓存策略

```json
{
  "cacheable": true,
  "cacheTTL": 600000  // 10 分钟
}
```

对于不常变化的数据（如币种、付款方式），启用缓存提升性能。

### 4. 权限控制

所有按钮都必须配置 `permission` 字段，确保与后端菜单权限一致：

```json
{
  "key": "add",
  "permission": "k3:saleorder:add"
}
```

### 5. 查询运算符选择

根据实际场景选择合适的查询运算符：

```javascript
// 精确匹配：eq
{ "queryOperator": "eq" }

// 模糊搜索：like / right_like
{ "queryOperator": "right_like" }  // 单据编号常用右模糊

// 区间查询：between
{ "queryOperator": "between" }  // 日期范围
```

---

## 🐛 常见问题

### Q1: 为什么要拆分为 8 个字段？

**答**: 
- 避免单 JSON 字段过于臃肿（超过 1000 行）
- 每个字段职责单一，便于维护和复用
- 支持按需加载，提升性能
- 便于版本控制和差异对比

### Q2: dict_config 为什么要用 dictionaries 对象而不是 dicts 数组？

**答**:
- 对象格式更容易通过 key 访问字典
- 支持嵌套结构（如 fieldMapping）
- 与后端 DictionaryBuilder 类兼容
- 更符合 JSON Schema 规范

### Q3: builder.enabled 的作用是什么？

**答**:
- 标识该字典配置使用后端的 DictionaryBuilder 构建
- 前端会根据此标志调用后端构建器接口
- 实现字典数据的统一管理和缓存

### Q4: search_config 和 form_config 的字段有什么区别？

**答**:
- `search_config.fields`: 只包含搜索相关的字段，支持`queryOperator`
- `form_config.fields`: 包含完整的表单字段，支持 `required` `rules` `defaultValue`
- 两者可以部分重叠（如都包含 FBillNo 字段）

---

## 📚 相关文档

- [ERP 配置优化 v3.0 - 实施完成报告](../../../../../docs/ERP 配置优化 v3.0 - 实施完成报告.md)
- [ERP 字典配置重构实施报告 v3.1](../../../../../docs/ERP 字典配置重构实施报告 v3.1.md)
- [销售订单配置完整文档](./销售订单配置完整文档.md)

---

## 📋 修订历史

| 版本 | 日期 | 修订内容 | 修订人 |
|------|------|----------|--------|
| v1.0 | 2026-03-22 | 初始版本（单 JSON 字段） | AI |
| v2.0 | 2026-03-22 | 六字段拆分方案 | AI |
| v3.0 | 2026-03-27 | 八字段强制拆分（新增 search/action） | AI |
| v3.1 | 2026-03-27 | 字典重构（dicts→dictionaries + builder.enabled） | AI |

---

**文档版本**: v3.1  
**最后更新**: 2026-03-27  
**维护团队**: ERP 低代码开发团队
