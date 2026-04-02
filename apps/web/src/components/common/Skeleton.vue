<script setup lang="ts">
interface Props {
  rows?: number
  animated?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  rows: 3,
  animated: true,
})
</script>

<template>
  <div class="skeleton">
    <div
      v-for="i in rows"
      :key="i"
      class="skeleton-row"
      :class="{ animated }"
      :style="{
        width: i === rows ? '60%' : '100%',
        height: i === 1 ? '24px' : '16px',
      }"
    />
  </div>
</template>

<style scoped>
.skeleton {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.skeleton-row {
  background-color: var(--color-bg-tertiary);
  border-radius: var(--radius-sm);
}

.skeleton-row.animated {
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}

@keyframes skeleton-pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>