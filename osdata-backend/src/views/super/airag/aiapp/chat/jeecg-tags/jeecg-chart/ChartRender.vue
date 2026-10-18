<template>
  <div class="ai-chat-chart">
    <div v-if="isError" class="ai-chat-chart__error">{{ errorMessage }}</div>
    <div v-else-if="!resolvedType || !hasData" class="ai-chat-chart__error">
      <div v-if="loading"
           style="color: #999; padding: 12px 8px; border: 1px dashed #eee; margin: 8px 0; border-radius: 4px;">
        <span>图表渲染中...</span>
      </div>
      <span v-else>模型返回的图表渲染格式不正确，请优化提示词或重新尝试。</span>
    </div>
    <div v-else class="ai-chat-chart__body">
      <Tabs v-model:activeKey="activeTab" size="small" class="ai-chat-chart__tabs">
        <!-- 主图表 Tab -->
        <TabPane :key="resolvedType" :tab="getChartLabel(resolvedType)">
          <ChartBody :chartType="resolvedType" />
        </TabPane>
        <!-- 可替代图表 Tabs -->
        <TabPane v-for="alt in validAltTypes" :key="alt" :tab="getChartLabel(alt)">
          <ChartBody :chartType="alt" />
        </TabPane>
        <!-- 数据 Tab -->
        <TabPane v-if="hasSql" key="__data__" tab="数据">
          <div class="ai-chat-chart__data-toolbar">
            <a-button size="small" :loading="exportLoading" @click="handleExport">
              <template #icon><DownloadOutlined /></template>
              导出
            </a-button>
          </div>
          <BasicTable
            v-bind="tableBindings"
            :loading="tableLoading"
            size="small"
            class="ai-chat-chart__table"
          />
        </TabPane>
      </Tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ChartType } from './types';
import type { BasicColumn } from '/@/components/Table';
import { computed, defineComponent, h, ref, watch, watchEffect } from 'vue';
import { Tabs, TabPane, message } from 'ant-design-vue';
import { DownloadOutlined } from '@ant-design/icons-vue';
import { BasicTable, useTable } from '/@/components/Table';
import LineMulti from '/@/components/chart/LineMulti.vue';
import BarMulti from '/@/components/chart/BarMulti.vue';
import Pie from '/@/components/chart/Pie.vue';
import Radar from '/@/components/chart/Radar.vue';
import Gauge from '/@/components/chart/Gauge.vue';
import BarAndLine from '/@/components/chart/BarAndLine.vue';
import SingleLine from '/@/components/chart/SingleLine.vue';
import { sqlPageExecute, sqlExportXls } from './ChartRender.api';

const props = defineProps({
  /**
   * 图表配置字符串，示例：
   * {"type":"bar","altTypes":["line","pie"],"data":[...],"sql":"SELECT ...","dbSource":"","columns":{"field1":"标题1"}}
   */
  data: {
    type: String,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

/** 图表类型中文名映射 */
const chartTypeLabels: Record<string, string> = {
  bar: '柱状图',
  line: '折线图',
  pie: '饼图',
  radar: '雷达图',
  gauge: '仪表盘',
  barline: '折柱图',
  multibar: '多列柱状图',
  multiline: '多行折线图',
  area: '面积图',
};

/** 获取图表类型的中文标签 */
function getChartLabel(type: string): string {
  return chartTypeLabels[type] || type;
}

/** 当前激活的 Tab */
const activeTab = ref<string>('');
/** 表格加载状态 */
const tableLoading = ref(false);
/** 导出加载状态 */
const exportLoading = ref(false);
/** 表格数据 */
const tableData = ref<Recordable[]>([]);
/** 表格列定义 */
const tableColumns = ref<BasicColumn[]>([]);
/** 数据总条数 */
const tableTotal = ref(0);
/** 是否已首次加载过数据 */
const dataLoaded = ref(false);

/**
 * 解析失败或类型错误的提示文本。
 */
const errorMessage = ref<string>('');
/**
 * 是否存在阻止渲染的错误。
 */
const isError = ref<boolean>(false);

/**
 * 将字符串解析为配置对象，失败时记录错误信息。
 */
const parsedConfig = computed<Recordable>(() => {
  try {
    errorMessage.value = '';
    isError.value = false;
    return JSON.parse(props.data || '{}');
  } catch (error) {
    errorMessage.value = '图表数据解析错误，无法渲染图表。';
    isError.value = true;
    return {};
  }
});

/**
 * 支持的图表类型集合。
 */
const supportedTypes: ChartType[] = ['bar', 'line', 'pie', 'radar', 'gauge', 'barline', 'multibar', 'multiline', 'area'];

/**
 * 解析得到的图表类型，统一小写后做合法性校验。
 */
const resolvedType = computed<ChartType>(() => {
  const rawType = String((parsedConfig.value as any).type || '').toLowerCase();
  const normalizedType = (rawType || '').replace(/[-_\s]/g, '');
  const typeAliasMap: Record<string, ChartType> = {
    bar: 'bar',
    line: 'line',
    pie: 'pie',
    radar: 'radar',
    gauge: 'gauge',
    barline: 'barline',
    barandline: 'barline',
    linebar: 'barline',
    multiline: 'multiline',
    multibar: 'multibar',
    area: 'area',
    arealine: 'area',
  };
  const mapped = typeAliasMap[normalizedType] || '';
  if (mapped && supportedTypes.includes(mapped)) {
    return mapped as ChartType;
  }
  return '';
});

/** 解析可替代图表类型列表（过滤掉无效类型和主类型重复） */
const validAltTypes = computed<ChartType[]>(() => {
  const altTypes = (parsedConfig.value as any).altTypes;
  if (!Array.isArray(altTypes)) {
    return [];
  }
  const primary = resolvedType.value;
  return altTypes
    .map((t: string) => String(t).toLowerCase() as ChartType)
    .filter((t: ChartType) => t !== primary && supportedTypes.includes(t));
});

/** 初始化 activeTab 为主图表类型 */
watchEffect(() => {
  if (resolvedType.value && !activeTab.value) {
    activeTab.value = resolvedType.value;
  }
});

/**
 * 当类型不被支持时，给出错误提示。
 */
watchEffect(() => {
  if (isError.value) {
    return;
  }
  if (resolvedType.value === '') {
    const typeValue = (parsedConfig.value as any).type;
    if (typeValue) {
      errorMessage.value = '当前仅支持 bar、line、pie、radar、gauge、barline、multibar、multiline、area 类型图表。';
      isError.value = true;
    }
  } else {
    errorMessage.value = '';
    isError.value = false;
  }
});

/**
 * 原始数据列表。
 */
const rawData = computed<unknown>(() => (parsedConfig.value as any).data);

/**
 * 判断是否存在可用的数据数组。
 */
const hasData = computed<boolean>(() => {
  if (resolvedType.value === 'gauge') {
    return Boolean(rawData.value);
  }
  return Array.isArray(rawData.value) && rawData.value.length > 0;
});

/** 是否包含可查询的 SQL */
const hasSql = computed<boolean>(() => {
  const sql = (parsedConfig.value as any).sql;
  return typeof sql === 'string' && sql.trim().length > 0;
});

/** 从配置中提取 SQL */
const chartSql = computed<string>(() => {
  return ((parsedConfig.value as any).sql || '').trim();
});

/** 从配置中提取数据源 */
const chartDbSource = computed<string>(() => {
  return ((parsedConfig.value as any).dbSource || '').trim();
});

/** 从配置中提取列标题映射 */
const chartColumns = computed<Recordable>(() => {
  return (parsedConfig.value as any).columns || {};
});

/**
 * 使用 BasicTable 组件
 */
const [registerTable, { setPagination }] = useTable({
  columns: tableColumns,
  dataSource: tableData,
  pagination: {
    pageSize: 10,
    current: 1,
    total: 0,
  },
  showIndexColumn: false,
  canResize: false,
  bordered: true,
  onChange: handleTableChange,
});

/** 表格绑定属性 */
const tableBindings = computed(() => ({
  onRegister: registerTable,
}));

/**
 * 加载表格数据
 */
async function loadTableData(pageNo = 1, pageSize = 10) {
  if (!chartSql.value) {
    return;
  }
  tableLoading.value = true;
  try {
    const res = await sqlPageExecute({
      sql: chartSql.value,
      dbSource: chartDbSource.value,
      pageNo,
      pageSize,
    });
    if (res.success && res.result) {
      const { records = [], total = 0 } = res.result;
      tableData.value = records;
      tableTotal.value = total;
      // 根据返回数据动态生成列（使用 columns 映射中文标题）
      if (records.length > 0) {
        tableColumns.value = buildColumns(records[0]);
      }
      setPagination({
        current: pageNo,
        pageSize,
        total,
      });
    } else {
      tableData.value = [];
      tableTotal.value = 0;
    }
  } catch (e) {
    console.error('加载图表原始数据失败', e);
    tableData.value = [];
    tableTotal.value = 0;
  } finally {
    tableLoading.value = false;
  }
}

/**
 * 根据数据行的 key 动态生成 BasicTable 列定义，优先使用 columns 映射的中文标题
 */
function buildColumns(row: Recordable): BasicColumn[] {
  const columnMap = chartColumns.value;
  return Object.keys(row).map((key) => ({
    title: columnMap[key] || key,
    dataIndex: key,
    width: 150,
    ellipsis: true,
  }));
}

/**
 * 表格分页变更处理
 */
function handleTableChange(pagination: any) {
  loadTableData(pagination.current, pagination.pageSize);
}

/**
 * 导出全部数据
 */
async function handleExport() {
  if (!chartSql.value) {
    return;
  }
  exportLoading.value = true;
  try {
    const response = await sqlExportXls({
      sql: chartSql.value,
      dbSource: chartDbSource.value,
      columns: chartColumns.value,
    });
    if (!response || !response.data) {
      message.warning('导出失败');
      return;
    }
    const data = response.data;
    // 检查是否为错误响应（JSON 格式）
    if (data.type && data.type.indexOf('json') !== -1) {
      const text = await data.text();
      try {
        const json = JSON.parse(text);
        if (!json.success) {
          message.warning('导出失败：' + (json.message || '未知错误'));
          return;
        }
      } catch (_e) {
        // 非 JSON，继续下载
      }
    }
    const url = window.URL.createObjectURL(new Blob([data], { type: 'application/vnd.ms-excel' }));
    const link = document.createElement('a');
    link.style.display = 'none';
    link.href = url;
    link.setAttribute('download', '数据导出.xls');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (e) {
    console.error('导出失败', e);
    message.error('导出失败');
  } finally {
    exportLoading.value = false;
  }
}

/**
 * 切换到数据 Tab 时首次加载
 */
watch(activeTab, (val) => {
  if (val === '__data__' && !dataLoaded.value && hasSql.value) {
    dataLoaded.value = true;
    loadTableData();
  }
});

// ======================== 图表数据转换 ========================

/**
 * 将原始数据标准化为多序列图表所需的结构。
 */
const multiSeriesData = computed<Recordable[]>(() => {
  if (!Array.isArray(rawData.value)) {
    return [];
  }
  return rawData.value.map((item: Recordable) => buildMultiSeriesItem(item));
});

/**
 * 面积折线与多行折线共享的数据结构，透传 seriesType 控制折线类型。
 */
const areaSeriesData = computed<Recordable[]>(() => {
  if (!Array.isArray(rawData.value)) {
    return [];
  }
  return rawData.value.map((item: Recordable) => {
    return {
      ...buildMultiSeriesItem(item),
      seriesType: item && item.seriesType ? String(item.seriesType) : 'line',
      areaStyle: {},
    };
  });
});

/**
 * 将原始数据标准化为饼图所需的结构。
 */
const pieSeriesData = computed<Recordable[]>(() => {
  if (!Array.isArray(rawData.value)) {
    return [];
  }
  return rawData.value.map((item: Recordable) => {
    return {
      name: resolveName(item),
      value: resolveNumber(item),
    };
  });
});

/**
 * 构建多序列图表需要的标准化数据项。
 */
function buildMultiSeriesItem(item: Recordable): Recordable {
  const seriesName = resolveSeriesName(item);
  const name = resolveName(item);
  const value = resolveNumber(item);
  return {type: seriesName, name, value};
}

/**
 * 解析系列名称，优先使用 series，其次使用 type，最后回退为"数据"。
 */
function resolveSeriesName(item: Recordable): string {
  if (item && item.series !== undefined) {
    return String(item.series);
  }
  if (item && item.type !== undefined) {
    return String(item.type);
  }
  return '数据';
}

/**
 * 解析横轴名称，支持 x 与 name 字段。
 */
function resolveName(item: Recordable): string {
  if (item && item.x !== undefined) {
    return String(item.x);
  }
  if (item && item.name !== undefined) {
    return String(item.name);
  }
  return '';
}

/**
 * 解析数值字段，支持 y 与 value，非数字时回退为 0。
 */
function resolveNumber(item: Recordable): number {
  const rawValue = item ? item.y ?? item.value : null;
  const num = Number(rawValue);
  if (Number.isFinite(num)) {
    return num;
  }
  return 0;
}

/**
 * 解析序列类型，折柱图需要区分 bar / line。
 */
function resolveSeriesType(item: Recordable): string {
  if (item && item.seriesType !== undefined) {
    return String(item.seriesType);
  }
  return 'bar';
}

/**
 * 仪表盘数据，允许数组或对象输入，确保返回 name、value。
 */
const gaugeData = computed(() => {
  const dataSource = rawData.value as any;
  if (Array.isArray(dataSource) && dataSource.length > 0) {
    const item = dataSource[0];
    return { name: resolveName(item) || '仪表盘', value: resolveNumber(item) };
  }
  if (dataSource && typeof dataSource === 'object') {
    return { name: resolveName(dataSource) || '仪表盘', value: resolveNumber(dataSource as Recordable) };
  }
  return { name: '仪表盘', value: 0 };
});

/**
 * 雷达图数据，支持 max 字段定义指标上限。
 */
const radarSeriesData = computed<Recordable[]>(() => {
  if (!Array.isArray(rawData.value)) {
    return [];
  }
  return rawData.value.map((item: Recordable) => {
    return {
      type: resolveSeriesName(item),
      name: resolveName(item),
      value: resolveNumber(item),
      max: item && item.max !== undefined ? Number(item.max) : undefined,
    };
  });
});

/**
 * 折柱图数据，附带 seriesType 用于区分柱/线。
 */
const barLineSeriesData = computed<Recordable[]>(() => {
  if (!Array.isArray(rawData.value)) {
    return [];
  }
  return rawData.value.map((item: Recordable) => {
    return {
      ...buildMultiSeriesItem(item),
      seriesType: resolveSeriesType(item),
    };
  });
});

// ======================== 根据类型获取图表 props ========================

/**
 * 根据图表类型返回对应的渲染属性
 */
function getChartPropsForType(type: ChartType): Recordable {
  switch (type) {
    case 'line':
      return { type: 'line', height: '360px', width: '100%', chartData: multiSeriesData.value };
    case 'bar':
      return { height: '360px', width: '100%', chartData: multiSeriesData.value };
    case 'pie':
      return { height: '360px', width: '100%', chartData: pieSeriesData.value };
    case 'multibar':
      return { height: '360px', width: '100%', chartData: multiSeriesData.value, option: parsedConfig.value.option || {} };
    case 'multiline':
      return { type: 'line', height: '360px', width: '100%', chartData: multiSeriesData.value, option: parsedConfig.value.option || {} };
    case 'barline':
      return { height: '360px', width: '100%', chartData: barLineSeriesData.value, customColor: (parsedConfig.value as any).colors || [], option: parsedConfig.value.option || {} };
    case 'area':
      return { type: 'line', height: '360px', width: '100%', chartData: areaSeriesData.value, option: { ...(parsedConfig.value.option || {}), areaStyle: {} } };
    case 'radar':
      return { height: '420px', width: '100%', chartData: radarSeriesData.value, option: parsedConfig.value.option || {} };
    case 'gauge':
      return { height: '360px', width: '100%', chartData: gaugeData.value, option: parsedConfig.value.option || {}, seriesColor: (parsedConfig.value as any).seriesColor || undefined };
    default:
      return {};
  }
}

/**
 * 根据图表类型返回对应的组件
 */
function getChartComponentForType(type: ChartType) {
  switch (type) {
    case 'line':
      return LineMulti;
    case 'bar':
      return BarMulti;
    case 'pie':
      return Pie;
    case 'multibar':
      return BarMulti;
    case 'multiline':
      return LineMulti;
    case 'barline':
      return BarAndLine;
    case 'area':
      return SingleLine;
    case 'radar':
      return Radar;
    case 'gauge':
      return Gauge;
    default:
      return null;
  }
}

/**
 * 内部图表渲染组件，根据 chartType 动态渲染对应图表
 */
const ChartBody = defineComponent({
  props: {
    chartType: {
      type: String as () => ChartType,
      required: true,
    },
  },
  setup(bodyProps) {
    return () => {
      const comp = getChartComponentForType(bodyProps.chartType as ChartType);
      if (!comp) {
        return null;
      }
      const chartProps = getChartPropsForType(bodyProps.chartType as ChartType);
      return h(comp, chartProps);
    };
  },
});
</script>

<style scoped lang="less">

.ai-chat-chart {
  width: 100%;
  min-width: 420px;
  padding: 12px 0;
}

.ai-chat-chart__body {
  width: 100%;
}

.ai-chat-chart__error {
  color: #ff4d4f;
  font-size: 14px;
  line-height: 20px;
}

.ai-chat-chart__tabs {
  :deep(.ant-tabs-nav) {
    margin-bottom: 8px;
  }
}

.ai-chat-chart__data-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.ai-chat-chart__table {
  :deep(.ant-table-wrapper) {
    padding: 0;
  }
}

</style>
