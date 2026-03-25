/**
 * API 响应处理辅助函数
 * @module utils/responseHelper
 * @description 统一处理多种 API 响应格式，简化响应判断逻辑
 */

/**
 * 判断 API 响应是否成功
 * @description 统一处理多种响应格式：
 * - code === 200 (标准 HTTP 成功状态)
 * - code === 0 (业务成功状态)
 * - errorCode === 0 (金蝶 K3 Cloud 响应格式)
 * @param {Object} response - API 响应对象
 * @returns {boolean} 是否成功
 * @example
 * isSuccessResponse({ code: 200, data: {} }) // true
 * isSuccessResponse({ code: 0, data: {} }) // true
 * isSuccessResponse({ errorCode: 0, data: {} }) // true
 * isSuccessResponse({ code: 500 }) // false
 */
export const isSuccessResponse = (response) => {
  if (!response) return false
  return response.code === 200 || 
         response.code === 0 || 
         response.errorCode === 0
}

/**
 * 获取响应数据
 * @description 从响应对象中提取数据，支持多种数据字段：
 * - data: 标准数据字段
 * - rows: 列表数据字段
 * @param {Object} response - API 响应对象
 * @returns {*} 响应数据，如果不存在则返回 null
 * @example
 * getResponseData({ code: 200, data: { id: 1 } }) // { id: 1 }
 * getResponseData({ code: 200, rows: [...] }) // [...]
 */
export const getResponseData = (response) => {
  if (!response) return null
  
  // 优先返回 data 字段
  if (response.data !== undefined) {
    // 处理 data 为空对象的情况
    if (typeof response.data === 'object' && Object.keys(response.data).length === 0) {
      return null
    }
    return response.data
  }
  
  // 其次返回 rows 字段（列表数据）
  if (response.rows !== undefined) {
    return response.rows
  }
  
  return null
}

/**
 * 获取响应消息
 * @param {Object} response - API 响应对象
 * @param {string} [defaultMsg='操作成功'] - 默认消息
 * @returns {string} 响应消息
 * @example
 * getResponseMessage({ msg: '保存成功' }) // '保存成功'
 * getResponseMessage({ message: '操作完成' }) // '操作完成'
 */
export const getResponseMessage = (response, defaultMsg = '操作成功') => {
  if (!response) return defaultMsg
  return response.msg || response.message || defaultMsg
}

/**
 * 获取响应错误消息
 * @param {Object} response - API 响应对象
 * @param {string} [defaultMsg='操作失败'] - 默认错误消息
 * @returns {string} 错误消息
 * @example
 * getResponseErrorMessage({ msg: '参数错误' }) // '参数错误'
 */
export const getResponseErrorMessage = (response, defaultMsg = '操作失败') => {
  if (!response) return defaultMsg
  return response.msg || response.message || response.errorMsg || defaultMsg
}

/**
 * 获取分页总数
 * @param {Object} response - API 响应对象
 * @returns {number} 总数，默认为 0
 * @example
 * getTotal({ total: 100 }) // 100
 */
export const getTotal = (response) => {
  if (!response) return 0
  return response.total || 0
}

/**
 * 判断响应是否有数据
 * @param {Object} response - API 响应对象
 * @returns {boolean} 是否有数据
 */
export const hasResponseData = (response) => {
  const data = getResponseData(response)
  if (data === null || data === undefined) return false
  if (Array.isArray(data)) return data.length > 0
  if (typeof data === 'object') return Object.keys(data).length > 0
  return true
}

export default {
  isSuccessResponse,
  getResponseData,
  getResponseMessage,
  getResponseErrorMessage,
  getTotal,
  hasResponseData
}
