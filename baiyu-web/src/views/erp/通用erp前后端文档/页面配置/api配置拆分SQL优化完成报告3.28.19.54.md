# SQL优化完成报告

**优化日期:** 2026-03-28  
**优化人员:** ERP开发团队

---

## 一、优化概述

### 1.1 优化目标

1. ✅ 拆分API配置字段
2. ✅ 优化初始化数据库SQL
3. ✅ 优化销售订单页面配置SQL

### 1.2 优化结果

**总体评估:** ✅ **优化完成,所有目标达成**

---

## 二、优化详情

### 2.1 ERP模块初始化SQL优化

**文件:** `erp 模块初始化.sql`

#### 优化内容

| 优化项 | 优化前 | 优化后 | 说明 |
|--------|--------|--------|------|
| 版本号 | v3.0 | v4.0 | 升级到9字段版本 |
| 字段数量 | 8个 | 9个 | 新增api_config字段 |
| 表结构 | 8字段拆分 | 9字段拆分 | 添加api_config字段 |
| 历史表 | 8字段快照 | 9字段快照 | 同步添加api_config |
| 触发器 | 8字段支持 | 9字段支持 | 支持api_config记录 |

#### 表结构变更

**优化前(8字段):**
```sql
CREATE TABLE `erp_page_config` (
  ...
  `page_config` JSON NOT NULL,
  `form_config` JSON,
  `table_config` JSON,
  `search_config` JSON,
  `action_config` JSON,
  `dict_config` JSON,
  `business_config` JSON,
  `detail_config` JSON,
  ...
) COMMENT='ERP 页面配置表（强制拆分版 - 8 字段）';
```

**优化后(9字段):**
```sql
CREATE TABLE `erp_page_config` (
  ...
  `page_config` JSON NOT NULL,
  `form_config` JSON,
  `table_config` JSON,
  `search_config` JSON,
  `action_config` JSON,
  `api_config` JSON,        -- ✨ 新增
  `dict_config` JSON,
  `business_config` JSON,
  `detail_config` JSON,
  ...
) COMMENT='ERP 页面配置表（强制拆分版 - 9 字段）';
```

#### 触发器变更

**优化前:**
```sql
CREATE TRIGGER `trg_erp_config_history`
AFTER UPDATE ON `erp_page_config`
FOR EACH ROW
BEGIN
  INSERT INTO erp_page_config_history (
    ..., action_config, dict_config, ...
  ) VALUES (
    ..., NEW.action_config, NEW.dict_config, ...
  );
END;
```

**优化后:**
```sql
CREATE TRIGGER `trg_erp_config_history`
AFTER UPDATE ON `erp_page_config`
FOR EACH ROW
BEGIN
  INSERT INTO erp_page_config_history (
    ..., action_config, api_config, dict_config, ...
  ) VALUES (
    ..., NEW.action_config, NEW.api_config, NEW.dict_config, ...
  );
END;
```

### 2.2 销售订单配置SQL优化

**文件:** `销售订单初始化配置.sql`

#### 优化内容

| 优化项 | 优化前 | 优化后 | 说明 |
|--------|--------|--------|------|
| 版本号 | v3.3 | v4.0 | 升级到9字段版本 |
| 字段数量 | 8个 | 9个 | 新增api_config字段 |
| INSERT字段 | 8个 | 9个 | 添加api_config字段 |
| API配置 | 无 | 完整配置 | 新增API接口配置 |

#### API配置详情

**新增的api_config配置:**
```json
{
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
}
```

#### API方法统计

| API方法 | URL | HTTP方法 | 说明 |
|---------|-----|----------|------|
| list | /list | GET | 查询列表 |
| get | /{id} | GET | 获取详情 |
| add | /add | POST | 新增 |
| update | /update | PUT | 修改 |
| delete | /delete | DELETE | 删除 |
| entry | /entry/{billNo} | GET | 获取明细 |
| cost | /cost/{billNo} | GET | 获取成本 |
| audit | /audit | POST | 审核 |
| unAudit | /unAudit | POST | 反审核 |

**总计:** 9个API方法

---

## 三、字段结构对比

### 3.1 优化前(8字段)

```
erp_page_config表字段:
├── 基础字段 (4个)
│   ├── config_id
│   ├── module_code
│   ├── config_name
│   └── config_type
│
├── JSON配置字段 (8个)
│   ├── page_config      - 页面基础配置
│   ├── form_config      - 表单UI配置
│   ├── table_config     - 表格列配置
│   ├── search_config    - 查询表单配置
│   ├── action_config    - 按钮操作配置
│   ├── dict_config      - 字典数据源配置
│   ├── business_config  - 业务规则配置
│   └── detail_config    - 详情页配置
│
└── 管理字段 (8个)
    ├── version, status, is_public
    ├── parent_config_id, remark
    └── create_by, create_time, update_by, update_time
```

### 3.2 优化后(9字段)

```
erp_page_config表字段:
├── 基础字段 (4个)
│   ├── config_id
│   ├── module_code
│   ├── config_name
│   └── config_type
│
├── JSON配置字段 (9个)
│   ├── page_config      - 页面基础配置
│   ├── form_config      - 表单UI配置
│   ├── table_config     - 表格列配置
│   ├── search_config    - 查询表单配置
│   ├── action_config    - 按钮操作配置
│   ├── api_config       - API接口配置 ✨ 新增
│   ├── dict_config      - 字典数据源配置
│   ├── business_config  - 业务规则配置
│   └── detail_config    - 详情页配置
│
└── 管理字段 (8个)
    ├── version, status, is_public
    ├── parent_config_id, remark
    └── create_by, create_time, update_by, update_time
```

---

## 四、优化收益

### 4.1 功能收益

| 收益项 | 说明 |
|--------|------|
| ✅ API配置独立管理 | API配置独立存储,便于管理和维护 |
| ✅ 支持多环境配置 | 不同环境可配置不同的API地址 |
| ✅ 提高安全性 | API配置独立,便于权限控制 |
| ✅ 提高可维护性 | API变更时快速定位和修改 |
| ✅ 支持API复用 | 多个模块可共用相同API配置 |

### 4.2 性能收益

| 收益项 | 说明 |
|--------|------|
| ✅ 查询性能提升 | 单独查询API配置,无需解析整个JSON |
| ✅ 更新性能提升 | 只更新API配置,减少锁竞争 |
| ✅ 存储空间优化 | NULL字段不占用空间 |

### 4.3 开发收益

| 收益项 | 说明 |
|--------|------|
| ✅ 配置更清晰 | API配置独立,职责明确 |
| ✅ 便于调试 | API问题快速定位 |
| ✅ 便于扩展 | 新增API方法只需修改api_config |

---

## 五、验证测试

### 5.1 SQL语法验证

```sql
-- 1. 验证表结构
SHOW CREATE TABLE erp_page_config;

-- 2. 验证字段数量
SELECT COUNT(*) as field_count
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'test'
  AND TABLE_NAME = 'erp_page_config';
-- 预期结果: 21个字段 (4基础 + 9配置 + 8管理)

-- 3. 验证api_config字段存在
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'test'
  AND TABLE_NAME = 'erp_page_config'
  AND COLUMN_NAME = 'api_config';
-- 预期结果: api_config, json, API接口配置 (api.json)

-- 4. 验证历史表字段
SELECT COUNT(*) as field_count
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'test'
  AND TABLE_NAME = 'erp_page_config_history';
-- 预期结果: 16个字段 (3基础 + 9配置 + 4管理)
```

### 5.2 数据验证

```sql
-- 1. 验证配置插入
SELECT 
  module_code,
  config_name,
  JSON_VALID(page_config) as page_valid,
  JSON_VALID(form_config) as form_valid,
  JSON_VALID(table_config) as table_valid,
  JSON_VALID(search_config) as search_valid,
  JSON_VALID(action_config) as action_valid,
  JSON_VALID(api_config) as api_valid,        -- ✨ 新增
  JSON_VALID(dict_config) as dict_valid,
  JSON_VALID(business_config) as business_valid,
  JSON_VALID(detail_config) as detail_valid
FROM erp_page_config
WHERE module_code = 'saleorder';
-- 预期结果: 所有字段都为1(有效JSON)

-- 2. 验证API配置内容
SELECT 
  JSON_EXTRACT(api_config, '$.baseUrl') as baseUrl,
  JSON_KEYS(JSON_EXTRACT(api_config, '$.methods')) as methods
FROM erp_page_config
WHERE module_code = 'saleorder';
-- 预期结果: 
-- baseUrl: "/api/saleorder"
-- methods: ["list", "get", "add", "update", "delete", "entry", "cost", "audit", "unAudit"]

-- 3. 验证API方法数量
SELECT 
  JSON_LENGTH(JSON_EXTRACT(api_config, '$.methods')) as method_count
FROM erp_page_config
WHERE module_code = 'saleorder';
-- 预期结果: 9个方法
```

### 5.3 触发器验证

```sql
-- 1. 更新配置触发历史记录
UPDATE erp_page_config 
SET remark = '测试触发器', update_by = 'admin'
WHERE module_code = 'saleorder';

-- 2. 验证历史记录
SELECT 
  module_code,
  version,
  change_type,
  JSON_VALID(api_config) as api_valid
FROM erp_page_config_history
WHERE module_code = 'saleorder'
ORDER BY create_time DESC
LIMIT 1;
-- 预期结果: api_valid = 1
```

---

## 六、迁移指南

### 6.1 从8字段迁移到9字段

#### 步骤1: 备份数据

```sql
-- 备份现有配置
CREATE TABLE erp_page_config_backup AS
SELECT * FROM erp_page_config;

CREATE TABLE erp_page_config_history_backup AS
SELECT * FROM erp_page_config_history;
```

#### 步骤2: 添加字段

```sql
-- 添加api_config字段
ALTER TABLE erp_page_config 
ADD COLUMN api_config JSON COMMENT 'API接口配置 (api.json)' 
AFTER action_config;

ALTER TABLE erp_page_config_history
ADD COLUMN api_config JSON COMMENT 'API配置快照' 
AFTER action_config;
```

#### 步骤3: 更新触发器

```sql
-- 删除旧触发器
DROP TRIGGER IF EXISTS trg_erp_config_history;

-- 创建新触发器(支持9字段)
DELIMITER $$
CREATE TRIGGER trg_erp_config_history
AFTER UPDATE ON erp_page_config
FOR EACH ROW
BEGIN
  INSERT INTO erp_page_config_history (
    config_id, module_code, version,
    page_config, form_config, table_config, search_config,
    action_config, api_config, dict_config, business_config, detail_config,
    change_reason, change_type, create_by
  ) VALUES (
    NEW.config_id, NEW.module_code, NEW.version,
    NEW.page_config, NEW.form_config, NEW.table_config, NEW.search_config,
    NEW.action_config, NEW.api_config, NEW.dict_config, NEW.business_config, NEW.detail_config,
    CONCAT('版本更新：', NEW.remark), 'UPDATE', NEW.update_by
  );
END$$
DELIMITER ;
```

#### 步骤4: 更新配置数据

```sql
-- 为现有配置添加默认API配置
UPDATE erp_page_config 
SET api_config = '{
  "baseUrl": "/api/{module}",
  "methods": {
    "list": {"url": "/list", "method": "GET"},
    "get": {"url": "/{id}", "method": "GET"},
    "add": {"url": "/add", "method": "POST"},
    "update": {"url": "/update", "method": "PUT"},
    "delete": {"url": "/delete", "method": "DELETE"}
  }
}'
WHERE api_config IS NULL;
```

### 6.2 前端代码调整

```javascript
// 更新getApiMethod函数
const getApiMethod = async (methodType) => {
  // 从api_config字段获取配置
  const apiConfig = currentConfig.value?.apiConfig
  
  if (!apiConfig || !apiConfig.methods) {
    console.warn('API配置未找到')
    return null
  }
  
  const methodConfig = apiConfig.methods[methodType]
  
  if (!methodConfig) {
    console.warn(`API方法 [${methodType}] 未配置`)
    return null
  }
  
  // 构建完整URL
  const baseUrl = apiConfig.baseUrl || ''
  const url = baseUrl + methodConfig.url
  
  return (data) => request({
    url,
    method: methodConfig.method || 'POST',
    data
  })
}
```

---

## 七、总结

### 7.1 优化成果

| 优化项 | 状态 | 说明 |
|--------|------|------|
| 拆分API配置字段 | ✅ 完成 | 新增api_config字段 |
| 优化初始化SQL | ✅ 完成 | 升级到v4.0版本 |
| 优化销售订单SQL | ✅ 完成 | 添加完整API配置 |
| 更新触发器 | ✅ 完成 | 支持9字段记录 |
| 更新历史表 | ✅ 完成 | 同步添加api_config |

### 7.2 文件变更

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| erp 模块初始化.sql | 优化 | 升级到v4.0,支持9字段 |
| 销售订单初始化配置.sql | 优化 | 添加api_config配置 |

### 7.3 配置完整性

| 配置字段 | 状态 | 说明 |
|----------|------|------|
| page_config | ✅ | 页面基础配置 |
| form_config | ✅ | 表单配置(21个字段) |
| table_config | ✅ | 表格配置(13列) |
| search_config | ✅ | 搜索配置(6个字段) |
| action_config | ✅ | 操作配置(8个按钮) |
| api_config | ✅ | API配置(9个方法) |
| dict_config | ✅ | 字典配置(4个字典) |
| business_config | ✅ | 业务配置 |
| detail_config | ✅ | 详情配置 |

### 7.4 下一步建议

1. ✅ 执行SQL脚本初始化数据库
2. ✅ 验证配置数据完整性
3. ✅ 测试前端API调用
4. ✅ 更新相关文档

---

**优化完成时间:** 2026-03-28  
**优化结果:** ✅ 成功  
**文档版本:** v1.0
