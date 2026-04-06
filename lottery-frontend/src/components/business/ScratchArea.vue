<template>
  <div class="scratch-area" :class="{ revealed: isRevealed }">
    <div class="scratch-canvas-container">
      <canvas
        ref="canvasRef"
        class="scratch-canvas"
        @mousedown="startScratch"
        @mousemove="scratch"
        @mouseup="endScratch"
        @mouseleave="endScratch"
        @touchstart.prevent="startScratch"
        @touchmove.prevent="scratch"
        @touchend="endScratch"
      ></canvas>

      <div class="result-container" :class="{ show: isRevealed }">
        <div v-if="result" class="result-content scratch-reveal" :class="{ 'prize-glow': result.won }">
          <Stamp
            v-if="result.won"
            :text="result.prizeName || '恭喜中奖'"
            color="#C41E3A"
            size="large"
            :animated="true"
          />
          <Stamp
            v-else
            text="谢谢参与"
            color="#4A4A4A"
            size="large"
            :animated="true"
          />
          <p class="result-message">{{ result.message }}</p>
        </div>
      </div>
    </div>

    <p class="scratch-hint" v-if="!isRevealed">
      刮开此处查看中奖结果
    </p>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import Stamp from '@/components/common/Stamp.vue'

const props = defineProps({
  result: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['reveal'])

const canvasRef = ref(null)
const isScratching = ref(false)
const isRevealed = ref(false)
const ctx = ref(null)
const scratchPercentage = ref(0)

let canvasWidth = 0
let canvasHeight = 0
let lastPercentageCheck = 0
const PERCENTAGE_CHECK_INTERVAL = 100 // Throttle percentage check to every 100ms

onMounted(() => {
  initCanvas()
  window.addEventListener('resize', initCanvas)
})

onUnmounted(() => {
  window.removeEventListener('resize', initCanvas)
})

watch(() => props.result, (newResult) => {
  if (newResult) {
    nextTick(() => {
      initCanvas()
    })
  }
})

function initCanvas() {
  const canvas = canvasRef.value
  if (!canvas) return

  const container = canvas.parentElement
  if (!container) return

  canvasWidth = container.offsetWidth
  canvasHeight = container.offsetHeight

  // Handle case where container has no dimensions
  if (canvasWidth === 0 || canvasHeight === 0) {
    console.warn('ScratchArea: Canvas container has zero dimensions')
    return
  }

  canvas.width = canvasWidth
  canvas.height = canvasHeight

  ctx.value = canvas.getContext('2d')

  drawCoverLayer()
}

function drawCoverLayer() {
  const ctxVal = ctx.value
  if (!ctxVal) return

  ctxVal.fillStyle = '#EBE6D7'
  ctxVal.fillRect(0, 0, canvasWidth, canvasHeight)

  ctxVal.fillStyle = '#C9C4B8'
  ctxVal.font = 'bold 24px serif'
  ctxVal.textAlign = 'center'
  ctxVal.textBaseline = 'middle'
  ctxVal.fillText('刮 开 此 处', canvasWidth / 2, canvasHeight / 2)

  for (let i = 0; i < 50; i++) {
    const x = Math.random() * canvasWidth
    const y = Math.random() * canvasHeight
    ctxVal.fillStyle = `rgba(139, 139, 139, ${Math.random() * 0.3})`
    ctxVal.beginPath()
    ctxVal.arc(x, y, Math.random() * 3, 0, Math.PI * 2)
    ctxVal.fill()
  }
}

function startScratch(e) {
  if (isRevealed.value) return
  isScratching.value = true
  scratch(e)
}

function scratch(e) {
  if (!isScratching.value || isRevealed.value) return

  const canvas = canvasRef.value
  const ctxVal = ctx.value
  if (!canvas || !ctxVal) return

  const rect = canvas.getBoundingClientRect()
  let x, y

  if (e.touches) {
    x = e.touches[0].clientX - rect.left
    y = e.touches[0].clientY - rect.top
  } else {
    x = e.clientX - rect.left
    y = e.clientY - rect.top
  }

  ctxVal.globalCompositeOperation = 'destination-out'
  ctxVal.beginPath()
  ctxVal.arc(x, y, 25, 0, Math.PI * 2)
  ctxVal.fill()

  // Throttle percentage check for performance
  const now = Date.now()
  if (now - lastPercentageCheck >= PERCENTAGE_CHECK_INTERVAL) {
    lastPercentageCheck = now
    checkScratchPercentage()
  }
}

function endScratch() {
  isScratching.value = false
  // Check percentage when scratch ends to ensure reveal triggers
  if (!isRevealed.value) {
    checkScratchPercentage()
  }
}

function checkScratchPercentage() {
  const canvas = canvasRef.value
  const ctxVal = ctx.value
  if (!canvas || !ctxVal) return

  const imageData = ctxVal.getImageData(0, 0, canvasWidth, canvasHeight)
  const pixels = imageData.data
  let transparentPixels = 0

  for (let i = 3; i < pixels.length; i += 4) {
    if (pixels[i] === 0) {
      transparentPixels++
    }
  }

  scratchPercentage.value = (transparentPixels / (pixels.length / 4)) * 100

  if (scratchPercentage.value >= 50 && !isRevealed.value) {
    revealResult()
  }
}

function revealResult() {
  isRevealed.value = true

  const canvas = canvasRef.value
  if (canvas) {
    canvas.style.transition = 'opacity 0.5s ease'
    canvas.style.opacity = '0'
  }

  emit('reveal', props.result)
}
</script>

<style scoped>
.scratch-area {
  width: 100%;
  text-align: center;
}

.scratch-canvas-container {
  position: relative;
  width: 100%;
  height: 200px;
  border: 3px double var(--ink-primary);
  background-color: var(--paper-bg);
  overflow: hidden;
}

.scratch-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  cursor: crosshair;
  touch-action: none;
}

.result-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--paper-bg);
  opacity: 0;
  transition: opacity 0.5s ease;
}

.result-container.show {
  opacity: 1;
}

.result-content {
  text-align: center;
}

.result-message {
  margin-top: 16px;
  font-size: 14px;
  color: var(--ink-secondary);
}

.scratch-hint {
  margin-top: 12px;
  font-size: 14px;
  color: var(--ink-secondary);
  letter-spacing: 2px;
}

.scratch-area.revealed .scratch-hint {
  display: none;
}
</style>
