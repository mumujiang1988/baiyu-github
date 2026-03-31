/**
 * @fileoverview ERP 通用工具函数
 * @description 提供 ERP 模块通用的格式化、响应处理等工具函数
 */

import dayjs from 'dayjs'

/**
 * 格式化货币
 * @param {number|string} value - 值
 * @param {number} precision - 精度，默认 2 位小数
 * @returns {string} 格式化后的货币字符串
 */
export const formatCurrency = (value, precision = 2) => {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  
  const numValue = typeof value === 'string' ? parseFloat(value) : Number(value)
  
  if (isNaN(numValue)) {
    return '-'
  }
  
  return numValue.toFixed(precision).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 格式化日期
 * @param {string} value - 日期字符串
 * @param {string} format - 格式，默认 YYYY-MM-DD
 * @returns {string} 格式化后的日期字符串
 */
export const formatDate = (value, format = 'YYYY-MM-DD') => {
  if (!value) return '-'
  return dayjs(value).format(format)
}

/**
 * 格式化日期时间
 * @param {string} value - 日期时间字符串
 * @param {string} format - 格式，默认 YYYY-MM-DD HH:mm:ss
 * @returns {string} 格式化后的日期时间字符串
 */
export const formatDateTime = (value, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!value) return '-'
  return dayjs(value).format(format)
}

/**
 * 格式化百分比
 * @param {number|string} value - 值
 * @param {number} precision - 精度，默认 2 位小数
 * @returns {string} 格式化后的百分比字符串
 */
export const formatPercent = (value, precision = 2) => {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  
  const numValue = typeof value === 'string' ? parseFloat(value) : Number(value)
  
  if (isNaN(numValue)) {
    return '-'
  }
  
  return numValue.toFixed(precision) + '%'
}

/**
 * 格式化金额（带货币符号）
 * @param {number|string} value - 值
 * @param {string} currency - 货币符号，默认 ¥
 * @param {number} precision - 精度，默认 2 位小数
 * @returns {string} 格式化后的金额字符串
 */
export const formatAmount = (value, currency = '¥', precision = 2) => {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  
  const formatted = formatCurrency(value, precision)
  return currency + formatted
}

/**
 * 获取响应数据
 * @template T
 * @param {Object} response - 响应对象
 * @param {T} [defaultValue=null] - 默认值
 * @returns {T} 响应数据
 */
export const getResponseData = (response, defaultValue = null) => {
  return isSuccessResponse(response) ? (response.data || defaultValue) : defaultValue
}

/**
 * 判断响应是否成功
 * @param {Object} response - 响应对象
 * @returns {boolean} 是否成功
 */
export const isSuccessResponse = (response) => {
  return response && (response.code === 200 || response.code === 0 || response.errorCode === 0)
}
