<template>
  <Layout :class="[layoutBoxClass]" v-bind="lockEvents">
    <LayoutFeatures />
    <LayoutHeader fixed v-if="getShowFullHeaderRef" />
    <Layout :class="[layoutClass]">
      <LayoutSideBar v-if="getShowSidebar || getIsMobile" />
      <Layout :class="layoutMainClass">
        <LayoutMultipleHeader />
        <LayoutContent />
        <LayoutFooter />
      </Layout>
    </Layout>
  </Layout>
</template>

<script lang="ts">
  import { defineComponent, computed, unref, ref } from 'vue';
  import { Layout } from 'ant-design-vue';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';

  import LayoutHeader from './header/index.vue';
  import LayoutContent from './content/index.vue';
  import LayoutSideBar from './sider/index.vue';
  import LayoutMultipleHeader from './header/MultipleHeader.vue';

  import { useHeaderSetting } from '/@/hooks/setting/useHeaderSetting';
  import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useLockPage } from '/@/hooks/web/useLockPage';

  import { useAppInject } from '/@/hooks/web/useAppInject';
  import { useGlobSetting } from '/@/hooks/setting';

  export default defineComponent({
    name: 'DefaultLayout',
    components: {
      LayoutFeatures: createAsyncComponent(() => import('/@/layouts/default/feature/index.vue')),
      LayoutFooter: createAsyncComponent(() => import('/@/layouts/default/footer/index.vue')),
      LayoutHeader,
      LayoutContent,
      LayoutSideBar,
      LayoutMultipleHeader,
      Layout,
    },
    setup() {
      const { prefixCls } = useDesign('default-layout');
      const { getIsMobile } = useAppInject();
      const { getShowFullHeaderRef } = useHeaderSetting();
      const { getShowSidebar, getIsMixSidebar, getShowMenu, getMenuType } = useMenuSetting();
      const glob = useGlobSetting();
      const { isQiankunMicro } = glob;

      // Create a lock screen monitor
      const lockEvents = useLockPage();

      const layoutBoxClass = computed(() => {
        let cls: string[] = [prefixCls];
        if (unref(getMenuType)) {
          cls.push(`${prefixCls}--menu-${unref(getMenuType)}`);
        }
        return cls;
      });

      const layoutClass = computed(() => {
        let cls: string[] = ['ant-layout'];
        if (unref(getIsMixSidebar) || unref(getShowMenu)) {
          cls.push('ant-layout-has-sider');
        }
        return cls;
      });

      const layoutMainClass = computed(() => {
        let cls: string[] = [`${prefixCls}-main`];

        // 【JEECG作为乾坤子应用】
        if (unref(isQiankunMicro)) {
          cls.push(`${prefixCls}-main--qiankun-micro`);
        }
        return cls;
      });

      return {
        getShowFullHeaderRef,
        getShowSidebar,
        prefixCls,
        getIsMobile,
        getIsMixSidebar,
        isQiankunMicro,
        layoutBoxClass,
        layoutClass,
        layoutMainClass,
        lockEvents
      };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-default-layout';

  .@{prefix-cls} {
    display: flex;
    width: 100%;
    min-height: 100%;
    background-color: @content-bg;
    flex-direction: column;

    &--menu {
      // 【JEECG作为乾坤子应用】
      &-mix-sidebar {

        .@{namespace}-layout-mix-sider {
          position: absolute;
          overflow: visible;

          > .@{namespace}-layout-mix-sider-menu-list {
            position: absolute;
          }
        }
      }

      // 【JEECG作为乾坤子应用】
      &-mix {
        .@{namespace}-multiple-tabs {
          margin-top: 0 !important;
        }
      }
    }

    > .ant-layout {
      min-height: 100%;
    }

    &-main {
      width: 100%;
      // 代码逻辑说明:【issues/8709】LayoutContent样式多出1px
      // margin-left: 1px;

      // 【JEECG作为乾坤子应用】根 Layout 作为 absolute 定位的参照容器
      &--qiankun-micro {
        position: relative;

        .@{namespace}-multiple-tabs {
          margin-top: 60px;
        }
      }

    }
  }
</style>
