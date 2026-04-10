/**
 * 前端响应适配器
 * 
 * 统一处理后端 API 响应，确保数据访问的一致性。
 * 适配后端扁平响应格式：{ success, message, ...business_data }
 * 
 * @example
 * // 后端返回
 * {
 *   "success": true,
 *   "message": "检索成功",
 *   "results": [...],
 *   "search_time_ms": 150
 * }
 * 
 * // 前端使用
 * const { results, search_time_ms } = response  // 直接解构
 */

/**
 * 提取响应数据（扁平格式）
 * 
 * 对于扁平格式，直接返回整个响应对象
 * 业务数据已经在根级别，无需额外解包
 * 
 * @param {Object} response - API 响应对象
 * @returns {Object} 响应对象本身
 * 
 * @example
 * const response = await searchImage(file)
 * const data = extractData(response)
 * console.log(data.results)  // 直接访问业务字段
 */
export function extractData(response) {
  if (!response) {
    return null
  }
  
  // 扁平格式：直接返回响应对象
  // 业务数据已经在根级别
  if (response.hasOwnProperty('success')) {
    // 成功响应，返回整个响应
    if (response.success) {
      return response
    }
    // 失败响应，抛出错误
    throw new Error(response.message || '请求失败')
  }
  
  // 旧格式已废弃，不再支持
  console.warn('⚠️ 检测到旧的嵌套格式响应，请联系后端开发人员更新为扁平格式')
  return response
}

/**
 * 提取分页数据（扁平格式）
 * 
 * @param {Object} response - API 响应对象
 * @returns {Object} { items, pagination }
 * 
 * @example
 * const { items, pagination } = extractPaginatedData(response)
 */
export function extractPaginatedData(response) {
  // 扁平格式：直接从响应中获取
  if (response && response.items && response.pagination) {
    return {
      items: response.items,
      pagination: response.pagination
    }
  }
  
  // 旧格式已废弃，不再支持
  console.warn('⚠️ 检测到旧的嵌套格式分页响应，请联系后端开发人员更新')
  return { items: [], pagination: { total: 0, page: 1, page_size: 20, total_pages: 0 } }
}

/**
 * 检查响应是否成功
 * @param {Object} response - API 响应对象
 * @returns {boolean} 是否成功
 */
export function isSuccess(response) {
  if (!response) {
    return false
  }
  
  // 统一格式
  if (response.hasOwnProperty('success')) {
    return response.success === true
  }
  
  // 兼容旧格式：没有 error 字段即认为成功
  return !response.error && !response.message?.includes('失败')
}

/**
 * 获取响应消息
 * @param {Object} response - API 响应对象
 * @returns {string} 消息文本
 */
export function getMessage(response) {
  if (!response) {
    return ''
  }
  
  // 统一格式
  if (response.message) {
    return response.message
  }
  
  return ''
}

/**
 * 获取错误信息
 * @param {Object} response - API 响应对象
 * @returns {string} 错误信息
 */
export function getErrorMessage(response) {
  if (!response) {
    return '未知错误'
  }
  
  // 统一格式
  if (response.message) {
    return response.message
  }
  
  // 兼容旧格式
  if (response.error) {
    return response.error
  }
  
  return '未知错误'
}

/**
 * 安全的响应处理器
 * @param {Object} response - API 响应对象
 * @param {Function} onSuccess - 成功回调
 * @param {Function} onError - 失败回调
 */
export function handleResponse(response, onSuccess, onError) {
  try {
    if (isSuccess(response)) {
      const data = extractData(response)
      if (onSuccess && typeof onSuccess === 'function') {
        onSuccess(data, response)
      }
    } else {
      const error = new Error(getErrorMessage(response))
      error.response = response
      if (onError && typeof onError === 'function') {
        onError(error, response)
      }
    }
  } catch (error) {
    if (onError && typeof onError === 'function') {
      onError(error, response)
    }
  }
}
