import request from './request'
import type { ApiResponse } from '@chainpass/shared/types'

// VC相关类型
export interface VerifiableCredential {
  context: string[]
  id: string
  type: string[]
  issuer: Issuer
  issuanceDate: string
  expirationDate: string
  credentialSubject: CredentialSubject
  proof: Proof
}

export interface Issuer {
  id: string
  name: string
}

export interface CredentialSubject {
  id: string
  claims: Record<string, any>
}

export interface Proof {
  type: string
  created: string
  proofPurpose: string
  verificationMethod: string
  proofValue: string
}

export interface VCType {
  id: number
  typeCode: string
  typeName: string
  typeNameEn: string
  validityDays: number
  description: string
  icon: string
}

export interface VCResponse {
  vcId: string
  holderDid: string
  vcType: string
  typeName: string
  vc: VerifiableCredential
  status: string
  issuedAt: string
  expiresAt: string
}

export interface IssueVCRequest {
  holderDid: string
  vcType: string
  claims?: Record<string, any>
}

export interface VerifyResult {
  valid: boolean
  vcId: string
  vcType: string
  holderDid: string
  message: string
}

// VC API
export const vcApi = {
  /**
   * 签发凭证
   */
  issue: (data: IssueVCRequest) =>
    request.post<ApiResponse<VerifiableCredential>>('/vc/issue', data),

  /**
   * 验证凭证
   */
  verify: (vcId: string) =>
    request.post<ApiResponse<VerifyResult>>('/vc/verify', { vcId }),

  /**
   * 获取我的凭证列表
   */
  getMy: () => request.get<ApiResponse<VCResponse[]>>('/vc/my'),

  /**
   * 根据DID获取凭证列表
   */
  getByDid: (did: string) =>
    request.get<ApiResponse<VCResponse[]>>(`/vc/list/${encodeURIComponent(did)}`),

  /**
   * 吊销凭证
   */
  revoke: (vcId: string, reason?: string) =>
    request.post<ApiResponse<void>>(`/vc/revoke/${vcId}`, null, { params: { reason } }),

  /**
   * 获取凭证类型列表
   */
  getTypes: () => request.get<ApiResponse<VCType[]>>('/vc/types'),
}