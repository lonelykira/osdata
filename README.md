# osdata 项目说明

本仓库包含三个子项目，共同构成一套基于 **世界银行 WDI（World Development Indicators，世界发展指标202607）、全球经济数据集 WEO (World Economic Outlook Database202610)** 数据的查询与管理平台：

```
osdata
├── osdata-server    # Java 后端服务（JeecgBoot 单体，提供全部接口与数据能力）
├── osdata-backend   # 管理后台前端（JeecgBoot Vue3，面向管理员）
└── osdata-web       # 数据查询门户前端（独立轻量站点，面向普通访客）
```

三个项目的关系：**两个前端共用一个后端**。`osdata-server` 是唯一的后端服务；`osdata-backend` 是配套的管理后台界面（登录后进行数据维护）；`osdata-web` 是面向公众的免登录查询门户，只调用后端开放接口。

---

## 1. osdata-server —— 后端服务

基于 **JeecgBoot 3.9.5** 低代码平台构建的 Java 后端单体服务。

- **技术栈**：Spring Boot（parent 4.1.0）、Java 17、MyBatis-Plus、Shiro + JWT、Redis、Druid、Knife4j（Swagger 文档）
- **数据库**：MySQL（库名 `osdata`）+ Redis
- **服务端口**：`10060`，上下文路径 `/osdata-server`
- **模块结构**：
  - `jeecg-boot-base-core` —— 平台核心基础模块（通用能力、工具、公共配置）
  - `jeecg-module-system` —— 系统模块（用户/权限/菜单等），其中 `jeecg-system-start` 为启动模块，主类 `org.jeecg.JeecgSystemApplication`
  - `jeecg-boot-module` —— 业务扩展模块（`jeecg-module-demo` 示例模块、`jeecg-boot-module-airag` AI/RAG 模块）
- **WDI 业务模块**（位于 `jeecg-module-system/jeecg-system-start` 下）：
  - `org.jeecg.modules.wdi` —— WDI 数据管理接口（指标系列、国家、国家系列映射、脚注、面板数据的增删改查与 Excel 导入导出），供管理后台使用
  - `org.jeecg.modules.api.controller.WdiApiController` —— 对外开放查询接口，统一前缀 `/webapi/wdi/`，已在 Shiro 中放行、无需登录，供 `osdata-web` 门户调用（指标/国家查询、数据查询、统计汇总、导出等）
- **启动方式**：运行 `jeecg-module-system/jeecg-system-start` 中的 `JeecgSystemApplication`（需先准备好 MySQL `osdata` 库与 Redis，配置见 `application-dev.yml`） 解压项目/db下的wdi_data202607.rar导入 详情查看/db目录下的README.MD

## 2. osdata-backend —— 管理后台前端

> 注意：目录名为 backend，实际是**前端工程**，即 JeecgBoot 官方 Vue3 版管理界面（jeecgboot-vue3 3.9.5）。

- **技术栈**：Vue 3 + Vite + Ant Design Vue + Pinia + vxe-table + ECharts
- **面向用户**：管理员/数据维护人员，需登录后使用
- **核心功能**：
  - JeecgBoot 平台自带的系统管理能力（用户、角色、菜单、日志等）
  - WDI 数据管理页面（`src/views/wdi/`）：世行 WDI 面板数据长表等数据表的管理，支持增删改查、Excel 导入导出，调用后端 `/wdi/wdiData/*` 等接口
- **开发端口**：`3100`，开发代理将 `/osdata-server` 转发至 `http://127.0.0.1:10060/osdata-server`
- **启动方式**：

```bash
cd osdata-backend
pnpm install
pnpm dev
```

## 3. osdata-web —— 数据查询门户前端（支持WDI、WEO数据查询）

独立的轻量级数据查询平台，**面向普通访客，无需登录**。

- **技术栈**：Vue 3 + Vite + Ant Design Vue + Pinia + ECharts（无平台依赖，代码精简）
- **核心功能** 查询指标、查询数据：
  - 三步查询流程：选择指标 → 选择国家/年份区间 → 执行查询
  - 结果支持三种视图：**长表**（TABLE_LONG，默认）、**宽表透视**（TABLE_WIDE）、**图表**（折线/柱状），切换视图优先复用已加载数据
  - 支持按当前视图导出 CSV（脚注数据全量附带）
- **接口**：只调用后端开放接口 `/webapi/wdi/*`（指标、国家、数据查询等）
- **开发端口**：`5173`，开发代理转发至 `http://localhost:10060/osdata-server`
- **启动方式**：

```bash
cd osdata-web
npm install
npm run dev
```

---

## 快速开始

1. 准备环境：JDK 17、Maven、Node.js、MySQL（`osdata` 库）、Redis
2. 启动后端：`osdata-server` → 运行 `JeecgSystemApplication`（端口 10060）
3. 启动管理后台：`osdata-backend` → `pnpm dev`（端口 3100，登录后维护 WDI 数据）
4. 启动查询门户：`osdata-web` → `npm run dev`（端口 5173，访客直接查询）


VX：molingcool
有疑问可解答
