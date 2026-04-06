<template>
  <div class="prize-record-view fade-in">
    <NewspaperTitle
      title="中奖公告"
      subtitle="WINNING ANNOUNCEMENTS"
    />

    <div class="record-list paper-card">
      <div v-for="record in records" :key="record.winnerId" class="record-item">
        <Stamp text="中奖" size="small" />
        <div class="record-info">
          <p class="winner-name">{{ record.winnerName }}</p>
          <p class="prize-name">{{ record.prizeName }} ({{ record.prizeTier }})</p>
          <p class="win-time">{{ formatDate(record.winningTime) }}</p>
        </div>
      </div>

      <div v-if="records.length === 0" class="empty-state">
        暂无中奖记录
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { prizeApi } from '@/api/modules/prize'
import NewspaperTitle from '@/components/common/NewspaperTitle.vue'
import Stamp from '@/components/common/Stamp.vue'

const records = ref([])

onMounted(async () => {
  try {
    const res = await prizeApi.getWinningRecords({})
    records.value = res.data || []
  } catch (error) {
    console.error('获取中奖记录失败', error)
  }
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<style scoped>
.prize-record-view {
  max-width: 800px;
  margin: 0 auto;
}

.record-list {
  margin-top: 24px;
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

.winner-name {
  font-weight: bold;
  margin: 0 0 4px 0;
}

.prize-name {
  color: var(--stamp-red);
  margin: 0 0 4px 0;
}

.win-time {
  font-size: 12px;
  color: var(--ink-secondary);
  margin: 0;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: var(--ink-secondary);
}
</style>
