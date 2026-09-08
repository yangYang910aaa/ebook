/* 应用入口：创建 Vue 实例，注册 Pinia、Vue Router、Ant Design Vue，挂载到 #app */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/antd.css'
import App from './App.vue'
import router from './router'
import './style.css'

const app = createApp(App)
app.use(createPinia()) // 状态管理
app.use(router)        // 路由
app.use(Antd)          // UI 组件库
app.mount('#app')
