<script setup>
/**
 * 网站首页
 * 展示平台简介、数据规模统计、功能入口卡片
 */
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getStatisticsSummary, getTopicTree } from "@/api/wdi";

const router = useRouter();

// ===== 数据 =====
const stats = ref({
  indicatorCount: "-", // 指标总数
  countryCount: "-", // 国家/地区总数
  yearSpan: "-", // 年份跨度
  dataRowCount: "-", // 数据行数
});
const topics = ref([]); // 热门主题（首页展示前 8 个）
const loading = ref(true);

// 数字格式化（千分位），空值显示 '-'
function formatNumber(value) {
  return value === null || value === undefined
    ? "-"
    : Number(value).toLocaleString("zh-CN");
}

// 页面加载时获取统计与主题
// 两个请求用 allSettled 独立处理：任一失败不影响另一个的渲染（失败时由拦截器统一提示）
onMounted(async () => {
  const [summarySettled, topicSettled] = await Promise.allSettled([
    getStatisticsSummary(),
    getTopicTree(),
  ]);
  // 统计概览：后端字段 seriesCount/countryCount/minYear/maxYear/dataCount
  if (summarySettled.status === "fulfilled" && summarySettled.value) {
    const s = summarySettled.value;
    stats.value = {
      indicatorCount: formatNumber(s.seriesCount),
      countryCount: formatNumber(s.countryCount),
      yearSpan: s.minYear && s.maxYear ? `${s.minYear} - ${s.maxYear}` : "-",
      dataRowCount: formatNumber(s.dataCount),
    };
  }
  // 热门主题：主题树字段 topic/topicCn/children 映射为卡片展示结构，取前 8 个
  if (
    topicSettled.status === "fulfilled" &&
    Array.isArray(topicSettled.value)
  ) {
    topics.value = topicSettled.value.slice(0, 8).map((t) => ({
      code: t.topic,
      name: t.topicCn || t.topic,
      indicatorCount: (t.children || []).length,
    }));
  }
  loading.value = false;
});

// ===== 功能入口卡片 =====
const features = [
  {
    key: "topic",
    title: "指标浏览",
    desc: "按主题分类浏览全部 WDI 指标，了解每个指标的定义与口径",
    icon: "📖",
  },
  {
    key: "query",
    title: "数据查询",
    desc: "自由选择指标、国家与年份，构建专属数据视图并可视化展示",
    icon: "🔍",
  },
];

// 跳转到对应功能页
function goFeature(key) {
  const pathMap = {
    topic: "/wdi/topic",
    query: "/wdi/query",
    scheme: "/wdi/scheme",
    export: "/wdi/export",
  };
  router.push(pathMap[key]);
}

// 点击主题卡片：跳转指标浏览页并携带主题编码，浏览页自动选中该主题
function goTopic(topicCode) {
  router.push({ path: "/wdi/topic", query: { topicCode } });
}
</script>

<template>
  <div class="home-page">
    <!-- ===== 顶部 Hero 区域 ===== -->
    <section class="hero">
      <div class="hero-inner">
        <h1 class="hero-title">探索世界发展数据</h1>
        <p class="hero-desc">
          WDI（World Development Indicators）汇聚全球 200+
          个国家和地区的经济社会发展指标，支持在线浏览、自由查询与数据导出。
        </p>
        <div class="hero-actions">
          <a-button
            type="primary"
            size="large"
            @click="router.push('/wdi/query')"
          >
            立即开始查询
          </a-button>
          <a-button size="large" @click="router.push('/wdi/topic')"
            >浏览指标主题</a-button
          >
        </div>

        <!-- 数据规模统计 -->
        <div class="stat-row">
          <a-spin :spinning="loading">
            <div class="stat-grid">
              <div class="stat-item">
                <div class="stat-value">{{ stats.indicatorCount }}</div>
                <div class="stat-label">指标总数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ stats.countryCount }}</div>
                <div class="stat-label">国家 / 地区</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ stats.yearSpan }}</div>
                <div class="stat-label">年份跨度</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ stats.dataRowCount }}</div>
                <div class="stat-label">数据记录</div>
              </div>
            </div>
          </a-spin>
        </div>
      </div>
    </section>

    <!-- ===== 功能入口 ===== -->
    <div class="page-container">
      <section class="feature-section">
        <h2 class="section-title">平台功能</h2>
        <div class="feature-grid">
          <div
            v-for="f in features"
            :key="f.key"
            class="feature-card"
            @click="goFeature(f.key)"
          >
            <div class="feature-icon">{{ f.icon }}</div>
            <div class="feature-title">{{ f.title }}</div>
            <div class="feature-desc">{{ f.desc }}</div>
            <span class="feature-link">进入 &rarr;</span>
          </div>
        </div>
      </section>

      <!-- ===== 热门主题 ===== -->
      <section class="topic-section">
        <div class="section-head">
          <h2 class="section-title">热门指标主题</h2>
          <a-button type="link" @click="router.push('/wdi/topic')"
            >查看全部 &rarr;</a-button
          >
        </div>
        <a-spin :spinning="loading">
          <div class="topic-grid">
            <div
              v-for="t in topics"
              :key="t.code"
              class="topic-card"
              @click="goTopic(t.code)"
            >
              <div class="topic-name">{{ t.name }}</div>
              <div class="topic-count">{{ t.indicatorCount || 0 }} 个指标</div>
            </div>
          </div>
        </a-spin>
      </section>
    </div>
  </div>
</template>

<style lang="scss" scoped>
// ===== Hero 区域 =====
.hero {
  background: linear-gradient(135deg, $primary-color 0%, $primary-hover 100%);
  color: #fff;
  padding: 56px 16px 48px;

  .hero-inner {
    max-width: $content-width;
    margin: 0 auto;
    text-align: center;
  }

  .hero-title {
    margin: 0;
    font-size: 36px;
    font-weight: 700;
  }

  .hero-desc {
    max-width: 640px;
    margin: 14px auto 0;
    font-size: 15px;
    line-height: 1.7;
    color: rgba(255, 255, 255, 0.9);
  }

  .hero-actions {
    margin-top: 24px;
    display: flex;
    justify-content: center;
    gap: 12px;
  }

  // 统计网格
  .stat-row {
    margin-top: 36px;
  }

  .stat-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
  }

  .stat-item {
    background: rgba(255, 255, 255, 0.12);
    border-radius: 8px;
    padding: 16px 8px;

    .stat-value {
      font-size: 24px;
      font-weight: 700;
    }

    .stat-label {
      margin-top: 4px;
      font-size: 13px;
      color: rgba(255, 255, 255, 0.8);
    }
  }
}

// 区块标题
.section-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: $text-color;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

// ===== 功能卡片 =====
.feature-section {
  margin-top: 32px;
}

.feature-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.feature-card {
  background: $bg-color-card;
  border-radius: 8px;
  padding: 20px 16px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.25s;

  &:hover {
    border-color: $primary-color;
    box-shadow: 0 4px 16px rgba(24, 144, 255, 0.12);
    transform: translateY(-2px);
  }

  .feature-icon {
    font-size: 28px;
  }

  .feature-title {
    margin-top: 10px;
    font-size: 16px;
    font-weight: 600;
  }

  .feature-desc {
    margin-top: 6px;
    font-size: 13px;
    line-height: 1.6;
    color: $text-color-tertiary;
    min-height: 42px;
  }

  .feature-link {
    display: inline-block;
    margin-top: 10px;
    font-size: 13px;
    color: $primary-color;
  }
}

// ===== 主题卡片 =====
.topic-section {
  margin-top: 32px;
}

.topic-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.topic-card {
  background: $bg-color-card;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  border: 1px solid $border-color;
  transition: all 0.25s;

  &:hover {
    border-color: $primary-color;
  }

  .topic-name {
    font-size: 14px;
    font-weight: 500;
  }

  .topic-count {
    margin-top: 6px;
    font-size: 12px;
    color: $text-color-tertiary;
  }
}
</style>
