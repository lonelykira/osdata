// import { router } from "@/router";
// import { apps } from './apps';
//
// export const {registerQiankunRouter} = (function () {
//
//   let registered = false;
//
//   /**
//    * 注册qiankun路由
//    */
//   function registerQiankunRouter() {
//     if (!router) {
//       // 如果路由对象不存在，递归调用，直到路由对象可用
//       setTimeout(() => registerQiankunRouter(), 1);
//     } else {
//       registerQiankunRouterNow();
//     }
//   }
//
//   function registerQiankunRouterNow() {
//     if (registered) {
//       return;
//     }
//     registered = true;
//     const checkQiankunRoute = (path: string) => apps.some(app => path.startsWith('/' + app.name));
//     // 添加路由守卫
//     // 路由守卫，判断是否是qiankun子应用路由
//     router.beforeEach(async (to, from, next) => {
//       const isQiankunRoute = checkQiankunRoute(to.path);
//       if (isQiankunRoute) {
//         // 如果是qiankun子应用路由，设置meta属性
//         to.meta.isQiankunRoute = true;
//       } else {
//         // 如果不是qiankun子应用路由，清除meta属性
//         delete to.meta.isQiankunRoute;
//       }
//       next();
//     });
//   }
//
//
//   return {
//     registerQiankunRouter,
//   }
// })();
