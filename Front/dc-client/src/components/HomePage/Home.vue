<template>
  <div class="home-container">
    <div class="home-top">
      <span v-if="session?.username" class="welcome">你好，{{ session.username }}</span>
      <el-button v-if="session?.username" type="danger" plain size="small" @click="onLogout">
        退出登录
      </el-button>
    </div>
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
      <el-card class="nav-card" shadow="hover" @click="go('/profile')">
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
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSession, logout } from '../../auth/session.js'

const router = useRouter()
const session = computed(() => getSession())

const onLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    type: 'warning',
    confirmButtonText: '退出',
    cancelButtonText: '取消'
  })
    .then(() => {
      logout()
      ElMessage.success('已退出')
      router.replace({ name: 'Login' })
    })
    .catch(() => {})
}

const go = (path) => {
  // 如果是已存在的路由则跳转
  if (path === '/doccontrol') {
    router.push(path)
  } else {
    // 暂时提示功能开发中
    //ElMessage.info(`跳转到 ${path} (功能开发中...)`)
    // 如果您后续创建了对应页面，可以取消注释下面这行
    router.push(path)
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

.home-top {
  width: 100%;
  max-width: 960px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.welcome {
  font-size: 14px;
  color: #606266;
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