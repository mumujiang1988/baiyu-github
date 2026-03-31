/**
 * @fileoverview ERP 通用工具函数类型声明
 * @description 为 ERP 模块提供 TypeScript 类型支持，解决模块导入类型错误
 */

// ==================== Axios 请求类型 ====================

/**
 * Axios 请求配置扩展
 */
export interface RequestOptions {
  /** 是否需要设置 token */
  isToken?: boolean
  /** 是否需要防止数据重复提交 */
  repeatSubmit?: boolean
  url?: string
  method?: string
  params?: any
  data?: any
  headers?: any
}

/**
 * 通用响应数据结构
 */
export interface ResponseData<T = any> {
  code: number | string
  msg: string
  data: T
  errorCode?: number
}

/**
 * 分页响应数据结构
 */
export interface PageResponseData<T = any> {
  rows: T[]
  total: number
}

// ==================== 格式化函数类型 ====================

/**
 * 格式化货币
 */
export function formatCurrency(value: number | string, precision?: number): string

/**
 * 格式化日期
 */
export function formatDate(value: string, format?: string): string

/**
 * 格式化日期时间
 */
export function formatDateTime(value: string, format?: string): string

/**
 * 格式化百分比
 */
export function formatPercent(value: number | string, precision?: number): string

/**
 * 格式化金额（带货币符号）
 */
export function formatAmount(value: number | string, currency?: string, precision?: number): string

// ==================== 响应处理工具类型 ====================

/**
 * 获取响应数据
 */
export function getResponseData<T = any>(response: ResponseData<T>, defaultValue?: T): T

/**
 * 判断响应是否成功
 */
export function isSuccessResponse(response: ResponseData<any>): boolean

// ==================== Request 实例类型 ====================

/**
 * Request 工具函数类型
 */
export type RequestFn = <T = any>(config: RequestOptions) => Promise<ResponseData<T>>
