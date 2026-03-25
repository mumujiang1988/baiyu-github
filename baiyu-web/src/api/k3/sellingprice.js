import request from '@/utils/request'

/**
 * 获取销售价目表分页列表
 * @param {Object} queryParams 查询参数
 * @returns {Promise}
 */
export function salesPriceList(queryParams) {
  return request({
    url: '/k3/salesPrice/list',
    method: 'get',
    params: queryParams
  })
}

