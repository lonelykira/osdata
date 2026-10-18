import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// Ant Design Vue 全量样式（组件本体由 unplugin-vue-components 按需自动注册）
import 'ant-design-vue/dist/reset.css'

// 全局 SCSS 样式（主色调、通用布局类）
import '@/assets/styles/index.scss'

const app = createApp(App)

// 安装 Pinia 状态管理
app.use(createPinia())
// 安装 Vue Router
app.use(router)

app.mount('#app')
