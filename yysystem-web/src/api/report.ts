import request from '../utils/request'

export function getSalesReport(params: any) {
  return request({
    url: '/report/sales',
    method: 'get',
    params
  })
}

export function getInventoryReport() {
  return request({
    url: '/report/inventory',
    method: 'get'
  })
}
