<template>
  <PageWrapper title="图标使用示例">
    <CollapseContainer title="Icon组件中iconfiy图标使用（不推荐使用，项目中所有图标都构建在一个js且首屏就加载，没有分割到chuck中，不是按需引入）" class="my-5">
      <div class="flex justify-around flex-wrap">
        <Icon icon="ion:layers-outline" :size="30" />
        <Icon icon="ion:bar-chart-outline" :size="30" />
        <Icon icon="ion:tv-outline" :size="30" />
        <Icon icon="ion:settings-outline" :size="30" />
        <Icon icon="ion:language" :size="30" />
      </div>
    </CollapseContainer>

    <CollapseContainer title="推荐直接使用iconify原生组件（分割到chunk中，按需引入）" class="my-5">
      <div class="flex justify-around flex-wrap">
        <IconifyonLayersOutline class="text-30px" />
        <IconifyIonBarChartOutline class="text-30px" />
        <IconifyIonTvOutline class="text-30px" />
        <IconifyIonSettingsOutline class="text-30px" />
        <IconIonLanguage class="text-30px" />
      </div>
    </CollapseContainer>

    <CollapseContainer title="Antv Icon使用 (直接按需引入相应组件即可)">
      <div class="flex justify-around">
        <GithubFilled :style="{ fontSize: '30px' }" />
        <QqCircleFilled :style="{ fontSize: '30px' }" />
        <WechatFilled :style="{ fontSize: '30px' }" />
        <AlipayCircleFilled :style="{ fontSize: '30px' }" />
        <IeCircleFilled :style="{ fontSize: '30px' }" />
        <TaobaoCircleFilled :style="{ fontSize: '30px' }" />
        <CodepenCircleFilled :style="{ fontSize: '30px' }" />
      </div>
    </CollapseContainer>

    <CollapseContainer title="svg 雪碧图" class="my-5">
      <div class="flex justify-around flex-wrap">
        <SvgIcon name="test" size="32" />
        <template v-for="item in 6" :key="item">
          <SvgIcon :name="`dynamic-avatar-${item}`" size="32" />
        </template>
      </div>
    </CollapseContainer>

    <CollapseContainer title="图标选择器(Iconify)" class="my-5">
      <div class="flex justify-around flex-wrap">
        <IconPicker />
      </div>
    </CollapseContainer>

    <CollapseContainer title="图标选择器(Svg)" class="my-5">
      <div class="flex justify-around flex-wrap">
        <IconPicker mode="svg" />
      </div>
    </CollapseContainer>

    <Alert
      show-icon
      message="推荐使用Iconify组件"
      description="Icon组件基本包含所有的图标,在下面网址内你可以查询到你想要的任何图标。并且打包只会打包所用到的图标。"
    />
    <a-button type="link" @click="toIconify"> Iconify 图标大全 </a-button>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { CollapseContainer } from '/@/components/Container/index';
  import { Alert } from 'ant-design-vue';
  import {
    QqCircleFilled,
    GithubFilled,
    WechatFilled,
    AlipayCircleFilled,
    IeCircleFilled,
    TaobaoCircleFilled,
    CodepenCircleFilled,
  } from '@ant-design/icons-vue';

  import { Icon, IconPicker, SvgIcon } from '/@/components/Icon/index';

  import { openWindow } from '/@/utils';
  import { PageWrapper } from '/@/components/Page';
  import IconIonLanguage from '~icons/ion/language'
  export default defineComponent({
    components: {
      PageWrapper,
      CollapseContainer,
      GithubFilled,
      QqCircleFilled,
      WechatFilled,
      AlipayCircleFilled,
      IeCircleFilled,
      TaobaoCircleFilled,
      CodepenCircleFilled,
      Icon,
      Alert,
      IconPicker,
      SvgIcon,
      IconIonLanguage,
    },
    setup() {
      return {
        toIconify: () => {
          openWindow('https://iconify.design/');
        },
      };
    },
  });
</script>
