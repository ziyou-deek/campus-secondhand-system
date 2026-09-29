import { request } from './request'
import type { UserInfo } from '@/utils/types'

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload {
  username: string
  password: string
  nickname?: string
  email?: string
  phone?: string
  studentId?: string
  college?: string
}

export interface LoginResult {
  token: string
  user?: UserInfo
  userInfo?: UserInfo
}

export function loginApi(data: LoginPayload) {
  return request<LoginResult>({ url: '/api/auth/login', method: 'post', data })
}

export function registerApi(data: RegisterPayload) {
  return request<UserInfo>({ url: '/api/auth/register', method: 'post', data })
}

export function getCurrentUserApi() {
  return request<UserInfo>({ url: '/api/auth/me', method: 'get' })
}

export function updateProfileApi(data: Partial<UserInfo>) {
  return request<UserInfo>({ url: '/api/auth/profile', method: 'put', data })
}

export function changePasswordApi(data: { oldPassword: string; newPassword: string }) {
  return request<void>({ url: '/api/auth/password', method: 'put', data })
}
