/**
 * 统一日志工具
 * 
 * 开发环境：输出所有日志（log/warn/error）
 * 生产环境：仅输出错误日志（error）
 * 
 * @example
 * import { logger } from '@/utils/logger'
 * logger.log('调试信息')    // 仅开发环境
 * logger.warn('警告信息')   // 仅开发环境
 * logger.error('错误信息')  // 始终输出
 */

const isDev = import.meta.env.DEV

export const logger = {
  /**
   * 普通日志 - 仅开发环境输出
   */
  log: (...args) => {
    if (isDev) {
      console.log(...args)
    }
  },

  /**
   * 警告日志 - 仅开发环境输出
   */
  warn: (...args) => {
    if (isDev) {
      console.warn(...args)
    }
  },

  /**
   * 错误日志 - 始终输出
   */
  error: (...args) => {
    console.error(...args)
  },

  /**
   * 分组日志 - 仅开发环境输出
   */
  group: (label, fn) => {
    if (isDev && console.group) {
      console.group(label)
      fn()
      console.groupEnd()
    } else {
      fn()
    }
  }
}
