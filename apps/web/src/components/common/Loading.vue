<script setup lang="ts">
interface Props {
  size?: 'small' | 'default' | 'large'
  text?: string
  fullscreen?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'default',
  text: '加载中...',
  fullscreen: false,
})

const sizeMap = {
  small: 24,
  default: 32,
  large: 48,
}
</script>

<template>
  <div class="loading-container" :class="{ fullscreen }">
    <div class="loading-content">
      <el-icon
        class="loading-icon"
        :size="sizeMap[size]"
      >
        <Loading />
      </el-icon>
      <p v-if="text" class="loading-text">{{ text }}</p>
    </div>
  </div>
</template>

<style scoped>
.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xl);
}

.loading-container.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.9);
  z-index: 9999;
}

[data-theme='dark'] .loading-container.fullscreen {
  background-color: rgba(17, 24, 39, 0.9);
}

.loading-content {
  text-align: center;
}

.loading-icon {
  color: var(--color-primary);
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  margin-top: var(--spacing-md);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}
</style>