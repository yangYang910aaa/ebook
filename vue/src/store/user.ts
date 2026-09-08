/* 用户 Pinia Store：管理登录态（token、用户信息），持久化到 sessionStorage */
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

/** 登录用户信息 */
export interface UserInfo {
  token: string      // 登录令牌
  id: number         // 用户 ID
  loginName: string  // 登录名
  name: string       // 昵称
}

const STORAGE_KEY = 'ebook_user'

// 从 sessionStorage 恢复登录态（刷新页面后保持登录）
function loadFromStorage(): UserInfo | null {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    return raw ? (JSON.parse(raw) as UserInfo) : null
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const saved = loadFromStorage()
  const token = ref(saved?.token ?? '')
  const id = ref(saved?.id ?? 0)
  const loginName = ref(saved?.loginName ?? '')
  const name = ref(saved?.name ?? '')

  // 是否已登录：token 非空即视为已登录
  const isLogin = computed(() => !!token.value)

  // 登录成功后保存用户信息并持久化
  function setUser(user: UserInfo) {
    token.value = user.token
    id.value = user.id
    loginName.value = user.loginName
    name.value = user.name
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(user))
  }

  // 退出登录：清空所有用户信息并移除持久化数据
  function clearUser() {
    token.value = ''
    id.value = 0
    loginName.value = ''
    name.value = ''
    sessionStorage.removeItem(STORAGE_KEY)
  }

  return { token, id, loginName, name, isLogin, setUser, clearUser }
})
