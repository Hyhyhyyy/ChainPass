import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, oauthApi, zkpApi } from '@/api'
import type { LoginRequest, LoginResponse, RegisterRequest } from '@chainpass/shared/types'

// 预览模式开关
const PREVIEW_MODE = true

/**
 * 解析JWT获取过期时间
 */
function parseJwtExpiration(token: string): number {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return 0

    const payload = JSON.parse(atob(parts[1]))
    if (payload.exp) {
      // exp是秒级时间戳，转换为毫秒
      return payload.exp * 1000
    }
    return 0
  } catch {
    return 0
  }
}

export const useUserStore = defineStore(
  'user',
  () => {
    // State
    const accessToken = ref<string>('')
    const refreshToken = ref<string>('')
    const tokenExpiresAt = ref<number>(0)
    const refreshTokenExpiresAt = ref<number>(0)

    const userId = ref<number | null>(null)
    const username = ref<string>('')
    const nickname = ref<string>('')
    const email = ref<string>('')
    const avatar = ref<string>('')
    const giteeId = ref<string>('')
    const roles = ref<string[]>([])
    const permissions = ref<string[]>([])
    const createdAt = ref<string>('')

    // Token验证函数（需要在computed之前定义）
    function isTokenValid(): boolean {
      if (!accessToken.value) return false
      return tokenExpiresAt.value > Date.now()
    }

    function isRefreshTokenValid(): boolean {
      if (!refreshToken.value) return false
      return refreshTokenExpiresAt.value > Date.now()
    }

    // Getters
    const isLoggedIn = computed(() => !!accessToken.value && isTokenValid())

    const userInitials = computed(() => {
      if (!nickname.value && !username.value) return 'U'
      const name = nickname.value || username.value
      const parts = name.split(/[_\-\s]/)
      return parts.map((p) => p.charAt(0).toUpperCase()).slice(0, 2).join('')
    })

    // Actions
    function setTokens(tokens: LoginResponse) {
      accessToken.value = tokens.accessToken
      refreshToken.value = tokens.refreshToken

      // 从JWT解析实际过期时间
      const accessExp = parseJwtExpiration(tokens.accessToken)
      const refreshExp = parseJwtExpiration(tokens.refreshToken)

      tokenExpiresAt.value = accessExp || Date.now() + 15 * 60 * 1000
      refreshTokenExpiresAt.value = refreshExp || Date.now() + 7 * 24 * 60 * 60 * 1000
    }

    // 用户信息类型
    interface UserInfoData {
      userId?: number | null
      username?: string
      nickname?: string
      email?: string
      avatar?: string
      giteeId?: string
      roles?: string[]
      permissions?: string[]
    }

    function setUserInfo(userInfo: UserInfoData) {
      if (userInfo.userId) userId.value = userInfo.userId
      if (userInfo.username) username.value = userInfo.username
      if (userInfo.nickname) nickname.value = userInfo.nickname
      if (userInfo.email) email.value = userInfo.email
      if (userInfo.avatar) avatar.value = userInfo.avatar
      if (userInfo.giteeId) giteeId.value = userInfo.giteeId
      if (userInfo.roles) roles.value = userInfo.roles
      if (userInfo.permissions) permissions.value = userInfo.permissions
    }

    async function login(credentials: LoginRequest): Promise<boolean> {
      try {
        const response = await authApi.login(credentials)
        if (response.code === 200 && response.data) {
          setTokens(response.data)
          username.value = credentials.username
          return true
        }
        return false
      } catch (error) {
        console.error('登录失败:', error)
        return false
      }
    }

    async function giteeLogin(autoLogin: boolean = false): Promise<void> {
      const response = await oauthApi.getGiteeConfig(autoLogin ? giteeId.value : undefined)
      if (response.code === 200 && response.data) {
        const { clientId, redirectUri, responseType, scope } = response.data
        const params = new URLSearchParams({
          client_id: clientId,
          redirect_uri: redirectUri,
          response_type: responseType,
          scope: scope || 'user_info',
        })
        window.location.href = `https://gitee.com/oauth/authorize?${params.toString()}`
      }
    }

    async function handleOAuthCallback(code: string): Promise<boolean> {
      try {
        const response = await oauthApi.handleGiteeCallback(code)
        if (response.code === 200 && response.data) {
          setTokens(response.data)
          return true
        }
        return false
      } catch (error) {
        console.error('OAuth 登录失败:', error)
        return false
      }
    }

    async function refreshAccessToken(): Promise<boolean> {
      if (!isRefreshTokenValid()) {
        return false
      }

      try {
        const response = await authApi.refreshToken(refreshToken.value)
        if (response.code === 200 && response.data) {
          setTokens(response.data)
          return true
        }
        return false
      } catch (error) {
        console.error('刷新 Token 失败:', error)
        return false
      }
    }

    async function logout(): Promise<void> {
      try {
        await authApi.logout()
      } catch (error) {
        console.error('登出失败:', error)
      } finally {
        clearAuth()
      }
    }

    function clearAuth() {
      accessToken.value = ''
      refreshToken.value = ''
      tokenExpiresAt.value = 0
      refreshTokenExpiresAt.value = 0
      userId.value = null
      username.value = ''
      nickname.value = ''
      email.value = ''
      avatar.value = ''
      giteeId.value = ''
      roles.value = []
      permissions.value = []
    }

    function hasPermission(permission: string): boolean {
      return permissions.value.includes(permission) || permissions.value.includes('*:*:*')
    }

    function hasRole(role: string): boolean {
      return roles.value.includes(role) || roles.value.includes('admin')
    }

    // 预览模式：初始化 mock 用户数据
    function initPreviewMode() {
      if (PREVIEW_MODE && !username.value) {
        userId.value = 1
        username.value = 'demo_user'
        nickname.value = '演示用户'
        email.value = 'demo@chainpass.io'
        roles.value = ['user', 'admin']
        permissions.value = ['*:*:*']
        createdAt.value = '2024-01-01 00:00:00'
        // 设置一个永不过期的 token
        accessToken.value = 'preview_mode_token'
        tokenExpiresAt.value = Date.now() + 365 * 24 * 60 * 60 * 1000 // 一年后过期
      }
    }

    // 自动初始化预览模式
    initPreviewMode()

    return {
      // State
      accessToken,
      refreshToken,
      tokenExpiresAt,
      refreshTokenExpiresAt,
      userId,
      username,
      nickname,
      email,
      avatar,
      giteeId,
      roles,
      permissions,
      createdAt,
      // Getters
      isLoggedIn,
      userInitials,
      // Actions
      setTokens,
      setUserInfo,
      isTokenValid,
      isRefreshTokenValid,
      login,
      giteeLogin,
      handleOAuthCallback,
      refreshAccessToken,
      logout,
      clearAuth,
      hasPermission,
      hasRole,
    }
  },
  {
    persist: {
      key: 'chainpass-user',
      storage: localStorage,
      pick: [
        'accessToken',
        'refreshToken',
        'tokenExpiresAt',
        'refreshTokenExpiresAt',
        'userId',
        'username',
        'nickname',
        'email',
        'avatar',
        'giteeId',
        'roles',
        'permissions',
        'createdAt',
      ],
    },
  }
)