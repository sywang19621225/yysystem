import request from '@/utils/request'

export const createOutboundFromContract = (contractId: number) => {
  return request({
    url: `/scm/outbound/create-from-contract/${contractId}`,
    method: 'post'
  })
}

export const createOutbound = (data?: any) => {
  return request({
    url: '/scm/outbound',
    method: 'post',
    data
  })
}

export const getOutboundList = (params: any) => {
  return request({
    url: '/scm/outbound/list',
    method: 'get',
    params
  })
}

export const auditOutbound = (id: number, status: string, detail?: string) => {
  return request({
    url: `/scm/outbound/${id}/audit`,
    method: 'put',
    params: { status, detail }
  })
}

// 解锁出库单（将已审核状态改为待审核，允许删除）
export const unlockOutbound = (id: number) => {
  return request({
    url: `/scm/outbound/${id}/unlock`,
    method: 'put'
  })
}

export const getOutboundById = (id: number) => {
  return request({
    url: `/scm/outbound/${id}`,
    method: 'get',
    timeout: 20000
  })
}

export const updateOutbound = (id: number, data: any) => {
  return request({
    url: `/scm/outbound/${id}`,
    method: 'put',
    data
  })
}

export const getOutboundDetails = (id: number) => {
  return request({
    url: `/scm/outbound/${id}/details`,
    method: 'get',
    timeout: 20000
  })
}

export const updateOutboundDetail = (detailId: number, data: any) => {
  return request({
    url: `/scm/outbound/detail/${detailId}`,
    method: 'put',
    data
  })
}

export const addOutboundDetail = (data: any, count: number = 1) => {
  return request({
    url: `/scm/outbound/detail`,
    method: 'post',
    params: { count },
    data
  })
}

export const deleteOutbound = (id: number) => {
  return request({
    url: `/scm/outbound/${id}`,
    method: 'delete',
    timeout: 20000
  })
}

export const applyContractToOutbound = (id: number, contractId: number, limit?: number) => {
  return request({
    url: `/scm/outbound/${id}/apply-contract/${contractId}`,
    method: 'post',
    timeout: 20000,
    params: typeof limit === 'number' ? { limit } : undefined
  })
}

export const deleteOutboundDetail = (detailId: number) => {
  return request({
    url: `/scm/outbound/detail/${detailId}`,
    method: 'delete',
    timeout: 30000 // 30秒超时
  })
}

export const importContractDetails = (id: number, contractId: number, items: Array<{ contractDetailId: number, quantity: number }>) => {
  return request({
    url: `/scm/outbound/${id}/import-contract-details`,
    method: 'post',
    data: { contractId, items },
    timeout: 20000
  })
}

// 获取采购申请已入库的商品及可退数量
// excludeOutboundId: 可选，排除指定出库单的退货数量（用于编辑时）
export const getPurchaseContractInbound = (purchaseRequestId: number, excludeOutboundId?: number) => {
  return request({
    url: `/scm/outbound/purchase-contract-inbound/${purchaseRequestId}`,
    method: 'get',
    params: excludeOutboundId ? { excludeOutboundId } : undefined
  })
}

// 导入采购退货明细
export const importPurchaseReturn = (id: number, items: Array<{ productCode: string, productName: string, productSpec: string, productUnit: string, unitPrice: number, quantity: number }>) => {
  return request({
    url: `/scm/outbound/${id}/import-purchase-return`,
    method: 'post',
    data: { items },
    timeout: 20000
  })
}
