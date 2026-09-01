<template>
  <a-layout style="min-height: 100vh">
    <a-layout-header class="header">
      <div class="logo">数字电子书 · 后台管理</div>
      <a-menu theme="dark" mode="horizontal" :selected-keys="selectedKeys" style="flex: 1" @click="onMenuClick">
        <a-menu-item key="ebook">电子书管理</a-menu-item>
        <a-menu-item key="category">分类管理</a-menu-item>
        <a-menu-item key="doc">文档管理</a-menu-item>
        <a-menu-item key="user">用户管理</a-menu-item>
      </a-menu>
      <div class="user-area">
        <span>{{ userStore.name }}</span>
        <a-button type="link" @click="router.push('/')">返回前台</a-button>
        <a-button type="link" @click="onLogout">退出登录</a-button>
      </div>
    </a-layout-header>
    <a-layout-content style="padding: 16px">
      <div class="page-enter" style="max-width: 1200px; margin: 0 auto">
        <router-view />
      </div>
    </a-layout-content>
  </a-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useUserStore } from '../store/user'
import { logoutApi } from '../api/user'
import { closeWebSocket } from '../utils/websocket'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const selectedKeys = computed(() => {
  const seg = route.path.replace('/admin/', '')
  return seg ? [seg] : ['ebook']
})

function onMenuClick({ key }: { key: string }) {
  router.push('/admin/' + key)
}

async function onLogout() {
  try {
    await logoutApi(userStore.token)
  } catch {
    // 忽略
  }
  userStore.clearUser()
  closeWebSocket()
  message.success('已退出登录')
  router.push('/')
}
</script>
