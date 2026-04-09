import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ingestProduct, batchIngestProducts } from '../api/search'
import { groupFilesByFolder, parseProductInfo } from '../components/ProductIngest/utils/folderParser'

/**
 * 产品入库状态管理
 */
export const useIngestStore = defineStore('ingest', () => {
  // State
  const loading = ref(false)
  const singleResult = ref(null)
  const batchResult = ref(null)
  const currentTab = ref('single') // 'single' | 'batch'
  
  // 批量入库相关状态
  const batchInputRef = ref(null)
  const batchSubmitting = ref(false)
  const batchResults = ref([])
  const successCount = ref(0)
  const failCount = ref(0)
  const folderStructure = ref('standard')
  const sceneFolderNames = ref('')

  // Getters
  const hasSingleResult = computed(() => singleResult.value !== null)
  const hasBatchResult = computed(() => batchResult.value !== null)
  const activeResult = computed(() => 
    currentTab.value === 'single' ? singleResult.value : batchResult.value
  )
  
  const successRate = computed(() => {
    const total = successCount.value + failCount.value
    return total > 0 ? ((successCount.value / total) * 100).toFixed(2) : 0
  })

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
   * 批量产品入库
   */
  async function ingestBatchProducts(batchData) {
    loading.value = true
    batchResult.value = null
    
    try {
      const result = await batchIngestProducts(
        batchData.products,
        batchData.filesMap,
        batchData.files,
        batchData.removeBg
      )
      
      batchResult.value = result
      return result
    } catch (error) {
      batchResult.value = {
        success: false,
        message: error.response?.data?.message || '批量入库失败',
        results: {
          success: [],
          failed: [],
          total_images: 0,
          total_success_images: 0,
          total_failed_images: 0
        }
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
    batchResult.value = null
  }

  /**
   * 切换页签
   */
  function switchTab(tab) {
    currentTab.value = tab
  }
  
  /**
   * 触发批量上传
   */
  function triggerBatchUpload() {
    if (batchInputRef.value) {
      batchInputRef.value.click()
    }
  }
  
  /**
   * 处理批量文件选择
   */
  async function handleBatchFileSelect(event) {
    const files = event.target.files
    if (!files || files.length === 0) {
      return
    }
    
    // 按文件夹分组文件
    const folderMap = groupFilesByFolder(files, folderStructure.value, sceneFolderNames.value)
    
    if (folderMap.size === 0) {
      return
    }
    
    // 解析产品信息并构建批量入库数据
    const products = []
    for (const [folderName, fileGroup] of folderMap.entries()) {
      const productInfo = parseProductInfo(folderName)
      
      products.push({
        folderName,
        productCode: productInfo.code,
        productName: productInfo.name || '',
        spec: productInfo.spec || '',
        category: productInfo.category || '',
        files: fileGroup,
        status: 'pending',
        progress: 0,
        message: ''
      })
    }
    
    batchResults.value = products
  }
  
  /**
   * 开始批量入库
   */
  async function startBatchIngest() {
    if (batchResults.value.length === 0) {
      return
    }
    
    batchSubmitting.value = true
    successCount.value = 0
    failCount.value = 0
    
    try {
      // 逐个处理产品
      for (let i = 0; i < batchResults.value.length; i++) {
        const product = batchResults.value[i]
        
        // 更新状态为处理中
        product.status = 'processing'
        product.progress = 0
        
        try {
          // 调用单个产品入库 API
          await ingestProduct(
            product.productCode,
            product.productName,
            product.files,
            product.spec,
            product.category
          )
          
          // 成功
          product.status = 'success'
          product.progress = 100
          product.message = '入库成功'
          successCount.value++
        } catch (error) {
          // 失败
          product.status = 'error'
          product.progress = 0
          product.message = error.response?.data?.message || '入库失败'
          failCount.value++
        }
      }
    } finally {
      batchSubmitting.value = false
    }
  }
  
  /**
   * 清空产品列表
   */
  function clearBatchProducts() {
    batchResults.value = []
    successCount.value = 0
    failCount.value = 0
  }
  
  /**
   * 删除单个产品
   */
  function removeProduct(index) {
    const product = batchResults.value[index]
    if (product.status === 'processing') {
      return // 处理中的产品不能删除
    }
    
    if (product.status === 'success') {
      successCount.value--
    } else if (product.status === 'error') {
      failCount.value--
    }
    
    batchResults.value.splice(index, 1)
  }
  
  /**
   * 处理目录结构变化
   */
  function handleStructureChange() {
    // 清空已选择的产品，因为目录结构变化后需要重新解析
    clearBatchProducts()
  }

  return {
    // State
    loading,
    singleResult,
    batchResult,
    currentTab,
    batchInputRef,
    batchSubmitting,
    batchResults,
    successCount,
    failCount,
    folderStructure,
    sceneFolderNames,
    
    // Getters
    hasSingleResult,
    hasBatchResult,
    activeResult,
    successRate,
    
    // Actions
    ingestSingleProduct,
    ingestBatchProducts,
    clearResults,
    switchTab,
    triggerBatchUpload,
    handleBatchFileSelect,
    startBatchIngest,
    clearBatchProducts,
    removeProduct,
    handleStructureChange
  }
})
