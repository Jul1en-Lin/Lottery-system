<template>
  <div class="prize-record-view fade-in">
    <NewspaperTitle
      title="中奖公告"
      subtitle="WINNING ANNOUNCEMENTS"
    />

    <!-- 未登录提示 -->
    <div v-if="!userStore.isLoggedIn" class="not-logged-in paper-card">
      <p>您尚未登录，请先登录以查看中奖记录。</p>
      <InkButton @click="goToLogin">前往登录</InkButton>
    </div>

    <!-- 已登录内容 -->
    <template v-else>
      <div class="record-list paper-card">
        <div v-if="loading" class="loading-state">
          <LoadingSpinner />
          <span>加载中...</span>
        </div>

        <div v-else-if="records.length === 0" class="empty-state">
          <Stamp text="空" size="large" />
          <p>暂无中奖记录</p>
        </div>

        <template v-else>
          <div class="records-container">
            <div v-for="record in paginatedRecords" :key="record.winnerId || record.id" class="record-item">
              <Stamp text="中奖" size="small" />
              <div class="record-info">
                <div class="record-header">
                  <p class="winner-name">{{ record.winnerName || '匿名用户' }}</p>
                  <span class="prize-tier">{{ getTierName(record.prizeTier) }}</span>
                </div>
                <p class="prize-name">{{ record.prizeName }}</p>
                <p class="activity-name">
                  <template v-if="record.activityName">活动：{{ record.activityName }}</template>
                  <template v-else>&nbsp;</template>
                </p>
                <p class="win-time">{{ formatDate(record.winningTime) }}</p>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div class="pagination" v-if="total > pageSize">
            <button
              class="page-btn"
              :disabled="currentPage === 1"
              @click="changePage(currentPage - 1)"
            >
              上一页
            </button>
            <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
            <button
              class="page-btn"
              :disabled="currentPage >= totalPages"
              @click="changePage(currentPage + 1)"
            >
              下一页
            </button>
          </div>
        </template>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { prizeApi } from '@/api/modules/prize'
import { getTierName } from '@/utils/index.js'
import NewspaperTitle from '@/components/common/NewspaperTitle.vue'
import Stamp from '@/components/common/Stamp.vue'
import InkButton from '@/components/common/InkButton.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const router = useRouter()
const userStore = useUserStore()

const records = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)

// 客户端分页：根据当前页码和每页数量计算显示的记录
const paginatedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return records.value.slice(start, end)
})

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

onMounted(async () => {
  if (userStore.isLoggedIn) {
    await fetchRecords()
  }
})

async function fetchRecords() {
  loading.value = true
  try {
    const res = await prizeApi.getWinningRecords({
      page: currentPage.value,
      size: pageSize.value
    })
    // 处理分页响应
    if (res.data?.records) {
      records.value = res.data.records
      total.value = res.data.total || 0
    } else if (Array.isArray(res.data)) {
      records.value = res.data
      total.value = res.data.length
    } else {
      records.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取中奖记录失败', error)
    records.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function changePage(page) {
  currentPage.value = page
  // 客户端分页：切换页面时不重新请求数据，直接使用已加载的数据
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function goToLogin() {
  router.push('/login')
}
</script>

<style scoped>
.prize-record-view {
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

.record-list {
  margin-top: 24px;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 60px;
  color: var(--ink-secondary);
}

.empty-state {
  text-align: center;
  padding: 60px;
  color: var(--ink-secondary);
}

.empty-state p {
  margin-top: 16px;
}

.records-container {
  /* 固定最小高度，确保无论显示多少条记录，分页按钮位置保持不变
     每条约80px高，5条约400px，加上padding预留空间 */
  min-height: 450px;
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

.record-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

.winner-name {
  font-weight: bold;
  margin: 0;
  font-size: 16px;
}

.prize-tier {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  border: 1px solid var(--stamp-red);
  color: var(--stamp-red);
}

.prize-name {
  color: var(--stamp-red);
  margin: 0 0 4px 0;
  font-weight: 500;
}

.activity-name {
  font-size: 14px;
  color: var(--ink-secondary);
  margin: 0 0 4px 0;
}

.win-time {
  font-size: 12px;
  color: var(--ink-secondary);
  margin: 0;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--divider-color);
}

.page-btn {
  background: none;
  border: 1px solid var(--border-color);
  padding: 8px 16px;
  font-family: inherit;
  font-size: 14px;
  cursor: pointer;
  color: var(--ink-primary);
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background-color: var(--paper-bg-dark);
  border-color: var(--stamp-red);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: var(--ink-secondary);
}
</style>
