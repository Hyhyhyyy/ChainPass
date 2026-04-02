import axios, { type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/modules/user'
import type { ApiResponse } from '@chainpass/shared/types'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 配置 NProgress
NProgress.configure({ showSpinner: false })

// 请求取消控制器映射
const pendingRequests = new Map<string, AbortController>()

/**
 * 生成请求唯一键
 */
function generateRequestKey(config: InternalAxiosRequestConfig): string {
  const { method, url } = config
  return `${method}-${url}`
}

/**
 * 取消所有pending请求
 */
export function cancelAllPendingRequests(): void {
  pendingRequests.forEach((controller, key) => {
    controller.abort()
    pendingRequests.delete(key)
  })
}

// 创建 Axios 实例
const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    NProgress.start()

    // 添加取消令牌
    const requestKey = generateRequestKey(config)
    if (pendingRequests.has(requestKey)) {
      // 取消重复请求
      pendingRequests.get(requestKey)?.abort()
    }
    const controller = new AbortController()
    config.signal = controller.signal
    pendingRequests.set(requestKey, controller)

    const userStore = useUserStore()
    const token = userStore.accessToken

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // Gitee OAuth 接口添加 giteeId
    if (config.url?.includes('/oauth/gitee/config') && userStore.giteeId) {
      config.headers['X-Gitee-Id'] = userStore.giteeId
    }

    return config
  },
  (error) => {
    NProgress.done()
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    NProgress.done()

    // 从pending中移除
    const requestKey = generateRequestKey(response.config as InternalAxiosRequestConfig)
    pendingRequests.delete(requestKey)

    const { data } = response

    // 业务错误处理
    if (data.code !== 200) {
      ElMessage.error(data.msg || '请求失败')
      return Promise.reject(new Error(data.msg || '请求失败'))
    }

    return data
  },
  async (error) => {
    NProgress.done()

    // 忽略取消请求的错误
    if (error.name === 'CanceledError' || error.code === 'ERR_CANCELED') {
      return Promise.reject(error)
    }

    const { response, config } = error
    const userStore = useUserStore()

    if (response) {
      const { status, data } = response

      switch (status) {
        case 401: {
          // Token 过期，尝试刷新
          if (!config._retry && userStore.refreshToken) {
            config._retry = true
            try {
              const refreshed = await userStore.refreshAccessToken()
              if (refreshed) {
                // 重试原请求
                return request(config)
              }
            } catch {
              // 刷新失败，清除登录状态
              userStore.clearAuth()
              window.location.href = '/auth/login'
            }
          } else {
            userStore.clearAuth()
            window.location.href = '/auth/login'
          }
          break
        }
        case 403:
          ElMessage.error('无权访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data?.msg || `请求错误: ${status}`)
      }
    } else {
      ElMessage.error('网络连接异常，请检查网络')
    }

    return Promise.reject(error)
  }
)

export default request