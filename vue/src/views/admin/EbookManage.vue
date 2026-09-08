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
        <template #cover="{ record }">
          <img v-if="record.cover" :src="record.cover" class="cover-thumb" :alt="record.name" />
          <span v-else class="cover-empty">-</span>
        </template>
        <template #category="{ record }">
          {{ record.category1Name || '未分类' }}<template v-if="record.category2Name"> / {{ record.category2Name }}</template>
        </template>
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" size="small" @click="goDocs(record)">文档管理</a-button>
            <a-popconfirm title="确定删除该电子书？" ok-text="删除" cancel-text="取消" @confirm="remove(record)">
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </section>

    <a-modal v-model:visible="modalOpen" :width="680" ok-text="确定" cancel-text="取消" @ok="save">
      <template #title>
        <span class="modal-title">{{ form.id ? '编辑电子书' : '新增电子书' }}</span>
      </template>
      <div class="ebook-form">
        <div class="cover-section">
          <div class="field-label">封面</div>
          <a-upload :show-upload-list="false" :custom-request="customUpload" accept="image/*">
            <div v-if="form.cover" class="cover-box has-cover">
              <img :src="form.cover" alt="封面" />
              <div class="cover-mask">点击更换</div>
            </div>
            <div v-else class="cover-box empty">
              <div class="cover-plus">+</div>
              <div class="cover-hint">点击上传封面</div>
              <div class="cover-sub">jpg / png / gif ≤ 10MB</div>
            </div>
          </a-upload>
        </div>
        <div class="fields-section">
          <a-form layout="vertical">
            <a-form-item label="名称">
              <a-input v-model:value="form.name" placeholder="请输入电子书名称" />
            </a-form-item>
            <a-form-item label="分类">
              <div class="category-row">
                <a-select v-model:value="form.category1Id" placeholder="一级分类" @change="onCategory1Change">
                  <a-select-option v-for="c in category1List" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
                </a-select>
                <a-select v-model:value="form.category2Id" placeholder="二级分类">
                  <a-select-option v-for="c in category2List" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
                </a-select>
              </div>
            </a-form-item>
            <a-form-item label="描述">
              <a-textarea v-model:value="form.description" :rows="4" placeholder="电子书简介，支持多行" />
            </a-form-item>
          </a-form>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
/* 电子书管理（后台）：电子书分页列表、模糊搜索、新增/编辑/删除、封面上传、跳转文档管理 */
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
  { title: '封面', key: 'cover', width: 80, slots: { customRender: 'cover' } },
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '分类', key: 'category', width: 160, slots: { customRender: 'category' } },
  { title: '文档数', dataIndex: 'docCount', key: 'docCount', width: 80 },
  { title: '阅读', dataIndex: 'viewCount', key: 'viewCount', width: 80 },
  { title: '点赞', dataIndex: 'voteCount', key: 'voteCount', width: 80 },
  { title: '操作', key: 'action', width: 220, slots: { customRender: 'action' } }
]

const pagination = computed(() => ({
  current: pageNum.value,
  pageSize: pageSize.value,
  total: total.value,
  showSizeChanger: true,
  showTotal: (t: number) => `共 ${t} 条`
}))

// 一级分类列表（parent=0）
const category1List = computed(() => allCategories.value.filter((c) => c.parent === 0))
// 二级分类列表：根据当前选中的一级分类动态筛选
const category2List = computed(() =>
  allCategories.value.filter((c) => c.parent === form.category1Id)
)

onMounted(async () => {
  allCategories.value = await getCategoryList()
  await load()
})

// 分页加载电子书列表
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

// 搜索：重置到第一页再加载
function search() {
  pageNum.value = 1
  load()
}

// 表格分页变化
function onTableChange(pg: { current: number; pageSize: number }) {
  pageNum.value = pg.current
  pageSize.value = pg.pageSize
  load()
}

// 打开新增弹窗：重置表单
function openAdd() {
  Object.assign(form, { id: 0, name: '', category1Id: undefined, category2Id: undefined, description: '', cover: '' })
  modalOpen.value = true
}

// 打开编辑弹窗：回填当前电子书数据
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

// 一级分类变化时清空已选的二级分类（级联联动）
function onCategory1Change() {
  form.category2Id = undefined
}

// 自定义封面上传：调用上传接口，成功后将返回的 URL 存入表单
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

// 保存电子书：新增（id=0）或更新
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

// 删除电子书
async function remove(record: EbookRow) {
  await removeEbook(record.id)
  message.success('删除成功')
  await load()
}

// 跳转到该电子书的文档管理页面
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

/* ===== 编辑弹窗美化 ===== */
.modal-title {
  font-family: var(--serif);
  font-size: 17px;
  font-weight: 600;
  color: var(--ink);
  letter-spacing: 0.02em;
}

.ebook-form {
  display: flex;
  gap: 28px;
  align-items: flex-start;
}

.cover-section {
  flex-shrink: 0;
}

.field-label {
  font-family: var(--serif);
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
  margin-bottom: 10px;
}

.cover-box {
  width: 170px;
  height: 215px;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  box-shadow: var(--shadow-sm);
  transition: box-shadow 0.2s, transform 0.2s;
}

.cover-box:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.cover-box.has-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-box.has-cover .cover-mask {
  position: absolute;
  inset: 0;
  background: rgba(45, 42, 36, 0.55);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  letter-spacing: 0.05em;
  opacity: 0;
  transition: opacity 0.2s;
}

.cover-box.has-cover:hover .cover-mask {
  opacity: 1;
}

.cover-box.empty {
  border: 1.5px dashed var(--line);
  background: var(--paper);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: border-color 0.2s, background 0.2s;
  box-shadow: none;
}

.cover-box.empty:hover {
  border-color: var(--accent-soft);
  background: #faf5ea;
  transform: none;
}

.cover-plus {
  font-size: 34px;
  color: var(--accent-soft);
  line-height: 1;
  font-weight: 300;
}

.cover-hint {
  font-size: 13px;
  color: var(--ink);
  font-weight: 500;
}

.cover-sub {
  font-size: 11px;
  color: var(--ink-soft);
}

.fields-section {
  flex: 1;
  min-width: 0;
}

.category-row {
  display: flex;
  gap: 10px;
}

.category-row :deep(.ant-select) {
  flex: 1;
  min-width: 0;
}

.fields-section :deep(.ant-form-item-label > label) {
  font-family: var(--serif);
  font-weight: 600;
  color: var(--ink);
  font-size: 14px;
}

.fields-section :deep(.ant-input),
.fields-section :deep(.ant-select-selector),
.fields-section :deep(.ant-input-affix-wrapper) {
  border-radius: 6px;
}

.fields-section :deep(.ant-input):hover,
.fields-section :deep(.ant-select-selector:hover) {
  border-color: var(--accent-soft);
}

.fields-section :deep(.ant-input):focus,
.fields-section :deep(.ant-input-focused),
.fields-section :deep(.ant-select-focused .ant-select-selector) {
  border-color: var(--accent);
  box-shadow: 0 0 0 2px rgba(176, 58, 46, 0.12);
}
</style>
