<template>
  <!-- 机器人 SVG 图标：currentColor 跟随父级颜色，任意背景可用 -->
  <svg :width="size" :height="size" viewBox="0 0 48 48" fill="currentColor" aria-hidden="true">
    <!-- 天线 -->
    <circle cx="24" cy="5.5" r="3" />
    <rect x="22.4" y="8" width="3.2" height="5" rx="1.6" />
    <!-- 左右耳 -->
    <rect x="3.5" y="20" width="4.5" height="8" rx="2.25" />
    <rect x="40" y="20" width="4.5" height="8" rx="2.25" />
    <!-- 头部主体（眼睛挖空，fill-rule=evenodd） -->
    <path fill-rule="evenodd" d="M8 14a7 7 0 0 1 7-7h18a7 7 0 0 1 7 7v20a7 7 0 0 1-7 7H15a7 7 0 0 1-7-7V14zM15 22a4 4 0 0 0 0 8 4 4 0 0 0 0-8zM33 22a4 4 0 0 0 0 8 4 4 0 0 0 0-8z" />
    <!-- 嘴巴 -->
    <rect x="18" y="33.5" width="12" height="2.6" rx="1.3" opacity="0.85" />
  </svg>
</template>

<script setup>
defineProps({
  size: { type: [Number, String], default: 20 }
})
</script>
