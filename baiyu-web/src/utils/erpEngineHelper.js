/**
 * ERP 引擎统一响应处理工具
 */

/**
 * 检查响应是否成功
 * @param {Object} response - 响应对象
 * @returns {boolean} 是否成功
 */
export const isSuccess = (response) => {
  if (!response) return false
  return response.code === 200 || response.code === 0 || response.errorCode === 0
}

/**
 * 处理引擎响应
 * @param {Object} response - 响应对象
 * @param {string} errorMessage - 错误消息
 * @returns {any} 成功返回 data，失败抛出异常
 */
export const handleEngineResponse = (response, errorMessage = '操作失败') => {
  if (isSuccess(response)) {
    return response.data
  } else {
    throw new Error(response.msg || errorMessage)
  }
}

/**
 * 安全调用引擎 API（返回 null 而不是抛异常）
 * @param {Function} apiMethod - API 方法
 * @param {any} params - 参数
 * @returns {Promise<any>} 结果数据
 */
export const safeCallEngine = async (apiMethod, params) => {
  try {
    const response = await apiMethod(params)
    return isSuccess(response) ? response.data : null
  } catch (error) {
    console.error('引擎调用失败:', error)
    return null
  }
}
