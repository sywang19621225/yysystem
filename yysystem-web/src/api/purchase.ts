import request from '../utils/request'

export function getPurchaseList(params: any) {
  return request({
    url: '/scm/purchase/list',
    method: 'get',
    params
  })
}

export function createPurchase(data: any) {
  return request({
    url: '/scm/purchase',
    method: 'post',
    data
  })
}

export function updatePurchase(data: any) {
  return request({
    url: `/scm/purchase/${data.id}`,
    method: 'put',
    data
  })
}

export function deletePurchase(id: number) {
  return request({
    url: `/scm/purchase/${id}`,
    method: 'delete'
  })
}

export function auditPurchase(id: number, status: string, detail?: string) {
  return request({
    url: `/scm/purchase/${id}/audit`,
    method: 'put',
    params: { status, detail }
  })
}

export function getPurchaseById(id: number) {
  return request({
    url: `/scm/purchase/${id}`,
    method: 'get'
  })
}
export function getPurchaseRequestList(params: any) {
  return request({
    url: '/scm/purchase-request/list',
    method: 'get',
    params
  })
}
export function getPurchaseRequestById(id: number) {
  return request({
    url: `/scm/purchase-request/${id}`,
    method: 'get'
  })
}
export function savePurchaseRequest(data: any) {
  return request({
    url: '/scm/purchase-request',
    method: 'post',
    data
  })
}
export function updatePurchaseRequest(data: any) {
  return request({
    url: '/scm/purchase-request',
    method: 'put',
    data
  })
}
export function deletePurchaseRequest(id: number) {
  return request({
    url: `/scm/purchase-request/${id}`,
    method: 'delete'
  })
}

// 锁单功能
export function lockPurchaseRequest(id: number) {
  return request({
    url: `/scm/purchase-request/${id}/lock`,
    method: 'post'
  })
}

// 解锁功能
export function unlockPurchaseRequest(id: number) {
  return request({
    url: `/scm/purchase-request/${id}/unlock`,
    method: 'post'
  })
}

// 审核采购申请
export function auditPurchaseRequest(id: number, status: string, detail?: string) {
  return request({
    url: `/scm/purchase-request/${id}/audit`,
    method: 'post',
    params: { status, detail }
  })
}

export function getPurchaseContractList(params: any) {
  return request({
    url: '/scm/purchase-contract/list',
    method: 'get',
    params
  })
}
export function getPurchaseContractById(id: number) {
  return request({
    url: `/scm/purchase-contract/${id}`,
    method: 'get'
  })
}
export function savePurchaseContract(data: any) {
  return request({
    url: '/scm/purchase-contract',
    method: 'post',
    data
  })
}
export function updatePurchaseContract(data: any) {
  return request({
    url: '/scm/purchase-contract',
    method: 'put',
    data
  })
}
export function deletePurchaseContract(id: number) {
  return request({
    url: `/scm/purchase-contract/${id}`,
    method: 'delete'
  })
}
export function createPurchaseContractFromRequest(requestId: number) {
  return request({
    url: `/scm/purchase-contract/create-from-request/${requestId}`,
    method: 'post'
  })
}

// 获取商品的采购统计信息
export function getProductPurchaseStats(productId: number) {
  return request({
    url: `/scm/purchase-request/product-stats/${productId}`,
    method: 'get'
  })
}