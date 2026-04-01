import request from '@/utils/request'
import type { ErpConfig, ConfigQueryParams } from '../types/config'

/**
 * API 响应类型
 */
export interface ApiResponse<T = any> {
  code: number
  msg: string
  data?: T
}

/**
 * 配置列表响应
 */
export interface ConfigListResponse {
  rows: ErpConfig[]
  total: number
}

/**
 * 查询配置列表
 * @param query - 查询参数
 * @returns Promise<ApiResponse<ConfigListResponse>>
 */
export function listConfig(query: ConfigQueryParams): Promise<ApiResponse<ConfigListResponse>> {
  return request({
    url: '/erp/config/list',
    method: 'get',
    params: query
  })
}

/**
 * 查询配置详情（支持 moduleCode 和 configId 两种方式）
 * @param id - 配置 ID 或 moduleCode
 * @param configType - 配置类型（使用 moduleCode 时必填）
 * @returns Promise<ApiResponse<ErpConfig>>
 */
export function getConfig(
  id: string | number, 
  configType?: string
): Promise<ApiResponse<ErpConfig>> {
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

/**
 * 保存配置（低代码通用接口——智能判断新增或修改）
 * @param data - 配置数据
 * @returns Promise<ApiResponse>
 */
export function saveConfig(data: Partial<ErpConfig>): Promise<ApiResponse> {
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

/**
 * 删除配置
 * @param id - 配置 ID
 * @returns Promise<ApiResponse>
 */
export function delConfig(id: string | number): Promise<ApiResponse> {
  return request({
    url: `/erp/config/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除配置
 * @param ids - 配置 ID 数组
 * @returns Promise<ApiResponse>
 */
export function batchDelConfig(ids: (string | number)[]): Promise<ApiResponse> {
  return request({
    url: '/erp/config/batch',        // 使用 /batch，与后端保持一致
    method: 'delete',                // 使用 DELETE，与后端保持一致
    data: ids
  })
}

/**
 * 查询配置历史版本
 * @param configId - 配置 ID
 * @returns Promise<ApiResponse>
 */
export function getConfigHistory(configId: string | number): Promise<ApiResponse> {
  return request({
    url: `/erp/config/history/${configId}`,
    method: 'get'
  })
}

/**
 * 查看版本详情
 * @param configId - 配置 ID
 * @param version - 版本号
 * @returns Promise<ApiResponse>
 */
export function getVersionDetail(configId: string | number, version: number): Promise<ApiResponse> {
  return request({
    url: `/erp/config/history/${configId}/${version}`,
    method: 'get'
  })
}

/**
 * 回滚到指定版本
 * @param data - 回滚参数
 * @returns Promise<ApiResponse>
 */
export function rollbackToVersion(data: { configId: string | number; version: number }): Promise<ApiResponse> {
  return request({
    url: '/erp/config/rollback',
    method: 'post',
    data: data
  })
}

/**
 * 导出配置
 * @param id - 配置 ID
 * @returns Promise<Blob>
 */
export function exportConfig(id: string | number): Promise<Blob> {
  return request({
    url: `/erp/config/${id}/export`,
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 导入配置
 * @param data - 导入数据（包含文件）
 * @returns Promise<ApiResponse>
 */
export function importConfig(data: FormData): Promise<ApiResponse> {
  return request({
    url: '/erp/config/import',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 复制配置
 * @param id - 配置 ID
 * @returns Promise<ApiResponse>
 */
export function copyConfig(id: string | number): Promise<ApiResponse> {
  return request({
    url: `/erp/config/${id}/copy`,
    method: 'post'
  })
}

/**
 * 获取配置模板列表
 * @param type - 配置类型
 * @returns Promise<ApiResponse>
 */
export function getConfigTemplates(type: string): Promise<ApiResponse> {
  return request({
    url: '/erp/config/templates',
    method: 'get',
    params: { type }
  })
}

/**
 * 获取配置模板内容
 * @param templateId - 模板 ID
 * @returns Promise<ApiResponse>
 */
export function getTemplateContent(templateId: string): Promise<ApiResponse> {
  return request({
    url: `/erp/config/templates/${templateId}`,
    method: 'get'
  })
}

/**
 * 更新配置状态
 * @param data - 状态数据（包含 configId, status）
 * @returns Promise<ApiResponse>
 */
export function updateConfigStatus(data: { configId: string | number; status: '0' | '1' }): Promise<ApiResponse> {
  return request({
    url: '/erp/config/status',
    method: 'put',
    data: data
  })
}

/**
 * 验证配置内容
 * @param data - 验证数据（包含 configType, configContent）
 * @returns Promise<ApiResponse>
 */
export function validateConfigContent(data: { configType: string; configContent: string }): Promise<ApiResponse> {
  return request({
    url: '/erp/config/validate',
    method: 'post',
    data: data
  })
}
