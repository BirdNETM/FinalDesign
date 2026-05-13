<template>
  <div class="auth-page">
    <div class="auth-bg" aria-hidden="true">
      <div class="bg-blob blob-1" />
      <div class="bg-blob blob-2" />
      <div class="bg-blob blob-3" />
    </div>

    <el-card class="auth-card" shadow="always">
      <div class="auth-brand">
        <div class="auth-logo">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
            <rect width="40" height="40" rx="10" fill="#409eff" />
            <path
              d="M12 20c0-4.418 3.582-8 8-8s8 3.582 8 8-3.582 8-8 8-8-3.582-8-8z"
              fill="#fff" fill-opacity="0.9"
            />
            <path
              d="M17 20l2.5 2.5L23 17"
              stroke="#409eff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"
            />
          </svg>
        </div>
        <h1>文档控制中心</h1>
        <p class="subtitle">请先登录或注册账号</p>
      </div>

      <el-tabs v-model="activeTab" stretch class="auth-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form
            ref="loginFormRef"
            size="large"
            :model="loginForm"
            :rules="loginRules"
            label-position="top"
            @submit.prevent="onLogin"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="loginForm.username" autocomplete="username" clearable />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                show-password
                autocomplete="current-password"
                @keyup.enter="onLogin"
              />
            </el-form-item>
            <el-button type="primary" size="large" class="auth-submit" :loading="loading" @click="onLogin">
              登录
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form
            ref="registerFormRef"
            size="large"
            :model="registerForm"
            :rules="registerRules"
            label-position="top"
            @submit.prevent="onRegister"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="registerForm.username" autocomplete="username" clearable />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                show-password
                autocomplete="new-password"
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirm">
              <el-input
                v-model="registerForm.confirm"
                type="password"
                show-password
                autocomplete="new-password"
                @keyup.enter="onRegister"
              />
            </el-form-item>
            <el-button type="primary" size="large" class="auth-submit" :loading="loading" @click="onRegister">
              注册
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '../../auth/session.js'

const route = useRoute()
const router = useRouter()

const activeTab = ref('login')
const loading = ref(false)
const loginFormRef = ref()
const registerFormRef = ref()

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirm: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 32, message: '长度为 2–32 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 64, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

function redirectAfterAuth() {
  const raw = route.query.redirect
  const path = typeof raw === 'string' && raw.startsWith('/') ? raw : '/'
  router.replace(path)
}

async function onLogin() {
  const valid = await loginFormRef.value
    ?.validate()
    .then(() => true)
    .catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = login(loginForm.username.trim(), loginForm.password)
    if (!res.ok) {
      ElMessage.error(res.message)
      return
    }
    ElMessage.success('登录成功')
    redirectAfterAuth()
  } finally {
    loading.value = false
  }
}

async function onRegister() {
  const valid = await registerFormRef.value
    ?.validate()
    .then(() => true)
    .catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = register(registerForm.username.trim(), registerForm.password)
    if (!res.ok) {
      ElMessage.error(res.message)
      return
    }
    ElMessage.success('注册成功，请登录')
    const name = registerForm.username.trim()
    registerForm.password = ''
    registerForm.confirm = ''
    registerFormRef.value?.resetFields()
    activeTab.value = 'login'
    loginForm.username = name
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  position: fixed;
  inset: 0;
  
  display: flex;
  align-items: center;
  justify-content: center;

  padding: 32px;
  overflow: hidden;
  background: linear-gradient(135deg, #e8edf5 0%, #f5f6fa 50%, #eef0f6 100%);
}

/* ---- background decorative blobs ---- */
.auth-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.35;
}

.blob-1 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, #409eff 0%, #79bbff 100%);
  top: -200px;
  right: -100px;
}

.blob-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #67c23a 0%, #b3e19d 100%);
  bottom: -180px;
  left: -120px;
}

.blob-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, #e6a23c 0%, #f3d19e 100%);
  top: 50%;
  left: 60%;
  transform: translate(-50%, -50%);
}

/* ---- card ---- */
.auth-card {
  position: relative;
  width: min(460px, 100%);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.06),
    0 2px 8px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.3s, transform 0.3s;
}

.auth-card:hover {
  box-shadow:
    0 12px 48px rgba(0, 0, 0, 0.10),
    0 4px 12px rgba(0, 0, 0, 0.06);
  transform: translateY(-2px);
}

.auth-card :deep(.el-card__body) {
  padding: 40px 40px 44px;
}

/* ---- brand ---- */
.auth-brand {
  text-align: center;
  margin-bottom: 24px;
}

.auth-logo {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.auth-logo svg {
  filter: drop-shadow(0 2px 8px rgba(64, 158, 255, 0.3));
}

.auth-brand h1 {
  margin: 0 0 8px;
  font-size: 1.75rem;
  color: #303133;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.subtitle {
  margin: 0;
  font-size: 15px;
  color: #909399;
}

/* ---- tabs ---- */
.auth-tabs {
  margin-top: 4px;
}

.auth-tabs :deep(.el-tabs__header) {
  margin-bottom: 28px;
}

.auth-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  height: 48px;
  line-height: 48px;
  padding: 0 20px;
}

.auth-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 2px;
}

/* ---- form ---- */
.auth-tabs :deep(.el-form-item) {
  margin-bottom: 20px;
}

.auth-tabs :deep(.el-form-item__label) {
  font-size: 14px;
  line-height: 20px;
  margin-bottom: 8px;
  color: #606266;
}

/* rounded inputs */
.auth-tabs :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: box-shadow 0.25s;
}

.auth-tabs :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.auth-tabs :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #409eff inset;
}

.auth-tabs :deep(.el-input__inner) {
  height: 42px;
}

/* ---- submit button ---- */
.auth-submit {
  width: 100%;
  margin-top: 8px;
  height: 46px;
  font-size: 16px;
  border-radius: 10px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.auth-submit:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.35);
}

.auth-submit:active {
  transform: translateY(0);
}

/* ---- responsive ---- */
/* @media (min-width: 1200px) {
  .auth-card {
    width: min(500px, 100%);
  }

  .auth-card :deep(.el-card__body) {
    padding: 48px 48px 52px;
  }

  .auth-brand h1 {
    font-size: 2rem;
  }

  .auth-tabs :deep(.el-tabs__item) {
    font-size: 17px;
    height: 52px;
    line-height: 52px;
  }

  .auth-submit {
    height: 48px;
    font-size: 17px;
  }
}

@media (max-width: 480px) {
  .auth-page {
    padding: 16px;
  }

  .auth-card :deep(.el-card__body) {
    padding: 28px 24px 32px;
  }
} */
</style>
