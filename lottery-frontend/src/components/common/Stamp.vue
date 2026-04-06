<template>
  <div class="stamp" :class="stampClass" :style="stampStyle">
    <span class="stamp-text">{{ text }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  text: {
    type: String,
    required: true
  },
  color: {
    type: String,
    default: 'var(--stamp-red)'
  },
  size: {
    type: String,
    default: 'medium'
  },
  animated: {
    type: Boolean,
    default: true
  },
  rotation: {
    type: Number,
    default: -5
  },
  inkEffect: {
    type: Boolean,
    default: false
  }
})

const stampClass = computed(() => {
  if (!props.animated) return ''
  return props.inkEffect ? 'stamp-appear-ink' : 'stamp-appear'
})

const stampStyle = computed(() => ({
  color: props.color,
  transform: `rotate(${props.rotation}deg)`,
  fontSize: props.size === 'large' ? '24px' : props.size === 'small' ? '14px' : '18px'
}))
</script>

<style scoped>
.stamp {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  border: 3px solid currentColor;
  border-radius: 4px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 2px;
  opacity: 0.9;
  position: relative;
}

.stamp::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 30% 30%, transparent 0%, rgba(0, 0, 0, 0.05) 100%);
  pointer-events: none;
}

.stamp-text {
  text-shadow: 1px 1px 0 rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1;
}
</style>
