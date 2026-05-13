// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import DCPage from '../components/DCPage/DCPage.vue'
import Home from '../components/HomePage/Home.vue'
import Community from '../components/CommunityPage/Posts.vue'
import AuthPage from '../components/AuthPage/AuthPage.vue'
import ProfilePage from '../components/ProfilePage/ProfilePage.vue'
import { isLoggedIn } from '../auth/session.js'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: AuthPage,
    meta: { public: true }
  },
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/doccontrol',
    name: 'DCPage',
    component: DCPage
  },
  {
    path: '/community',
    name: 'Community',
    component: Community
  },
  {
    path: '/profile',
    name: 'Profile',
    component: ProfilePage
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to) => {
  if (to.meta.public) {
    if (to.name === 'Login' && isLoggedIn()) {
      return typeof to.query.redirect === 'string' && to.query.redirect.startsWith('/')
        ? to.query.redirect
        : '/'
    }
    return true
  }
  if (!isLoggedIn()) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router