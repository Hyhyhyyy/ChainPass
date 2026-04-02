import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 本地存储
 */
export function useLocalStorage<T>(key: string, defaultValue: T) {
  const data = ref<T>(defaultValue)

  const read = () => {
    const stored = localStorage.getItem(key)
    if (stored !== null) {
      try {
        data.value = JSON.parse(stored)
      } catch {
        data.value = stored as unknown as T
      }
    }
  }

  const write = () => {
    if (typeof data.value === 'string') {
      localStorage.setItem(key, data.value)
    } else {
      localStorage.setItem(key, JSON.stringify(data.value))
    }
  }

  const remove = () => {
    localStorage.removeItem(key)
    data.value = defaultValue
  }

  onMounted(read)

  return {
    data,
    read,
    write,
    remove,
  }
}