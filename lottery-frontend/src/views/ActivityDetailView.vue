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
        <PrizeList :prizes="activity?.activityPrizeList || []" />
      </div>

      <div class="draw-section paper-card">
        <!-- 活动已结束：显示中奖名单 -->
        <template v-if="activity.status !== 'START'">
          <div class="winners-section">
            <h3 class="section-title">中 奖 名 单</h3>
            <Divider type="double" />
            <div v-if="loadingWinners" class="loading-winners">
              <LoadingSpinner text="加载中奖名单" />
            </div>
            <div v-else-if="winnersList.length > 0" class="winners-list">
              <div v-for="(record, index) in winnersList" :key="index" class="winner-item">
                <div class="winner-info">
                  <span class="winner-prize">{{ record.prizeName }}</span>
                  <span class="winner-tier" v-if="record.prizeTier">{{ getTierName(record.prizeTier) }}</span>
                </div>
                <div class="winner-name">
                  <Stamp text="中奖" color="#C41E3A" size="small" :animated="false" />
                  <span class="name">{{ record.winnerName }}</span>
                </div>
                <div class="winner-time">{{ formatTime(record.winningTime) }}</div>
              </div>
            </div>
            <div v-else class="no-winners">
              <Stamp text="暂无中奖记录" color="var(--ink-secondary)" size="medium" :animated="false" />
            </div>
          </div>
          <div class="draw-action">
            <InkButton disabled>活动已结束</InkButton>
            <p class="draw-hint">该活动已结束，以下是最终中奖名单</p>
          </div>
        </template>

        <!-- 活动进行中：显示刮奖区域 -->
        <template v-else>
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
            <InkButton primary lottery @click="handleDraw" :disabled="isDrawing">
              确认抽奖
            </InkButton>
          </div>
          <div v-else class="draw-again">
            <InkButton @click="resetDraw">重新抽奖</InkButton>
          </div>
        </template>
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
import { ElMessage } from 'element-plus'
import { getTierName } from '@/utils/index.js'

const route = useRoute()
const activityStore = useActivityStore()

const activity = computed(() => activityStore.currentActivity)
const isDrawing = ref(false)
const drawResult = ref(null)
const winnersList = ref([])
const loadingWinners = ref(false)

onMounted(() => {
  const id = route.params.id
  activityStore.fetchActivityDetail(id)
  fetchWinningRecords(id)
})

onUnmounted(() => {
  activityStore.clearCurrentActivity()
})

/**
 * 获取中奖记录
 */
async function fetchWinningRecords(activityId) {
  loadingWinners.value = true
  try {
    const res = await prizeApi.getWinningRecords({ activityId: Number(activityId) })
    winnersList.value = res.data || []
  } catch (error) {
    console.error('Failed to fetch winning records:', error)
    winnersList.value = []
  } finally {
    loadingWinners.value = false
  }
}

/**
 * 格式化时间
 */
function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * 执行随机抽奖
 * 1. 从可用奖品中随机选择一个
 * 2. 从参与者中随机选择中奖者
 * 3. 构造请求发送给后端
 */
async function handleDraw() {
  if (isDrawing.value || activity.value?.status !== 'START') return

  const prizes = activity.value.activityPrizeList || []
  const participants = activity.value.activityUserList || []

  // 检查是否有可用的奖品
  const availablePrizes = prizes.filter(p => p.prizeStatus === 'INIT')
  if (availablePrizes.length === 0) {
    ElMessage.warning('没有可抽取的奖品')
    return
  }

  // 检查是否有参与者
  const availableParticipants = participants.filter(u => u.userStatus === 'INIT')
  if (availableParticipants.length === 0) {
    ElMessage.warning('没有可参与的抽奖人员')
    return
  }

  isDrawing.value = true

  try {
    // 1. 随机选择一个奖品
    const selectedPrize = availablePrizes[Math.floor(Math.random() * availablePrizes.length)]

    // 2. 根据奖品数量随机选择中奖者
    const winnerCount = Math.min(selectedPrize.prizeAmount, availableParticipants.length)
    const winners = shuffleArray([...availableParticipants]).slice(0, winnerCount)

    // 3. 构造请求
    const drawRequest = {
      activityId: activity.value.activityId,
      prizeId: selectedPrize.prizeId,
      winningTime: new Date().toISOString(),
      prizeTiers: selectedPrize.prizeTiers,
      winnerList: winners.map(w => ({
        userId: w.userId,
        userName: w.userName
      }))
    }

    // 4. 发送请求
    const res = await prizeApi.drawPrize(drawRequest)

    // 5. 显示结果
    drawResult.value = {
      won: true,
      prizeName: selectedPrize.prizeName,
      prizeTiers: selectedPrize.prizeTiers,
      winners: winners.map(w => w.userName),
      message: `恭喜：${winners.map(w => w.userName).join('、')} 获得 ${selectedPrize.prizeName}！`
    }

    // 刷新活动详情以更新状态
    activityStore.fetchActivityDetail(route.params.id)

  } catch (error) {
    console.error('Draw prize failed:', error)
    ElMessage.error('抽奖失败，请稍后重试')
  } finally {
    isDrawing.value = false
  }
}

/**
 * 数组随机打乱（Fisher-Yates 洗牌算法）
 */
function shuffleArray(array) {
  for (let i = array.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [array[i], array[j]] = [array[j], array[i]]
  }
  return array
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

/* 中奖名单样式 */
.winners-section {
  width: 100%;
  max-width: 600px;
}

.loading-winners {
  padding: 40px 0;
}

.winners-list {
  margin-top: 16px;
  text-align: left;
}

.winner-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  margin-bottom: 8px;
  background-color: var(--paper-bg-dark);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  flex-wrap: wrap;
  gap: 8px;
}

.winner-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 120px;
}

.winner-prize {
  font-weight: bold;
  color: var(--ink-primary);
}

.winner-tier {
  font-size: 12px;
  color: var(--ink-secondary);
  background-color: var(--paper-bg);
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid var(--border-color);
}

.winner-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.winner-name .name {
  font-size: 16px;
  font-weight: bold;
  color: var(--accent-color, #C41E3A);
}

.winner-time {
  font-size: 12px;
  color: var(--ink-secondary);
  font-style: italic;
}

.no-winners {
  padding: 40px 0;
  text-align: center;
}
</style>
