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
          <a-menu-item key="all-ebooks">全部电子书</a-menu-item>
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

    <a-modal
      v-model:visible="loginVisible"
      :footer="null"
      :mask-closable="false"
      :width="448"
      :centered="true"
      wrap-class-name="login-modal"
    >
      <div class="login-card">
        <div class="login-frame" aria-hidden="true"><i></i><i></i><i></i><i></i></div>
        <div class="login-emblem" aria-hidden="true">
          <svg viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="32" cy="32" r="30.5" fill="#2d2a24" />
            <circle cx="32" cy="32" r="26" stroke="#b03a2e" stroke-width="1.4" stroke-dasharray="2 4" />
            <path d="M32 21c-4.3-2.9-9.6-4.3-15.2-4.3V45c5.6 0 10.9 1.4 15.2 4.3 4.3-2.9 9.6-4.3 15.2-4.3V16.7C41.6 16.7 36.3 18.1 32 21Z" fill="#f6f1e5" />
            <path d="M32 21v28.3" stroke="#2d2a24" stroke-width="1.5" />
            <path d="M43.5 9.5v9.2l-2.7-1.8-2.5 1.8v-9.2h5.2Z" fill="#b03a2e" />
          </svg>
        </div>
        <h2 class="login-title display">欢迎回来</h2>
        <p class="login-sub">登录后即可进入后台，打理这座数字书房</p>

        <div class="login-form">
          <label class="login-label" for="login-name">登录名</label>
          <a-input id="login-name" v-model:value="loginForm.loginName" size="large" placeholder="请输入登录名" @press-enter="onLogin" />
          <label class="login-label" for="login-password">密码</label>
          <a-input-password id="login-password" v-model:value="loginForm.password" size="large" placeholder="请输入密码" @press-enter="onLogin" />
          <a-button class="login-btn" type="primary" block :loading="loggingIn" @click="onLogin">
            {{ loggingIn ? '正在登录…' : '进入书房' }}
          </a-button>
        </div>

        <p class="login-note">游客可自由浏览与阅读 · 后台管理需登录</p>
      </div>
    </a-modal>
  </a-layout>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Modal, message } from 'ant-design-vue'
import { useUserStore } from '../store/user'
import { getCategoryList } from '../api/category'
import { loginApi, logoutApi } from '../api/user'
import { closeWebSocket, connectWebSocket } from '../utils/websocket'
import { md5 } from '../utils/md5'

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
  if (category2Id) return 'c2-' + category2Id
  return route.path === '/ebook' ? 'all-ebooks' : 'welcome'
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
  } else if (key === 'all-ebooks') {
    router.push('/ebook')
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
      password: md5(loginForm.value.password)
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
  Modal.confirm({
    title: '确认退出登录？',
    content: '退出后将无法访问后台管理功能。',
    okText: '退出',
    cancelText: '取消',
    onOk: async () => {
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
  })
}
</script>

<style>
/* ===== 登录弹窗（藏书票风格；弹窗渲染在 body 下，需全局样式） ===== */
.login-modal .ant-modal-content {
  border-radius: 20px;
  overflow: hidden;
  background:
    radial-gradient(circle at 14% 0%, rgba(176, 58, 46, 0.07), transparent 46%),
    radial-gradient(circle at 92% 100%, rgba(47, 107, 95, 0.08), transparent 46%),
    #fffdf7;
  border: 1px solid var(--line);
  box-shadow: 0 24px 64px rgba(45, 42, 36, 0.24);
  padding: 0;
  animation: login-rise 0.32s ease both;
}

.login-modal .ant-modal-close {
  color: var(--ink-soft);
}

.login-modal .ant-modal-close:hover {
  color: var(--accent);
}

.login-card {
  position: relative;
  padding: 44px 46px 28px;
  text-align: center;
}

/* 内衬线 + 四角饰（票证感） */
.login-frame {
  position: absolute;
  inset: 14px;
  pointer-events: none;
  border: 1px solid rgba(176, 58, 46, 0.22);
}

.login-frame i {
  position: absolute;
  width: 14px;
  height: 14px;
  border: 2px solid var(--accent);
}

.login-frame i:nth-child(1) { top: -2px; left: -2px; border-right: none; border-bottom: none; }
.login-frame i:nth-child(2) { top: -2px; right: -2px; border-left: none; border-bottom: none; }
.login-frame i:nth-child(3) { bottom: -2px; left: -2px; border-right: none; border-top: none; }
.login-frame i:nth-child(4) { bottom: -2px; right: -2px; border-left: none; border-top: none; }

/* 印章徽记 */
.login-emblem {
  width: 86px;
  height: 86px;
  margin: 0 auto 18px;
  animation: login-pop 0.5s ease 0.05s both;
}

.login-emblem svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 6px 14px rgba(45, 42, 36, 0.28));
}

.login-title {
  margin: 0 0 6px;
  font-size: 27px;
  color: var(--ink);
  letter-spacing: 0.1em;
  animation: login-rise 0.45s ease 0.12s both;
}

.login-sub {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--ink-soft);
  animation: login-rise 0.45s ease 0.18s both;
}

.login-form {
  text-align: left;
  animation: login-rise 0.45s ease 0.24s both;
}

.login-label {
  display: block;
  margin: 20px 0 4px;
  font-size: 13px;
  letter-spacing: 0.14em;
  color: var(--ink-soft);
}

/* 下划线式输入 */
.login-modal .ant-input {
  background: transparent;
  border: none;
  border-bottom: 1px solid #cfc5ad;
  border-radius: 0;
  padding: 8px 2px;
  font-size: 15px;
  color: var(--ink);
  box-shadow: none !important;
}

.login-modal .ant-input:focus,
.login-modal .ant-input:hover {
  border-bottom-color: var(--accent);
}

.login-modal .ant-input::placeholder {
  color: #b3a992;
}

/* 赭红圆钮 */
.login-btn.ant-btn {
  height: 46px;
  margin-top: 28px;
  border-radius: 999px;
  background: var(--accent);
  border-color: var(--accent);
  font-size: 16px;
  letter-spacing: 0.4em;
  text-indent: 0.4em;
  font-weight: 600;
  box-shadow: 0 8px 20px rgba(176, 58, 46, 0.22);
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.login-btn.ant-btn:hover,
.login-btn.ant-btn:focus {
  background: var(--accent-deep);
  border-color: var(--accent-deep);
  transform: translateY(-1px);
  box-shadow: 0 12px 26px rgba(176, 58, 46, 0.3);
}

.login-note {
  margin: 22px 0 0;
  font-size: 12px;
  letter-spacing: 0.06em;
  color: #9a9182;
  animation: login-rise 0.45s ease 0.3s both;
}

@keyframes login-rise {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}

@keyframes login-pop {
  from {
    opacity: 0;
    transform: scale(0.72) translateY(8px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}
</style>
