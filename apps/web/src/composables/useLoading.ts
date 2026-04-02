import { ref } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 加载状态管理
 */
export function useLoading(initLoading = false) {
  const loading = ref(initLoading)

  const startLoading = (message = '加载中...') => {
    loading.value = true
    return ElMessage({
      message,
      type: 'info',
      duration: 0,
    })
  }

  const stopLoading = (messageInstance?: ReturnType<typeof ElMessage>) => {
    loading.value = false
    if (messageInstance) {
      messageInstance.close()
    }
  }

  const withLoading = async <T>(
    fn: () => Promise<T>,
    options: {
      loadingMessage?: string
      successMessage?: string
      errorMessage?: string
    } = {}
  ): Promise<T | null> => {
    const { loadingMessage, successMessage, errorMessage } = options
    const msg = loadingMessage ? startLoading(loadingMessage) : undefined
    loading.value = true

    try {
      const result = await fn()
      if (successMessage) {
        ElMessage.success(successMessage)
      }
      return result
    } catch (error) {
      if (errorMessage) {
        ElMessage.error(errorMessage)
      }
      return null
    } finally {
      stopLoading(msg)
      loading.value = false
    }
  }

  return {
    loading,
    startLoading,
    stopLoading,
    withLoading,
  }
}