<script setup>
/**
 * 数据查询构建页
 * 三个步骤：选择指标 -> 选择国家/年份 -> 执行查询
 * 查询结果支持 TABLE_LONG长表(默认) / TABLE_WIDE宽表透视 / 图表（折线/柱状）三种视图，
 * 不同视图对应不同数据格式与表头，并可保存为查询方案（本地）或导出 CSV
 */
import { ref, computed, watch, onMounted } from "vue";
import { message } from "ant-design-vue";
import { useWdiStore } from "@/pinia/wdiStore";
import {
  getStatisticsSummary,
  getSeriesYearRange,
  queryPanelData,
} from "@/api/wdi";
import IndicatorPicker from "@/components/IndicatorPicker.vue";
import CountryPicker from "@/components/CountryPicker.vue";
import EChart from "@/components/EChart.vue";

const wdiStore = useWdiStore();

// 查询方案本地存储 key（后端暂无方案接口，先存浏览器本地）
const SCHEME_KEY = "wdi_schemes";

// ===== 弹窗控制 =====
const indicatorPickerOpen = ref(false);
const countryPickerOpen = ref(false);

// ===== 年份 =====
const yearOptions = ref([]); // 可选年份列表
const globalYearSpan = ref(null); // 全局年份跨度 {min, max}（统计概览接口，重置时恢复用）
const queryLoading = ref(false); // 查询加载中
const viewMode = ref("TABLE_LONG"); // 结果视图：TABLE_LONG长表(默认) / TABLE_WIDE宽表透视 / CHART图表
const chartType = ref("line"); // 图表类型：line / bar

// ===== 宽表数据与视图加载标记 =====
// 宽表数据（viewType=TABLE_WIDE 时后端直接返回透视结果: 行=国家×指标, 列=年份）
const wideData = ref(null); // { yearColumns: [升序年份], rows: [{countryCode, countryName, seriesCode, indicatorName, values}] }
// 同一查询条件下已加载过数据的视图标记，避免切换视图重复请求（key=查询时的条件指纹）
const fetchedViews = ref({ key: "", long: false, wide: false });

// ===== 指标时间覆盖与脚注（随查询结果返回）=====
const seriesTimeList = ref([]); // 各指标年份跨度与覆盖说明
const allFootnotes = ref([]); // 全量脚注（导出用）
const footnotes = ref([]); // 脚注列表（展示用，最多前 100 条）
const footnoteTotal = ref(0); // 脚注总数
const MAX_FOOTNOTE_SHOW = 100; // 脚注渲染上限（防止大列表卡顿）

// 页面加载：初始化年份范围 + 消费待加载方案
onMounted(async () => {
  try {
    // 初始年份用统计概览的全局年份跨度（选中指标后由 year-range 接口按交集收敛）
    const summary = await getStatisticsSummary();
    if (summary && summary.minYear && summary.maxYear) {
      globalYearSpan.value = { min: summary.minYear, max: summary.maxYear };
      rebuildYearOptions(summary.minYear, summary.maxYear);
      // 默认最近 10 年
      if (!wdiStore.yearRange.length) {
        wdiStore.setYearRange([
          Math.max(summary.minYear, summary.maxYear - 9),
          summary.maxYear,
        ]);
      }
    }
  } catch (e) {
    // 接口失败时给默认区间
    if (!wdiStore.yearRange.length) {
      const end = new Date().getFullYear() - 1;
      wdiStore.setYearRange([end - 9, end]);
    }
  }

  // 若从方案页跳转过来，应用方案条件
  if (wdiStore.pendingScheme) {
    wdiStore.applyScheme(wdiStore.pendingScheme);
    wdiStore.setPendingScheme(null);
  }
});

// ===== 查询 =====

// 当前条件指纹（判断切换视图时数据是否对应当前条件）
const conditionKey = computed(() =>
  JSON.stringify([
    wdiStore.selectedIndicators,
    wdiStore.selectedCountries,
    wdiStore.yearRange,
  ]),
);

// 按视图类型执行数据查询（POST /webapi/wdi/data/query）
// TABLE_LONG返回longRows长表行；TABLE_WIDE返回wide透视结果(行=国家×指标, 列=年份, 后端Java内存行转列)
async function fetchByViewType(viewType) {
  queryLoading.value = true;
  try {
    const result = await queryPanelData({
      seriesCodeList: wdiStore.selectedIndicators,
      countryCodeList: wdiStore.selectedCountries,
      startYear: Number(wdiStore.yearRange[0]),
      endYear: Number(wdiStore.yearRange[1]),
      includeFootnote: true,
      viewType,
    });
    // 长表行映射为 store 通用行结构（seriesCode -> indicatorCode），长表视图与图表共用
    const rows = (result?.longRows || []).map((r) => ({
      countryCode: r.countryCode,
      countryName: r.countryName,
      indicatorCode: r.seriesCode,
      indicatorName: r.indicatorName,
      year: r.year,
      value: r.value,
    }));
    wdiStore.setResult({ rows, total: result?.returnedRows ?? rows.length });
    // 宽表透视结果直接使用后端返回，前端不再自行透视
    wideData.value = result?.wide || null;
    fetchedViews.value = {
      key: conditionKey.value,
      long: viewType === "TABLE_LONG",
      wide: viewType === "TABLE_WIDE",
    };
    // 指标时间覆盖与脚注（脚注全量留存供导出，页面仅渲染前 100 条防卡顿）
    seriesTimeList.value = result?.seriesTimeList || [];
    allFootnotes.value = result?.footnotes || [];
    footnoteTotal.value = allFootnotes.value.length;
    footnotes.value = allFootnotes.value.slice(0, MAX_FOOTNOTE_SHOW);
    if (result?.truncated) {
      message.warning(
        `结果较大已截断，仅返回前 ${wdiStore.queryResult.total} 行，建议缩小查询范围`,
      );
    } else {
      message.success(`查询完成，共 ${wdiStore.queryResult.total} 条记录`);
    }
  } finally {
    queryLoading.value = false;
  }
}

// 执行数据查询：图表基于长表数据构建，图表视图查询时取 TABLE_LONG
async function handleQuery() {
  if (!wdiStore.isConditionReady) {
    message.warning("请先完整选择指标、国家和年份范围");
    return;
  }
  await fetchByViewType(
    viewMode.value === "CHART" ? "TABLE_LONG" : viewMode.value,
  );
}

// 切换长表/宽表视图：当前条件下的目标视图数据未加载过时补一次请求
// （图表不请求基于已载数据构建；条件变化后未重新查询时不发请求，等待用户点击「开始查询」）
watch(viewMode, async (mode) => {
  if (mode === "CHART") return;
  const target = mode === "TABLE_LONG" ? "long" : "wide";
  if (fetchedViews.value.key !== conditionKey.value) return;
  if (fetchedViews.value[target]) return;
  await fetchByViewType(mode);
});

// 年份选项按 [start, end] 重建（降序展示）
function rebuildYearOptions(start, end) {
  yearOptions.value = [];
  for (let y = end; y >= start; y--) yearOptions.value.push(y);
}

// ===== 指标变化联动年份范围 =====

// 选中指标后调用 /webapi/wdi/series/year-range，用指标年份交集收敛可选年份边界，
// 避免用户选到选中指标无数据的年份（接口要求指标列表非空，空时保持全局范围）
watch(
  () => wdiStore.selectedIndicators,
  async (codes) => {
    if (!codes || !codes.length) return;
    try {
      const range = await getSeriesYearRange(codes);
      if (range && range.minYear && range.maxYear) {
        rebuildYearOptions(range.minYear, range.maxYear);
        // 当前已选年份范围裁剪到交集内
        if (
          Array.isArray(wdiStore.yearRange) &&
          wdiStore.yearRange.length === 2
        ) {
          const [s, e] = wdiStore.yearRange;
          wdiStore.setYearRange([
            Math.min(Math.max(Number(s), range.minYear), range.maxYear),
            Math.min(Math.max(Number(e), range.minYear), range.maxYear),
          ]);
        }
      }
    } catch (e) {
      // 交集查询失败时保持全局年份范围
    }
  },
);

// ===== 结果表格（长表/宽表两种数据格式，表头与行结构不同） =====

// 长表表头（TABLE_LONG）：国家、指标、年份、数值，一行一个数据点
const longColumns = [
  {
    title: "国家 / 地区",
    dataIndex: "countryName",
    key: "countryName",
    width: 160,
    fixed: "left",
  },
  {
    title: "指标",
    dataIndex: "indicatorName",
    key: "indicatorName",
    width: 260,
    fixed: "left",
  },
  { title: "年份", dataIndex: "year", key: "year", width: 90, align: "center" },
  { title: "数值", dataIndex: "value", key: "value", align: "right" },
];

// 宽表表头（TABLE_WIDE）：国家、指标 + 动态年份列（后端 wide.yearColumns 升序）
const wideColumns = computed(() => [
  {
    title: "国家 / 地区",
    dataIndex: "countryName",
    key: "countryName",
    width: 160,
    fixed: "left",
  },
  {
    title: "指标",
    dataIndex: "indicatorName",
    key: "indicatorName",
    width: 260,
    fixed: "left",
  },
  ...(wideData.value?.yearColumns || []).map((y) => ({
    title: `${y}年`,
    dataIndex: `y${y}`,
    key: `y${y}`,
    align: "right",
  })),
]);

// 按当前视图切换表头
const tableColumns = computed(() =>
  viewMode.value === "TABLE_LONG" ? longColumns : wideColumns.value,
);

// 是否有结果（长表行或宽表行任一存在）
const hasResult = computed(
  () =>
    wdiStore.queryResult.rows.length > 0 ||
    (wideData.value?.rows?.length || 0) > 0,
);

// 长表数据行：直接使用后端长表结构（一行 = 国家×指标×年份）
const longRows = computed(() => wdiStore.queryResult.rows);

// 宽表数据行：values 与 yearColumns 按索引对齐，摊平为 y{year} 字段（一行 = 国家×指标）
const wideRows = computed(() => {
  const wide = wideData.value;
  if (!wide?.rows?.length) return [];
  return wide.rows.map((r, idx) => {
    const row = {
      key: `${r.countryCode}_${r.seriesCode}_${idx}`,
      countryCode: r.countryCode,
      countryName: r.countryName,
      indicatorCode: r.seriesCode,
      indicatorName: r.indicatorName,
    };
    (wide.yearColumns || []).forEach((y, i) => {
      row[`y${y}`] = r.values?.[i] ?? null;
    });
    return row;
  });
});

// 按当前视图切换数据行
const tableRows = computed(() =>
  viewMode.value === "TABLE_LONG" ? longRows.value : wideRows.value,
);

// 结果年份（升序）：有长表行时取行内年份，仅有宽表数据时取 wide.yearColumns
const resultYears = computed(() => {
  const set = new Set();
  if (longRows.value.length) {
    longRows.value.forEach((r) => set.add(r.year));
  } else {
    (wideData.value?.yearColumns || []).forEach((y) => set.add(y));
  }
  return [...set].sort((a, b) => a - b);
});

// ===== 结果图表 =====

// y 轴数值缩写显示（万/亿级别缩短，避免轴标签过宽挤压图表）
function formatAxisValue(value) {
  if (value === null || value === undefined || Number.isNaN(value)) return "";
  const abs = Math.abs(value);
  if (abs >= 1e8) return `${(value / 1e8).toFixed(1)}亿`;
  if (abs >= 1e4) return `${(value / 1e4).toFixed(1)}万`;
  return String(value);
}

// 图表 option：x 轴为年份，每个 (国家, 指标) 组合一条线/一组柱
const chartOption = computed(() => {
  const years = resultYears.value;
  if (!years.length) return {};

  // 按组合分组：优先长表行，仅有宽表数据时从宽表构建（同源数据）
  const seriesMap = {};
  if (longRows.value.length) {
    longRows.value.forEach((r) => {
      const key = `${r.countryName} - ${r.indicatorName}`;
      if (!seriesMap[key]) seriesMap[key] = {};
      seriesMap[key][r.year] = r.value;
    });
  } else {
    (wideData.value?.rows || []).forEach((r) => {
      const key = `${r.countryName} - ${r.indicatorName}`;
      seriesMap[key] = {};
      (wideData.value.yearColumns || []).forEach((y, i) => {
        seriesMap[key][y] = r.values?.[i] ?? null;
      });
    });
  }

  const series = Object.entries(seriesMap)
    .slice(0, 20) // 最多展示 20 条序列，避免图表过载
    .map(([name, data]) => ({
      name,
      type: chartType.value,
      smooth: true,
      data: years.map((y) => data[y] ?? null),
    }));

  return {
    tooltip: {
      trigger: "axis",
      // 悬浮提示显示完整千分位数值
      valueFormatter: (v) =>
        v === null || v === undefined ? "-" : Number(v).toLocaleString("zh-CN"),
    },
    legend: { type: "scroll", bottom: 0 },
    // containLabel: 网格自适应包含轴标签，y 轴大数不被截断
    grid: { left: 16, right: 24, top: 40, bottom: 60, containLabel: true },
    xAxis: { type: "category", data: years.map((y) => `${y}年`) },
    yAxis: {
      type: "value",
      scale: true,
      axisLabel: { formatter: formatAxisValue },
    },
    dataZoom: [{ type: "inside" }],
    series,
  };
});

// 脚注折叠面板标题（超出展示上限时提示）
const footnoteHeader = computed(() =>
  footnoteTotal.value > MAX_FOOTNOTE_SHOW
    ? `数据脚注（共 ${footnoteTotal.value} 条，展示前 ${MAX_FOOTNOTE_SHOW} 条）`
    : `数据脚注（${footnoteTotal.value} 条）`,
);

// ===== 保存方案 =====

const schemeModalOpen = ref(false);
const schemeName = ref("");

// 打开保存方案弹窗
function openSchemeModal() {
  if (!wdiStore.isConditionReady) {
    message.warning("查询条件不完整，无法保存方案");
    return;
  }
  schemeName.value = "";
  schemeModalOpen.value = true;
}

// 提交保存方案（后端暂无方案接口，先保存到浏览器本地存储）
function handleSaveScheme() {
  if (!schemeName.value.trim()) {
    message.warning("请输入方案名称");
    return;
  }
  try {
    const schemes = JSON.parse(localStorage.getItem(SCHEME_KEY) || "[]");
    schemes.unshift({
      id: Date.now(),
      name: schemeName.value.trim(),
      condition: wdiStore.getConditionSnapshot(),
      createTime: new Date().toLocaleString("zh-CN"),
    });
    localStorage.setItem(SCHEME_KEY, JSON.stringify(schemes));
    schemeModalOpen.value = false;
    message.success("查询方案保存成功");
  } catch (e) {
    message.error("方案保存失败，请重试");
  }
}

// ===== 导出结果 =====

// 将当前查询结果导出为 CSV 文件（前端生成，无需后端），导出格式跟随当前视图
function handleExport() {
  const lines = [];
  if (viewMode.value === "TABLE_LONG") {
    // 长表格式：国家、指标、年份、数值
    if (!longRows.value.length) {
      message.warning("暂无查询结果，请先执行查询");
      return;
    }
    lines.push(["国家 / 地区", "指标", "年份", "数值"]);
    longRows.value.forEach((r) => {
      lines.push([
        r.countryName,
        `${r.indicatorName}(${r.indicatorCode})`,
        r.year ?? "",
        r.value ?? "",
      ]);
    });
  } else {
    // 宽表透视格式：国家、指标 + 动态年份列
    // （图表视图仅有长表数据时前端透视；仅有宽表数据时直接使用 wide 行）
    const years = resultYears.value;
    let dataRows = wideRows.value;
    if (!dataRows.length && longRows.value.length) {
      const rowMap = {};
      longRows.value.forEach((r) => {
        const key = `${r.countryCode}_${r.indicatorCode}`;
        if (!rowMap[key]) {
          rowMap[key] = {
            countryName: r.countryName,
            label: `${r.indicatorName}(${r.indicatorCode})`,
          };
        }
        rowMap[key][`y${r.year}`] = r.value;
      });
      dataRows = Object.values(rowMap);
    }
    if (!dataRows.length) {
      message.warning("暂无查询结果，请先执行查询");
      return;
    }
    lines.push(["国家 / 地区", "指标", ...years.map((y) => `${y}年`)]);
    dataRows.forEach((row) => {
      lines.push([
        row.countryName,
        row.label ?? `${row.indicatorName}(${row.indicatorCode})`,
        ...years.map((y) => row[`y${y}`] ?? ""),
      ]);
    });
  }
  // 追加脚注区块（全量脚注随查询结果一并导出）
  if (allFootnotes.value.length) {
    lines.push([]);
    lines.push([`数据脚注（共 ${allFootnotes.value.length} 条）`]);
    lines.push(["国家 / 地区", "指标", "年份", "脚注内容"]);
    allFootnotes.value.forEach((f) => {
      lines.push([
        f.countryName || "",
        f.indicatorName || "",
        f.year ?? "",
        f.footnoteTextCn || f.footnoteText || "",
      ]);
    });
  }
  const csv =
    "\uFEFF" +
    lines
      .map((l) =>
        l.map((cell) => `"${String(cell).replace(/"/g, '""')}"`).join(","),
      )
      .join("\r\n");
  const url = URL.createObjectURL(
    new Blob([csv], { type: "text/csv;charset=utf-8" }),
  );
  const a = document.createElement("a");
  a.href = url;
  a.download = `WDI查询结果_${new Date().toISOString().slice(0, 10)}.csv`;
  a.click();
  URL.revokeObjectURL(url);
  message.success("查询结果已导出为 CSV 文件");
}

// 重置全部条件（年份恢复为全局跨度默认值）
function handleReset() {
  wdiStore.resetAll();
  wideData.value = null;
  fetchedViews.value = { key: "", long: false, wide: false };
  if (globalYearSpan.value) {
    rebuildYearOptions(globalYearSpan.value.min, globalYearSpan.value.max);
    wdiStore.setYearRange([
      Math.max(globalYearSpan.value.min, globalYearSpan.value.max - 9),
      globalYearSpan.value.max,
    ]);
  }
  message.info("已重置查询条件");
}
</script>

<template>
  <div class="page-container query-page">
    <!-- 页头 -->
    <div class="page-header">
      <h1 class="page-title">数据查询</h1>
      <p class="page-desc">
        选择指标、国家与年份，构建属于你的世界发展数据视图
      </p>
    </div>

    <!-- ===== 条件构建区 ===== -->
    <div class="card-box condition-panel">
      <a-row :gutter="16">
        <!-- 指标选择 -->
        <a-col :span="8">
          <div class="cond-block">
            <div class="cond-label">
              1. 选择指标
              <a-button
                type="link"
                size="small"
                @click="indicatorPickerOpen = true"
                >添加</a-button
              >
            </div>
            <div class="cond-tags">
              <a-tag
                v-for="item in wdiStore.selectedIndicatorObjects"
                :key="item.code"
                color="blue"
              >
                {{ item.name }}
              </a-tag>
              <span
                v-if="!wdiStore.selectedIndicators.length"
                class="cond-empty"
                >尚未选择</span
              >
            </div>
          </div>
        </a-col>

        <!-- 国家选择 -->
        <a-col :span="8">
          <div class="cond-block">
            <div class="cond-label">
              2. 选择国家
              <a-button
                type="link"
                size="small"
                @click="countryPickerOpen = true"
                >添加</a-button
              >
            </div>
            <div class="cond-tags">
              <a-tag
                v-for="item in wdiStore.selectedCountryObjects"
                :key="item.code"
                color="cyan"
              >
                {{ item.name }}
              </a-tag>
              <span v-if="!wdiStore.selectedCountries.length" class="cond-empty"
                >尚未选择</span
              >
            </div>
          </div>
        </a-col>

        <!-- 年份选择 -->
        <a-col :span="8">
          <div class="cond-block">
            <div class="cond-label">3. 选择年份范围</div>
            <div class="year-select">
              <a-select
                v-model:value="wdiStore.yearRange[0]"
                :options="yearOptions.map((y) => ({ value: y, label: y }))"
                placeholder="起始年份"
                show-search
              />
              <span class="year-sep">至</span>
              <a-select
                v-model:value="wdiStore.yearRange[1]"
                :options="yearOptions.map((y) => ({ value: y, label: y }))"
                placeholder="结束年份"
                show-search
              />
            </div>
          </div>
        </a-col>
      </a-row>

      <!-- 操作按钮行 -->
      <div class="action-bar">
        <div class="left-info">
          <span class="summary">{{ wdiStore.conditionSummary }}</span>
        </div>
        <div class="right-btns">
          <a-button @click="handleReset">重置</a-button>
          <a-button type="primary" :loading="queryLoading" @click="handleQuery"
            >开始查询</a-button
          >
        </div>
      </div>
    </div>

    <!-- ===== 结果区 ===== -->
    <div class="card-box result-panel">
      <a-empty
        v-if="!hasResult && !queryLoading"
        description="暂无查询结果，请在上方配置条件后点击「开始查询」"
      />

      <template v-else>
        <!-- 结果工具栏：长表/宽表透视/图表三种视图，对应后端 TABLE_LONG/TABLE_WIDE/CHART -->
        <div class="result-toolbar">
          <a-radio-group v-model:value="viewMode" button-style="solid">
            <a-radio-button value="TABLE_LONG">长表</a-radio-button>
            <a-radio-button value="TABLE_WIDE">宽表透视</a-radio-button>
            <a-radio-button value="CHART">图表</a-radio-button>
          </a-radio-group>

          <template v-if="viewMode === 'CHART'">
            <a-radio-group v-model:value="chartType">
              <a-radio value="line">折线图</a-radio>
              <a-radio value="bar">柱状图</a-radio>
            </a-radio-group>
          </template>

          <div class="toolbar-right">
            <span class="result-count"
              >共 {{ wdiStore.queryResult.total }} 条记录</span
            >
            <a-button @click="handleExport">导出结果</a-button>
          </div>
        </div>

        <!-- 指标时间覆盖信息 -->
        <div v-if="seriesTimeList.length" class="series-time-bar">
          <span class="st-label">指标时间覆盖：</span>
          <a-tag
            v-for="t in seriesTimeList"
            :key="t.indicatorCode"
            class="st-tag"
            :title="t.descriptionCn || t.description"
          >
            {{ t.indicatorName }}（{{ t.minYear }}–{{ t.maxYear }}）
          </a-tag>
        </div>

        <!-- 表格视图：长表(国家/指标/年份/数值)与宽表透视(国家/指标+动态年份列)两套表头 -->
        <a-table
          v-if="viewMode !== 'CHART'"
          :columns="tableColumns"
          :data-source="tableRows"
          :loading="queryLoading"
          :scroll="{ x: 'max-content', y: 480 }"
          :pagination="{
            pageSize: 20,
            showSizeChanger: true,
            showTotal: (t) => `共 ${t} 行`,
          }"
          size="middle"
          bordered
        />

        <!-- 图表视图 -->
        <EChart
          v-else
          :option="chartOption"
          height="440px"
          :loading="queryLoading"
        />

        <!-- 数据脚注（默认收起，点击展开） -->
        <a-collapse v-if="footnotes.length" ghost class="footnote-box">
          <a-collapse-panel key="fn" :header="footnoteHeader">
            <div v-for="(f, i) in footnotes" :key="i" class="footnote-item">
              <span class="fn-where">
                {{
                  [f.countryName, f.indicatorName, f.year]
                    .filter((v) => v)
                    .join(" · ")
                }}
              </span>
              <span class="fn-text">{{
                f.footnoteTextCn || f.footnoteText
              }}</span>
            </div>
          </a-collapse-panel>
        </a-collapse>
      </template>
    </div>

    <!-- ===== 指标选择弹窗 ===== -->
    <IndicatorPicker
      v-model:open="indicatorPickerOpen"
      :selected="wdiStore.selectedIndicatorObjects"
      @confirm="(list) => wdiStore.setIndicators(list)"
    />

    <!-- ===== 国家选择弹窗 ===== -->
    <CountryPicker
      v-model:open="countryPickerOpen"
      :selected="wdiStore.selectedCountryObjects"
      @confirm="(list) => wdiStore.setCountries(list)"
    />

    <!-- ===== 保存方案弹窗 ===== -->
    <a-modal
      v-model:open="schemeModalOpen"
      title="保存查询方案"
      width="420px"
      @ok="handleSaveScheme"
    >
      <a-form layout="vertical">
        <a-form-item label="方案名称" required>
          <a-input
            v-model:value="schemeName"
            placeholder="例如：金砖五国 GDP 对比（2010-2023）"
            :maxlength="50"
            show-count
          />
        </a-form-item>
        <div class="scheme-summary">
          当前条件：{{ wdiStore.conditionSummary }}
        </div>
      </a-form>
    </a-modal>
  </div>
</template>

<style lang="scss" scoped>
// ===== 条件面板 =====
.condition-panel {
  .cond-block {
    .cond-label {
      font-size: 13px;
      font-weight: 600;
      color: $text-color-secondary;
      margin-bottom: 8px;
    }

    .cond-tags {
      min-height: 28px;
      max-height: 48px;
      overflow-y: auto;
      display: flex;
      flex-wrap: wrap;
      gap: 4px;

      // 扁平紧凑标签：清除默认 margin-bottom(行距撑高根因)，压低整体高度
      :deep(.ant-tag) {
        margin: 0;
        padding: 0 6px;
        font-size: 12px;
        line-height: 18px;
        border-radius: 2px;
      }

      .cond-empty {
        font-size: 12px;
        color: $text-color-tertiary;
      }
    }

    .year-select {
      display: flex;
      align-items: center;
      gap: 8px;

      .year-sep {
        color: $text-color-tertiary;
      }

      :deep(.ant-select) {
        flex: 1;
        min-width: 0;
      }
    }
  }

  // 操作按钮行
  .action-bar {
    margin-top: 14px;
    padding-top: 12px;
    border-top: 1px dashed $border-color;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .summary {
      font-size: 12px;
      color: $text-color-tertiary;
    }

    .right-btns {
      display: flex;
      gap: 8px;
    }
  }
}

// ===== 结果面板 =====
.result-panel {
  margin-top: 16px;

  // 指标时间覆盖行
  .series-time-bar {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 4px;
    margin-bottom: 12px;

    .st-label {
      font-size: 12px;
      color: $text-color-tertiary;
    }

    // 扁平紧凑标签，悬停 title 查看覆盖说明
    .st-tag {
      margin: 0;
      padding: 0 6px;
      font-size: 12px;
      line-height: 18px;
      border-radius: 2px;
      color: $text-color-secondary;
      background: $bg-color-page;
      cursor: default;
    }
  }

  // 脚注折叠区
  .footnote-box {
    margin-top: 12px;
    border-top: 1px dashed $border-color;

    .footnote-item {
      font-size: 12px;
      line-height: 1.8;
      color: $text-color-tertiary;

      .fn-where {
        margin-right: 8px;
        color: $text-color-secondary;
        font-weight: 500;
      }
    }
  }

  .result-toolbar {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 14px;

    .toolbar-right {
      margin-left: auto;
      display: flex;
      align-items: center;
      gap: 12px;

      .result-count {
        font-size: 12px;
        color: $text-color-tertiary;
      }
    }
  }
}

// 方案条件摘要
.scheme-summary {
  font-size: 12px;
  color: $text-color-tertiary;
  background: $bg-color-page;
  padding: 8px 10px;
  border-radius: 4px;
}
</style>
