<script setup lang="ts">
interface Props {
  icon?: string
  title: string
  subtitle?: string
  extra?: string
  bordered?: boolean
  hoverable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  bordered: true,
  hoverable: false,
})
</script>

<template>
  <div
    class="card"
    :class="{
      bordered,
      hoverable,
    }"
  >
    <div v-if="icon || $slots.icon" class="card-icon">
      <slot name="icon">
        <el-icon :size="24">
          <component :is="icon" />
        </el-icon>
      </slot>
    </div>
    <div class="card-content">
      <h3 class="card-title">{{ title }}</h3>
      <p v-if="subtitle" class="card-subtitle">{{ subtitle }}</p>
    </div>
    <div v-if="extra || $slots.extra" class="card-extra">
      <slot name="extra">{{ extra }}</slot>
    </div>
  </div>
</template>

<style scoped>
.card {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background-color: var(--color-bg-primary);
  border-radius: var(--radius-lg);
}

.card.bordered {
  border: 1px solid var(--color-border);
}

.card.hoverable {
  cursor: pointer;
  transition: all var(--transition-fast);
}

.card.hoverable:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.card-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--color-primary);
  color: white;
  border-radius: var(--radius-md);
  flex-shrink: 0;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.card-subtitle {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin: var(--spacing-xs) 0 0;
}

.card-extra {
  flex-shrink: 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}
</style>