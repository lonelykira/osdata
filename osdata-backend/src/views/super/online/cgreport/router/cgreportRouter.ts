import {router} from '/@/router';
import {LAYOUT} from '/@/router/constant';

export function registerCgreportRouter() {
  router.addRoute({
    path: '/online-auto-cgreport-router',
    name: 'onl-auto-cgreport-router',
    component: LAYOUT,
    redirect: '/online/cgreport',
    meta: {
      title: 'OnlCgreportAuto',
      hideMenu: true,
      hideBreadcrumb: true,
    },
    children: [
      {
        path: '/online/cgreport/:id',
        name: 'OnlCgReportList',
        component: () => import('../auto/OnlCgReportList.vue'),
        meta: {title: 'AUTO在线报表'},
      },
    ],
  })
}
