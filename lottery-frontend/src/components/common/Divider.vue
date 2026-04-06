<template>
  <hr class="divider" :class="type">
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'single'
  }
})
</script>

<style scoped>
.divider {
  border: none;
  margin: 16px 0;
}

.divider.single {
  border-top: 1px solid var(--divider-color);
}

.divider.double {
  border-top: 3px double var(--ink-primary);
}

.divider.dashed {
  border-top: 2px dashed var(--border-color);
}
</style>
