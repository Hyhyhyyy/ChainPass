import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 监听窗口大小变化
 */
export function useWindowSize() {
  const width = ref(window.innerWidth)
  const height = ref(window.innerHeight)

  const update = () => {
    width.value = window.innerWidth
    height.value = window.innerHeight
  }

  onMounted(() => {
    window.addEventListener('resize', update)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', update)
  })

  return { width, height }
}

/**
 * 判断是否为移动端
 */
export function useIsMobile() {
  const { width } = useWindowSize()
  return width.value < 768
}