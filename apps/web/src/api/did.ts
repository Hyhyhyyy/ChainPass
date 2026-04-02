import request from './request'
import type { ApiResponse } from '@chainpass/shared/types'

// DID相关类型
export interface DIDDocument {
  context: string[]
  id: string
  verificationMethod: VerificationMethod[]
  authentication: string[]
  created: string
}

export interface VerificationMethod {
  id: string
  type: string
  controller: string
  publicKeyBase64: string
}

export interface DIDResponse {
  did: string
  document: DIDDocument
  publicKey: string
  status: string
  createdAt: string
  expiresAt: string
}

export interface VerifyDIDRequest {
  did: string
  challenge: string
  signature: string
}

// DID API
export const didApi = {
  /**
   * 创建DID
   */
  create: () => request.post<ApiResponse<DIDDocument>>('/did/create'),

  /**
   * 获取我的DID
   */
  getMy: () => request.get<ApiResponse<DIDResponse>>('/did/my'),

  /**
   * 根据DID获取文档
   */
  getByDid: (did: string) => request.get<ApiResponse<DIDDocument>>(`/did/${encodeURIComponent(did)}`),

  /**
   * 验证DID签名
   */
  verify: (data: VerifyDIDRequest) => request.post<ApiResponse<boolean>>('/did/verify', data),

  /**
   * 吊销DID
   */
  revoke: (did: string, reason?: string) =>
    request.post<ApiResponse<void>>('/did/revoke', null, { params: { did, reason } }),

  /**
   * 检查DID有效性
   */
  check: (did: string) => request.get<ApiResponse<boolean>>(`/did/check/${encodeURIComponent(did)}`),
}