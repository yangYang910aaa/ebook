<template>
  <div class="doc-manage">
    <section class="panel tree-panel">
      <div class="tree-head">
        <h4 class="display">文档目录</h4>
        <a-button type="link" size="small" @click="addRoot">新增根文档</a-button>
      </div>
      <div class="tree-body">
        <a-tree
          :tree-data="treeData"
          :default-expand-all="true"
          :selected-keys="selectedKeys"
          :field-names="{ title: 'title', key: 'key', children: 'children' }"
          @select="onSelect"
        />
      </div>
      <div v-if="selectedDoc" class="tree-actions">
        <a-button size="small" @click="addChild">在选中下新增</a-button>
        <a-popconfirm :title="deleteTip" ok-text="删除" cancel-text="取消" @confirm="removeSelected">
          <a-button size="small" danger>删除选中</a-button>
        </a-popconfirm>
      </div>
    </section>

    <section class="panel form-panel">
      <div class="form-head">
        <div class="form-title">
          <h4 class="display">{{ form.id ? '编辑文档' : '新增文档' }}</h4>
          <span class="save-status" :class="{ dirty: isDirty }">
            <template v-if="isDirty">● 未保存</template>
            <template v-else>✓ 已保存<span v-if="savedAt"> · {{ savedAt }}</span></template>
          </span>
        </div>
        <a-button @click="previewVisible = true" :disabled="!form.content">内容预览</a-button>
      </div>
      <a-form layout="vertical">
        <a-form-item label="文档名称">
          <a-input v-model:value="form.name" placeholder="请输入文档名称" />
        </a-form-item>
        <a-form-item label="父文档">
          <a-tree-select
            v-model:value="form.parent"
            :tree-data="parentTreeData"
            :field-names="{ label: 'title', value: 'key', children: 'children' }"
            allow-clear
            placeholder="无（作为根文档）"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="顺序">
          <a-input-number v-model:value="form.sort" :min="0" style="width: 100%" />
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

    <a-drawer v-model:visible="previewVisible" :width="520">
      <template #title>
        <span class="drawer-title">内容预览</span>
      </template>
      <div class="preview-body" v-html="form.content"></div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
/* 文档管理（后台）：左侧文档目录树 + 右侧文档编辑表单（wangEditor 富文本）；支持新增根/子文档、编辑、删除（含子文档）、未保存修改确认、Ctrl+S 快捷保存 */
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import E from 'wangeditor'
import { deleteDocs, findContent, getAllDocs, saveDoc, type DocNode } from '../../api/doc'

const route = useRoute()
const ebookId = Number(route.query.ebookId || 0)

const docs = ref<DocNode[]>([])
const selectedKey = ref('')
const saving = ref(false)
const previewVisible = ref(false)
const editorRef = ref<HTMLDivElement>()
let editor: E | null = null

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
  disabled?: boolean
  children: TreeItem[]
}

// 左侧目录树数据：将后端返回的 DocNode 递归转为 antd Tree 需要的 { title, key, children } 结构
const treeData = computed(() => docs.value.map((d) => toTree(d, false)))
const selectedKeys = computed(() => (selectedKey.value ? [selectedKey.value] : []))
const selectedDoc = computed(() => findNode(docs.value, selectedKey.value))

// 脏检查：当前表单与初始快照不一致时提示"未保存"
const isDirty = computed(() => {
  const s = initialSnapshot.value
  if (!s) return false
  return form.name !== s.name || form.parent !== s.parent || form.sort !== s.sort || form.content !== s.content
})

// 父文档下拉树：编辑当前文档时，禁用当前节点及其所有子孙（防止选自己或后代作为父节点造成环）
const parentTreeData = computed(() => docs.value.map((d) => toTree(d, form.id ? String(form.id) === String(d.id) : false)))

// 删除确认提示：递归收集当前节点及所有子文档名称，让用户明确删除范围
const deleteTip = computed(() => {
  if (!selectedDoc.value) return '确定删除？'
  const names = collectNames(selectedDoc.value)
  return `将删除：${names.join('、')}，是否继续？`
})

/**
 * 将 DocNode 转为 TreeItem
 * @param d 文档节点
 * @param selfDisabled 是否禁用当前节点（编辑时防止选自己作为父文档）；禁用状态会递归传递给子孙
 */
function toTree(d: DocNode, selfDisabled: boolean): TreeItem {
  const disabled = selfDisabled
  return {
    title: d.name,
    key: String(d.id),
    disabled,
    children: d.children.map((c) => toTree(c, disabled))
  }
}

// 递归查找文档节点（深度优先）
function findNode(nodes: DocNode[], key: string): DocNode | null {
  for (const n of nodes) {
    if (String(n.id) === key) return n
    const hit = findNode(n.children || [], key)
    if (hit) return hit
  }
  return null
}

// 递归收集节点及其所有子孙的名称（删除确认提示用）
function collectNames(node: DocNode): string[] {
  const names = [node.name]
  ;(node.children || []).forEach((c) => names.push(...collectNames(c)))
  return names
}

// 统计字数：去除 HTML 标签和 &nbsp; 后计算字符数
function countWords(html: string): number {
  const text = html.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').replace(/\s+/g, '')
  return text.length
}

function formatTime(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

// 保存当前表单快照，用于脏检查对比
function takeSnapshot(): Snapshot {
  return { name: form.name, parent: form.parent, sort: form.sort, content: form.content }
}

/** 有未保存修改时先弹确认框，用户确认后再执行操作 */
function withConfirm(fn: () => void) {
  if (!isDirty.value) {
    fn()
    return
  }
  Modal.confirm({
    title: '有未保存的修改',
    content: '当前文档有未保存的修改，离开后将丢失，是否继续？',
    okText: '离开',
    cancelText: '取消',
    onOk: () => { fn() }
  })
}

// 全局键盘监听：Ctrl+S / Cmd+S 触发保存
function onKeydown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 's') {
    e.preventDefault()
    save()
  }
}

onMounted(async () => {
  if (!ebookId) {
    message.warning('缺少电子书参数')
    return
  }
  // 初始化 wangEditor 富文本编辑器
  editor = new E(editorRef.value as HTMLDivElement)
  editor.config.height = 420
  editor.config.placeholder = '请输入文档内容…'
  // 编辑器内容变化时同步到表单并更新字数
  editor.config.onchange = (html: string) => {
    form.content = html
    wordCount.value = countWords(html)
  }
  editor.create()
  window.addEventListener('keydown', onKeydown)
  await loadTree()
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeydown)
  editor?.destroy()
})

// 加载文档树，默认选中第一篇
async function loadTree() {
  docs.value = await getAllDocs(ebookId)
  if (docs.value.length && !selectedKey.value) {
    await selectDoc(String(docs.value[0].id))
  }
}

// 左侧树节点点击：有未保存修改时先确认
async function onSelect(keys: string[]) {
  if (keys.length) {
    withConfirm(() => { selectDoc(keys[0]) })
  }
}

// 选中并加载文档内容到表单；后台编辑不计阅读数（count=false）
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
  initialSnapshot.value = takeSnapshot() // 更新快照，重置脏状态
  wordCount.value = countWords(resp.content || '')
}

// 重置表单为新增状态
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

// 新增根文档：parent 为空
function addRoot() {
  withConfirm(() => {
    resetForm()
    form.parent = undefined
  })
}

// 在当前选中文档下新增子文档
function addChild() {
  if (!selectedDoc.value) {
    message.warning('请先在左侧选择一个父文档')
    return
  }
  const parentId = selectedDoc.value.id
  withConfirm(() => {
    resetForm()
    form.parent = parentId
  })
}

// 保存文档：新增（id=0）或更新，保存后刷新树并更新快照
async function save() {
  if (!form.name.trim()) {
    message.warning('请输入文档名称')
    return
  }
  saving.value = true
  try {
    await saveDoc({
      id: form.id || undefined,
      ebookId,
      parent: form.parent,
      name: form.name,
      sort: form.sort,
      content: form.content
    })
    message.success('保存成功')
    initialSnapshot.value = takeSnapshot() // 保存后更新快照，清除脏状态
    savedAt.value = formatTime(new Date())
    await loadTree()
  } finally {
    saving.value = false
  }
}

// 删除选中文档（含所有子文档），后端级联删除
async function removeSelected() {
  if (!selectedKey.value) return
  await deleteDocs(selectedKey.value)
  message.success('删除成功（含子文档）')
  resetForm()
  await loadTree()
}
</script>

<style scoped>
.doc-manage {
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
  max-height: 480px;
  overflow: auto;
}

.tree-actions {
  display: flex;
  gap: 8px;
  border-top: 1px solid var(--line);
  padding-top: 10px;
}

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
.form-panel :deep(.ant-select-selector:hover) {
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
  .doc-manage {
    grid-template-columns: 1fr;
  }
}
</style>
