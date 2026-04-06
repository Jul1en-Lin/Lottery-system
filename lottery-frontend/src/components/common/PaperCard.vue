<template>
  <div class="paper-card" :class="{ 'card-hover-lift': hoverable, 'card-hover-glow': hoverable }">
    <slot></slot>
  </div>
</template>

<script setup>
defineProps({
  hoverable: {
    type: Boolean,
    default: true
  }
})
</script>

<style scoped>
.paper-card {
  background-color: var(--paper-bg);
  border: var(--border-double);
  padding: 16px;
  position: relative;
  transition: transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94), box-shadow 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.paper-card::before {
  content: '';
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border: 1px solid var(--divider-color);
  pointer-events: none;
  opacity: 0.5;
}
</style>
