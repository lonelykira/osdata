<template>
  <Dropdown placement="bottomLeft" :overlayClassName="`${prefixCls}-dropdown-overlay`">
    <span :class="[prefixCls, `${prefixCls}--${theme}`]" class="flex">
      <img :class="`${prefixCls}__header`" :src="getAvatarUrl"/>
      <span :class="`${prefixCls}__info hidden md:block`">
        <span :class="`${prefixCls}__name  `" class="truncate">
          {{ getUserInfo.realname }}
        </span>
      </span>
    </span>

    <template #overlay>
      <Menu @click="handleMenuClick">
        <MenuItem itemKey="cache" :text="t('layout.header.dropdownItemRefreshCache')" icon="ion:sync-outline"/>
        <MenuItem itemKey="logout" :text="t('layout.header.dropdownItemLoginOut')" icon="ion:power-outline"/>
      </Menu>
    </template>
  </Dropdown>
</template>
<script lang="ts">
// components
import {Dropdown, Menu} from 'ant-design-vue';

import {computed, defineComponent} from 'vue';

import {useUserStore} from '/@/store/modules/user';

import {useHeaderSetting} from '/@/hooks/setting/useHeaderSetting';
import {useI18n} from '/@/hooks/web/useI18n';
import {useDesign} from '/@/hooks/web/useDesign';
import {useMessage} from '/@/hooks/web/useMessage';
import headerImg from '/@/assets/images/header.jpg';
import {propTypes} from '/@/utils/propTypes';

import {createAsyncComponent} from '/@/utils/factory/createAsyncComponent';

import {queryAllDictItems, refreshCache} from '/@/views/system/dict/dict.api';
import {DB_DICT_DATA_KEY} from '/@/enums/cacheEnum';
import {removeAuthCache, setAuthCache} from '/@/utils/auth';
import {getFileAccessHttpUrl} from '/@/utils/common/compUtils';

type MenuEvent = 'logout' | 'doc' | 'lock' | 'cache' | 'depart';

const {createMessage} = useMessage();

export default defineComponent({
  name: 'UserDropdown',
  components: {
    Dropdown,
    Menu,
    MenuItem: createAsyncComponent(() => import('./DropMenuItem.vue')),
    MenuDivider: Menu.Divider,
  },
  props: {
    theme: propTypes.oneOf(['dark', 'light']),
  },
  setup() {
    const {prefixCls} = useDesign('online-share-header-user-dropdown');
    const {t} = useI18n();
    const {getUseLockPage} = useHeaderSetting();

    const userStore = useUserStore();

    const getUserInfo = computed(() => {
      const {realname = 'NoLogin', avatar} = userStore.getUserInfo || {};
      return {
        realname,
        avatar: avatar || headerImg
      }
    });

    const getAvatarUrl = computed(() => {
      let {avatar} = getUserInfo.value;
      if (avatar == headerImg) {
        return avatar;
      } else {
        return getFileAccessHttpUrl(avatar);
      }
    });

    //  login out
    function handleLoginOut() {
      userStore.confirmLoginOut();
    }

    // 清除缓存
    async function clearCache() {
      const result = await refreshCache();
      if (result.success) {
        const res = await queryAllDictItems();
        removeAuthCache(DB_DICT_DATA_KEY);
        setAuthCache(DB_DICT_DATA_KEY, res.result);
        // update-begin--author:liaozhiyang---date:20240124---for：【QQYUN-7970】国际化
        createMessage.success(t('layout.header.refreshCacheComplete'));
        // update-end--author:liaozhiyang---date:20240124---for：【QQYUN-7970】国际化
        // update-begin--author:wangshuai---date:20241112---for：【issues/7433】vue3 数据字典优化建议
        userStore.setAllDictItems(res.result);
        // update-end--author:wangshuai---date:20241112---for：【issues/7433】vue3 数据字典优化建议
      } else {
        // update-begin--author:liaozhiyang---date:20240124---for：【QQYUN-7970】国际化
        createMessage.error(t('layout.header.refreshCacheFailure'));
        // update-end--author:liaozhiyang---date:20240124---for：【QQYUN-7970】国际化
      }
    }

    function handleMenuClick(e: { key: MenuEvent }) {
      switch (e.key) {
        case 'logout':
          handleLoginOut();
          break;
        case 'cache':
          clearCache();
          break;
      }
    }

    return {
      prefixCls,
      t,
      getUserInfo,
      getAvatarUrl,
      handleMenuClick,
      getUseLockPage,
    };
  }
});
</script>
<style lang="less">
@prefix-cls: ~'@{namespace}-online-share-header-user-dropdown';

.@{prefix-cls} {
  height: @header-height;
  padding: 0 0 0 10px;
  padding-right: 10px;
  overflow: hidden;
  font-size: 12px;
  cursor: pointer;
  align-items: center;

  img {
    width: 24px;
    height: 24px;
    margin-right: 12px;
  }

  &__header {
    border-radius: 50%;
  }

  &__name {
    font-size: 14px;
  }

  &--dark {
    &:hover {
      background-color: @header-dark-bg-hover-color;
    }
  }

  &--light {
    &:hover {
      background-color: @header-light-bg-hover-color;
    }

    .@{prefix-cls}__name {
      color: @text-color-base;
    }

    .@{prefix-cls}__desc {
      color: @header-light-desc-color;
    }
  }

  &-dropdown-overlay {
    // update-begin--author:liaozhiyang---date:20231226---for：【QQYUN-7512】顶部账号划过首次弹出时位置会变更一下
    width: 160px;
    // update-end--author:liaozhiyang---date:20231226---for：【QQYUN-7512】顶部账号划过首次弹出时位置会变更一下
    .ant-dropdown-menu-item {
      min-width: 160px;
    }
  }
}
</style>
