<template>
  <div class="stamp" :class="{ 'stamp-appear': animated }" :style="stampStyle">
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
    default: '#C41E3A'
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
  }
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
}

.stamp-text {
  text-shadow: 1px 1px 0 rgba(0, 0, 0, 0.1);
}
</style>
