<template>
  <div class="doc-read-page">
    <aside class="doc-tree panel">
      <h4 class="display">目录</h4>
      <a-tree
        :tree-data="treeData"
        :default-expand-all="true"
        :selected-keys="selectedKeys"
        field-names="{ title: 'title', key: 'key', children: 'children' }"
        @select="onSelect"
      />
    </aside>

    <article class="doc-content panel">
      <template v-if="current">
        <header class="doc-head">
          <h1 class="display">{{ current.name }}</h1>
          <div class="doc-meta">
            <span>阅读 {{ current.viewCount }}</span>
            <span>点赞 {{ current.voteCount }}</span>
            <button class="vote-btn" :disabled="voting" @click="vote">
              <span class="vote-heart">♥</span> 点赞
            </button>
          </div>
        </header>
        <div class="content-body" v-html="contentHtml"></div>
      </template>
      <div v-else class="empty">
        <p>从左侧目录选择一篇文档开始阅读</p>
      </div>
    </article>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { findContent, getAllDocs, voteDoc, type DocNode } from '../api/doc'

const route = useRoute()

const docs = ref<DocNode[]>([])
const currentKey = ref<string>('')
const contentHtml = ref('')
const voting = ref(false)

const ebookId = Number(route.query.ebookId || 0)

const treeData = computed(() => docs.value.map((d) => toTree(d)))

const selectedKeys = computed(() => (currentKey.value ? [currentKey.value] : []))

const current = computed(() => findNode(docs.value, currentKey.value))

interface TreeItem {
  title: string
  key: string
  children: TreeItem[]
}

function toTree(d: DocNode): TreeItem {
  return {
    title: d.name,
    key: String(d.id),
    children: d.children.map((c) => toTree(c))
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

onMounted(async () => {
  if (!ebookId) {
    message.warning('缺少电子书参数')
    return
  }
  try {
    docs.value = await getAllDocs(ebookId)
    const first = docs.value[0]
    if (first) {
      await openDoc(String(first.id))
    }
  } catch {
    // 拦截器已提示
  }
})

async function onSelect(keys: string[]) {
  if (keys.length) {
    await openDoc(keys[0])
  }
}

async function openDoc(id: string) {
  currentKey.value = id
  const resp = await findContent(Number(id))
  contentHtml.value = resp.content || ''
  // 阅读数本地 +1
  const node = findNode(docs.value, id)
  if (node) {
    node.viewCount = (node.viewCount || 0) + 1
  }
}

async function vote() {
  if (!currentKey.value) return
  voting.value = true
  try {
    await voteDoc(Number(currentKey.value))
    const node = findNode(docs.value, currentKey.value)
    if (node) {
      node.voteCount = (node.voteCount || 0) + 1
    }
    message.success('点赞成功')
  } catch {
    // 已点赞等提示由拦截器处理
  } finally {
    voting.value = false
  }
}
</script>

<style scoped>
.doc-read-page {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 20px;
  align-items: start;
}

.doc-tree {
  position: sticky;
  top: 16px;
}

.doc-tree h4 {
  margin: 0 0 12px;
  font-size: 16px;
  color: var(--ink);
  border-bottom: 1px solid var(--line);
  padding-bottom: 10px;
}

.doc-tree :deep(.ant-tree) {
  background: transparent;
}

.doc-content {
  min-height: 480px;
}

.doc-head {
  border-bottom: 1px solid var(--line);
  padding-bottom: 14px;
  margin-bottom: 20px;
}

.doc-head h1 {
  margin: 0 0 10px;
  font-size: 26px;
}

.doc-meta {
  display: flex;
  align-items: center;
  gap: 18px;
  color: var(--ink-soft);
  font-size: 13px;
}

.vote-btn {
  border: 1px solid rgba(176, 58, 46, 0.4);
  background: rgba(176, 58, 46, 0.08);
  color: var(--accent-deep);
  border-radius: 999px;
  padding: 5px 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.vote-btn:hover:not(:disabled) {
  background: var(--accent);
  color: #fff;
}

.vote-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.vote-heart {
  color: var(--accent);
}

.content-body {
  font-size: 15px;
  line-height: 1.9;
  color: #3a352d;
}

.content-body :deep(img),
.content-body :deep(video) {
  max-width: 100%;
  height: auto;
}

.content-body :deep(h1),
.content-body :deep(h2),
.content-body :deep(h3) {
  font-family: var(--serif);
}

.empty {
  text-align: center;
  color: var(--ink-soft);
  padding: 80px 0;
}

@media (max-width: 900px) {
  .doc-read-page {
    grid-template-columns: 1fr;
  }
}
</style>
