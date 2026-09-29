import { request } from './request'
import type { Conversation, Message } from '@/utils/types'

export function getConversationsApi() {
  return request<Conversation[]>({ url: '/api/messages/conversations', method: 'get' })
}

export function getMessagesApi(conversationId: number | string, params?: { page?: number; size?: number }) {
  return request<Message[]>({
    url: `/api/messages/conversations/${conversationId}`,
    method: 'get',
    params,
  })
}

export function sendMessageApi(data: {
  receiverId: number | string
  content: string
  conversationId?: number | string
}) {
  return request<Message>({ url: '/api/messages', method: 'post', data })
}

export function startConversationApi(peerId: number | string) {
  return request<Conversation>({
    url: '/api/messages/conversations',
    method: 'post',
    data: { peerId },
  })
}
