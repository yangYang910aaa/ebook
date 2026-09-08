/* 电子书 API：电子书的分页查询、增删改、封面上传 */
import request from './request'
import type { PageResult } from './types'

/** 电子书行数据 */
export interface EbookRow {
  id: number              // 电子书 ID
  name: string            // 名称
  category1Id: number     // 一级分类 ID
  category2Id: number     // 二级分类 ID
  category1Name?: string  // 一级分类名称（列表展示用）
  category2Name?: string  // 二级分类名称（列表展示用）
  description?: string    // 简介
  cover?: string          // 封面图片 URL
  docCount: number        // 文档数量
  viewCount: number       // 阅读次数
  voteCount: number       // 点赞次数
}

/** 分页查询电子书，支持按名称模糊搜索和二级分类筛选 */
export function queryEbooks(params: { name?: string; category2Id?: number; pageNum: number; pageSize: number }) {
  return request.get<unknown, PageResult<EbookRow>>('/ebook/query', { params })
}

/** 保存电子书：id 为空时新增，否则更新 */
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

/** 根据 ID 删除电子书 */
export function removeEbook(id: number) {
  return request.get<unknown, void>('/ebook/remove', { params: { id } })
}

/** 上传电子书封面，返回图片 URL */
export function uploadCover(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<unknown, string>('/ebook/uploadImage', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
