<script setup lang="ts">
withDefaults(defineProps<{
  variant?: 'full' | 'mark'
  alt?: string
}>(), {
  variant: 'full',
  alt: 'ChainPass 跨境数字身份与合规支付',
})
</script>

<template>
  <span class="brand-logo" :class="`brand-logo--${variant}`">
    <img src="@/assets/logo.jpg" :alt="alt" />
  </span>
</template>

<style scoped>
.brand-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #fff;
}

.brand-logo img {
  display: block;
  max-width: none;
}

.brand-logo--full {
  width: min(100%, 220px);
  aspect-ratio: 1;
  border: 1px solid rgba(14, 165, 233, 0.18);
  border-radius: 24px;
  box-shadow: 0 18px 45px rgba(8, 145, 178, 0.16);
}

.brand-logo--full img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.brand-logo--mark {
  width: 44px;
  height: 44px;
  border-radius: 13px;
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.25);
}

.brand-logo--mark img {
  width: 60px;
  height: 60px;
  transform: translateY(-1px);
}
</style>
