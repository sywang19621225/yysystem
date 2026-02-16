import request from '@/utils/request'

export const login = (data: any) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export const getInfo = () => {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}
