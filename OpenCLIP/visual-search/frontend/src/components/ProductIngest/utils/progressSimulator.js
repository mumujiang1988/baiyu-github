/**
 * 进度模拟工具
 */

/**
 * 模拟单产品上传进度
 * @param {Object} callbacks - 回调函数对象
 * @param {Function} callbacks.onProgress - 进度更新回调 (progress, detail)
 * @param {Function} callbacks.onComplete - 完成回调
 * @returns {Promise<void>}
 */
export function simulateSingleProductProgress(callbacks) {
  return new Promise((resolve) => {
    let progress = 0
    let currentImageIndex = 1
    const totalImages = callbacks.totalImages || 1
    
    const interval = setInterval(() => {
      if (progress < 30) {
        // 上传阶段 0-30%
        progress += Math.random() * 5
        callbacks.onProgress(progress, '正在上传图片...')
      } else if (progress < 70) {
        // 处理阶段 30-70%
        currentImageIndex = Math.min(
          Math.floor((progress - 30) / 40 * totalImages) + 1,
          totalImages
        )
        progress += Math.random() * 3
        callbacks.onProgress(progress, `正在处理图片 ${currentImageIndex}/${totalImages}...`)
      } else if (progress < 95) {
        // 特征提取阶段 70-95%
        progress += Math.random() * 2
        callbacks.onProgress(progress, '正在提取图片特征...')
      } else {
        // 完成阶段
        clearInterval(interval)
        callbacks.onComplete()
        resolve()
        return
      }
      
      if (progress > 100) progress = 100
    }, 100)
  })
}

/**
 * 模拟批量入库中单个产品的进度
 * @param {Object} product - 产品对象（需要有 progress 属性）
 * @returns {Promise<void>}
 */
export function simulateBatchProductProgress(product) {
  return new Promise((resolve) => {
    let currentProgress = 0
    
    const interval = setInterval(() => {
      if (currentProgress < 30) {
        currentProgress += Math.random() * 8
      } else if (currentProgress < 70) {
        currentProgress += Math.random() * 6
      } else if (currentProgress < 95) {
        currentProgress += Math.random() * 4
      } else {
        currentProgress = 95
        clearInterval(interval)
        resolve()
        return
      }
      
      if (currentProgress > 95) currentProgress = 95
      product.progress = Math.floor(currentProgress)
    }, 80)
  })
}
