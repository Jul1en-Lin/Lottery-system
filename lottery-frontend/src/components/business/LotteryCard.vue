<template>
  <div class="lottery-card paper-card card-hover-lift" @click="handleClick">
    <div class="card-header">
      <h3 class="activity-name">{{ activity.activityName }}</h3>
      <p class="activity-subtitle">{{ englishSubtitle }}</p>
    </div>

    <Divider />

    <div class="card-body">
      <p class="activity-desc">{{ activity.description }}</p>

      <div class="activity-meta">
        <div class="meta-item">
          <span class="meta-label">剩余时间</span>
          <span class="meta-value" :class="timeClass">{{ remainingTime }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">奖品数量</span>
          <span class="meta-value">{{ prizeCount }}</span>
        </div>
      </div>
    </div>

    <div class="card-footer">
      <Stamp
        v-if="activity.valid"
        text="进行中"
        color="#C41E3A"
        size="small"
        :animated="false"
      />
      <Stamp
        v-else
        text="已结束"
        color="#4A4A4A"
        size="small"
        :animated="false"
      />

      <InkButton
        v-if="showAction"
        :disabled="!activity.valid"
        :lottery="activity.valid"
        @click.stop="handleAction"
      >
        参与抽奖
      </InkButton>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import Stamp from '@/components/common/Stamp.vue'
import InkButton from '@/components/common/InkButton.vue'
import Divider from '@/components/common/Divider.vue'

const props = defineProps({
  activity: {
    type: Object,
    required: true
  },
  showAction: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['click', 'action'])

const englishSubtitle = computed(() => {
  return `ACTIVITY NO.${String(props.activity.activityId).padStart(3, '0')}`
})

const prizeCount = computed(() => {
  return props.activity.prizeCount || 0
})

const remainingTime = computed(() => {
  if (!props.activity.endTime) return '未知'

  const now = new Date()
  const end = new Date(props.activity.endTime)

  if (end <= now) return '已结束'

  const diff = end - now
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))

  if (days > 0) return `${days}天${hours}小时`
  return `${hours}小时`
})

const timeClass = computed(() => {
  if (!props.activity.endTime) return ''
  const now = new Date()
  const end = new Date(props.activity.endTime)
  const diff = end - now
  const hours = diff / (1000 * 60 * 60)

  if (hours < 24) return 'urgent'
  if (hours < 72) return 'warning'
  return ''
})

function handleClick() {
  emit('click', props.activity)
}

function handleAction() {
  emit('action', props.activity)
}
</script>

<style scoped>
.lottery-card {
  cursor: pointer;
  padding: 16px;
  position: relative;
  overflow: hidden;
}

.lottery-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s ease;
  box-shadow: inset 0 0 0 2px rgba(26, 26, 26, 0.1);
}

.lottery-card:hover::after {
  opacity: 1;
}

.card-header {
  text-align: center;
}

.activity-name {
  font-size: 20px;
  letter-spacing: 4px;
  margin: 0 0 4px 0;
  transition: letter-spacing 0.3s ease;
}

.lottery-card:hover .activity-name {
  letter-spacing: 6px;
}

.activity-subtitle {
  font-size: 12px;
  color: var(--ink-secondary);
  letter-spacing: 2px;
  margin: 0;
  text-transform: uppercase;
}

.card-body {
  margin: 12px 0;
}

.activity-desc {
  color: var(--ink-secondary);
  font-size: 14px;
  margin: 0 0 12px 0;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.meta-label {
  font-size: 12px;
  color: var(--ink-secondary);
  margin-bottom: 4px;
}

.meta-value {
  font-size: 14px;
  font-weight: bold;
}

.meta-value.urgent {
  color: var(--stamp-red);
  animation: urgent-pulse 1s ease-in-out infinite;
}

@keyframes urgent-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.meta-value.warning {
  color: #B8860B;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--divider-color);
}
</style>
