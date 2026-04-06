<template>
  <NewspaperLayout>
    <router-view v-slot="{ Component }">
      <transition name="page-flip" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </NewspaperLayout>
</template>

<script setup>
import NewspaperLayout from '@/components/layout/NewspaperLayout.vue'
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background-color: #F5F0E1;
}
</style>
