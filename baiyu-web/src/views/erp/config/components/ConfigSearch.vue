/**
 * 配置搜索表单组件 - UI 组件
 * 职责：渲染搜索条件表单，不包含业务逻辑
 */

<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { ConfigQueryParams } from '../types/config'
import { getConfigTypeOptions } from '@/constants/configTypes'

interface Props {
  modelValue: ConfigQueryParams
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({
    pageNum: 1,
    pageSize: 10,
    moduleCode: '',
    configName: '',
    configType: '',
    status: ''
  })
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: ConfigQueryParams): void
  (e: 'query'): void
  (e: 'reset'): void
}>()

// 本地响应式数据
const localQueryParams = reactive<ConfigQueryParams>({ ...props.modelValue })

// 监听外部变化
watch(() => props.modelValue, (newVal) => {
  Object.assign(localQueryParams, newVal)
}, { deep: true })

// 同步到父组件
watch(localQueryParams, (newVal) => {
  emit('update:modelValue', { ...newVal })
}, { deep: true })

// 事件处理
const handleSearch = () => emit('query')
const handleReset = () => emit('reset')
</script>

<template>
  <el-form :model="localQueryParams" :inline="true" label-width="60px" class="config-search-form">
    <el-row :gutter="12">
      <el-col :span="5">
        <el-form-item label="模块编码">
          <el-input
            v-model="localQueryParams.moduleCode"
            placeholder="请输入模块编码"
            clearable
            style="width: 100%"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
      </el-col>
      
      <el-col :span="5">
        <el-form-item label="配置名称">
          <el-input
            v-model="localQueryParams.configName"
            placeholder="请输入配置名称"
            clearable
            style="width: 100%"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
      </el-col>
      
      <el-col :span="5">
        <el-form-item label="配置类型">
          <el-select
            v-model="localQueryParams.configType"
            placeholder="请选择配置类型"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="option in getConfigTypeOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
      </el-col>
      
      <el-col :span="4">
        <el-form-item label="状态">
          <el-select
            v-model="localQueryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 100%"
          >
            <el-option label="正常" value="1" />
            <el-option label="停用" value="0" />
          </el-select>
        </el-form-item>
      </el-col>
      
      <el-col :span="5">
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button icon="Refresh" @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<style scoped lang="scss">
.config-search-form {
  margin-bottom: 12px;
  
  :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: 0;
  }
  
  :deep(.el-form-item__label) {
    font-size: 13px;
  }
  
  :deep(.el-input__wrapper),
  :deep(.el-select-wrapper) {
    height: 32px;
    font-size: 13px;
  }
  
  :deep(.el-button) {
    padding: 8px 12px;
    font-size: 13px;
  }
}
</style>
