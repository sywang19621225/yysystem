import request from '@/utils/request'

export const saveFinance = (data: any) => {
  return request({
    url: '/crm/finance',
    method: 'post',
    data
  })
}

export const auditFinance = (id: number, status: string, detail?: string) => {
  return request({
    url: `/crm/finance/${id}/audit`,
    method: 'put',
    params: { status, detail }
  })
}

export const getFinanceList = (params: any) => {
  return request({
    url: '/crm/finance/list',
    method: 'get',
    params
  })
}

export const deleteFinance = (id: number) => {
  return request({
    url: `/crm/finance/${id}`,
    method: 'delete'
  })
}

export const updateFinance = (id: number, data: any) => {
  return request({
    url: `/crm/finance/${id}`,
    method: 'put',
    data
  })
}

export const getFinanceById = (id: number) => {
  return request({
    url: `/crm/finance/${id}`,
    method: 'get'
  })
}
