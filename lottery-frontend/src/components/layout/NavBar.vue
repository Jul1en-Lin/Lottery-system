<template>
  <header class="navbar">
    <div class="navbar-content">
      <div class="navbar-brand" @click="goHome">
        <span class="brand-icon">★</span>
        <span class="brand-text">LUCKY LOTTERY DAILY</span>
      </div>
      <nav class="navbar-nav">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/activities" class="nav-link">活动</router-link>
        <template v-if="!userStore.isLoggedIn">
          <router-link to="/login" class="nav-link">登录</router-link>
        </template>
        <template v-else>
          <router-link to="/user" class="nav-link">用户中心</router-link>
          <router-link to="/prizes" class="nav-link">中奖记录</router-link>
          <router-link v-if="userStore.isAdmin" to="/admin" class="nav-link nav-link-admin">
            管理后台
          </router-link>
          <div class="user-info">
            <span class="user-email">{{ userEmail }}</span>
            <button class="logout-btn" @click="handleLogout">退出</button>
          </div>
        </template>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { decodeJwt } from '@/utils/index'

const router = useRouter()
const userStore = useUserStore()

const userEmail = computed(() => {
  if (!userStore.token) return ''
  const payload = decodeJwt(userStore.token)
  return payload?.sub || payload?.email || '用户'
})

function goHome() {
  router.push('/')
}

function handleLogout() {
  userStore.clearToken()
  router.push('/')
}
</script>

<style scoped>
.navbar {
  background-color: var(--paper-bg-dark);
  border-bottom: 3px double var(--ink-primary);
  padding: 12px 0;
}

.navbar-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.navbar-brand:hover {
  opacity: 0.8;
}

.brand-icon {
  font-size: 20px;
  color: var(--stamp-red);
}

.brand-text {
  font-family: 'Special Elite', 'Courier New', monospace;
  font-size: 18px;
  letter-spacing: 2px;
}

.navbar-nav {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-link {
  color: var(--ink-primary);
  text-decoration: none;
  font-size: 16px;
  letter-spacing: 2px;
  padding: 4px 8px;
  border-bottom: 2px solid transparent;
  transition: border-color 0.2s;
}

.nav-link:hover,
.nav-link.router-link-active {
  border-bottom-color: var(--stamp-red);
}

.nav-link-admin {
  color: var(--stamp-red);
  font-weight: 600;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: 8px;
  padding-left: 16px;
  border-left: 1px solid var(--divider-color);
}

.user-email {
  font-size: 14px;
  color: var(--ink-secondary);
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.logout-btn {
  background: transparent;
  border: 1px solid var(--border-color);
  color: var(--ink-secondary);
  font-size: 14px;
  padding: 4px 12px;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.logout-btn:hover {
  background-color: var(--paper-bg-dark);
  color: var(--stamp-red);
  border-color: var(--stamp-red);
}

@media (max-width: 768px) {
  .navbar-content {
    flex-direction: column;
    gap: 12px;
  }

  .navbar-nav {
    flex-wrap: wrap;
    justify-content: center;
    gap: 12px;
  }

  .user-info {
    border-left: none;
    padding-left: 0;
    width: 100%;
    justify-content: center;
    margin-left: 0;
    margin-top: 8px;
  }
}
</style>
