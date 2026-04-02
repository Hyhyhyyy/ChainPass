<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  size?: 'small' | 'default' | 'large'
  disabled?: boolean
  loading?: boolean
  icon?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'primary',
  size: 'default',
  disabled: false,
  loading: false,
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const buttonClass = computed(() => [
  'cp-button',
  `cp-button--${props.type}`,
  `cp-button--${props.size}`,
  {
    'is-disabled': props.disabled,
    'is-loading': props.loading,
  },
])
</script>

<template>
  <button
    :class="buttonClass"
    :disabled="disabled || loading"
    @click="emit('click', $event)"
  >
    <el-icon v-if="loading" class="is-loading">
      <Loading />
    </el-icon>
    <el-icon v-else-if="icon">
      <component :is="icon" />
    </el-icon>
    <slot />
  </button>
</template>

<style scoped>
.cp-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  border: none;
  border-radius: var(--radius-md);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.cp-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

/* Sizes */
.cp-button--small {
  padding: 6px 12px;
  font-size: var(--font-size-xs);
}

.cp-button--default {
  padding: 8px 16px;
  font-size: var(--font-size-sm);
}

.cp-button--large {
  padding: 12px 24px;
  font-size: var(--font-size-base);
}

/* Types */
.cp-button--primary {
  background-color: var(--color-primary);
  color: white;
}

.cp-button--primary:hover:not(:disabled) {
  background-color: var(--color-primary-dark);
}

.cp-button--success {
  background-color: var(--color-success);
  color: white;
}

.cp-button--success:hover:not(:disabled) {
  filter: brightness(0.9);
}

.cp-button--warning {
  background-color: var(--color-warning);
  color: white;
}

.cp-button--warning:hover:not(:disabled) {
  filter: brightness(0.9);
}

.cp-button--danger {
  background-color: var(--color-danger);
  color: white;
}

.cp-button--danger:hover:not(:disabled) {
  filter: brightness(0.9);
}

.cp-button--info {
  background-color: var(--color-info);
  color: white;
}

.cp-button--info:hover:not(:disabled) {
  filter: brightness(0.9);
}

.is-loading .el-icon {
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
</style>