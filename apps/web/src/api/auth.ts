import request from './request'
import type { ApiResponse, LoginResponse } from '@chainpass/shared/types'
import type { LoginRequest, RegisterRequest } from '@chainpass/shared/types'

/**
 * 认证相关 API
 */
export const authApi = {
  /**
   * 账密登录
   */
  login: (data: LoginRequest): Promise<ApiResponse<LoginResponse>> =>
    request.post('/auth/login', data),

  /**
   * 用户登出
   */
  logout: (): Promise<ApiResponse<void>> => request.post('/auth/logout'),

  /**
   * 刷新 Token
   */
  refreshToken: (refreshToken: string): Promise<ApiResponse<LoginResponse>> =>
    request.post('/auth/refresh', { refreshToken }),

  /**
   * 用户注册
   */
  register: (data: RegisterRequest): Promise<ApiResponse<void>> =>
    request.post('/auth/register', data),

}
