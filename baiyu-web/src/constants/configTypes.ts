/**
 * ERP 配置类型常量定义
 * @module constants/configTypes
 * @description 统一管理配置类型相关的常量和辅助函数
 */

/**
 * 配置类型定义
 */
export interface ConfigTypeItem {
  label: string
  tag: string
  value: string
}

export interface ConfigTypeMap {
  [key: string]: ConfigTypeItem
}

/**
 * 配置类型枚举值
 */
export const CONFIG_TYPES: ConfigTypeMap = {
  PAGE: { label: '页面配置', tag: 'success', value: 'PAGE' },
  DICT: { label: '字典配置', tag: 'primary', value: 'DICT' },
  PUSH: { label: '下推配置', tag: 'warning', value: 'PUSH' },
  APPROVAL: { label: '审批配置', tag: 'danger', value: 'APPROVAL' },
  NUMBER_RULE: { label: '编号规则', tag: 'info', value: 'NUMBER_RULE' },
  PRINT: { label: '打印模板', tag: 'success', value: 'PRINT' }
} as const

/**
 * 获取配置类型标签文本
 * @param type - 配置类型值
 * @returns 配置类型标签文本
 */
export const getConfigTypeLabel = (type: string): string => {
  return CONFIG_TYPES[type]?.label || type
}

/**
 * 获取配置类型标签颜色
 * @param type - 配置类型值
 * @returns Element Plus Tag 组件的 type 属性值
 */
export const getConfigTypeTag = (type: string): string => {
  return CONFIG_TYPES[type]?.tag || 'info'
}

/**
 * 获取配置类型选项列表
 * @returns 选项列表，用于 el-select 组件
 */
export const getConfigTypeOptions = (): Array<{ label: string; value: string }> => {
  return Object.values(CONFIG_TYPES).map(item => ({
    label: item.label,
    value: item.value
  }))
}

/**
 * 判断是否为有效的配置类型
 * @param type - 配置类型值
 * @returns 是否有效
 */
export const isValidConfigType = (type: string): boolean => {
  return Object.prototype.hasOwnProperty.call(CONFIG_TYPES, type)
}

/**
 * 获取所有配置类型值列表
 * @returns 配置类型值数组
 */
export const getAllConfigTypeValues = (): string[] => {
  return Object.keys(CONFIG_TYPES)
}

export default {
  CONFIG_TYPES,
  getConfigTypeLabel,
  getConfigTypeTag,
  getConfigTypeOptions,
  isValidConfigType,
  getAllConfigTypeValues
}
