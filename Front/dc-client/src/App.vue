<template>
  <div class="app-container" :class="{ 'app-container--full': isAuthRoute }">
    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isAuthRoute = computed(() => route.name === 'Login' || route.name === 'Profile')
</script>

<style scoped>
.app-container {
  max-width: 1200px;
  margin: 0 auto;
}

.app-container--full {
  max-width: none;
  margin: 0;
}
.nav {
  margin-bottom: 30px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}
.nav a {
  margin-right: 20px;
  text-decoration: none;
  color: #409eff;
  font-size: 16px;
}
.nav a.router-link-active {
  color: #67c23a;
  font-weight: bold;
}
</style>