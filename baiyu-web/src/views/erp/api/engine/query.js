import request from '@/utils/request'

/**
 * 动态查询引擎 API
 */

/**
 * 执行动态查询
 * @param {Object} data - 查询参数
 * @param {string} data.moduleCode - 模块编码
 * @param {Object} data.queryParams - 查询条件
 * @param {Object} data.searchConfig - 搜索配置
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
 * 构建查询条件
 * @param {Object} data - 条件参数
 * @param {Object} data.searchConfig - 搜索配置
 * @param {Object} data.queryParams - 查询参数
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
 * 获取可用的查询类型
 * @returns {Promise}
 */
export function getAvailableQueryTypes() {
  return request({
    url: '/erp/engine/query/types',
    method: 'get'
  })
}
