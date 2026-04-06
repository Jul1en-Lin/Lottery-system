<template>
  <div class="paper-card" :class="{ 'paper-shake': hoverable }">
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
