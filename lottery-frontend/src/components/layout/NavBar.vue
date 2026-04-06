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
        <router-link v-if="!userStore.isLoggedIn" to="/login" class="nav-link">登录</router-link>
        <router-link v-else to="/user" class="nav-link">用户中心</router-link>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

function goHome() {
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
  gap: 24px;
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
</style>
