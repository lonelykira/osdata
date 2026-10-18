import {router} from '/@/router';
import {LAYOUT} from '/@/router/constant';

export function registerGraphreportRouter() {
  router.addRoute({
    path: '/online-auto-graphreport-router',
    name: 'onl-auto-graphreport-router',
    component: LAYOUT,
    redirect: '/online/graphreport',
    meta: {
      title: 'OnlGraphreportAuto',
      hideMenu: true,
      hideBreadcrumb: true,
    },
    children: [
      {
        path: '/online/graphreport/chart/:code',
        name: 'GraphreportAutoChart',
        component: () => import('../auto/GraphreportAutoChart.vue'),
        meta: {title: 'AUTO在线图表'},
      },
    ],
  })
}
