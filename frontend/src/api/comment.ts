import { request } from './request'
import type { Comment, PageResult } from '@/utils/types'

export function getCommentsApi(params: { productId?: number | string; page?: number; size?: number }) {
  return request<PageResult<Comment> | Comment[]>({
    url: '/api/comments',
    method: 'get',
    params,
  })
}

export function createCommentApi(data: {
  productId?: number | string
  orderId?: number | string
  content: string
  rating?: number
}) {
  return request<Comment>({ url: '/api/comments', method: 'post', data })
}
