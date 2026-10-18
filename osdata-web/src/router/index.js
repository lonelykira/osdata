import { createRouter, createWebHistory } from 'vue-router'

/**
 * 用户端路由配置（仅用户页面，无 admin / 无登录鉴权）
 */
const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'wdi/topic',
        name: 'WdiTopic',
        component: () => import('@/views/wdi/topic.vue'),
        meta: { title: '指标浏览' }
      },
      {
        path: 'wdi/query',
        name: 'WdiQuery',
        component: () => import('@/views/wdi/query.vue'),
        meta: { title: '数据查询' }
      },
    ]
  },
  // 兜底路由：未匹配路径重定向首页
  {
    path: '/:pathMatch(.*)*',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    // 切换页面回到顶部
    return { top: 0 }
  }
})

// 路由后置钩子：动态更新页面标题
router.afterEach((to) => {
  const title = to.meta?.title
  document.title = title ? `${title} - WDI 世界发展指标数据平台` : 'WDI 世界发展指标数据平台'
})

export default router
