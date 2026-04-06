<template>
  <div class="newspaper-title">
    <h1 class="headline" :class="sizeClass">{{ title }}</h1>
    <p v-if="subtitle" class="english-subtitle">{{ subtitle }}</p>
    <div v-if="showDate" class="title-date">
      {{ formattedDate }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'medium'
  },
  showDate: {
    type: Boolean,
    default: false
  }
})

const sizeClass = computed(() => {
  const map = {
    small: 'subtitle',
    medium: '',
    large: 'large'
  }
  return map[props.size] || ''
})

const formattedDate = computed(() => {
  const now = new Date()
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${weekdays[now.getDay()]}`
})
</script>

<style scoped>
.newspaper-title {
  text-align: center;
  padding: 16px 0;
  border-bottom: 3px double var(--ink-primary);
  margin-bottom: 16px;
}

.headline {
  font-size: 48px;
  font-weight: bold;
  letter-spacing: 8px;
  margin: 0;
}

.headline.large {
  font-size: 64px;
  letter-spacing: 12px;
}

.headline.subtitle {
  font-size: 24px;
  letter-spacing: 4px;
}

.title-date {
  margin-top: 8px;
  font-size: 14px;
  color: var(--ink-secondary);
}
</style>
