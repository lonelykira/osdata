/**
 * PWA 插件配置 - 适配按需加载
 */
import { VitePWA } from 'vite-plugin-pwa';
import type { VitePWAOptions } from 'vite-plugin-pwa';
import type { PluginOption } from 'vite';

export function configPwaPlugin(isBuild: boolean): PluginOption | PluginOption[] {
  if (!isBuild) {
    console.log('非生产环境不启用 PWA 插件!');
    return [];
  }

  const pwaOptions: Partial<VitePWAOptions> = {
    registerType: 'manual',
    injectRegister: 'inline', // 将 Service Worker 注册代码内联到 HTML 中，避免缓存问题
    includeAssets: ['favicon.ico', 'logo.png'],
    manifest: {
      name: 'JeecgBoot',
      short_name: 'Jeecg',
      theme_color: '#ffffff',
      icons: [
        {
          src: '/logo.png',
          sizes: '192x192',
          type: 'image/png',
        },
        {
          src: '/logo.png',
          sizes: '512x512',
          type: 'image/png',
        },
      ],
    },
    workbox: {
      maximumFileSizeToCacheInBytes: 10 * 1024 * 1024, // 10MB
      cleanupOutdatedCaches: true,
      
      // 预缓存：只缓存关键资源，不预缓存路由组件 CSS/JS（避免登录页加载全部资源）
      globPatterns: [
        'index.html', // 必须预缓存（避免 non-precached-url 错误）
        'manifest.webmanifest',
        'assets/index-*.css', // 仅入口 CSS
        'favicon.ico',
        'logo.png',
        'js/index-*.js',
        'js/*-vendor-*.js',
      ],
      
      // 不使用导航回退功能
      navigateFallback: undefined,
      
      // 运行时缓存：按需加载的资源
      runtimeCaching: [
        {
          urlPattern: /\/js\/.*\.js$/i,
          handler: 'NetworkFirst',
          options: {
            cacheName: 'js-chunks-cache',
            networkTimeoutSeconds: 3,
            expiration: {
              maxEntries: 100,
              maxAgeSeconds: 60 * 60 * 24 * 7, // 7天
            },
            cacheableResponse: {
              statuses: [0, 200],
            },
          },
        },
        {
          urlPattern: /\/assets\/.*\.css$/i,
          handler: 'CacheFirst',
          options: {
            cacheName: 'css-cache',
            expiration: {
              maxEntries: 50,
              maxAgeSeconds: 60 * 60 * 24 * 30, // 30天
            },
            cacheableResponse: {
              statuses: [0, 200],
            },
          },
        },
        // Google Fonts
        {
          urlPattern: /^https:\/\/fonts\.googleapis\.com\/.*/i,
          handler: 'CacheFirst',
          options: {
            cacheName: 'google-fonts-cache',
            expiration: {
              maxEntries: 10,
              maxAgeSeconds: 60 * 60 * 24 * 365,
            },
            cacheableResponse: {
              statuses: [0, 200],
            },
          },
        },
        //update-begin---author:scott ---date:20260417  for：[issues/9564]PWA图片规则过宽导致/filereview/等业务接口走缓存-----------
        // 图片资源（仅缓存构建产物，避免命中 /filereview/、/jeecgboot/ 等业务接口返回的图片）
        {
          urlPattern: /\/(?:assets|img|static|resource)\/.*\.(?:png|jpg|jpeg|svg|gif|webp)$/i,
          handler: 'CacheFirst',
          options: {
            cacheName: 'image-cache',
            expiration: {
              maxEntries: 100,
              maxAgeSeconds: 60 * 60 * 24 * 30,
            },
          },
        },
        // API 请求（JeecgBoot 实际前缀是 /jeecgboot/，原 /api/ 规则未生效）
        {
          urlPattern: /\/jeecgboot\/.*/i,
          handler: 'NetworkOnly',
        },
        //update-end---author:scott ---date:20260417  for：[issues/9564]PWA图片规则过宽导致/filereview/等业务接口走缓存-----------
      ],
      // 启用立即更新：新 SW 立即激活并接管页面
      skipWaiting: true,
      clientsClaim: true,
    },
    devOptions: {
      enabled: false,
    },
  };

  return VitePWA(pwaOptions);
}

