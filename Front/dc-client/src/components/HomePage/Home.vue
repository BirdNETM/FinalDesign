<template>
  <div class="home-container">
    <h1>欢迎来到文档控制中心</h1>
    <div class="nav-cards">
      <!-- 资料管理 -->
      <el-card class="nav-card" shadow="hover" @click="go('/doccontrol')">
        <template #header>
          <div class="card-header">
            <span>资料管理</span>
          </div>
        </template>
        <div class="card-content">
          管理和查阅文档资料
        </div>
      </el-card>

      <!-- 社区 -->
      <el-card class="nav-card" shadow="hover" @click="go('/community')">
        <template #header>
          <div class="card-header">
            <span>社区</span>
          </div>
        </template>
        <div class="card-content">
          参与讨论与分享
        </div>
      </el-card>

      <!-- 我的 -->
      <el-card class="nav-card" shadow="hover" @click="go('/me')">
        <template #header>
          <div class="card-header">
            <span>我的</span>
          </div>
        </template>
        <div class="card-content">
          个人中心与设置
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

const go = (path) => {
  // 如果是已存在的路由则跳转
  if (path === '/doccontrol') {
    router.push(path)
  } else {
    // 暂时提示功能开发中
    ElMessage.info(`跳转到 ${path} (功能开发中...)`)
    // 如果您后续创建了对应页面，可以取消注释下面这行
    // router.push(path)
  }
}
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
}

h1 {
  margin-bottom: 60px;
  color: #303133;
}

.nav-cards {
  display: flex;
  gap: 40px;
  justify-content: center;
  flex-wrap: wrap;
}

.nav-card {
  width: 280px;
  cursor: pointer;
  text-align: center;
  transition: transform 0.3s;
}

.nav-card:hover {
  transform: translateY(-5px);
}

.card-header {
  font-size: 20px;
  font-weight: bold;
}

.card-content {
  color: #606266;
  padding: 20px 0;
}
</style>