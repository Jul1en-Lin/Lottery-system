<template>
  <button
    class="ink-button"
    :class="{ 'ink-spread': pressed, 'primary': primary }"
    @mousedown="pressed = true"
    @mouseup="pressed = false"
    @mouseleave="pressed = false"
    :disabled="disabled"
  >
    <slot></slot>
  </button>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  disabled: {
    type: Boolean,
    default: false
  },
  primary: {
    type: Boolean,
    default: false
  }
})

const pressed = ref(false)
</script>

<style scoped>
.ink-button {
  background-color: var(--paper-bg);
  border: 2px solid var(--ink-primary);
  color: var(--ink-primary);
  padding: 12px 32px;
  font-family: inherit;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.ink-button:hover:not(:disabled) {
  background-color: var(--ink-primary);
  color: var(--paper-bg);
}

.ink-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.ink-button.primary {
  background-color: var(--stamp-red);
  border-color: var(--stamp-red);
  color: var(--paper-bg);
}

.ink-button.primary:hover:not(:disabled) {
  background-color: #a01830;
  border-color: #a01830;
}
</style>
