import request from '@/utils/request'

/**
 * 根据字典类型查询字典项列表
 * @param {string} dictType 字典类型 (如：currency, payment_clause, nation 等)
 * @returns {Promise} 返回字典项列表
 */
export function listByType(dictType) {
  return request({
    url: `/k3/dictionary/listByType/${dictType}`,
    method: 'get'
  })
}

/**
 * 批量查询多个字典类型
 * @param {Array<string>} dictTypes 字典类型数组
 * @returns {Promise} 返回对象，key 为字典类型，value 为字典数据
 */
export async function batchListByType(dictTypes) {
  const results = {}
  const promises = dictTypes.map(async (type) => {
    try {
      const response = await listByType(type)
      results[type] = response.data || response
    } catch (error) {
      console.error(`加载字典 ${type} 失败:`, error)
      results[type] = []
    }
  })
  await Promise.all(promises)
  return results
}
