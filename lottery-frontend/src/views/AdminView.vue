<template>
  <div class="admin-view fade-in">
    <NewspaperTitle
      title="编辑部"
      subtitle="ADMINISTRATION"
    />

    <!-- 非管理员提示 -->
    <div v-if="!userStore.isAdmin" class="not-admin paper-card">
      <Stamp text="禁止访问" size="large" />
      <p>您没有管理员权限，无法访问此页面。</p>
      <InkButton @click="goHome">返回首页</InkButton>
    </div>

    <!-- 管理员内容 -->
    <template v-else>
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
            <div class="panel-header">
              <h3>活动列表</h3>
              <InkButton @click="openCreateActivityDialog">创建活动</InkButton>
            </div>

            <div v-if="loading.activities" class="loading-state">
              <LoadingSpinner />
              <span>加载中...</span>
            </div>

            <div v-else-if="activities.length === 0" class="empty-state">
              暂无活动
            </div>

            <div v-else class="data-table">
              <div class="table-header">
                <span class="col-name">活动名称</span>
                <span class="col-time">开始时间</span>
                <span class="col-time">结束时间</span>
                <span class="col-status">状态</span>
              </div>
              <div v-for="activity in activities" :key="activity.activityId" class="table-row">
                <span class="col-name">{{ activity.activityName }}</span>
                <span class="col-time">{{ formatDate(activity.startTime) }}</span>
                <span class="col-time">{{ formatDate(activity.endTime) }}</span>
                <span class="col-status">
                  <span :class="['status-badge', activity.valid ? 'active' : 'inactive']">
                    {{ activity.valid ? '进行中' : '已结束' }}
                  </span>
                </span>
              </div>
            </div>
          </div>

          <!-- 奖品管理 -->
          <div v-if="activeTab === 'prizes'" class="tab-panel">
            <div class="panel-header">
              <h3>奖品列表</h3>
            </div>

            <div v-if="loading.prizes" class="loading-state">
              <LoadingSpinner />
              <span>加载中...</span>
            </div>

            <div v-else-if="prizes.length === 0" class="empty-state">
              暂无奖品
            </div>

            <div v-else class="data-table">
              <div class="table-header">
                <span class="col-name">奖品名称</span>
                <span class="col-tier">奖项等级</span>
                <span class="col-count">数量</span>
                <span class="col-prob">中奖概率</span>
              </div>
              <div v-for="prize in prizes" :key="prize.prizeId || prize.id" class="table-row">
                <span class="col-name">{{ prize.prizeName }}</span>
                <span class="col-tier">{{ getTierName(prize.prizeTier) }}</span>
                <span class="col-count">{{ prize.prizeCount }}</span>
                <span class="col-prob">{{ (prize.probability * 100).toFixed(1) }}%</span>
              </div>
            </div>
          </div>

          <!-- 用户管理 -->
          <div v-if="activeTab === 'users'" class="tab-panel">
            <div class="panel-header">
              <h3>用户列表</h3>
            </div>

            <div v-if="loading.users" class="loading-state">
              <LoadingSpinner />
              <span>加载中...</span>
            </div>

            <div v-else-if="users.length === 0" class="empty-state">
              暂无用户
            </div>

            <div v-else class="data-table">
              <div class="table-header">
                <span class="col-name">用户名</span>
                <span class="col-email">邮箱</span>
                <span class="col-identity">身份</span>
              </div>
              <div v-for="user in users" :key="user.id || user.userId" class="table-row">
                <span class="col-name">{{ user.userName }}</span>
                <span class="col-email">{{ user.email }}</span>
                <span class="col-identity">
                  <span :class="['identity-badge', user.identity?.toLowerCase()]">
                    {{ user.identity === 'ADMIN' ? '管理员' : '用户' }}
                  </span>
                </span>
              </div>
            </div>
          </div>

          <!-- 中奖记录 -->
          <div v-if="activeTab === 'records'" class="tab-panel">
            <div class="panel-header">
              <h3>中奖记录</h3>
            </div>

            <div v-if="loading.records" class="loading-state">
              <LoadingSpinner />
              <span>加载中...</span>
            </div>

            <div v-else-if="records.length === 0" class="empty-state">
              暂无中奖记录
            </div>

            <div v-else class="data-table">
              <div class="table-header">
                <span class="col-name">中奖者</span>
                <span class="col-prize">奖品</span>
                <span class="col-tier">等级</span>
                <span class="col-time">中奖时间</span>
              </div>
              <div v-for="record in records" :key="record.winnerId || record.id" class="table-row">
                <span class="col-name">{{ record.winnerName }}</span>
                <span class="col-prize">{{ record.prizeName }}</span>
                <span class="col-tier">{{ getTierName(record.prizeTier) }}</span>
                <span class="col-time">{{ formatDate(record.winningTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 创建活动对话框 -->
      <div v-if="showCreateDialog" class="dialog-overlay" @click.self="closeCreateActivityDialog">
        <div class="dialog paper-card">
          <h3>创建活动</h3>
          <Divider />
          <form @submit.prevent="createActivity" class="create-form">
            <div class="form-group">
              <label>活动名称</label>
              <input v-model="activityForm.activityName" type="text" required placeholder="请输入活动名称" />
            </div>
            <div class="form-group">
              <label>活动描述</label>
              <textarea v-model="activityForm.activityDescription" placeholder="请输入活动描述"></textarea>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>开始时间</label>
                <input v-model="activityForm.startTime" type="datetime-local" required />
              </div>
              <div class="form-group">
                <label>结束时间</label>
                <input v-model="activityForm.endTime" type="datetime-local" required />
              </div>
            </div>
            <div class="form-actions">
              <InkButton type="button" variant="outline" @click="closeCreateActivityDialog">取消</InkButton>
              <InkButton type="submit" :disabled="submitting">
                {{ submitting ? '创建中...' : '创建' }}
              </InkButton>
            </div>
          </form>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useActivityStore } from '@/stores/activity'
import { userApi } from '@/api/modules/user'
import { prizeApi } from '@/api/modules/prize'
import { activityApi } from '@/api/modules/activity'
import { getTierName } from '@/utils/index.js'
import NewspaperTitle from '@/components/common/NewspaperTitle.vue'
import InkButton from '@/components/common/InkButton.vue'
import Divider from '@/components/common/Divider.vue'
import Stamp from '@/components/common/Stamp.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const router = useRouter()
const userStore = useUserStore()
const activityStore = useActivityStore()

const tabs = [
  { key: 'activities', label: '活动管理' },
  { key: 'prizes', label: '奖品管理' },
  { key: 'users', label: '用户管理' },
  { key: 'records', label: '中奖记录' }
]

const activeTab = ref('activities')
const showCreateDialog = ref(false)
const submitting = ref(false)

const loading = reactive({
  activities: false,
  prizes: false,
  users: false,
  records: false
})

const activities = ref([])
const prizes = ref([])
const users = ref([])
const records = ref([])

const activityForm = reactive({
  activityName: '',
  activityDescription: '',
  startTime: '',
  endTime: ''
})

onMounted(async () => {
  if (userStore.isAdmin) {
    await loadCurrentTabData()
  }
})

watch(activeTab, () => {
  loadCurrentTabData()
})

async function loadCurrentTabData() {
  switch (activeTab.value) {
    case 'activities':
      await loadActivities()
      break
    case 'prizes':
      await loadPrizes()
      break
    case 'users':
      await loadUsers()
      break
    case 'records':
      await loadRecords()
      break
  }
}

async function loadActivities() {
  if (activities.value.length > 0) return
  loading.activities = true
  try {
    await activityStore.fetchActivities(1, 50)
    activities.value = activityStore.activities
  } finally {
    loading.activities = false
  }
}

async function loadPrizes() {
  if (prizes.value.length > 0) return
  loading.prizes = true
  try {
    const res = await prizeApi.getPrizeList(1, 50)
    prizes.value = res.data?.records || res.data || []
  } catch (error) {
    console.error('加载奖品失败', error)
  } finally {
    loading.prizes = false
  }
}

async function loadUsers() {
  if (users.value.length > 0) return
  loading.users = true
  try {
    const res = await userApi.getUserList()
    users.value = res.data || []
  } catch (error) {
    console.error('加载用户失败', error)
  } finally {
    loading.users = false
  }
}

async function loadRecords() {
  if (records.value.length > 0) return
  loading.records = true
  try {
    const res = await prizeApi.getWinningRecords({ page: 1, size: 50 })
    records.value = res.data?.records || res.data || []
  } catch (error) {
    console.error('加载记录失败', error)
  } finally {
    loading.records = false
  }
}

function openCreateActivityDialog() {
  showCreateDialog.value = true
}

function closeCreateActivityDialog() {
  showCreateDialog.value = false
  resetActivityForm()
}

function resetActivityForm() {
  activityForm.activityName = ''
  activityForm.activityDescription = ''
  activityForm.startTime = ''
  activityForm.endTime = ''
}

async function createActivity() {
  submitting.value = true
  try {
    await activityApi.createActivity({
      activityName: activityForm.activityName,
      activityDescription: activityForm.activityDescription,
      startTime: activityForm.startTime,
      endTime: activityForm.endTime
    })
    closeCreateActivityDialog()
    // 刷新活动列表
    activities.value = []
    await loadActivities()
    alert('活动创建成功！')
  } catch (error) {
    console.error('创建活动失败', error)
    alert('创建活动失败，请重试。')
  } finally {
    submitting.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

function goHome() {
  router.push('/')
}
</script>

<style scoped>
.admin-view {
  max-width: 1000px;
  margin: 0 auto;
}

.not-admin {
  text-align: center;
  padding: 60px;
}

.not-admin p {
  margin: 20px 0;
  color: var(--ink-secondary);
}

.admin-tabs {
  margin-top: 24px;
}

.tab-header {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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
  transition: all 0.2s;
}

.tab-btn:hover {
  color: var(--ink-primary);
}

.tab-btn.active {
  color: var(--ink-primary);
  border-bottom-color: var(--stamp-red);
}

.tab-panel {
  padding: 16px 0;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.panel-header h3 {
  margin: 0;
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

/* 表格样式 */
.data-table {
  border: 1px solid var(--border-color);
}

.table-header,
.table-row {
  display: flex;
  padding: 12px 16px;
}

.table-header {
  background-color: var(--paper-bg-dark);
  font-weight: bold;
  font-size: 14px;
}

.table-row {
  border-top: 1px solid var(--divider-color);
}

.table-row:hover {
  background-color: rgba(0, 0, 0, 0.02);
}

.col-name {
  flex: 2;
  min-width: 120px;
}

.col-email {
  flex: 3;
  min-width: 150px;
  color: var(--ink-secondary);
  font-size: 14px;
}

.col-time {
  flex: 1.5;
  min-width: 100px;
  font-size: 14px;
}

.col-status,
.col-identity {
  flex: 1;
  min-width: 80px;
}

.col-tier {
  flex: 1;
  min-width: 80px;
}

.col-count,
.col-prob {
  flex: 1;
  min-width: 60px;
}

.col-prize {
  flex: 2;
  min-width: 120px;
}

.status-badge,
.identity-badge {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  border-radius: 2px;
}

.status-badge.active {
  background-color: rgba(180, 60, 50, 0.1);
  color: var(--stamp-red);
  border: 1px solid var(--stamp-red);
}

.status-badge.inactive {
  background-color: var(--paper-bg-dark);
  color: var(--ink-secondary);
  border: 1px solid var(--border-color);
}

.identity-badge.admin {
  background-color: rgba(180, 60, 50, 0.1);
  color: var(--stamp-red);
}

.identity-badge.user {
  background-color: var(--paper-bg-dark);
  color: var(--ink-secondary);
}

/* 对话框样式 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  padding: 24px;
}

.dialog h3 {
  margin: 0 0 16px 0;
}

.create-form {
  margin-top: 16px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  font-family: inherit;
  font-size: 14px;
  background-color: var(--paper-bg);
  box-sizing: border-box;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--stamp-red);
}

.form-group textarea {
  min-height: 80px;
  resize: vertical;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--divider-color);
}

@media (max-width: 600px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .table-header,
  .table-row {
    font-size: 12px;
    padding: 8px 12px;
  }
}
</style>
