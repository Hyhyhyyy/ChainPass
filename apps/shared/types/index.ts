/**
 * 通用 API 响应结构
 */
export interface ApiResponse<T = unknown> {
  code: number
  msg: string
  data: T
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

/**
 * 登录请求
 */
export interface LoginRequest {
  username: string
  password: string
}

/**
 * 注册请求
 */
export interface RegisterRequest {
  username: string
  password: string
  email?: string
  phone?: string
}

/**
 * 登录响应
 */
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  userId?: number
  username?: string
  nickname?: string
  avatar?: string
  giteeId?: string
}

/**
 * Token 响应
 */
export interface TokenResponse {
  accessToken: string
  refreshToken: string
}

/**
 * OAuth 配置
 */
export interface OAuthConfig {
  clientId: string
  redirectUri: string
  responseType: string
  scope?: string
}

/**
 * ZKP 挑战
 */
export interface ZKPChallenge {
  challenge: string
  sessionId: string
  expiresAt: number
}

/**
 * ZKP 证明
 */
export interface ZKPProof {
  sessionId: string
  proof: string
  publicKey: string
}

/**
 * 用户信息
 */
export interface UserInfo {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  status: number
  roles: string[]
  permissions: string[]
  createdAt: string
  updatedAt: string
}

/**
 * 角色信息
 */
export interface RoleInfo {
  id: number
  roleName: string
  roleCode: string
  description: string
  status: number
  permissions: PermissionInfo[]
}

/**
 * 权限信息
 */
export interface PermissionInfo {
  id: number
  permissionName: string
  permissionCode: string
  type: number // 1-菜单 2-按钮 3-API
  parentId: number
  path: string
  component: string
  icon: string
  sortOrder: number
}

/**
 * 操作日志
 */
export interface OperationLog {
  id: number
  userId: number
  username: string
  operationType: string
  operationDesc: string
  requestMethod: string
  requestUrl: string
  ip: string
  location: string
  executionTime: number
  status: number
  createdAt: string
}

/**
 * 登录日志
 */
export interface LoginLog {
  id: number
  userId: number
  username: string
  loginType: 'password' | 'oauth' | 'zkp'
  ip: string
  location: string
  device: string
  browser: string
  os: string
  status: number
  message: string
  loginAt: string
}

/**
 * 二维码会话
 */
export interface QRSession {
  sessionId: string
  type: 'LOGIN' | 'PAYMENT_CONFIRM' | 'DID_REVOKE'
  qrContent: string
  status: 'PENDING' | 'SCANNED' | 'CONFIRMED' | 'EXPIRED'
  createdAt: number
  expiresAt: number
  confirmedBy?: number
  data?: any
}

/**
 * 二维码数据格式（二维码内容）
 */
export interface QRCodeData {
  type: string
  sessionId: string
  timestamp: number
  expiresIn: number
  callback: string
}