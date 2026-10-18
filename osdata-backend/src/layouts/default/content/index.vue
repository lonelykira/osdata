<template>
  <div :class="[prefixCls, getLayoutContentMode]" v-loading="getOpenPageLoading && getPageLoading">
    <PageLayout />
    <div :id="qiankunDivId" class="app-view-box" v-if="openQiankun && qiankunDivId"></div>
  </div>
</template>
<script lang="ts">
  import { ref, defineComponent } from 'vue';
  import PageLayout from '/@/layouts/page/index.vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useRootSetting } from '/@/hooks/setting/useRootSetting';
  import { useTransitionSetting } from '/@/hooks/setting/useTransitionSetting';
  import { useContentViewHeight } from './useContentViewHeight';
  // import registerApps from '/@/qiankun';
  import { useGlobSetting } from '/@/hooks/setting';
  export default defineComponent({
    name: 'LayoutContent',
    components: { PageLayout },
    setup() {
      const { prefixCls } = useDesign('layout-content');
      const { getOpenPageLoading } = useTransitionSetting();
      const { getLayoutContentMode, getPageLoading } = useRootSetting();
      const globSetting = useGlobSetting();
      const openQiankun = globSetting.openQianKun == 'true';
      const qiankunDivId = ref('');
      useContentViewHeight();

      // // 注册 qiankun
      // if (openQiankun) {
      //   qiankunDivId.value = registerApps?.containerId;
      //   registerApps();
      // }

      return {
        prefixCls,
        openQiankun,
        qiankunDivId,
        getOpenPageLoading,
        getLayoutContentMode,
        getPageLoading,
      };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-layout-content';

  .@{prefix-cls} {
    position: relative;
    flex: 1 1 auto;
    min-height: 0;

    &.fixed {
      width: 1200px;
      margin: 0 auto;
    }

    &-loading {
      position: absolute;
      top: 200px;
      z-index: @page-loading-z-index;
    }
  }
</style>
