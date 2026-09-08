/* 文档 API：电子书下的文档目录（树形）、内容、点赞等接口 */
import request from './request'

/** 文档树节点 */
export interface DocNode {
  id: number           // 文档 ID
  ebookId: number      // 所属电子书 ID
  parent: number       // 父文档 ID，0 表示根文档
  name: string         // 文档名称
  sort: number         // 排序值
  viewCount: number    // 阅读次数
  voteCount: number    // 点赞次数
  liked?: boolean      // 当前用户是否已点赞（前台阅读页用）
  children: DocNode[]  // 子文档列表
}

/** 获取某本电子书下的全部文档（已组装为树形结构） */
export function getAllDocs(ebookId: number) {
  return request.get<unknown, DocNode[]>('/doc/all', { params: { ebookId } })
}

/** 获取文档内容；count=true 时阅读数 +1（前台阅读），后台编辑传 false */
export function findContent(id: number, count = true) {
  return request.get<unknown, { id: number; content: string }>(`/doc/find-content/${id}`, { params: { count } })
}

/** 保存文档：id 为空时新增，否则更新（含内容） */
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

/** 批量删除文档（含子文档），idsStr 为逗号分隔的 ID 字符串 */
export function deleteDocs(idsStr: string) {
  return request.delete<unknown, void>(`/doc/delete/${idsStr}`)
}

/** 点赞文档 */
export function voteDoc(id: number) {
  return request.get<unknown, void>(`/doc/vote/${id}`)
}

/** 取消点赞 */
export function unvoteDoc(id: number) {
  return request.get<unknown, void>(`/doc/unvote/${id}`)
}
