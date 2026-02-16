import request from '@/utils/request'

export const getInvoiceList = (params: any) => {
  return request({
    url: '/crm/invoice/list',
    method: 'get',
    params
  })
}

export const getInvoiceById = (id: number) => {
  return request({
    url: `/crm/invoice/${id}`,
    method: 'get'
  })
}

export const saveInvoice = (data: any) => {
  return request({
    url: '/crm/invoice',
    method: 'post',
    data
  })
}

export const updateInvoice = (id: number, data: any) => {
  return request({
    url: `/crm/invoice/${id}`,
    method: 'put',
    data
  })
}

export const deleteInvoice = (id: number) => {
  return request({
    url: `/crm/invoice/${id}`,
    method: 'delete'
  })
}

export const getInvoiceDetails = (id: number, params: any) => {
  return request({
    url: `/crm/invoice/${id}/details`,
    method: 'get',
    params
  })
}
