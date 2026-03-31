/**
 * ERP Page Template - Initialization Script
 * @module views/erp/pageTemplate/scripts/initScript
 * @description Responsible for dictionary data pre-loading and query config building during page initialization
 */

import request from '@/utils/request'

/**
 * Preload dictionary data
 * @param {Object} dictionaryConfig - Dictionary config object
 * @param {String} moduleCode - Module code
 * @returns {Promise<Map>} - Loaded dictionary Map
 */
export const preloadDictionaries = async (dictionaryConfig, moduleCode) => {
  const dictionaries = new Map()
  
  if (!dictionaryConfig) {
    return dictionaries
  }
  
  try {
    const promises = Object.entries(dictionaryConfig).map(async ([key, config]) => {
      if (Array.isArray(config)) {
        dictionaries.set(key, config)
        return
      }
      
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
        } catch (error) {
          dictionaries.set(key, [])
        }
      }
    })
    
    await Promise.all(promises)
    
  } catch (error) {
  }
  
  return dictionaries
}

/**
 * Build query conditions (Builder Pattern)
 * @param {Array} searchFields - Search field config
 * @param {Object} queryParams - Query params
 * @param {Array} dateRange - Date range
 * @returns {Object} - queryConfig object
 */
export const buildQueryConfig = (searchFields, queryParams, dateRange) => {
  const conditions = []
  
  if (!searchFields || !Array.isArray(searchFields)) {
    return { conditions: [], orderBy: [] }
  }
  
  searchFields.forEach(field => {
    let value = queryParams[field.field]
    const operator = field.queryOperator || 'eq'
    
    if (field.component === 'daterange') {
      if (Array.isArray(dateRange) && dateRange.length === 2) {
        value = dateRange
      } else {
        return
      }
    }
    
    if (value === undefined || value === null || value === '') {
      return
    }
    
    if (field.component === 'daterange') {
      conditions.push({
        field: field.field,
        operator: operator, // between
        value: value
      })
    } else if (Array.isArray(value)) {
      conditions.push({
        field: field.field,
        operator: operator,
        value: value
      })
    } else {
      conditions.push({
        field: field.field,
        operator: operator,
        value: value
      })
    }
  })
  
  return {
    conditions: conditions,
    orderBy: []
  }
}

/**
 * Get table name
 * @param {Object} pageConfig - Page config
 * @param {String} defaultTableName - Default table name
 * @returns {String} - Table name
 */
export const getTableName = (pageConfig, defaultTableName = 't_sale_order') => {
  return pageConfig?.tableName || defaultTableName
}

/**
 * Initialize salespersons dictionary
 * @param {String} moduleCode - Module code
 * @returns {Promise<Array>} - Salespersons list
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
    return []
  }
}

/**
 * Initialize country dictionary (with search support)
 * @param {String} keyword - Search keyword
 * @param {String} moduleCode - Module code
 * @returns {Promise<Array>} - Country list
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
    return []
  }
}

/**
 * Validate query config completeness
 * @param {Object} queryConfig - Query config
 * @returns {Boolean} - Is valid
 */
export const validateQueryConfig = (queryConfig) => {
  if (!queryConfig) {
    return false
  }
  
  if (!queryConfig.conditions || !Array.isArray(queryConfig.conditions)) {
    return false
  }
  
  for (const condition of queryConfig.conditions) {
    if (!condition.field || !condition.operator) {
      return false
    }
    
    if (!['isNull', 'isNotNull'].includes(condition.operator)) {
      if (condition.value === undefined || condition.value === null) {
        return false
      }
    }
  }
  
  return true
}

/**
 * Get supported operators list
 * @returns {Promise<Array>} - Operators list
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
