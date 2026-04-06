<template>
  <div class="user-center-view fade-in">
    <NewspaperTitle
      title="读者中心"
      subtitle="READER CENTER"
    />

    <div class="user-info paper-card">
      <h3>个人信息</h3>
      <Divider />
      <p><strong>用户ID：</strong>{{ userStore.userId || '-' }}</p>
      <p><strong>身份：</strong>{{ userStore.isAdmin ? '管理员' : '普通用户' }}</p>
    </div>

    <div class="quick-actions paper-card">
      <h3>快捷入口</h3>
      <Divider />
      <div class="action-grid">
        <router-link to="/activities" class="action-item">
          <span class="action-icon">◆</span>
          <span>参与活动</span>
        </router-link>
        <router-link to="/prizes" class="action-item">
          <span class="action-icon">◆</span>
          <span>中奖记录</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import NewspaperTitle from '@/components/common/NewspaperTitle.vue'
import Divider from '@/components/common/Divider.vue'

const userStore = useUserStore()
</script>

<style scoped>
.user-center-view {
  max-width: 800px;
  margin: 0 auto;
}

.user-info,
.quick-actions {
  margin-bottom: 24px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-top: 16px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border: 1px solid var(--border-color);
  text-decoration: none;
  color: var(--ink-primary);
  transition: background-color 0.2s;
}

.action-item:hover {
  background-color: var(--paper-bg-dark);
}

.action-icon {
  color: var(--stamp-red);
}
</style>
