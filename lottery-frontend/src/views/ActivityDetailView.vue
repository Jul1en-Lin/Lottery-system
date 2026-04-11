<template>
  <div class="activity-detail-view fade-in">
    <NewspaperTitle
      :title="activity?.activityName || '活动详情'"
      subtitle="ACTIVITY DETAIL"
    />

    <div v-if="activity" class="detail-content">
      <div class="info-section paper-card double-border">
        <p class="participants-label"><strong>参与人员：</strong></p>
        <div class="participants-list">
          <span
            v-for="user in activity.activityUserList"
            :key="user.userId"
            class="participant-tag"
          >
            {{ user.userName }}
          </span>
          <span v-if="!activity.activityUserList?.length" class="no-participants">暂无参与人员</span>
        </div>
      </div>

      <div class="prize-section">
        <h3 class="section-title">奖 品 清 单</h3>
        <Divider type="double" />
        <PrizeList :prizes="enrichedPrizes.length > 0 ? enrichedPrizes : (activity?.activityPrizeList || [])" />
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
          <InkButton primary lottery @click="handleDraw" :disabled="isDrawing || activity.status !== 'START'">
            {{ activity.status === 'START' ? '确认抽奖' : '活动已结束' }}
          </InkButton>
          <p v-if="activity.status !== 'START'" class="draw-hint">该活动已结束，无法参与抽奖</p>
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
import { activityApi } from '@/api/modules/activity'

const route = useRoute()
const activityStore = useActivityStore()

const activity = computed(() => activityStore.currentActivity)
const isDrawing = ref(false)
const drawResult = ref(null)
const enrichedPrizes = ref([])

onMounted(async () => {
  const id = route.params.id
  await activityStore.fetchActivityDetail(id)
  // 补充奖品图片信息（后端返回的数据可能缺少图片URL）
  await enrichPrizeImages()
})

/**
 * 补充奖品图片信息
 * 后端 getActivityDetail 返回的奖品数据缺少 imageUrl，需要单独获取奖品列表来补充
 */
async function enrichPrizeImages() {
  if (!activity.value?.activityPrizeList?.length) return

  try {
    // 获取所有奖品列表
    const res = await prizeApi.getPrizeList(1, 100)
    const allPrizes = res.data?.records || res.data || []

    // 创建奖品ID到图片URL的映射
    const prizeImageMap = {}
    allPrizes.forEach(prize => {
      if (prize.prizeId && prize.imageUrl) {
        prizeImageMap[prize.prizeId] = prize.imageUrl
      }
    })

    // 为活动奖品补充图片URL
    enrichedPrizes.value = activity.value.activityPrizeList.map(prize => ({
      ...prize,
      imageUrl: prize.imageUrl || prizeImageMap[prize.prizeId] || ''
    }))
  } catch (error) {
    console.error('获取奖品图片失败:', error)
    enrichedPrizes.value = activity.value.activityPrizeList
  }
}

onUnmounted(() => {
  activityStore.clearCurrentActivity()
})

async function handleDraw() {
  if (isDrawing.value || !activity.value?.valid) return

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

.participants-label {
  margin-bottom: 12px;
  font-size: 16px;
}

.participants-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.participant-tag {
  display: inline-block;
  padding: 4px 12px;
  background-color: var(--paper-bg-dark);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 14px;
  color: var(--ink-primary);
}

.no-participants {
  font-size: 14px;
  color: var(--ink-secondary);
  font-style: italic;
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
