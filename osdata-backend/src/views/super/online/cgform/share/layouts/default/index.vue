<template>
  <template v-if="pageLoading">
    <div style="text-align: center; padding-top: 120px;">
      <a-spin tip="加载中…"/>
    </div>
  </template>
  <template v-else-if="pageErrorTip">
    <ErrorTip :subTitle="pageErrorTip"/>
  </template>
  <Layout v-else :class="prefixCls" v-bind="lockEvents">
    <LayoutHeader v-if="!getIsMobile" fixed/>
    <Layout :class="[layoutClass]">
      <Layout :class="`${prefixCls}-main`">
        <LayoutContent/>
      </Layout>
    </Layout>
  </Layout>
</template>

<script lang="ts">
import {computed, defineComponent, unref, watch} from 'vue';
import {Layout} from 'ant-design-vue';

import LayoutHeader from './header/index.vue';
import LayoutContent from './content/index.vue';
import ErrorTip from './components/ErrorTip.vue';

import {useHeaderSetting} from '/@/hooks/setting/useHeaderSetting';
import {useMenuSetting} from '/@/hooks/setting/useMenuSetting';
import {useDesign} from '/@/hooks/web/useDesign';
import {useLockPage} from '/@/hooks/web/useLockPage';

import {useAppInject} from '/@/hooks/web/useAppInject';
import {useCgformShare} from "../../hooks/useCgformShare";
import {router} from "@/router";

export default defineComponent({
  name: 'DefaultLayout',
  components: {
    LayoutHeader,
    LayoutContent,
    ErrorTip,
    Layout,
  },
  setup() {
    const {prefixCls} = useDesign('online-share-default-layout');

    const {initCgformShare, pageLoading, pageErrorTip} = useCgformShare();

    const {getIsMobile} = useAppInject();
    const {getShowFullHeaderRef} = useHeaderSetting();
    const {getShowSidebar, getIsMixSidebar, getShowMenu} = useMenuSetting();

    // Create a lock screen monitor
    const lockEvents = useLockPage();

    const layoutClass = computed(() => {
      let cls: string[] = ['ant-layout'];
      if (unref(getIsMixSidebar) || unref(getShowMenu)) {
        cls.push('ant-layout-has-sider');
      }
      return cls;
    });

    watch(router.currentRoute, () => {
      initCgformShare()
      console.warn('layout - watch(router.currentRoute :')
    }, {immediate: true})

    return {
      pageLoading,
      pageErrorTip,

      getShowFullHeaderRef,
      getShowSidebar,
      prefixCls,
      getIsMobile,
      getIsMixSidebar,
      layoutClass,
      lockEvents,
    };
  },
});
</script>
<style lang="less">
//noinspection LessUnresolvedVariable
@prefix-cls: ~'@{namespace}-online-share-default-layout';

.@{prefix-cls} {
  display: flex;
  width: 100%;
  min-height: 100%;
  background-color: @content-bg;
  flex-direction: column;

  > .ant-layout {
    min-height: 100%;
  }

  &-main {
    width: 100%;
    margin-left: 1px;
  }
}
</style>
