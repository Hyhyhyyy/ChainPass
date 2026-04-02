<script setup lang="ts">
interface Props {
  description?: string
  image?: string
  imageSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  description: '暂无数据',
  imageSize: 100,
})

const emit = defineEmits<{
  click: []
}>()
</script>

<template>
  <div class="empty-container">
    <div class="empty-image">
      <img v-if="image" :src="image" :style="{ width: `${imageSize}px`, height: `${imageSize}px` }" />
      <el-icon v-else :size="imageSize" class="empty-icon">
        <Folder />
      </el-icon>
    </div>
    <p class="empty-description">{{ description }}</p>
    <el-button v-if="$slots.default" type="primary" @click="emit('click')">
      <slot />
    </el-button>
  </div>
</template>

<style scoped>
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl);
}

.empty-image {
  margin-bottom: var(--spacing-lg);
}

.empty-icon {
  color: var(--color-text-muted);
}

.empty-description {
  color: var(--color-text-secondary);
  font-size: var(--font-size-base);
  margin-bottom: var(--spacing-lg);
}
</style>