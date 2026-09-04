<template>
  <div class="admin-page">
    <section class="panel">
      <div class="toolbar">
        <h3 class="display">用户管理</h3>
        <div class="actions">
          <a-input v-model:value="queryLoginName" placeholder="按登录名精确查询" allow-clear style="width: 200px" @press-enter="search" />
          <a-button @click="search">查询</a-button>
          <a-button type="primary" @click="openAdd">新增用户</a-button>
        </div>
      </div>
      <a-table
        :data-source="list"
        :columns="columns"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        @change="onTableChange"
      >
        <template #password="{ record }"><span class="mono">{{ record.password }}</span></template>
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" size="small" @click="openReset(record)">重置密码</a-button>
            <a-popconfirm title="确定删除该用户？" ok-text="删除" cancel-text="取消" @confirm="remove(record)">
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </section>

    <a-modal v-model:visible="modalOpen" :title="form.id ? '编辑用户' : '新增用户'" @ok="save">
      <a-form layout="vertical">
        <a-form-item label="登录名">
          <a-input v-model:value="form.loginName" :disabled="!!form.id" placeholder="登录名创建后不可修改" />
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model:value="form.name" placeholder="请输入昵称" />
        </a-form-item>
        <a-form-item v-if="!form.id" label="密码">
          <a-input-password v-model:value="form.password" placeholder="6~32 位，需包含数字和英文" />
        </a-form-item>
        <a-form-item v-else label="提示">
          <span class="hint">编辑用户仅修改昵称，密码请在"重置密码"中修改</span>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:visible="resetOpen" title="重置密码" @ok="resetPassword">
      <a-form layout="vertical">
        <a-form-item label="新密码">
          <a-input-password v-model:value="resetPwd" placeholder="6~32 位，需包含数字和英文" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { md5 } from '../../utils/md5'
import {
  getUserListByPage,
  removeUser,
  resetPassword as resetPasswordApi,
  saveUser,
  type UserRow
} from '../../api/user'

const list = ref<UserRow[]>([])
const loading = ref(false)
const queryLoginName = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const modalOpen = ref(false)
const resetOpen = ref(false)
const resetId = ref(0)
const resetPwd = ref('')
const form = reactive({ id: 0, loginName: '', name: '', password: '' })
const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).{6,32}$/

const columns = [
  { title: '登录名', dataIndex: 'loginName', key: 'loginName' },
  { title: '昵称', dataIndex: 'name', key: 'name' },
  { title: '密码（密文）', dataIndex: 'password', key: 'password', width: 220, slots: { customRender: 'password' } },
  { title: '操作', key: 'action', width: 240, slots: { customRender: 'action' } }
]

const pagination = computed(() => ({
  current: pageNum.value,
  pageSize: pageSize.value,
  total: total.value,
  showSizeChanger: true,
  showTotal: (t: number) => `共 ${t} 条`
}))

onMounted(load)

async function load() {
  loading.value = true
  try {
    const data = await getUserListByPage({
      loginName: queryLoginName.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function search() {
  pageNum.value = 1
  load()
}

function onTableChange(pg: { current: number; pageSize: number }) {
  pageNum.value = pg.current
  pageSize.value = pg.pageSize
  load()
}

function openAdd() {
  Object.assign(form, { id: 0, loginName: '', name: '', password: '' })
  modalOpen.value = true
}

function openEdit(record: UserRow) {
  Object.assign(form, { id: record.id, loginName: record.loginName, name: record.name, password: '' })
  modalOpen.value = true
}

async function save() {
  if (form.id) {
    await saveUser({ id: form.id, name: form.name })
  } else {
    if (!form.loginName.trim() || !form.name.trim() || !form.password) {
      message.warning('请填写完整信息')
      return
    }
    if (!PASSWORD_PATTERN.test(form.password)) {
      message.warning('密码需包含数字和英文，长度 6~32 位')
      return
    }
    // 前端先做一次 MD5，后端再做一次，形成双重加密
    await saveUser({ loginName: form.loginName, name: form.name, password: md5(form.password) })
  }
  modalOpen.value = false
  message.success('保存成功')
  await load()
}

function openReset(record: UserRow) {
  resetId.value = record.id
  resetPwd.value = ''
  resetOpen.value = true
}

async function resetPassword() {
  if (!resetPwd.value) {
    message.warning('请输入新密码')
    return
  }
  if (!PASSWORD_PATTERN.test(resetPwd.value)) {
    message.warning('密码需包含数字和英文，长度 6~32 位')
    return
  }
  await resetPasswordApi({ id: resetId.value, password: md5(resetPwd.value) })
  resetOpen.value = false
  message.success('密码已重置')
}

async function remove(record: UserRow) {
  await removeUser(record.id)
  message.success('删除成功')
  await load()
}
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar h3 {
  margin: 0;
  font-size: 22px;
}

.actions {
  display: flex;
  gap: 8px;
}

.mono {
  font-family: Consolas, Menlo, monospace;
  font-size: 12px;
  color: var(--ink-soft);
}

.hint {
  color: var(--ink-soft);
  font-size: 13px;
}
</style>
