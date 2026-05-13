const USERS_KEY = 'dc_client_users'
const SESSION_KEY = 'dc_client_session'

function readJson(key, fallback) {
  try {
    const raw = localStorage.getItem(key)
    if (!raw) return fallback
    return JSON.parse(raw)
  } catch {
    return fallback
  }
}

export function getRegisteredUsers() {
  return readJson(USERS_KEY, [])
}

function saveUsers(users) {
  localStorage.setItem(USERS_KEY, JSON.stringify(users))
}

export function register(username, password) {
  const users = getRegisteredUsers()
  if (users.some((u) => u.username === username)) {
    return { ok: false, message: '用户名已存在' }
  }
  users.push({ username, password, nickname: '', email: '' })
  saveUsers(users)
  return { ok: true }
}

export function login(username, password) {
  const users = getRegisteredUsers()
  const u = users.find((x) => x.username === username && x.password === password)
  if (!u) {
    return { ok: false, message: '用户名或密码错误' }
  }
  localStorage.setItem(SESSION_KEY, JSON.stringify({ username: u.username, at: Date.now() }))
  return { ok: true }
}

export function logout() {
  localStorage.removeItem(SESSION_KEY)
}

export function getSession() {
  return readJson(SESSION_KEY, null)
}

export function isLoggedIn() {
  return !!getSession()?.username
}

export function getCurrentUser() {
  const session = getSession()
  if (!session) return null
  const users = getRegisteredUsers()
  return users.find((u) => u.username === session.username) || null
}

export function updateProfile(nickname, email) {
  const session = getSession()
  if (!session) return { ok: false, message: '未登录' }
  const users = getRegisteredUsers()
  const u = users.find((x) => x.username === session.username)
  if (!u) return { ok: false, message: '用户不存在' }
  u.nickname = nickname
  u.email = email
  saveUsers(users)
  return { ok: true }
}

export function changePassword(oldPassword, newPassword) {
  const session = getSession()
  if (!session) return { ok: false, message: '未登录' }
  const users = getRegisteredUsers()
  const u = users.find((x) => x.username === session.username)
  if (!u) return { ok: false, message: '用户不存在' }
  if (u.password !== oldPassword) return { ok: false, message: '原密码错误' }
  u.password = newPassword
  saveUsers(users)
  return { ok: true }
}
