import request from './request'

export interface CategoryRow {
  id: number
  parent: number
  name: string
  sort: number
  parentName?: string
}

export function getCategoryList() {
  return request.get<unknown, CategoryRow[]>('/category/getCategoryList')
}

export function getParents() {
  return request.get<unknown, CategoryRow[]>('/category/getParents')
}

export function saveCategory(data: { id?: number; parent?: number; name: string; sort?: number }) {
  return request.post<unknown, void>('/category/save', data)
}

export function removeCategory(id: number) {
  return request.get<unknown, void>('/category/remove', { params: { id } })
}
