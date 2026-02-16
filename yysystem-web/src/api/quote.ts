import request from '@/utils/request'

export interface QuoteDetail {
  id?: number
  quoteId?: number
  productName: string
  productCode: string
  salesPrice: number
  salesQuantity: number
  productSpec?: string
  productUnit?: string
}

export interface Quote {
  id?: number
  quoteNo?: string
  customerId: number
  quoteAmount?: number
  costTotalAmount?: number
  channelTotalAmount?: number
  terminalTotalAmount?: number
  quoteDate: string
  status?: string
  detailList: QuoteDetail[]
}

export const getQuoteList = (params: any) => {
  return request({
    url: '/crm/quote/list',
    method: 'get',
    params
  })
}

export const getQuoteById = (id: number) => {
  return request({
    url: `/crm/quote/${id}`,
    method: 'get'
  })
}

export const getQuoteDetails = (id: number, params: any) => {
  return request({
    url: `/crm/quote/${id}/details`,
    method: 'get',
    params
  })
}

export const saveQuote = (data: Quote) => {
  return request({
    url: '/crm/quote',
    method: 'post',
    data
  })
}

export const updateQuote = (id: number, data: Quote) => {
  return request({
    url: `/crm/quote/${id}`,
    method: 'put',
    data
  })
}

export const deleteQuote = (id: number) => {
  return request({
    url: `/crm/quote/${id}`,
    method: 'delete'
  })
}
