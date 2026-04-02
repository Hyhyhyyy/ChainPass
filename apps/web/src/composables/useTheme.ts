import { ref, computed } from 'vue'
import { useAppStore } from '@/stores'

/**
 * 主题切换
 */
export function useTheme() {
  const appStore = useAppStore()

  const isDark = computed(() => appStore.theme === 'dark')

  const toggleTheme = () => {
    appStore.setTheme(isDark.value ? 'light' : 'dark')
  }

  const setTheme = (theme: 'light' | 'dark' | 'auto') => {
    appStore.setTheme(theme)
  }

  return {
    theme: computed(() => appStore.theme),
    isDark,
    toggleTheme,
    setTheme,
  }
}