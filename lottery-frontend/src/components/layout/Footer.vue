<template>
  <footer class="footer">
    <div class="footer-content">
      <div class="footer-divider"></div>
      <p class="footer-text">
        © 2026 Lucky Lottery Daily - 幸运抽奖日报
      </p>
      <p class="footer-subtitle">
        All Rights Reserved
      </p>
    </div>
  </footer>
</template>

<script setup>
</script>

<style scoped>
.footer {
  background-color: var(--paper-bg-dark);
  border-top: 3px double var(--ink-primary);
  padding: 24px 0;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: center;
}

.footer-divider {
  width: 60px;
  height: 2px;
  background-color: var(--divider-color);
  margin: 0 auto 16px;
}

.footer-text {
  font-size: 14px;
  color: var(--ink-secondary);
  margin: 0;
}

.footer-subtitle {
  font-family: 'Special Elite', 'Courier New', monospace;
  font-size: 12px;
  color: var(--ink-secondary);
  margin-top: 8px;
}
</style>
