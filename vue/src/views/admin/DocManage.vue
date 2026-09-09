<template>
  <div class="doc-manage">
    <!-- 电子书选择栏：先选分类，再选该分类下的电子书 -->
    <div class="ebook-bar panel">
      <span class="ebook-bar-label">分类</span>
      <a-select
        v-model:value="selectedCategoryId"
        style="width: 150px"
        @change="onCategoryChange"
      >
        <a-select-option :value="0">全部分类</a-select-option>
        <a-select-option v-for="c in categoryList" :key="c.id" :value="c.id">
          {{ c.name }}
        </a-select-option>
      </a-select>
      <span class="ebook-bar-label">电子书</span>
      <a-select
        v-model:value="currentEbookId"
        placeholder="请选择电子书"
        style="width: 280px"
        show-search
        option-filter-prop="children"
        @change="onEbookChange"
      >
        <a-select-option v-for="b in filteredEbooks" :key="b.id" :value="b.id">
          {{ b.name }}
        </a-select-option>
      </a-select>
      <span v-if="currentEbookId" class="ebook-bar-hint">{{ currentEbookName }} · 共 {{ docTotalCount }} 章</span>
    </div>

    <div v-if="currentEbookId" class="doc-manage-body">
      <!-- 左侧：文档目录树 -->
      <section class="panel tree-panel">
        <div class="tree-head">
          <h4 class="display">章节目录</h4>
        </div>
        <div class="tree-body">
          <a-empty v-if="!docs.length" description="暂无章节，点击下方按钮新增" :image="emptyImage" />
          <a-tree
            v-else
            :tree-data="treeData"
            :expanded-keys="expandedKeys"
            :selected-keys="selectedKeys"
            :field-names="{ title: 'title', key: 'key', children: 'children' }"
            @select="onSelect"
            @expand="onExpand"
          />
        </div>
        <div class="tree-actions">
          <a-button size="small" type="primary" @click="addRoot">新增章节</a-button>
          <a-button size="small" @click="addChild" :disabled="!selectedDoc">新增子章节</a-button>
          <a-popconfirm v-if="selectedDoc" :title="deleteTip" ok-text="删除" cancel-text="取消" @confirm="removeSelected">
            <a-button size="small" danger>删除</a-button>
          </a-popconfirm>
        </div>
        <div class="tree-tip">
          <span>「新增章节」= 一级目录</span>
          <span>「新增子章节」= 当前章节的下一级</span>
        </div>
      </section>

      <!-- 右侧：编辑表单 -->
      <section class="panel form-panel">
        <div class="form-head">
          <div class="form-title">
            <h4 class="display">{{ form.id ? '编辑章节' : '新增章节' }}</h4>
            <span class="save-status" :class="{ dirty: isDirty }">
              <template v-if="isDirty">● 未保存</template>
              <template v-else>✓ 已保存<span v-if="savedAt"> · {{ savedAt }}</span></template>
            </span>
          </div>
          <a-button @click="previewVisible = true" :disabled="!form.content">内容预览</a-button>
        </div>
        <a-form layout="vertical">
          <a-form-item label="章节名称">
            <a-input v-model:value="form.name" placeholder="请输入章节名称，如：第一章 概述" />
          </a-form-item>
          <a-form-item label="父章节">
            <a-tree-select
              v-model:value="form.parent"
              :tree-data="parentTreeData"
              :field-names="{ label: 'title', value: 'key', children: 'children' }"
              allow-clear
              placeholder="不选 = 作为一级章节"
              style="width: 100%"
              tree-default-expand-all
            />
            <div class="form-item-hint">当前章节及其子章节不会出现在列表中，防止循环嵌套</div>
          </a-form-item>
          <a-form-item label="顺序">
            <a-input-number v-model:value="form.sort" :min="0" style="width: 100%" placeholder="数字越小越靠前" />
          </a-form-item>
          <a-form-item label="内容">
            <div ref="editorRef" class="editor"></div>
          </a-form-item>
          <div class="editor-status">
            <span class="status-words">共 {{ wordCount }} 字</span>
            <span class="status-tip">Ctrl + S 快速保存</span>
          </div>
          <div class="form-actions">
            <a-button type="primary" :loading="saving" @click="save">保存</a-button>
            <a-button @click="onResetClick">清空</a-button>
          </div>
        </a-form>
      </section>
    </div>

    <!-- 未选书时的空状态 -->
    <div v-else class="empty-tip panel">
      <a-empty description="请先在上方选择要管理的电子书" />
    </div>

    <!-- 内容预览抽屉 -->
    <a-drawer v-model:visible="previewVisible" :width="560">
      <template #title>
        <span class="drawer-title">内容预览 · {{ form.name || '未命名' }}</span>
      </template>
      <div class="preview-body" v-html="form.content"></div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
/* 文档管理（后台）优化版：电子书选择器 + 左侧文档树 + 右侧富文本编辑；
   父文档下拉过滤不可选项（而非禁用）；保存后自动选中新文档；空状态提示 */
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import E from 'wangeditor'
import { deleteDocs, findContent, getAllDocs, saveDoc, type DocNode } from '../../api/doc'
import { queryEbooks, type EbookRow } from '../../api/ebook'
import { getCategoryList, type CategoryRow } from '../../api/category'

const route = useRoute()

// ===== 电子书选择 =====
const ebookList = ref<EbookRow[]>([])
const currentEbookId = ref<number | undefined>(route.query.ebookId ? Number(route.query.ebookId) : undefined)
const currentEbookName = computed(() => ebookList.value.find(b => b.id === currentEbookId.value)?.name || '')

// 分类筛选：先选分类，再选该分类下的电子书
const categoryList = ref<CategoryRow[]>([])       // 一级分类列表
const selectedCategoryId = ref<number>(0)          // 0 = 全部分类

// 根据选中分类过滤电子书
const filteredEbooks = computed(() => {
  if (selectedCategoryId.value === 0) return ebookList.value
  return ebookList.value.filter((b) => b.category1Id === selectedCategoryId.value)
})

// ===== 文档树 =====
const docs = ref<DocNode[]>([])
const selectedKey = ref('')
const expandedKeys = ref<string[]>([])
const saving = ref(false)
const previewVisible = ref(false)
const editorRef = ref<HTMLDivElement>()
let editor: E | null = null

// 文档总数（递归统计）
const docTotalCount = computed(() => {
  let count = 0
  const walk = (nodes: DocNode[]) => {
    for (const n of nodes) {
      count++
      walk(n.children || [])
    }
  }
  walk(docs.value)
  return count
})

interface Snapshot {
  name: string
  parent: number | undefined
  sort: number
  content: string
}
const initialSnapshot = ref<Snapshot | null>(null)
const wordCount = ref(0)
const savedAt = ref('')

const form = reactive({ id: 0, name: '', parent: undefined as number | undefined, sort: 0, content: '' })

interface TreeItem {
  title: string
  key: string
  children: TreeItem[]
}

// 左侧目录树
const treeData = computed(() => docs.value.map((d) => toTree(d)))
const selectedKeys = computed(() => (selectedKey.value ? [selectedKey.value] : []))
const selectedDoc = computed(() => findNode(docs.value, selectedKey.value))

// 脏检查
const isDirty = computed(() => {
  const s = initialSnapshot.value
  if (!s) return false
  return form.name !== s.name || form.parent !== s.parent || form.sort !== s.sort || form.content !== s.content
})

// 父文档下拉：过滤掉当前文档及其子孙（而非禁用），用户看到的都是可选项
const parentTreeData = computed(() => {
  if (!form.id) return docs.value.map((d) => toTree(d))
  const current = findNode(docs.value, String(form.id))
  if (!current) return docs.value.map((d) => toTree(d))
  const excludeIds = collectDescendantIds(current)
  return filterTree(docs.value, excludeIds)
})

// 删除确认提示
const deleteTip = computed(() => {
  if (!selectedDoc.value) return '确定删除？'
  const names = collectNames(selectedDoc.value)
  return `将删除：${names.join('、')}，是否继续？`
})

// antd empty 组件默认图太大，用简单占位
const emptyImage = ''

function toTree(d: DocNode): TreeItem {
  return {
    title: d.name,
    key: String(d.id),
    children: (d.children || []).map((c) => toTree(c))
  }
}

// 递归收集节点及其所有子孙 ID（父文档下拉过滤用）
function collectDescendantIds(node: DocNode): Set<number> {
  const ids = new Set<number>([node.id])
  ;(node.children || []).forEach((c) => {
    collectDescendantIds(c).forEach((id) => ids.add(id))
  })
  return ids
}

// 过滤树：排除指定 ID 的节点
function filterTree(nodes: DocNode[], excludeIds: Set<number>): TreeItem[] {
  return nodes
    .filter((n) => !excludeIds.has(n.id))
    .map((n) => ({
      title: n.name,
      key: String(n.id),
      children: filterTree(n.children || [], excludeIds)
    }))
}

function findNode(nodes: DocNode[], key: string): DocNode | null {
  for (const n of nodes) {
    if (String(n.id) === key) return n
    const hit = findNode(n.children || [], key)
    if (hit) return hit
  }
  return null
}

// 按名称查找节点（保存后自动选中用）
function findNodeByName(nodes: DocNode[], name: string): DocNode | null {
  for (const n of nodes) {
    if (n.name === name) return n
    const hit = findNodeByName(n.children || [], name)
    if (hit) return hit
  }
  return null
}

function collectNames(node: DocNode): string[] {
  const names = [node.name]
  ;(node.children || []).forEach((c) => names.push(...collectNames(c)))
  return names
}

function countWords(html: string): number {
  const text = html.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').replace(/\s+/g, '')
  return text.length
}

function formatTime(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function takeSnapshot(): Snapshot {
  return { name: form.name, parent: form.parent, sort: form.sort, content: form.content }
}

function withConfirm(fn: () => void) {
  if (!isDirty.value) {
    fn()
    return
  }
  Modal.confirm({
    title: '有未保存的修改',
    content: '当前章节有未保存的修改，离开后将丢失，是否继续？',
    okText: '离开',
    cancelText: '取消',
    onOk: () => { fn() }
  })
}

function onKeydown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 's') {
    e.preventDefault()
    save()
  }
}

// 初始化 wangEditor 编辑器（确保容器已渲染后才调用）
function initEditor() {
  if (editor || !editorRef.value) return
  editor = new E(editorRef.value)
  editor.config.height = 420
  editor.config.placeholder = '请输入章节内容…'
  editor.config.onchange = (html: string) => {
    form.content = html
    wordCount.value = countWords(html)
  }
  editor.create()
}

onMounted(async () => {
  // 加载一级分类列表
  try {
    const cats = await getCategoryList()
    categoryList.value = cats.filter((c) => c.parent === 0)
  } catch {
    categoryList.value = []
  }

  // 加载电子书列表（用于选择器）
  try {
    const data = await queryEbooks({ pageNum: 1, pageSize: 1000 })
    ebookList.value = data.list
  } catch {
    ebookList.value = []
  }

  window.addEventListener('keydown', onKeydown)

  // 如果 URL 带了 ebookId，自动选中对应分类，初始化编辑器并加载文档
  if (currentEbookId.value) {
    const book = ebookList.value.find((b) => b.id === currentEbookId.value)
    if (book) selectedCategoryId.value = book.category1Id || 0
    initEditor()
    await loadTree()
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeydown)
  editor?.destroy()
})

// 切换分类：如果当前选中的电子书不在新分类下，清空选择
function onCategoryChange() {
  if (!currentEbookId.value) return
  const stillExists = filteredEbooks.value.some((b) => b.id === currentEbookId.value)
  if (!stillExists) {
    withConfirm(() => {
      currentEbookId.value = undefined
      resetForm()
      docs.value = []
    })
  }
}

// 切换电子书
async function onEbookChange(id: number) {
  withConfirm(async () => {
    currentEbookId.value = id
    // 第一次选择电子书时，容器刚从 v-if 渲染出来，等待 DOM 就绪后初始化编辑器
    await nextTick()
    initEditor()
    resetForm()
    await loadTree()
  })
}

// 加载文档树，默认展开全部并选中第一篇
async function loadTree() {
  if (!currentEbookId.value) return
  docs.value = await getAllDocs(currentEbookId.value)
  // 默认展开所有节点
  expandedKeys.value = collectAllKeys(docs.value)
  if (docs.value.length && !selectedKey.value) {
    await selectDoc(String(docs.value[0].id))
  }
}

// 收集所有节点 key（用于默认展开）
function collectAllKeys(nodes: DocNode[]): string[] {
  const keys: string[] = []
  const walk = (list: DocNode[]) => {
    for (const n of list) {
      keys.push(String(n.id))
      walk(n.children || [])
    }
  }
  walk(nodes)
  return keys
}

function onExpand(keys: string[]) {
  expandedKeys.value = keys
}

async function onSelect(keys: string[]) {
  if (keys.length) {
    withConfirm(() => { selectDoc(keys[0]) })
  }
}

// 选中并加载文档内容（后台编辑不计阅读数）
async function selectDoc(id: string) {
  selectedKey.value = id
  const node = findNode(docs.value, id)
  if (!node) return
  const resp = await findContent(Number(id), false)
  Object.assign(form, {
    id: node.id,
    name: node.name,
    parent: node.parent || undefined,
    sort: node.sort,
    content: resp.content || ''
  })
  editor?.txt.html(resp.content || '')
  initialSnapshot.value = takeSnapshot()
  wordCount.value = countWords(resp.content || '')
}

function resetForm() {
  selectedKey.value = ''
  Object.assign(form, { id: 0, name: '', parent: undefined, sort: 0, content: '' })
  editor?.txt.html('')
  initialSnapshot.value = takeSnapshot()
  wordCount.value = 0
}

function onResetClick() {
  withConfirm(() => resetForm())
}

// 新增根文档（一级章节）
function addRoot() {
  withConfirm(() => {
    resetForm()
    form.parent = undefined
    form.sort = 0
  })
}

// 在当前选中文档下新增子文档
function addChild() {
  if (!selectedDoc.value) {
    message.warning('请先在左侧选择一个父章节')
    return
  }
  const parentId = selectedDoc.value.id
  withConfirm(() => {
    resetForm()
    form.parent = parentId
    form.sort = 0
  })
}

// 保存文档：新增或更新，保存后刷新树并自动选中刚保存的文档
async function save() {
  if (!form.name.trim()) {
    message.warning('请输入章节名称')
    return
  }
  if (!currentEbookId.value) {
    message.warning('请先选择电子书')
    return
  }
  saving.value = true
  const savedName = form.name
  const isNew = !form.id
  try {
    await saveDoc({
      id: form.id || undefined,
      ebookId: currentEbookId.value,
      parent: form.parent,
      name: form.name,
      sort: form.sort,
      content: form.content
    })
    message.success('保存成功')
    initialSnapshot.value = takeSnapshot()
    savedAt.value = formatTime(new Date())
    await loadTree()
    // 新增文档后自动选中它（按名称匹配）
    if (isNew) {
      const found = findNodeByName(docs.value, savedName)
      if (found) {
        selectedKey.value = String(found.id)
        await selectDoc(String(found.id))
      }
    }
  } finally {
    saving.value = false
  }
}

// 删除选中文档（含所有子文档）
async function removeSelected() {
  if (!selectedKey.value) return
  await deleteDocs(selectedKey.value)
  message.success('删除成功（含子章节）')
  resetForm()
  await loadTree()
}
</script>

<style scoped>
.doc-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ===== 电子书选择栏 ===== */
.ebook-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
}

.ebook-bar-label {
  font-family: var(--serif);
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
  white-space: nowrap;
}

.ebook-bar-hint {
  font-size: 13px;
  color: var(--ink-soft);
}

/* ===== 主体两栏 ===== */
.doc-manage-body {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 16px;
  align-items: start;
}

.tree-panel {
  position: sticky;
  top: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tree-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--line);
  padding-bottom: 10px;
}

.tree-head h4,
.form-head h4 {
  margin: 0;
  font-size: 17px;
}

.tree-body {
  max-height: 420px;
  overflow: auto;
}

.tree-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  border-top: 1px solid var(--line);
  padding-top: 10px;
}

.tree-tip {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 10px;
  background: var(--paper);
  border-radius: 6px;
  font-size: 11px;
  color: var(--ink-soft);
  line-height: 1.5;
}

/* ===== 表单 ===== */
.form-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.form-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.save-status {
  font-size: 12px;
  color: var(--teal);
  font-weight: 500;
}

.save-status.dirty {
  color: var(--accent);
}

.form-item-hint {
  font-size: 11px;
  color: var(--ink-soft);
  margin-top: 4px;
  line-height: 1.4;
}

.editor {
  z-index: 1;
}

.editor-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 4px 0;
  margin: -8px 0 12px;
  font-size: 12px;
  color: var(--ink-soft);
  border-top: 1px dashed var(--line);
}

.status-tip {
  opacity: 0.75;
}

.form-actions {
  display: flex;
  gap: 10px;
}

/* ===== 空状态 ===== */
.empty-tip {
  display: flex;
  justify-content: center;
  padding: 60px 20px;
}

/* ===== 表单控件美化 ===== */
.form-panel :deep(.ant-form-item-label > label) {
  font-family: var(--serif);
  font-weight: 600;
  color: var(--ink);
  font-size: 14px;
}

.form-panel :deep(.ant-input),
.form-panel :deep(.ant-input-number),
.form-panel :deep(.ant-select-selector) {
  border-radius: 6px;
}

.form-panel :deep(.ant-input):hover,
.form-panel :deep(.ant-input-number:hover),
.form-panel :deep(.ant-select-selector):hover {
  border-color: var(--accent-soft);
}

.form-panel :deep(.ant-input):focus,
.form-panel :deep(.ant-input-focused),
.form-panel :deep(.ant-input-number-focused),
.form-panel :deep(.ant-select-focused .ant-select-selector) {
  border-color: var(--accent);
  box-shadow: 0 0 0 2px rgba(176, 58, 46, 0.12);
}

/* ===== 预览抽屉 ===== */
.drawer-title {
  font-family: var(--serif);
  font-size: 16px;
  font-weight: 600;
  color: var(--ink);
}

.preview-body {
  font-size: 15px;
  line-height: 1.9;
  color: #3a352d;
}

.preview-body :deep(img),
.preview-body :deep(video) {
  max-width: 100%;
  height: auto;
}

@media (max-width: 900px) {
  .doc-manage-body {
    grid-template-columns: 1fr;
  }
  .tree-panel {
    position: static;
  }
}
</style>
