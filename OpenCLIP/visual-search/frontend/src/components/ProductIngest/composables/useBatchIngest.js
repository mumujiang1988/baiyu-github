/**
 * 批量入库逻辑
 */
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ingestProduct } from '../../../api/search'
import { handleApiError } from '../../../utils/messageHandler'
import { groupFilesByFolder, parseProductInfo } from '../utils/folderParser'
import { simulateBatchProductProgress } from '../utils/progressSimulator'

export function useBatchIngest() {
  // 状态
  const batchInputRef = ref(null)
  const batchSubmitting = ref(false)
  const batchResults = ref([])
  const successCount = ref(0)
  const failCount = ref(0)
  
  // 目录结构配置
  const folderStructure = ref('standard')
  const sceneFolderNames = ref('')
  
  // 页面关闭保护
  const handleBeforeUnload = (e) => {
    if (batchSubmitting.value) {
      e.preventDefault()
      e.returnValue = '批量入库进行中，确认离开？'
      return e.returnValue
    }
  }
  
  // 监听入库状态，添加/移除页面关闭保护
  const watchSubmitting = () => {
    if (batchSubmitting.value) {
      window.addEventListener('beforeunload', handleBeforeUnload)
    } else {
      window.removeEventListener('beforeunload', handleBeforeUnload)
    }
  }
  
  // 计算成功率
  const successRate = computed(() => {
    const total = successCount.value + failCount.value
    return total > 0 ? ((successCount.value / total) * 100).toFixed(2) : 0
  })
  
  // 触发批量上传
  const triggerBatchUpload = () => {
    if (batchInputRef.value) {
      batchInputRef.value.click()
    }
  }
  
  // 处理批量文件选择
  const handleBatchFileSelect = async (event) => {
    const files = event.target.files
    console.log('[BatchIngest] 文件选择事件触发', {
      fileCount: files?.length,
      firstFilePath: files?.[0]?.webkitRelativePath
    })
      
    if (!files || files.length === 0) {
      ElMessage.warning('未选择任何文件')
      return
    }
      
    // 按文件夹分组文件
    console.log('[BatchIngest] 开始解析文件夹', {
      structure: folderStructure.value,
      sceneNames: sceneFolderNames.value
    })
    const folderMap = groupFilesByFolder(files, folderStructure.value, sceneFolderNames.value)
      
    console.log('[BatchIngest] 文件夹解析结果', {
      folderCount: folderMap.size,
      folders: Array.from(folderMap.keys())
    })
      
    if (folderMap.size === 0) {
      ElMessage.warning('未找到有效的产品文件夹')
      return
    }
      
    // 将文件夹信息添加到表格中
    let addedCount = 0
    for (const [folderName, imageFiles] of folderMap.entries()) {
      // 检查是否已存在
      const exists = batchResults.value.some(item => item.folderName === folderName)
      if (exists) {
        console.warn('[BatchIngest] 文件夹已存在，跳过', folderName)
        continue
      }
        
      // 解析产品信息
      const { productCode, productName, spec, category } = parseProductInfo(folderName)
      console.log('[BatchIngest] 解析产品信息', {
        folderName,
        productCode,
        productName,
        spec,
        category,
        imageCount: imageFiles.length
      })
        
      batchResults.value.push({
        folderName,
        productCode,
        productName,
        spec,
        category,
        imageCount: imageFiles.length,
        status: 'pending',
        progress: 0,
        message: '',
        imageFiles
      })
      addedCount++
    }
      
    ElMessage.success(`成功导入 ${addedCount} 个产品，请确认后点击“产品入库”`)
      
    // 清空文件输入
    event.target.value = ''
  }
  
  // 开始批量入库
  const startBatchIngest = async () => {
    if (batchResults.value.length === 0) {
      ElMessage.warning('请先导入产品')
      return
    }
    
    // 确认入库
    try {
      await ElMessageBox.confirm(
        `确认对 ${batchResults.value.length} 个产品执行入库操作？`,
        '入库确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } catch {
      return
    }
    
    // 开始批量入库
    batchSubmitting.value = true
    watchSubmitting()  // 添加页面关闭保护
    
    // 重置统计
    successCount.value = 0
    failCount.value = 0
    
    try {
      for (const product of batchResults.value) {
        // 跳过已经成功的
        if (product.status === 'success') {
          continue
        }
        
        // 更新当前处理项的状态
        product.status = 'processing'
        product.progress = 0
        
        try {
          // 同时执行进度模拟和实际上传
          const [response] = await Promise.all([
            ingestProduct(product.productCode, product.productName, product.imageFiles, product.spec, product.category),
            simulateBatchProductProgress(product)
          ])
          
          // 完成进度
          product.progress = 100
          
          // 更新表格中的状态（扁平格式：直接访问字段）
          if (response.success) {
            product.status = 'success'
            const ingestedImages = response.ingested_images || 0
            
            if (ingestedImages > 0) {
              product.message = `${ingestedImages} 张图片入库成功`
            } else {
              product.message = response.message || '入库成功'
            }
            successCount.value++
          } else {
            product.status = 'error'
            product.message = response.message || '入库失败'
            failCount.value++
          }
        } catch (error) {
          failCount.value++
          product.status = 'error'
          product.progress = 100
          product.message = error.message || '入库失败'
        }
      }
      
      // 显示结果
      if (failCount.value === 0) {
        ElMessage.success(`批量入库完成！共 ${successCount.value} 个产品`)
      } else {
        ElMessage.warning(`批量入库完成！成功: ${successCount.value}, 失败: ${failCount.value}`)
      }
    } catch (error) {
      handleApiError(error.response || error, '批量入库失败')
    } finally {
      batchSubmitting.value = false
      watchSubmitting()  // 移除页面关闭保护
    }
  }
  
  // 清空产品表格
  const clearBatchProducts = async () => {
    if (batchResults.value.length === 0) {
      return
    }
    
    try {
      await ElMessageBox.confirm(
        '确认清空所有产品？此操作不可恢复。',
        '清空确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } catch {
      return
    }
    
    batchResults.value = []
    successCount.value = 0
    failCount.value = 0
    ElMessage.success('已清空产品列表')
  }
  
  // 删除单个产品
  const removeProduct = (index) => {
    const product = batchResults.value[index]
    if (product.status === 'processing') {
      ElMessage.warning('正在处理的产品不能删除')
      return
    }
    
    batchResults.value.splice(index, 1)
    ElMessage.success('已删除')
  }
  
  // 重试失败的产品
  const retryProduct = async (index) => {
    const product = batchResults.value[index]
    if (!product || product.status !== 'error') {
      return
    }
    
    // 保存前一个状态，用于准确调整计数
    const previousStatus = product.status
    
    // 重置状态
    product.status = 'processing'
    product.progress = 0
    product.message = ''
    
    try {
      const [response] = await Promise.all([
        ingestProduct(product.productCode, product.productName, product.imageFiles, product.spec, product.category),
        simulateBatchProductProgress(product)
      ])
      
      product.progress = 100
      
      if (response.success) {
        product.status = 'success'
        const ingestedImages = response.ingested_images || 0
        
        if (ingestedImages > 0) {
          product.message = `${ingestedImages} 张图片入库成功`
        } else {
          product.message = response.message || '入库成功'
        }
        
        // 只有从失败变为成功时才调整计数
        if (previousStatus === 'error') {
          successCount.value++
          failCount.value--
        }
        
        ElMessage.success(`${product.folderName} 重试成功`)
      } else {
        product.status = 'error'
        product.message = response.message || '入库失败'
        
        // 只有从成功变为失败时才调整计数（理论上不应该发生）
        if (previousStatus === 'success') {
          failCount.value++
          successCount.value--
        }
        
        ElMessage.error(`${product.folderName} 重试失败`)
      }
    } catch (error) {
      product.status = 'error'
      product.progress = 100
      product.message = error.message || '入库失败'
      ElMessage.error(`${product.folderName} 重试失败: ${error.message}`)
    }
  }
  
  // 批量重试所有失败项
  const retryAllFailed = async () => {
    const failedProducts = batchResults.value.filter(p => p.status === 'error')
    
    if (failedProducts.length === 0) {
      ElMessage.info('没有失败的产品需要重试')
      return
    }
    
    try {
      await ElMessageBox.confirm(
        `确认重试 ${failedProducts.length} 个失败的产品？`,
        '批量重试确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } catch {
      return
    }
    
    // 依次重试每个失败的产品
    for (let i = 0; i < batchResults.value.length; i++) {
      const product = batchResults.value[i]
      if (product.status === 'error') {
        await retryProduct(i)
      }
    }
    
    ElMessage.success(`批量重试完成！成功: ${successCount.value}, 失败: ${failCount.value}`)
  }
  
  // 处理目录结构变化
  const handleStructureChange = () => {
    if (batchResults.value.length > 0) {
      ElMessageBox.confirm(
        '切换目录结构将清空当前产品列表，是否继续？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        clearBatchProducts()
      }).catch(() => {
        // 取消操作
      })
    }
  }
  
  return {
    // 状态
    batchInputRef,
    batchSubmitting,
    batchResults,
    successCount,
    failCount,
    successRate,
    folderStructure,
    sceneFolderNames,
    
    // 方法
    triggerBatchUpload,
    handleBatchFileSelect,
    startBatchIngest,
    clearBatchProducts,
    removeProduct,
    retryProduct,  // 单个重试
    retryAllFailed,  // 批量重试
    handleStructureChange
  }
}
