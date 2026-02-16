import request from '../utils/request'

export function getWorkOrderList(params: any) {
  return request({
    url: '/asm/work-order/list',
    method: 'get',
    params
  })
}

export function createWorkOrder(data: any) {
  return request({
    url: '/asm/work-order',
    method: 'post',
    data
  })
}

export function updateWorkOrder(data: any) {
  return request({
    url: '/asm/work-order',
    method: 'put',
    data
  })
}

export function deleteWorkOrder(id: number) {
  return request({
    url: `/asm/work-order/${id}`,
    method: 'delete'
  })
}

export function getWorkOrderById(id: number) {
  return request({
    url: `/asm/work-order/${id}`,
    method: 'get'
  })
}

export function createRepairRecord(data: any) {
  return request({
    url: '/asm/repair-record',
    method: 'post',
    data
  })
}

export function updateRepairRecord(data: any) {
  return request({
    url: '/asm/repair-record',
    method: 'put',
    data
  })
}

export function deleteRepairRecord(id: number) {
  return request({
    url: `/asm/repair-record/${id}`,
    method: 'delete'
  })
}
