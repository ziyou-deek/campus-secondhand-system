export interface UserInfo {
  id: number | string
  username: string
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  role: string
  studentId?: string
  college?: string
  status?: number | string
  createTime?: string
}

export interface Category {
  id: number | string
  name: string
  description?: string
  sort?: number
  status?: number
}

export interface Product {
  id: number | string
  title: string
  description?: string
  price: number
  originalPrice?: number
  images?: string[] | string
  cover?: string
  categoryId?: number | string
  categoryName?: string
  condition?: string
  status?: number | string
  sellerId?: number | string
  sellerName?: string
  viewCount?: number
  favoriteCount?: number
  createTime?: string
  updateTime?: string
  location?: string
}

export interface Order {
  id: number | string
  orderNo?: string
  productId: number | string
  productTitle?: string
  productCover?: string
  price: number
  buyerId?: number | string
  buyerName?: string
  sellerId?: number | string
  sellerName?: string
  status: number | string
  buyerConfirmed?: boolean
  sellerConfirmed?: boolean
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface Comment {
  id: number | string
  productId?: number | string
  orderId?: number | string
  userId?: number | string
  userName?: string
  avatar?: string
  content: string
  rating?: number
  createTime?: string
}

export interface Conversation {
  id: number | string
  peerId: number | string
  peerName?: string
  peerAvatar?: string
  lastMessage?: string
  lastTime?: string
  unread?: number
}

export interface Message {
  id: number | string
  conversationId?: number | string
  senderId: number | string
  receiverId?: number | string
  content: string
  createTime?: string
  read?: boolean
}

export interface Violation {
  id: number | string
  userId?: number | string
  userName?: string
  productId?: number | string
  productTitle?: string
  reason: string
  status?: number | string
  createTime?: string
  handleResult?: string
}

export interface PageResult<T> {
  list?: T[]
  records?: T[]
  total: number
  page?: number
  size?: number
}

export interface ApiResult<T = any> {
  code?: number
  message?: string
  msg?: string
  data: T
}

export interface StatsOverview {
  userCount?: number
  productCount?: number
  orderCount?: number
  categoryStats?: { name: string; value: number }[]
  orderTrend?: { date: string; count: number }[]
  productStatusStats?: { name: string; value: number }[]
}
