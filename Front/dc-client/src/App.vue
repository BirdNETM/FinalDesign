<template>
  <div class="app-container">
    <!-- 路由出口：匹配的路由组件会渲染在这里 -->
    <router-view />
  </div>
</template>

<script setup>
// 无需额外导入组件，路由会自动渲染
</script>

<style scoped>
.app-container {
  max-width: 1200px;
  margin: 0 auto;
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