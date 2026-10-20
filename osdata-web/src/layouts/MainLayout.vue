<script setup>
/**
 * 网站公共布局
 * 顶部：简单导航栏（网站标题 + 文字链接）
 * 中间：Content 内容区（路由视图）
 * 底部：网站说明 footer
 * 注意：面向普通访客，无侧边管理菜单、无登录
 */
import { computed } from "vue";
import { useRoute } from "vue-router";
import GlobalLoading from "@/components/GlobalLoading.vue";

const route = useRoute();

// 顶部导航链接配置
const navItems = [
  { key: "home", label: "首页", path: "/home" },
  { key: "topic", label: "WDI指标浏览", path: "/wdi/topic" },
  { key: "weoTopic", label: "WEO指标浏览", path: "/weo/topic" },
];

// 当前激活的导航项
const activeKey = computed(() => {
  const path = route.path;
  if (path.startsWith("/wdi/topic")) return "topic";
  if (path.startsWith("/wdi/query")) return "query";
  if (path.startsWith("/wdi/scheme")) return "scheme";
  if (path.startsWith("/wdi/export")) return "export";
  if (path.startsWith("/weo/topic")) return "weoTopic";
  if (path.startsWith("/weo/query")) return "weoQuery";
  return "home";
});
</script>

<template>
  <div class="main-layout">
    <!-- 全局顶部加载条 -->
    <GlobalLoading />

    <!-- ===== 顶部导航栏 ===== -->
    <header class="site-header">
      <div class="header-inner">
        <!-- 左侧：LOGO + 网站标题 -->
        <router-link to="/home" class="site-logo">
          <div class="logo-icon">W</div>
          <div class="logo-text">
            <span class="logo-title">开源数据平台</span>
            <span class="logo-sub">Open-source Data Platform</span>
          </div>
        </router-link>

        <!-- 中部/右侧：文字链接导航 -->
        <nav class="site-nav">
          <router-link
            v-for="item in navItems"
            :key="item.key"
            :to="item.path"
            class="nav-link"
            :class="{ active: activeKey === item.key }"
          >
            {{ item.label }}
          </router-link>
        </nav>
      </div>
    </header>

    <!-- ===== 内容区 ===== -->
    <main class="site-content">
      <router-view />
    </main>

    <!-- ===== 底部 footer ===== -->
    <footer class="site-footer">
      <div class="footer-inner">
        <p class="footer-text">开源数据平台</p>
        <p class="footer-text sub">
          本平台面向公众免费开放，仅供学习研究使用 · © 2026 开源数据平台
        </p>
      </div>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

// ===== 顶部导航 =====
.site-header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: $header-height;
  background: #fff;
  border-bottom: 1px solid $border-color;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.06);

  .header-inner {
    max-width: $content-width;
    height: 100%;
    margin: 0 auto;
    padding: 0 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}

// LOGO + 标题
.site-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;

  .logo-icon {
    width: 34px;
    height: 34px;
    border-radius: 6px;
    background: $primary-color;
    color: #fff;
    font-size: 20px;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .logo-text {
    display: flex;
    flex-direction: column;
    line-height: 1.2;

    .logo-title {
      font-size: 16px;
      font-weight: 600;
      color: $text-color;
    }

    .logo-sub {
      font-size: 11px;
      color: $text-color-tertiary;
    }
  }
}

// 导航链接
.site-nav {
  display: flex;
  align-items: center;
  gap: 8px;

  .nav-link {
    padding: 6px 14px;
    border-radius: 4px;
    font-size: 14px;
    color: $text-color-secondary;
    text-decoration: none;
    transition: all 0.2s;

    &:hover {
      color: $primary-color;
      background: $primary-bg;
    }

    &.active {
      color: $primary-color;
      background: $primary-bg;
      font-weight: 500;
    }
  }
}

// ===== 内容区 =====
.site-content {
  flex: 1;
  width: 100%;
}

// ===== 底部 =====
.site-footer {
  margin-top: 24px;
  padding: 20px 16px;
  background: #001529;
  color: rgba(255, 255, 255, 0.65);

  .footer-inner {
    max-width: $content-width;
    margin: 0 auto;
    text-align: center;
  }

  .footer-text {
    margin: 4px 0;
    font-size: 13px;

    &.sub {
      font-size: 12px;
      color: rgba(255, 255, 255, 0.4);
    }
  }
}
</style>
