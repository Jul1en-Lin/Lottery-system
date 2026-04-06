<template>
  <div class="admin-view fade-in">
    <NewspaperTitle
      title="编辑部"
      subtitle="ADMINISTRATION"
    />

    <div class="admin-tabs paper-card">
      <div class="tab-header">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          :class="['tab-btn', { active: activeTab === tab.key }]"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>

      <Divider />

      <div class="tab-content">
        <!-- 活动管理 -->
        <div v-if="activeTab === 'activities'" class="tab-panel">
          <h3>活动列表</h3>
          <InkButton @click="showCreateDialog = true">创建活动</InkButton>
          <div class="activity-list">
            <div v-for="activity in activities" :key="activity.activityId" class="list-item">
              <span>{{ activity.activityName }}</span>
              <span :class="['status', activity.valid ? 'active' : 'inactive']">
                {{ activity.valid ? '进行中' : '已结束' }}
              </span>
            </div>
          </div>
        </div>

        <!-- 用户管理 -->
        <div v-if="activeTab === 'users'" class="tab-panel">
          <h3>用户列表</h3>
          <div class="user-list">
            <div v-for="user in users" :key="user.id" class="list-item">
              <span>{{ user.userName }}</span>
              <span class="user-email">{{ user.email }}</span>
            </div>
          </div>
        </div>

        <!-- 中奖记录 -->
        <div v-if="activeTab === 'records'" class="tab-panel">
          <h3>中奖记录</h3>
          <div class="record-list">
            <div v-for="record in records" :key="record.winnerId" class="list-item">
              <span>{{ record.winnerName }}</span>
              <span>{{ record.prizeName }}</span>
              <span>{{ formatDate(record.winningTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useActivityStore } from '@/stores/activity'
import { userApi } from '@/api/modules/user'
import { prizeApi } from '@/api/modules/prize'
import NewspaperTitle from '@/components/common/NewspaperTitle.vue'
import InkButton from '@/components/common/InkButton.vue'
import Divider from '@/components/common/Divider.vue'

const activityStore = useActivityStore()

const tabs = [
  { key: 'activities', label: '活动管理' },
  { key: 'users', label: '用户管理' },
  { key: 'records', label: '中奖记录' }
]

const activeTab = ref('activities')
const showCreateDialog = ref(false)
const users = ref([])
const records = ref([])

const activities = ref([])

onMounted(async () => {
  await activityStore.fetchActivities()
  activities.value = activityStore.activities

  try {
    const userRes = await userApi.getUserList()
    users.value = userRes.data || []

    const recordRes = await prizeApi.getWinningRecords({})
    records.value = recordRes.data || []
  } catch (error) {
    console.error('加载数据失败', error)
  }
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.admin-view {
  max-width: 1000px;
  margin: 0 auto;
}

.admin-tabs {
  margin-top: 24px;
}

.tab-header {
  display: flex;
  gap: 16px;
}

.tab-btn {
  background: none;
  border: none;
  padding: 8px 16px;
  font-family: inherit;
  font-size: 16px;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  color: var(--ink-secondary);
}

.tab-btn.active {
  color: var(--ink-primary);
  border-bottom-color: var(--stamp-red);
}

.tab-panel h3 {
  margin: 0 0 16px 0;
}

.list-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--divider-color);
}

.status.active {
  color: var(--stamp-red);
}

.status.inactive {
  color: var(--ink-secondary);
}

.user-email {
  color: var(--ink-secondary);
  font-size: 14px;
}
</style>
