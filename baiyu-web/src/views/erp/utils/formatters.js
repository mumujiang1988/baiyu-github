/**
 * ERP 低代码引擎 - 公共格式化工具函数
 * @module views/erp/utils/formatters
 * @description 统一的格式化函数，避免代码重复
 */

import dayjs from 'dayjs'

/**
 * 格式化货币金额
 * @param {number|string} value - 数值
 * @param {number} [precision=2] - 小数位数
 * @returns {string} 格式化后的货币字符串
 */
export const formatCurrency = (value, precision = 2) => {
  if (!value && value !== 0) return '-'
  return Number(value).toLocaleString('zh-CN', {
    minimumFractionDigits: precision,
    maximumFractionDigits: precision
  })
}

/**
 * 格式化日期
 * @param {string|Date} value - 日期值
 * @param {string} [format='YYYY-MM-DD'] - 格式化模板
 * @returns {string} 格式化后的日期字符串
 */
export const formatDate = (value, format = 'YYYY-MM-DD') => {
  if (!value) return '-'
  try {
    return dayjs(value).format(format)
  } catch (error) {
    return value
  }
}

/**
 * 格式化日期时间
 * @param {string|Date} value - 日期时间值
 * @param {string} [format='YYYY-MM-DD HH:mm:ss'] - 格式化模板
 * @returns {string} 格式化后的日期时间字符串
 */
export const formatDateTime = (value, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!value) return '-'
  try {
    return dayjs(value).format(format)
  } catch (error) {
    return value
  }
}

/**
 * 格式化百分比
 * @param {number|string} value - 数值
 * @param {number} [precision=2] - 小数位数
 * @returns {string} 格式化后的百分比字符串
 */
export const formatPercent = (value, precision = 2) => {
  if (!value && value !== 0) return '-'
  return `${Number(value).toFixed(precision)}%`
}

/**
 * 格式化金额（简化版，用于表格显示）
 * @param {number|string} value - 数值
 * @returns {string} 格式化后的金额字符串
 */
export const formatAmount = (value) => {
  if (!value && value !== 0) return '-'
  return Number(value).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

/**
 * 格式化数字
 * @param {number|string} value - 数值
 * @param {number} [precision=0] - 小数位数
 * @returns {string} 格式化后的数字字符串
 */
export const formatNumber = (value, precision = 0) => {
  if (!value && value !== 0) return '-'
  return Number(value).toLocaleString('zh-CN', {
    minimumFractionDigits: precision,
    maximumFractionDigits: precision
  })
}

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的文件大小字符串
 */
export const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const k = 1024
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${units[i]}`
}

export default {
  formatCurrency,
  formatDate,
  formatDateTime,
  formatPercent,
  formatAmount,
  formatNumber,
  formatFileSize
}
