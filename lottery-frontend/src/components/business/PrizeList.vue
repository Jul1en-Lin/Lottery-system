<template>
  <div class="prize-list">
    <div v-if="loading" class="loading-container">
      <div class="loading-text">加载奖品清单...</div>
    </div>

    <div v-else-if="prizes && prizes.length > 0" class="prize-grid">
      <div
        v-for="prize in prizes"
        :key="prize.prizeId"
        class="prize-item paper-card"
      >
        <div class="prize-tier-badge">{{ getTierName(prize.prizeTiers) }}</div>
        <div class="prize-info">
          <h4 class="prize-name">{{ prize.prizeName }}</h4>
          <div class="prize-meta">
            <span class="prize-amount">数量: {{ prize.prizeAmount }}</span>
            <span v-if="prize.probability" class="prize-probability">
              中奖率: {{ formatProbability(prize.probability) }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-container">
      <Stamp text="暂无奖品" color="var(--ink-secondary)" :animated="false" />
    </div>
  </div>
</template>

<script setup>
import Stamp from '@/components/common/Stamp.vue'
import { getTierName } from '@/utils/index.js'

const props = defineProps({
  prizes: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

/**
 * Format probability for display.
 * Expected input format: probability as a percentage value (e.g., 0.5 means 0.5%, 50 means 50%)
 * Values >= 1 are treated as direct percentages, values < 1 are multiplied by 100
 */
function formatProbability(prob) {
  if (prob >= 1) {
    return `${prob.toFixed(1)}%`
  }
  return `${(prob * 100).toFixed(2)}%`
}
</script>

<style scoped>
.prize-list {
  width: 100%;
}

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.loading-text {
  font-size: 16px;
  color: var(--ink-secondary);
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.prize-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.prize-item {
  position: relative;
  padding: 20px 16px;
  text-align: center;
  transition: transform 0.2s ease;
}

.prize-item:hover {
  transform: translateY(-4px);
}

.prize-tier-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background-color: var(--stamp-red);
  color: var(--paper-bg);
  padding: 4px 8px;
  font-size: 12px;
  font-weight: bold;
  letter-spacing: 1px;
}

.prize-info {
  text-align: center;
}

.prize-name {
  font-size: 18px;
  margin: 0 0 12px 0;
  letter-spacing: 2px;
}

.prize-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
  color: var(--ink-secondary);
}

.prize-amount,
.prize-probability {
  display: block;
}
</style>
