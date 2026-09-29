import { request } from './request'
import type { PageResult, Product } from '@/utils/types'

export interface ProductQuery {
  page?: number
  size?: number
  keyword?: string
  categoryId?: number | string
  status?: number | string
  sellerId?: number | string
}

export function getProductsApi(params: ProductQuery) {
  return request<PageResult<Product> | Product[]>({
    url: '/api/products',
    method: 'get',
    params,
  })
}

export function getProductDetailApi(id: number | string) {
  return request<Product>({ url: `/api/products/${id}`, method: 'get' })
}

export function createProductApi(data: Partial<Product>) {
  return request<Product>({ url: '/api/products', method: 'post', data })
}

export function updateProductApi(id: number | string, data: Partial<Product>) {
  return request<Product>({ url: `/api/products/${id}`, method: 'put', data })
}

export function deleteProductApi(id: number | string) {
  return request({ url: `/api/products/${id}`, method: 'delete' })
}

export function getMyProductsApi(params?: ProductQuery) {
  return request<PageResult<Product> | Product[]>({
    url: '/api/products/mine',
    method: 'get',
    params,
  })
}
