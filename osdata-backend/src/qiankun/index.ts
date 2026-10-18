// /**
//  * qiankun配置
//  */
// import {
//   start,
//   registerMicroApps,
//   runAfterFirstMounted,
//   addGlobalUncaughtErrorHandler
// } from 'qiankun';
// import { apps, containerId } from './apps';
// import { getProps, initGlState } from './state';
// import { registerQiankunRouter } from './route';
//
// registerQiankunRouter();
//
// /**
//  * 重构apps
//  */
// function filterApps() {
//   apps.forEach((item) => {
//     //主应用需要传递给微应用的数据。
//     // @ts-ignore
//     item.props = getProps();
//     //微应用触发的路由规则
//     // @ts-ignore
//     item.activeRule = genActiveRule('/' + item.activeRule);
//   });
//   return apps;
// }
//
// /**
//  * 路由监听
//  * @param {*} routerPrefix 前缀
//  */
// function genActiveRule(routerPrefix) {
//   return (location) => location.pathname.startsWith(routerPrefix);
// }
//
// let retryCount = 0;
//
// /**
//  * 微应用注册
//  */
// function registerApps() {
//   const container = document.querySelector('#' + containerId);
//   if (!container) {
//     // 如果容器不存在，递归尝试注册应用，最多尝试10次，每次间隔500毫秒
//     if (retryCount < 10) {
//       retryCount++;
//       setTimeout(() => registerApps(), 500);
//     }
//   } else {
//     registerAppsNow();
//   }
// }
//
// registerApps['containerId'] = containerId;
//
// function registerAppsNow() {
//   if (window.qiankunStarted) {
//     return;
//   }
//   window.qiankunStarted = true;
//   const _apps = filterApps();
//   // @ts-ignore
//   registerMicroApps(_apps, {
//     beforeLoad: [
//       // @ts-ignore
//       (loadApp) => {
//         console.log('[qiankun] before load', loadApp);
//       },
//     ],
//     beforeMount: [
//       // @ts-ignore
//       (mountApp) => {
//         console.log('[qiankun] before mount', mountApp);
//       },
//     ],
//     afterMount: [
//       // @ts-ignore
//       (mountApp) => {
//         console.log('[qiankun] after mount', mountApp);
//       },
//     ],
//     beforeUnmount: [
//       // @ts-ignore
//       (unloadApp) => {
//         console.log('[qiankun] before unmount', unloadApp);
//       },
//     ],
//     afterUnmount: [
//       // @ts-ignore
//       (unloadApp) => {
//         console.log('[qiankun] after unmount', unloadApp);
//       },
//     ],
//   });
//   // 设置默认子应用,与 genActiveRule中的参数保持一致
//   // setDefaultMountApp();
//   // 第一个微应用 mount 后需要调用的方法，比如开启一些监控或者埋点脚本。
//   runAfterFirstMounted(() => console.log('开启监控'));
//   // 添加全局的未捕获异常处理器。
//   addGlobalUncaughtErrorHandler((event) => console.log(event));
//   // 定义全局状态
//   initGlState();
//   //启动qiankun
//   start({});
// }
//
// export default registerApps;
