import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  loginApi,
  registerApi,
  getCurrentUserApi,
  updateProfileApi,
  type LoginPayload,
  type RegisterPayload,
} from '@/api/auth'
import {
  getToken,
  setToken,
  clearAuth,
  getStoredUser,
  setStoredUser,
} from '@/utils/auth'
import type { UserInfo } from '@/utils/types'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const userInfo = ref<UserInfo | null>(getStoredUser<UserInfo>())

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => {
    const r = (userInfo.value?.role || '').toString().toUpperCase()
    return r === 'ADMIN'
  })
  const displayName = computed(
    () => userInfo.value?.nickname || userInfo.value?.username || '用户'
  )

  function normalizeRole(user: UserInfo): UserInfo {
    const r = (user.role || '').toString().toUpperCase()
    return { ...user, role: r === 'ADMIN' ? 'ADMIN' : (r === 'USER' || r === 'STUDENT' ? 'USER' : user.role) }
  }

  function applyAuth(t: string, user: UserInfo) {
    const normalized = normalizeRole(user)
    token.value = t
    userInfo.value = normalized
    setToken(t)
    setStoredUser(normalized)
  }

  async function login(payload: LoginPayload) {
    const res = await loginApi(payload)
    const t = res.token
    const user = res.user || res.userInfo
    if (!t) throw new Error('登录返回缺少 token')
    if (user) {
      applyAuth(t, user)
    } else {
      applyAuth(t, {
        id: 0,
        username: payload.username,
        role: 'student',
      })
      await fetchProfile().catch(() => undefined)
    }
    return true
  }

  async function register(payload: RegisterPayload) {
    await registerApi(payload)
    return true
  }

  async function fetchProfile() {
    try {
      const user = await getCurrentUserApi()
      userInfo.value = user
      setStoredUser(user)
      return user
    } catch {
      return userInfo.value
    }
  }

  async function updateProfile(data: Partial<UserInfo>) {
    const user = await updateProfileApi(data)
    userInfo.value = { ...userInfo.value, ...user } as UserInfo
    setStoredUser(userInfo.value)
    return userInfo.value
  }

  function logout() {
    token.value = null
    userInfo.value = null
    clearAuth()
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    displayName,
    login,
    register,
    fetchProfile,
    updateProfile,
    logout,
  }
})
