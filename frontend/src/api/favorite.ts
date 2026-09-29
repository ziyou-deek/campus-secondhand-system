import { request } from './request'
import type { PageResult, Product } from '@/utils/types'

export interface FavoriteItem {
  id: number | string
  userId?: number | string
  productId: number | string
  createTime?: string
  product?: Product
}

export function getFavoritesApi(params?: { page?: number; size?: number }) {
  return request<PageResult<FavoriteItem> | FavoriteItem[]>({
    url: '/api/favorites',
    method: 'get',
    params,
  })
}

export function addFavoriteApi(productId: number | string) {
  return request({ url: `/api/favorites/${productId}`, method: 'post' })
}

export function removeFavoriteApi(productId: number | string) {
  return request({ url: `/api/favorites/${productId}`, method: 'delete' })
}

export function checkFavoriteApi(productId: number | string) {
  return request<{ favorited: boolean }>({
    url: `/api/favorites/check/${productId}`,
    method: 'get',
  })
}
