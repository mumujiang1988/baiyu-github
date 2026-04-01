# ERP 可视化配置功能模块 - 使用说明

## 📋 目录结构

```
baiyu-web/src/views/erp/config/
├── components/
│   ├── ConfigSearch.vue          # 查询组件（已有）
│   ├── ConfigTable.vue           # 表格组件（已有）
│   ├── JsonEditor.vue            # JSON 编辑器（已有）
│   ├── DynamicFormItem.vue       # ✨ 动态表单渲染器
│   └── VisualConfigEditor.vue    # ✨ 可视化配置编辑器
├── composables/
│   └── useConfigData.ts          # 数据管理 Composable（已有）
├── metadata/
│   └── configMetadata.ts         # ✨ 配置元数据定义
├── types/
│   └── config.ts                 # TypeScript 类型定义（已扩展）
├── utils/
│   ├── configJsonUtils.ts        # JSON 工具（已有）
│   ├── ConfigParser.ts           # ✨ 配置解析器
│   └── dbFieldUtils.ts           # ✨ 数据库字段工具
├── api.ts                        # API 接口（已有）
└── index.vue                     # 主页面（已集成可视化编辑器）
```

## 🎯 核心功能

### 1. 可视化配置编辑器

**位置**: `VisualConfigEditor.vue`

**功能特点**:
- ✅ 三栏布局：左侧导航 + 中间表单 + 右侧预览
- ✅ 支持 9 个配置类别（PAGE、FORM、TABLE、SEARCH、ACTION、API、DICT、BUSINESS、DETAIL）
- ✅ 动态表单渲染：根据元数据自动生成表单
- ✅ 实时预览：JSON 格式即时显示
- ✅ 验证规则：完整的表单验证
- ✅ 分组管理：折叠面板组织字段

**使用方式**:
```vue
<VisualConfigEditor
  :config-id="configId"
  :initial-data="configData"
  @save="handleSave"
  @cancel="handleCancel"
/>
```

### 2. 动态表单系统

**核心组件**: `DynamicFormItem.vue`

**支持的组件类型**:
- input - 输入框
- textarea - 文本域
- input-number - 数字输入
- select - 下拉框
- radio - 单选框
- checkbox - 复选框
- switch - 开关
- date-picker - 日期选择器
- time-picker - 时间选择器
- color-picker - 颜色选择器
- upload - 上传组件
- cascader - 级联选择器
- slider - 滑块
- rate - 评分

**元数据配置示例**:
```typescript
{
  field: 'FBillNo',
  label: '单据编号',
  component: 'input',
  required: true,
  placeholder: '请输入单据编号',
  span: 12,
  validation: [
    { required: true, message: '单据编号不能为空', trigger: 'blur' }
  ],
  helpText: '单据的唯一标识'
}
```

### 3. 配置解析器

**文件**: `ConfigParser.ts`

**功能**:
- JSON → 表单数据双向转换
- 自动验证
- 错误收集

**使用示例**:
```typescript
import { createConfigParser } from './utils/ConfigParser'

const parser = createConfigParser()

// JSON 转表单
const result = parser.parseToJsonData(jsonString, 'PAGE')

// 表单转 JSON
const jsonString = parser.generateFromFormData(formData, 'PAGE')

// 验证
const errors = parser.validateFormData(formData)
```

### 4. 数据库字段工具

**文件**: `dbFieldUtils.ts`

**功能**:
- 获取数据库表列表
- 获取表字段信息
- 数据库字段 → 配置元数据转换
- 字段验证

**使用示例**:
```typescript
import { getTableColumns, convertDbColumnsToFieldMetas } from './dbFieldUtils'

// 获取表字段
const columns = await getTableColumns('t_sale_order')

// 转换为配置元数据
const fieldMetas = convertDbColumnsToFieldMetas(columns)
```

## 🚀 快速开始

### Step 1: 访问配置管理页面

访问：`http://localhost/erp/config`

### Step 2: 新增配置

1. 点击"新增配置"按钮
2. 可视化编辑器自动打开
3. 在左侧选择配置类别（如：PAGE）
4. 在中间区域填写表单
5. 右侧实时查看 JSON 预览
6. 点击"保存配置"

### Step 3: 编辑现有配置

1. 在配置列表中点击"查看"
2. 可视化编辑器打开并加载现有配置
3. 修改配置内容
4. 保存即可更新

## 📊 配置类别说明

### PAGE - 页面配置
- 页面 ID、名称
- 权限标识
- 布局方式
- API 前缀
- 主表名

### FORM - 表单配置
- 对话框宽度
- 标签宽度
- 表单布局
- 字段列表（动态添加）

### TABLE - 表格配置
- 表名、主键
- 边框、斑马纹
- 分页设置
- 列配置（动态添加）

### SEARCH - 查询配置
- 显示查询
- 默认展开
- 查询字段（动态添加）

### ACTION - 按钮配置
- 工具栏位置
- 显示工具栏
- 按钮列表（动态添加）

### API - 接口配置
- 基础路径
- 接口方法（动态添加）

### DICT - 字典配置
- 启用字典构建器
- 全局缓存设置
- 数据源配置（动态添加）

### BUSINESS - 业务配置
- 实体名称
- 实体简称
- 对话框标题
- 消息提示

### DETAIL - 详情配置
- 启用详情页
- 展示类型（抽屉/对话框）
- 抽屉宽度、方向
- 加载策略
- 页签列表（动态添加）

## 🔧 高级功能

### 1. 从数据库导入字段

```typescript
import { getTableColumns, convertDbColumnsToFieldMetas } from './dbFieldUtils'

async function importFieldsFromDatabase(tableName: string) {
  const columns = await getTableColumns(tableName)
  const fieldMetas = convertDbColumnsToFieldMetas(columns)
  
  // 将 fieldMetas 添加到配置中
  return fieldMetas
}
```

### 2. 自定义验证规则

```typescript
{
  field: 'FPrice',
  label: '价格',
  component: 'input-number',
  validation: [
    { 
      required: true, 
      message: '价格不能为空', 
      trigger: 'blur' 
    },
    { 
      min: 0, 
      message: '价格必须大于 0', 
      trigger: 'change' 
    },
    {
      validator: (rule, value, callback) => {
        if (value > 1000000) {
          callback(new Error('价格不能超过 100 万'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}
```

### 3. 导出配置 JSON

在可视化编辑器中：
1. 点击右上角"导出 JSON"按钮
2. JSON 自动复制到剪贴板
3. 可以粘贴到 SQL 脚本或其他地方

### 4. 批量导入配置

```sql
-- 在 SQL 脚本中使用导出的 JSON
UPDATE erp_page_config
SET page_config = '{"pageId":"saleorder","pageName":"销售订单管理",...}',
    form_config = '{"formConfig":{"dialogWidth":"1400px"},...}',
    -- ... 其他配置
WHERE module_code = 'saleorder';
```

## ⚙️ 技术栈

- **Vue 3** - Composition API
- **Element Plus** - UI 组件库
- **TypeScript** - 类型安全
- **SCSS** - 样式预处理

## 📝 注意事项

1. **数据库连接**: 确保后端提供 `/erp/database/tables` 和 `/erp/database/columns` 接口
2. **字典数据**: 字典选择器需要 `/erp/engine/dict/all` 接口支持
3. **权限控制**: 配置管理页面需要相应权限才能访问
4. **缓存管理**: 修改配置后记得清理缓存

## 🐛 常见问题

### Q1: 动态表单不显示？
**A**: 检查以下几点：
- 元数据中的 `component` 是否正确
- `dictionaryData`是否传递
- 字段是否被设置为`hidden`

### Q2: 验证不生效？
**A**: 确保：
- 验证规则在 `validation` 数组中
- `trigger` 属性正确（blur/change）
- 表单有对应的 `ref` 引用

### Q3: JSON 解析失败？
**A**: 检查：
- JSON 格式是否正确
- 配置类别是否匹配
- 是否有语法错误

## 📖 相关文档

- [ERP 页面配置完全指南 v4.0.md](../通用erp前后端文档/页面配置/ERP 页面配置完全指南 v4.0.md)
- [configMetadata.ts](./metadata/configMetadata.ts) - 元数据定义
- [ConfigParser.ts](./utils/ConfigParser.ts) - 解析器源码

## 🎉 后续优化方向

- [ ] 拖拽排序字段
- [ ] 字段模板库
- [ ] 配置对比功能
- [ ] 版本历史管理
- [ ] 批量操作
- [ ] 富文本编辑器集成
- [ ] 更多表单组件支持

---

**最后更新**: 2026-04-01  
**维护团队**: MM 团队
