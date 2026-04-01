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
      
      // 强制使用 relationConfig 关联逻辑（已移除旧兼容模式）
      let conditions = [...defaultConditions]
      
      if (!relationConfig) {
        throw new Error(`子表 ${tableName} 必须配置 relationConfig`)
      }
      
      const masterFieldValue = contextData[relationConfig.masterField]
      
      // 替换模板变量为实际的关联字段值
      conditions = conditions.map(cond => {
        if (cond.field === relationConfig.detailField && cond.value && cond.value.startsWith('${') && cond.value.endsWith('}')) {
          return {
            ...cond,
            value: masterFieldValue
          }
        }
        return cond
      })
      
      const queryConfig = {
        conditions,
        orderBy: defaultOrderBy || []
      }
      
      const result = await querySubTable({
        moduleCode,
        tableName,
        queryConfig,
        pageNum: 1,
        pageSize: 100
      })
      
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
    console.error('[queryAllSubTables] 批量查询失败:', error)
    throw error
  }
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
        
        // Parse relation config if exists (enabled field is deprecated)
        if (tab.relationConfig) {
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
  parseSubTableConfigs,
  getMainTableQueryConfig,
  queryEntryByBillNo,
  queryCostByBillNo
}
