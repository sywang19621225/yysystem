import request from '../utils/request'

export function getInboundList(params: any) {
  return request({
    url: '/scm/inbound/list',
    method: 'get',
    params
  })
}

export function createInboundFromPurchase(purchaseId: number) {
  return request({
    url: `/scm/inbound/create-from-purchase/${purchaseId}`,
    method: 'post'
  })
}

export function createInboundFromPurchaseRequest(requestId: number) {
  return request({
    url: `/scm/inbound/create-from-purchase-request/${requestId}`,
    method: 'post'
  })
}

export function createTransferFromPurchase(requestId: number) {
  return request({
    url: `/scm/inbound/create-transfer-from-purchase/${requestId}`,
    method: 'post'
  })
}

export function createInboundFromSalesReturn(outboundId: number) {
  return request({
    url: `/scm/inbound/create-from-sales-return/${outboundId}`,
    method: 'post'
  })
}

export function createTransferInbound(data: any) {
  return request({
    url: '/scm/inbound/create-transfer',
    method: 'post',
    data
  })
}

export function auditInbound(id: number, status: string, detail?: string) {
  return request({
    url: `/scm/inbound/${id}/audit`,
    method: 'put',
    params: {
      status,
      detail
    }
  })
}

export function getInboundById(id: number) {
  return request({
    url: `/scm/inbound/${id}`,
    method: 'get'
  })
}

export function createInbound(data: any) {
  return request({
    url: '/scm/inbound',
    method: 'post',
    data
  })
}

export function updateInbound(id: number, data: any) {
  return request({
    url: `/scm/inbound/${id}`,
    method: 'put',
    data
  })
}

export function getInboundDetails(id: number) {
  return request({
    url: `/scm/inbound/${id}/details`,
    method: 'get'
  })
}

export function addInboundDetail(data: any) {
  return request({
    url: '/scm/inbound/detail',
    method: 'post',
    data
  })
}

export function updateInboundDetail(id: number, data: any) {
  return request({
    url: `/scm/inbound/detail/${id}`,
    method: 'put',
    data
  })
}

export function deleteInboundDetail(id: number) {
  return request({
    url: `/scm/inbound/detail/${id}`,
    method: 'delete'
  })
}

export function deleteInbound(id: number) {
  return request({
    url: `/scm/inbound/${id}`,
    method: 'delete'
  })
}
