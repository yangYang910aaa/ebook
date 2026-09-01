<template>
  <a-layout style="min-height: 100vh">
    <a-layout-header class="header">
      <div class="logo">数字电子书</div>
      <a-menu theme="dark" mode="horizontal" :selected-keys="selectedKeys" style="flex: 1" @click="onMenuClick">
        <a-menu-item key="home">首页</a-menu-item>
        <template v-if="userStore.isLogin">
          <a-menu-item key="admin-ebook">电子书管理</a-menu-item>
          <a-menu-item key="admin-category">分类管理</a-menu-item>
          <a-menu-item key="admin-user">用户管理</a-menu-item>
        </template>
        <a-menu-item key="about">关于</a-menu-item>
      </a-menu>
      <div class="user-area">
        <template v-if="userStore.isLogin">
          <span>当前用户：{{ userStore.name }}</span>
          <a-button type="link" @click="onLogout">退出登录</a-button>
        </template>
        <template v-else>
          <a-button type="primary" size="small" @click="loginVisible = true">登录</a-button>
        </template>
      </div>
    </a-layout-header>

    <a-layout>
      <a-layout-sider width="220" theme="light">
        <a-menu mode="inline" :selected-keys="[selectedCategory]" style="height: 100%" @click="onCategoryClick">
          <a-menu-item key="welcome">欢迎</a-menu-item>
          <a-sub-menu v-for="c1 in categoryTree" :key="'c1-' + c1.id">
            <template #title>{{ c1.name }}</template>
            <a-menu-item v-for="c2 in c1.children" :key="'c2-' + c2.id">{{ c2.name }}</a-menu-item>
          </a-sub-menu>
        </a-menu>
      </a-layout-sider>

    <a-layout-content style="padding: 16px">
        <div class="page-enter" style="max-width: 1200px; margin: 0 auto">
          <router-view />
        </div>
      </a-layout-content>
    </a-layout>

    <a-layout-footer class="footer">
      <div>数字电子书在线平台 ©2026</div>
      <div v-if="userStore.isLogin">欢迎：{{ userStore.name }}</div>
    </a-layout-footer>

    <a-modal v-model:open="loginVisible" title="登录" :footer="null" :mask-closable="false">
      <a-form layout="vertical">
        <a-form-item label="登录名">
          <a-input v-model:value="loginForm.loginName" placeholder="请输入登录名" />
        </a-form-item>
        <a-form-item label="密码">
          <a-input-password v-model:value="loginForm.password" placeholder="请输入密码" @press-enter="onLogin" />
        </a-form-item>
        <a-button type="primary" block :loading="loggingIn" @click="onLogin">登录</a-button>
      </a-form>
    </a-modal>
  </a-layout>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useUserStore } from '../store/user'
import { getCategoryList } from '../api/category'
import { loginApi, logoutApi } from '../api/user'
import { closeWebSocket, connectWebSocket } from '../utils/websocket'

interface CategoryNode {
  id: number
  parent: number
  name: string
  sort: number
  children: CategoryNode[]
}

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const categoryTree = ref<CategoryNode[]>([])
const loginVisible = ref(false)
const loggingIn = ref(false)
const loginForm = ref({ loginName: '', password: '' })

const selectedKeys = computed(() => {
  if (route.path.startsWith('/admin')) {
    return ['admin-' + route.path.replace('/admin/', '')]
  }
  return ['home']
})

const selectedCategory = computed(() => {
  const category2Id = route.query.category2Id as string | undefined
  return category2Id ? 'c2-' + category2Id : 'welcome'
})

onMounted(() => {
  loadCategories()
  if (userStore.token) {
    connectWebSocket(userStore.token)
  }
})

onUnmounted(() => {
  closeWebSocket()
})

async function loadCategories() {
  try {
    const list = await getCategoryList()
    const map = new Map<number, CategoryNode>()
    list.forEach((c) => map.set(c.id, { ...c, children: [] }))
    categoryTree.value = []
    map.forEach((c) => {
      if (c.parent === 0) {
        categoryTree.value.push(c)
      } else {
        const parent = map.get(c.parent)
        if (parent) parent.children.push(c)
      }
    })
    categoryTree.value.sort((a, b) => a.sort - b.sort)
  } catch {
    // 分类加载失败不阻塞页面
  }
}

function onMenuClick({ key }: { key: string }) {
  if (key === 'home') {
    router.push('/')
  } else if (key === 'about') {
    message.info('数字电子书在线学习平台')
  } else if (key.startsWith('admin-')) {
    router.push('/admin/' + key.replace('admin-', ''))
  }
}

function onCategoryClick({ key }: { key: string }) {
  if (key === 'welcome') {
    router.push('/')
  } else if (key.startsWith('c2-')) {
    router.push({ path: '/ebook', query: { category2Id: key.replace('c2-', '') } })
  }
}

async function onLogin() {
  if (!loginForm.value.loginName || !loginForm.value.password) {
    message.warning('请输入登录名和密码')
    return
  }
  loggingIn.value = true
  try {
    const resp = await loginApi({
      loginName: loginForm.value.loginName,
      password: loginForm.value.password
    })
    userStore.setUser(resp)
    loginVisible.value = false
    loginForm.value = { loginName: '', password: '' }
    message.success('登录成功')
    connectWebSocket(resp.token)
  } catch {
    // 错误提示由 axios 拦截器统一处理
  } finally {
    loggingIn.value = false
  }
}

async function onLogout() {
  try {
    await logoutApi(userStore.token)
  } catch {
    // 忽略退出接口异常
  }
  userStore.clearUser()
  closeWebSocket()
  message.success('已退出登录')
  router.push('/')
}
</script>
