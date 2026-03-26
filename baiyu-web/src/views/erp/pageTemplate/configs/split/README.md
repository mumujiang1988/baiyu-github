# ERP配置 JSON 强制拆分方案 - 销售订单模块

## 📋 概述

本目录包含销售订单模块的**强制拆分 JSON 配置文件**，符合 ERP 低代码系统行业标准。

## 📁 文件结构

```
configs/split/
├── page.json          # 页面基础配置（标题、权限、路由）
├── form.json          # 表单 UI 组件配置（字段、验证规则）
├── table.json         # 表格查询配置（列定义、查询构造器）
├── dict.json          # 字典数据源配置（动态/静态字典）
└── config.json        # 业务规则配置（按钮、消息、流程）
```

## 🔍 各配置文件说明

### 1. page.json - 页面基础配置

**用途**: 定义页面的基本信息和全局设置

**核心字段**:
```json
{
  "pageId": "saleorder",           // 页面 ID（与 moduleCode 对应）
  "pageName": "销售订单管理",      // 页面名称
  "permission": "k3:saleorder:query", // 默认权限
  "layout": "standard",            // 布局方式
  "apiPrefix": "/erp/engine"       // API 前缀
}
```

**使用场景**:
- 页面初始化时加载
- 权限检查
- 路由配置

---

### 2. form.json - 表单 UI 组件配置

**用途**: 定义新增/编辑弹窗中的表单字段和验证规则

**核心字段**:
```json
{
  "formConfig": {
    "dialogWidth": "1400px",       // 弹窗宽度
    "labelWidth": "120px",         // 标签宽度
    "layout": "horizontal"         // 布局方式
  },
  "fields": [                      // 字段列表
    {
      "field": "fbillno",          // 字段名
      "label": "单据编号",         // 显示标签
      "component": "input",        // 组件类型
      "span": 6,                   // 栅格占比
      "required": true,            // 是否必填
      "rules": [...],              // 验证规则
      "props": {...}               // 组件属性
    }
  ]
}
```

**支持的组件类型**:
- `input` - 文本输入框
- `select` - 下拉选择框
- `date` - 日期选择器
- `daterange` - 日期范围选择器
- `input-number` - 数字输入框
- `textarea` - 多行文本

**使用场景**:
- 新增表单渲染
- 编辑表单渲染
- 表单验证

---

### 3. table.json - 表格查询配置

**用途**: 定义主表格的列定义和查询条件

**核心字段**:
```json
{
  "tableName": "t_sale_order",     // 数据库表名
  "primaryKey": "id",              // 主键字段
  "queryBuilder": {                // 查询构造器
    "enabled": true,
    "fields": [...],               // 查询字段
    "defaultConditions": [...],    // 默认条件
    "defaultOrderBy": [...]        // 默认排序
  },
  "columns": [                     // 表格列定义
    {
      "prop": "FBillNo",           // 字段名
      "label": "单据编号",         // 列标题
      "width": 150,                // 列宽
      "renderType": "text"         // 渲染类型
    }
  ],
  "pagination": {                  // 分页配置
    "defaultPageSize": 10,
    "pageSizeOptions": [10, 20, 50, 100]
  }
}
```

**支持的渲染类型**:
- `text` - 普通文本
- `currency` - 货币格式
- `date` - 日期格式
- `datetime` - 日期时间格式
- `tag` - 标签样式
- `percent` - 百分比
- `number` - 数字格式

**使用场景**:
- 主表格数据查询
- 表格列渲染
- 查询条件构建

---

### 4. dict.json - 字典数据源配置

**用途**: 定义所有字典数据的来源和映射规则

**核心字段**:
```json
{
  "dicts": [                       // 字典列表
    {
      "dictKey": "salespersons",   // 字典键
      "dictType": "dynamic",       // 字典类型（dynamic/static/remote）
      "table": "sys_user",         // 数据表名
      "conditions": [...],         // 查询条件
      "fieldMapping": {            // 字段映射
        "valueField": "user_id",
        "labelField": "nick_name"
      },
      "config": {                  // 额外配置
        "api": "/erp/engine/dictionary/salespersons/data",
        "ttl": 600000
      },
      "cacheable": true,           // 是否缓存
      "cacheTTL": 600000           // 缓存过期时间
    }
  ],
  "globalCacheSettings": {         // 全局缓存设置
    "enabled": true,
    "defaultTTL": 300000
  }
}
```

**字典类型**:
- **dynamic** - 动态字典（从数据库表查询）
- **static** - 静态字典（配置文件中直接定义）
- **remote** - 远程字典（支持搜索的异步加载）

**使用场景**:
- 下拉选择框数据源
- 表格列字典渲染
- 查询条件中的字典过滤

---

### 5. config.json - 业务规则配置

**用途**: 定义按钮权限、消息提示、业务实体等规则

**核心字段**:
```json
{
  "buttons": [                     // 按钮列表
    {
      "key": "add",                // 按钮标识
      "label": "新增",             // 按钮文本
      "icon": "Plus",              // 图标
      "permission": "k3:saleorder:add", // 权限标识
      "type": "primary",           // 按钮类型
      "position": "left"           // 位置
    }
  ],
  "messages": {                    // 消息配置
    "confirmDelete": "是否确认删除？",
    "success": {
      "add": "新增成功"
    }
  },
  "entityName": "销售订单",        // 实体名称
  "entityNameSingular": "订单"     // 实体单数名称
}
```

**使用场景**:
- 工具栏按钮渲染
- 操作确认提示
- 成功/错误消息显示

---

## 🚀 导入 SQL 脚本

### 使用方法

```bash
# 1. 连接到 MySQL 数据库
mysql -u root -p

# 2. 选择数据库
USE test;

# 3. 执行导入脚本
source d:/baiyuyunma/baiyu-github/baiyu-github/scripts/import/import-saleorder-split-config.sql;
```

### 脚本内容

`import-saleorder-split-config.sql` 会：
1. 清理旧的 `saleorder` 配置数据
2. 从 5 个 JSON 文件读取配置并插入到 `erp_page_config` 表
3. 验证导入结果并显示统计信息

---

## ✅ 拆分优势

### 1. **复用性提升**
- ✅ 字典配置全局复用（所有模块共用同一份 `customers` 字典）
- ✅ 查询构造器复用（标准查询条件跨模块共享）
- ✅ 按钮权限复用（通用按钮配置一次定义多处使用）

### 2. **性能优化**
- ✅ 按需加载（表格页面只加载 `table.json`，不加载 `form.json`）
- ✅ 减少内存占用（不解析冗余配置）
- ✅ 字典缓存独立管理

### 3. **维护简单**
- ✅ 改查询只动 `table.json`
- ✅ 改 UI 只动 `form.json`
- ✅ 改字典只动 `dict.json`
- ✅ 改业务流程只动 `config.json`
- ✅ 互不影响，降低风险

### 4. **便于审计**
- ✅ UI 结构 → 独立审计
- ✅ 查询 SQL → 独立审计
- ✅ 字典来源 → 独立审计
- ✅ 权限流程 → 独立审计

---

## 📊 对比分析

| 维度 | 旧方案 (单 JSON) | 新方案 (强制拆分) |
|------|----------------|------------------|
| **文件大小** | ~1159 行 | 平均每个文件 ~200 行 |
| **可读性** | ❌ 差 | ✅ 优 |
| **复用性** | ❌ 不能 | ✅ 极高 |
| **性能** | ❌ 慢 | ✅ 快 |
| **维护** | ❌ 灾难 | ✅ 简单 |
| **审计** | ❌ 无法 | ✅ 专业标准 |

---

## 🎯 最佳实践

### 1. 修改表单字段

**只需编辑 `form.json`**:
```json
// 添加新字段
{
  "field": "new_field",
  "label": "新字段",
  "component": "input",
  "span": 6,
  "required": false
}
```

### 2. 修改表格列

**只需编辑 `table.json`**:
```json
// 添加新列
{
  "prop": "new_column",
  "label": "新列",
  "width": 120,
  "renderType": "text"
}
```

### 3. 添加新字典

**只需编辑 `dict.json`**:
```json
{
  "dictKey": "new_dict",
  "dictType": "dynamic",
  "table": "your_table",
  "fieldMapping": {
    "valueField": "id",
    "labelField": "name"
  }
}
```

### 4. 添加新按钮

**只需编辑 `config.json`**:
```json
{
  "key": "new_button",
  "label": "新按钮",
  "icon": "Star",
  "permission": "k3:saleorder:new_action",
  "type": "info"
}
```

---

## 📝 开发指南

### 前端使用示例

```javascript
// src/views/erp/pageTemplate/configurable/BusinessConfigurable.vue

// 1. 加载配置
const loadConfig = async () => {
  const response = await request({
    url: '/erp/config/get/saleorder',
    method: 'get'
  })
  
  // 解构获取各个配置
  const { pageConfig, formConfig, tableConfig, dictConfig, businessConfig } = response.data
  
  // 2. 使用配置
  parsedConfig.page = parsePageConfig(pageConfig)
  parsedConfig.form = parseFormConfig(formConfig)
  parsedConfig.table = parseTableConfig(tableConfig)
  parsedConfig.dict = parseDictConfig(dictConfig)
  parsedConfig.business = parseBusinessConfig(businessConfig)
}

// 3. 按需加载（推荐）
const loadTableConfigOnly = async () => {
  const response = await request({
    url: '/erp/config/get/saleorder/partial?types=table',
    method: 'get'
  })
  
  parsedConfig.table = parseTableConfig(response.data.tableConfig)
}
```

### 后端接口示例

```java
// ErpPageConfigController.java

/**
 * 获取完整配置（返回 5 个独立字段）
 */
@GetMapping("/get/{moduleCode}")
public R<ErpPageConfigVO> getPageConfig(@PathVariable String moduleCode) {
    ErpPageConfig config = pageConfigService.getByModuleCode(moduleCode);
    
    ErpPageConfigVO vo = new ErpPageConfigVO();
    vo.setPageConfig(JSON.parseObject(config.getPageConfig()));
    vo.setFormConfig(JSON.parseObject(config.getFormConfig()));
    vo.setTableConfig(JSON.parseObject(config.getTableConfig()));
    vo.setDictConfig(JSON.parseObject(config.getDictConfig()));
    vo.setBusinessConfig(JSON.parseObject(config.getBusinessConfig()));
    
    return R.ok(vo);
}

/**
 * 按需加载配置（推荐用于性能优化）
 */
@GetMapping("/get/{moduleCode}/partial")
public R<Map<String, Object>> getPartialConfig(
    @PathVariable String moduleCode,
    @RequestParam List<String> types) {
    
    Map<String, Object> result = new HashMap<>();
    ErpPageConfig config = pageConfigService.getByModuleCode(moduleCode);
    
    for (String type : types) {
        switch (type) {
            case "page":
                result.put("pageConfig", JSON.parseObject(config.getPageConfig()));
                break;
            case "form":
                result.put("formConfig", JSON.parseObject(config.getFormConfig()));
                break;
            case "table":
                result.put("tableConfig", JSON.parseObject(config.getTableConfig()));
                break;
            case "dict":
                result.put("dictConfig", JSON.parseObject(config.getDictConfig()));
                break;
            case "business":
                result.put("businessConfig", JSON.parseObject(config.getBusinessConfig()));
                break;
        }
    }
    
    return R.ok(result);
}
```

---

## 🔗 相关文档

- [ERP配置 JSON 强制拆分方案.md](../../ERP配置 JSON 强制拆分方案.md) - 完整方案文档
- [erp-config-forced-split-init.sql](../../scripts/init/erp-config-forced-split-init.sql) - 初始化脚本
- [import-saleorder-split-config.sql](../../scripts/import/import-saleorder-split-config.sql) - 导入脚本

---

**版本**: v2.0  
**创建时间**: 2026-03-26  
**最后更新**: 2026-03-26  
**维护团队**: ERP 开发团队
