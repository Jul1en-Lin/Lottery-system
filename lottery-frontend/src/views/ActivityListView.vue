<template>
  <div class="activity-list-view fade-in">
    <NewspaperTitle
      title="活动版面"
      subtitle="ACTIVITIES - 第 A02 版"
    />

    <LoadingSpinner v-if="activityStore.loading" text="加载活动列表" />

    <div v-else-if="error" class="error-state paper-card">
      <Stamp text="加载失败" color="var(--stamp-red)" size="large" :animated="false" />
      <p class="error-hint">{{ error }}</p>
    </div>

    <div v-else-if="activityStore.activities.length > 0" class="activity-grid">
      <LotteryCard
        v-for="activity in activityStore.activities"
        :key="activity.activityId"
        :activity="activity"
        @click="goToDetail"
        @action="goToDetail"
      />
    </div>

    <div v-else class="empty-state paper-card">
      <Stamp text="暂无活动" color="var(--ink-secondary)" size="large" :animated="false" />
      <p class="empty-hint">敬请期待新活动上线</p>
    </div>

    <div v-if="activityStore.total > pageSize" class="pagination paper-card">
      <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
      <div class="page-buttons">
        <InkButton :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">上一页</InkButton>
        <InkButton :disabled="currentPage >= totalPages" @click="changePage(currentPage + 1)">下一页</InkButton>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useActivityStore } from '@/stores/activity'
import NewspaperTitle from '@/components/common/NewspaperTitle.vue'
import LotteryCard from '@/components/business/LotteryCard.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import Stamp from '@/components/common/Stamp.vue'
import InkButton from '@/components/common/InkButton.vue'

const router = useRouter()
const activityStore = useActivityStore()

const currentPage = ref(1)
const pageSize = ref(10)
const error = ref(null)

const totalPages = computed(() => Math.ceil(activityStore.total / pageSize.value))

onMounted(() => {
  fetchActivities()
})

async function fetchActivities() {
  error.value = null
  try {
    await activityStore.fetchActivities(currentPage.value, pageSize.value)
  } catch (err) {
    error.value = err.message || '加载活动列表失败'
  }
}

function goToDetail(activity) {
  router.push(`/activity/${activity.activityId}`)
}

async function changePage(page) {
  currentPage.value = page
  await fetchActivities()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<style scoped>
.activity-list-view {
  max-width: 1000px;
  margin: 0 auto;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-top: 24px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  margin-top: 24px;
}

.empty-hint {
  margin-top: 16px;
  color: var(--ink-secondary);
  font-size: 14px;
}

.error-state {
  text-align: center;
  padding: 60px 20px;
  margin-top: 24px;
}

.error-hint {
  margin-top: 16px;
  color: var(--stamp-red);
  font-size: 14px;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 32px;
  padding: 16px 24px;
}

.page-info {
  color: var(--ink-secondary);
  font-size: 14px;
}

.page-buttons {
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .activity-grid {
    grid-template-columns: 1fr;
  }

  .pagination {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
