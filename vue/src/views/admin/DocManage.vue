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
        <h4 class="display">{{ form.id ? '编辑文档' : '新增文档' }}</h4>
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
        <div class="form-actions">
          <a-button type="primary" :loading="saving" @click="save">保存</a-button>
          <a-button @click="resetForm">清空</a-button>
        </div>
      </a-form>
    </section>

    <a-drawer v-model:visible="previewVisible" title="内容预览" width="520">
      <div class="preview-body" v-html="form.content"></div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
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

const form = reactive({ id: 0, name: '', parent: undefined as number | undefined, sort: 0, content: '' })

interface TreeItem {
  title: string
  key: string
  disabled?: boolean
  children: TreeItem[]
}

const treeData = computed(() => docs.value.map((d) => toTree(d, false)))
const selectedKeys = computed(() => (selectedKey.value ? [selectedKey.value] : []))
const selectedDoc = computed(() => findNode(docs.value, selectedKey.value))

// 父文档选择：编辑时当前节点及其子孙禁用
const parentTreeData = computed(() => docs.value.map((d) => toTree(d, form.id ? String(form.id) === String(d.id) : false)))

const deleteTip = computed(() => {
  if (!selectedDoc.value) return '确定删除？'
  const names = collectNames(selectedDoc.value)
  return `将删除：${names.join('、')}，是否继续？`
})

function toTree(d: DocNode, selfDisabled: boolean): TreeItem {
  const disabled = selfDisabled
  return {
    title: d.name,
    key: String(d.id),
    disabled,
    children: d.children.map((c) => toTree(c, disabled))
  }
}

function findNode(nodes: DocNode[], key: string): DocNode | null {
  for (const n of nodes) {
    if (String(n.id) === key) return n
    const hit = findNode(n.children || [], key)
    if (hit) return hit
  }
  return null
}

function collectNames(node: DocNode): string[] {
  const names = [node.name]
  ;(node.children || []).forEach((c) => names.push(...collectNames(c)))
  return names
}

onMounted(async () => {
  if (!ebookId) {
    message.warning('缺少电子书参数')
    return
  }
  editor = new E(editorRef.value as HTMLDivElement)
  editor.config.height = 420
  editor.config.placeholder = '请输入文档内容…'
  editor.config.onchange = (html: string) => {
    form.content = html
  }
  editor.create()
  await loadTree()
})

onBeforeUnmount(() => {
  editor?.destroy()
})

async function loadTree() {
  docs.value = await getAllDocs(ebookId)
  if (docs.value.length && !selectedKey.value) {
    await selectDoc(String(docs.value[0].id))
  }
}

async function onSelect(keys: string[]) {
  if (keys.length) {
    await selectDoc(keys[0])
  }
}

async function selectDoc(id: string) {
  selectedKey.value = id
  const node = findNode(docs.value, id)
  if (!node) return
  // 后台编辑/预览不计阅读数（count=false）
  const resp = await findContent(Number(id), false)
  Object.assign(form, {
    id: node.id,
    name: node.name,
    parent: node.parent || undefined,
    sort: node.sort,
    content: resp.content || ''
  })
  editor?.txt.html(resp.content || '')
}

function resetForm() {
  selectedKey.value = ''
  Object.assign(form, { id: 0, name: '', parent: undefined, sort: 0, content: '' })
  editor?.txt.html('')
}

function addRoot() {
  resetForm()
  form.parent = undefined
}

function addChild() {
  if (!selectedDoc.value) {
    message.warning('请先在左侧选择一个父文档')
    return
  }
  resetForm()
  form.parent = selectedDoc.value.id
}

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
    await loadTree()
  } finally {
    saving.value = false
  }
}

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

.editor {
  z-index: 1;
}

.form-actions {
  display: flex;
  gap: 10px;
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
