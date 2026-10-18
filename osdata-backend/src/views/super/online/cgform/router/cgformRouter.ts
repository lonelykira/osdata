import {router} from '/@/router';
import {LAYOUT} from '/@/router/constant';

export function registerCgformRouter() {
  router.addRoute({
    path: '/online-auto-cgform-router',
    name: 'onl-auto-cgform-router',
    component: LAYOUT,
    redirect: '/online/cgform',
    meta: {
      title: 'OnlCgformAuto',
      hideMenu: true,
      hideBreadcrumb: true,
    },
    children: [
      {
        path: '/online/cgformList/:id',
        name: 'OnlineAutoList',
        component: () => import('../auto/default/OnlineAutoList.vue'),
        meta: {title: 'AUTO在线表单', keepAlive: true},
      },
      {
        path: '/online/cgformTreeList/:id',
        name: 'DefaultOnlineList',
        component: () => import('../auto/tree/OnlineAutoTreeList.vue'),
        meta: {title: 'AUTO在线树表单', keepAlive: true},
      },
      {
        path: '/online/cgformErpList/:id',
        name: 'CgformErpList',
        component: () => import('../auto/erp/OnlCgformErpList.vue'),
        meta: {title: 'AUTO在线ERP表单', keepAlive: true},
      },
      {
        path: '/online/cgformInnerTableList/:id',
        name: 'OnlCgformInnerTableList',
        component: () => import('../auto/innerTable/OnlCgformInnerTableList.vue'),
        meta: {title: 'AUTO在线一对多内嵌', keepAlive: true},
      },
      {
        path: '/online/cgformTabList/:id',
        name: 'OnlCgformTabList',
        component: () => import('../auto/tab/OnlCgformTabList.vue'),
        meta: {title: 'AUTO在线Tab风格', keepAlive: true},
      },
    ],
  })
}
