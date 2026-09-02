<template>
  <div class="ebook-list-page">
    <header class="page-head">
      <h2 class="display">{{ categoryName }}</h2>
      <span class="total">共 {{ total }} 本</span>
    </header>

    <div class="ebook-grid">
      <article v-for="eb in list" :key="eb.id" class="ebook-card" @click="openEbook(eb)">
        <div class="cover">
          <img v-if="eb.cover && !broken[eb.id]" :src="eb.cover" :alt="eb.name" @error="onImgError(eb.id)" />
          <div v-else class="cover-fallback display">{{ eb.name.charAt(0) }}</div>
        </div>
        <div class="ebook-info">
          <h3 class="display">{{ eb.name }}</h3>
          <p class="desc">{{ eb.description || '暂无简介' }}</p>
          <div class="meta">
            <span class="tag">{{ eb.category2Name || '未分类' }}</span>
            <span>{{ eb.docCount }} 篇</span>
            <span>{{ eb.viewCount }} 阅读</span>
            <span>{{ eb.voteCount }} 赞</span>
          </div>
        </div>
      </article>
    </div>

    <div v-if="total > pageSize" class="pager">
      <a-pagination v-model:current="pageNum" :total="total" :page-size="pageSize" @change="load" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { queryEbooks, type EbookRow } from '../api/ebook'
import { getCategoryList } from '../api/category'

const route = useRoute()
const router = useRouter()

const list = ref<EbookRow[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 9
const broken = reactive<Record<number, boolean>>({})

const categoryName = ref(route.query.category2Id ? '分类' : '全部电子书')

onMounted(refresh)

// 左侧菜单切换分类：路由 query 变化时组件被复用，需监听并重新加载
watch(() => route.query.category2Id, refresh)

async function refresh() {
  const category2Id = Number(route.query.category2Id || 0)
  if (category2Id) {
    try {
      const cats = await getCategoryList()
      const hit = cats.find((c) => c.id === category2Id)
      categoryName.value = hit ? hit.name : `分类 · ${category2Id}`
    } catch {
      // 忽略
    }
  } else {
    categoryName.value = '全部电子书'
  }
  pageNum.value = 1
  await load()
}

async function load() {
  const category2Id = Number(route.query.category2Id || 0) || undefined
  const data = await queryEbooks({
    name: undefined,
    category2Id,
    pageNum: pageNum.value,
    pageSize
  })
  list.value = data.list
  total.value = data.total
}

function openEbook(eb: EbookRow) {
  router.push({ path: '/doc', query: { ebookId: eb.id } })
}

function onImgError(id: number) {
  broken[id] = true
}
</script>

<style scoped>
.ebook-list-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-head {
  display: flex;
  align-items: baseline;
  gap: 14px;
  padding: 8px 4px 0;
  animation: page-fade 0.5s ease both;
}

.page-head h2 {
  margin: 0;
  font-size: 26px;
}

.total {
  color: var(--ink-soft);
  font-size: 13px;
}

.ebook-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.ebook-card {
  background: var(--paper-light);
  border: 1px solid var(--line);
  border-radius: 14px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  animation: page-fade 0.5s ease both;
}

.ebook-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.ebook-card:nth-child(2) { animation-delay: 0.05s; }
.ebook-card:nth-child(3) { animation-delay: 0.1s; }
.ebook-card:nth-child(4) { animation-delay: 0.15s; }
.ebook-card:nth-child(5) { animation-delay: 0.2s; }
.ebook-card:nth-child(6) { animation-delay: 0.25s; }

.cover {
  height: 170px;
  background: linear-gradient(150deg, #efe3c8, #e3d3b4);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-fallback {
  font-size: 64px;
  font-weight: 700;
  color: rgba(45, 42, 36, 0.45);
}

.ebook-info {
  padding: 14px 16px 16px;
}

.ebook-info h3 {
  margin: 0 0 6px;
  font-size: 18px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.desc {
  margin: 0 0 12px;
  color: var(--ink-soft);
  font-size: 13px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 39px;
}

.meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #9a9182;
  flex-wrap: wrap;
}

.meta .tag {
  background: rgba(176, 58, 46, 0.1);
  color: var(--accent-deep);
  padding: 2px 8px;
  border-radius: 999px;
  font-weight: 600;
}

.pager {
  display: flex;
  justify-content: center;
  padding: 8px 0 4px;
}

@media (max-width: 900px) {
  .ebook-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
