<template>
  <div class="profile-page">
    <!-- 背景 -->
    <div class="profile-bg" aria-hidden="true">
      <div class="bg-blob blob-1" />
      <div class="bg-blob blob-2" />
    </div>

    <!-- 主卡片 -->
    <el-card class="profile-card" shadow="never">
      <!-- header -->
      <div class="profile-header">
        <div class="profile-avatar">
          <span>{{ avatarLetter }}</span>
        </div>
        <div class="profile-header-info">
          <h2>{{ displayName }}</h2>
          <p class="profile-username">@{{ user?.username }}</p>
        </div>
      </div>

      <el-divider />

      <!-- 基本信息 -->
      <section class="profile-section">
        <h3 class="section-title">基本信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>昵称</label>
            <p>{{ user?.nickname || '123' }}</p>
          </div>
          <div class="info-item">
            <label>邮箱</label>
            <p>{{ user?.email || '123@example.com' }}</p>
          </div>
          <div class="info-item">
            <label>用户名</label>
            <p>{{ user?.username || '123' }}</p>
          </div>
        </div>
      </section>

      <el-divider />

      <!-- 编辑资料 -->
      <section class="profile-section">
        <h3 class="section-title">编辑资料</h3>
        <el-form
          ref="profileFormRef"
          :model="profileForm"
          :rules="profileRules"
          label-position="top"
          size="large"
        >
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="profileForm.nickname" clearable />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="profileForm.email" clearable />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="saving"
              class="profile-btn"
              @click="onSaveProfile"
            >
              保存修改
            </el-button>
          </el-form-item>
        </el-form>
      </section>

      <el-divider />

      <!-- 修改密码 -->
      <section class="profile-section">
        <h3 class="section-title">修改密码</h3>
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-position="top"
          size="large"
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password />
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password />
          </el-form-item>

          <el-form-item label="确认新密码" prop="confirm">
            <el-input
              v-model="passwordForm.confirm"
              type="password"
              show-password
              @keyup.enter="onChangePassword"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="warning"
              :loading="changing"
              class="profile-btn"
              @click="onChangePassword"
            >
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </section>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getCurrentUser,
  logout,
  updateProfile,
  changePassword
} from '../../auth/session.js'
import { useRouter } from 'vue-router'

const router = useRouter()
const user = ref(null)

onMounted(() => {
  user.value = getCurrentUser()
  if (user.value) {
    profileForm.nickname = user.value.nickname || ''
    profileForm.email = user.value.email || ''
  }
})

const avatarLetter = computed(() => {
  const name = user.value?.nickname || user.value?.username || '?'
  return name.charAt(0).toUpperCase()
})

const displayName = computed(() => {
  return user.value?.nickname || user.value?.username || '用户'
})

/* ---- profile ---- */
const profileFormRef = ref()
const saving = ref(false)

const profileForm = reactive({
  nickname: '',
  email: ''
})

const profileRules = {
  nickname: [{ max: 32, message: '昵称不超过 32 个字符', trigger: 'blur' }],
  email: [
    {
      pattern: /^\S+@\S+\.\S+$/,
      message: '请输入正确的邮箱格式',
      trigger: 'blur'
    }
  ]
}

async function onSaveProfile() {
  const valid = await profileFormRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const res = updateProfile(profileForm.nickname.trim(), profileForm.email.trim())
    if (!res.ok) return ElMessage.error(res.message)

    user.value = getCurrentUser()
    ElMessage.success('资料已更新')
  } finally {
    saving.value = false
  }
}

/* ---- password ---- */
const passwordFormRef = ref()
const changing = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirm: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '至少 6 位', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_r, v, cb) =>
        v !== passwordForm.newPassword ? cb(new Error('不一致')) : cb(),
      trigger: 'blur'
    }
  ]
}

async function onChangePassword() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  changing.value = true
  try {
    const res = changePassword(passwordForm.oldPassword, passwordForm.newPassword)
    if (!res.ok) return ElMessage.error(res.message)

    ElMessage.success('请重新登录')
    logout()
    router.push({ name: 'Login' })
  } finally {
    changing.value = false
  }
}
</script>

<style scoped>
/* 🔥 关键：彻底解决左偏 */
.profile-page {
  position: fixed;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 64px 24px;
  overflow-y: auto;
  background: linear-gradient(135deg, #e8edf5, #f5f6fa, #eef0f6);
}

/* 背景 */
.profile-bg {
  position: absolute;
  inset: 0;
}

.bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.3;
}

.blob-1 {
  width: 500px;
  height: 500px;
  background: #409eff;
  top: -150px;
  left: -100px;
}

.blob-2 {
  width: 400px;
  height: 400px;
  background: #e6a23c;
  bottom: -120px;
  right: -80px;
}

/* 卡片 */
.profile-card {
  width: min(820px, 100%);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
}

/* header */
.profile-header {
  display: flex;
  gap: 20px;
  align-items: center;
}

.profile-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
}

/* 信息卡片 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

.info-item {
  padding: 16px;
  border-radius: 12px;
  background: #f5f7fa;
  transition: 0.2s;
}

.info-item:hover {
  transform: translateY(-2px);
}

/* 按钮 */
.profile-btn {
  width: 220px;
  margin: 10px auto 0;
  display: block;
}

/* 响应式 */
@media (max-width: 640px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>