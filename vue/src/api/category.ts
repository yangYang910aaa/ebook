/* 分类管理 API：电子书分类（一级/二级树形）的增删改查 */
import request from './request'

/** 分类行数据 */
export interface CategoryRow {
  id: number          // 分类 ID
  parent: number      // 父分类 ID，0 表示一级分类
  name: string        // 分类名称
  sort: number        // 排序值，数字越小越靠前
  parentName?: string // 父分类名称（列表展示用）
}

/** 获取全部分类列表（扁平结构，前端自行组装树） */
export function getCategoryList() {
  return request.get<unknown, CategoryRow[]>('/category/getCategoryList')
}

/** 获取一级分类列表（用作父分类下拉选项） */
export function getParents() {
  return request.get<unknown, CategoryRow[]>('/category/getParents')
}

/** 保存分类：id 为空时新增，否则更新 */
export function saveCategory(data: { id?: number; parent?: number; name: string; sort?: number }) {
  return request.post<unknown, void>('/category/save', data)
}

/** 根据 ID 删除分类 */
export function removeCategory(id: number) {
  return request.get<unknown, void>('/category/remove', { params: { id } })
}
