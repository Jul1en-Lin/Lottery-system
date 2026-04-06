<template>
  <div class="loading-spinner" :class="{ 'full-screen': fullScreen }">
    <div class="spinner-content" :style="contentStyle">
      <!-- 印刷滚筒动画 -->
      <div class="print-roller-container" :style="rollerStyle">
        <div class="print-roller">
          <div class="roller-dots">
            <span v-for="i in 8" :key="i" class="dot" :style="dotStyle"></span>
          </div>
        </div>
      </div>

      <!-- 加载文字 -->
      <p v-if="showText" class="loading-text" :style="textStyle">
        <span class="text-content">{{ text }}</span>
        <span class="loading-dots">
          <span class="dot-1">.</span>
          <span class="dot-2">.</span>
          <span class="dot-3">.</span>
        </span>
      </p>

      <!-- 进度条（可选） -->
      <div v-if="showProgress" class="progress-bar">
        <div class="progress-fill" :style="{ width: `${progress}%` }"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  size: {
    type: String,
    default: 'medium', // small, medium, large
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  },
  color: {
    type: String,
    default: 'var(--ink-primary)'
  },
  text: {
    type: String,
    default: '正在印刷'
  },
  showText: {
    type: Boolean,
    default: true
  },
  showProgress: {
    type: Boolean,
    default: false
  },
  progress: {
    type: Number,
    default: 0
  },
  fullScreen: {
    type: Boolean,
    default: false
  }
})

const sizeMap = {
  small: { roller: 30, text: 12, gap: 8 },
  medium: { roller: 50, text: 14, gap: 12 },
  large: { roller: 70, text: 16, gap: 16 }
}

const currentSize = computed(() => sizeMap[props.size])

const contentStyle = computed(() => ({
  gap: `${currentSize.value.gap}px`
}))

const rollerStyle = computed(() => ({
  width: `${currentSize.value.roller}px`,
  height: `${currentSize.value.roller}px`
}))

const textStyle = computed(() => ({
  fontSize: `${currentSize.value.text}px`
}))

const dotStyle = computed(() => ({
  backgroundColor: props.color
}))
</script>

<style scoped>
.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.loading-spinner.full-screen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(245, 240, 225, 0.9);
  z-index: 9999;
}

.spinner-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* 印刷滚筒 */
.print-roller-container {
  position: relative;
}

.print-roller {
  width: 100%;
  height: 100%;
  border: 3px solid var(--ink-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: print-roller 1.5s linear infinite;
}

.roller-dots {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 3px;
  width: 60%;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--ink-primary);
  animation: dot-pulse 1.5s ease-in-out infinite;
}

.dot:nth-child(1) { animation-delay: 0s; }
.dot:nth-child(2) { animation-delay: 0.1s; }
.dot:nth-child(3) { animation-delay: 0.2s; }
.dot:nth-child(4) { animation-delay: 0.3s; }
.dot:nth-child(5) { animation-delay: 0.4s; }
.dot:nth-child(6) { animation-delay: 0.5s; }
.dot:nth-child(7) { animation-delay: 0.6s; }
.dot:nth-child(8) { animation-delay: 0.7s; }

/* 加载文字 */
.loading-text {
  margin: 0;
  color: var(--ink-primary);
  font-weight: bold;
  letter-spacing: 2px;
  display: flex;
  align-items: center;
}

.text-content {
  margin-right: 2px;
}

.loading-dots span {
  animation: dot-blink 1.5s ease-in-out infinite;
}

.dot-1 { animation-delay: 0s; }
.dot-2 { animation-delay: 0.3s; }
.dot-3 { animation-delay: 0.6s; }

@keyframes dot-blink {
  0%, 20% { opacity: 0; }
  50% { opacity: 1; }
  80%, 100% { opacity: 0; }
}

/* 进度条 */
.progress-bar {
  width: 100%;
  max-width: 200px;
  height: 4px;
  background-color: var(--paper-bg-dark);
  border: 1px solid var(--border-color);
  margin-top: 8px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: repeating-linear-gradient(
    90deg,
    var(--ink-primary),
    var(--ink-primary) 4px,
    var(--paper-bg-dark) 4px,
    var(--paper-bg-dark) 8px
  );
  transition: width 0.3s ease;
  animation: progress-stripe 1s linear infinite;
  background-size: 200% 100%;
}

@keyframes progress-stripe {
  0% { background-position: 0 0; }
  100% { background-position: 8px 0; }
}
</style>
