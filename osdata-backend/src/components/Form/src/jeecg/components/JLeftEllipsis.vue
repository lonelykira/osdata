<template>
  <span ref="rootRef" class="j-left-ellipsis" :class="contentClass" :title="fullText">{{ displayText }}</span>
</template>

<script lang="ts" setup>
  import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue';
  import { propTypes } from '/@/utils/propTypes';

  /**
   * 左侧省略：空间不足时显示 "...右侧文字"，保证末尾内容完整可见。
   * 按指定容器（默认 .ant-select-selector）的实际宽度测量，避免被文案自身撑开导致测不准。
   */
  defineOptions({ name: 'JLeftEllipsis' });

  const props = defineProps({
    /** 完整文案 */
    text: propTypes.string.def(''),
    /** 用于测量可用宽度的容器选择器 */
    containerSelector: propTypes.string.def('.ant-select-selector'),
    /** 额外预留宽度（关闭按钮、padding 等），单位 px */
    reserveWidth: propTypes.number.def(40),
    /** 附加到根节点的 class */
    contentClass: propTypes.string.def(''),
  });

  const rootRef = ref<HTMLElement | null>(null);
  const displayText = ref(props.text || '');
  let resizeObserver: ResizeObserver | null = null;
  let measureCtx: CanvasRenderingContext2D | null = null;

  const fullText = computed(() => (props.text == null ? '' : String(props.text)));

  function getMeasureCtx(el: HTMLElement) {
    if (!measureCtx) {
      measureCtx = document.createElement('canvas').getContext('2d');
    }
    if (measureCtx) {
      const style = getComputedStyle(el);
      measureCtx.font = `${style.fontWeight} ${style.fontSize} ${style.fontFamily}`;
    }
    return measureCtx;
  }

  function getMaxTextWidth(el: HTMLElement) {
    const box = (el.closest(props.containerSelector) || el.parentElement) as HTMLElement | null;
    if (!box) {
      return 0;
    }
    const boxStyle = getComputedStyle(box);
    const paddingX = (parseFloat(boxStyle.paddingLeft) || 0) + (parseFloat(boxStyle.paddingRight) || 0);
    return Math.max(box.clientWidth - paddingX - (props.reserveWidth || 0), 24);
  }

  function updateDisplay() {
    const el = rootRef.value;
    const full = fullText.value;
    if (!el || !full) {
      displayText.value = full;
      return;
    }
    const maxWidth = getMaxTextWidth(el);
    if (maxWidth <= 0) {
      displayText.value = full;
      return;
    }
    const ctx = getMeasureCtx(el);
    if (!ctx) {
      displayText.value = full;
      return;
    }
    if (ctx.measureText(full).width <= maxWidth) {
      displayText.value = full;
      return;
    }
    const ellipsis = '...';
    const ellipsisWidth = ctx.measureText(ellipsis).width;
    let lo = 0;
    let hi = full.length;
    while (lo < hi) {
      const mid = Math.ceil((lo + hi) / 2);
      const suffix = full.slice(-mid);
      if (ellipsisWidth + ctx.measureText(suffix).width <= maxWidth) {
        lo = mid;
      } else {
        hi = mid - 1;
      }
    }
    displayText.value = lo > 0 ? ellipsis + full.slice(-lo) : ellipsis;
  }

  function bindObserver() {
    resizeObserver?.disconnect();
    resizeObserver = null;
    if (typeof ResizeObserver === 'undefined' || !rootRef.value) {
      return;
    }
    const el = rootRef.value;
    const box = (el.closest(props.containerSelector) || el.parentElement) as HTMLElement | null;
    resizeObserver = new ResizeObserver(() => updateDisplay());
    if (box) {
      resizeObserver.observe(box);
    } else {
      resizeObserver.observe(el);
    }
  }

  watch(
    () => [props.text, props.reserveWidth, props.containerSelector],
    () => {
      nextTick(() => {
        requestAnimationFrame(updateDisplay);
      });
    }
  );

  onMounted(() => {
    nextTick(() => {
      updateDisplay();
      requestAnimationFrame(updateDisplay);
      bindObserver();
    });
  });

  onBeforeUnmount(() => {
    resizeObserver?.disconnect();
    resizeObserver = null;
  });
</script>

<style lang="less" scoped>
  .j-left-ellipsis {
    display: block;
    min-width: 0;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: clip;
  }
</style>
