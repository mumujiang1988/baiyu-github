/**
 * Retry ingestion logic for failed products
 */
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { retryProductIngest } from '../../../api/search'

export function useRetry() {
  const retrying = ref(false)
  
  /**
   * Handle retry for a failed product
   * @param {string} productCode - Product code to retry
   * @param {Function} onSuccess - Callback on success
   * @returns {Promise<boolean>} Success status
   */
  const handleRetry = async (productCode, onSuccess = null) => {
    try {
      // Confirm with user
      await ElMessageBox.confirm(
        `确定要重试产品 ${productCode} 吗？这将清除现有数据并允许重新上传。`,
        '确认重试',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      retrying.value = true
      
      // Call retry API
      const response = await retryProductIngest(productCode)
      
      if (response.success) {
        ElMessage.success(response.message)
        
        // Execute callback if provided
        if (onSuccess && typeof onSuccess === 'function') {
          onSuccess()
        }
        
        return true
      } else {
        ElMessage.error(response.message || '重试失败')
        return false
      }
      
    } catch (error) {
      // User cancelled
      if (error === 'cancel' || error === 'close') {
        return false
      }
      
      // API error
      const errorMsg = error.response?.data?.message || error.message || '重试失败'
      ElMessage.error(errorMsg)
      return false
      
    } finally {
      retrying.value = false
    }
  }
  
  return {
    retrying,
    handleRetry
  }
}
