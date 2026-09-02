<template>
  <div class="admin-page">
    <section class="panel">
      <div class="toolbar">
        <h3 class="display">分类管理</h3>
        <div class="actions">
          <a-input v-model:value="queryName" placeholder="按名称模糊查询" allow-clear style="width: 200px" />
          <a-button type="primary" @click="openAdd">新增分类</a-button>
        </div>
      </div>
      <a-table
        :data-source="treeList"
        :columns="columns"
        :pagination="false"
        :loading="loading"
        :default-expand-all-rows="true"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'parentName'">{{ record.parentName || '一级分类' }}</template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
              <a-popconfirm title="确定删除该分类？" ok-text="删除" cancel-text="取消" @confirm="remove(record)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </section>

    <a-modal v-model:visible="modalOpen" :title="form.id ? '编辑分类' : '新增分类'" @ok="save">
      <a-form layout="vertical">
        <a-form-item label="分类名称">
          <a-input v-model:value="form.name" placeholder="请输入分类名称" />
        </a-form-item>
        <a-form-item label="父分类">
          <a-select v-model:value="form.parent">
            <a-select-option :value="0">一级分类</a-select-option>
            <a-select-option v-for="p in parents" :key="p.id" :value="p.id">{{ p.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="form.sort" :min="0" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getCategoryList, removeCategory, saveCategory, type CategoryRow } from '../../api/category'

interface CatNode extends CategoryRow {
  children: CatNode[]
}

const allCats = ref<CategoryRow[]>([])
const loading = ref(false)
const queryName = ref('')
const parents = ref<CategoryRow[]>([])
const modalOpen = ref(false)
const form = reactive({ id: 0, name: '', parent: 0, sort: 0 })

const columns = [
  { title: '编号', dataIndex: 'id', key: 'id', width: 80 },
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '父分类', key: 'parentName' },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 90 },
  { title: '操作', key: 'action', width: 140 }
]

onMounted(load)

async function load() {
  loading.value = true
  try {
    allCats.value = await getCategoryList()
    parents.value = allCats.value.filter((c) => c.parent === 0)
  } finally {
    loading.value = false
  }
}

// 树形表格：一级分类为根、二级分类挂其下；按名称模糊过滤（命中保留其子树）
const treeList = computed<CatNode[]>(() => {
  const map = new Map<number, CatNode>()
  allCats.value.forEach((c) => map.set(c.id, { ...c, children: [] }))
  const roots: CatNode[] = []
  map.forEach((c) => {
    if (c.parent === 0) {
      roots.push(c)
    } else {
      const parent = map.get(c.parent)
      if (parent) {
        c.parentName = parent.name
        parent.children.push(c)
      }
    }
  })
  const kw = queryName.value.trim()
  if (!kw) return roots
  const filter = (nodes: CatNode[]): CatNode[] => {
    const out: CatNode[] = []
    for (const n of nodes) {
      const kids = filter(n.children || [])
      if (n.name.includes(kw) || kids.length) {
        out.push({ ...n, children: kids })
      }
    }
    return out
  }
  return filter(roots)
})

function openAdd() {
  Object.assign(form, { id: 0, name: '', parent: 0, sort: 0 })
  modalOpen.value = true
}

function openEdit(record: CategoryRow) {
  Object.assign(form, { id: record.id, name: record.name, parent: record.parent, sort: record.sort })
  modalOpen.value = true
}

async function save() {
  if (!form.name.trim()) {
    message.warning('请输入分类名称')
    return
  }
  await saveCategory({ id: form.id || undefined, parent: form.parent, name: form.name, sort: form.sort })
  modalOpen.value = false
  message.success('保存成功')
  await load()
}

async function remove(record: CategoryRow) {
  await removeCategory(record.id)
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
</style>
