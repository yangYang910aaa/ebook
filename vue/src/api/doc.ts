import request from './request'

export interface DocNode {
  id: number
  ebookId: number
  parent: number
  name: string
  sort: number
  viewCount: number
  voteCount: number
  children: DocNode[]
}

export function getAllDocs(ebookId: number) {
  return request.get<unknown, DocNode[]>('/doc/all', { params: { ebookId } })
}

export function findContent(id: number) {
  return request.get<unknown, { id: number; content: string }>(`/doc/find-content/${id}`)
}

export function saveDoc(data: {
  id?: number
  ebookId: number
  parent?: number
  name: string
  sort?: number
  content?: string
}) {
  return request.post<unknown, void>('/doc/save', data)
}

export function deleteDocs(idsStr: string) {
  return request.delete<unknown, void>(`/doc/delete/${idsStr}`)
}

export function voteDoc(id: number) {
  return request.get<unknown, void>(`/doc/vote/${id}`)
}
