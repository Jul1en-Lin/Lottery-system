<template>
  <button
    class="ink-button"
    :class="{ 'btn-press': pressed, 'primary': primary, 'lottery-btn-glow': lottery }"
    @mousedown="handleMouseDown"
    @mouseup="handleMouseUp"
    @mouseleave="handleMouseLeave"
    :disabled="disabled"
  >
    <span class="button-content">
      <slot></slot>
    </span>
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
  },
  lottery: {
    type: Boolean,
    default: false
  }
})

const pressed = ref(false)

function handleMouseDown() {
  pressed.value = true
}

function handleMouseUp() {
  pressed.value = false
}

function handleMouseLeave() {
  pressed.value = false
}
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
  transition: background-color 0.2s ease, color 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
  position: relative;
  overflow: hidden;
}

.ink-button::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, rgba(26, 26, 26, 0.1) 0%, transparent 70%);
  transform: translate(-50%, -50%);
  transition: width 0.4s ease, height 0.4s ease;
}

.ink-button:active::before {
  width: 200%;
  height: 200%;
}

.ink-button:hover:not(:disabled) {
  background-color: var(--ink-primary);
  color: var(--paper-bg);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(26, 26, 26, 0.2);
}

.ink-button:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: none;
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

.button-content {
  position: relative;
  z-index: 1;
}
</style>
