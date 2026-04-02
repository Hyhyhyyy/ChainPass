/**
 * 预览模式专用假数据
 * 仅在 PREVIEW_MODE = true 时使用
 */

// 用户信息
export const mockUser = {
  id: 1,
  username: 'demo_user',
  nickname: '演示用户',
  email: 'demo@chainpass.io',
  phone: '138****8888',
  avatar: '',
  status: 1,
  roles: ['user', 'admin'],
  permissions: ['*:*:*'],
  createdAt: '2024-01-15 10:30:00',
  updatedAt: '2024-03-20 15:45:00'
}

// DID 信息
export const mockDID = {
  did: 'did:chainpass:7f8e9d6c5b4a3210fedcba9876543210abcdef',
  publicKey: '7f8e9d6c5b4a3210fedcba9876543210abcdef1234567890abcdef1234567890abcdef',
  status: 'ACTIVE',
  document: {
    '@context': ['https://www.w3.org/ns/did/v1'],
    id: 'did:chainpass:7f8e9d6c5b4a3210fedcba9876543210abcdef',
    verificationMethod: [
      {
        id: 'did:chainpass:7f8e9d6c5b4a3210fedcba9876543210abcdef#keys-1',
        type: 'Ed25519VerificationKey2020',
        controller: 'did:chainpass:7f8e9d6c5b4a3210fedcba9876543210abcdef',
        publicKeyMultibase: 'z7f8e9d6c5b4a3210fedcba9876543210abcdef'
      }
    ],
    authentication: ['did:chainpass:7f8e9d6c5b4a3210fedcba9876543210abcdef#keys-1'],
    created: '2024-01-15T10:30:00Z',
    updated: '2024-01-15T10:30:00Z'
  },
  createdAt: '2024-01-15 10:30:00',
  expiresAt: '2029-01-15 10:30:00'
}

// 可验证凭证列表
export const mockVCList = [
  {
    id: 1,
    vcId: 'vc:chainpass:kyc:basic:20240115001',
    type: 'KYCCredential',
    issuer: 'did:chainpass:issuer:authority',
    issuanceDate: '2024-01-20 14:30:00',
    expirationDate: '2025-01-20 14:30:00',
    status: 'VALID',
    credentialSubject: {
      kycLevel: 1,
      kycLevelName: '基础认证',
      verifiedAt: '2024-01-20 14:30:00'
    }
  },
  {
    id: 2,
    vcId: 'vc:chainpass:payment:20240210001',
    type: 'PaymentCredential',
    issuer: 'did:chainpass:issuer:authority',
    issuanceDate: '2024-02-10 09:15:00',
    expirationDate: '2025-02-10 09:15:00',
    status: 'VALID',
    credentialSubject: {
      dailyLimit: 20000,
      singleLimit: 5000,
      currency: 'CNY'
    }
  }
]

// 钱包信息
export const mockWallet = {
  address: '0x7f8e9d6c5b4a3210fedcba9876543210abcdef12',
  balanceCny: 125680.50,
  balanceUsd: 18250.00,
  balanceEth: 2.58340000,
  totalBalanceCny: 142580.75
}

// 交易记录
export const mockTransactions = [
  {
    id: 1,
    orderNo: 'TX202403150001',
    type: 'OUT',
    amount: 5000.00,
    currency: 'CNY',
    targetCurrency: 'USD',
    exchangeRate: 0.1425,
    feeAmount: 5.00,
    counterpartyDid: 'did:chainpass:1234567890abcdef',
    description: '跨境支付-测试订单',
    status: 'SUCCESS',
    createdAt: '2024-03-15 10:25:30'
  },
  {
    id: 2,
    orderNo: 'TX202403120002',
    type: 'IN',
    amount: 1200.00,
    currency: 'USD',
    targetCurrency: '',
    exchangeRate: null,
    feeAmount: 0,
    counterpartyDid: 'did:chainpass:abcdef1234567890',
    description: '接收汇款',
    status: 'SUCCESS',
    createdAt: '2024-03-12 16:45:00'
  },
  {
    id: 3,
    orderNo: 'TX202403080003',
    type: 'OUT',
    amount: 10000.00,
    currency: 'CNY',
    targetCurrency: '',
    exchangeRate: null,
    feeAmount: 10.00,
    counterpartyDid: 'did:chainpass:fedcba0987654321',
    description: '供应商付款',
    status: 'SUCCESS',
    createdAt: '2024-03-08 09:30:15'
  },
  {
    id: 4,
    orderNo: 'TX202403010004',
    type: 'IN',
    amount: 50000.00,
    currency: 'CNY',
    targetCurrency: '',
    exchangeRate: null,
    feeAmount: 0,
    counterpartyDid: 'did:chainpass:issuer:authority',
    description: '账户充值',
    status: 'SUCCESS',
    createdAt: '2024-03-01 14:20:00'
  }
]

// KYC 状态
export const mockKYCStatus = {
  verified: true,
  status: 'APPROVED',
  kycLevel: 1,
  kycLevelName: '基础认证'
}

// KYC 详情
export const mockKYCDetail = {
  fullName: '张**',
  nationality: '中国',
  idType: 'id_card',
  idNumber: '210***********1234',
  verifiedAt: '2024-01-20 14:30:00'
}

// 登录设备
export const mockDevices = [
  {
    id: 1,
    deviceName: 'Windows PC - Chrome',
    deviceType: 'Desktop',
    browser: 'Chrome 122.0',
    os: 'Windows 11',
    ip: '192.168.1.***',
    location: '辽宁大连',
    lastLoginAt: '2024-03-20 15:30:00',
    isCurrent: true
  },
  {
    id: 2,
    deviceName: 'iPhone 15 Pro - Safari',
    deviceType: 'Mobile',
    browser: 'Safari 17.0',
    os: 'iOS 17.3',
    ip: '192.168.1.***',
    location: '辽宁大连',
    lastLoginAt: '2024-03-18 20:15:00',
    isCurrent: false
  }
]

// 操作日志
export const mockOperationLogs = [
  {
    id: 1,
    operationType: '登录',
    operationDesc: '账号密码登录',
    requestMethod: 'POST',
    requestUrl: '/api/auth/login',
    ip: '192.168.1.100',
    location: '辽宁大连',
    executionTime: 156,
    status: 1,
    createdAt: '2024-03-20 15:30:00'
  },
  {
    id: 2,
    operationType: '支付',
    operationDesc: '创建跨境支付订单',
    requestMethod: 'POST',
    requestUrl: '/api/payment/create',
    ip: '192.168.1.100',
    location: '辽宁大连',
    executionTime: 245,
    status: 1,
    createdAt: '2024-03-15 10:25:00'
  },
  {
    id: 3,
    operationType: 'KYC',
    operationDesc: '提交KYC认证申请',
    requestMethod: 'POST',
    requestUrl: '/api/kyc/submit',
    ip: '192.168.1.100',
    location: '辽宁大连',
    executionTime: 1890,
    status: 1,
    createdAt: '2024-01-20 14:25:00'
  }
]

// 用户列表（管理员视图）
export const mockUserList = [
  {
    id: 1,
    username: 'admin',
    nickname: '系统管理员',
    email: 'admin@chainpass.io',
    status: 1,
    roles: ['admin'],
    createdAt: '2024-01-01 00:00:00'
  },
  {
    id: 2,
    username: 'demo_user',
    nickname: '演示用户',
    email: 'demo@chainpass.io',
    status: 1,
    roles: ['user'],
    createdAt: '2024-01-15 10:30:00'
  },
  {
    id: 3,
    username: 'test_user',
    nickname: '测试用户',
    email: 'test@chainpass.io',
    status: 0,
    roles: ['user'],
    createdAt: '2024-02-20 16:45:00'
  }
]

// 角色列表
export const mockRoles = [
  {
    id: 1,
    roleName: '超级管理员',
    roleCode: 'admin',
    description: '拥有系统全部权限',
    status: 1,
    userCount: 1
  },
  {
    id: 2,
    roleName: '普通用户',
    roleCode: 'user',
    description: '基础用户权限',
    status: 1,
    userCount: 1256
  },
  {
    id: 3,
    roleName: '审计员',
    roleCode: 'auditor',
    description: '审计和日志查看权限',
    status: 1,
    userCount: 5
  }
]

// 汇率数据
export const mockExchangeRate = {
  CNY_USD: 0.1425,
  CNY_ETH: 0.0000583,
  USD_CNY: 7.0175,
  USD_ETH: 0.000409,
  ETH_CNY: 24415.50,
  ETH_USD: 3478.25
}