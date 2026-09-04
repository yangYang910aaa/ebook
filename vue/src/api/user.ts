import request from './request'
import type { PageResult } from './types'

export interface LoginResp {
  token: string
  id: number
  loginName: string
  name: string
}

export function loginApi(data: { loginName: string; password: string }) {
  return request.post<unknown, LoginResp>('/user/userLogin', data)
}

export function logoutApi(token: string) {
  return request.get<unknown, void>(`/user/logout/${token}`)
}

export function getUserListByPage(params: { loginName?: string; pageNum: number; pageSize: number }) {
  return request.get<unknown, PageResult<UserRow>>('/user/getUserListByPage', { params })
}

export interface UserRow {
  id: number
  loginName: string
  name: string
  password?: string
}

export function saveUser(data: { id?: number; loginName?: string; name: string; password?: string }) {
  return request.post<unknown, void>('/user/save', data)
}

export function resetPassword(data: { id: number; password: string }) {
  return request.post<unknown, void>('/user/resetPassword', data)
}

export function removeUser(id: number) {
  return request.get<unknown, void>('/user/remove', { params: { id } })
}
