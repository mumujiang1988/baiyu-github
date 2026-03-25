# ERP 配置化方案 - 前端开发指南

> 📅 **版本**: v1.0  
> 🎯 **目标**: 提供完整的前端开发指导，包括设计理念、文件结构、使用方式、配置方式和 API 二开方法  
> 📦 **适用范围**: Vue 3 + Element Plus + Vite  
> 🕐 **创建时间**: 2026-03-23  
> 👥 **目标读者**: 前端开发工程师、UI/UX设计师

---

## 📋 目录

1. [设计理念](#设计理念)
2. [文件结构](#文件结构)
3. [核心组件](#核心组件)
4. [使用方式](#使用方式)
5. [配置方式](#配置方式)
6. [API 二开方法](#api-二开方法)
7. [最佳实践](#最佳实践)

---

## 🎨 设计理念

### 核心理念

**配置驱动的动态页面（Configuration-Driven UI）**

```
┌─────────────────────────────────────────┐
│        后端返回 JSON 配置                 │
│    (无需硬编码，动态渲染)                │
└──────────────┬──────────────────────────┘
               ↓
┌─────────────────────────────────────────┐
│      BusinessConfigurable 组件           │
│   (通用配置化页面渲染引擎)               │
└──────────────┬──────────────────────────┘
               ↓
┌─────────────────────────────────────────┐
│         动态生成业务页面                 │
│    (搜索 + 表格 + 表单 + 工具栏)          │
└─────────────────────────────────────────┘
```

### 六大设计原则

| 原则 | 说明 | 实现方式 |
|------|------|---------|
| **单一职责** | 每个组件只做一件事 | 组件拆分、职责分离 |
| **开闭原则** | 对扩展开放，对修改关闭 | 插槽机制、组件组合 |
| **复用性** | 最大化组件复用 | 高阶组件、Hooks |
| **可配置性** | 通过配置改变行为 | Props 传递、配置驱动 |
| **响应式** | 数据变化自动更新 UI | Vue 3 Composition API |
| **类型安全** | TypeScript 类型检查 | 类型定义、接口约束 |

### 技术栈

- **框架**: Vue 3.x (Composition API)
- **UI 库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4.x
- **HTTP**: Axios
- **构建工具**: Vite 5.x
- **权限**: v-hasPermi 指令

---

## 📁 文件结构

### 完整目录结构

```
baiyu-web/src/
│
├── views/erp/pageTemplate/            # ERP 配置化页面模板
│   ├── configurable/                  # 🔧 配置化页面核心目录
│   │   ├── BusinessConfigurable.vue   # ✨ 主组件（1739 行）
│   │   ├── BusinessConfigurable.styles.css  # 样式文件
│   │   └── components/                # 子组件
│   │       ├── DynamicTable.vue       # 动态表格组件
│   │       ├── SmartSearch.vue        # 智能搜索组件
│   │       ├── ConfigurableForm.vue   # 可配置表单组件
│   │       └── ToolbarActions.vue     # 工具栏按钮组件
│   │
│   ├── components/                    # 通用组件
│   │   └── ExpandRowDetail.jsx        # 展开行详情组件
│   │
│   ├── configs/                       # 配置文件
│   │   └── business.config.template.json  # 配置模板
│   │
│   └── 表结构/                        # 数据库表结构参考
│       ├── bd_customer.sql
│       ├── bymaterial_dictionary.sql
│       └── ...\r\n│\r\n├── api/erp/engine/                    # 🚀 引擎 API 调用层\r\n│   ├── index.js                       # 统一导出文件\r\n│   ├── query.js                       # 动态查询 API\r\n│   ├── validation.js                  # 表单验证 API\r\n│   ├── approval.js                    # 审批流程 API\r\n│   └── push.js                        # 下推引擎 API\r\n│\r\n├── constants/                         # 📋 常量定义（新增）\r\n│   └── configTypes.js                 # 配置类型常量\r\n│\r\n├── components/                        # 全局公共组件\r\n│   ├── IconSelect/                    # 图标选择器\r\n│   ├── Pagination/                    # 分页组件\r\n│   └── ...\r\n│\r\n└── utils/                             # 工具类\r\n    ├── dict.js                        # 字典工具\r\n    ├── permission.js                  # 权限工具\r\n    ├── formatters.js                  # 📊 格式化工具（新增）\r\n    ├── responseHelper.js              # 📡 响应处理工具（新增）\r\n    └── ...\r\n```\r\n\r\n### 核心文件说明\r\n\r\n#### 0️⃣ 公共工具模块（新增 2026-03-23）\r\n\r\n**formatters.js** - 统一格式化函数\r\n\r\n```javascript\r\nimport { formatCurrency, formatDate, formatDateTime, formatPercent, formatAmount } from '@/utils/formatters'\r\n\r\n// 使用示例\r\nconst price = formatCurrency(12345.67)    // '12,345.67'\r\nconst date = formatDate('2026-03-23')     // '2026-03-23'\r\nconst percent = formatPercent(85.5)       // '85.50%'\r\n```\r\n\r\n**configTypes.js** - 配置类型常量\r\n\r\n```javascript\r\nimport { getConfigTypeLabel, getConfigTypeTag, getConfigTypeOptions } from '@/constants/configTypes'\r\n\r\n// 获取配置类型标签\r\nconst label = getConfigTypeLabel('PAGE')  // '页面配置'\r\nconst tag = getConfigTypeTag('PAGE')      // 'success'\r\n\r\n// 获取下拉选项\r\nconst options = getConfigTypeOptions()\r\n// [{ label: '页面配置', value: 'PAGE' }, ...]\r\n```\r\n\r\n**responseHelper.js** - API 响应处理\r\n\r\n```javascript\r\nimport { isSuccessResponse, getResponseData } from '@/utils/responseHelper'\r\n\r\n// 统一响应判断\r\nif (isSuccessResponse(response)) {\r\n  const data = getResponseData(response)\r\n  // 处理数据\r\n}\r\n```\r\n\r\n#### 1️⃣ BusinessConfigurable.vue (核心中的核心)

**位置**: `views/erp/pageTemplate/configurable/BusinessConfigurable.vue`

**作用**: 通用配置化页面渲染引擎，根据 JSON 配置动态生成完整业务页面

**代码结构**:

```vue
<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <!-- 页面标题 -->
      <div class="page-header">...</div>
      
      <!-- 工具栏按钮 -->
      <div class="toolbar-row">...</div>
      
      <!-- 查询表单 -->
      <el-form :model="queryParams">...</el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData">
        <!-- 动态列 -->
        <el-table-column 
          v-for="col in parsedConfig.table?.columns"
          :key="col.field"
          :field="col.field"
          :label="col.label"
        />
        
        <!-- 操作列 -->
        <el-table-column label="操作">
          <template #default="scope">
            <el-button @click="handleEdit(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <pagination
        v-show="total > 0"
        :total="total"
        @update:page="handlePageChange"
        @update:limit="handleLimitChange"
      />
    </el-card>
  </div>
</template>

<script setup name="BusinessConfigurable">
import { loadPageConfig } from '@/api/erp/config'
import { executeDynamicQuery } from '@/api/erp/engine/query'

// ========== 响应式数据 ==========
const config = ref({})           // 原始配置
const tableData = ref([])        // 表格数据
const queryParams = ref({})      // 查询参数
const loading = ref(false)       // 加载状态
const total = ref(0)             // 总数

// ========== 生命周期 ==========
onMounted(async () => {
  await initPageConfig()
  await handleQuery()
})

// ========== 核心方法 ==========
const handleQuery = async () => {
  loading.value = true
  try {
    const response = await executeDynamicQuery({
      moduleCode: props.moduleCode,
      searchConfig: parsedConfig.value.search,
      queryParams: queryParams.value,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    
    tableData.value = response.data.rows
    total.value = response.data.total
  } finally {
    loading.value = false
  }
}
</script>
```

#### 2️⃣ 引擎 API 调用层

**位置**: `api/erp/engine/`

| 文件 | 说明 | 核心函数 |
|------|------|---------|
| `index.js` | 统一导出 | `export * from './query'` |
| `query.js` | 查询 API | `executeDynamicQuery()` |
| `validation.js` | 验证 API | `executeFormValidation()` |
| `approval.js` | 审批 API | `executeApproval()`, `transferApproval()` |
| `push.js` | 下推 API | `executePushDown()`, `batchPushDown()` |

---

## 🧩 核心组件

### 一、动态表格组件 (DynamicTable)

#### 功能描述

根据配置动态渲染表格列，支持排序、筛选、自定义渲染等。

#### 示例代码

```vue
<template>
  <el-table :data="data" border stripe>
    <el-table-column
      v-for="column in columns"
      :key="column.field"
      :prop="column.field"
      :label="column.label"
      :width="column.width"
      :sortable="column.sortable"
    >
      <template #default="scope">
        <!-- 自定义渲染 -->
        <component 
          v-if="column.render"
          :is="column.render"
          :row="scope.row"
        />
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
defineProps({
  data: Array,
  columns: Array
})
</script>
```

### 二、智能搜索组件 (SmartSearch)

#### 功能描述

根据配置动态生成搜索条件，支持多种组件类型和字典联动。

#### 支持的组件类型

```javascript
['input', 'select', 'daterange', 'checkbox', 'radio']
```

#### 示例代码

```vue
<template>
  <el-form :model="queryParams" inline>
    <el-form-item
      v-for="field in fields"
      :key="field.field"
      :label="field.label"
    >
      <!-- 日期范围 -->
      <el-date-picker
        v-if="field.component === 'daterange'"
        v-model="dateRange[field.field]"
        type="daterange"
        range-separator="至"
      />
      
      <!-- 下拉选择 -->
      <el-select
        v-else-if="field.component === 'select'"
        v-model="queryParams[field.field]"
      >
        <el-option
          v-for="opt in getDictOptions(field.dictionary)"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      
      <!-- 输入框 -->
      <el-input
        v-else
        v-model="queryParams[field.field]"
      />
    </el-form-item>
  </el-form>
</template>
```

### 三、可配置表单组件 (ConfigurableForm)

#### 功能描述

根据 JSON 配置动态生成表单，支持字段验证、布局配置等。

#### 示例代码

```vue
<template>
  <el-form :model="formData" :rules="formRules" ref="formRef">
    <el-row :gutter="20">
      <el-col 
        v-for="section in sections" 
        :key="section.title"
        :span="24"
      >
        <el-divider>{{ section.title }}</el-divider>
        
        <el-col 
          v-for="field in section.fields" 
          :key="field.field"
          :span="field.span || 12"
        >
          <el-form-item :label="field.label" :prop="field.field">
            <el-input v-model="formData[field.field]" />
          </el-form-item>
        </el-col>
      </el-col>
    </el-row>
  </el-form>
</template>
```

---

## 💻 使用方式

### 快速开始

#### 步骤 1: 创建配置化页面

在 `views/erp/` 下创建新的业务模块目录：

```
views/erp/
└── saleorder/              # 销售订单模块
    ├── index.vue           # 列表页（使用配置化组件）
    ├── detail.vue          # 详情页
    └── form.vue            # 编辑页
```

#### 步骤 2: 使用 BusinessConfigurable 组件

```vue
<template>
  <BusinessConfigurable
    :module-code="'saleOrder'"
    :page-title="'销售订单'"
    @refresh="handleRefresh"
  />
</template>

<script setup>
import BusinessConfigurable from '@/views/erp/pageTemplate/configurable/BusinessConfigurable.vue'

const handleRefresh = () => {
  console.log('页面刷新')
}
</script>
```

#### 步骤 3: 配置后端接口

确保后端已配置以下接口：

- `GET /erp/config/get/{moduleCode}` - 获取页面配置
- `POST /erp/engine/query/execute` - 执行查询
- `POST /erp/engine/validation/execute` - 表单验证
- `POST /erp/engine/approval/execute` - 审批操作
- `POST /erp/engine/push/execute` - 下推操作

---

## ⚙️ 配置方式

### 一、页面配置 JSON

#### 完整配置示例

```json
{
  "page": {
    "title": "销售订单",
    "icon": "Document",
    "description": "销售订单管理页面"
  },
  
  "search": {
    "showSearch": true,
    "fields": [
      {
        "field": "fbillNo",
        "label": "单据编号",
        "component": "input",
        "searchType": "like",
        "props": {
          "placeholder": "请输入单据编号",
          "clearable": true,
          "prefixIcon": "Search"
        }
      },
      {
        "field": "fDocumentStatus",
        "label": "单据状态",
        "component": "select",
        "searchType": "eq",
        "dictionary": "bill_status",
        "props": {
          "placeholder": "请选择状态",
          "clearable": true,
          "filterable": true
        }
      },
      {
        "field": "fdate",
        "label": "创建日期",
        "component": "daterange",
        "searchType": "between",
        "props": {
          "startPlaceholder": "开始日期",
          "endPlaceholder": "结束日期",
          "valueFormat": "YYYY-MM-DD"
        }
      }
    ]
  },
  
  "table": {
    "rowKey": "id",
    "border": true,
    "stripe": true,
    "showOverflowTooltip": true,
    "columns": [
      {
        "field": "fbillNo",
        "label": "单据编号",
        "width": 180,
        "fixed": "left"
      },
      {
        "field": "fDocumentStatus",
        "label": "单据状态",
        "width": 100,
        "dict": "bill_status",
        "tagType": {
          "暂存": "info",
          "审核中": "warning",
          "已审核": "success"
        }
      },
      {
        "field": "fBillAmount",
        "label": "金额",
        "width": 120,
        "align": "right",
        "formatter": "currency"
      }
    ]
  },
  
  "toolbar": {
    "actions": [
      {
        "label": "新增",
        "type": "primary",
        "icon": "Plus",
        "handler": "handleAdd",
        "permission": "erp:saleorder:add",
        "disabled": false
      },
      {
        "label": "编辑",
        "type": "success",
        "icon": "Edit",
        "handler": "handleEdit",
        "permission": "erp:saleorder:edit",
        "disabled": "selectionEmpty"
      },
      {
        "label": "删除",
        "type": "danger",
        "icon": "Delete",
        "handler": "handleDelete",
        "permission": "erp:saleorder:remove",
        "disabled": "selectionEmpty"
      },
      {
        "label": "审核",
        "type": "warning",
        "icon": "Check",
        "handler": "handleAudit",
        "permission": "erp:saleorder:audit",
        "disabled": "noPermission"
      }
    ]
  },
  
  "form": {
    "sections": [
      {
        "title": "基本信息",
        "fields": [
          {
            "field": "fbillNo",
            "label": "单据编号",
            "component": "input",
            "required": true,
            "disabled": true
          },
          {
            "field": "fCustomerNumber",
            "label": "客户编号",
            "component": "select",
            "dictionary": "customer",
            "required": true
          }
        ]
      },
      {
        "title": "明细信息",
        "fields": [
          {
            "field": "entries",
            "label": "明细",
            "component": "subTable",
            "columns": [
              {
                "field": "fMaterialId",
                "label": "物料",
                "component": "select"
              },
              {
                "field": "fQty",
                "label": "数量",
                "component": "input-number"
              }
            ]
          }
        ]
      }
    ],
    
    "rules": {
      "fbillNo": [
        { "required": true, "message": "请输入单据编号", "trigger": "blur" }
      ],
      "fCustomerNumber": [
        { "required": true, "message": "请选择客户", "trigger": "change" }
      ]
    }
  }
}
```

### 二、工具栏按钮配置

#### 预置处理器

```javascript
const HANDLERS = {
  handleAdd: () => console.log('新增'),
  handleEdit: () => console.log('编辑'),
  handleDelete: () => console.log('删除'),
  handleAudit: () => console.log('审核'),
  handleTransfer: () => console.log('转审'),
  handleWithdraw: () => console.log('撤回'),
  handlePush: () => console.log('下推'),
  handlePreview: () => console.log('预览')
}
```

#### 禁用逻辑配置

```javascript
{
  "label": "编辑",
  "disabled": "selectionEmpty"  // 未选中行时禁用
}

{
  "label": "审核",
  "disabled": "noPermission"    // 无权限时禁用
}

{
  "label": "删除",
  "disabled": (selectedRows) => {
    return selectedRows.some(row => row.status === '已审核')
  }
}
```

---

## 🔌 API 二开方法

### 一、新增自定义 API

#### 场景：添加打印接口

#### 步骤 1: 创建 API 文件

```javascript
// api/erp/engine/print.js
import request from '@/utils/request'

/**
 * 打印单据
 */
export function printBill(moduleCode, billId, templateId) {
  return request({
    url: '/erp/engine/print/execute',
    method: 'post',
    params: { moduleCode, billId, templateId }
  })
}

/**
 * 批量打印
 */
export function batchPrint(moduleCode, billIds, templateId) {
  return request({
    url: '/erp/engine/print/batch',
    method: 'post',
    data: { moduleCode, billIds, templateId }
  })
}

/**
 * 预览打印效果
 */
export function previewPrint(moduleCode, billId, templateId) {
  return request({
    url: '/erp/engine/print/preview',
    method: 'post',
    data: { moduleCode, billId, templateId }
  })
}
```

#### 步骤 2: 在 index.js 中导出

```javascript
// api/erp/engine/index.js
export * from './query'
export * from './validation'
export * from './approval'
export * from './push'
export * from './print'  // ✅ 新增
```

#### 步骤 3: 在组件中使用

```vue
<script setup>
import { printBill } from '@/api/erp/engine/print'

const handlePrint = async () => {
  try {
    const result = await printBill('saleOrder', currentBillId.value, 'template_001')
    ElMessage.success('打印成功')
  } catch (error) {
    ElMessage.error('打印失败：' + error.message)
  }
}
</script>
```

### 二、扩展现有组件

#### 场景：为 BusinessConfigurable 添加导出功能

#### 步骤 1: 在父组件中添加方法

```vue
<!-- BusinessConfigurable.vue -->
<script setup>
// 添加导出方法
const handleExport = async () => {
  loading.value = true
  try {
    await download('/erp/engine/export', {
      moduleCode: props.moduleCode,
      queryParams: queryParams.value
    })
    ElMessage.success('导出成功')
  } finally {
    loading.value = false
  }
}

// 添加到工具栏
const toolbarActions = computed(() => [
  ...existingActions,
  {
    label: '导出',
    icon: 'Download',
    handler: handleExport,
    permission: 'erp:export'
  }
])
</script>
```

### 三、自定义渲染器

#### 场景：为表格列添加自定义渲染

#### 步骤 1: 创建渲染组件

```vue
<!-- components/renderers/CurrencyRenderer.vue -->
<template>
  <span :style="{ color: value < 0 ? 'red' : 'green' }">
    {{ formatCurrency(value) }}
  </span>
</template>

<script setup>
defineProps({
  value: Number,
  row: Object
})

const formatCurrency = (value) => {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(value)
}
</script>
```

#### 步骤 2: 在配置中使用

```json
{
  "field": "fBillAmount",
  "label": "金额",
  "render": "CurrencyRenderer"
}
```

#### 步骤 3: 注册渲染器

```vue
<!-- BusinessConfigurable.vue -->
<script setup>
import CurrencyRenderer from '@/components/renderers/CurrencyRenderer.vue'

const renderers = {
  CurrencyRenderer,
  StatusRenderer,
  DateRenderer
}
</script>

<template>
  <component 
    :is="renderers[column.render]"
    :value="row[column.field]"
    :row="row"
  />
</template>
```

### 四、集成第三方服务

#### 场景：集成条形码生成

#### 步骤 1: 安装依赖

```bash
npm install jsbarcode
```

#### 步骤 2: 创建条码生成工具

```javascript
// utils/barcode.js
import JsBarcode from 'jsbarcode'

export function generateBarcode(value, options = {}) {
  const canvas = document.createElement('canvas')
  
  JsBarcode(canvas, value, {
    format: 'CODE128',
    width: 2,
    height: 50,
    displayValue: true,
    ...options
  })
  
  return canvas.toDataURL('image/png')
}
```

#### 步骤 3: 在表格列中使用

```vue
<template>
  <el-table-column label="条形码">
    <template #default="scope">
      <img :src="generateBarcode(scope.row.fbillNo)" alt="barcode" />
    </template>
  </el-table-column>
</template>

<script setup>
import { generateBarcode } from '@/utils/barcode'
</script>
```

---

## 📊 最佳实践

### 1. 组件性能优化

✅ **使用 v-memo 缓存静态内容**:

```vue
<template>
  <div v-memo="[config.version]">
    <!-- 只在 config.version 变化时重新渲染 -->
  </div>
</template>
```

✅ **虚拟滚动优化大表格**:

```vue
<template>
  <el-table-v2
    :columns="columns"
    :data="largeData"
    :width="800"
    :height="600"
  />
</template>
```

✅ **防抖搜索**:

```javascript
import { debounce } from 'lodash-es'

const handleQuery = debounce(async () => {
  // 查询逻辑
}, 300)
```

### 2. 状态管理规范

✅ **使用 Pinia 管理全局状态**:

```javascript
// store/modules/erpConfig.js
import { defineStore } from 'pinia'

export const useErpConfigStore = defineStore('erpConfig', {
  state: () => ({
    configs: {},
    loading: false
  }),
  
  actions: {
    async loadConfig(moduleCode) {
      this.loading = true
      const config = await loadPageConfig(moduleCode)
      this.configs[moduleCode] = config
      this.loading = false
    }
  }
})
```

### 3. 错误处理

✅ **统一的错误处理**:

```javascript
// utils/errorHandler.js
export function handleApiError(error) {
  if (error.response) {
    const { status, data } = error.response
    
    switch (status) {
      case 401:
        ElMessage.error('未授权，请重新登录')
        break
      case 403:
        ElMessage.error('无权限访问')
        break
      case 404:
        ElMessage.error('资源不存在')
        break
      default:
        ElMessage.error(data.msg || '请求失败')
    }
  } else {
    ElMessage.error('网络错误')
  }
}
```

### 4. 代码规范

✅ **ESLint 配置**:

```javascript
// .eslintrc.cjs
module.exports = {
  extends: [
    'plugin:vue/vue3-recommended',
    '@vue/eslint-config-typescript'
  ],
  rules: {
    'vue/multi-word-component-names': 'off',
    '@typescript-eslint/no-explicit-any': 'warn'
  }
}
```

✅ **Prettier 配置**:

```javascript
// .prettierrc
{
  "semi": false,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "none"
}
```

---

## 🎯 总结

### 核心优势

✅ **高复用** - BusinessConfigurable 组件支持所有 CRUD 场景  
✅ **少冗余** - 配置驱动，避免重复代码  
✅ **易扩展** - 插槽机制、组件组合  
✅ **配置化** - 业务页面可通过 JSON 配置生成  
✅ **标准化** - 统一的组件规范和数据结构  

### 适用场景

✅ **适合配置化的场景**:
- CRUD 业务页面（销售订单、采购订单等）
- 单据管理页面（入库单、出库单等）
- 报表查询页面（库存明细、销售统计等）
- 基础资料维护（客户、供应商、物料等）

❌ **不适合配置化的场景**:
- 复杂业务逻辑（需要大量自定义代码）
- 特殊 UI 需求（高度定制化界面）
- 性能敏感场景（需要极致优化）

---

## 📚 相关文档

- [ERP 配置化后端架构设计方案](./ERP 配置化后端架构设计方案.md)
- [ERP 配置化方案 - 待实现功能清单](./ERP 配置化方案 - 待实现功能清单.md)
- [前端配置化设计方案](./前端配置化设计方案.md)
- [RuoYi通用配置化后端接口设计方案](./RuoYi通用配置化后端接口设计方案.md)

---

**文档版本**: v1.0  
**创建时间**: 2026-03-23  
**作者**: ERP 研发团队  
**最后更新**: 2026-03-23  
**审核状态**: 已审核 ✅
