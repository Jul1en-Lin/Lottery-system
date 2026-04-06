# 新闻纸复古风格前端重构实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 Lottery-system 前端从现代 SaaS 风格重构为新闻纸复古风格

**Architecture:** Vue 3 + Element Plus + Vite 前端项目，构建产物输出到 Spring Boot static 目录，通过代理与后端 API 交互

**Tech Stack:** Vue 3.4+, Element Plus 2.5+, Vue Router 4.2+, Pinia 2.1+, Axios 1.6+, Vite 5.0+, Sass 1.69+

---

## 文件结构

```
lottery-frontend/
├── public/
│   └── textures/
│       └── paper-noise.svg          # 纸张纹理
├── src/
│   ├── assets/
│   │   ├── fonts/                   # 手写字体
│   │   └── images/                  # 静态图片
│   ├── components/
│   │   ├── common/
│   │   │   ├── PaperCard.vue
│   │   │   ├── Stamp.vue
│   │   │   ├── InkButton.vue
│   │   │   ├── NewspaperTitle.vue
│   │   │   └── Divider.vue
│   │   ├── layout/
│   │   │   ├── NewspaperLayout.vue
│   │   │   ├── NavBar.vue
│   │   │   └── Footer.vue
│   │   └── business/
│   │       ├── LotteryCard.vue
│   │       ├── PrizeList.vue
│   │       └── ScratchArea.vue
│   ├── views/
│   │   ├── HomeView.vue
│   │   ├── ActivityListView.vue
│   │   ├── ActivityDetailView.vue
│   │   ├── UserCenterView.vue
│   │   ├── PrizeRecordView.vue
│   │   ├── AdminView.vue
│   │   ├── LoginView.vue
│   │   ├── SignupView.vue
│   │   └── ForgotPasswordView.vue
│   ├── styles/
│   │   ├── variables.scss
│   │   ├── textures.scss
│   │   ├── typography.scss
│   │   └── animations.scss
│   ├── router/
│   │   └── index.js
│   ├── api/
│   │   ├── index.js
│   │   └── modules/
│   │       ├── user.js
│   │       ├── activity.js
│   │       └── prize.js
│   ├── stores/
│   │   ├── user.js
│   │   └── activity.js
│   ├── utils/
│   │   └── index.js
│   ├── App.vue
│   └── main.js
├── index.html
├── vite.config.js
└── package.json
```

---

## 阶段一：项目初始化（当前会话直接执行）

### Task 1: 创建 Vue 3 + Vite 项目

**Files:**
- Create: `lottery-frontend/package.json`
- Create: `lottery-frontend/vite.config.js`
- Create: `lottery-frontend/index.html`
- Create: `lottery-frontend/src/main.js`
- Create: `lottery-frontend/src/App.vue`

- [ ] **Step 1: 创建项目目录结构**

```bash
mkdir -p lottery-frontend/public/textures
mkdir -p lottery-frontend/src/assets/fonts
mkdir -p lottery-frontend/src/assets/images
mkdir -p lottery-frontend/src/components/common
mkdir -p lottery-frontend/src/components/layout
mkdir -p lottery-frontend/src/components/business
mkdir -p lottery-frontend/src/views
mkdir -p lottery-frontend/src/styles
mkdir -p lottery-frontend/src/router
mkdir -p lottery-frontend/src/api/modules
mkdir -p lottery-frontend/src/stores
mkdir -p lottery-frontend/src/utils
```

- [ ] **Step 2: 创建 package.json**

```json
{
  "name": "lottery-frontend",
  "version": "1.0.0",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.0",
    "pinia": "^2.1.0",
    "axios": "^1.6.0",
    "element-plus": "^2.5.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.0.0",
    "sass": "^1.69.0"
  }
}
```

- [ ] **Step 3: 创建 vite.config.js**

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
    rollupOptions: {
      output: {
        assetFileNames: 'assets/[name].[ext]'
      }
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/user': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/activity': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/prize': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/drawPrize': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/getWinningRecords': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

- [ ] **Step 4: 创建 index.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>幸运抽奖 - Lucky Lottery Daily</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Special+Elite&display=swap" rel="stylesheet">
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.js"></script>
  </body>
</html>
```

- [ ] **Step 5: 创建 main.js**

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './styles/variables.scss'
import './styles/typography.scss'
import './styles/textures.scss'
import './styles/animations.scss'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
```

- [ ] **Step 6: 创建 App.vue**

```vue
<template>
  <NewspaperLayout>
    <router-view v-slot="{ Component }">
      <transition name="page-flip" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </NewspaperLayout>
</template>

<script setup>
import NewspaperLayout from '@/components/layout/NewspaperLayout.vue'
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background-color: #F5F0E1;
}
</style>
```

- [ ] **Step 7: 安装依赖并验证项目启动**

```bash
cd lottery-frontend && npm install && npm run dev
```

预期：开发服务器在 http://localhost:3000 启动（此时会报错缺少组件，这是正常的）

---

### Task 2: 配置路由和状态管理

**Files:**
- Create: `lottery-frontend/src/router/index.js`
- Create: `lottery-frontend/src/stores/user.js`
- Create: `lottery-frontend/src/stores/activity.js`

- [ ] **Step 1: 创建路由配置**

```javascript
// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue')
  },
  {
    path: '/activities',
    name: 'ActivityList',
    component: () => import('@/views/ActivityListView.vue')
  },
  {
    path: '/activity/:id',
    name: 'ActivityDetail',
    component: () => import('@/views/ActivityDetailView.vue')
  },
  {
    path: '/user',
    name: 'UserCenter',
    component: () => import('@/views/UserCenterView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/prizes',
    name: 'PrizeRecord',
    component: () => import('@/views/PrizeRecordView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/AdminView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue')
  },
  {
    path: '/signup',
    name: 'Signup',
    component: () => import('@/views/SignupView.vue')
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/ForgotPasswordView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
```

- [ ] **Step 2: 创建用户状态管理**

```javascript
// src/stores/user.js
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
      const res = await userApi.getUserInfo()
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
```

- [ ] **Step 3: 创建活动状态管理**

```javascript
// src/stores/activity.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { activityApi } from '@/api/modules/activity'

export const useActivityStore = defineStore('activity', () => {
  const activities = ref([])
  const currentActivity = ref(null)
  const total = ref(0)
  const loading = ref(false)

  async function fetchActivities(page = 1, size = 10) {
    loading.value = true
    try {
      const res = await activityApi.getActivityList(page, size)
      activities.value = res.data.records
      total.value = res.data.total
    } finally {
      loading.value = false
    }
  }

  async function fetchActivityDetail(id) {
    loading.value = true
    try {
      const res = await activityApi.getActivityDetail(id)
      currentActivity.value = res.data
    } finally {
      loading.value = false
    }
  }

  function clearCurrentActivity() {
    currentActivity.value = null
  }

  return {
    activities,
    currentActivity,
    total,
    loading,
    fetchActivities,
    fetchActivityDetail,
    clearCurrentActivity
  }
})
```

- [ ] **Step 4: 验证文件创建成功**

```bash
ls -la lottery-frontend/src/router/ lottery-frontend/src/stores/
```

预期：显示 index.js, user.js, activity.js 文件

---

### Task 3: 配置 API 请求封装

**Files:**
- Create: `lottery-frontend/src/api/index.js`
- Create: `lottery-frontend/src/api/modules/user.js`
- Create: `lottery-frontend/src/api/modules/activity.js`
- Create: `lottery-frontend/src/api/modules/prize.js`

- [ ] **Step 1: 创建 Axios 实例**

```javascript
// src/api/index.js
import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    const message = error.response?.data?.message || '网络请求失败'
    ElMessage.error(message)

    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }

    return Promise.reject(error)
  }
)

export default api
```

- [ ] **Step 2: 创建用户 API 模块**

```javascript
// src/api/modules/user.js
import api from '../index'

export const userApi = {
  // 用户名密码注册
  register(data) {
    return api.post('/user/register', data)
  },

  // 发送邮箱验证码
  sendEmailCode(email) {
    return api.post('/user/sendEmailCode', null, { params: { email } })
  },

  // 邮箱验证码注册
  emailRegister(data) {
    return api.post('/user/emailRegister', data)
  },

  // 发送管理员邮箱验证码
  sendAdminEmailCode(email) {
    return api.post('/user/admin/sendEmailCode', null, { params: { email } })
  },

  // 管理员邮箱登录
  adminEmailLogin(data) {
    return api.post('/user/admin/emailLogin', data)
  },

  // 管理员密码登录
  adminPasswordLogin(data) {
    return api.post('/user/admin/passwordLogin', data)
  },

  // 获取用户列表
  getUserList(identity) {
    return api.get('/user/getListInfo', { params: { identity } })
  }
}
```

- [ ] **Step 3: 创建活动 API 模块**

```javascript
// src/api/modules/activity.js
import api from '../index'

export const activityApi = {
  // 获取活动列表
  getActivityList(page = 1, size = 10) {
    return api.get('/activity/queryList', {
      params: { page, size }
    })
  },

  // 获取活动详情
  getActivityDetail(activityId) {
    return api.get('/activity/getDetail', {
      params: { activityId }
    })
  },

  // 创建活动
  createActivity(data) {
    return api.post('/activity/create', data)
  }
}
```

- ] **Step 4: 创建抽奖 API 模块**

```javascript
// src/api/modules/prize.js
import api from '../index'

export const prizeApi = {
  // 获取奖品列表
  getPrizeList(page = 1, size = 10) {
    return api.get('/prize/getList', {
      params: { page, size }
    })
  },

  // 抽奖
  drawPrize(data) {
    return api.post('/drawPrize', data)
  },

  // 获取中奖记录
  getWinningRecords(data) {
    return api.post('/getWinningRecords', data)
  }
}
```

- [ ] **Step 5: 验证 API 模块**

```bash
ls -la lottery-frontend/src/api/modules/
```

---

### Task 4: 创建样式系统

**Files:**
- Create: `lottery-frontend/src/styles/variables.scss`
- Create: `lottery-frontend/src/styles/typography.scss`
- Create: `lottery-frontend/src/styles/textures.scss`
- Create: `lottery-frontend/src/styles/animations.scss`

- [ ] **Step 1: 创建 CSS 变量**

```scss
// src/styles/variables.scss

:root {
  // 新闻纸配色
  --paper-bg: #F5F0E1;
  --paper-bg-dark: #EBE6D7;
  --ink-primary: #1A1A1A;
  --ink-secondary: #4A4A4A;
  --stamp-red: #C41E3A;
  --blue-ink: #2B5F8E;
  --border-color: #8B8B8B;
  --divider-color: #C9C4B8;

  // Element Plus 主题覆盖
  --el-color-primary: #C41E3A;
  --el-color-primary-light-3: #E04A62;
  --el-color-primary-light-5: #E86B7F;
  --el-color-primary-light-7: #F08C9C;
  --el-color-primary-light-8: #F5A3AF;
  --el-color-primary-light-9: #FABAC4;

  --el-bg-color: #F5F0E1;
  --el-bg-color-page: #EBE6D7;
  --el-text-color-primary: #1A1A1A;
  --el-text-color-regular: #4A4A4A;
  --el-border-color: #8B8B8B;

  --el-font-family: 'ZCOOL KuaiLe', 'Special Elite', 'Courier New', serif;
}

// 字体大小
$font-size-headline: 48px;
$font-size-subtitle: 24px;
$font-size-body: 16px;
$font-size-small: 12px;

// 间距
$spacing-xs: 4px;
$spacing-sm: 8px;
$spacing-md: 16px;
$spacing-lg: 24px;
$spacing-xl: 32px;

// 边框
$border-style: 2px solid var(--border-color);
$border-double: 3px double var(--ink-primary);
```

- [ ] **Step 2: 创建字体样式**

```scss
// src/styles/typography.scss

@import url('https://fonts.googleapis.com/css2?family=ZCOOL+KuaiLe&display=swap');

// 基础字体设置
body {
  font-family: 'ZCOOL KuaiLe', 'Special Elite', 'Courier New', serif;
  color: var(--ink-primary);
  line-height: 1.6;
}

// 标题样式
.newspaper-headline {
  font-size: 48px;
  font-weight: bold;
  text-align: center;
  letter-spacing: 8px;
  line-height: 1.2;
  margin: 0;
  padding: 16px 0;

  &.large {
    font-size: 64px;
    letter-spacing: 12px;
  }

  &.subtitle {
    font-size: 24px;
    letter-spacing: 4px;
  }
}

// 英文副标题
.english-subtitle {
  font-family: 'Special Elite', 'Courier New', monospace;
  font-size: 14px;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: var(--ink-secondary);
  text-align: center;
  margin-top: 4px;
}

// 正文样式
.newspaper-body {
  font-size: 16px;
  line-height: 1.8;

  &.large {
    font-size: 18px;
  }
}

// 小字说明
.newspaper-caption {
  font-size: 12px;
  color: var(--ink-secondary);
}

// 强调文字
.emphasis {
  color: var(--stamp-red);
  font-weight: bold;
}

// 链接样式
.newspaper-link {
  color: var(--blue-ink);
  text-decoration: underline;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: var(--stamp-red);
  }
}
```

- [ ] **Step 3: 创建纹理样式**

```scss
// src/styles/textures.scss

// 纸张纹理背景
.paper-texture {
  background-color: var(--paper-bg);
  background-image: url('/textures/paper-noise.svg');
  background-repeat: repeat;
  position: relative;

  // 老化效果
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: radial-gradient(ellipse at center, transparent 0%, rgba(139, 139, 139, 0.1) 100%);
    pointer-events: none;
  }
}

// 纸张卡片基础样式
.paper-card {
  background-color: var(--paper-bg);
  border: 2px solid var(--border-color);
  padding: 20px;
  position: relative;
  box-shadow:
    2px 2px 0 rgba(0, 0, 0, 0.1),
    inset 0 0 30px rgba(139, 139, 139, 0.05);

  // 磨损边缘效果
  &::after {
    content: '';
    position: absolute;
    top: -2px;
    left: -2px;
    right: -2px;
    bottom: -2px;
    border: 2px solid transparent;
    border-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='100' height='100'%3E%3Crect fill='none' stroke='%238B8B8B' stroke-width='2' stroke-dasharray='4,2' width='100' height='100'/%3E%3C/svg%3E") 2 round;
    pointer-events: none;
  }

  &:hover {
    box-shadow:
      4px 4px 0 rgba(0, 0, 0, 0.15),
      inset 0 0 30px rgba(139, 139, 139, 0.08);
  }
}

// 双线边框
.double-border {
  border: 3px double var(--ink-primary);
  padding: 16px;
}

// 印刷网点效果
.print-dots {
  background-image: radial-gradient(circle, var(--ink-secondary) 1px, transparent 1px);
  background-size: 4px 4px;
  opacity: 0.3;
}

// 分割线样式
.divider-line {
  border: none;
  border-top: 1px solid var(--divider-color);
  margin: 16px 0;

  &.double {
    border-top: 3px double var(--ink-primary);
  }

  &.dashed {
    border-top: 2px dashed var(--border-color);
  }
}

// 报纸栏目边框
.column-border {
  border-left: 1px solid var(--divider-color);
  padding-left: 16px;
  margin-left: 16px;
}
```

- [ ] **Step 4: 创建动画样式**

```scss
// src/styles/animations.scss

// 页面翻折动画
.page-flip-enter-active {
  animation: flip-in 0.6s ease-out;
}

.page-flip-leave-active {
  animation: flip-out 0.4s ease-in;
}

@keyframes flip-in {
  0% {
    transform: perspective(1200px) rotateY(-90deg);
    opacity: 0;
  }
  100% {
    transform: perspective(1200px) rotateY(0);
    opacity: 1;
  }
}

@keyframes flip-out {
  0% {
    transform: perspective(1200px) rotateY(0);
    opacity: 1;
  }
  100% {
    transform: perspective(1200px) rotateY(90deg);
    opacity: 0;
  }
}

// 印章盖章动画
.stamp-appear {
  animation: stamp-down 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

@keyframes stamp-down {
  0% {
    transform: scale(2) rotate(-15deg);
    opacity: 0;
  }
  50% {
    transform: scale(0.9) rotate(-3deg);
  }
  100% {
    transform: scale(1) rotate(-5deg);
    opacity: 1;
  }
}

// 油墨晕染效果
.ink-spread {
  animation: ink-spread 0.3s ease-out;
}

@keyframes ink-spread {
  0% {
    box-shadow: inset 0 0 0 0 rgba(26, 26, 26, 0.3);
  }
  100% {
    box-shadow: inset 0 0 20px 10px rgba(26, 26, 26, 0.1);
  }
}

// 纸张晃动
.paper-shake:hover {
  animation: paper-shake 0.5s ease-in-out;
}

@keyframes paper-shake {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-0.5deg); }
  75% { transform: rotate(0.5deg); }
}

// 淡入效果
.fade-in {
  animation: fade-in 0.3s ease-out;
}

@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 打字机效果
.typewriter {
  overflow: hidden;
  white-space: nowrap;
  animation: typing 2s steps(30, end);
}

@keyframes typing {
  from { width: 0; }
  to { width: 100%; }
}
```

- [ ] **Step 5: 创建纸张纹理 SVG**

```svg
<!-- public/textures/paper-noise.svg -->
<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200">
  <filter id="noise">
    <feTurbulence type="fractalNoise" baseFrequency="0.9" numOctaves="4" stitchTiles="stitch"/>
    <feColorMatrix type="saturate" values="0"/>
  </filter>
  <rect width="100%" height="100%" filter="url(#noise)" opacity="0.08"/>
</svg>
```

- [ ] **Step 6: 验证样式文件**

```bash
ls -la lottery-frontend/src/styles/ lottery-frontend/public/textures/
```

---

## 阶段二：模块顺序开发（派发 Agent 执行）

### Agent 1: 组件库搭建

**任务内容：**
- PaperCard 组件
- Stamp 印章组件
- InkButton 油墨按钮
- NewspaperTitle 报纸标题
- Divider 分割线

**详细步骤见附录 A**

### Agent 2: 页面框架

**任务内容：**
- NewspaperLayout 布局组件
- NavBar 导航栏
- Footer 页脚
- 路由配置完善

**详细步骤见附录 B**

### Agent 3: 动画系统

**任务内容：**
- 翻页效果组件
- 刮奖交互组件
- 印章动画组件
- 微交互动画库

**详细步骤见附录 C**

### Agent 4: 认证页面

**任务内容：**
- LoginView 登录页
- SignupView 注册页
- ForgotPasswordView 忘记密码页

**详细步骤见附录 D**

### Agent 5: 业务页面

**任务内容：**
- HomeView 首页
- ActivityListView 活动列表
- ActivityDetailView 活动详情

**详细步骤见附录 E**

### Agent 6: 用户相关页面

**任务内容：**
- UserCenterView 用户中心
- PrizeRecordView 中奖记录
- AdminView 管理后台

**详细步骤见附录 F**

---

## 阶段三：集成部署（当前会话直接执行）

### Task 5: 构建验证与集成

- [ ] **Step 1: 执行生产构建**

```bash
cd lottery-frontend && npm run build
```

预期：构建产物输出到 `../src/main/resources/static`

- [ ] **Step 2: 验证构建产物**

```bash
ls -la ../src/main/resources/static/
```

预期：包含 index.html 和 assets 目录

- [ ] **Step 3: 启动后端服务测试**

```bash
cd .. && mvn spring-boot:run
```

访问 http://localhost:8080 验证前端页面正常加载

- [ ] **Step 4: 提交代码**

```bash
git add lottery-frontend/
git commit -m "feat: add newspaper retro style frontend

- Vue 3 + Vite + Element Plus 技术栈
- 新闻纸复古视觉风格
- 完整的页面和组件
- 与后端 API 集成

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## 附录 A: Agent 1 详细步骤 - 组件库搭建

### A.1 PaperCard 组件

```vue
<!-- src/components/common/PaperCard.vue -->
<template>
  <div class="paper-card" :class="{ 'paper-shake': hoverable }">
    <slot></slot>
  </div>
</template>

<script setup>
defineProps({
  hoverable: {
    type: Boolean,
    default: true
  }
})
</script>
```

### A.2 Stamp 组件

```vue
<!-- src/components/common/Stamp.vue -->
<template>
  <div class="stamp" :class="{ 'stamp-appear': animated }" :style="stampStyle">
    <span class="stamp-text">{{ text }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  text: {
    type: String,
    required: true
  },
  color: {
    type: String,
    default: '#C41E3A'
  },
  size: {
    type: String,
    default: 'medium' // small, medium, large
  },
  animated: {
    type: Boolean,
    default: true
  },
  rotation: {
    type: Number,
    default: -5
  }
})

const stampStyle = computed(() => ({
  color: props.color,
  transform: `rotate(${props.rotation}deg)`,
  fontSize: props.size === 'large' ? '24px' : props.size === 'small' ? '14px' : '18px'
}))
</script>

<style scoped>
.stamp {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  border: 3px solid currentColor;
  border-radius: 4px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 2px;
  opacity: 0.9;
}

.stamp-text {
  text-shadow: 1px 1px 0 rgba(0, 0, 0, 0.1);
}
</style>
```

### A.3 InkButton 组件

```vue
<!-- src/components/common/InkButton.vue -->
<template>
  <button
    class="ink-button"
    :class="{ 'ink-spread': pressed }"
    @mousedown="pressed = true"
    @mouseup="pressed = false"
    @mouseleave="pressed = false"
    :disabled="disabled"
  >
    <slot></slot>
  </button>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  disabled: {
    type: Boolean,
    default: false
  }
})

const pressed = ref(false)
</script>

<style scoped>
.ink-button {
  background-color: var(--paper-bg);
  border: 2px solid var(--ink-primary);
  color: var(--ink-primary);
  padding: 12px 32px;
  font-family: inherit;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.ink-button:hover:not(:disabled) {
  background-color: var(--ink-primary);
  color: var(--paper-bg);
}

.ink-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.ink-button.primary {
  background-color: var(--stamp-red);
  border-color: var(--stamp-red);
  color: var(--paper-bg);
}

.ink-button.primary:hover:not(:disabled) {
  background-color: darken(#C41E3A, 10%);
}
</style>
```

### A.4 NewspaperTitle 组件

```vue
<!-- src/components/common/NewspaperTitle.vue -->
<template>
  <div class="newspaper-title">
    <h1 class="headline" :class="sizeClass">{{ title }}</h1>
    <p v-if="subtitle" class="english-subtitle">{{ subtitle }}</p>
    <div v-if="showDate" class="title-date">
      {{ formattedDate }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'medium' // small, medium, large
  },
  showDate: {
    type: Boolean,
    default: false
  }
})

const sizeClass = computed(() => {
  const map = {
    small: 'subtitle',
    medium: '',
    large: 'large'
  }
  return map[props.size] || ''
})

const formattedDate = computed(() => {
  const now = new Date()
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${weekdays[now.getDay()]}`
})
</script>

<style scoped>
.newspaper-title {
  text-align: center;
  padding: 16px 0;
  border-bottom: 3px double var(--ink-primary);
  margin-bottom: 16px;
}

.headline {
  font-size: 48px;
  font-weight: bold;
  letter-spacing: 8px;
  margin: 0;
}

.headline.large {
  font-size: 64px;
  letter-spacing: 12px;
}

.headline.subtitle {
  font-size: 24px;
  letter-spacing: 4px;
}

.title-date {
  margin-top: 8px;
  font-size: 14px;
  color: var(--ink-secondary);
}
</style>
```

### A.5 Divider 组件

```vue
<!-- src/components/common/Divider.vue -->
<template>
  <hr class="divider" :class="type">
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'single' // single, double, dashed
  }
})
</script>

<style scoped>
.divider {
  border: none;
  margin: 16px 0;
}

.divider.single {
  border-top: 1px solid var(--divider-color);
}

.divider.double {
  border-top: 3px double var(--ink-primary);
}

.divider.dashed {
  border-top: 2px dashed var(--border-color);
}
</style>
```

---

## 附录 B-F: 其他 Agent 详细步骤

（由于篇幅限制，Agent 2-6 的详细步骤将在派发 Agent 时提供完整内容）

---

## 验收清单

- [ ] 项目可正常启动 (`npm run dev`)
- [ ] 构建产物正确输出到 static 目录
- [ ] 所有页面路由正常工作
- [ ] 视觉风格符合新闻纸复古设计
- [ ] 动画效果正常工作
- [ ] 与后端 API 正常交互
- [ ] 主流浏览器兼容（Chrome、Firefox、Edge）
