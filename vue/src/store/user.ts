import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

export interface UserInfo {
  token: string
  id: number
  loginName: string
  name: string
}

const STORAGE_KEY = 'ebook_user'

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

  const isLogin = computed(() => !!token.value)

  function setUser(user: UserInfo) {
    token.value = user.token
    id.value = user.id
    loginName.value = user.loginName
    name.value = user.name
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(user))
  }

  function clearUser() {
    token.value = ''
    id.value = 0
    loginName.value = ''
    name.value = ''
    sessionStorage.removeItem(STORAGE_KEY)
  }

  return { token, id, loginName, name, isLogin, setUser, clearUser }
})
