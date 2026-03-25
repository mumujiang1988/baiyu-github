import request from '@/utils/request'

export function billHeadsList(queryParams) {
  return request({
    url: '/k3/billhead/list',
    method: 'get',
    params: queryParams
  })
}
