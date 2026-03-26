# ERP 低代码后端优化方案 - 极简版实施指南

**文档类型**: 快速实施指南  
**生成时间**: 2026-03-25  
**优化目标**: 消除中间转换层，前后端直接使用数据库字段名

---

## 一、核心思想

### 1.1 优化原则

 **三零政策**:
1. **零新增代码** - 不创建任何 Service、VO、Mapper
2. **零性能开销** - 无字段映射转换
3. **零维护成本** - 配置即数据库

 **三个直接**:
1. 配置 JSON 中的字段 = **数据库字段名**
2. 前端传入参数 = **数据库字段名**
3. 后端返回数据 = **数据库字段名**

### 1.2 对比表

| 维度 |  错误做法 |  正确做法 |
|------|----------|----------|
| 配置字段 | `customerName` (业务字段) | `fcustomername` (数据库字段) |
| 查询参数 | `{customerName: "xxx"}` | `{fcustomername: "xxx"}` |
| 返回数据 | `{customerName: "xxx"}` | `{fcustomername: "xxx"}` |
| 映射逻辑 | 需要字段映射服务 | 无需映射 |

---

## 二、现状问题

### 2.1 配置 JSON 混用字段名

**错误示例** ():
```json
{
  "searchConfig": {
    "fields": [
      {
        "field": "customerName",      //  业务字段名
        "label": "客户名称"
      },
      {
        "field": "fbillNo",           //  数据库字段
        "label": "单据编号"
      }
    ]
  }
}
```

### 2.2 不必要的复杂性

-  前端需要 `getDictOptions()` 进行字典映射
-  后端 `DynamicQueryEngine` 使用硬编码的字段白名单
-  存在字段名转换的潜在需求（databaseField ↔ businessField）

---

## 三、实施步骤

### 3.1 准备工作 (0.5 天)

#### Step 1: 梳理数据库字段

```sql
-- 查询销售订单表字段
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 't_sale_order'
ORDER BY ORDINAL_POSITION;

-- 输出示例：
-- fid, fbillno, fcustomerid, fcustomername, famount, ...
```

#### Step 2: 生成字段对照表（仅参考）

```markdown
| 业务说明 | 数据库字段名 | 类型 | 说明 |
|---------|------------|------|------|
| 订单编号 | fbillno | VARCHAR(50) | 主键 |
| 客户名称 | fcustomername | VARCHAR(200) | 客户全称 |
| 订单金额 | famount | DECIMAL(18,6) | 含税金额 |
```

**注意**: 此对照表仅用于开发参考，**不需要在代码中实现映射逻辑**

#### Step 3: 备份现有配置

```sql
-- 导出数据库中的配置
SELECT config_id, module_code, config_name, config_content 
INTO OUTFILE 'D:/backup/erp_configs_20260325.json'
FROM erp_page_config;
```

### 3.2 修改配置 JSON (1-2 天)

#### 修改前 ()

```json
{
  "searchConfig": {
    "fields": [
      {
        "field": "customerName",      //  业务字段名
        "label": "客户名称",
        "component": "input"
      }
    ]
  },
  "tableConfig": {
    "columns": [
      {
        "prop": "customerName",       //  业务字段名
        "label": "客户名称"
      }
    ]
  }
}
```

#### 修改后 ()

```json
{
  "searchConfig": {
    "fields": [
      {
        "field": "fcustomername",     //  数据库字段名
        "label": "客户名称",
        "component": "input"
      }
    ]
  },
  "tableConfig": {
    "columns": [
      {
        "prop": "fcustomername",      //  数据库字段名
        "label": "客户名称"
      }
    ]
  }
}
```

### 3.3 后端简化 (0.5 天)

#### DynamicQueryEngine 简化

**删除硬编码的字段白名单**:

```java
//  删除这段代码
private static final Set<String> ALLOWED_FIELDS = Set.of(
    "fbillNo", "fOraBaseProperty", "fDocumentStatus", "fBillAmount",
    "fdate", "fCustomerNumber", "fCustomerName", "fCreatorId",
    // ... 这个白名单需要手动维护，麻烦且容易出错
);

//  简化为简单校验
private boolean isValidField(String field) {
    return StringUtils.isNotEmpty(field);  // 只校验非空
}
```

#### ErpEngineController 保持不变

```java
//  现有的 executeDynamicQuery 已经是正确的
@PostMapping("/query/execute")
public R<?> executeDynamicQuery(@RequestBody Map<String, Object> params) {
    String moduleCode = (String) params.get("moduleCode");
    Map<String, Object> queryParams = (Map<String, Object>) params.get("queryParams");
    
    // queryParams 中的字段名已经是数据库字段名，无需转换
    QueryWrapper<Object> queryWrapper = 
        queryEngine.buildQueryConditions(queryWrapper, searchConfig, queryParams);
    
    // 直接查询，返回数据库字段名
    Page<Map<String, Object>> page = dataPermissionService.selectPageByModule(
        moduleCode, pageQuery, queryWrapper);
    
    return R.ok(page);
}
```

### 3.4 前端保持不变 (0 天)

#### BusinessConfigurable.vue

```javascript
//  现有的 handleQuery 已经是正确的
const handleQuery = async () => {
  const params = {
    pageNum: queryParams.value.pageNum,
    pageSize: queryParams.value.pageSize,
    ...queryParams.value  // 直接传递，无需转换
  }
  
  const apiMethod = await getApiMethod('list')
  const response = await apiMethod(params)
  
  tableData.value = response.rows || response.data  // 字段名已是数据库字段名
  total.value = response.total
}
```

#### ERPConfigParser.js

```javascript
//  parseSearchForm 已经是正确的
parseSearchForm() {
  return {
    fields: searchConfig.fields.map(field => ({
      ...field,
      // field 已经是数据库字段名，无需额外处理
    }))
  }
}
```

### 3.5 更新 SQL 脚本 (0.5 天)

```sql
-- init-all-in-one.sql 更新配置内容
UPDATE erp_page_config 
SET config_content = '{
  "searchConfig": {
    "fields": [
      {
        "field": "fbillno",
        "label": "单据编号",
        "component": "input"
      },
      {
        "field": "fcustomername",
        "label": "客户名称",
        "component": "input"
      }
    ]
  }
}'
WHERE module_code = 'saleOrder';
```

### 3.6 测试验证 (0.5 天)

#### 快速验证清单

- [ ] 搜索功能测试（每个搜索字段）
- [ ] 表格列显示测试
- [ ] 表单编辑测试
- [ ] 数据提交测试
- [ ] **确认传入参数 = 数据库字段名**
- [ ] **确认返回数据 = 数据库字段名**

#### 验证方法

```javascript
// 浏览器控制台检查
console.log('查询参数:', queryParams);
// 应该输出：{ fbillno: 'SO001', fcustomername: '测试客户' }

console.log('表格数据:', tableData.rows[0]);
// 应该输出：{ fid: '1', fbillno: 'SO001', fcustomername: '测试客户' }
```

---

## 四、工作量估算

| 任务 | 工时 (人天) | 备注 |
|------|------------|------|
| 数据库字段梳理 | 0.5 | 输出对照表（参考用） |
| 配置 JSON 修改 | 1-2 | 按模块数量浮动 |
| SQL 脚本更新 | 0.5 | 包含测试脚本 |
| 后端简化 | 0.5 | 删除冗余逻辑 |
| 前端简化 | 0 | 基本无需修改 |
| 测试验证 | 0.5 | 快速验证 |
| **总计** | **3.5-4.5** | 约 0.5 周 |

---

## 五、关键优势

### 5.1 对比"字段映射服务版"

| 维度 | 极简版 | 字段映射服务版 |
|------|-------|--------------|
| 新增文件 | 0 个 | 3 个 (Service+VO+Mapper) |
| 代码行数 | ~0 行 | ~300 行 |
| 性能开销 | 零开销 | 每次查询需映射 |
| 维护成本 | 极低 | 中等 |
| 学习成本 | 低 | 中 |
| 推荐指数 |  |  |

### 5.2 核心价值

1.  **零新增代码** - 不创建任何 Service、VO、Mapper
2.  **零性能开销** - 无字段映射转换
3.  **零维护成本** - 配置即数据库
4.  **开发友好** - 新人可直接查看数据库字段

---

## 六、风险提示

| 风险项 | 影响程度 | 缓解措施 |
|--------|---------|---------|
| 配置修改错误 | 🔴 高 | 双人审核 + 测试环境验证 |
| 字段名不一致 | 🟡 中 | 提供字段对照表文档（仅参考） |
| 开发人员不熟悉数据库字段 | 🟢 低 | IDE 提示 + 数据库文档 |

---

## 七、附录

### A. 配置修改检查清单

- [ ] 所有 `searchConfig.fields[].field` 改为数据库字段名
- [ ] 所有 `tableConfig.columns[].prop` 改为数据库字段名
- [ ] 所有 `formConfig.sections[].fields[].field` 改为数据库字段名
- [ ] 更新 SQL 初始化脚本
- [ ] 测试搜索功能
- [ ] 测试表格显示
- [ ] 测试表单编辑
- [ ] 验证传入参数 = 数据库字段名
- [ ] 验证返回数据 = 数据库字段名

### B. 数据库字段对照表模板

```sql
-- 通用模板（仅供参考）
CREATE VIEW v_{module}_field_mapping AS
SELECT 
  '字段中文 1' AS field_name_cn, 'database_field_1' AS database_field, 'VARCHAR(50)' AS data_type
UNION ALL
SELECT '字段中文 2', 'database_field_2', 'DECIMAL(18,6)'
UNION ALL
SELECT '字段中文 3', 'database_field_3', 'DATE';
```

**再次强调**: 此对照表仅用于开发参考，**不需要在代码中实现映射逻辑**

---

**文档结束**

## 快速查阅

- 完整方案文档：[`ERP 低代码前后端模块一致性检查与优化方案.md`](./ERP 低代码前后端模块一致性检查与优化方案.md)
- 配置示例：[`saleOrder.config.json`](../../baiyu-web/src/views/erp/ConfigDrivenPage/saleorder/configs/saleOrder.config.json)
