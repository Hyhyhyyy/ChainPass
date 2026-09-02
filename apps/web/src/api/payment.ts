import request from './request'
import type { ApiResponse } from '@chainpass/shared/types'

// 支付相关类型
export interface Wallet {
  id: number
  did: string
  address: string
  balanceCny: number
  balanceUsd: number
  balanceEth: number
  totalBalanceCny: number
  status: string
}

export interface PaymentOrder {
  orderNo: string
  payerDid: string
  payeeDid: string
  amount: number
  currency: string
  originalAmount: number
  originalCurrency: string
  exchangeRate: number
  feeAmount: number
  status: string
  createdAt: string
  paidAt: string
  sourceCountry: string
  targetCountry: string
  beneficiaryName: string
  paymentPurpose: string
  riskScore: number
  riskLevel: string
  complianceDecision: string
  complianceReasons: string
}

export interface Transaction {
  id: number
  orderNo: string
  txHash: string
  gateway: string
  amount: number
  currency: string
  status: number
  createdAt: string
}

export interface TransactionHistory {
  orderNo: string
  type: 'IN' | 'OUT'
  counterpartyDid: string
  amount: number
  currency: string
  status: string
  description: string
  createdAt: string
}

export interface CreatePaymentRequest {
  payeeDid: string
  amount: number
  currency: string
  targetCurrency?: string
  paymentMethod?: string
  description?: string
  sourceCountry: string
  targetCountry: string
  beneficiaryName: string
  paymentPurpose: string
}

export interface ComplianceReviewOrder extends PaymentOrder {
  id: number
  description?: string
}

// 支付 API
export const paymentApi = {
  /**
   * 获取钱包信息
   */
  getWallet: () => request.get<ApiResponse<Wallet>>('/payment/wallet'),

  /**
   * 创建支付订单
   */
  createOrder: (data: CreatePaymentRequest) =>
    request.post<ApiResponse<PaymentOrder>>('/payment/create', data),

  /**
   * 执行支付
   */
  execute: (orderNo: string) =>
    request.post<ApiResponse<Transaction>>(`/payment/execute/${orderNo}`),

  getOrder: (orderNo: string) =>
    request.get<ApiResponse<PaymentOrder>>(`/payment/orders/${orderNo}`),

  /**
   * 获取交易历史
   */
  getHistory: () => request.get<ApiResponse<TransactionHistory[]>>('/payment/history'),

  /**
   * 获取汇率
   */
  getRate: (from: string, to: string) =>
    request.get<ApiResponse<number>>(`/payment/rate/${from}/${to}`),

  getComplianceReviews: () =>
    request.get<ApiResponse<ComplianceReviewOrder[]>>('/payment/compliance/reviews'),

  approveComplianceReview: (orderNo: string, note: string) =>
    request.post<ApiResponse<void>>(`/payment/compliance/reviews/${orderNo}/approve`, null, { params: { note } }),

  rejectComplianceReview: (orderNo: string, note: string) =>
    request.post<ApiResponse<void>>(`/payment/compliance/reviews/${orderNo}/reject`, null, { params: { note } }),
}
