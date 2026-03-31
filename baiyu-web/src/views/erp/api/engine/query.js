import request from '@/utils/request'

/**
 * Dynamic Query Engine API
 */

/**
 * Execute dynamic query
 * @param {Object} data - Query parameters
 * @param {string} data.moduleCode - Module code
 * @param {Object} data.queryParams - Query conditions
 * @param {Object} data.searchConfig - Search configuration
 * @returns {Promise}
 */
export function executeDynamicQuery(data) {
  return request({
    url: '/erp/engine/query/execute',
    method: 'post',
    data: data
  })
}

/**
 * Build query conditions
 * @param {Object} data - Condition parameters
 * @param {Object} data.searchConfig - Search configuration
 * @param {Object} data.queryParams - Query parameters
 * @returns {Promise}
 */
export function buildQueryConditions(data) {
  return request({
    url: '/erp/engine/query/build',
    method: 'post',
    data: data
  })
}

/**
 * Get available query types
 * @returns {Promise}
 */
export function getAvailableQueryTypes() {
  return request({
    url: '/erp/engine/query/types',
    method: 'get'
  })
}
