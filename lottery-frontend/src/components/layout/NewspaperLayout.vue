<template>
  <div class="newspaper-layout paper-texture">
    <NavBar />
    <main class="main-content">
      <slot></slot>
    </main>
    <Footer />
  </div>
</template>

<script setup>
import NavBar from './NavBar.vue'
import Footer from './Footer.vue'
</script>

<style scoped>
.newspaper-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
}

@media (max-width: 768px) {
  .main-content {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: 12px;
  }
}
</style>
