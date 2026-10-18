<template>
  <Header :class="getHeaderClass">
    <!-- left start -->
    <div :class="`${prefixCls}-left`">
      <!-- logo -->
      <AppLogo
          :class="`${prefixCls}-logo`"
          :theme="getHeaderTheme"
          :style="getLogoWidth"
      />
      <!-- 欢迎语 -->
      <span
          :class="[prefixCls, `${prefixCls}--${getHeaderTheme}`,'headerIntroductionClass']"
      >
        {{ t('layout.header.welcomeIn') }} {{ title }}
      </span>
    </div>
    <!-- left end -->

    <!-- action  -->
    <div :class="`${prefixCls}-action`">
      <FullScreen :class="`${prefixCls}-action__item fullscreen-item`"/>
      <UserDropDown :theme="getHeaderTheme"/>
    </div>
  </Header>
  <LoginSelect ref="loginSelectRef"/>
  <div v-if="fixed" style="height: 60px;"></div>
</template>

<script lang="ts">
import {computed, defineComponent, onMounted, ref, toRaw, unref} from 'vue';
import {useGlobSetting} from '/@/hooks/setting';
import {propTypes} from '/@/utils/propTypes';

import {Layout} from 'ant-design-vue';
import {AppLogo} from '/@/components/Application';

import {useHeaderSetting} from '/@/hooks/setting/useHeaderSetting';
import {useMenuSetting} from '/@/hooks/setting/useMenuSetting';
import {FullScreen, LayoutBreadcrumb} from "@/layouts/default/header/components";
import {UserDropDown} from './components';
import {useAppInject} from '/@/hooks/web/useAppInject';
import {useDesign} from '/@/hooks/web/useDesign';

import LoginSelect from '/@/views/sys/login/LoginSelect.vue';
import {useUserStore} from '/@/store/modules/user';
import {useI18n} from '/@/hooks/web/useI18n';

const {t} = useI18n();

export default defineComponent({
  name: 'LayoutHeader',
  components: {
    Header: Layout.Header,
    AppLogo,
    LayoutBreadcrumb,
    UserDropDown,
    FullScreen,
    LoginSelect,
  },
  props: {
    fixed: propTypes.bool,
  },
  setup(props) {
    const {prefixCls} = useDesign('online-share-layout-header');
    const userStore = useUserStore();
    const {
      getIsMixMode,
      getMenuWidth,
    } = useMenuSetting();
    const {title} = useGlobSetting();

    const {
      getHeaderTheme,
    } = useHeaderSetting();

    const {getIsMobile} = useAppInject();

    const getHeaderClass = computed(() => {
      const theme = unref(getHeaderTheme);
      return [
        prefixCls,
        {
          [`${prefixCls}--fixed`]: props.fixed,
          [`${prefixCls}--mobile`]: unref(getIsMobile),
          [`${prefixCls}--${theme}`]: theme,
        },
      ];
    });


    const getLogoWidth = computed(() => {
      if (!unref(getIsMixMode) || unref(getIsMobile)) {
        return {};
      }
      const width = unref(getMenuWidth) < 180 ? 180 : unref(getMenuWidth);
      return {width: `${width}px`};
    });

    /**
     * 首页多租户部门弹窗逻辑
     */
    const loginSelectRef = ref();

    function showLoginSelect() {
      //判断是否是登陆进来
      const loginInfo = toRaw(userStore.getLoginInfo) || {};
      if (!!loginInfo.isLogin) {
        loginSelectRef.value.show(loginInfo);
      }
    }

    onMounted(() => {
      showLoginSelect();
    });

    return {
      t,
      title,
      loginSelectRef,

      prefixCls,
      getHeaderClass,
      getHeaderTheme,
      getIsMobile,
      getLogoWidth,
    };
  },
});

</script>
<style lang="less">
@import './index.less';
// 顶部欢迎语展示样式
// noinspection LessUnresolvedVariable
@prefix-cls: ~'@{namespace}-online-share-layout-header';

.ant-layout .@{prefix-cls} {
  display: flex;
  padding: 0 8px;
  height: @header-height;
  align-items: center;

  .headerIntroductionClass {
    margin-right: 4px;
    margin-bottom: 2px;
    border-bottom: 0;
    border-left: 0;
  }

  &--light {
    .headerIntroductionClass {
      color: #000;
    }
  }

  &--dark {
    .headerIntroductionClass {
      color: rgba(255, 255, 255, 1);
    }

    .anticon, .truncate {
      color: rgba(255, 255, 255, 1);
    }
  }
}
</style>
