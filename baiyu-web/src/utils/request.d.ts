import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'

// 扩展 AxiosRequestConfig 支持自定义属性
export interface CustomAxiosRequestConfig extends AxiosRequestConfig {
  headers?: {
    isToken?: boolean
    repeatSubmit?: boolean
    [key: string]: any
  }
}

// 响应数据结构
export interface ResponseData<T = any> {
  code: number
  msg: string
  data: T
}

// request 实例类型
declare const service: AxiosInstance

// 下载方法
export function download(
  url: string,
  params: any,
  filename: string,
  config?: CustomAxiosRequestConfig
): Promise<void>

// 重新登录状态
export interface ReloginState {
  show: boolean
}

export const isRelogin: ReloginState

// 默认导出
export default service
