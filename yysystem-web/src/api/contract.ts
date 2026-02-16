import request from '@/utils/request'

export interface Contract {
  id?: number
  contractNo?: string
  customerId: number
  contractAmount?: number
  arrears?: number
  quoteDate?: string
  auditStatus?: string
  detailList: any[]
}

export const getContractList = (params: any) => {
  return request({
    url: '/crm/contract/list',
    method: 'get',
    params
  })
}

export const getContractListByCustomer = (customerId: number) => {
  return request({
    url: `/crm/contract/by-customer/${customerId}`,
    method: 'get'
  })
}

export const getContractById = (id: number) => {
  return request({
    url: `/crm/contract/${id}`,
    method: 'get'
  })
}

export const saveContract = (data: Contract) => {
  return request({
    url: '/crm/contract',
    method: 'post',
    data
  })
}

export const updateContract = (id: number, data: Contract) => {
  return request({
    url: `/crm/contract/${id}`,
    method: 'put',
    data
  })
}

export const deleteContract = (id: number) => {
  return request({
    url: `/crm/contract/${id}`,
    method: 'delete'
  })
}

export const createContractFromQuote = (quoteId: number) => {
  return request({
    url: `/crm/contract/create-from-quote/${quoteId}`,
    method: 'post'
  })
}

export const getContractSummary = () => {
  return request({
    url: '/crm/contract/summary',
    method: 'get'
  })
}

export const getContractFlags = (ids: number[]) => {
  return request({
    url: '/crm/contract/flags',
    method: 'get',
    params: { ids: ids.join(',') }
  })
}

export const recalculateContracts = () => {
  return request({
    url: '/crm/contract/recalculate',
    method: 'post'
  })
}

export const unlockContract = (id: number) => {
  return request({
    url: `/crm/contract/${id}/unlock`,
    method: 'put'
  })
}
