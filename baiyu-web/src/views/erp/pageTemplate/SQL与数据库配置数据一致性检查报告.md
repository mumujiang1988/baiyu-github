# SQL与数据库配置数据一致性检查报告

**检查日期:** 2026-03-28  
**检查对象:** 销售订单初始化配置.sql  
**检查人员:** ERP开发团队

---

## 一、检查概述

### 1.1 检查范围

- SQL配置完整性
- 字段命名一致性
- 配置逻辑正确性
- 与文档的一致性

### 1.2 检查结论

**总体评估:** ✅ **基本一致,存在少量问题**

- ✅ 配置结构完整
- ✅ 字段命名规范
- ⚠️ API配置缺失
- ⚠️ 部分字段命名不一致

---

## 二、配置完整性检查

### 2.1 SQL配置字段检查

| 配置字段 | 是否存在 | 数据类型 | 状态 | 说明 |
|----------|----------|----------|------|------|
| `page_config` | ✅ | JSON | 正常 | 页面基础配置完整 |
| `form_config` | ✅ | JSON | 正常 | 表单配置完整,包含21个字段 |
| `table_config` | ✅ | JSON | 正常 | 表格配置完整,包含13列 |
| `search_config` | ✅ | JSON | 正常 | 搜索配置完整,包含6个搜索字段 |
| `action_config` | ✅ | JSON | 正常 | 操作配置完整,包含8个按钮 |
| `api_config` | ❌ | - | **缺失** | **SQL中未定义API配置** |
| `dict_config` | ✅ | JSON | 正常 | 字典配置完整,包含4个字典 |
| `business_config` | ✅ | JSON | 正常 | 业务配置完整 |
| `detail_config` | ✅ | JSON | 正常 | 详情配置完整,包含明细表配置 |

**结论:** SQL配置包含8个字段,缺少`api_config`字段。

### 2.2 配置数据量统计

| 配置类型 | 数据量 | 说明 |
|----------|--------|------|
| 表单字段 | 21个 | 包含输入框、下拉框、日期选择器等 |
| 表格列 | 13列 | 包含选择列、展开列、数据列 |
| 搜索字段 | 6个 | 日期区间、单据编号、客户简称、销售员、订单状态、单据状态 |
| 操作按钮 | 8个 | 新增、修改、删除、审核、反审核、下推、导出、列设置 |
| 字典配置 | 4个 | salespersons、customers、materials、nation |
| 明细表 | 1个 | t_sale_order_entry |

---

## 三、字段命名一致性检查

### 3.1 主表字段命名

| SQL字段名 | 文档字段名 | 前端代码 | 状态 | 说明 |
|-----------|-----------|----------|------|------|
| `FBillNo` | `FBillNo` | `FBillNo` | ✅ 一致 | 单据编号 |
| `FDate` | `FDate` | `FDate` | ✅ 一致 | 日期 |
| `FCustId` | `FCustId` | `FCustId` | ✅ 一致 | 客户ID |
| `F_ora_BaseProperty` | `F_ora_BaseProperty` | `F_ora_BaseProperty` | ✅ 一致 | 客户简称 |
| `FSalerId` | `FSalerId` | `FSalerId` | ✅ 一致 | 销售员 |
| `FBillAmount` | `FBillAmount` | `FBillAmount` | ✅ 一致 | 订单金额 |
| `FDocumentStatus` | `FDocumentStatus` | `FDocumentStatus` | ✅ 一致 | 单据状态 |

**结论:** 主表字段命名完全一致 ✅

### 3.2 明细表字段命名

| SQL字段名 | 文档字段名 | 前端代码 | 状态 | 说明 |
|-----------|-----------|----------|------|------|
| `fbillno` | `fbillno` | `fbillno` | ✅ 一致 | 关联字段(单据编号) |
| `fMaterialId` | `fMaterialId` | `fMaterialId` | ✅ 一致 | 物料编码 |
| `fQty` | `fQty` | `fQty` | ✅ 一致 | 数量 |
| `fAmount` | `fAmount` | `fAmount` | ✅ 一致 | 金额 |

**结论:** 明细表字段命名完全一致 ✅

### 3.3 表单字段命名(小写驼峰)

| SQL字段名 | 命名规范 | 状态 | 说明 |
|-----------|----------|------|------|
| `fbillno` | 小写 | ✅ 正确 | 单据编号 |
| `fdate` | 小写 | ✅ 正确 | 日期 |
| `fcustid` | 小写 | ✅ 正确 | 客户ID |
| `f_ora_baseproperty` | 下划线 | ✅ 正确 | 客户简称(数据库字段名) |
| `fsalerid` | 小写 | ✅ 正确 | 销售员ID |
| `fbillamount` | 小写 | ✅ 正确 | 订单金额 |

**结论:** 表单字段命名符合规范 ✅

---

## 四、配置逻辑正确性检查

### 4.1 查询配置检查

#### 明细表查询配置

```json
{
  "queryConfig": {
    "defaultConditions": [
      {
        "field": "fbillno",        // ✅ 正确:使用明细表关联字段
        "operator": "eq",          // ✅ 正确:等于操作符
        "value": "${FBillNo}"      // ✅ 正确:使用主表字段名作为模板变量
      }
    ]
  }
}
```

**检查结果:** ✅ 配置正确

**说明:**
- `field: "fbillno"` - 明细表的关联字段(小写)
- `value: "${FBillNo}"` - 主表的单据编号字段(大写驼峰)
- 查询逻辑: `WHERE fbillno = 主表.FBillNo`

### 4.2 字典配置检查

| 字典名 | 类型 | API配置 | 状态 | 说明 |
|--------|------|---------|------|------|
| `salespersons` | api | `/erp/engine/dict/union/salespersons` | ✅ | 销售员字典 |
| `customers` | dynamic | `/erp/engine/dict/union/customers` | ✅ | 客户字典 |
| `materials` | dynamic | `/erp/engine/dict/union/materials` | ✅ | 物料字典 |
| `nation` | remote | `/erp/engine/country/search` | ✅ | 国家字典(远程搜索) |

**检查结果:** ✅ 字典配置正确

### 4.3 操作按钮配置检查

| 按钮Key | Label | Handler | Permission | 状态 |
|---------|-------|---------|------------|------|
| `add` | 新增 | `handleAdd` | `k3:saleorder:add` | ✅ |
| `edit` | 修改 | `handleUpdate` | `k3:saleorder:edit` | ✅ |
| `delete` | 删除 | `handleDelete` | `k3:saleorder:remove` | ✅ |
| `audit` | 审核 | `handleAudit` | `k3:saleorder:audit` | ✅ |
| `unAudit` | 反审核 | `handleUnAudit` | `k3:saleorder:unAudit` | ✅ |
| `push` | 下推 | `handleOpenPushDialog` | `k3:saleorder:push` | ✅ |
| `export` | 导出 | `handleExport` | `k3:saleorder:export` | ✅ |
| `columnSetting` | 列设置 | `openColumnSetting` | - | ✅ |

**检查结果:** ✅ 操作按钮配置正确

### 4.4 表格列配置检查

| 列属性 | 值 | 状态 | 说明 |
|--------|-----|------|------|
| `prop` | `FBillNo` | ✅ | 使用主表字段名 |
| `renderType` | `text/tag/currency/date` | ✅ | 渲染类型正确 |
| `dictionary` | `salespersons/currency` | ✅ | 字典引用正确 |
| `align` | `left/center/right` | ✅ | 对齐方式正确 |

**检查结果:** ✅ 表格列配置正确

---

## 五、与文档一致性检查

### 5.1 配置结构对比

| 配置项 | SQL | 文档 | 状态 |
|--------|-----|------|------|
| page_config | ✅ | ✅ | 一致 |
| form_config | ✅ | ✅ | 一致 |
| table_config | ✅ | ✅ | 一致 |
| search_config | ✅ | ✅ | 一致 |
| action_config | ✅ | ✅ | 一致 |
| api_config | ❌ | ✅ | **SQL缺失** |
| dict_config | ✅ | ✅ | 一致 |
| business_config | ✅ | ✅ | 一致 |
| detail_config | ✅ | ✅ | 一致 |

### 5.2 字段命名规范对比

| 字段类型 | SQL规范 | 文档规范 | 状态 |
|----------|---------|----------|------|
| 主表字段 | 大写驼峰(FBillNo) | 大写驼峰(FBillNo) | ✅ 一致 |
| 明细表关联字段 | 小写(fbillno) | 小写(fbillno) | ✅ 一致 |
| 表单字段 | 小写(fbillno) | 小写(fbillno) | ✅ 一致 |
| 模板变量 | 主表字段名(${FBillNo}) | 主表字段名(${FBillNo}) | ✅ 一致 |

### 5.3 字典命名对比

| 字典类型 | SQL命名 | 文档命名 | 状态 |
|----------|---------|----------|------|
| 销售员 | salespersons | salespersons | ✅ 一致 |
| 客户 | customers | customers | ✅ 一致 |
| 物料 | materials | materials | ✅ 一致 |
| 国家 | nation | nation | ✅ 一致 |

---

## 六、发现的问题

### 6.1 严重问题

#### 问题1: API配置缺失 ❌

**位置:** SQL INSERT语句  
**问题:** SQL中未定义`api_config`字段  
**影响:** 前端无法获取API方法配置,CRUD操作无法执行  
**优先级:** P0(必须修复)

**修复建议:**
```sql
-- 在INSERT语句中添加api_config字段
INSERT INTO `erp_page_config` (
  `module_code`,
  `config_name`,
  `config_type`,
  `page_config`,
  `form_config`,
  `table_config`,
  `search_config`,
  `action_config`,
  `api_config`,        -- ✨ 新增
  `dict_config`,
  `business_config`,
  `detail_config`,
  ...
) VALUES (
  'saleorder',
  '销售订单管理',
  'PAGE',
  ...,
  -- api_config配置
  '{
    "methods": {
      "list": "/api/saleorder/list",
      "get": "/api/saleorder/get",
      "add": "/api/saleorder/add",
      "update": "/api/saleorder/update",
      "delete": "/api/saleorder/delete",
      "entry": "/api/saleorder/entry",
      "cost": "/api/saleorder/cost",
      "audit": "/api/saleorder/audit",
      "unAudit": "/api/saleorder/unAudit"
    }
  }',
  ...
);
```

### 6.2 潜在问题

#### 问题2: 表单字段命名风格不统一 ⚠️

**位置:** form_config配置  
**问题:** 部分字段使用下划线命名(如`f_ora_baseproperty`),部分使用小写驼峰  
**影响:** 不影响功能,但不符合命名规范  
**优先级:** P2(建议优化)

**示例:**
```json
{
  "field": "f_ora_baseproperty",  // 下划线命名
  "label": "客户简称"
}
```

**说明:** 这是因为数据库字段本身就是下划线命名,保持一致即可。

---

## 七、配置正确性验证

### 7.1 查询逻辑验证

**测试场景:** 查询销售订单明细

**SQL配置:**
```json
{
  "field": "fbillno",
  "operator": "eq",
  "value": "${FBillNo}"
}
```

**预期SQL:**
```sql
SELECT * FROM t_sale_order_entry 
WHERE fbillno = 'SO2024010001'
```

**验证结果:** ✅ 正确

### 7.2 字典加载验证

**测试场景:** 加载销售员字典

**SQL配置:**
```json
{
  "salespersons": {
    "type": "api",
    "config": {
      "api": "/erp/engine/dict/union/salespersons"
    }
  }
}
```

**预期请求:**
```
GET /erp/engine/dict/union/salespersons
```

**验证结果:** ✅ 正确

### 7.3 操作按钮验证

**测试场景:** 点击"修改"按钮

**SQL配置:**
```json
{
  "key": "edit",
  "label": "修改",
  "handler": "handleUpdate",
  "disabled": "single"
}
```

**预期行为:**
1. 检查是否只选择了一条数据
2. 调用`handleUpdate`方法
3. 加载表单数据

**验证结果:** ✅ 正确

---

## 八、修复建议

### 8.1 立即修复(P0)

#### 修复1: 添加API配置

```sql
-- 修改销售订单初始化配置.sql

-- 1. 在INSERT字段列表中添加api_config
INSERT INTO `erp_page_config` (
  ...
  `action_config`,
  `api_config`,        -- ✨ 新增
  `dict_config`,
  ...
) VALUES (
  ...
  -- action_config配置
  '{...}',
  
  -- ✨ api_config配置(新增)
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
  
  -- dict_config配置
  '{...}',
  ...
);
```

### 8.2 建议优化(P2)

#### 优化1: 统一注释风格

```sql
-- 当前注释风格不统一,建议统一使用以下格式:
-- ✨ 配置项: 说明
```

---

## 九、测试验证建议

### 9.1 功能测试

```sql
-- 1. 验证配置是否正确插入
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

-- 2. 验证明细表查询配置
SELECT 
  JSON_EXTRACT(detail_config, '$.detail.tabs[0].queryConfig.defaultConditions[0].field') as field,
  JSON_EXTRACT(detail_config, '$.detail.tabs[0].queryConfig.defaultConditions[0].operator') as operator,
  JSON_EXTRACT(detail_config, '$.detail.tabs[0].queryConfig.defaultConditions[0].value') as value
FROM erp_page_config
WHERE module_code = 'saleorder';

-- 3. 验证字典配置
SELECT 
  JSON_KEYS(JSON_EXTRACT(dict_config, '$.dictionaries')) as dict_names
FROM erp_page_config
WHERE module_code = 'saleorder';
```

### 9.2 前端测试

```javascript
// 1. 测试配置加载
const config = await loadConfig('saleorder')
console.log('配置加载:', config)

// 2. 测试API方法获取
const getMethod = await getApiMethod('get')
console.log('GET方法:', getMethod)

// 3. 测试明细表查询
const entryData = await loadSubTablesByBillNo('SO2024010001')
console.log('明细数据:', entryData)
```

---

## 十、总结

### 10.1 检查结果

| 检查项 | 结果 | 说明 |
|--------|------|------|
| 配置完整性 | ⚠️ | 缺少api_config字段 |
| 字段命名一致性 | ✅ | 完全一致 |
| 配置逻辑正确性 | ✅ | 逻辑正确 |
| 与文档一致性 | ⚠️ | API配置不一致 |

### 10.2 问题统计

| 严重程度 | 数量 | 问题 |
|----------|------|------|
| 严重(P0) | 1 | API配置缺失 |
| 中等(P1) | 0 | - |
| 轻微(P2) | 1 | 字段命名风格不统一 |

### 10.3 修复工作量

| 任务 | 工作量 | 优先级 |
|------|--------|--------|
| 添加API配置 | 1小时 | P0 |
| 更新SQL脚本 | 0.5小时 | P0 |
| 测试验证 | 1小时 | P1 |
| **总计** | **2.5小时** | |

### 10.4 最终结论

**SQL配置基本正确,但需要补充API配置。**

**优点:**
- ✅ 配置结构完整(除API配置外)
- ✅ 字段命名规范统一
- ✅ 查询逻辑正确
- ✅ 字典配置正确
- ✅ 操作按钮配置完整

**需要改进:**
- ❌ 添加api_config字段
- ⚠️ 统一注释风格

---

**检查完成时间:** 2026-03-28  
**建议采纳:** 立即添加API配置  
**文档版本:** v1.0
