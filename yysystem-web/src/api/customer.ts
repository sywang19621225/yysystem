import request from '@/utils/request'

export interface Customer {
  id?: number
  customerCode?: string
  customerName: string
  customerType: string
  linkman?: string
  phone?: string
  status: number
  createTime?: string
}

export const getCustomerList = (params: any) => {
  return request({
    url: '/crm/customer/list',
    method: 'get',
    params
  })
}

export const saveCustomer = (data: Customer) => {
  return request({
    url: '/crm/customer',
    method: 'post',
    data
  })
}

export const updateCustomer = (id: number, data: Customer) => {
  return request({
    url: `/crm/customer/${id}`,
    method: 'put',
    data
  })
}

export const deleteCustomer = (id: number) => {
  return request({
    url: `/crm/customer/${id}`,
    method: 'delete'
  })
}
