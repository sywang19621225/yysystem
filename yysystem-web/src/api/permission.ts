import request from '@/utils/request'

export const getPermissionList = (params: any) => {
  return request({ url: '/system/permission/list', method: 'get', params })
}

export const savePermission = (data: any) => {
  return request({ url: '/system/permission', method: 'post', data })
}

export const updatePermission = (data: any) => {
  return request({ url: '/system/permission', method: 'put', data })
}

export const deletePermission = (id: number) => {
  return request({ url: `/system/permission/${id}`, method: 'delete' })
}
