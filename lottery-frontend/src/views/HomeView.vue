<template>
  <div class="home-view fade-in">
    <NewspaperTitle
      title="幸运抽奖日报"
      subtitle="LUCKY LOTTERY DAILY"
      show-date
      size="large"
    />

    <div class="headline-section paper-card double-border">
      <h2 class="headline-title">特 大 奖 抽 奖 活 动</h2>
      <p class="english-subtitle">JACKPOT LOTTERY EVENT</p>
    </div>

    <div class="content-grid">
      <div class="column paper-card">
        <h3 class="column-title">活动快讯</h3>
        <Divider type="double" />
        <div class="news-item">
          <span class="news-bullet">◆</span>
          <span>春季大抽奖 - 剩余3天结束</span>
        </div>
        <div class="news-item">
          <span class="news-bullet">◆</span>
          <span>新用户专享 - 首次参与双倍</span>
        </div>
      </div>

      <div class="column paper-card">
        <h3 class="column-title">近期中奖公告</h3>
        <Divider type="double" />
        <div class="winner-item">
          <Stamp text="中奖" size="small" />
          <span>林** 中得 智能手表</span>
        </div>
        <div class="winner-item">
          <Stamp text="中奖" size="small" />
          <span>吴** 中得 蓝牙耳机</span>
        </div>
        <router-link to="/prizes" class="view-more">查看更多 &gt;</router-link>
      </div>
    </div>

    <div class="action-buttons">
      <InkButton @click="goToActivities">立即参与</InkButton>
      <InkButton @click="goToActivities">查看全部活动</InkButton>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import NewspaperTitle from '@/components/common/NewspaperTitle.vue'
import Stamp from '@/components/common/Stamp.vue'
import InkButton from '@/components/common/InkButton.vue'
import Divider from '@/components/common/Divider.vue'

const router = useRouter()

function goToActivities() {
  router.push('/activities')
}
</script>

<style scoped>
.home-view {
  max-width: 900px;
  margin: 0 auto;
}

.headline-section {
  text-align: center;
  margin: 24px 0;
  padding: 32px;
}

.headline-title {
  font-size: 36px;
  letter-spacing: 8px;
  margin: 0 0 8px 0;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin: 24px 0;
}

.column {
  padding: 20px;
}

.column-title {
  font-size: 18px;
  letter-spacing: 4px;
  margin: 0 0 8px 0;
}

.news-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
}

.news-bullet {
  color: var(--stamp-red);
}

.winner-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.view-more {
  display: block;
  text-align: right;
  color: var(--blue-ink);
  text-decoration: underline;
  margin-top: 12px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 32px;
}
</style>
