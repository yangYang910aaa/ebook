/* 路由配置：前台（首页/电子书列表/文档阅读）+ 后台管理（需登录），含全局路由守卫 */
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  {
    path: '/',
    component: () => import('../layout/FrontLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('../views/HomePage.vue'), meta: { title: '首页' } },
      { path: 'ebook', name: 'ebookList', component: () => import('../views/EbookListPage.vue'), meta: { title: '电子书' } },
      { path: 'doc', name: 'docRead', component: () => import('../views/DocReadPage.vue'), meta: { title: '在线阅读' } }
    ]
  },
  {
    path: '/admin',
    component: () => import('../layout/AdminLayout.vue'),
    meta: { requiresLogin: true }, // 后台管理路由需登录
    children: [
      { path: '', redirect: '/admin/ebook' },
      { path: 'ebook', name: 'adminEbook', component: () => import('../views/admin/EbookManage.vue'), meta: { title: '电子书管理' } },
      { path: 'category', name: 'adminCategory', component: () => import('../views/admin/CategoryManage.vue'), meta: { title: '分类管理' } },
      { path: 'doc', name: 'adminDoc', component: () => import('../views/admin/DocManage.vue'), meta: { title: '文档管理' } },
      { path: 'user', name: 'adminUser', component: () => import('../views/admin/UserManage.vue'), meta: { title: '用户管理' } }
    ]
  },
  // 未匹配路由统一跳转首页
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫：校验登录状态 + 设置页面标题
router.beforeEach((to) => {
  const userStore = useUserStore()
  // 访问需登录的后台页面但未登录时，跳转首页
  if (to.meta.requiresLogin && !userStore.isLogin) {
    return { path: '/' }
  }
  // 动态设置浏览器标签页标题
  document.title = to.meta.title ? `${to.meta.title} - 数字电子书` : '数字电子书'
  return true
})

export default router
