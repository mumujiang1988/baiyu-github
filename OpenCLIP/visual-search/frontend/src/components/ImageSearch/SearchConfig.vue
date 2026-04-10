<template>
  <div class="search-config">
    <el-form label-width="70px" size="small">
      <el-form-item label="返回数量">
        <el-input-number 
          :model-value="topK" 
          :min="SEARCH_CONFIG.MIN_TOP_K" 
          :max="SEARCH_CONFIG.MAX_TOP_K" 
          style="width: 100%"
          @update:model-value="$emit('update:topK', $event)"
        />
      </el-form-item>
      <el-form-item label="聚合策略">
        <el-select 
          :model-value="aggregation" 
          style="width: 100%"
          @update:model-value="$emit('update:aggregation', $event)"
        >
          <el-option label="最大相似度" value="max" />
          <el-option label="平均相似度" value="avg" />
        </el-select>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
/**
 * 检索配置组件
 * 
 * 负责管理图像检索的配置参数
 */
import { SEARCH_CONFIG, AGGREGATION_STRATEGIES } from '../../constants/search'

defineProps({
  /**
   * 返回结果数量 (1-50)
   */
  topK: {
    type: Number,
    default: SEARCH_CONFIG.DEFAULT_TOP_K
  },
  
  /**
   * 相似度聚合策略
   * - 'max': 最大相似度
   * - 'avg': 平均相似度
   */
  aggregation: {
    type: String,
    default: AGGREGATION_STRATEGIES.MAX,
    validator: (value) => [AGGREGATION_STRATEGIES.MAX, AGGREGATION_STRATEGIES.AVG].includes(value)
  }
})

defineEmits(['update:topK', 'update:aggregation'])
</script>

<style scoped>
.search-config {
  margin-top: 16px;
}
</style>
