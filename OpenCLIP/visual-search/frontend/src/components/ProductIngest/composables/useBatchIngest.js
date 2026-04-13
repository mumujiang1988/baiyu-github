/**
 * 批量入库逻辑
 */
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { ingestProduct, getAllProductCodes } from '../../../api/search'
import { handleApiError } from '../../../utils/messageHandler'
import { groupFilesByFolder, parseProductInfo } from '../utils/folderParser'
import { logger } from '../../../utils/logger'

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
    logger.log('[BatchIngest] 文件选择事件触发', {
      fileCount: files?.length,
      firstFilePath: files?.[0]?.webkitRelativePath
    })
      
    if (!files || files.length === 0) {
      ElMessage.warning('未选择任何文件')
      return
    }
      
    // 按文件夹分组文件
    logger.log('[BatchIngest] 开始解析文件夹', {
      structure: folderStructure.value,
      sceneNames: sceneFolderNames.value
    })
    const folderMap = groupFilesByFolder(files, folderStructure.value, sceneFolderNames.value)
      
    logger.log('[BatchIngest] 文件夹解析结果', {
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
        logger.warn('[BatchIngest] 文件夹已存在，跳过', folderName)
        continue
      }
        
      // 解析产品信息
      const { productCode, productName, spec, category } = parseProductInfo(folderName)
      logger.log('[BatchIngest] 解析产品信息', {
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
    
    // 生成批次ID
    const batchId = `batch_${Math.random().toString(36).substring(2, 10)}${Date.now().toString(36)}`
    
    // 重置统计
    successCount.value = 0
    failCount.value = 0
    
    try {
      // 串行处理所有产品（一次只处理一个）
      for (let i = 0; i < batchResults.value.length; i++) {
        const product = batchResults.value[i]
        
        // 跳过已成功的产品
        if (product.status === 'success') {
          continue
        }
        
        // 更新当前处理项的状态
        product.status = 'processing'
        product.progress = 0
        
        logger.log(`[BatchIngest] 开始处理产品 ${i + 1}/${batchResults.value.length}: ${product.folderName}`)
        
        try {
          // 计算总文件大小（用于显示上传进度）
          const totalSize = product.imageFiles.reduce((sum, file) => sum + file.size, 0)
          let uploadedSize = 0
          
          // 创建进度回调函数
          const onUploadProgress = (progressEvent) => {
            if (progressEvent.total) {
              // 计算上传进度百分比 (0-30% 为上传阶段)
              const uploadPercent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
              // 映射到 0-30% 的范围
              product.progress = Math.round(uploadPercent * 0.3)
              
              // 更新已上传大小
              uploadedSize = progressEvent.loaded
              
              logger.log(`[BatchIngest] ${product.folderName} 上传进度: ${product.progress}% (${uploadedSize}/${totalSize})`)
            }
          }
          
          // 调用API，传入进度回调
          const response = await ingestProduct(
            product.productCode, 
            product.productName, 
            product.imageFiles, 
            product.spec, 
            product.category,
            onUploadProgress,  // 传入进度回调
            batchId            // 传入批次ID
          )
          
          // API返回后直接设置为100%
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
                  
          // 根据HTTP状态码提供友好的错误提示
          const statusCode = error.response?.status
          const errorData = error.response?.data
          // 提取详细错误信息（在所有分支中使用）
          const detail = errorData?.message || errorData?.detail || error.message || ''
          let errorMessage = '入库失败'
          let suggestion = ''
                  
          if (statusCode === 400) {
            // 400 Bad Request - 显示后端返回的完整错误信息
            // 直接使用后端返回的详细错误信息
            errorMessage = '入库失败'
            suggestion = detail  // 保留完整的错误详情
                    
            // 记录详细错误日志
            logger.error(`[BatchIngest] 产品 ${product.folderName} 入库失败 - 详细错误:`, detail)
          } else if (statusCode === 429) {
            // 429 Too Many Requests - 触发限流
            errorMessage = '请求过于频繁'
            suggestion = '系统限流保护中（5次/分钟），请稍后重试或使用“批量重试”功能'
          } else if (statusCode === 504) {
            // 504 Gateway Timeout - 网关超时
            errorMessage = '处理超时'
            suggestion = '产品图片较多或系统繁忙，建议稍后使用“重试”功能再次尝试'
          } else if (statusCode === 500) {
            // 500 Internal Server Error
            errorMessage = '服务器内部错误'
            suggestion = '系统异常，请联系管理员或使用“重试”功能'
          } else {
            // 其他错误
            errorMessage = detail || '未知错误'
            suggestion = '请检查网络连接或稍后重试'
          }
                  
          product.message = errorMessage
                  
          // 如果有详细建议,在控制台输出
          if (suggestion && suggestion !== detail) {
            logger.warn(`[BatchIngest] 建议: ${suggestion}`)
          }
                  
          // 对于400错误,使用非阻塞的通知显示详细信息（不阻塞循环）
          if (statusCode === 400 && detail) {
            // 使用ElNotification而非ElMessageBox.alert，避免阻塞循环
            ElNotification({
              title: `产品 ${product.folderName} 入库失败`,
              message: `<div style="white-space: pre-wrap; line-height: 1.6; font-size: 13px; max-height: 300px; overflow-y: auto;">${detail}</div>`,
              type: 'error',
              dangerouslyUseHTMLString: true,
              duration: 8000,  // 8秒后自动关闭
              position: 'top-right'
            })
          }
          
          // 记录详细错误日志
          logger.error(`[BatchIngest] 产品 ${product.folderName} 入库失败`, {
            statusCode,
            errorMessage,
            suggestion,
            errorData
          })
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
      
      // 根据HTTP状态码提供友好的错误提示（与主入库逻辑一致）
      const statusCode = error.response?.status
      const errorData = error.response?.data
      let errorMessage = '入库失败'
      let suggestion = ''
      
      if (statusCode === 400) {
        const detail = errorData?.message || errorData?.detail || error.message || ''
        if (detail.includes('duplicate') || detail.includes('重复')) {
          errorMessage = '所有图片均已存在'
          suggestion = '该产品的所有图片已在数据库中，无需重复入库'
        } else {
          errorMessage = `请求参数错误: ${detail}`
          suggestion = '请检查产品信息和图片格式是否正确'
        }
      } else if (statusCode === 429) {
        errorMessage = '请求过于频繁'
        suggestion = '系统限流保护中（5次/分钟），请稍后再次重试'
      } else if (statusCode === 504) {
        errorMessage = '处理超时'
        suggestion = '产品图片较多或系统繁忙，建议稍后再次尝试'
      } else if (statusCode === 500) {
        errorMessage = '服务器内部错误'
        suggestion = '系统异常，请联系管理员'
      } else {
        errorMessage = errorData?.message || errorData?.detail || error.message || '未知错误'
        suggestion = '请检查网络连接或稍后重试'
      }
      
      product.message = `${errorMessage}${suggestion ? ' - ' + suggestion : ''}`
      
      logger.error(`[BatchIngest] 产品 ${product.folderName} 重试失败`, {
        statusCode,
        errorMessage,
        suggestion,
        errorData
      })
      
      ElMessage.error(`${product.folderName} 重试失败: ${errorMessage}`)
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
    
    // 依次重试每个失败的产品（添加延迟避免触发限流）
    let retryCount = 0
    for (let i = 0; i < batchResults.value.length; i++) {
      const product = batchResults.value[i]
      if (product.status === 'error') {
        await retryProduct(i)
        retryCount++
        
        // 每5个产品后等待12秒，避免触发429限流（5次/分钟）
        if (retryCount % 5 === 0 && retryCount < failedProducts.length) {
          logger.log(`[BatchIngest] 已重试 ${retryCount}/${failedProducts.length} 个产品，等待12秒以避免限流...`)
          await new Promise(resolve => setTimeout(resolve, 12000))
        }
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
  
  // 重复清理功能：对比导入的产品和已入库的产品编码，清理重复项
  const cleanDuplicateProducts = async () => {
    if (batchResults.value.length === 0) {
      ElMessage.warning('请先导入产品')
      return
    }
    
    try {
      // 显示加载状态
      const loadingMsg = ElMessage({
        message: '正在获取已入库产品列表...',
        type: 'info',
        duration: 0
      })
      
      // 获取所有已入库的产品编码
      const response = await getAllProductCodes()
      loadingMsg.close()
      
      if (!response.success || !response.product_codes) {
        ElMessage.error('获取已入库产品列表失败')
        return
      }
      
      const existingCodes = new Set(response.product_codes)
      logger.log('[BatchIngest] 已入库产品编码数量:', existingCodes.size)
      
      // 找出重复的产品
      const duplicates = []
      batchResults.value.forEach((product, index) => {
        if (existingCodes.has(product.productCode)) {
          duplicates.push({ index, product })
        }
      })
      
      if (duplicates.length === 0) {
        ElMessage.success('✅ 没有发现重复产品，可以安全入库')
        return
      }
      
      // 显示确认对话框
      const duplicateList = duplicates.map(d => 
        `- ${d.product.folderName} (${d.product.productCode})`
      ).join('\n')
      
      await ElMessageBox.confirm(
        `发现 ${duplicates.length} 个重复产品，将从列表中移除：\n\n${duplicateList}\n\n是否继续清理？`,
        '重复清理确认',
        {
          confirmButtonText: '清理重复项',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: false
        }
      )
      
      // 从后往前删除，避免索引变化
      for (let i = duplicates.length - 1; i >= 0; i--) {
        const { index } = duplicates[i]
        batchResults.value.splice(index, 1)
      }
      
      ElMessage.success(`✅ 已清理 ${duplicates.length} 个重复产品，剩余 ${batchResults.value.length} 个产品待入库`)
      
    } catch (error) {
      if (error !== 'cancel') {
        logger.error('[BatchIngest] 重复清理失败:', error)
        handleApiError(error.response || error, '重复清理失败')
      }
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
    handleStructureChange,
    cleanDuplicateProducts  // 重复清理
  }
}
