import request from './request'
import type { ApiResponse } from '@chainpass/shared/types'

// 二维码会话类型
export interface QRSession {
  sessionId: string
  type: string
  qrContent: string
  status: 'PENDING' | 'SCANNED' | 'CONFIRMED' | 'EXPIRED'
  createdAt: number
  expiresAt: number
  confirmedBy?: number
  data?: any
}

// 二维码数据格式
export interface QRCodeData {
  type: string
  sessionId: string
  timestamp: number
  expiresIn: number
  callback: string
}

// 二维码登录 API
export const qrApi = {
  /**
   * 创建二维码会话
   */
  create: (type: string = 'LOGIN'): Promise<ApiResponse<QRSession>> =>
    request.post('/qr/create', null, { params: { type } }),

  /**
   * 查询二维码状态
   */
  getStatus: (sessionId: string): Promise<ApiResponse<QRSession>> =>
    request.get(`/qr/status/${sessionId}`),

  /**
   * 扫描二维码 (移动端调用)
   */
  scan: (sessionId: string): Promise<ApiResponse<void>> =>
    request.post(`/qr/scan/${sessionId}`),

  /**
   * 确认操作 (移动端调用)
   */
  confirm: (sessionId: string, operationType: string, extraData?: any): Promise<ApiResponse<any>> =>
    request.post(`/qr/confirm/${sessionId}`, { operationType, extraData }),

  /**
   * 取消操作
   */
  cancel: (sessionId: string): Promise<ApiResponse<void>> =>
    request.post(`/qr/cancel/${sessionId}`),
}