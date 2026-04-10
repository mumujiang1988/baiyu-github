/**
 * Blob URL 管理 Composable
 * 
 * 统一管理 Object URL 的创建和清理，防止内存泄漏
 * 
 * @example
 * const { createUrl, cleanup } = useBlobUrl()
 * const url = createUrl(file)
 * onUnmounted(() => cleanup())
 */

import { ref, onUnmounted } from 'vue'

export function useBlobUrl() {
  const createdUrls = ref([])

  /**
   * 创建安全的 Object URL（自动跟踪）
   * @param {File|Blob} file - 文件或 Blob 对象
   * @returns {string} Object URL
   */
  const createUrl = (file) => {
    if (!file) return ''
    
    const url = URL.createObjectURL(file)
    createdUrls.value.push(url)
    return url
  }

  /**
   * 清理所有创建的 URL
   */
  const cleanup = () => {
    createdUrls.value.forEach(url => {
      try {
        URL.revokeObjectURL(url)
      } catch (e) {
        // 忽略清理错误
      }
    })
    createdUrls.value = []
  }

  /**
   * 清理单个 URL
   * @param {string} url - 要清理的 URL
   */
  const revokeUrl = (url) => {
    if (!url) return
    
    const index = createdUrls.value.indexOf(url)
    if (index !== -1) {
      try {
        URL.revokeObjectURL(url)
      } catch (e) {
        // 忽略清理错误
      }
      createdUrls.value.splice(index, 1)
    }
  }

  // 组件卸载时自动清理
  onUnmounted(() => {
    cleanup()
  })

  return {
    createUrl,
    cleanup,
    revokeUrl,
    urls: createdUrls
  }
}
