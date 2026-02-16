import request from '@/utils/request'

export function getWarehouseList(params: any) {
  return request({
    url: '/pdm/warehouse/list',
    method: 'get',
    params
  })
}

export function getWarehouseById(id: number) {
  return request({
    url: `/pdm/warehouse/${id}`,
    method: 'get'
  })
}

export function createWarehouse(data: any) {
  return request({
    url: '/pdm/warehouse',
    method: 'post',
    data
  })
}

export function updateWarehouse(data: any) {
  return request({
    url: '/pdm/warehouse',
    method: 'put',
    data
  })
}

export function deleteWarehouse(id: number) {
  return request({
    url: `/pdm/warehouse/${id}`,
    method: 'delete'
  })
}
