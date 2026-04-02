import request from './request'
import type { ApiResponse } from '@chainpass/shared/types'
import type { LoginResponse } from '@chainpass/shared/types'

export interface OAuthConfig {
  clientId: string
  redirectUri: string
  responseType: string
  scope?: string
}

/**
 * OAuth 相关 API
 */
export const oauthApi = {
  /**
   * 获取 Gitee OAuth 配置
   */
  getGiteeConfig: (giteeId?: string): Promise<ApiResponse<OAuthConfig>> => {
    const headers: Record<string, string> = {}
    if (giteeId) {
      headers['X-Gitee-Id'] = giteeId
    }
    return request.post('/oauth/gitee/config', {}, { headers })
  },

  /**
   * 处理 Gitee OAuth 回调
   */
  handleGiteeCallback: (code: string): Promise<ApiResponse<LoginResponse>> =>
    request.post('/oauth/gitee/callback', { code }),
}