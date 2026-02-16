import request from '@/utils/request'

export const getMyTasks = () => {
  return request({
    url: '/workflow/tasks/my',
    method: 'get'
  })
}

export const approveTask = (id: number, remark?: string) => {
  return request({
    url: `/workflow/task/${id}/approve`,
    method: 'post',
    data: { remark: remark || '' }
  })
}

export const rejectTask = (id: number, remark?: string) => {
  return request({
    url: `/workflow/task/${id}/reject`,
    method: 'post',
    data: { remark: remark || '' }
  })
}

export const addSignTask = (id: number, roleName: string, userId?: number) => {
  return request({
    url: `/workflow/task/${id}/add-sign`,
    method: 'post',
    data: { roleName, userId }
  })
}
