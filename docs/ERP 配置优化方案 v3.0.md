# ERP 配置优化方案 v3.0 - 完全拆分版

## 📋 优化概述

### 核心变更
将 ERP 配置从**6 字段**升级为**8 字段**，实现更清晰的职责分离：

#### 新增字段
1. **search_config** - 搜索区域配置（从 table_config.queryBuilder 拆分）
2. **action_config** - 按钮操作配置（从 business_config.buttons 拆分）

#### 最终架构（8 字段）
```
erp_page_config:
├── page_config       - 页面基础配置
├── form_config       - 表单 UI 组件配置
├── table_config      - 表格列配置（精简为纯表格定义）
├── search_config     - ✨ 查询表单配置（新增）
├── action_config     - ✨ 按钮操作配置（新增）
├── dict_config       - 字典数据源配置
├── business_config   - 业务规则配置（移除 buttons 后更纯粹）
└── detail_config     - 详情页配置
```

---

## 🎯 优化目标

### 1. 职责分离
- **search_config**: 专注搜索区域的字段、条件、运算符
- **table_config**: 专注表格列的渲染、排序、显示
- **action_config**: 专注按钮的权限、事件、位置

### 2. 前端解析优化
```javascript
// ✅ 新逻辑 - 直接读取，无需转换
parseSearchForm() {
  const { searchConfig } = this.config;
  return {
    showSearch: searchConfig?.showSearch !== false,
    fields: searchConfig?.fields || []
  };
}

parseActions() {
  const { actionConfig } = this.config;
  return {
    toolbar: actionConfig?.toolbar || [],
    row: actionConfig?.row || []
  };
}
```

### 3. 后端接口简化
```java
// ✅ 返回结构更清晰
{
  "code": 200,
  "data": {
    "pageConfig": {...},
    "formConfig": {...},
    "tableConfig": {...},
    "searchConfig": {...},  // ← 独立字段
    "actionConfig": {...},  // ← 独立字段
    "dictConfig": {...},
    "businessConfig": {...},
    "detailConfig": {...}
  }
}
```

---

## 📦 文件清单

### SQL 脚本
| 文件名 | 用途 | 说明 |
|--------|------|------|
| `upgrade-to-v3.sql` | 表结构升级 | 添加 search_config 和 action_config 字段 |
| `拆分 json 导入-v3.sql` | 数据导入 | 销售订单模块完整配置（8 字段版本） |

### 配置文件（split 目录）
| 文件 | 对应数据库字段 | 说明 |
|------|---------------|------|
| `page.json` | page_config | 页面基础配置 |
| `form.json` | form_config | 表单配置 |
| `table.json` | table_config | 表格列配置 |
| `search.json` | search_config | ✨ 搜索配置（新增） |
| `config.json` | action_config | ✨ 按钮配置（已调整） |
| `dict.json` | dict_config | 字典配置 |
| `detail.json` | detail_config | 详情配置 |

---

## 🚀 实施步骤

### Step 1: 备份现有数据
```sql
-- 自动备份到 erp_page_config_backup_20260327
```

### Step 2: 执行表结构升级
```bash
mysql -u root -p test < upgrade-to-v3.sql
```

**执行结果：**
- ✅ 添加 `search_config` 字段
- ✅ 添加 `action_config` 字段
- ✅ 同步更新历史表
- ✅ 更新触发器支持新字段

### Step 3: 导入新配置数据
```bash
mysql -u root -p test < 拆分 json 导入-v3.sql
```

**验证结果：**
```
页面配置字段数：21
搜索字段数：6
工具栏按钮数：8
表格列数：13
字典数量：10
详情页签数：2
```

### Step 4: 更新前端解析器
修改 `ERPConfigParser.js`:

```javascript
// ✅ parseSearchForm - 直接读取 searchConfig
parseSearchForm() {
  const { searchConfig } = this.config;
  if (!searchConfig) return { showSearch: false, fields: [] };
  
  return {
    showSearch: searchConfig.showSearch !== false,
    defaultExpand: searchConfig.defaultExpand !== false,
    fields: searchConfig.fields.map(field => ({
      ...field,
      componentType: this.getComponentType(field.component),
      queryOperator: field.queryOperator || 'eq'
    }))
  };
}

// ✅ parseActions - 直接读取 actionConfig
parseActions() {
  const { actionConfig } = this.config;
  if (!actionConfig) return { toolbar: [], row: [] };
  
  return {
    toolbar: actionConfig.toolbar || [],
    row: actionConfig.row || []
  };
}
```

### Step 5: 重启后端服务
```bash
# 终止旧进程
taskkill /F /PID <后端进程 ID>

# 编译启动
mvn clean package -DskipTests
java -jar target/ruoyi-admin-wms.jar
```

---

## 📊 优化效果对比

### Before (v2.1 - 6 字段)
```json
{
  "table_config": {
    "queryBuilder": {
      "fields": [...]  // ← 搜索条件混在表格配置中
    },
    "columns": [...]
  },
  "business_config": {
    "buttons": [...],  // ← 按钮和业务规则混在一起
    "messages": {...}
  }
}
```

**问题：**
- ❌ 职责不清：搜索条件放在表格配置中
- ❌ 解析复杂：需要从 queryBuilder 中提取字段
- ❌ 维护困难：按钮配置和业务规则耦合

### After (v3.0 - 8 字段)
```json
{
  "search_config": {
    "showSearch": true,
    "fields": [...]  // ✅ 独立的搜索配置
  },
  "action_config": {
    "toolbar": [...],  // ✅ 独立的按钮配置
    "row": []
  },
  "table_config": {
    "columns": [...]  // ✅ 纯粹的表格列定义
  },
  "business_config": {
    "messages": {...}  // ✅ 纯粹的业务规则
  }
}
```

**优势：**
- ✅ 职责清晰：每个字段专注于单一功能
- ✅ 解析简单：直接读取，无需转换
- ✅ 易于维护：各配置独立演进，互不影响

---

## 🔧 关键技术点

### 1. search_config 结构
```json
{
  "showSearch": true,
  "defaultExpand": true,
  "fields": [
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

### 2. action_config 结构
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
    }
  ],
  "row": []
}
```

### 3. 数据库字段顺序
```sql
ALTER TABLE erp_page_config 
ADD COLUMN search_config JSON AFTER table_config,
ADD COLUMN action_config JSON AFTER search_config;
```

---

## ⚠️ 注意事项

### 1. 数据迁移
- 升级前务必备份现有数据
- `upgrade-to-v3.sql` 会自动创建备份表

### 2. 兼容性
- ❌ **不向后兼容**：采用全新逻辑
- ✅ 新配置必须使用 8 字段格式
- ✅ 旧配置需要重新导入

### 3. 前端更新
- 必须更新 `ERPConfigParser.js` 支持新字段
- BusinessConfigurable.vue 无需修改（已适配）

### 4. 后端更新
- ErpPageConfigController 无需修改（自动映射 JSON 字段）
- 确保后端服务重启以加载新配置

---

## 📝 验证清单

### 数据库层面
- [ ] search_config 字段已添加
- [ ] action_config 字段已添加
- [ ] 历史表已同步更新
- [ ] 触发器已更新

### 配置数据层面
- [ ] 成功导入 saleorder 模块配置
- [ ] search_config 包含 6 个搜索字段
- [ ] action_config 包含 8 个工具栏按钮
- [ ] table_config 只包含表格列定义

### 前端层面
- [ ] 搜索区域正常显示
- [ ] 工具栏按钮正常显示
- [ ] 查询功能正常工作
- [ ] 按钮权限正常控制

### 后端层面
- [ ] 接口返回 8 个配置字段
- [ ] data 字段不为 null
- [ ] msg 字段为 null 或"操作成功"

---

## 🎯 总结

### 核心改进
1. **架构优化**：从 6 字段 → 8 字段，职责更清晰
2. **代码简化**：前端解析逻辑更直接，无转换
3. **维护性提升**：各配置独立，易于扩展

### 技术债务清理
- ✅ 移除了 `queryBuilder.fields` 的双重职责
- ✅ 分离了 `business_config.buttons` 的耦合
- ✅ 统一了前后端配置结构

### 最佳实践
- 📌 每个 JSON 字段只负责一件事
- 📌 前端直接读取，不做复杂转换
- 📌 后端保持简洁，不解析配置内容

---

## 📞 下一步计划

1. **其他模块迁移**：将其他业务模块升级到 v3.0 架构
2. **配置管理界面**：开发可视化配置编辑工具
3. **版本控制**：实现配置版本管理和回滚功能
4. **性能优化**：添加配置缓存机制

---

**文档版本**: v3.0  
**更新日期**: 2026-03-27  
**维护团队**: JMH
