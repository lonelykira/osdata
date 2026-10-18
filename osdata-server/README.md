开源数据服务端


技术架构：
-----------------------------------

#### 后端

- IDE建议： IDEA (必须安装lombok插件 )
- 语言：Java 默认jdk17(jdk21、jdk24)
- 依赖管理：Maven
- 基础框架：Spring Boot 4.1.0
- 微服务框架： Spring Cloud Alibaba 2025.1.0.0
- 持久层框架：MybatisPlus 3.5.16
- 报表工具： JimuReport 2.1.5
- 安全框架：Apache Shiro 3.0.0，Jwt 4.5.0
- 微服务技术栈：Spring Cloud Alibaba、Nacos、Gateway、Sentinel、Skywalking
- 数据库连接池：阿里巴巴Druid 1.2.28
- AI大模型：支持 `ChatGPT` `DeepSeek` `千问`等各种常规模式
- 日志打印：logback
- 缓存：Redis
- 其他：autopoi, fastjson，poi，Swagger-ui，quartz, lombok（简化代码）等。
- 默认提供MySQL5.7+数据库脚本
- [其他数据库，需要自己转](https://my.oschina.net/jeecg/blog/4905722)


#### 前端

- 前端环境要求：Node.js要求`Node 20+` 版本以上、pnpm 要求`9+` 版本以上
  ` ( Vite 不再支持已结束生命周期（EOL）的 Node.js 18。现在需要使用 Node.js 20.19+ 或 22.12+)`

- 依赖管理：node、npm、pnpm
- 前端IDE建议：IDEA、WebStorm、Vscode
- 采用 Vue3.0+TypeScript+Vite6+Ant-Design-Vue4等新技术方案，包括二次封装组件、utils、hooks、动态菜单、权限校验、按钮级别权限控制等功能
- 最新技术栈：Vue3.0 + TypeScript + Vite6 + ant-design-vue4 + pinia + echarts + unocss + vxe-table + qiankun + es6




#### 支持库

|  数据库   |  支持   |
| --- | --- |
|   MySQL   |  √   |
|  Oracle11g   |  √   |
|  Sqlserver2017   |  √   |
|   PostgreSQL   |  √   |
|   MariaDB   |  √   |
|   达梦   |  √   |
|   人大金仓   |  √   |
|   TiDB   |  √   |



 
## 微服务解决方案


- 1、服务注册和发现 Nacos √
- 2、统一配置中心 Nacos  √
- 3、路由网关 gateway(三种加载方式) √
- 4、分布式 http feign √
- 5、熔断降级限流 Sentinel √
- 6、分布式文件 Minio、阿里OSS √ 
- 7、统一权限控制 JWT + Shiro √
- 8、服务监控 SpringBootAdmin√
- 9、链路跟踪 Skywalking   [参考文档](https://help.jeecg.com/java/springcloud/super/skywarking)
- 10、消息中间件 RabbitMQ  √
- 11、分布式任务 xxl-job  √ 
- 12、分布式事务 Seata
- 13、轻量分布式日志 Loki+grafana套件
- 14、支持 docker-compose、k8s、jenkins
- 15、CAS 单点登录   √
- 16、路由限流   √



后台目录结构
-----------------------------------
```
项目结构
├─jeecg-boot-parent（父POM： 项目依赖、modules组织）
│  ├─jeecg-boot-base-core（共通模块： 工具类、config、权限、查询过滤器、注解等）
│  ├─jeecg-module-demo    示例代码
│  ├─jeecg-module-system  System系统管理目录
│  │  ├─jeecg-system-biz    System系统管理权限等功能
│  │  ├─jeecg-system-start  System单体启动项目(8080）
│  │  ├─jeecg-system-api    System系统管理模块对外api
│  │  │  ├─jeecg-system-cloud-api   System模块对外提供的微服务接口
│  │  │  ├─jeecg-system-local-api   System模块对外提供的单体接口
│  ├─jeecg-server-cloud           --微服务模块
     ├─jeecg-cloud-gateway       --微服务网关模块(9999)
     ├─jeecg-cloud-nacos       --Nacos服务模块(服务:8848, 控制台:18080)
     ├─jeecg-system-cloud-start  --System微服务启动项目(7001)
     ├─jeecg-demo-cloud-start    --Demo微服务启动项目(7002)
     ├─jeecg-visual
        ├─jeecg-cloud-monitor       --微服务监控模块 (9111)
        ├─jeecg-cloud-xxljob        --微服务xxljob定时任务服务端 (9080)
        ├─jeecg-cloud-sentinel     --sentinel服务端 (9000)
        ├─jeecg-cloud-test           -- 微服务测试示例（各种例子）
           ├─jeecg-cloud-test-more         -- 微服务测试示例（feign、熔断降级、xxljob、分布式锁）
           ├─jeecg-cloud-test-rabbitmq     -- 微服务测试示例（rabbitmq）
           ├─jeecg-cloud-test-seata          -- 微服务测试示例（seata分布式事务）
           ├─jeecg-cloud-test-shardingsphere    -- 微服务测试示例（分库分表）
```




#### 微服务架构图
![微服务架构图](https://jeecgos.oss-cn-beijing.aliyuncs.com/files/jeecgboot_springcloud2022.png "在这里输入图片标题")





