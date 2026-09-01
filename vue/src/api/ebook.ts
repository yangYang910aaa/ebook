import request from './request'
import type { PageResult } from './user'

export interface EbookRow {
  id: number
  name: string
  category1Id: number
  category2Id: number
  category1Name?: string
  category2Name?: string
  description?: string
  cover?: string
  docCount: number
  viewCount: number
  voteCount: number
}

export function queryEbooks(params: { name?: string; category2Id?: number; pageNum: number; pageSize: number }) {
  return request.get<unknown, PageResult<EbookRow>>('/ebook/query', { params })
}

export function saveEbook(data: {
  id?: number
  name: string
  category1Id?: number
  category2Id?: number
  description?: string
  cover?: string
}) {
  return request.post<unknown, void>('/ebook/save', data)
}

export function removeEbook(id: number) {
  return request.get<unknown, void>('/ebook/remove', { params: { id } })
}

export function uploadCover(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<unknown, string>('/ebook/uploadImage', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
