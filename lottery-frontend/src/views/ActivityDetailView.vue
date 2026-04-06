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
        <PrizeList :prizes="activity?.activityPrizeList || []" />
      </div>

      <div class="draw-section paper-card">
        <LoadingSpinner v-if="isDrawing" text="抽奖中" />
        <template v-else-if="drawResult">
          <ScratchArea
            :result="drawResult"
            @reveal="handleReveal"
          />
        </template>
        <div v-else class="scratch-placeholder">
          <p>[ 刮 开 此 处 查 看 中 奖 结 果 ]</p>
        </div>
        <div class="draw-action" v-if="!drawResult">
          <InkButton primary lottery @click="handleDraw" :disabled="isDrawing || !isActivityValid">
            {{ isActivityValid ? '确认抽奖' : '活动已结束' }}
          </InkButton>
          <p v-if="!isActivityValid" class="draw-hint">该活动已结束，无法参与抽奖</p>
        </div>
        <div v-else class="draw-again">
          <InkButton @click="resetDraw">重新抽奖</InkButton>
        </div>
      </div>
    </div>

    <LoadingSpinner v-else-if="activityStore.loading" text="加载活动" />

    <div v-else class="error-state">
      <Stamp text="活动不存在" color="var(--ink-secondary)" size="large" :animated="false" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useActivityStore } from '@/stores/activity'
import NewspaperTitle from '@/components/common/NewspaperTitle.vue'
import InkButton from '@/components/common/InkButton.vue'
import Divider from '@/components/common/Divider.vue'
import Stamp from '@/components/common/Stamp.vue'
import ScratchArea from '@/components/business/ScratchArea.vue'
import PrizeList from '@/components/business/PrizeList.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import { prizeApi } from '@/api/modules/prize'

const route = useRoute()
const activityStore = useActivityStore()

const activity = computed(() => activityStore.currentActivity)
const isDrawing = ref(false)
const drawResult = ref(null)

// 活动有效性判断：只有当 valid 明确为 false 时才认为活动已结束
const isActivityValid = computed(() => {
  if (!activity.value) return false
  return activity.value.valid !== false
})

onMounted(() => {
  const id = route.params.id
  activityStore.fetchActivityDetail(id)
})

onUnmounted(() => {
  activityStore.clearCurrentActivity()
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

async function handleDraw() {
  if (isDrawing.value || !isActivityValid.value) return

  isDrawing.value = true
  try {
    const res = await prizeApi.drawPrize({ activityId: activity.value.activityId })
    const data = res.data

    if (data && data.prizeName) {
      drawResult.value = {
        won: true,
        prizeName: data.prizeName,
        message: `恭喜您获得 ${data.prizeName}！`
      }
    } else {
      drawResult.value = {
        won: false,
        prizeName: null,
        message: '感谢您的参与，下次好运！'
      }
    }
  } catch (error) {
    console.error('Draw prize failed:', error)
    drawResult.value = {
      won: false,
      prizeName: null,
      message: '抽奖失败，请稍后重试'
    }
  } finally {
    isDrawing.value = false
  }
}

function handleReveal(result) {
  // Result revealed callback - can be used for analytics or tracking
}

function resetDraw() {
  drawResult.value = null
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

.draw-section {
  text-align: center;
  padding: 32px;
  margin-top: 24px;
  min-height: 300px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.scratch-placeholder {
  border: 2px dashed var(--border-color);
  padding: 24px;
  margin-bottom: 16px;
  background-color: var(--paper-bg-dark);
  width: 100%;
  max-width: 400px;
}

.draw-action {
  margin-top: 16px;
}

.draw-hint {
  margin-top: 12px;
  font-size: 12px;
  color: var(--ink-secondary);
}

.draw-again {
  margin-top: 16px;
}

.error-state {
  text-align: center;
  padding: 60px 20px;
}
</style>
