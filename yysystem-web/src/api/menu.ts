import request from '../utils/request'

export interface Menu {
  id: number
  parentId: number
  menuName: string
  path: string
  component: string
  perms: string
  icon: string
  menuType: string
  orderNum: number
  status: number
  remark: string
  children?: Menu[]
}

// 获取菜单树
export function getMenuTree() {
  return request({
    url: '/system/menu/tree',
    method: 'get'
  })
}

// 获取菜单列表
export function getMenuList() {
  return request({
    url: '/system/menu/list',
    method: 'get'
  })
}
