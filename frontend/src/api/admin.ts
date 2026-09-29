import { request } from './request'
import type { Category, PageResult, Product, StatsOverview, UserInfo, Violation } from '@/utils/types'

export function getAdminUsersApi(params?: { page?: number; size?: number; keyword?: string }) {
  return request<PageResult<UserInfo> | UserInfo[]>({
    url: '/api/admin/users',
    method: 'get',
    params,
  })
}

export function updateUserStatusApi(id: number | string, status: number | string) {
  return request({ url: `/api/admin/users/${id}/status`, method: 'put', data: { status } })
}

export function resetUserPasswordApi(id: number | string) {
  return request<{ username: string; temporaryPassword: string }>({
    url: `/api/admin/users/${id}/reset-password`,
    method: 'put',
  })
}

export function getAdminProductsApi(params?: {
  page?: number
  size?: number
  status?: number | string
  keyword?: string
}) {
  return request<PageResult<Product> | Product[]>({
    url: '/api/admin/products',
    method: 'get',
    params,
  })
}

export function auditProductApi(id: number | string, data: { status: number | string; reason?: string }) {
  return request({ url: `/api/admin/products/${id}/audit`, method: 'put', data })
}

export function getAdminCategoriesApi() {
  return request<Category[]>({ url: '/api/admin/categories', method: 'get' })
}

export function createAdminCategoryApi(data: Partial<Category>) {
  return request<Category>({ url: '/api/admin/categories', method: 'post', data })
}

export function updateAdminCategoryApi(id: number | string, data: Partial<Category>) {
  return request<Category>({ url: `/api/admin/categories/${id}`, method: 'put', data })
}

export function deleteAdminCategoryApi(id: number | string) {
  return request({ url: `/api/admin/categories/${id}`, method: 'delete' })
}

export function getViolationsApi(params?: { page?: number; size?: number; status?: number | string }) {
  return request<PageResult<Violation> | Violation[]>({
    url: '/api/admin/violations',
    method: 'get',
    params,
  })
}

export function handleViolationApi(id: number | string, data: { status: number | string; handleResult?: string }) {
  return request({ url: `/api/admin/violations/${id}`, method: 'put', data })
}

export function getAdminStatsApi() {
  return request<StatsOverview>({ url: '/api/admin/stats', method: 'get' })
}
