<script setup>
/**
 * WEO 指标主题浏览页
 * 左侧主题列表 + 右侧该主题下的指标列表（支持搜索、分页）
 * 点击指标可查看详情（单位、数量级、年份覆盖等）
 */
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getTopicTree, getIndicatorPage, getIndicatorDetail } from "@/api/weo";

const router = useRouter();
const route = useRoute();

// ===== 主题 =====
const topics = ref([]);
const activeTopicCode = ref(null);
const topicLoading = ref(true);

// ===== 指标列表 =====
const indicators = ref([]);
const indicatorTotal = ref(0);
const indicatorLoading = ref(false);
const keyword = ref("");
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
});

// ===== 指标详情弹窗 =====
const detailVisible = ref(false);
const detailData = ref(null);
const detailLoading = ref(false);

// 页面加载：拉取主题列表并默认选中第一个
onMounted(async () => {
  try {
    const tree = (await getTopicTree()) || [];
    // 主题树映射：一级节点为主题，指标数 = 子指标数量（name 中文 + en 英文）
    topics.value = tree.map((t) => ({
      code: t.topicCode,
      name: t.topicCn || t.topicEn || t.topicCode,
      en: t.topicEn || t.topicCode,
      indicatorCount: (t.children || []).length,
    }));
    if (topics.value.length) {
      // 优先选中路由参数携带的主题，无匹配时默认第一个
      const initial = route.query.topicCode;
      const target = topics.value.find((t) => t.code === initial);
      activeTopicCode.value = target ? target.code : topics.value[0].code;
      fetchIndicators();
    }
  } finally {
    topicLoading.value = false;
  }
});

// 拉取指标列表（分页 + 关键词，GET /webapi/weo/indicator/page）
async function fetchIndicators() {
  indicatorLoading.value = true;
  try {
    const res = await getIndicatorPage({
      topicCode: activeTopicCode.value,
      keyword: keyword.value || undefined,
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize,
    });
    indicators.value = res?.records || [];
    pagination.value.total = res?.total || 0;
  } finally {
    indicatorLoading.value = false;
  }
}

// 切换主题：重置分页并重新加载
function switchTopic(topic) {
  activeTopicCode.value = topic.code;
  pagination.value.current = 1;
  fetchIndicators();
}

// 关键词搜索：防抖（输入触发）
let searchTimer = null;
function onSearchInput() {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    pagination.value.current = 1;
    fetchIndicators();
  }, 300);
}

// 分页变化
function onPageChange(page, pageSize) {
  pagination.value.current = page;
  pagination.value.pageSize = pageSize;
  fetchIndicators();
}

// 查看指标详情（按指标ID查询，GET /webapi/weo/indicator/{indicatorCode}）
async function openDetail(indicator) {
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    detailData.value = await getIndicatorDetail(indicator.indicatorCode);
  } finally {
    detailLoading.value = false;
  }
}

// 跳转到数据查询页（携带该主题）
function goQuery() {
  router.push({
    path: "/weo/query",
    query: {},
  });
}
</script>

<template>
  <div class="page-container topic-page">
    <!-- 页头 -->
    <div class="page-header">
      <h1 class="page-title">指标浏览</h1>
      <p class="page-desc">
        按主题浏览 WEO 全部指标，点击指标查看定义与数据口径
      </p>
    </div>

    <a-spin :spinning="topicLoading">
      <div class="browse-body">
        <!-- 左侧：主题列表 -->
        <aside class="topic-list">
          <div
            v-for="t in topics"
            :key="t.code"
            class="topic-item"
            :class="{ active: activeTopicCode === t.code }"
            @click="switchTopic(t)"
          >
            <div class="topic-info">
              <span class="topic-name">{{ t.name }}</span>
              <span class="topic-en">{{ t.en }}</span>
            </div>
            <span class="topic-badge">{{ t.indicatorCount || "" }}</span>
          </div>
        </aside>

        <!-- 右侧：指标列表 -->
        <div class="indicator-area">
          <!-- 搜索栏 -->
          <div class="search-bar">
            <a-button type="primary" @click="goQuery">数据查询</a-button>
          </div>

          <!-- 指标卡片列表 -->
          <a-spin :spinning="indicatorLoading">
            <div class="indicator-cards">
              <div
                v-for="item in indicators"
                :key="item.id"
                class="indicator-card"
                @click="openDetail(item)"
              >
                <div class="ind-name">
                  {{ item.indicatorNameCn || item.indicatorName }}
                </div>
                <div
                  class="ind-name-en"
                  v-if="
                    item.indicatorNameCn &&
                    item.indicatorName !== item.indicatorNameCn
                  "
                >
                  {{ item.indicatorName }}
                </div>
                <div class="ind-code">指标编码：{{ item.indicatorCode }}</div>
                <div class="ind-unit" v-if="item.unit || item.scale">
                  <span v-if="item.unit">单位：{{ item.unit }}</span>
                  <span v-if="item.unit && item.scale">｜</span>
                  <span v-if="item.scale">数量级：{{ item.scale }}</span>
                </div>
              </div>
              <a-empty
                v-if="!indicatorLoading && !indicators.length"
                description="该主题下暂无指标"
              />
            </div>
          </a-spin>

          <!-- 分页 -->
          <div class="pager">
            <a-pagination
              v-model:current="pagination.current"
              :page-size="pagination.pageSize"
              :total="pagination.total"
              :show-size-changer="true"
              :show-total="(total) => `共 ${total} 个指标`"
              @change="onPageChange"
            />
          </div>
        </div>
      </div>
    </a-spin>

    <!-- 指标详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="指标详情"
      width="1200px"
      :footer="null"
    >
      <a-spin :spinning="detailLoading">
        <div v-if="detailData" class="detail-body">
          <a-descriptions :column="2" bordered size="small" class="detail-desc">
            <a-descriptions-item label="所属主题" :span="2">
              {{
                topics.find((t) => t.code === detailData.topicCode)?.en ||
                detailData.topicCode ||
                "-"
              }}
            </a-descriptions-item>
            <a-descriptions-item label="指标编码">
              <span class="detail-code-text">{{ detailData.indicatorCode }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="单位">
              {{ detailData.unit || "-" }}
            </a-descriptions-item>
            <a-descriptions-item label="数量级">
              {{ detailData.scale || "-" }}
            </a-descriptions-item>
          </a-descriptions>
        </div>
      </a-spin>
    </a-modal>
  </div>
</template>

<style lang="scss" scoped>
.browse-body {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

// 左侧主题列表（固定高度自适应视口，超出滚动）
.topic-list {
  width: 240px;
  flex-shrink: 0;
  background: $bg-color-card;
  border-radius: 8px;
  padding: 8px;
  max-height: max(360px, calc(100vh - 200px));
  overflow-y: auto;

  .topic-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 7px 12px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 13px;
    color: $text-color-secondary;
    transition: all 0.2s;

    // 中文名 + 英文名两行展示, 超长省略
    .topic-info {
      flex: 1;
      min-width: 0;

      .topic-name {
        display: block;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .topic-en {
        display: block;
        margin-top: 1px;
        font-size: 11px;
        color: $text-color-tertiary;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    &:hover {
      color: $primary-color;
      background: $primary-bg;
    }

    &.active {
      color: $primary-color;
      background: $primary-bg;
      font-weight: 500;
    }

    .topic-badge {
      margin-left: 8px;
      font-size: 12px;
      color: $text-color-tertiary;
    }
  }
}

// 右侧指标区域
.indicator-area {
  flex: 1;
  min-width: 0;

  .search-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 12px;

    :deep(.ant-input-affix-wrapper) {
      max-width: 360px;
    }
  }

  // 指标卡片网格
  .indicator-cards {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    min-height: 200px;
  }

  .indicator-card {
    background: $bg-color-card;
    border: 1px solid $border-color;
    border-radius: 8px;
    padding: 12px 14px;
    cursor: pointer;
    transition: all 0.2s;
    // 固定卡片高度，名称/编码/单位超长时截断省略，保证网格整齐
    height: 120px;
    overflow: hidden;

    &:hover {
      border-color: $primary-color;
      box-shadow: 0 2px 10px rgba(24, 144, 255, 0.1);
    }

    .ind-name {
      font-size: 14px;
      font-weight: 500;
      line-height: 1.5;
      // 名称最多两行，超出省略
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      word-break: break-all;
    }

    .ind-code {
      margin-top: 4px;
      font-size: 12px;
      color: $text-color-tertiary;
      font-family: monospace;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    // 指标英文名(中文名存在且与英文不同时才显示), 单行省略
    .ind-name-en {
      margin-top: 2px;
      font-size: 12px;
      color: $text-color-secondary;
      line-height: 1.4;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .ind-unit {
      margin-top: 4px;
      font-size: 12px;
      color: $text-color-secondary;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .pager {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}

// 详情弹窗
.detail-body {
  // 表格内指标编码: 等宽字体 + 浅色底 chip 样式
  .detail-code-text {
    font-family: monospace;
    font-size: 12px;
    color: $primary-color;
    background: rgba(24, 144, 255, 0.08);
    border-radius: 4px;
    padding: 1px 8px;
    word-break: break-all;
  }
}
</style>
