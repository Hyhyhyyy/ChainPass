import { defineStore } from 'pinia'
import { ref } from 'vue'

export type ThemeMode = 'light' | 'dark' | 'auto'

export const useAppStore = defineStore(
  'app',
  () => {
    // State
    const theme = ref<ThemeMode>('light')
    const sidebarCollapsed = ref(false)
    const language = ref<string>('zh-CN')
    const loading = ref(false)

    // Actions
    function setTheme(newTheme: ThemeMode) {
      theme.value = newTheme
      applyTheme(newTheme)
    }

    function applyTheme(mode: ThemeMode) {
      let actualTheme = mode
      if (mode === 'auto') {
        actualTheme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
      }
      document.documentElement.setAttribute('data-theme', actualTheme)
    }

    function toggleSidebar() {
      sidebarCollapsed.value = !sidebarCollapsed.value
    }

    function setLanguage(lang: string) {
      language.value = lang
    }

    function setLoading(value: boolean) {
      loading.value = value
    }

    // 初始化主题
    if (typeof window !== 'undefined') {
      applyTheme(theme.value)
    }

    return {
      theme,
      sidebarCollapsed,
      language,
      loading,
      setTheme,
      toggleSidebar,
      setLanguage,
      setLoading,
    }
  },
  {
    persist: {
      key: 'chainpass-app',
      storage: localStorage,
      pick: ['theme', 'sidebarCollapsed', 'language'],
    },
  }
)