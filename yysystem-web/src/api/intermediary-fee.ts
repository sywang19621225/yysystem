import request from '@/utils/request'

// 获取居间费列表
export function getIntermediaryFeeList(params: any) {
  return request({
    url: '/crm/intermediary-fee/list',
    method: 'get',
    params
  })
}

// 获取居间费统计
export function getIntermediaryFeeStatistics() {
  return request({
    url: '/crm/intermediary-fee/statistics',
    method: 'get'
  })
}

// 更新居间费支付状态
export function updateIntermediaryFeeStatus(id: number, data: any) {
  return request({
    url: `/crm/intermediary-fee/${id}/status`,
    method: 'put',
    data
  })
}
