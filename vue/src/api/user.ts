/* 用户 API：登录/登出、用户分页管理、新增/编辑/删除/重置密码 */
import request from './request'
import type { PageResult } from './types'

/** 登录成功返回的用户信息 */
export interface LoginResp {
  token: string      // 登录令牌，后续请求头携带
  id: number         // 用户 ID
  loginName: string  // 登录名
  name: string       // 昵称
}

/** 用户登录：密码需前端先做一次 MD5 再提交 */
export function loginApi(data: { loginName: string; password: string }) {
  return request.post<unknown, LoginResp>('/user/userLogin', data)
}

/** 退出登录：携带当前 token 通知后端失效 */
export function logoutApi(token: string) {
  return request.get<unknown, void>(`/user/logout/${token}`)
}

/** 分页查询用户，支持按登录名精确筛选 */
export function getUserListByPage(params: { loginName?: string; pageNum: number; pageSize: number }) {
  return request.get<unknown, PageResult<UserRow>>('/user/getUserListByPage', { params })
}

/** 用户行数据 */
export interface UserRow {
  id: number            // 用户 ID
  loginName: string     // 登录名（创建后不可修改）
  name: string          // 昵称
  password?: string     // 密码密文（列表展示用，新增/编辑时不传）
}

/** 保存用户：id 为空时新增（需传密码），否则仅更新昵称 */
export function saveUser(data: { id?: number; loginName?: string; name: string; password?: string }) {
  return request.post<unknown, void>('/user/save', data)
}

/** 重置指定用户密码：密码需前端先做一次 MD5 */
export function resetPassword(data: { id: number; password: string }) {
  return request.post<unknown, void>('/user/resetPassword', data)
}

/** 根据 ID 删除用户 */
export function removeUser(id: number) {
  return request.get<unknown, void>('/user/remove', { params: { id } })
}
