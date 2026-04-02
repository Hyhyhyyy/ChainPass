import request from './request'
import type { ApiResponse } from '@chainpass/shared/types'

export interface ZKPChallenge {
  sessionId: string
  challenge: string
  expiresAt: number
}

export interface ZKPVerifyRequest {
  sessionId: string
  signature: string
  publicKey: string
}

export interface ZKPInitRequest {
  sessionId: string
  publicKey: string
  jwt?: string
}

/**
 * ZKP 零知识证明认证 API
 */
export const zkpApi = {
  /**
   * 初始化 ZKP 认证
   */
  initAuth: (): Promise<ApiResponse<ZKPChallenge>> => request.post('/zkp/init'),

  /**
   * 提交公钥
   */
  submitPublicKey: (data: ZKPInitRequest): Promise<ApiResponse<void>> =>
    request.post('/zkp/public-key', data),

  /**
   * 验证 ZKP 认证
   */
  verifyAuth: (data: ZKPVerifyRequest): Promise<ApiResponse<any>> =>
    request.post('/zkp/verify', data),

  /**
   * 检查认证状态
   */
  checkStatus: (sessionId: string): Promise<ApiResponse<boolean>> =>
    request.get(`/zkp/status/${sessionId}`),
}