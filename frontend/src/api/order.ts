import { request } from './request'
import type { Order, PageResult } from '@/utils/types'

export function getOrdersApi(params?: { page?: number; size?: number; status?: number | string }) {
  return request<PageResult<Order> | Order[]>({
    url: '/api/orders/mine',
    method: 'get',
    params,
  })
}

export function getOrderDetailApi(id: number | string) {
  return request<Order>({ url: `/api/orders/${id}`, method: 'get' })
}

export function createOrderApi(data: { productId: number | string; remark?: string }) {
  return request<Order>({ url: '/api/orders', method: 'post', data })
}

export function updateOrderStatusApi(id: number | string, status: number | string) {
  return request<Order>({ url: `/api/orders/${id}/status`, method: 'put', data: { status } })
}

/** 取消订单：PUT /api/orders/{id}/status，status=3 */
export function cancelOrderApi(id: number | string) {
  return updateOrderStatusApi(id, 3)
}
