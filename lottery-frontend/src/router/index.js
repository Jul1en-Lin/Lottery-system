import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

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
    component: () => import('@/views/LoginView.vue'),
    meta: { guest: true }
  },
  {
    path: '/signup',
    name: 'Signup',
    component: () => import('@/views/SignupView.vue'),
    meta: { guest: true }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/ForgotPasswordView.vue'),
    meta: { guest: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  // 需要登录但未登录
  if (to.meta.requiresAuth && !token) {
    return next('/login')
  }

  // 已登录但访问登录/注册页面，重定向到首页
  if (to.meta.guest && token) {
    return next('/')
  }

  // 需要管理员权限
  if (to.meta.requiresAdmin) {
    const userStore = useUserStore()
    if (!userStore.isAdmin) {
      return next('/')
    }
  }

  next()
})

export default router
