/**
 * JSON 配置处理工具函数
 */

import type { ErpConfig } from '../types/config'

/**
 * 将分散的 JSON 字段合并为单个对象
 */
export function mergeConfigFields(config: Partial<ErpConfig>): Record<string, any> {
  const configObj: Record<string, any> = {}
  
  const fields = [
    'pageConfig',
    'formConfig',
    'tableConfig',
    'searchConfig',
    'actionConfig',
    'apiConfig',
    'dictConfig',
    'businessConfig',
    'detailConfig'
  ]
  
  for (const field of fields) {
    const value = config[field as keyof ErpConfig]
    if (value) {
      try {
        configObj[field] = typeof value === 'string' ? JSON.parse(value) : value
      } catch {
        configObj[field] = value
      }
    }
  }
  
  return configObj
}

/**
 * 将合并的 JSON 对象分散到各个字段
 */
export function splitConfigFields(mergedJson: string): Partial<ErpConfig> {
  try {
    const parsed = JSON.parse(mergedJson)
    
    return {
      pageConfig: parsed.pageConfig ? JSON.stringify(parsed.pageConfig) : '',
      formConfig: parsed.formConfig ? JSON.stringify(parsed.formConfig) : '',
      tableConfig: parsed.tableConfig ? JSON.stringify(parsed.tableConfig) : '',
      searchConfig: parsed.searchConfig ? JSON.stringify(parsed.searchConfig) : '',
      actionConfig: parsed.actionConfig ? JSON.stringify(parsed.actionConfig) : '',
      apiConfig: parsed.apiConfig ? JSON.stringify(parsed.apiConfig) : '',
      dictConfig: parsed.dictConfig ? JSON.stringify(parsed.dictConfig) : '',
      businessConfig: parsed.businessConfig ? JSON.stringify(parsed.businessConfig) : '',
      detailConfig: parsed.detailConfig ? JSON.stringify(parsed.detailConfig) : ''
    }
  } catch (error) {
    console.error('JSON 解析失败:', error)
    return {}
  }
}

/**
 * 格式化 JSON 字符串
 */
export function formatJsonString(jsonStr: string, space = 2): string {
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, space)
  } catch {
    return jsonStr
  }
}

/**
 * 验证 JSON 是否有效
 */
export function isValidJson(jsonStr: string): boolean {
  try {
    JSON.parse(jsonStr)
    return true
  } catch {
    return false
  }
}

/**
 * 获取配置字段的显示文本
 */
export function getConfigFieldDisplayText(config: ErpConfig): string {
  const merged = mergeConfigFields(config)
  const fieldCount = Object.keys(merged).length
  
  if (fieldCount === 0) {
    return '无配置内容'
  }
  
  const fields = Object.keys(merged)
  return `${fieldCount}个配置项：${fields.join(', ')}`
}
