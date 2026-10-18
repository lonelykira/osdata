import type { AppRouteRecordRaw, AppRouteModule } from '/@/router/types';

import { PAGE_NOT_FOUND_ROUTE, REDIRECT_ROUTE } from '/@/router/routes/basic';

import { mainOutRoutes } from './mainOut';
import { PageEnum } from '/@/enums/pageEnum';
import { t } from '/@/hooks/web/useI18n';
import { LAYOUT } from '/@/router/constant';

const modules = import.meta.glob('./modules/**/*.ts', { eager: true });

const routeModuleList: AppRouteModule[] = [];

// 加入到路由集合中
Object.keys(modules).forEach((key) => {
  const mod = (modules as Recordable)[key].default || {};
  const modList = Array.isArray(mod) ? [...mod] : [mod];
  routeModuleList.push(...modList);
});

export const asyncRoutes = [PAGE_NOT_FOUND_ROUTE, ...routeModuleList];

export const RootRoute: AppRouteRecordRaw = {
  path: '/',
  name: 'Root',
  redirect: PageEnum.BASE_HOME,
  meta: {
    title: 'Root',
  },
};

export const LoginRoute: AppRouteRecordRaw = {
  path: '/login',
  name: 'Login',
  //新版后台登录，如果想要使用旧版登录放开即可
  // component: () => import('/@/views/sys/login/Login.vue'),
  component: () => import('/@/views/system/loginmini/MiniLogin.vue'),
  meta: {
    title: t('routes.basic.login'),
  },
};

// 代码逻辑说明: auth2登录页面路由------------
export const Oauth2LoginRoute: AppRouteRecordRaw = {
  path: '/oauth2-app/login',
  name: 'oauth2-app-login',
  //新版钉钉免登录，如果想要使用旧版放开即可
  // component: () => import('/@/views/sys/login/OAuth2Login.vue'),
  component: () => import('/@/views/system/loginmini/OAuth2Login.vue'),
  meta: {
    title: t('routes.oauth2.login'),
  },
};

/**
 * 【通过token直接静默登录】流程办理登录页面 中转跳转
 */
export const TokenLoginRoute: AppRouteRecordRaw = {
  path: '/tokenLogin',
  name: 'TokenLoginRoute',
  component: () => import('/@/views/sys/login/TokenLoginPage.vue'),
  meta: {
    title: '带token登录页面',
    ignoreAuth: true,
  },
};
// 代码逻辑说明: 【QQYUN-7967】新增、编辑路由访问
export const formUrlDetail = {
  path: '/online/formUrlDetail/:id/:dataId',
  name: 'formUrlDetail',
  component: () => import('/@/views/super/online/cgform/auto/default/OnlineFormUrlDetail.vue'),
  meta: {
    title: '外部填报表单详情',
    ignoreAuth: true,
  },
};

export const formUrlAdd = {
  path: '/online/formUrlAdd/:id',
  name: 'formUrlAdd',
  component: () => import('/@/views/super/online/cgform/auto/default/OnlineFormUrlAdd.vue'),
  meta: {
    title: '外部填报表单新增',
    ignoreAuth: true,
  },
};

export const formUrlEdit = {
  path: '/online/formUrlEdit/:id/:dataId',
  name: 'formUrlEdit',
  component: () => import('/@/views/super/online/cgform/auto/default/OnlineFormUrlEdit.vue'),
  meta: {
    title: '外部填报表单编辑',
    ignoreAuth: true,
  },
};

export const formUrlSuccess = {
  path: '/online/formUrlSuccess',
  name: 'formUrlSuccess',
  component: () => import('/@/views/super/online/cgform/auto/default/OnlineFormUrlSuccess.vue'),
  meta: {
    title: '外部填报表单成功',
    ignoreAuth: true,
  },
};


export const onlinePreview = {
  path: '/onlinePreview',
  name: 'online-preview',
  meta: {
    title: 'wps文件预览',
    ignoreAuth: false,
  },
  component: () => import('/@/components/onlinePreview/WpsFileView.vue'),
}



// Basic routing without permission
export const basicRoutes = [LoginRoute, RootRoute, ...mainOutRoutes, REDIRECT_ROUTE, PAGE_NOT_FOUND_ROUTE, TokenLoginRoute, Oauth2LoginRoute, formUrlDetail, formUrlAdd, formUrlEdit, formUrlSuccess, onlinePreview];
