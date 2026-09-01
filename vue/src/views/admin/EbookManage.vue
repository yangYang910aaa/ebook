<template>
  <div class="admin-page">
    <section class="panel">
      <div class="toolbar">
        <h3 class="display">电子书管理</h3>
        <div class="actions">
          <a-input v-model:value="queryName" placeholder="按名称模糊查询" allow-clear style="width: 200px" @press-enter="search" />
          <a-button @click="search">查询</a-button>
          <a-button type="primary" @click="openAdd">新增电子书</a-button>
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
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'cover'">
            <img v-if="record.cover" :src="record.cover" class="cover-thumb" :alt="record.name" />
            <span v-else class="cover-empty">-</span>
          </template>
          <template v-else-if="column.key === 'category'">
            {{ record.category1Name || '未分类' }}<template v-if="record.category2Name"> / {{ record.category2Name }}</template>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="goDocs(record)">文档管理</a-button>
              <a-popconfirm title="确定删除该电子书？" ok-text="删除" cancel-text="取消" @confirm="remove(record)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </section>

    <a-modal v-model:visible="modalOpen" :title="form.id ? '编辑电子书' : '新增电子书'" :width="560" @ok="save">
      <a-form layout="vertical">
        <a-form-item label="封面">
          <a-upload
            :show-upload-list="false"
            :custom-request="customUpload"
            accept="image/*"
          >
            <div v-if="form.cover" class="cover-upload">
              <img :src="form.cover" alt="封面" />
              <span class="cover-replace">点击更换</span>
            </div>
            <div v-else class="cover-upload cover-placeholder">点击上传封面<br />jpg/png/gif ≤ 10MB</div>
          </a-upload>
        </a-form-item>
        <a-form-item label="名称">
          <a-input v-model:value="form.name" placeholder="请输入电子书名称" />
        </a-form-item>
        <a-form-item label="分类">
          <a-space>
            <a-select v-model:value="form.category1Id" placeholder="一级分类" style="width: 220px" @change="onCategory1Change">
              <a-select-option v-for="c in category1List" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
            </a-select>
            <a-select v-model:value="form.category2Id" placeholder="二级分类" style="width: 220px">
              <a-select-option v-for="c in category2List" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
            </a-select>
          </a-space>
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="form.description" :rows="3" placeholder="电子书简介" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getCategoryList, type CategoryRow } from '../../api/category'
import { queryEbooks, removeEbook, saveEbook, uploadCover, type EbookRow } from '../../api/ebook'

const router = useRouter()

const list = ref<EbookRow[]>([])
const loading = ref(false)
const queryName = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const modalOpen = ref(false)
const allCategories = ref<CategoryRow[]>([])
const form = reactive({
  id: 0,
  name: '',
  category1Id: undefined as number | undefined,
  category2Id: undefined as number | undefined,
  description: '',
  cover: ''
})

const columns = [
  { title: '封面', key: 'cover', width: 80 },
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '分类', key: 'category', width: 160 },
  { title: '文档数', dataIndex: 'docCount', key: 'docCount', width: 80 },
  { title: '阅读', dataIndex: 'viewCount', key: 'viewCount', width: 80 },
  { title: '点赞', dataIndex: 'voteCount', key: 'voteCount', width: 80 },
  { title: '操作', key: 'action', width: 220 }
]

const pagination = computed(() => ({
  current: pageNum.value,
  pageSize: pageSize.value,
  total: total.value,
  showSizeChanger: true,
  showTotal: (t: number) => `共 ${t} 条`
}))

const category1List = computed(() => allCategories.value.filter((c) => c.parent === 0))
const category2List = computed(() =>
  allCategories.value.filter((c) => c.parent === form.category1Id)
)

onMounted(async () => {
  allCategories.value = await getCategoryList()
  await load()
})

async function load() {
  loading.value = true
  try {
    const data = await queryEbooks({
      name: queryName.value || undefined,
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
  Object.assign(form, { id: 0, name: '', category1Id: undefined, category2Id: undefined, description: '', cover: '' })
  modalOpen.value = true
}

function openEdit(record: EbookRow) {
  Object.assign(form, {
    id: record.id,
    name: record.name,
    category1Id: record.category1Id || undefined,
    category2Id: record.category2Id || undefined,
    description: record.description,
    cover: record.cover
  })
  modalOpen.value = true
}

function onCategory1Change() {
  form.category2Id = undefined
}

function customUpload(options: { file: File; onSuccess: (body: unknown) => void; onError: (err: Error) => void }) {
  uploadCover(options.file)
    .then((url) => {
      form.cover = url
      options.onSuccess(url)
      message.success('封面上传成功')
    })
    .catch((err) => {
      options.onError(err)
    })
}

async function save() {
  if (!form.name.trim()) {
    message.warning('请输入电子书名称')
    return
  }
  await saveEbook({
    id: form.id || undefined,
    name: form.name,
    category1Id: form.category1Id,
    category2Id: form.category2Id,
    description: form.description,
    cover: form.cover
  })
  modalOpen.value = false
  message.success('保存成功')
  await load()
}

async function remove(record: EbookRow) {
  await removeEbook(record.id)
  message.success('删除成功')
  await load()
}

function goDocs(record: EbookRow) {
  router.push({ path: '/admin/doc', query: { ebookId: record.id } })
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

.cover-thumb {
  width: 44px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid var(--line);
}

.cover-empty {
  color: var(--ink-soft);
}

.cover-upload {
  width: 120px;
  height: 150px;
  border: 1px dashed var(--line);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
  position: relative;
  text-align: center;
  font-size: 13px;
  color: var(--ink-soft);
  background: var(--paper);
}

.cover-upload img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-replace {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(45, 42, 36, 0.6);
  color: #fff;
  font-size: 12px;
  padding: 3px 0;
}
</style>
