# ERP 配置化前端架构设计方案

> 📅 **版本**: v1.0  
> 🎯 **目标**: 提供高复用、配置驱动的前端架构方案，实现 90% 业务模块零代码开发  
> 📦 **适用范围**: RuoYi-WMS + Vue 3 + Element Plus + Vite  
> 🕐 **创建时间**: 2026-03-23  
> 👥 **目标读者**: 前端开发工程师、架构师

---

## 📋 目录

1. [设计原则](#设计原则)
2. [总体架构](#总体架构)
3. [核心组件](#核心组件)
4. [配置解析器](#配置解析器)
5. [API 接口规范](#api 接口规范)
6. [代码实现](#代码实现)
7. [最佳实践](#最佳实践)

---

## 🎯 设计原则

### 核心理念

**配置驱动，组件复用**

```
         ┌─────────────────┐
         │  JSON 配置       │
         └────────┬────────┘
                  │
    ┌─────────────┼─────────────┐
    │             │             │
┌───▼────┐  ┌────▼─────┐  ┌───▼────┐
│解析器   │  │ 组件库   │  │ API 层  │
└────────┘  └──────────┘  └────────┘
```

### 六大设计原则

| 原则 | 说明 | 实现方式 |
|------|------|---------|
| **单一职责** | 每个组件只负责一个功能 | 组件拆分、功能分离 |
| **开闭原则** | 对扩展开放，对修改关闭 | 抽象基类 + 策略模式 |
| **可复用性** | 组件可在多个模块复用 | 通用组件设计 |
| **可配置性** | 行为通过配置调整 | JSON 配置驱动 |
| **类型安全** | 使用 TypeScript 类型约束 | 类型定义、接口规范 |
| **性能优化** | 懒加载、缓存机制 | 动态导入、虚拟滚动 |

### 复用度对比

| 组件类型 | 传统开发 | 配置化开发 | 复用度提升 |
|---------|---------|-----------|----------|
| **列表页面** | 每个模块手写 | 通用组件 | ⬆️ **95%** |
| **表单页面** | 每个模块手写 | 配置生成 | ⬆️ **90%** |
| **搜索表单** | 硬编码字段 | 配置渲染 | ⬆️ **95%** |
| **表格列** | 固定列定义 | 动态列配置 | ⬆️ **90%** |
| **验证逻辑** | if-else 堆砌 | 配置规则 | ⬆️ **95%** |

---

## 🏗️ 总体架构

### 四层架构设计

```
┌─────────────────────────────────────────────────────────┐
│                   应用层 (Application)                   │
│  销售订单页面 | 采购订单页面 | 库存管理页面 | ...         │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   组件层 (Component)                     │
│  ┌───────────┐  ┌───────────┐  ┌───────────────────┐   │
│  │通用组件   │  │业务组件   │  │ 特殊场景组件      │   │
│  └───────────┘  └───────────┘  └───────────────────┘   │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   解析层 (Parser)                        │
│  ┌───────────┐  ┌───────────┐  ┌───────────────────┐   │
│  │配置解析器 │  │字典解析器 │  │ 权限解析器        │   │
│  └───────────┘  └───────────┘  └───────────────────┘   │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   API 层 (Service)                       │
│  ┌───────────┐  ┌───────────┐  ┌───────────────────┐   │
│  │配置 API   │  │引擎 API   │  │ 业务 API          │   │
│  └───────────┘  └───────────┘  └───────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### 核心类图

```
┌──────────────────────────────────────────────────────────┐
│                   应用层 (Application)                    │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  ┌─────────────────┐       ┌─────────────────────┐      │
│  │BusinessConfig   │────▶│ ConfigurableTemplate │      │
│  └─────────────────┘       └─────────────────────┘      │
│           ▲                       ▲                      │
│           │                       │                      │
│  ┌─────────────────┐       ┌─────────────────────┐      │
│  │SaleOrderPage    │────▶│ PurchaseOrderPage    │      │
│  └─────────────────┘       └─────────────────────┘      │
│                                                           │
├──────────────────────────────────────────────────────────┤
│                   组件层 (Component)                      │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  ┌─────────────────┐       ┌─────────────────────┐      │
│  │DynamicSearch    │────▶│ DynamicTable         │      │
│  └─────────────────┘       └─────────────────────┘      │
│           ▲                       ▲                      │
│           │                       │                      │
│  ┌─────────────────┐       ┌─────────────────────┐      │
│  │DynamicForm      │────▶│ DynamicDialog        │      │
│  └─────────────────┘       └─────────────────────┘      │
│                                                           │
└──────────────────────────────────────────────────────────┘
```

### 技术栈

```
┌─────────────────────────────────────┐
│         应用层 (Vue 3)               │
│  Composition API + Script Setup     │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│         UI 库 (Element Plus)         │
│   组件库 + 样式系统                 │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│         工具库 (Utils)               │
│  配置解析器 + 字典管理 + 权限控制   │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│         HTTP 客户端 (Axios)          │
│    RESTful API + 拦截器             │
└─────────────────────────────────────┘
```

---

##  核心组件

### 四大核心组件架构

```
┌─────────────────────────────────────────────────────────┐
│                   组件层 (Component Layer)                │
├─────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ DynamicSearch│  │ DynamicTable │  │ DynamicForm  │  │
│  │  动态搜索    │  │  动态表格    │  │  动态表单    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐                                       │
│  │ConfigurablePg│                                       │
│  │  配置化页面  │                                       │
│  └──────────────┘                                       │
└─────────────────────────────────────────────────────────┘
```

### 一、DynamicSearch - 动态搜索组件

#### 功能描述
根据 JSON 配置动态渲染搜索表单，支持多种输入组件类型。

#### 组件代码

```vue
<template>
  <el-card shadow="never" class="search-card" v-if="showSearch">
    <el-form :model="queryParams" ref="queryRef" :inline="true">
      <template v-for="field in fields" :key="field.field">
        <el-form-item :label="field.label" :prop="field.field">
          <!-- 文本输入框 -->
          <el-input
            v-if="field.component === 'input'"
            v-model="queryParams[field.field]"
            :placeholder="field.props?.placeholder"
            :clearable="field.props?.clearable ?? true"
            @keyup.enter="handleQuery"
          />
          
          <!-- 下拉选择框 -->
          <el-select
            v-else-if="field.component === 'select'"
            v-model="queryParams[field.field]"
            :placeholder="field.props?.placeholder"
            :filterable="field.props?.filterable ?? false"
            :clearable="field.props?.clearable ?? true"
          >
            <el-option
              v-for="option in getDictOptions(field.dictionary, field.options)"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
          
          <!-- 日期选择框 -->
          <el-date-picker
            v-else-if="field.component === 'date'"
            v-model="queryParams[field.field]"
            :type="field.props?.type || 'date'"
            :placeholder="field.props?.placeholder"
            :format="field.props?.format || 'YYYY-MM-DD'"
            :value-format="field.props?.valueFormat || 'YYYY-MM-DD'"
            :clearable="field.props?.clearable ?? true"
          />
          
          <!-- 数字输入框 -->
          <el-input-number
            v-else-if="field.component === 'number'"
            v-model="queryParams[field.field]"
            :min="field.props?.min"
            :max="field.props?.max"
            :step="field.props?.step || 1"
            :precision="field.props?.precision || 0"
          />
        </el-form-item>
      </template>
      
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">
          搜索
        </el-button>
        <el-button icon="Refresh" @click="resetQuery">
          重置
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getDictOptions } from '@/utils/dict'

// Props
const props = defineProps({
  fields: {
    type: Array,
    required: true,
    default: () => []
  },
  showSearch: {
    type: Boolean,
    default: true
  }
})

// Emits
const emit = defineEmits(['query', 'reset'])

// 响应式数据
const queryRef = ref(null)
const queryParams = ref({})

// 处理查询
const handleQuery = () => {
  emit('query', queryParams.value)
}

// 重置查询
const resetQuery = () => {
  if (queryRef.value) {
    queryRef.value.resetFields()
  }
  emit('reset', queryParams.value)
}

// 暴露方法
defineExpose({
  queryParams,
  resetQuery
})
</script>

<style scoped>
.search-card {
  margin-bottom: 20px;
}
</style>
```

#### 支持的组件类型

```javascript
['input', 'select', 'date', 'datetime', 'number', 
 'textarea', 'radio', 'checkbox', 'switch']
```

---

### 二、DynamicTable - 动态表格组件

#### 功能描述
根据 JSON 配置动态渲染表格列，支持多种数据渲染类型。

#### 组件代码

```vue
<template>
  <el-table
    v-loading="loading"
    :data="tableData"
    :border="border"
    :stripe="stripe"
    :height="height"
    @selection-change="handleSelectionChange"
  >
    <!-- 选择列 -->
    <el-table-column
      v-if="showSelection"
      type="selection"
      width="55"
      align="center"
    />
    
    <!-- 序号列 -->
    <el-table-column
      v-if="showIndex"
      type="index"
      label="序号"
      width="60"
      align="center"
    />
    
    <!-- 动态列 -->
    <template v-for="(column, index) in columns" :key="index">
      <el-table-column
        v-if="!column.hidden"
        :prop="column.prop"
        :label="column.label"
        :width="column.width"
        :min-width="column.minWidth || 'auto'"
        :align="column.align || 'left'"
        :fixed="column.fixed"
        :sortable="column.sortable"
      >
        <template #default="{ row }">
          <!-- 货币类型 -->
          <span v-if="column.renderType === 'currency'">
            {{ formatCurrency(row[column.prop]) }}
          </span>
          
          <!-- 字典标签 -->
          <el-tag
            v-else-if="column.renderType === 'tag'"
            :type="getTagType(row[column.prop], column.tagMap)"
          >
            {{ getDictLabel(column.dictionary, row[column.prop]) }}
          </el-tag>
          
          <!-- 状态开关 -->
          <el-switch
            v-else-if="column.renderType === 'switch'"
            v-model="row[column.prop]"
            :active-value="column.activeValue ?? 1"
            :inactive-value="column.inactiveValue ?? 0"
            @change="handleStatusChange(row, column)"
          />
          
          <!-- 操作按钮 -->
          <template v-else-if="column.renderType === 'action'">
            <template v-for="(btn, btnIndex) in column.buttons" :key="btnIndex">
              <el-button
                v-if="hasPermission(btn.permission)"
                :type="btn.type || 'text'"
                :icon="btn.icon"
                @click="handleAction(btn.action, row)"
              >
                {{ btn.label }}
              </el-button>
            </template>
          </template>
          
          <!-- 默认文本 -->
          <span v-else>{{ row[column.prop] }}</span>
        </template>
      </el-table-column>
    </template>
    
    <!-- 空数据提示 -->
    <template #empty>
      <div class="empty-data">
        <el-empty description="暂无数据" />
      </div>
    </template>
  </el-table>
  
  <!-- 分页 -->
  <pagination
    v-show="total > 0"
    v-model:page="pageNum"
    v-model:limit="pageSize"
    :total="total"
    @pagination="handlePagination"
  />
</template>

<script setup>
import { ref, watch } from 'vue'
import { getDictLabel } from '@/utils/dict'
import { hasPermission } from '@/utils/permission'

// Props
const props = defineProps({
  columns: {
    type: Array,
    required: true,
    default: () => []
  },
  tableData: {
    type: Array,
    default: () => []
  },
  total: {
    type: Number,
    default: 0
  },
  loading: {
    type: Boolean,
    default: false
  },
  border: {
    type: Boolean,
    default: true
  },
  stripe: {
    type: Boolean,
    default: true
  },
  height: {
    type: [Number, String],
    default: 'auto'
  },
  showSelection: {
    type: Boolean,
    default: false
  },
  showIndex: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['selection-change', 'pagination', 'action'])

// 响应式数据
const pageNum = ref(1)
const pageSize = ref(10)
const selectedRows = ref([])

// 格式化货币
const formatCurrency = (value) => {
  if (value === null || value === undefined) return ''
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(value)
}

// 获取标签类型
const getTagType = (value, tagMap) => {
  if (tagMap && tagMap[value]) {
    return tagMap[value]
  }
  return 'info'
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
  emit('selection-change', selection)
}

// 处理分页
const handlePagination = ({ page, limit }) => {
  emit('pagination', { pageNum: page, pageSize: limit })
}

// 处理操作
const handleAction = (action, row) => {
  emit('action', { action, row })
}

// 处理状态变化
const handleStatusChange = (row, column) => {
  emit('action', {
    action: 'statusChange',
    row,
    field: column.prop,
    value: row[column.prop]
  })
}

// 暴露方法
defineExpose({
  selectedRows,
  refresh: () => emit('pagination', { pageNum: pageNum.value, pageSize: pageSize.value })
})
</script>

<style scoped>
.empty-data {
  padding: 40px 0;
}
</style>
```

#### 支持的渲染类型

```javascript
['text', 'currency', 'tag', 'switch', 'action', 
 'date', 'datetime', 'image', 'link', 'progress']
```

---

### 三、DynamicForm - 动态表单组件

#### 功能描述
根据 JSON 配置动态渲染表单，支持多种表单控件和验证规则。

#### 组件代码

```vue
<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    :label-width="labelWidth"
    :size="size"
  >
    <el-row :gutter="20">
      <template v-for="(section, sectionIndex) in sections" :key="sectionIndex">
        <el-col :span="section.span || 24">
          <el-form-item
            v-if="section.title"
            :label="section.title"
            class="section-title"
          />
        </el-col>
        
        <template v-for="(field, fieldIndex) in section.fields" :key="fieldIndex">
          <el-col :span="field.span || 12">
            <el-form-item
              :label="field.label"
              :prop="field.field"
              :required="field.required"
            >
              <!-- 文本输入 -->
              <el-input
                v-if="field.component === 'input'"
                v-model="formData[field.field]"
                :placeholder="field.placeholder"
                :maxlength="field.maxlength"
                :show-word-limit="field.showWordLimit"
                :disabled="field.disabled"
                :readonly="field.readonly"
              />
              
              <!-- 文本域 -->
              <el-input
                v-else-if="field.component === 'textarea'"
                v-model="formData[field.field]"
                type="textarea"
                :rows="field.rows || 4"
                :placeholder="field.placeholder"
                :maxlength="field.maxlength"
                :show-word-limit="field.showWordLimit"
                :disabled="field.disabled"
              />
              
              <!-- 下拉选择 -->
              <el-select
                v-else-if="field.component === 'select'"
                v-model="formData[field.field]"
                :placeholder="field.placeholder"
                :multiple="field.multiple"
                :filterable="field.filterable"
                :disabled="field.disabled"
                style="width: 100%"
              >
                <el-option
                  v-for="option in getDictOptions(field.dictionary, field.options)"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                  :disabled="option.disabled"
                />
              </el-select>
              
              <!-- 日期选择 -->
              <el-date-picker
                v-else-if="field.component === 'date'"
                v-model="formData[field.field]"
                :type="field.type || 'date'"
                :placeholder="field.placeholder"
                :format="field.format"
                :value-format="field.valueFormat"
                :disabled="field.disabled"
                style="width: 100%"
              />
              
              <!-- 数字输入 -->
              <el-input-number
                v-else-if="field.component === 'number'"
                v-model="formData[field.field]"
                :min="field.min"
                :max="field.max"
                :step="field.step"
                :precision="field.precision"
                :disabled="field.disabled"
                style="width: 100%"
              />
              
              <!-- 单选框 -->
              <el-radio-group
                v-else-if="field.component === 'radio'"
                v-model="formData[field.field]"
                :disabled="field.disabled"
              >
                <el-radio
                  v-for="option in field.options"
                  :key="option.value"
                  :label="option.value"
                >
                  {{ option.label }}
                </el-radio>
              </el-radio-group>
              
              <!-- 复选框 -->
              <el-checkbox-group
                v-else-if="field.component === 'checkbox'"
                v-model="formData[field.field]"
                :disabled="field.disabled"
              >
                <el-checkbox
                  v-for="option in field.options"
                  :key="option.value"
                  :label="option.value"
                >
                  {{ option.label }}
                </el-checkbox>
              </el-checkbox-group>
              
              <!-- 开关 -->
              <el-switch
                v-else-if="field.component === 'switch'"
                v-model="formData[field.field]"
                :active-value="field.activeValue ?? 1"
                :inactive-value="field.inactiveValue ?? 0"
                :disabled="field.disabled"
              />
              
              <!-- 文件上传 -->
              <el-upload
                v-else-if="field.component === 'upload'"
                :action="field.uploadUrl"
                :headers="field.headers"
                :on-success="(res) => handleUploadSuccess(res, field)"
                :before-upload="field.beforeUpload"
                :limit="field.limit || 1"
              >
                <el-button type="primary">点击上传</el-button>
              </el-upload>
            </el-form-item>
          </el-col>
        </template>
      </template>
    </el-row>
    
    <!-- 表单操作按钮 -->
    <el-form-item>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
        {{ submitText || '提交' }}
      </el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button @click="handleCancel">取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getDictOptions } from '@/utils/dict'

// Props
const props = defineProps({
  sections: {
    type: Array,
    required: true,
    default: () => []
  },
  formData: {
    type: Object,
    required: true,
    default: () => ({})
  },
  labelWidth: {
    type: String,
    default: '100px'
  },
  size: {
    type: String,
    default: 'default'
  },
  submitText: {
    type: String,
    default: '提交'
  }
})

// Emits
const emit = defineEmits(['submit', 'reset', 'cancel', 'upload-success'])

// 响应式数据
const formRef = ref(null)
const submitLoading = ref(false)

// 表单验证规则
const formRules = computed(() => {
  const rules = {}
  
  props.sections.forEach(section => {
    section.fields?.forEach(field => {
      const fieldRules = []
      
      // 必填校验
      if (field.required) {
        fieldRules.push({
          required: true,
          message: `${field.label}不能为空`,
          trigger: 'blur'
        })
      }
      
      // 邮箱校验
      if (field.rules?.includes('email')) {
        fieldRules.push({
          type: 'email',
          message: '请输入正确的邮箱格式',
          trigger: 'blur'
        })
      }
      
      // 手机号校验
      if (field.rules?.includes('phone')) {
        fieldRules.push({
          pattern: /^1[3-9]\d{9}$/,
          message: '请输入正确的手机号',
          trigger: 'blur'
        })
      }
      
      // 数字校验
      if (field.rules?.includes('number')) {
        fieldRules.push({
          type: 'number',
          message: '请输入数字',
          trigger: 'blur'
        })
      }
      
      // 长度校验
      if (field.minlength || field.maxlength) {
        fieldRules.push({
          min: field.minlength,
          max: field.maxlength,
          message: `长度在 ${field.minlength || 0} 到 ${field.maxlength || 999} 个字符`,
          trigger: 'blur'
        })
      }
      
      if (fieldRules.length > 0) {
        rules[field.field] = fieldRules
      }
    })
  })
  
  return rules
})

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        emit('submit', props.formData)
      } finally {
        submitLoading.value = false
      }
    } else {
      ElMessage.error('请填写完整信息')
    }
  })
}

// 重置表单
const handleReset = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  emit('reset', props.formData)
}

// 取消操作
const handleCancel = () => {
  emit('cancel')
}

// 上传成功
const handleUploadSuccess = (response, field) => {
  if (response.code === 200) {
    props.formData[field.field] = response.data.url
    ElMessage.success('上传成功')
    emit('upload-success', { field, response })
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

// 暴露方法
defineExpose({
  formData,
  validate: async () => {
    if (!formRef.value) return false
    return await formRef.value.validate()
  },
  resetFields: () => formRef.value?.resetFields(),
  clearValidate: () => formRef.value?.clearValidate()
})
</script>

<style scoped>
.section-title {
  font-weight: bold;
  color: #303133;
  margin-bottom: 20px;
}
</style>
```

#### 支持的表单控件

```javascript
['input', 'textarea', 'select', 'date', 'datetime',
 'number', 'radio', 'checkbox', 'switch', 'upload']
```

---

## 🔍 配置解析器

### ERPConfigParser - 配置解析器

#### 功能描述
负责解析 JSON 配置并生成 Vue 组件所需的配置对象。

#### 核心代码

```javascript
/**
 * ERP 配置解析器
 * 负责解析 JSON 配置并生成 Vue 组件所需的配置对象
 */
class ERPConfigParser {
  constructor(config) {
    this.config = config
    this.validateConfig()
  }
  
  /**
   * 验证配置完整性
   */
  validateConfig() {
    const requiredKeys = ['pageConfig']
    for (const key of requiredKeys) {
      if (!this.config[key]) {
        throw new Error(`配置缺少必需字段：${key}`)
      }
    }
  }
  
  /**
   * 解析页面基础配置
   */
  get pageConfig() {
    return {
      title: this.config.pageConfig.title || '页面标题',
      permissionPrefix: this.config.pageConfig.permissionPrefix || '',
      apiPrefix: this.config.pageConfig.apiPrefix || '',
      entityName: this.config.pageConfig.entityName || '',
      template: this.config.pageConfig.template || 'default'
    }
  }
  
  /**
   * 解析搜索配置
   */
  get search() {
    const searchConfig = this.config.searchConfig || {}
    return {
      showSearch: searchConfig.showSearch ?? true,
      fields: searchConfig.fields || [],
      layout: searchConfig.layout || 'inline'
    }
  }
  
  /**
   * 解析表格配置
   */
  get table() {
    const tableConfig = this.config.tableConfig || {}
    return {
      columns: tableConfig.columns || [],
      border: tableConfig.border ?? true,
      stripe: tableConfig.stripe ?? true,
      showSelection: tableConfig.showSelection ?? false,
      showIndex: tableConfig.showIndex ?? false,
      pagination: tableConfig.pagination ?? true
    }
  }
  
  /**
   * 解析表单配置
   */
  get form() {
    const formConfig = this.config.formConfig || {}
    return {
      sections: formConfig.sections || [],
      labelWidth: formConfig.labelWidth || '100px',
      size: formConfig.size || 'default',
      submitText: formConfig.submitText || '提交'
    }
  }
  
  /**
   * 解析权限配置
   */
  get permissions() {
    const prefix = this.pageConfig.permissionPrefix
    return {
      query: `${prefix}:query`,
      add: `${prefix}:add`,
      edit: `${prefix}:edit`,
      delete: `${prefix}:delete`,
      audit: `${prefix}:audit`,
      unAudit: `${prefix}:unAudit`,
      push: `${prefix}:push`,
      export: `${prefix}:export`
    }
  }
  
  /**
   * 解析操作按钮配置
   */
  get actions() {
    const actionConfig = this.config.actionConfig || {}
    return {
      showAdd: actionConfig.showAdd ?? true,
      showEdit: actionConfig.showEdit ?? true,
      showDelete: actionConfig.showDelete ?? true,
      showExport: actionConfig.showExport ?? false,
      showImport: actionConfig.showImport ?? false,
      customActions: actionConfig.customActions || []
    }
  }
  
  /**
   * 解析审批配置
   */
  get approval() {
    const approvalConfig = this.config.approvalConfig || {}
    return {
      enabled: approvalConfig.enabled ?? false,
      workflow: approvalConfig.workflow || {},
      showHistory: approvalConfig.showHistory ?? true
    }
  }
  
  /**
   * 解析下推配置
   */
  get push() {
    const pushConfig = this.config.pushConfig || {}
    return {
      enabled: pushConfig.enabled ?? false,
      targets: pushConfig.targets || []
    }
  }
  
  /**
   * 获取完整配置
   */
  getConfig() {
    return this.config
  }
  
  /**
   * 合并配置（支持继承）
   */
  static merge(baseConfig, extendConfig) {
    return {
      ...baseConfig,
      ...extendConfig,
      pageConfig: { ...baseConfig?.pageConfig, ...extendConfig?.pageConfig },
      searchConfig: { ...baseConfig?.searchConfig, ...extendConfig?.searchConfig },
      tableConfig: { ...baseConfig?.tableConfig, ...extendConfig?.tableConfig },
      formConfig: { ...baseConfig?.formConfig, ...extendConfig?.formConfig }
    }
  }
}

export default ERPConfigParser
```

#### 配置示例

```json
{
  "pageConfig": {
    "title": "销售订单管理",
    "permissionPrefix": "k3:saleOrder",
    "apiPrefix": "/k3/sale-order",
    "entityName": "销售订单"
  },
  "searchConfig": {
    "showSearch": true,
    "fields": [
      {
        "field": "fbillNo",
        "label": "单据编号",
        "component": "input",
        "searchType": "like",
        "props": {
          "placeholder": "请输入单据编号",
          "clearable": true
        }
      },
      {
        "field": "fDocumentStatus",
        "label": "单据状态",
        "component": "select",
        "dictionary": "document_status",
        "searchType": "eq",
        "props": {
          "placeholder": "请选择单据状态",
          "filterable": true
        }
      }
    ]
  },
  "tableConfig": {
    "columns": [
      {
        "prop": "fbillNo",
        "label": "单据编号",
        "width": 180,
        "fixed": "left"
      },
      {
        "prop": "fDocumentStatus",
        "label": "单据状态",
        "width": 120,
        "renderType": "tag",
        "dictionary": "document_status"
      },
      {
        "prop": "fBillAmount",
        "label": "金额",
        "width": 120,
        "renderType": "currency",
        "align": "right"
      },
      {
        "renderType": "action",
        "label": "操作",
        "width": 200,
        "fixed": "right",
        "buttons": [
          {
            "label": "编辑",
            "action": "edit",
            "type": "primary",
            "permission": "edit"
          },
          {
            "label": "删除",
            "action": "delete",
            "type": "danger",
            "permission": "delete"
          }
        ]
      }
    ],
    "showSelection": true,
    "showIndex": true,
    "pagination": true
  }
}
```

---

## 📡 API 接口规范

### RESTful API 设计

#### 1. 配置管理 API

```javascript
/**
 * 获取页面配置
 * @param {string} moduleCode - 模块编码
 * @returns {Promise}
 */
export function getPageConfig(moduleCode) {
  return request({
    url: `/erp/config/get/${moduleCode}`,
    method: 'get'
  })
}

/**
 * 保存页面配置
 * @param {Object} config - 配置对象
 * @returns {Promise}
 */
export function savePageConfig(config) {
  return request({
    url: '/erp/config/save',
    method: 'post',
    data: config
  })
}
```

#### 2. 引擎 API

```javascript
/**
 * 执行动态查询
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function executeDynamicQuery(params) {
  return request({
    url: '/erp/engine/query',
    method: 'post',
    data: params
  })
}

/**
 * 执行表单验证
 * @param {Object} params - 验证参数
 * @returns {Promise}
 */
export function executeValidation(params) {
  return request({
    url: '/erp/engine/validation',
    method: 'post',
    data: params
  })
}

/**
 * 执行审批操作
 * @param {Object} params - 审批参数
 * @returns {Promise}
 */
export function executeApproval(params) {
  return request({
    url: '/erp/engine/approval',
    method: 'post',
    data: params
  })
}

/**
 * 执行下推操作
 * @param {Object} params - 下推参数
 * @returns {Promise}
 */
export function executePushDown(params) {
  return request({
    url: '/erp/engine/push',
    method: 'post',
    data: params
  })
}
```

### Axios 拦截器配置

```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API,
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 添加 token
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    // 防止重复提交
    if (config.method === 'post' || config.method === 'put') {
      const key = `${config.url}_${JSON.stringify(config.data)}`
      if (window.pendingRequests.has(key)) {
        return Promise.reject(new Error('请勿重复提交'))
      }
      window.pendingRequests.set(key, true)
      
      config.onFinally = () => {
        window.pendingRequests.delete(key)
      }
    }
    
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 业务错误处理
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      
      // 401: 未授权
      if (res.code === 401) {
        router.push('/login')
      }
      
      // 403: 无权限
      if (res.code === 403) {
        ElMessage.error('无访问权限')
      }
      
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('响应错误:', error)
    
    // 网络错误
    if (!error.response) {
      ElMessage.error('网络连接失败，请检查网络')
      return Promise.reject(error)
    }
    
    // HTTP 错误码处理
    switch (error.response.status) {
      case 400:
        ElMessage.error('请求参数错误')
        break
      case 401:
        ElMessage.error('未授权，请重新登录')
        router.push('/login')
        break
      case 403:
        ElMessage.error('拒绝访问')
        break
      case 404:
        ElMessage.error('请求资源不存在')
        break
      case 500:
        ElMessage.error('服务器内部错误')
        break
      case 502:
        ElMessage.error('网关错误')
        break
      case 503:
        ElMessage.error('服务不可用')
        break
      case 504:
        ElMessage.error('网关超时')
        break
      default:
        ElMessage.error(error.response.data?.msg || '请求失败')
    }
    
    return Promise.reject(error)
  }
)

export default service
```

---

## 💻 代码实现

### 目录结构

```
baiyu-web/src/
├── api/erp/
│   ├── config.js                      # 配置管理 API
│   └── engine/
│       ├── index.js                   # 统一导出
│       ├── query.js                   # 查询引擎 API
│       ├── validation.js              # 验证引擎 API
│       ├── approval.js                # 审批引擎 API
│       └── push.js                    # 下推引擎 API
├── components/
│   └── erp/
│       ├── DynamicSearch.vue          # 动态搜索组件
│       ├── DynamicTable.vue           # 动态表格组件
│       ├── DynamicForm.vue            # 动态表单组件
│       └── ConfigurablePage.vue       # 配置化页面组件
├── utils/
│   ├── erpConfigParser.js             # 配置解析器
│   ├── dict.js                        # 字典工具
│   └── permission.js                  # 权限工具
└── views/erp/
    └── pageTemplate/
        └── configurable/
            ├── BusinessConfigurable.vue       # 通用配置化组件
            ├── BusinessConfigurable.styles.css
            └── components/
```

### 核心实现示例

#### BusinessConfigurable.vue - 通用配置化组件

```vue
<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <el-card shadow="never" class="header-card">
      <h2>{{ parsedConfig?.pageConfig?.title || '配置化页面' }}</h2>
    </el-card>
    
    <!-- 搜索区域 -->
    <DynamicSearch
      v-if="parsedConfig?.search?.showSearch"
      :fields="parsedConfig.search.fields"
      @query="handleQuery"
      @reset="handleReset"
    />
    
    <!-- 操作按钮 -->
    <el-card shadow="never" class="toolbar-card">
      <el-button
        v-if="hasPermission(parsedConfig.permissions.add)"
        type="primary"
        icon="Plus"
        @click="handleAdd"
      >
        新增
      </el-button>
      <el-button
        v-if="hasPermission(parsedConfig.permissions.batchDelete)"
        type="danger"
        icon="Delete"
        :disabled="!selectedRows.length"
        @click="handleBatchDelete"
      >
        批量删除
      </el-button>
      <el-button
        v-if="parsedConfig.actions.showExport"
        type="warning"
        icon="Download"
        @click="handleExport"
      >
        导出
      </el-button>
    </el-card>
    
    <!-- 表格区域 -->
    <el-card shadow="never" class="table-card">
      <DynamicTable
        ref="tableRef"
        :columns="parsedConfig.table.columns"
        :table-data="tableData"
        :total="total"
        :loading="loading"
        :show-selection="parsedConfig.table.showSelection"
        :show-index="parsedConfig.table.showIndex"
        @selection-change="handleSelectionChange"
        @pagination="handlePagination"
        @action="handleTableAction"
      />
    </el-card>
    
    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <DynamicForm
        ref="formRef"
        :sections="parsedConfig.form.sections"
        :form-data="formData"
        @submit="handleSubmit"
        @cancel="dialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPageConfig } from '@/api/erp/config'
import ERPConfigParser from '@/utils/erpConfigParser'
import { hasPermission } from '@/utils/permission'
import DynamicSearch from '@/components/erp/DynamicSearch.vue'
import DynamicTable from '@/components/erp/DynamicTable.vue'
import DynamicForm from '@/components/erp/DynamicForm.vue'

// ==================== 响应式数据 ====================
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedRows = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formData = ref({})
const parsedConfig = ref(null)
const tableRef = ref(null)
const formRef = ref(null)

// ==================== 加载配置 ====================
const loadPageConfig = async () => {
  try {
    const response = await getPageConfig('saleOrder')
    const dbConfig = JSON.parse(response.data)
    
    // 解析配置
    parsedConfig.value = new ERPConfigParser(dbConfig)
  } catch (error) {
    console.error('加载配置失败:', error)
    ElMessage.error('加载配置失败')
  }
}

// ==================== 查询列表 ====================
const getList = async (params = {}) => {
  loading.value = true
  try {
    // TODO: 调用实际 API
    // const response = await listApi(params)
    // tableData.value = response.rows
    // total.value = response.total
    
    // 模拟数据
    tableData.value = []
    total.value = 0
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

// ==================== 事件处理 ====================
const handleQuery = (queryParams) => {
  getList(queryParams)
}

const handleReset = () => {
  getList()
}

const handlePagination = ({ pageNum, pageSize }) => {
  getList({ pageNum, pageSize })
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleAdd = () => {
  dialogTitle.value = '新增'
  formData.value = {}
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑'
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除 "${row.fbillNo}" 吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用删除 API
    // await deleteApi(row.id)
    
    ElMessage.success('删除成功')
    getList()
  } catch {
    // 用户取消
  }
}

const handleBatchDelete = async () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请选择要删除的数据')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 条数据吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用批量删除 API
    // await batchDeleteApi(selectedRows.value.map(item => item.id))
    
    ElMessage.success('批量删除成功')
    getList()
  } catch {
    // 用户取消
  }
}

const handleSubmit = async (data) => {
  try {
    // TODO: 调用保存 API
    // await saveApi(data)
    
    ElMessage.success('保存成功')
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('保存失败:', error)
  }
}

const handleTableAction = ({ action, row, field, value }) => {
  switch (action) {
    case 'edit':
      handleEdit(row)
      break
    case 'delete':
      handleDelete(row)
      break
    case 'statusChange':
      handleStatusChange(row, field, value)
      break
    default:
      console.log('未知操作:', action)
  }
}

const handleStatusChange = async (row, field, value) => {
  try {
    // TODO: 调用状态更新 API
    // await updateStatusApi(row.id, field, value)
    
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
    // 恢复状态
    row[field] = !value
  }
}

const handleExport = () => {
  // TODO: 导出功能
  ElMessage.info('导出功能开发中')
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  formData.value = {}
}

// ==================== 生命周期 ====================
onMounted(() => {
  loadPageConfig()
  getList()
})
</script>

<style scoped>
.header-card {
  margin-bottom: 20px;
}

.header-card h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.toolbar-card {
  margin-bottom: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}
</style>
```

---

##  最佳实践

### 1. 组件设计规范

 **推荐做法**:
- 使用 Composition API (Script Setup)
- Props 和 Emits 明确定义类型
- 使用 defineExpose 暴露必要方法
- 组件职责单一，功能专注

 **不推荐做法**:
- Options API 与 Composition API 混用
- Props 类型定义不明确
- 组件过于庞大，职责不清晰

### 2. 性能优化

 **懒加载组件**:
```javascript
// 使用 Vite 的动态导入
const DynamicSearch = defineAsyncComponent(() =>
  import('@/components/erp/DynamicSearch.vue')
)
```

 **虚拟滚动**:
```vue
<!-- 大数据量表格使用虚拟滚动 -->
<el-table
  :data="tableData"
  height="600"
  virtual-scroll
  :virtual-scroll-item-size="50"
>
```

 **防抖节流**:
```javascript
import { debounce } from 'lodash-es'

// 搜索防抖
const handleSearch = debounce((value) => {
  // 搜索逻辑
}, 300)
```

### 3. 类型安全

 **使用 JSDoc 类型注解**:
```javascript
/**
 * @typedef {Object} SearchField
 * @property {string} field - 字段名
 * @property {string} label - 字段标签
 * @property {string} component - 组件类型
 * @property {Object} [props] - 组件属性
 */

/**
 * @type {import('vue').PropType<SearchField[]>}
 */
const props = defineProps({
  fields: {
    type: Array,
    required: true
  }
})
```

### 4. 错误处理

 **全局错误边界**:
```javascript
// App.vue
import { onErrorCaptured } from 'vue'
import { ElMessage } from 'element-plus'

onErrorCaptured((error, instance, info) => {
  console.error('组件错误:', error, instance, info)
  ElMessage.error('组件渲染失败')
  return false
})
```

 **API 错误统一处理**:
```javascript
// api/errorHandler.js
export function handleApiError(error) {
  if (error.response) {
    // HTTP 错误
    const status = error.response.status
    const message = error.response.data?.msg || '请求失败'
    
    switch (status) {
      case 400:
        ElMessage.error(message)
        break
      case 401:
        ElMessage.error('未授权')
        break
      default:
        ElMessage.error(message)
    }
  } else if (error.request) {
    // 网络错误
    ElMessage.error('网络连接失败')
  } else {
    // 其他错误
    ElMessage.error(error.message)
  }
}
```

### 5. 代码质量

 **ESLint 配置**:
```javascript
// .eslintrc.cjs
module.exports = {
  root: true,
  extends: [
    'plugin:vue/vue3-recommended',
    'eslint:recommended'
  ],
  rules: {
    'vue/multi-word-component-names': 'off',
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off'
  }
}
```

 **Prettier 配置**:
```javascript
// .prettierrc
{
  "semi": false,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "none",
  "printWidth": 100
}
```

---

## 📈 实施路线图

### 阶段一：基础组件开发（1 周）

**目标**: 建立通用组件库

**任务**:
- [ ] 开发 DynamicSearch 组件
- [ ] 开发 DynamicTable 组件
- [ ] 开发 DynamicForm 组件
- [ ] 开发 ConfigurablePage 组件

**交付物**:
-  4 个核心通用组件
-  组件使用文档
-  组件单元测试

### 阶段二：解析器开发（1 周）

**目标**: 实现配置解析器

**任务**:
- [ ] 开发 ERPConfigParser
- [ ] 实现字典解析
- [ ] 实现权限解析
- [ ] 实现配置验证

**交付物**:
-  配置解析器
-  配置验证机制
-  配置合并工具

### 阶段三：API 集成（3 天）

**目标**: 完成 API 接口对接

**任务**:
- [ ] 开发配置管理 API
- [ ] 开发引擎 API
- [ ] 配置 Axios 拦截器
- [ ] 实现错误处理

**交付物**:
-  完整的 API 接口
-  统一的错误处理
-  请求拦截器

### 阶段四：业务集成（1 周）

**目标**: 在现有业务中应用配置化

**任务**:
- [ ] 改造销售订单页面
- [ ] 改造采购订单页面
- [ ] 新增配置管理页面
- [ ] 编写使用文档

**交付物**:
-  配置化的业务页面
-  配置管理界面
-  完整使用文档

---

## ✨ 总结

### 核心优势

 **高复用** - 通用组件提供 90% 标准功能  
 **少冗余** - 避免重复代码，DRY 原则  
 **易扩展** - 开闭原则，新功能无需改旧代码  
 **配置化** - 页面行为可通过 JSON 配置调整  
 **标准化** - 统一的组件规范和接口标准  

### 关键特性

1. **四层架构**: Application → Component → Parser → API，职责清晰
2. **配置驱动**: 页面行为可配置，无需改代码
3. **组件支撑**: 搜索、表格、表单、页面四大组件
4. **类型安全**: JSDoc 类型注解，代码提示完善
5. **性能优秀**: 懒加载、虚拟滚动、防抖节流

### 适用场景

 **适合配置化的场景**:
- CRUD 业务页面（销售订单、采购订单等）
- 单据管理页面（入库单、出库单等）
- 报表查询页面（库存明细、销售统计等）
- 基础资料维护（客户、供应商、物料等）

 **不适合配置化的场景**:
- 复杂业务逻辑（需要大量自定义代码）
- 特殊 UI 需求（高度定制化界面）
- 性能敏感场景（需要极致优化）

---

## 📚 相关文档

- [RuoYi通用配置化后端接口设计方案](./RuoYi通用配置化后端接口设计方案.md)
- [ERP 配置化后端架构设计方案](./ERP 配置化后端架构设计方案.md)
- [ERP 配置管理页面使用指南](./ERP 配置管理页面使用指南.md)
- [ERP 前后端配置化模式完整落地手册](./ERP 前后端配置化模式完整落地手册.md)

---

**文档版本**: v1.0  
**创建时间**: 2026-03-23  
**作者**: ERP 研发团队  
**最后更新**: 2026-03-23  
**审核状态**: 已审核 
