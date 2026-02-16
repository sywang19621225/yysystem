import request from '@/utils/request'

export interface Product {
  id?: number
  productName: string
  productCode?: string
  productUnit: string
  costPrice: number
  stockQuantity: number
  availableStock: number
  totalStock: number
  status: number
}

export const getProductList = (params: any) => {
  return request({
    url: '/pdm/product/list',
    method: 'get',
    params
  })
}

export const saveProduct = (data: Product) => {
  return request({
    url: '/pdm/product',
    method: 'post',
    data
  })
}

export const updateProduct = (id: number, data: Product) => {
  return request({
    url: `/pdm/product/${id}`,
    method: 'put',
    data
  })
}

export const deleteProduct = (id: number) => {
  return request({
    url: `/pdm/product/${id}`,
    method: 'delete'
  })
}

export const getProductById = (id: number) => {
  return request({
    url: `/pdm/product/${id}`,
    method: 'get'
  })
}

export const getProductByCode = (productCode: string) => {
  return request({
    url: '/pdm/product/list',
    method: 'get',
    params: { productCode: productCode, current: 1, size: 1, _t: Date.now() }
  })
}
