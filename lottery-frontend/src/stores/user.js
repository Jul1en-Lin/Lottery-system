import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { decodeJwt, isTokenExpired } from '@/utils/index'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => {
    if (!token.value) return false
    return !isTokenExpired(token.value)
  })

  const isAdmin = computed(() => {
    if (!token.value) return false
    const payload = decodeJwt(token.value)
    return payload?.identity === 'ADMIN'
  })

  const userId = computed(() => {
    if (!token.value) return null
    const payload = decodeJwt(token.value)
    return payload?.userId || null
  })

  // 从 JWT 获取用户名
  const userName = computed(() => {
    if (!token.value) return null
    const payload = decodeJwt(token.value)
    return payload?.userName || payload?.sub || null
  })

  // 从 JWT 获取邮箱
  const userEmail = computed(() => {
    if (!token.value) return null
    const payload = decodeJwt(token.value)
    return payload?.email || null
  })

  // 从 JWT 获取身份
  const identity = computed(() => {
    if (!token.value) return null
    const payload = decodeJwt(token.value)
    return payload?.identity || null
  })

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function clearToken() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    userId,
    userName,
    userEmail,
    identity,
    setToken,
    clearToken
  }
})
