<script setup>
/**
 * 全局加载条
 * 订阅 request.js 中的 loadingState，进行中的请求 > 0 时显示顶部进度条
 */
import { loadingState } from '@/utils/request'
</script>

<template>
  <!-- 顶部细进度条：有请求时出现动画 -->
  <Transition name="fade">
    <div v-if="loadingState.visible" class="global-loading-bar">
      <div class="loading-inner"></div>
    </div>
  </Transition>
</template>

<style lang="scss" scoped>
.global-loading-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  z-index: 9999;
  background: rgba(24, 144, 255, 0.15);
  overflow: hidden;

  .loading-inner {
    height: 100%;
    width: 40%;
    background: $primary-color;
    border-radius: 2px;
    animation: loading-slide 1.2s ease-in-out infinite;
  }
}

// 进度条往复滑动动画
@keyframes loading-slide {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(350%);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
