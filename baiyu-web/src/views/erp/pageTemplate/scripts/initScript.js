/**
 * ERP 公共模板 - 初始化脚本
 * @module views/erp/pageTemplate/scripts/initScript
 * @description 负责页面初始化时的字典数据预加载、查询配置构建等
 */

import request from '@/utils/request'

/**
 * 预加载字典数据
 * @param {Object} dictionaryConfig - 字典配置对象
 * @param {String} moduleCode - 模块编码
 * @returns {Promise<Map>} - 加载完成的字典 Map
 */
export const preloadDictionaries = async (dictionaryConfig, moduleCode) => {
  const dictionaries = new Map()
  
  if (!dictionaryConfig) {
    return dictionaries
  }
  
  try {
    // 并行加载所有字典
    const promises = Object.entries(dictionaryConfig).map(async ([key, config]) => {
      // 跳过静态字典
      if (Array.isArray(config)) {
        dictionaries.set(key, config)
        return
      }
      
      // 跳过远程搜索的字典（如国家）
      if (key === 'nation') {
        dictionaries.set(key, [])
        return
      }
      
      if (config && config.api) {
        try {
          const api = config.api.replace(/{moduleCode}/g, moduleCode)
          const response = await request(api)
          
          let data = []
          if (response.code === 200 || response.errorCode === 0) {
            data = response.data || response.rows || []
          } else if (Array.isArray(response)) {
            data = response
          }
          
          const mappedData = data.map(item => ({
            label: item[config.labelField] || item.label || item.name,
            value: item[config.valueField] || item.value || item.kingdee,
            ...item
          }))
          
          dictionaries.set(key, mappedData)
          console.log(` 字典 "${key}" 预加载成功，共 ${mappedData.length} 条`)
        } catch (error) {
          console.warn(` 字典 "${key}" 预加载失败:`, error.message)
          dictionaries.set(key, [])
        }
      }
    })
    
    // 等待所有字典加载完成
    await Promise.all(promises)
    
  } catch (error) {
    console.warn(' 预加载字典失败:', error.message)
  }
  
  return dictionaries
}

/**
 * 构建查询条件（构建器模式）
 * @param {Array} searchFields - 搜索字段配置
 * @param {Object} queryParams - 查询参数
 * @param {Array} dateRange - 日期范围
 * @returns {Object} - queryConfig 配置对象
 */
export const buildQueryConfig = (searchFields, queryParams, dateRange) => {
  const conditions = []
  
  if (!searchFields || !Array.isArray(searchFields)) {
    return { conditions: [], orderBy: [] }
  }
  
  searchFields.forEach(field => {
    const value = queryParams[field.field]
    const operator = field.queryOperator || 'eq'
    
    // 跳过空值
    if (value === undefined || value === null || value === '') {
      return
    }
    
    // 日期范围特殊处理
    if (field.component === 'daterange' && Array.isArray(dateRange) && dateRange.length === 2) {
      conditions.push({
        field: field.field,
        operator: 'between',
        value: dateRange
      })
    } else if (Array.isArray(value)) {
      // IN 条件
      conditions.push({
        field: field.field,
        operator: 'in',
        value: value
      })
    } else {
      // 单个值条件
      conditions.push({
        field: field.field,
        operator: operator,
        value: value
      })
    }
  })
  
  return {
    conditions: conditions,
    orderBy: [] // 默认排序在别处配置
  }
}

/**
 * 获取表名
 * @param {Object} pageConfig - 页面配置
 * @param {String} defaultTableName - 默认表名
 * @returns {String} - 表名
 */
export const getTableName = (pageConfig, defaultTableName = 't_sale_order') => {
  return pageConfig?.tableName || defaultTableName
}

/**
 * 初始化销售人员字典
 * @param {String} moduleCode - 模块编码
 * @returns {Promise<Array>} - 销售人员列表
 */
export const loadSalespersons = async (moduleCode) => {
  try {
    const response = await request({
      url: '/erp/engine/dictionary/salespersons',
      method: 'get',
      params: { moduleCode }
    })
    
    if (response.code === 200 || response.errorCode === 0) {
      const data = response.data || response.rows || []
      return data.map(item => ({
        label: item.nickName || item.name,
        value: item.fseller || id,
        ...item
      }))
    }
    
    return []
  } catch (error) {
    console.warn(' 加载销售人员失败:', error.message)
    return []
  }
}

/**
 * 初始化国家字典（支持搜索）
 * @param {String} keyword - 搜索关键词
 * @param {String} moduleCode - 模块编码
 * @returns {Promise<Array>} - 国家列表
 */
export const searchNations = async (keyword, moduleCode) => {
  if (!keyword || keyword.trim() === '') {
    return []
  }
  
  try {
    const response = await request({
      url: '/erp/engine/dictionary/listByType/nation',
      method: 'get',
      params: { 
        type: 'nation',
        moduleCode,
        keyword: encodeURIComponent(keyword)
      }
    })
    
    if (response.code === 200 || response.errorCode === 0) {
      const data = response.data || response.rows || []
      return data.map(item => ({
        label: item.name || item.label,
        value: item.kingdee || item.value,
        ...item
      }))
    }
    
    return []
  } catch (error) {
    console.warn(' 搜索国家失败:', error.message)
    return []
  }
}

/**
 * 验证查询配置是否完整
 * @param {Object} queryConfig - 查询配置
 * @returns {Boolean} - 是否有效
 */
export const validateQueryConfig = (queryConfig) => {
  if (!queryConfig) {
    return false
  }
  
  if (!queryConfig.conditions || !Array.isArray(queryConfig.conditions)) {
    return false
  }
  
  // 检查每个条件的必需字段
  for (const condition of queryConfig.conditions) {
    if (!condition.field || !condition.operator) {
      return false
    }
    
    // isNull 和 isNotNull 不需要 value
    if (!['isNull', 'isNotNull'].includes(condition.operator)) {
      if (condition.value === undefined || condition.value === null) {
        return false
      }
    }
  }
  
  return true
}

/**
 * 获取支持的运算符列表
 * @returns {Promise<Array>} - 运算符列表
 */
export const getSupportedOperators = async () => {
  try {
    const response = await request({
      url: '/erp/engine/query/operators',
      method: 'get'
    })
    
    if (response.code === 200 || response.errorCode === 0) {
      return response.data || []
    }
    
    return ['eq', 'ne', 'gt', 'ge', 'lt', 'le', 'like', 'left_like', 'right_like', 'in', 'between', 'isNull', 'isNotNull']
  } catch (error) {
    console.warn(' 获取运算符列表失败:', error.message)
    return ['eq', 'ne', 'gt', 'ge', 'lt', 'le', 'like', 'left_like', 'right_like', 'in', 'between', 'isNull', 'isNotNull']
  }
}

export default {
  preloadDictionaries,
  buildQueryConfig,
  getTableName,
  loadSalespersons,
  searchNations,
  validateQueryConfig,
  getSupportedOperators
}
