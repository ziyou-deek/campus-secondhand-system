import { request } from './request'
import type { Category } from '@/utils/types'

export function getCategoriesApi() {
  return request<Category[]>({ url: '/api/categories', method: 'get' })
}

export function createCategoryApi(data: Partial<Category>) {
  return request<Category>({ url: '/api/categories', method: 'post', data })
}

export function updateCategoryApi(id: number | string, data: Partial<Category>) {
  return request<Category>({ url: `/api/categories/${id}`, method: 'put', data })
}

export function deleteCategoryApi(id: number | string) {
  return request({ url: `/api/categories/${id}`, method: 'delete' })
}
