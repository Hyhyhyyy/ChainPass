import request from './request'
import type { ApiResponse } from '@chainpass/shared/types'

// KYC相关类型
export interface KYCSubmitRequest {
  fullName: string
  nationality: string
  idType: string
  idNumber: string
}

export interface KYCResponse {
  id: number
  did: string
  kycLevel: number
  kycLevelName: string
  fullName: string
  nationality: string
  idType: string
  status: string
  statusName: string
  verifiedAt: string
  expiresAt: string
  vcId: string
}

export interface KYCStatusResponse {
  verified: boolean
  kycLevel: number
  status: string
  message: string
}

export interface KYCReviewResponse {
  id: number
  did: string
  fullName: string
  nationality: string
  idType: string
  idNumber: string
  status: string
  submittedAt?: string
}

// KYC API
export const kycApi = {
  /**
   * 提交KYC认证
   */
  submit: (data: KYCSubmitRequest) =>
    request.post<ApiResponse<KYCResponse>>('/kyc/submit', data),

  /**
   * 获取KYC状态
   */
  getStatus: () => request.get<ApiResponse<KYCStatusResponse>>('/kyc/status'),

  /**
   * 获取KYC详情
   */
  getDetail: () => request.get<ApiResponse<KYCResponse>>('/kyc/detail'),

  getReviews: (status = 1) =>
    request.get<ApiResponse<KYCReviewResponse[]>>('/kyc/reviews', { params: { status } }),

  approve: (id: number) => request.post<ApiResponse<void>>(`/kyc/${id}/approve`),

  reject: (id: number, reason: string) =>
    request.post<ApiResponse<void>>(`/kyc/${id}/reject`, null, { params: { reason } }),
}

// KYC等级选项
export const KYC_LEVEL_OPTIONS = [
  { value: 1, label: '人工审核', description: '由授权审核员复核提交信息' },
]

// 证件类型选项
export const ID_TYPE_OPTIONS = [
  { value: 'id_card', label: '身份证' },
  { value: 'passport', label: '护照' },
  { value: 'driver_license', label: '驾驶证' },
  { value: 'residence_permit', label: '居留证' },
]

// 国籍选项
export const NATIONALITY_OPTIONS = [
  { value: 'China', label: '中国' },
  { value: 'USA', label: '美国' },
  { value: 'UK', label: '英国' },
  { value: 'Japan', label: '日本' },
  { value: 'Korea', label: '韩国' },
  { value: 'Singapore', label: '新加坡' },
  { value: 'HongKong', label: '中国香港' },
  { value: 'Taiwan', label: '中国台湾' },
  { value: 'Other', label: '其他' },
]
