<template>
  <div class="redis-monitor p-4">
    <!-- 顶部概览卡片 -->
    <a-row :gutter="16" class="overview-row">
      <a-col :sm="12" :md="6" v-for="item in overviewCards" :key="item.label">
        <div class="overview-card" :style="{ borderTopColor: item.color }">
          <div class="overview-card__value" :style="{ color: item.color }">{{ item.value }}</div>
          <div class="overview-card__label">{{ item.label }}</div>
        </div>
      </a-col>
    </a-row>

    <!-- Redis 信息实时监控 -->
    <a-row :gutter="16" class="chart-row">
      <a-col :sm="24" :xl="12">
        <a-card :bordered="false" class="chart-card">
          <div ref="chartRef" style="width: 100%; height: 300px"></div>
        </a-card>
      </a-col>
      <a-col :sm="24" :xl="12">
        <a-card :bordered="false" class="chart-card">
          <div ref="chartRef2" style="width: 100%; height: 300px"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- Redis 详细信息表格 -->
    <a-card :bordered="false" class="table-card" title="Redis 配置详情">
      <BasicTable @register="registerTable" :api="getInfo" :canResize="false"></BasicTable>
    </a-card>
  </div>
</template>
<script lang="ts" name="monitor-redis" setup>
  import { onMounted, ref, reactive, Ref, onUnmounted, computed } from 'vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { getInfo, getRedisInfo, getMetricsHistory } from './redis.api';
  import dayjs from 'dayjs';
  import { columns } from './redis.data';
  import { useECharts } from '/@/hooks/web/useECharts';

  const chartRef = ref<HTMLDivElement | null>(null);
  const chartRef2 = ref<HTMLDivElement | null>(null);
  const { setOptions } = useECharts(chartRef as Ref<HTMLDivElement>);
  const { setOptions: setOptions2 } = useECharts(chartRef2 as Ref<HTMLDivElement>);
  let timer: any = null;

  // 概览数据
  const currentMemory = ref('--');
  const currentKeys = ref('--');
  const currentUptime = ref('--');
  const currentPort = ref('--');

  const overviewCards = computed(() => [
    { label: '已用内存', value: currentMemory.value, color: '#1890ff' },
    { label: 'Key 数量', value: currentKeys.value, color: '#52c41a' },
    { label: '运行时间', value: currentUptime.value, color: '#faad14' },
    { label: '监听端口', value: currentPort.value, color: '#722ed1' },
  ]);

  const memoryOption = reactive({
    title: {
      text: 'Redis 内存实时占用（KB）',
      textStyle: { fontSize: 14, fontWeight: 500, color: '#333' },
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e8e8e8',
      borderWidth: 1,
      textStyle: { color: '#333', fontSize: 12 },
      formatter(params) {
        const p = params[0];
        return `<div style="font-weight:500;margin-bottom:4px">${p.axisValue}</div>
                <span style="color:#1890ff">● 内存</span>：${p.value} KB`;
      },
    },
    grid: { top: 50, right: 20, bottom: 30, left: 60 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: [],
      axisLine: { lineStyle: { color: '#d9d9d9' } },
      axisTick: { show: false },
      axisLabel: { color: '#8c8c8c', fontSize: 11 },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#8c8c8c', fontSize: 11 },
    },
    series: [
      {
        data: [],
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 4,
        showSymbol: false,
        lineStyle: { color: '#1890ff', width: 2 },
        itemStyle: { color: '#1890ff' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(24,144,255,0.25)' },
              { offset: 1, color: 'rgba(24,144,255,0.02)' },
            ],
          },
        },
      },
    ],
  });

  const keyOption = reactive({
    title: {
      text: 'Redis Key 实时数量（个）',
      textStyle: { fontSize: 14, fontWeight: 500, color: '#333' },
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e8e8e8',
      borderWidth: 1,
      textStyle: { color: '#333', fontSize: 12 },
      formatter(params) {
        const p = params[0];
        return `<div style="font-weight:500;margin-bottom:4px">${p.axisValue}</div>
                <span style="color:#52c41a">● Key 数量</span>：${p.value}`;
      },
    },
    grid: { top: 50, right: 20, bottom: 30, left: 60 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: [],
      axisLine: { lineStyle: { color: '#d9d9d9' } },
      axisTick: { show: false },
      axisLabel: { color: '#8c8c8c', fontSize: 11 },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#8c8c8c', fontSize: 11 },
    },
    series: [
      {
        data: [],
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 4,
        showSymbol: false,
        lineStyle: { color: '#52c41a', width: 2 },
        itemStyle: { color: '#52c41a' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(82,196,26,0.25)' },
              { offset: 1, color: 'rgba(82,196,26,0.02)' },
            ],
          },
        },
      },
    ],
  });

  const [registerTable] = useTable({
    columns,
    showIndexColumn: false,
    pagination: false,
    bordered: true,
    canResize: false,
    showTableSetting: false,
  });

  function initCharts() {
    setOptions(memoryOption);
    setOptions2(keyOption);
  }

  /** 开启定时器 */
  function openTimer() {
    loadHistoryData();
    closeTimer();
    timer = setInterval(() => {
      loadData();
    }, 15000);
  }

  /** 关闭定时器 */
  function closeTimer() {
    if (timer) clearInterval(timer);
  }

  /** 加载历史监控数据 */
  function loadHistoryData() {
    getMetricsHistory().then((res) => {
      const dbSizes = res.dbSize;
      const memories = res.memory;
      dbSizes.forEach((dbSize) => {
        keyOption.xAxis.data.push(dayjs(dbSize.create_time).format('HH:mm:ss'));
        keyOption.series[0].data.push(dbSize.dbSize);
      });
      memories.forEach((memoryData) => {
        memoryOption.xAxis.data.push(dayjs(memoryData.create_time).format('HH:mm:ss'));
        memoryOption.series[0].data.push(memoryData.used_memory / 1000);
      });
      // 更新概览卡片
      if (memories.length > 0) {
        const lastMem = memories[memories.length - 1].used_memory / 1000;
        currentMemory.value = lastMem.toFixed(0) + ' KB';
      }
      if (dbSizes.length > 0) {
        currentKeys.value = dbSizes[dbSizes.length - 1].dbSize + '';
      }
      setOptions(memoryOption, false);
      setOptions2(keyOption, false);
    });
    // 加载详细信息获取端口和运行时间
    getInfo().then((res) => {
      const list = res.result || res;
      if (Array.isArray(list)) {
        list.forEach((item) => {
          if (item.key === 'tcp_port') currentPort.value = item.value;
          if (item.key === 'uptime_in_days') currentUptime.value = item.value + ' 天';
        });
      }
    });
  }

  function loadData() {
    getRedisInfo()
      .then((res) => {
        const time = dayjs().format('HH:mm:ss');
        const [{ dbSize: curSize }, memInfo] = res;
        const curMem = memInfo.used_memory / 1000;

        keyOption.xAxis.data.push(time);
        keyOption.series[0].data.push(curSize);
        memoryOption.xAxis.data.push(time);
        memoryOption.series[0].data.push(curMem);

        // 更新概览
        currentMemory.value = curMem.toFixed(0) + ' KB';
        currentKeys.value = curSize + '';

        // 最大长度为80
        if (keyOption.series[0].data.length > 80) {
          keyOption.xAxis.data.splice(0, 1);
          keyOption.series[0].data.splice(0, 1);
          memoryOption.xAxis.data.splice(0, 1);
          memoryOption.series[0].data.splice(0, 1);
        }
        setOptions(memoryOption, false);
        setOptions2(keyOption, false);
      })
      .catch(() => {});
  }

  onMounted(() => {
    initCharts();
    openTimer();
  });

  onUnmounted(() => {
    closeTimer();
  });
</script>
<style lang="less" scoped>
  .redis-monitor {
    .overview-row {
      margin-bottom: 16px;
    }

    .overview-card {
      background: #fff;
      border-radius: 6px;
      padding: 20px 24px;
      border-top: 3px solid #1890ff;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
      transition: box-shadow 0.3s;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
      }

      &__value {
        font-size: 28px;
        font-weight: 600;
        line-height: 1.2;
        margin-bottom: 4px;
      }

      &__label {
        font-size: 13px;
        color: #8c8c8c;
      }
    }

    .chart-row {
      margin-bottom: 16px;
    }

    .chart-card {
      border-radius: 6px;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);

      :deep(.ant-card-body) {
        padding: 16px;
      }
    }

    .table-card {
      border-radius: 6px;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
    }
  }
</style>
