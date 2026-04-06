<template>
  <div class="activity-list-view fade-in">
    <NewspaperTitle
      title="活动版面"
      subtitle="ACTIVITIES - 第 A02 版"
    />

    <div class="activity-grid">
      <div
        v-for="activity in activityStore.activities"
        :key="activity.activityId"
        class="activity-card paper-card paper-shake"
        @click="goToDetail(activity.activityId)"
      >
        <h3 class="activity-name">{{ activity.activityName }}</h3>
        <p class="activity-desc">{{ activity.description }}</p>
        <Divider />
        <div class="activity-status">
          <span :class="['status-badge', activity.valid ? 'active' : 'inactive']">
            {{ activity.valid ? '进行中' : '已结束' }}
          </span>
        </div>
        <InkButton @click.stop="goToDetail(activity.activityId)">参与抽奖</InkButton>
      </div>
    </div>

    <div v-if="activityStore.loading" class="loading">
      加载中...
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useActivityStore } from '@/stores/activity'
import NewspaperTitle from '@/components/common/NewspaperTitle.vue'
import InkButton from '@/components/common/InkButton.vue'
import Divider from '@/components/common/Divider.vue'

const router = useRouter()
const activityStore = useActivityStore()

onMounted(() => {
  activityStore.fetchActivities()
})

function goToDetail(id) {
  router.push(`/activity/${id}`)
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

.activity-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.activity-name {
  font-size: 20px;
  letter-spacing: 4px;
  margin: 0 0 8px 0;
}

.activity-desc {
  color: var(--ink-secondary);
  font-size: 14px;
  margin: 0;
}

.activity-status {
  margin: 12px 0;
}

.status-badge {
  padding: 4px 12px;
  font-size: 12px;
  border: 1px solid;
}

.status-badge.active {
  color: var(--stamp-red);
  border-color: var(--stamp-red);
}

.status-badge.inactive {
  color: var(--ink-secondary);
  border-color: var(--ink-secondary);
}

.loading {
  text-align: center;
  padding: 40px;
  color: var(--ink-secondary);
}
</style>
