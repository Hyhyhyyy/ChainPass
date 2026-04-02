import request from './request'
import type { ApiResponse } from '@chainpass/shared/types'

// KYC相关类型
export interface KYCSubmitRequest {
  fullName: string
  nationality: string
  idType: string
  idNumber: string
  idDocumentFront?: string
  idDocumentBack?: string
  facePhoto?: string
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
}

// KYC等级选项
export const KYC_LEVEL_OPTIONS = [
  { value: 1, label: '基础认证', description: '姓名、国籍、证件信息' },
  { value: 2, label: '中级认证', description: '增加人脸识别、地址验证' },
  { value: 3, label: '高级认证', description: '完整企业级认证' },
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