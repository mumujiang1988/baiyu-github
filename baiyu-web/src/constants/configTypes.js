/**
 * ERP 配置类型常量定义
 * @module constants/configTypes
 * @description 统一管理配置类型相关的常量和辅助函数
 */

/**
 * 配置类型定义
 * @type {Object.<string, {label: string, tag: string, value: string}>}
 */
export const CONFIG_TYPES = {
  PAGE: { label: '页面配置', tag: 'success', value: 'PAGE' },
  DICT: { label: '字典配置', tag: 'primary', value: 'DICT' },
  PUSH: { label: '下推配置', tag: 'warning', value: 'PUSH' },
  APPROVAL: { label: '审批配置', tag: 'danger', value: 'APPROVAL' },
  NUMBER_RULE: { label: '编号规则', tag: 'info', value: 'NUMBER_RULE' },
  PRINT: { label: '打印模板', tag: 'success', value: 'PRINT' }
}

/**
 * 获取配置类型标签文本
 * @param {string} type - 配置类型值
 * @returns {string} 配置类型标签文本
 * @example
 * getConfigTypeLabel('PAGE') // '页面配置'
 * getConfigTypeLabel('UNKNOWN') // 'UNKNOWN'
 */
export const getConfigTypeLabel = (type) => {
  return CONFIG_TYPES[type]?.label || type
}

/**
 * 获取配置类型标签颜色
 * @param {string} type - 配置类型值
 * @returns {string} Element Plus Tag 组件的 type 属性值
 * @example
 * getConfigTypeTag('PAGE') // 'success'
 * getConfigTypeTag('UNKNOWN') // 'info'
 */
export const getConfigTypeTag = (type) => {
  return CONFIG_TYPES[type]?.tag || 'info'
}

/**
 * 获取配置类型选项列表
 * @returns {Array<{label: string, value: string}>} 选项列表，用于 el-select 组件
 * @example
 * // 在 el-select 中使用
 * <el-select v-model="form.configType">
 *   <el-option
 *     v-for="option in getConfigTypeOptions()"
 *     :key="option.value"
 *     :label="option.label"
 *     :value="option.value"
 *   />
 * </el-select>
 */
export const getConfigTypeOptions = () => {
  return Object.values(CONFIG_TYPES).map(item => ({
    label: item.label,
    value: item.value
  }))
}

/**
 * 判断是否为有效的配置类型
 * @param {string} type - 配置类型值
 * @returns {boolean} 是否有效
 * @example
 * isValidConfigType('PAGE') // true
 * isValidConfigType('INVALID') // false
 */
export const isValidConfigType = (type) => {
  return Object.prototype.hasOwnProperty.call(CONFIG_TYPES, type)
}

/**
 * 获取所有配置类型值列表
 * @returns {string[]} 配置类型值数组
 * @example
 * getAllConfigTypeValues() // ['PAGE', 'DICT', 'PUSH', 'APPROVAL', 'NUMBER_RULE', 'PRINT']
 */
export const getAllConfigTypeValues = () => {
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
