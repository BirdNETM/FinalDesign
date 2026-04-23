// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
// 导入资料管理组件
import DCPage from '../components/DCPage/DCPage.vue'
import Home from '../components/HomePage/Home.vue' // 导入 Home 组件
import Community from '../components/CommunityPage/Posts.vue'
// 定义路由规则
const routes = [
    {
    path: '/',        // 主页路径
    name: 'Home',
    component: Home
  },
  {
    path: '/doccontrol', // 资料管理页面的路由路径
    name: 'DCPage', // 路由名称（可选，用于编程式导航）
    component: DCPage // 对应资料管理组件
  },
  {
    path: '/community', // 资料管理页面的路由路径
    name: 'Community', // 路由名称（可选，用于编程式导航）
    component: Community // 对应资料管理组件
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL), // 基于 HTML5 History 模式
  routes // 注入路由规则
})

export default router