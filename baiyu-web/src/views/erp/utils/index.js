/**
 * ERP 低代码引擎 - 工具函数统一导出
 * @module views/erp/utils
 */

// 格式化工具
export {
  formatCurrency,
  formatDate,
  formatDateTime,
  formatPercent,
  formatAmount,
  formatNumber,
  formatFileSize
} from './formatters'

// 响应处理
export {
  isSuccessResponse,
  getResponseData,
  getResponseMessage,
  getResponseErrorMessage,
  getTotal,
  hasResponseData
} from './responseHelper'

// 配置解析器
export { default as ERPConfigParser } from './ERPConfigParser.mjs'

// 默认导出
import * as formatters from './formatters'
import * as responseHelper from './responseHelper'
import ERPConfigParser from './ERPConfigParser.mjs'

export default {
  ...formatters,
  ...responseHelper,
  ERPConfigParser
}
