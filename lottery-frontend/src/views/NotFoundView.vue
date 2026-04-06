<template>
  <div class="not-found">
    <div class="error-content paper-card">
      <div class="stamp-404">404</div>
      <h1 class="error-title">页面未找到</h1>
      <p class="error-subtitle">PAGE NOT FOUND</p>
      <p class="error-message">
        抱歉，您访问的页面似乎已经遗失在时空之中...
      </p>
      <p class="error-hint">
        也许它从未存在，又或者已被时间的洪流冲刷殆尽。
      </p>
      <router-link to="/" class="home-link">
        [ 返回首页 ]
      </router-link>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  padding: 40px 20px;
}

.error-content {
  text-align: center;
  max-width: 500px;
}

.stamp-404 {
  font-family: 'Special Elite', 'Courier New', monospace;
  font-size: 120px;
  font-weight: bold;
  color: var(--stamp-red);
  line-height: 1;
  opacity: 0.8;
  transform: rotate(-5deg);
  margin-bottom: 20px;
}

.error-title {
  font-size: 32px;
  color: var(--ink-primary);
  margin: 0 0 8px;
}

.error-subtitle {
  font-family: 'Special Elite', 'Courier New', monospace;
  font-size: 18px;
  color: var(--ink-secondary);
  margin: 0 0 24px;
  letter-spacing: 3px;
}

.error-message {
  font-size: 16px;
  color: var(--ink-primary);
  margin: 0 0 12px;
  line-height: 1.6;
}

.error-hint {
  font-size: 14px;
  color: var(--ink-secondary);
  margin: 0 0 32px;
  line-height: 1.6;
}

.home-link {
  display: inline-block;
  color: var(--blue-ink);
  text-decoration: none;
  font-size: 16px;
  padding: 8px 16px;
  border: 1px solid var(--border-color);
  transition: all 0.2s;
}

.home-link:hover {
  color: var(--stamp-red);
  border-color: var(--stamp-red);
}
</style>
