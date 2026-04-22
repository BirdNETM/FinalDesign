import { createApp } from 'vue'
import './style.css'
import ElementPlus from 'element-plus' // 新增
import 'element-plus/dist/index.css'   // 新增
import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(ElementPlus) 
app.use(router) // 核心：注入路由

app.mount('#app')
