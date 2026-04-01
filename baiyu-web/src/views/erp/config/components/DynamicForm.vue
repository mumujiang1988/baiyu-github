/**
 * 动态表单容器 - 根据字段元数据自动渲染多个表单字段
 */
<template>
  <template v-for="fieldMeta in fieldMetas" :key="fieldMeta.field">
    <DynamicFormItem
      v-model="formData[fieldMeta.field]"
      :meta="fieldMeta"
      :dictionary-data="getDictionaryData(fieldMeta)"
    />
  </template>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { PropType } from 'vue'
import type { ConfigFieldMeta } from '../types/config'
import DynamicFormItem from './DynamicFormItem.vue'

const props = defineProps({
  // 表单数据
  modelValue: {
    type: Object as PropType<Record<string, any>>,
    required: true,
    default: () => ({})
  },
  // 字段元数据数组
  fieldMetas: {
    type: Array as PropType<ConfigFieldMeta[]>,
    required: true,
    default: () => []
  },
  // 字典数据
  dictionaryData: {
    type: Object as PropType<Record<string, Array<{ label: string; value: any }>>>,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue'])

// 双向绑定
const formData = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

/**
 * 根据字段元数据获取对应的字典数据
 */
function getDictionaryData(meta: ConfigFieldMeta) {
  if (!meta.dictionary) {
    return []
  }
  
  // 从字典数据中查找对应的数据
  const dictData = props.dictionaryData[meta.dictionary]
  if (dictData && Array.isArray(dictData)) {
    return dictData
  }
  
  // 如果字典数据中有 options 配置，使用 options
  if (meta.props?.options && Array.isArray(meta.props.options)) {
    return meta.props.options
  }
  
  return []
}
</script>

<style scoped>
/* 不需要额外样式 */
</style>
