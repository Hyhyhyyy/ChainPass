import request from './request'
import type { ApiResponse, PageResponse, UserInfo } from '@chainpass/shared/types'

export interface UserQuery {
  username?: string
  email?: string
  status?: number
  page?: number
  pageSize?: number
}

export interface CreateUserRequest {
  username: string
  password: string
  email?: string
  phone?: string
  nickname?: string
  roles?: number[]
}

export interface UpdateUserRequest {
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  status?: number
  roles?: number[]
}

/**
 * 用户管理 API
 */
export const userApi = {
  /**
   * 获取用户列表
   */
  getUserList: (params: UserQuery): Promise<ApiResponse<PageResponse<UserInfo>>> =>
    request.get('/user/list', { params }),

  /**
   * 获取用户详情
   */
  getUserById: (id: number): Promise<ApiResponse<UserInfo>> => request.get(`/user/${id}`),

  /**
   * 创建用户
   */
  createUser: (data: CreateUserRequest): Promise<ApiResponse<void>> =>
    request.post('/user', data),

  /**
   * 更新用户
   */
  updateUser: (id: number, data: UpdateUserRequest): Promise<ApiResponse<void>> =>
    request.put(`/user/${id}`, data),

  /**
   * 删除用户
   */
  deleteUser: (id: number): Promise<ApiResponse<void>> => request.delete(`/user/${id}`),

  /**
   * 修改用户状态
   */
  updateUserStatus: (id: number, status: number): Promise<ApiResponse<void>> =>
    request.put(`/user/${id}/status`, { status }),

  /**
   * 重置用户密码
   */
  resetPassword: (id: number): Promise<ApiResponse<string>> =>
    request.post(`/user/${id}/reset-password`),

  /**
   * 获取当前用户信息
   */
  getCurrentUser: (): Promise<ApiResponse<UserInfo>> => request.get('/user/current'),

  /**
   * 更新当前用户信息
   */
  updateCurrentUser: (data: Partial<UserInfo>): Promise<ApiResponse<void>> =>
    request.put('/user/current', data),

  /**
   * 修改密码
   */
  changePassword: (oldPassword: string, newPassword: string): Promise<ApiResponse<void>> =>
    request.put('/user/current/password', { oldPassword, newPassword }),
}