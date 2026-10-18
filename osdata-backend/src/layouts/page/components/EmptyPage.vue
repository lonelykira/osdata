<template>
  <template v-if="isQiankunRoute">
    <!-- qiankun 路由不显示空白页提示 -->
  </template>
  <!-- 【QQYUN-13593】空白页美化 -->
  <div v-else class="animationEffect" :style="effectVars">
    <div class="effect-layer">
      <div class="blob blob-a"></div>
      <div class="blob blob-b"></div>
      <div class="blob blob-c"></div>
    </div>
    <div class="effect-grid"></div>
    <div class="effect-tip">
      <p>{{ pageTip }}</p>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { router } from "@/router";
import { useEmpty } from '../useEmpty';

// 判断是否是 qiankun 路由
const isQiankunRoute = computed(() => !!router.currentRoute.value?.meta?.isQiankunRoute);

const {pageTip, effectVars} = useEmpty();
</script>

<style lang="less" scoped>
/** update-begin---author:liaozy ---date:2025-08-26  for：空白页美化样式 */
.pageTip {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  font-size: 18px;
  color: #999;
  margin: 0;
}

.animationEffect {
  position: relative;
  height: 100%;
  min-height: 420px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, var(--bg-1) 0%, var(--bg-2) 100%);
}

.effect-layer {
  position: absolute;
  top: -20%;
  left: -20%;
  right: -20%;
  bottom: -20%;
  filter: blur(30px);
  pointer-events: none;
  z-index: 0;
}

.blob {
  position: absolute;
  width: 380px;
  height: 380px;
  border-radius: 50%;
  opacity: 0.45;
}

.blob-a {
  background: radial-gradient(circle at 30% 30%, var(--blob-a-1) 0%, var(--blob-a-2) 60%, var(--blob-a-2) 100%);
  left: 5%;
  top: 10%;
  animation: float-a 18s ease-in-out infinite;
}

.blob-b {
  background: radial-gradient(circle at 30% 30%, var(--blob-b-1) 0%, var(--blob-b-2) 60%, var(--blob-b-2) 100%);
  right: 0;
  top: 30%;
  animation: float-b 22s ease-in-out infinite;
}

.blob-c {
  background: radial-gradient(circle at 30% 30%, var(--blob-c-1) 0%, var(--blob-c-2) 60%, var(--blob-c-2) 100%);
  left: 35%;
  bottom: -5%;
  animation: float-c 26s ease-in-out infinite;
}

@keyframes float-a {
  0% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(20%, -10%) scale(1.05);
  }
  50% {
    transform: translate(35%, 5%) scale(0.95);
  }
  75% {
    transform: translate(10%, 15%) scale(1.02);
  }
  100% {
    transform: translate(0, 0) scale(1);
  }
}

@keyframes float-b {
  0% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(-15%, 10%) scale(1.08);
  }
  50% {
    transform: translate(-30%, -5%) scale(0.92);
  }
  75% {
    transform: translate(-10%, -15%) scale(1.03);
  }
  100% {
    transform: translate(0, 0) scale(1);
  }
}

@keyframes float-c {
  0% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(-10%, -10%) scale(0.9);
  }
  50% {
    transform: translate(10%, -25%) scale(1.05);
  }
  75% {
    transform: translate(20%, 0%) scale(0.98);
  }
  100% {
    transform: translate(0, 0) scale(1);
  }
}

.effect-grid {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(0deg, var(--grid-color) 1px, rgba(0, 0, 0, 0) 1px),
  linear-gradient(90deg, var(--grid-color) 1px, rgba(0, 0, 0, 0) 1px);
  background-size: 36px 36px, 36px 36px;
  mask-image: radial-gradient(circle at 50% 50%, rgba(0, 0, 0, 0.8), rgba(0, 0, 0, 0) 70%);
  pointer-events: none;
  z-index: 1;
}

.effect-tip {
  position: relative;
  z-index: 2;
  text-align: center;
  pointer-events: none;

  p {
    margin: 0;
    padding: 8px 14px;
    color: var(--tip-color);
    font-size: 20px;
    border-radius: 8px;
  }
}

/** update-end---author:liaozy ---date:2025-08-26  for：空白页美化样式 */
</style>
