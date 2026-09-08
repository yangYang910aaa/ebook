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
        <template #parentName="{ record }">{{ record.parentName || '一级分类' }}</template>
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确定删除该分类？" ok-text="删除" cancel-text="取消" @confirm="remove(record)">
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </section>

    <a-modal v-model:visible="modalOpen" :width="480" ok-text="确定" cancel-text="取消" @ok="save">
      <template #title>
        <span class="modal-title">{{ form.id ? '编辑分类' : '新增分类' }}</span>
      </template>
      <div class="cat-form">
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
            <div class="field-hint">数字越小越靠前，默认为 0</div>
          </a-form-item>
        </a-form>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
/* 分类管理（后台）：分类的树形展示、模糊搜索、新增/编辑/删除；分类为二级结构（一级 parent=0，二级挂在一级下） */
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
  { title: '父分类', key: 'parentName', slots: { customRender: 'parentName' } },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 90 },
  { title: '操作', key: 'action', width: 140, slots: { customRender: 'action' } }
]

onMounted(load)

// 加载分类列表，并筛选出一级分类作为父分类下拉选项
async function load() {
  loading.value = true
  try {
    allCats.value = await getCategoryList()
    parents.value = allCats.value.filter((c) => c.parent === 0)
  } finally {
    loading.value = false
  }
}

// 树形表格数据：扁平列表 -> Map 索引 -> 按 parent 挂载；支持按名称模糊过滤（命中节点保留其子树）
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
        c.parentName = parent.name // 回填父分类名称用于展示
        parent.children.push(c)
      }
    }
  })
  const kw = queryName.value.trim()
  if (!kw) return roots
  // 递归过滤：节点名称命中或其子节点有命中时保留
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

// 打开新增弹窗：重置表单
function openAdd() {
  Object.assign(form, { id: 0, name: '', parent: 0, sort: 0 })
  modalOpen.value = true
}

// 打开编辑弹窗：回填当前分类数据
function openEdit(record: CategoryRow) {
  Object.assign(form, { id: record.id, name: record.name, parent: record.parent, sort: record.sort })
  modalOpen.value = true
}

// 保存分类：新增（id=0）或更新，保存后刷新列表
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

// 删除分类
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

/* ===== 编辑弹窗美化 ===== */
.modal-title {
  font-family: var(--serif);
  font-size: 17px;
  font-weight: 600;
  color: var(--ink);
  letter-spacing: 0.02em;
}

.cat-form :deep(.ant-form-item-label > label) {
  font-family: var(--serif);
  font-weight: 600;
  color: var(--ink);
  font-size: 14px;
}

.cat-form :deep(.ant-input),
.cat-form :deep(.ant-select-selector),
.cat-form :deep(.ant-input-number) {
  border-radius: 6px;
}

.cat-form :deep(.ant-input):hover,
.cat-form :deep(.ant-select-selector:hover),
.cat-form :deep(.ant-input-number:hover) {
  border-color: var(--accent-soft);
}

.cat-form :deep(.ant-input):focus,
.cat-form :deep(.ant-input-focused),
.cat-form :deep(.ant-select-focused .ant-select-selector),
.cat-form :deep(.ant-input-number-focused) {
  border-color: var(--accent);
  box-shadow: 0 0 0 2px rgba(176, 58, 46, 0.12);
}

.field-hint {
  font-size: 11px;
  color: var(--ink-soft);
  margin-top: 4px;
}
</style>
