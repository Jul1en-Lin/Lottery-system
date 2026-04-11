<template>
  <div class="user-center-view fade-in">
    <NewspaperTitle
      title="读者中心"
      subtitle="READER CENTER"
    />

    <!-- 未登录提示 -->
    <div v-if="!userStore.isLoggedIn" class="not-logged-in paper-card">
      <p>您尚未登录，请先登录以查看个人信息。</p>
      <InkButton @click="goToLogin">前往登录</InkButton>
    </div>

    <!-- 已登录内容 -->
    <template v-else>
      <div class="user-info paper-card">
        <h3>个人信息</h3>
        <Divider />
        <div class="info-grid">
          <div class="info-item">
            <span class="label">用户ID：</span>
            <span class="value">{{ userStore.userId || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">用户名：</span>
            <span class="value">{{ userStore.userName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">邮箱：</span>
            <span class="value">{{ userStore.userEmail || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">身份：</span>
            <span class="value">
              <Stamp :text="userStore.isAdmin ? '管理员' : '用户'" size="small" />
            </span>
          </div>
        </div>
        <div class="user-actions">
          <InkButton variant="outline" @click="handleLogout">退出登录</InkButton>
        </div>
      </div>

      <div class="my-records paper-card">
        <h3>我的中奖记录</h3>
        <Divider />
        <div v-if="recordsLoading" class="loading-state">
          <LoadingSpinner />
          <span>加载中...</span>
        </div>
        <div v-else-if="myRecords.length === 0" class="empty-state">
          暂无中奖记录
        </div>
        <div v-else class="record-list">
          <div v-for="record in myRecords" :key="record.winnerId" class="record-item">
            <Stamp text="中奖" size="small" />
            <div class="record-info">
              <p class="prize-name">{{ record.prizeName }}</p>
              <p class="prize-tier">奖项等级：{{ getTierName(record.prizeTier) }}</p>
              <p class="win-time">{{ formatDate(record.winningTime) }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="quick-actions paper-card">
        <h3>快捷入口</h3>
        <Divider />
        <div class="action-grid">
          <router-link to="/activities" class="action-item">
            <span class="action-icon">◆</span>
            <span>参与活动</span>
          </router-link>
          <router-link to="/prizes" class="action-item">
            <span class="action-icon">◆</span>
            <span>中奖记录</span>
          </router-link>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { prizeApi } from '@/api/modules/prize'
import { getTierName } from '@/utils/index.js'
import NewspaperTitle from '@/components/common/NewspaperTitle.vue'
import Divider from '@/components/common/Divider.vue'
import InkButton from '@/components/common/InkButton.vue'
import Stamp from '@/components/common/Stamp.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const router = useRouter()
const userStore = useUserStore()

const allRecords = ref([])
const recordsLoading = ref(false)

// 过滤当前用户的中奖记录
const myRecords = computed(() => {
  const currentUserId = userStore.userId
  const currentUserName = userStore.userName
  if (!currentUserId && !currentUserName) return []

  return allRecords.value.filter(record => {
    // 根据用户ID或用户名匹配
    return record.winnerId === currentUserId ||
           record.winnerName === currentUserName ||
           record.userId === currentUserId
  })
})

onMounted(async () => {
  if (userStore.isLoggedIn) {
    await fetchMyRecords()
  }
})

async function fetchMyRecords() {
  recordsLoading.value = true
  try {
    const res = await prizeApi.getWinningRecords({ page: 1, size: 100 })
    allRecords.value = res.data?.records || res.data || []
  } catch (error) {
    console.error('获取中奖记录失败', error)
  } finally {
    recordsLoading.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

function goToLogin() {
  router.push('/login')
}

function handleLogout() {
  userStore.clearToken()
  router.push('/')
}
</script>

<style scoped>
.user-center-view {
  max-width: 800px;
  margin: 0 auto;
}

.not-logged-in {
  text-align: center;
  padding: 40px;
}

.not-logged-in p {
  margin-bottom: 20px;
  color: var(--ink-secondary);
}

.user-info,
.my-records,
.quick-actions {
  margin-bottom: 24px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item .label {
  color: var(--ink-secondary);
  min-width: 70px;
}

.info-item .value {
  font-weight: 500;
}

.user-actions {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--divider-color);
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px;
  color: var(--ink-secondary);
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: var(--ink-secondary);
}

.record-list {
  max-height: 400px;
  overflow-y: auto;
}

.record-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid var(--divider-color);
}

.record-item:last-child {
  border-bottom: none;
}

.record-info {
  flex: 1;
}

.prize-name {
  font-weight: bold;
  margin: 0 0 4px 0;
  color: var(--stamp-red);
}

.prize-tier {
  margin: 0 0 4px 0;
  font-size: 14px;
}

.win-time {
  font-size: 12px;
  color: var(--ink-secondary);
  margin: 0;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-top: 16px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border: 1px solid var(--border-color);
  text-decoration: none;
  color: var(--ink-primary);
  transition: background-color 0.2s;
}

.action-item:hover {
  background-color: var(--paper-bg-dark);
}

.action-icon {
  color: var(--stamp-red);
}
</style>
