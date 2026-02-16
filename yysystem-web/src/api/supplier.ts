import request from '@/utils/request'

export function getSupplierList(params: any) {
  return request({
    url: '/scm/supplier/list',
    method: 'get',
    params
  })
}

export function getSupplierById(id: number) {
  return request({
    url: `/scm/supplier/${id}`,
    method: 'get'
  })
}

export function createSupplier(data: any) {
  return request({
    url: '/scm/supplier',
    method: 'post',
    data
  })
}

export function updateSupplier(data: any) {
  return request({
    url: '/scm/supplier',
    method: 'put',
    data
  })
}

export function deleteSupplier(id: number) {
  return request({
    url: `/scm/supplier/${id}`,
    method: 'delete'
  })
}
