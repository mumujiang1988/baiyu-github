<template>
  <div class="text-search-section">
    <div class="section-header">
      <span class="section-title">文本搜索</span>
    </div>
    
    <el-input
      :model-value="keyword"
      placeholder="输入产品名称、规格或编码"
      size="default"
      clearable
      @update:model-value="$emit('update:keyword', $event)"
      @keyup.enter="$emit('search')"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
      <template #append>
        <el-button 
          type="primary" 
          @click="$emit('search')" 
          :loading="loading"
        >
          搜索
        </el-button>
      </template>
    </el-input>
  </div>
</template>

<script setup>
/**
 * 文本搜索组件
 * 
 * 负责关键词输入和触发文本搜索
 */
import { Search } from '@element-plus/icons-vue'

defineProps({
  /**
   * 搜索关键词
   */
  keyword: {
    type: String,
    default: ''
  },
  
  /**
   * 是否正在搜索中
   */
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['update:keyword', 'search'])
</script>

<style scoped>
.text-search-section {
  margin-top: 16px;
}

.section-header {
  margin-bottom: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}
</style>
