<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  status?: 'success' | 'warning' | 'danger' | 'info' | 'default'
  label: string
  size?: 'small' | 'default' | 'large'
}

const props = withDefaults(defineProps<Props>(), {
  status: 'default',
  size: 'default',
})

const statusColors: Record<string, string> = {
  success: 'var(--color-success)',
  warning: 'var(--color-warning)',
  danger: 'var(--color-danger)',
  info: 'var(--color-info)',
  default: 'var(--color-text-secondary)',
}
</script>

<template>
  <span
    class="status-tag"
    :class="[`status-tag--${size}`]"
    :style="{
      backgroundColor: `${statusColors[status]}20`,
      color: statusColors[status],
    }"
  >
    <span class="status-dot" :style="{ backgroundColor: statusColors[status] }" />
    {{ label }}
  </span>
</template>

<style scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border-radius: var(--radius-full);
  font-weight: 500;
}

.status-tag--small {
  padding: 2px 8px;
  font-size: var(--font-size-xs);
}

.status-tag--default {
  padding: 4px 12px;
  font-size: var(--font-size-sm);
}

.status-tag--large {
  padding: 6px 16px;
  font-size: var(--font-size-base);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}
</style>