import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { AntDesignVueResolver } from 'unplugin-vue-components/resolvers'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    // Vue3 单文件组件支持
    vue(),
    // 自动引入 Vue API（ref、reactive 等），无需手动 import
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      dts: false
    }),
    // Ant Design Vue 组件按需自动注册（模板里直接使用 a-button 等）
    Components({
      resolvers: [
        AntDesignVueResolver({
          importStyle: false // 样式由 main.js 统一全量引入 css，避免按需样式与全局主题冲突
        })
      ],
      dts: false
    })
  ],
  resolve: {
    // @ 指向 src 目录
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        // 全局注入变量与混入，所有 scss/vue 文件可直接使用
        additionalData: `@use "@/assets/styles/variables.scss" as *;`
      }
    }
  },
  server: {
    port: 5173,
    open: false,
    // 接口代理：前端统一请求 /api，转发到后端服务（rewrite 去除 /api 前缀）
    // 例：/api/webapi/wdi/statisticsSummary -> http://localhost:10060/osdata-server/webapi/wdi/statisticsSummary
    proxy: {
      '/api': {
        target: 'http://localhost:10060/osdata-server',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  build: {
    chunkSizeWarningLimit: 1500,
    rollupOptions: {
      output: {
        // 拆分大依赖，提升加载性能
        manualChunks: {
          echarts: ['echarts'],
          'ant-design-vue': ['ant-design-vue']
        }
      }
    }
  }
})
