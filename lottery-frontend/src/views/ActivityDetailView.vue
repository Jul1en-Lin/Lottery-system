<template>
  <div class="activity-detail-view fade-in">
    <NewspaperTitle
      :title="activity?.activityName || '活动详情'"
      subtitle="ACTIVITY DETAIL"
    />

    <div v-if="activity" class="detail-content">
      <div class="info-section paper-card double-border">
        <p><strong>活动时间：</strong>{{ formatDate(activity.startTime) }} - {{ formatDate(activity.endTime) }}</p>
        <p><strong>参与条件：</strong>注册用户均可参与</p>
      </div>

      <div class="prize-section">
        <h3 class="section-title">奖 品 清 单</h3>
        <Divider type="double" />

        <div class="prize-grid">
          <div
            v-for="prize in activity.activityPrizeList"
            :key="prize.prizeId"
            class="prize-card paper-card"
          >
            <img v-if="prize.imageUrl" :src="prize.imageUrl" :alt="prize.prizeName" class="prize-image" />
            <h4 class="prize-name">{{ prize.prizeName }}</h4>
            <p class="prize-tier">等级: {{ prize.prizeTiers }}</p>
            <p class="prize-amount">数量: {{ prize.prizeAmount }}</p>
          </div>
        </div>
      </div>

      <div class="draw-section paper-card">
        <div class="scratch-area">
          <p>[ 刮 开 此 处 查 看 中 奖 结 果 ]</p>
        </div>
        <div class="draw-action">
          <InkButton primary @click="handleDraw">确认抽奖</InkButton>
        </div>
      </div>
    </div>

    <div v-else-if="activityStore.loading" class="loading">
      加载中...
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useActivityStore } from '@/stores/activity'
import NewspaperTitle from '@/components/common/NewspaperTitle.vue'
import InkButton from '@/components/common/InkButton.vue'
import Divider from '@/components/common/Divider.vue'

const route = useRoute()
const activityStore = useActivityStore()

const activity = computed(() => activityStore.currentActivity)

onMounted(() => {
  const id = route.params.id
  activityStore.fetchActivityDetail(id)
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

function handleDraw() {
  // TODO: 实现抽奖逻辑
  alert('抽奖功能开发中...')
}
</script>

<style scoped>
.activity-detail-view {
  max-width: 900px;
  margin: 0 auto;
}

.detail-content {
  margin-top: 24px;
}

.info-section {
  margin-bottom: 24px;
}

.info-section p {
  margin: 8px 0;
}

.section-title {
  font-size: 20px;
  letter-spacing: 4px;
  margin: 0;
}

.prize-section {
  margin: 24px 0;
}

.prize-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 16px;
}

.prize-card {
  text-align: center;
  padding: 16px;
}

.prize-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  margin-bottom: 8px;
}

.prize-name {
  font-size: 16px;
  margin: 0 0 8px 0;
}

.prize-tier,
.prize-amount {
  font-size: 12px;
  color: var(--ink-secondary);
  margin: 4px 0;
}

.draw-section {
  text-align: center;
  padding: 32px;
  margin-top: 24px;
}

.scratch-area {
  border: 2px dashed var(--border-color);
  padding: 24px;
  margin-bottom: 16px;
  background-color: var(--paper-bg-dark);
}

.loading {
  text-align: center;
  padding: 40px;
  color: var(--ink-secondary);
}
</style>
