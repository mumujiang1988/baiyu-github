import request from '@/utils/request'
import type { ErpConfig, ConfigQueryParams } from './types/config'

export interface ApiResponse<T = any> {
  code: number
  msg: string
  data?: T
}

export interface ConfigListResponse {
  rows: ErpConfig[]
  total: number
}

export function listConfig(query: ConfigQueryParams) {
  return request({
    url: '/erp/config/list',
    method: 'get',
    params: query
  })
}

export function getConfig(id: string | number, configType?: string) {
  // 如果为数字或可转换为数字字符串，使用 ID 查询
  const numericId = typeof id === 'number' ? id : Number(id)
  if (!isNaN(numericId)) {
    return request({
      url: `/erp/config/${numericId}`,
      method: 'get'
    })
  }
  // 否则使用 moduleCode + configType 查询
  else {
    const params = configType ? { moduleCode: id, configType } : { moduleCode: id }
    return request({
      url: '/erp/config/get',
      method: 'get',
      params: params
    })
  }
}

export function saveConfig(data: Partial<ErpConfig>) {
  // 根据 configId 判断使用 POST 还是 PUT
  if (data.configId) {
    // 更新操作，使用 PUT 方法
    return request({
      url: '/erp/config',
      method: 'put',
      data: data
    })
  } else {
    // 新增操作，使用 POST 方法
    return request({
      url: '/erp/config',
      method: 'post',
      data: data
    })
  }
}

export function delConfig(id: string | number) {
  return request({
    url: `/erp/config/${id}`,
    method: 'delete'
  })
}

export function batchDelConfig(ids: (string | number)[]) {
  return request({
    url: '/erp/config/batch',        // 使用 /batch，与后端保持一致
    method: 'delete',                // 使用 DELETE，与后端保持一致
    data: ids
  })
}

export function getConfigHistory(configId: string | number) {
  return request({
    url: `/erp/config/history/${configId}`,
    method: 'get'
  })
}

export function getVersionDetail(configId: string | number, version: number) {
  return request({
    url: `/erp/config/history/${configId}/${version}`,
    method: 'get'
  })
}

export function rollbackToVersion(data: { configId: string | number; version: number }) {
  return request({
    url: '/erp/config/rollback',
    method: 'post',
    data: data
  })
}

export function exportConfig(id: string | number) {
  return request({
    url: `/erp/config/${id}/export`,
    method: 'get',
    responseType: 'blob'
  })
}

export function importConfig(data: FormData) {
  return request({
    url: '/erp/config/import',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function copyConfig(id: string | number) {
  return request({
    url: `/erp/config/${id}/copy`,
    method: 'post'
  })
}

export function getConfigTemplates(type: string) {
  return request({
    url: '/erp/config/templates',
    method: 'get',
    params: { type }
  })
}

export function getTemplateContent(templateId: string) {
  return request({
    url: `/erp/config/templates/${templateId}`,
    method: 'get'
  })
}

export function updateConfigStatus(data: { configId: string | number; status: '0' | '1' }) {
  return request({
    url: '/erp/config/status',
    method: 'put',
    data: data
  })
}

export function validateConfigContent(data: { configType: string; configContent: string }) {
  return request({
    url: '/erp/config/validate',
    method: 'post',
    data: data
  })
}
