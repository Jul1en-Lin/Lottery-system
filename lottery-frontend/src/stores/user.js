import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '@/api/modules/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.identity === 'ADMIN')

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function clearToken() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const res = await userApi.getUserList()
      userInfo.value = res.data
    } catch (error) {
      clearToken()
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    setToken,
    clearToken,
    fetchUserInfo
  }
})
