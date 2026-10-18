# osdata-web（WDI 世界发展指标数据平台）

WDI 世界发展指标数据查询网站（面向普通访客），提供指标浏览、数据查询（长表 / 宽表透视 / 图表）、统计概览与 CSV 导出等能力，对接后端 jeecg-boot（osdata-server）的 `/webapi/wdi/` 系列接口。

## 技术栈

| 分类 | 技术 | 说明 |
| --- | --- | --- |
| 框架 | Vue `^3.5` | Composition API + `<script setup>` 单文件组件 |
| 构建工具 | Vite `^5.4` | 开发服务器端口 5173，构建产物在 `dist/` |
| 路由 | Vue Router `^4.5` | `createWebHistory` 模式，路由懒加载 |
| 状态管理 | Pinia `^2.3` | 查询条件 / 查询结果跨页面共享（`src/pinia/wdiStore.js`） |
| UI 组件库 | Ant Design Vue `^4.2` | 通过 `unplugin-vue-components` 按需自动注册，样式在 `main.js` 全量引入 |
| 图表 | ECharts `^5.6` | 封装为 `src/components/EChart.vue` |
| HTTP 请求 | Axios `^1.7` | 统一封装 `src/utils/request.js` |
| CSS 预处理 | Sass | 全局自动注入 `@/assets/styles/variables.scss` 变量 |
| 自动导入 | unplugin-auto-import / unplugin-vue-components | `ref`、`computed` 等 Vue API 与 `a-*` 组件免手动 import |
| 其他 | dayjs `^1.11` | 日期处理 |

## 环境要求

- Node.js >= 18（开发环境使用 v20 验证）
- npm（Node 自带即可）
- 后端服务：jeecg-boot `osdata-server` 运行在 `http://localhost:10060`（WDI 接口前缀 `/webapi/wdi/`，已在后端 Shiro 配置中放行，前端无需登录 token）

## 启动方式

```bash
# 1. 安装依赖
npm install

# 2. 启动开发服务器（默认 http://localhost:5173）
npm run dev

# 3. 生产构建（产物输出到 dist/）
npm run build

# 4. 本地预览生产构建产物
npm run preview
```

> 开发模式下无需额外配置环境变量：所有请求统一走 `/api` 前缀，由 Vite 代理转发到后端。

## 接口代理说明

见 `vite.config.js` 中的 `server.proxy`：

```
前端请求: /api/webapi/wdi/statisticsSummary
实际转发: http://localhost:10060/osdata-server/webapi/wdi/statisticsSummary
```

- 匹配 `/api` 前缀的请求转发到 `http://localhost:10060/osdata-server`，并 rewrite 去除 `/api`
- 如后端地址 / 端口变更，修改 `vite.config.js` 中 `proxy['/api'].target` 即可

## 目录结构

```
osdata-web
├── vite.config.js            # Vite 配置（自动导入、SCSS 全局变量、代理、分包）
├── package.json
└── src
    ├── main.js               # 应用入口（全量引入 Ant Design Vue 样式）
    ├── App.vue
    ├── api/
    │   └── wdi.js            # WDI 接口封装（主题/指标/国家/面板查询/统计概览）
    ├── assets/styles/
    │   └── variables.scss    # 全局 SCSS 变量（自动注入所有样式文件）
    ├── components/           # 通用组件
    │   ├── IndicatorPicker.vue   # 指标选择弹窗（含 10 个上限拦截）
    │   ├── CountryPicker.vue     # 国家选择弹窗（含 10 个上限拦截）
    │   ├── EChart.vue            # ECharts 封装
    │   └── GlobalLoading.vue     # 全局顶部加载条
    ├── layouts/
    │   └── MainLayout.vue    # 主布局（导航 + 内容区）
    ├── pinia/
    │   └── wdiStore.js       # WDI 状态仓库（条件、结果、页面联动）
    ├── router/
    │   └── index.js          # 路由配置（history 模式，动态页面标题）
    ├── utils/
    │   ├── request.js        # axios 封装（/api 前缀、Result 解包、统一错误提示、全局 loading）
    │   └── tools.js          # 通用工具函数
    └── views/
        ├── home/index.vue    # 首页（统计概览 + 热门主题）
        └── wdi/
            ├── topic.vue     # 指标浏览（主题树 + 指标详情）
            └── query.vue     # 数据查询（长表/宽表透视/图表三视图 + 脚注 + CSV 导出 + 查询方案）
```

## 页面说明

| 路由 | 页面 | 功能 |
| --- | --- | --- |
| `/home` | 首页 | WDI 统计概览（指标数 / 国家数 / 年份跨度 / 记录总数）、热门主题入口 |
| `/wdi/topic` | 指标浏览 | Topic 主题树导航、指标分页检索、指标详情弹窗 |
| `/wdi/query` | 数据查询 | 三步构建条件（指标 → 国家 → 年份），结果支持长表（TABLE_LONG）/ 宽表透视（TABLE_WIDE）/ 图表三种视图，指标时间覆盖展示、数据脚注、CSV 导出（含全量脚注）、查询方案保存 |

## 请求约定

- axios `baseURL` 统一为 `/api`，默认超时 30s（面板查询接口单独放宽到 60s）
- 响应拦截器统一解包后端结构：兼容 `{ code, message, data }` 与 jeecg `{ code, message, result }`，`code === 200` 视为成功并直接返回业务数据
- 业务错误 / HTTP 错误统一 `message.error` 提示；请求配置传 `silent: true` 可关闭全局 loading

## 构建优化

- `echarts`、`ant-design-vue` 通过 `manualChunks` 单独分包，提升首屏加载性能
- `chunkSizeWarningLimit` 调整为 1500，避免大依赖误报
