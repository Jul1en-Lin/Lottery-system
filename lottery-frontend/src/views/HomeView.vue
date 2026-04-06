<template>
  <div class="home-view fade-in">
    <NewspaperTitle
      title="幸运抽奖日报"
      subtitle="LUCKY LOTTERY DAILY"
      show-date
      size="large"
    />

    <div class="headline-section paper-card double-border">
      <h2 class="headline-title">特 大 奖 抽 奖 活 动</h2>
      <p class="english-subtitle">JACKPOT LOTTERY EVENT</p>
    </div>

    <div class="content-grid">
      <div class="column paper-card">
        <h3 class="column-title">活动快讯</h3>
        <Divider type="double" />
        <LoadingSpinner v-if="activityLoading" size="small" text="加载活动" />
        <div v-else-if="activityError" class="error-text">
          <Stamp text="加载失败" color="var(--stamp-red)" size="small" :animated="false" />
          <p>{{ activityError }}</p>
        </div>
        <template v-else-if="recentActivities.length > 0">
          <div
            v-for="activity in recentActivities"
            :key="activity.activityId"
            class="news-item"
            @click="goToDetail(activity.activityId)"
          >
            <span class="news-bullet">◆</span>
            <span class="news-link">{{ activity.activityName }}</span>
            <span v-if="activity.valid" class="news-status active">进行中</span>
            <span v-else class="news-status">已结束</span>
          </div>
        </template>
        <div v-else class="empty-text">暂无活动</div>
      </div>

      <div class="column paper-card">
        <h3 class="column-title">近期中奖公告</h3>
        <Divider type="double" />
        <LoadingSpinner v-if="recordsLoading" size="small" text="加载记录" />
        <div v-else-if="recordsError" class="error-text">
          <Stamp text="加载失败" color="var(--stamp-red)" size="small" :animated="false" />
          <p>{{ recordsError }}</p>
        </div>
        <template v-else-if="recentWinners.length > 0">
          <div
            v-for="record in recentWinners"
            :key="record.id"
            class="winner-item"
          >
            <Stamp text="中奖" size="small" />
            <span>{{ maskUsername(record.winnerName) }} 中得 {{ record.prizeName }}</span>
          </div>
        </template>
        <div v-else class="empty-text">暂无中奖记录</div>
        <router-link to="/prizes" class="view-more">查看更多 &gt;</router-link>
      </div>
    </div>

    <div class="action-buttons">
      <InkButton @click="goToActivities">立即参与</InkButton>
      <InkButton @click="scrollToActivities">查看全部活动</InkButton>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NewspaperTitle from '@/components/common/NewspaperTitle.vue'
import Stamp from '@/components/common/Stamp.vue'
import InkButton from '@/components/common/InkButton.vue'
import Divider from '@/components/common/Divider.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import { activityApi } from '@/api/modules/activity'
import { prizeApi } from '@/api/modules/prize'

const router = useRouter()

const recentActivities = ref([])
const recentWinners = ref([])
const activityLoading = ref(false)
const recordsLoading = ref(false)
const activityError = ref(null)
const recordsError = ref(null)

onMounted(() => {
  fetchRecentActivities()
  fetchRecentWinners()
})

async function fetchRecentActivities() {
  activityLoading.value = true
  activityError.value = null
  try {
    const res = await activityApi.getActivityList(1, 5)
    recentActivities.value = res.data?.records || []
  } catch (error) {
    console.error('Failed to fetch activities:', error)
    activityError.value = error.message || '加载活动失败'
  } finally {
    activityLoading.value = false
  }
}

async function fetchRecentWinners() {
  recordsLoading.value = true
  recordsError.value = null
  try {
    const res = await prizeApi.getWinningRecords({ page: 1, size: 5 })
    recentWinners.value = res.data?.records || []
  } catch (error) {
    console.error('Failed to fetch winning records:', error)
    recordsError.value = error.message || '加载中奖记录失败'
  } finally {
    recordsLoading.value = false
  }
}

function maskUsername(username) {
  if (!username) return '**'
  if (username.length <= 2) return username[0] + '*'
  return username[0] + '*'.repeat(username.length - 2) + username[username.length - 1]
}

function goToActivities() {
  router.push('/activities')
}

function scrollToActivities() {
  const activitiesSection = document.querySelector('.column:first-child')
  if (activitiesSection) {
    activitiesSection.scrollIntoView({ behavior: 'smooth' })
  }
}

function goToDetail(id) {
  router.push(`/activity/${id}`)
}
</script>

<style scoped>
.home-view {
  max-width: 900px;
  margin: 0 auto;
}

.headline-section {
  text-align: center;
  margin: 24px 0;
  padding: 32px;
}

.headline-title {
  font-size: 36px;
  letter-spacing: 8px;
  margin: 0 0 8px 0;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin: 24px 0;
}

.column {
  padding: 20px;
  min-height: 200px;
}

.column-title {
  font-size: 18px;
  letter-spacing: 4px;
  margin: 0 0 8px 0;
}

.news-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  cursor: pointer;
}

.news-item:hover .news-link {
  text-decoration: underline;
}

.news-bullet {
  color: var(--stamp-red);
}

.news-link {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.news-status {
  font-size: 12px;
  padding: 2px 8px;
  border: 1px solid var(--ink-secondary);
  color: var(--ink-secondary);
}

.news-status.active {
  border-color: var(--stamp-red);
  color: var(--stamp-red);
}

.winner-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.empty-text {
  color: var(--ink-secondary);
  font-size: 14px;
  padding: 16px 0;
  text-align: center;
}

.error-text {
  color: var(--stamp-red);
  font-size: 14px;
  padding: 16px 0;
  text-align: center;
}

.error-text p {
  margin-top: 8px;
}

.view-more {
  display: block;
  text-align: right;
  color: var(--blue-ink);
  text-decoration: underline;
  margin-top: 12px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 32px;
}
</style>
