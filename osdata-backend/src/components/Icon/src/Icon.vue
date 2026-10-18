<template>
  <SvgIcon :size="size" :name="getSvgIcon" v-if="isSvgIcon" :class="[$attrs.class, 'anticon']" :spin="spin" />
  <span v-else ref="elRef" :class="[$attrs.class, 'app-iconify anticon', spin && 'app-iconify-spin']" :style="getWrapStyle"></span>
</template>
<script lang="ts">
  import type { PropType } from 'vue';
  import { defineComponent, ref, watch, onMounted, nextTick, unref, computed, CSSProperties } from 'vue';
  import SvgIcon from './SvgIcon.vue';
  import { isString } from '/@/utils/is';
  import { propTypes } from '/@/utils/propTypes';
  const SVG_END_WITH_FLAG = '|svg';
  // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-143】online新增字段图标会闪烁
  // 缓存Iconify实例，避免每次动态import造成图标插入延迟（闪烁）
  let iconifyInstance: any = null;
  // update-end--author:liaozhiyang---date:20260713---for：【LHZP-143】online新增字段图标会闪烁
  export default defineComponent({
    name: 'Icon',
    components: { SvgIcon },
    props: {
      // icon name
      icon: propTypes.string,
      // icon color
      color: propTypes.string,
      // icon size
      size: {
        type: [String, Number] as PropType<string | number>,
        default: 16,
      },
      spin: propTypes.bool.def(false),
      prefix: propTypes.string.def(''),
    },
    setup(props) {
      const elRef = ref<ElRef>(null);

      const isSvgIcon = computed(() => props.icon?.endsWith(SVG_END_WITH_FLAG));
      const getSvgIcon = computed(() => props.icon.replace(SVG_END_WITH_FLAG, ''));
      const getIconRef = computed(() => `${props.prefix ? props.prefix + ':' : ''}${props.icon}`);

      const update = async () => {
        if (unref(isSvgIcon)) return;

        const el = unref(elRef);
        if (!el) return;

        await nextTick();
        const icon = unref(getIconRef);
        if (!icon) return;
        // update-begin--author:liaozhiyang---date:20260304---for:【QQYUN-14802】新增unplugin-icons插件，及icon支持online和local两种模式
        try {
          let svg: SVGElement | null = null;
          // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-143】online新增字段图标会闪烁
          if (iconifyInstance) {
            svg = iconifyInstance.renderSVG(icon, {});
          } else if (import.meta.env.VITE_GLOB_ICONIFY_USE_TYPE === 'local') {
            // 使用本地 purge-icons 图标（离线，图标已打包进产物）
            const iconifyModule = await import('@purge-icons/generated');
            iconifyInstance = iconifyModule.default;
            svg = iconifyInstance.renderSVG(icon, {});
          } else {
            // 使用 @iconify/iconify 在线按需加载
            const iconifyModule = await import('@iconify/iconify');
            iconifyInstance = iconifyModule.default;
            svg = iconifyInstance.renderSVG(icon, {});
          }
          // update-end--author:liaozhiyang---date:20260713---for：【LHZP-143】online新增字段图标会闪烁
          if (svg) {
            el.textContent = '';
            el.appendChild(svg);
          } else {
            // 如果图标不存在，显示占位符
            const span = document.createElement('span');
            span.className = 'iconify';
            span.dataset.icon = icon;
            el.textContent = '';
            el.appendChild(span);
          }
        } catch (err) {
          console.error('Failed to render icon:', icon, err);
        }
        // update-end--author:liaozhiyang---date:20260304---for:【QQYUN-14802】新增unplugin-icons插件，及icon支持online和local两种模式
      };

      const getWrapStyle = computed((): CSSProperties => {
        const { size, color } = props;
        let fs = size;
        if (isString(size)) {
          fs = parseInt(size, 10);
        }

        return {
          fontSize: `${fs}px`,
          color: color,
          display: 'inline-flex',
        };
      });

      watch(() => props.icon, update, { flush: 'post' });

      onMounted(update);

      return { elRef, getWrapStyle, isSvgIcon, getSvgIcon };
    },
  });
</script>
<style lang="less">
  .app-iconify {
    display: inline-block;
    // vertical-align: middle;

    &-spin {
      svg {
        animation: loadingCircle 1s infinite linear;
      }
    }
  }

  span.iconify {
    display: block;
    min-width: 1em;
    min-height: 1em;
    background-color: @iconify-bg-color;
    border-radius: 100%;
  }
</style>
