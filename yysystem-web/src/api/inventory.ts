import request from '@/utils/request'

export function getCheckList(params: any) {
  return request({
    url: '/scm/inventory/check/list',
    method: 'get',
    params
  })
}

export function getCheckById(id: number) {
  return request({
    url: `/scm/inventory/check/${id}`,
    method: 'get'
  })
}

export function createCheck(data: any) {
  return request({
    url: '/scm/inventory/check',
    method: 'post',
    data
  })
}

export function updateCheck(data: any) {
  return request({
    url: '/scm/inventory/check',
    method: 'put',
    data
  })
}

export function deleteCheck(id: number) {
  return request({
    url: `/scm/inventory/check/${id}`,
    method: 'delete'
  })
}

export function getStockFlowList(params: any) {
  return request({
    url: '/scm/inventory/flow/list',
    method: 'get',
    params
  })
}
