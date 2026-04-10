import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ingestProduct } from '../api/search'

/**
 * 产品入库状态管理（仅保留单产品入库和结果展示）
 * 批量入库逻辑已移至 composables/useBatchIngest.js
 */
export const useIngestStore = defineStore('ingest', () => {
  // State - 仅保留单产品相关状态
  const loading = ref(false)
  const singleResult = ref(null)
  const currentTab = ref('single') // 'single' | 'batch'
  
  // Getters
  const hasSingleResult = computed(() => singleResult.value !== null)
  const activeResult = computed(() => 
    currentTab.value === 'single' ? singleResult.value : null
  )

  // Actions
  /**
   * 单产品入库
   */
  async function ingestSingleProduct(productData) {
    loading.value = true
    singleResult.value = null
    
    try {
      const result = await ingestProduct(
        productData.productCode,
        productData.name,
        productData.files,
        productData.spec,
        productData.category
      )
      
      singleResult.value = {
        success: result.success,
        message: result.message,
        product_code: result.product_code,
        success_count: result.ingested_images,
        fail_count: 0,
        ingest_time_ms: Math.round(result.elapsed_seconds * 1000),
        errors: result.errors || []
      }
      
      return result
    } catch (error) {
      singleResult.value = {
        success: false,
        message: error.response?.data?.message || '入库失败',
        product_code: productData.productCode,
        success_count: 0,
        fail_count: productData.files.length,
        ingest_time_ms: 0,
        errors: [error.response?.data?.message || '未知错误']
      }
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 清除结果
   */
  function clearResults() {
    singleResult.value = null
  }

  /**
   * 切换页签
   */
  function switchTab(tab) {
    currentTab.value = tab
  }

  return {
    // State
    loading,
    singleResult,
    currentTab,
    
    // Getters
    hasSingleResult,
    activeResult,
    
    // Actions
    ingestSingleProduct,
    clearResults,
    switchTab
  }
})
