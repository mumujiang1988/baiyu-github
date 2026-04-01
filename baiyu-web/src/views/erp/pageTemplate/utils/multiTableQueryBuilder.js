/**
 * Multi-Table Query Builder - supports independent queries for multiple tables on a single page
 * @module views/erp/pageTemplate/utils/multiTableQueryBuilder
 * @description Generate independent queryConfig for each sub-table, supporting different table names and query conditions
 */

import request from '@/utils/request'

/**
 * Main table query (standard builder pattern)
 * @param {Object} params - Query params
 * @returns {Promise} - Query result
 */
export const queryMainTable = async (params) => {
  const { moduleCode, tableName, queryConfig, pageNum = 1, pageSize = 10 } = params
  
  try {
    const response = await request({
      url: '/erp/engine/query/execute',
      method: 'post',
      data: {
        moduleCode,
        tableName,
        queryConfig,
        pageNum,
        pageSize
      }
    })
    
    // Backend returns R.ok(result), so data is in response.data
    return response.data || {}
  } catch (error) {
    throw error
  }
}

/**
 * Sub-table query (generic method)
 * @param {Object} params - Query params
 * @returns {Promise} - Query result
 */
export const querySubTable = async (params) => {
  const { moduleCode, tableName, queryConfig, pageNum = 1, pageSize = 100 } = params
  
  try {
    const response = await request({
      url: '/erp/engine/query/execute',
      method: 'post',
      data: {
        moduleCode,
        tableName,
        queryConfig,
        pageNum,
        pageSize
      }
    })
    
    // Backend returns R.ok(result), so data is in response.data
    return response.data || {}
  } catch (error) {
    throw error
  }
}

/**
 * Query multiple sub-tables in parallel
 * @param {String} moduleCode - Module code
 * @param {Array} subTableConfigs - Sub-table config array
 * @param {Object} contextData - Context data (e.g., billNo)
 * @returns {Promise<Object>} - All sub-table query results
 */
export const queryAllSubTables = async (moduleCode, subTableConfigs, contextData = {}) => {
  try {
    // Build query requests for all sub-tables
    const promises = subTableConfigs.map(async (config) => {
      const { key, tableName, defaultConditions, defaultOrderBy, relationConfig } = config
      
      // If relation config exists, extract master field value from contextData
      let conditions = [...defaultConditions]
      if (relationConfig && relationConfig.enabled) {
        const masterFieldValue = contextData[relationConfig.masterField]
        console.error(`[queryAllSubTables] ${key}: Using relation config, ${relationConfig.masterTable}.${relationConfig.masterField} =`, masterFieldValue)
        
        // Replace template variables with actual master field value
        conditions = conditions.map(cond => {
          if (cond.field === relationConfig.detailField && cond.value && cond.value.startsWith('${') && cond.value.endsWith('}')) {
            return {
              ...cond,
              value: masterFieldValue
            }
          }
          return cond
        })
      } else {
        // Old method: replace template variables
        conditions = replaceTemplateVariables(defaultConditions, contextData)
      }
      
      const queryConfig = {
        conditions,
        orderBy: defaultOrderBy || []
      }
      
      const result = await querySubTable({
        moduleCode,
        tableName,
        queryConfig,
        pageNum: 1,
        pageSize: 100 // Sub-tables usually don't need pagination
      })
      
      // result is already response.data, containing rows and total
      return {
        key,
        data: result.rows || [],
        total: result.total || 0
      }
    })
    
    // Wait for all queries to complete
    const results = await Promise.all(promises)
    
    // Convert to object format
    const resultMap = {}
    results.forEach(result => {
      resultMap[result.key] = {
        data: result.data,
        total: result.total
      }
    })
    
    return resultMap
  } catch (error) {
    throw error
  }
}

/**
 * Replace template variables in query conditions
 * @param {Array} conditions - Query condition array
 * @param {Object} contextData - Context data
 * @returns {Array} - Replaced condition array
 */
export const replaceTemplateVariables = (conditions, contextData) => {
  if (!Array.isArray(conditions)) {
    return conditions
  }
  
  return conditions.map(condition => {
    let value = condition.value
    
    // Handle array type values
    if (Array.isArray(value)) {
      value = value.map(v => {
        if (typeof v === 'string' && v.startsWith('${') && v.endsWith('}')) {
          const varName = v.slice(2, -1)
          return contextData[varName] || v
        }
        return v
      })
    } 
    // Handle string type values
    else if (typeof value === 'string' && value.startsWith('${') && value.endsWith('}')) {
      const varName = value.slice(2, -1)
      value = contextData[varName] || value
    }
    
    return {
      ...condition,
      value
    }
  })
}

/**
 * Parse sub-table query configs from page config
 * @param {Object} pageConfig - Page config object
 * @returns {Array} - Sub-table config array
 */
export const parseSubTableConfigs = (pageConfig) => {
  const configs = []
  
  // Priority: parse from detailConfig (new version)
  if (pageConfig && pageConfig.detailConfig && pageConfig.detailConfig.detail) {
    const tabs = pageConfig.detailConfig.detail.tabs || []
    
    for (const tab of tabs) {
      // Support table, descriptions and form types (cost table)
      if (tab.type === 'table' || tab.type === 'descriptions' || tab.type === 'form') {
        const config = {
          key: tab.name,
          tableName: tab.tableName,
          defaultConditions: tab.queryConfig?.defaultConditions || [],
          defaultOrderBy: tab.queryConfig?.defaultOrderBy || [],
          dataField: tab.dataField || `${tab.name}Data`,
          type: tab.type
        }
        
        // Parse relation config if exists
        if (tab.relationConfig && tab.relationConfig.enabled) {
          config.relationConfig = {
            masterTable: tab.relationConfig.masterTable,
            masterField: tab.relationConfig.masterField,
            detailTable: tab.relationConfig.detailTable,
            detailField: tab.relationConfig.detailField,
            operator: tab.relationConfig.operator || 'eq'
          }
        }
        
        configs.push(config)
      }
    }
  }
  // Compatible with old subTableQueryConfigs (old version)
  else if (pageConfig && pageConfig.subTableQueryConfigs) {
    const subTableConfigs = pageConfig.subTableQueryConfigs
    
    // Iterate through all sub-table configs
    for (const [key, config] of Object.entries(subTableConfigs)) {
      if (config.enabled) {
        configs.push({
          key,
          tableName: config.tableName,
          defaultConditions: config.defaultConditions || [],
          defaultOrderBy: config.defaultOrderBy || []
        })
      }
    }
  }
  
  return configs
}

/**
 * Get main table query config
 * @param {Object} pageConfig - Page config object
 * @returns {Object|null} - Main table query config
 */
export const getMainTableQueryConfig = (pageConfig) => {
  if (!pageConfig || !pageConfig.queryConfig || !pageConfig.queryConfig.enabled) {
    return null
  }
  
  return {
    tableName: pageConfig.pageConfig?.tableName,
    defaultConditions: pageConfig.queryConfig.defaultConditions || [],
    defaultOrderBy: pageConfig.queryConfig.defaultOrderBy || []
  }
}

/**
 * Query entry details by bill number (specialized method)
 * @param {String} moduleCode - Module code
 * @param {String} billNo - Bill number
 * @returns {Promise<Array>} - Entry details
 */
export const queryEntryByBillNo = async (moduleCode, billNo) => {
  const queryConfig = {
    conditions: [
      {
        field: 'FBillNo',
        operator: 'eq',
        value: billNo
      }
    ],
    orderBy: [
      {
        field: 'FPlanMaterialId',
        direction: 'ASC'
      }
    ]
  }
  
  const response = await querySubTable({
    moduleCode,
    tableName: 't_sale_order_entry',
    queryConfig,
    pageNum: 1,
    pageSize: 100
  })
  
  // response already contains response.data with rows and total
  return response.rows || []
}

/**
 * Query cost data by bill number (specialized method)
 * @param {String} moduleCode - Module code
 * @param {String} billNo - Bill number
 * @returns {Promise<Object>} - Cost data
 */
export const queryCostByBillNo = async (moduleCode, billNo) => {
  const queryConfig = {
    conditions: [
      {
        field: 'FBillNo',
        operator: 'eq',
        value: billNo
      }
    ],
    orderBy: [
      {
        field: 'FID',
        direction: 'ASC'
      }
    ]
  }
  
  const response = await querySubTable({
    moduleCode,
    tableName: 't_sale_order_cost',
    queryConfig,
    pageNum: 1,
    pageSize: 1 // Cost table usually has only one record
  })
  
  // response already contains response.data with rows and total
  return response.rows?.[0] || {}
}

export default {
  queryMainTable,
  querySubTable,
  queryAllSubTables,
  replaceTemplateVariables,
  parseSubTableConfigs,
  getMainTableQueryConfig,
  queryEntryByBillNo,
  queryCostByBillNo
}
