<template>
  <el-col :span="meta.span || 24" :offset="meta.offset || 0">
    <el-form-item :label="meta.label" :prop="meta.field">
      <component :is="currentComponent" v-bind="componentProps">
        <template v-if="hasOptions">
          <el-option
            v-for="opt in options"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </template>
        <template v-if="hasRadioOptions">
          <el-radio
            v-for="opt in radioOptions"
            :key="opt.value"
            :label="opt.value"
          >
            {{ opt.label }}
          </el-radio>
        </template>
        <template v-if="hasCheckboxOptions">
          <el-checkbox
            v-for="opt in checkboxOptions"
            :key="opt.value"
            :label="opt.value"
            border
          >
            {{ opt.label }}
          </el-checkbox>
        </template>
        <template v-if="isUpload">
          <el-icon :size="20">
            <Upload />
          </el-icon>
          <div class="el-upload__text">点击上传</div>
        </template>
        <template v-if="isJsonEditor">
          <div class="json-editor-wrapper">
            <pre class="json-preview">{{ formatJson(value) }}</pre>
          </div>
        </template>
        <template v-if="isCustomFieldEditor">
          <component
            :is="getCustomEditorComponent(props.meta.component)"
            :model-value="(value as any[]) || []"
            :title="customEditorTitle"
            @update:model-value="(val: any[]) => emit('update:modelValue', val)"
          />
        </template>
      </component>
      <div v-if="meta.helpText" class="form-item-help-text">
        {{ meta.helpText }}
      </div>
    </el-form-item>
  </el-col>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { PropType } from 'vue' 
import type { ConfigFieldMeta } from '../types/config'
import { Upload } from '@element-plus/icons-vue'
import FieldListEditor from './FieldListEditor.vue'
import RelationTabsEditor from './RelationTabsEditor.vue'

// 引入 ElMessage 用于上传文件数量超限提示（使用全局变量绕过类型检查）
declare global {
  interface Window {
    ElMessage: any
  }
}
const ElMessage = window.ElMessage || (window as any).ELEMENT_PLUS?.message
const props = defineProps({
  // 字段元数据
  meta: {
    type: Object as PropType<ConfigFieldMeta>,
    required: true
  },
  // 字段值
  modelValue: {
    type: [String, Number, Boolean, Array, Object],
    default: null
  },
  // 是否只读
  readonly: {
    type: Boolean,
    default: false
  },
  // 字典数据（用于下拉框等）
  dictionaryData: {
    type: Array as PropType<Array<{ label: string; value: any }>>,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

// 双向绑定
const value = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 计算组件类型
const currentComponent = computed(() => {
  const componentMap: Record<string, string> = {
    'input': 'el-input',
    'textarea': 'el-input',
    'input-number': 'el-input-number',
    'select': 'el-select',
    'radio': 'el-radio-group',
    'checkbox': 'el-checkbox-group',
    'switch': 'el-switch',
    'date-picker': 'el-date-picker',
    'time-picker': 'el-time-picker',
    'color-picker': 'el-color-picker',
    'upload': 'el-upload',
    'cascader': 'el-cascader',
    'slider': 'el-slider',
    'rate': 'el-rate',
    'json-editor': 'div' // JSON 编辑器使用 div 展示
  }
  return componentMap[props.meta.component || 'input'] || 'el-input'
})

// 通用组件属性
const componentProps = computed(() => {
  const baseProps: any = {
    modelValue: value.value,
    'onUpdate:modelValue': (val: any) => {
      value.value = val
    },
    placeholder: props.meta.placeholder || `请输入${props.meta.label}`,
    disabled: props.meta.disabled || props.readonly,
    readonly: props.meta.readonly || props.readonly,
    clearable: true,
    ...props.meta.props
  }

  switch (props.meta.component) {
    case 'textarea':
      return {
        ...baseProps,
        type: 'textarea',
        rows: 4,
        maxlength: 500,
        showWordLimit: true
      }
    case 'input-number':
      return {
        ...baseProps,
        min: props.meta.props?.min ?? 0,
        max: props.meta.props?.max ?? 999999,
        precision: props.meta.props?.precision ?? 2,
        step: props.meta.props?.step ?? 1,
        controlsPosition: props.meta.props?.controlsPosition ?? 'right'
      }
    case 'select':
      return {
        ...baseProps,
        filterable: props.meta.props?.filterable ?? true,
        allowCreate: props.meta.props?.allowCreate ?? false
      }
    case 'radio':
      return {
        modelValue: value.value,
        'onUpdate:modelValue': (val: any) => {
          value.value = val
        },
        disabled: props.meta.disabled || props.readonly
      }
    case 'checkbox':
      return {
        modelValue: value.value,
        'onUpdate:modelValue': (val: any) => {
          value.value = val
        },
        disabled: props.meta.disabled || props.readonly
      }
    case 'switch':
      return {
        modelValue: value.value,
        'onUpdate:modelValue': (val: any) => {
          value.value = val
        },
        disabled: props.meta.disabled || props.readonly,
        activeText: props.meta.props?.activeText,
        inactiveText: props.meta.props?.inactiveText
      }
    case 'date-picker':
      return {
        ...baseProps,
        type: props.meta.props?.type ?? 'date',
        format: props.meta.props?.format ?? 'YYYY-MM-DD',
        valueFormat: props.meta.props?.valueFormat ?? 'YYYY-MM-DD'
      }
    case 'time-picker':
      return {
        ...baseProps,
        format: props.meta.props?.format ?? 'HH:mm:ss',
        valueFormat: props.meta.props?.valueFormat ?? 'HH:mm:ss'
      }
    case 'color-picker':
      return {
        modelValue: value.value,
        'onUpdate:modelValue': (val: any) => {
          value.value = val
        },
        disabled: props.meta.disabled || props.readonly,
        showAlpha: props.meta.props?.showAlpha ?? true
      }
    case 'upload':
      return {
        action: props.meta.props?.action || '/api/upload',
        multiple: props.meta.props?.multiple ?? false,
        limit: props.meta.props?.limit ?? 3,
        onExceed: () => {
          ElMessage.warning(`最多只能上传 ${props.meta.props?.limit || 3} 个文件`)
        },
        onSuccess: (response: any) => {
          if (response.data && response.data.url) {
            value.value = response.data.url
          }
        }
      }
    case 'cascader':
      return {
        modelValue: value.value,
        'onUpdate:modelValue': (val: any) => {
          value.value = val
        },
        options: props.meta.props?.options || [],
        disabled: props.meta.disabled || props.readonly,
        clearable: true,
        filterable: true
      }
    case 'slider':
      return {
        modelValue: value.value,
        'onUpdate:modelValue': (val: any) => {
          value.value = val
        },
        disabled: props.meta.disabled || props.readonly,
        min: props.meta.props?.min ?? 0,
        max: props.meta.props?.max ?? 100,
        step: props.meta.props?.step ?? 1
      }
    case 'rate':
      return {
        modelValue: value.value,
        'onUpdate:modelValue': (val: any) => {
          value.value = val
        },
        disabled: props.meta.disabled || props.readonly,
        max: props.meta.props?.max ?? 5
      }
    default:
      return {
        ...baseProps,
        type: 'text'
      }
  }
})

// 是否有选项（用于下拉框）
const hasOptions = computed(() => {
  return props.meta.component === 'select'
})

// 获取选项数据
const options = computed(() => {
  return props.dictionaryData.length > 0 
    ? props.dictionaryData 
    : (props.meta.props?.options || [])
})

// 是否有单选选项
const hasRadioOptions = computed(() => {
  return props.meta.component === 'radio'
})

// 获取单选选项
const radioOptions = computed(() => {
  return props.dictionaryData.length > 0 
    ? props.dictionaryData 
    : (props.meta.props?.options || [])
})

// 是否有复选选项
const hasCheckboxOptions = computed(() => {
  return props.meta.component === 'checkbox'
})

// 获取复选选项
const checkboxOptions = computed(() => {
  return props.dictionaryData.length > 0 
    ? props.dictionaryData 
    : (props.meta.props?.options || [])
})

// 是否是上传组件
const isUpload = computed(() => {
  return props.meta.component === 'upload'
})

// 是否是 JSON 编辑器
const isJsonEditor = computed(() => {
  return props.meta.component === 'json-editor'
})

// 是否是自定义字段编辑器
const isCustomFieldEditor = computed(() => {
  const customComponents = [
    'custom-form-fields-editor',
    'custom-table-columns-editor',
    'custom-search-fields-editor',
    'custom-relation-tabs-editor'
  ]
  return customComponents.includes(props.meta.component)
})

// 获取自定义编辑器标题
const customEditorTitle = computed(() => {
  const titleMap: Record<string, string> = {
    'custom-form-fields-editor': '表单字段配置',
    'custom-table-columns-editor': '表格列配置',
    'custom-search-fields-editor': '查询字段配置',
    'custom-relation-tabs-editor': '详情页签配置'
  }
  return titleMap[props.meta.component] || '字段配置'
})

/**
 * 获取自定义编辑器组件
 */
const getCustomEditorComponent = (component: string) => {
  const componentMap: Record<string, any> = {
    'custom-form-fields-editor': FieldListEditor,
    'custom-table-columns-editor': FieldListEditor,
    'custom-search-fields-editor': FieldListEditor,
    'custom-relation-tabs-editor': RelationTabsEditor
  }
  return componentMap[component] || FieldListEditor
}

/**
 * 格式化 JSON 用于展示
 */
function formatJson(val: any): string {
  if (!val) return '{}'
  try {
    return typeof val === 'string' 
      ? JSON.stringify(JSON.parse(val), null, 2)
      : JSON.stringify(val, null, 2)
  } catch {
    return String(val)
  }
}
</script>

<style scoped>
.form-item-help-text {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.json-editor-wrapper {
  width: 100%;
  min-height: 200px;
  background: #f5f7fa;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  padding: 12px;
}

.json-preview {
  margin: 0;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #606266;
  white-space: pre-wrap;
  word-wrap: break-word;
  line-height: 1.5;
}
</style>
