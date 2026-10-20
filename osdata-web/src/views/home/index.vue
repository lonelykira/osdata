<script setup>
/**
 * 平台主页面（总门户）
 * 介绍 WDI 与 WEO 两大权威数据集，点击进入各自的数据展示页面
 */
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getStatisticsSummary as getWdiStatistics } from "@/api/wdi";
import { getStatisticsSummary as getWeoStatistics } from "@/api/weo";

const router = useRouter();

// ===== 数据集统计（实时调取两个后端统计概览接口） =====
const wdiStats = ref({
  indicatorCount: "-",
  countryCount: "-",
  yearSpan: "-",
  dataRowCount: "-",
});
const weoStats = ref({
  indicatorCount: "-",
  countryCount: "-",
  yearSpan: "-",
  dataRowCount: "-",
});
const loading = ref(true);

// 数字格式化（千分位），空值显示 '-'
function formatNumber(value) {
  return value === null || value === undefined
    ? "-"
    : Number(value).toLocaleString("zh-CN");
}

// 通用统计映射：WDI 字段 seriesCount，WEO 字段 indicatorCount，其余一致
function mapStats(s, countField) {
  return {
    indicatorCount: formatNumber(s[countField]),
    countryCount: formatNumber(s.countryCount),
    yearSpan: s.minYear && s.maxYear ? `${s.minYear} - ${s.maxYear}` : "-",
    dataRowCount: formatNumber(s.dataCount),
  };
}

// 页面加载时并行获取两个数据集的统计概览（allSettled：任一失败不影响另一个渲染）
onMounted(async () => {
  const [wdiSettled, weoSettled] = await Promise.allSettled([
    getWdiStatistics(),
    getWeoStatistics(),
  ]);
  if (wdiSettled.status === "fulfilled" && wdiSettled.value) {
    wdiStats.value = mapStats(wdiSettled.value, "seriesCount");
  }
  if (weoSettled.status === "fulfilled" && weoSettled.value) {
    weoStats.value = mapStats(weoSettled.value, "indicatorCount");
  }
  loading.value = false;
});

// ===== 数据集介绍配置 =====
const datasets = [
  {
    key: "wdi",
    abbr: "WDI",
    name: "世界发展指标",
    enName: "World Development Indicators",
    source: "世界银行",
    icon: "🌍",
    desc: "汇聚全球 200+ 个国家和地区的经济社会发展指标，覆盖贫困与收入、人口、教育、卫生健康、环境、能源、基础设施等 6 大主题，是国际公认的发展研究权威数据库。",
    browsePath: "/wdi/topic",
    queryPath: "/wdi/query",
  },
  {
    key: "weo",
    abbr: "WEO",
    name: "世界经济展望",
    enName: "World Economic Outlook",
    source: "国际货币基金组织（IMF）",
    icon: "📈",
    desc: "汇聚全球 190+ 个国家和经济体的宏观经济指标，覆盖国民账户、价格指数、财政收支、对外贸易与金融等主题，同时提供单个国家与聚合经济体两种口径，适合宏观分析与比较研究。",
    browsePath: "/weo/topic",
    queryPath: "/weo/query",
  },
];

// 点击数据集卡片：进入对应数据集的指标浏览页
function goDataset(ds) {
  router.push(ds.browsePath);
}
</script>

<template>
  <div class="home-page">
    <!-- ===== 顶部 Hero 区域 ===== -->
    <section class="hero">
      <div class="hero-inner">
        <h1 class="hero-title">全球宏观经济与发展数据平台</h1>
        <p class="hero-desc">
          汇聚世界银行 WDI 与国际货币基金组织 WEO 两大权威数据集，
          支持指标在线浏览、自由查询、可视化与数据导出。
        </p>
        <div class="hero-actions">
          <a-button
            size="large"
            class="hero-btn"
            @click="router.push('/wdi/query')"
          >
            WDI 数据查询
          </a-button>
          <a-button
            size="large"
            class="hero-btn"
            @click="router.push('/weo/query')"
          >
            WEO 数据查询
          </a-button>
        </div>
      </div>
    </section>

    <!-- ===== 数据集介绍 ===== -->
    <div class="page-container">
      <section class="dataset-section">
        <h2 class="section-title">数据集详情</h2>
        <a-spin :spinning="loading">
          <div class="dataset-grid">
            <div
              v-for="ds in datasets"
              :key="ds.key"
              class="dataset-card"
              @click="goDataset(ds)"
            >
              <div class="dataset-head">
                <span class="dataset-icon">{{ ds.icon }}</span>
                <div class="dataset-name">
                  <div class="dataset-title">{{ ds.abbr }} · {{ ds.name }}</div>
                  <div class="dataset-en">{{ ds.enName }}</div>
                </div>
                <a-tag class="dataset-source" color="blue">{{
                  ds.source
                }}</a-tag>
              </div>

              <p class="dataset-desc">{{ ds.desc }}</p>

              <!-- 数据规模统计 -->
              <div class="stat-grid">
                <div class="stat-item">
                  <div class="stat-value">
                    {{
                      ds.key === "wdi"
                        ? wdiStats.indicatorCount
                        : weoStats.indicatorCount
                    }}
                  </div>
                  <div class="stat-label">指标总数</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">
                    {{
                      ds.key === "wdi"
                        ? wdiStats.countryCount
                        : weoStats.countryCount
                    }}
                  </div>
                  <div class="stat-label">
                    {{ ds.key === "wdi" ? "国家 / 地区" : "国家 / 经济体" }}
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">
                    {{
                      ds.key === "wdi" ? wdiStats.yearSpan : weoStats.yearSpan
                    }}
                  </div>
                  <div class="stat-label">年份跨度</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">
                    {{
                      ds.key === "wdi"
                        ? wdiStats.dataRowCount
                        : weoStats.dataRowCount
                    }}
                  </div>
                  <div class="stat-label">数据记录</div>
                </div>
              </div>

              <!-- 功能按钮 -->
              <div class="dataset-actions" @click.stop>
                <a-button type="primary" @click="router.push(ds.browsePath)"
                  >浏览指标</a-button
                >
                <a-button @click="router.push(ds.queryPath)">数据查询</a-button>
              </div>
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
    max-width: 680px;
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

    // 白色实心按钮：在渐变背景上对比清晰醒目
    .hero-btn {
      background: #fff;
      color: $primary-color;
      border: none;
      font-weight: 600;

      &:hover,
      &:focus {
        background: rgba(255, 255, 255, 0.92);
        color: $primary-hover;
      }
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

// ===== 数据集卡片 =====
.dataset-section {
  margin-top: 32px;
}

.dataset-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.dataset-card {
  background: $bg-color-card;
  border-radius: 8px;
  padding: 24px 20px;
  cursor: pointer;
  border: 1px solid $border-color;
  transition: all 0.25s;

  &:hover {
    border-color: $primary-color;
    box-shadow: 0 4px 16px rgba(24, 144, 255, 0.12);
    transform: translateY(-2px);
  }
}

.dataset-head {
  display: flex;
  align-items: center;
  gap: 12px;

  .dataset-icon {
    font-size: 32px;
  }

  .dataset-name {
    flex: 1;
    min-width: 0;

    .dataset-title {
      font-size: 18px;
      font-weight: 600;
      color: $text-color;
    }

    .dataset-en {
      margin-top: 2px;
      font-size: 12px;
      color: $text-color-tertiary;
    }
  }

  .dataset-source {
    flex-shrink: 0;
  }
}

.dataset-desc {
  margin: 14px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: $text-color-tertiary;
}

// 数据规模统计（年份跨度格约1.5倍宽, 数据记录格加宽约20px, 保证各板块字号统一且不折行）
.stat-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: 1fr 1fr 1.33fr 1.17fr;
  gap: 10px;
}

.stat-item {
  background: rgba(24, 144, 255, 0.06);
  border-radius: 8px;
  padding: 12px 8px;
  text-align: center;

  .stat-value {
    font-size: 20px;
    font-weight: 700;
    color: $primary-color;
    white-space: nowrap;
  }

  .stat-label {
    margin-top: 4px;
    font-size: 12px;
    color: $text-color-tertiary;
  }
}

.dataset-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}
</style>
