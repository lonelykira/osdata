<template>
  <div :id="printId">
    <a-card v-bind="chartCardProps">
      <template #extra>
        <div :ignore-print="true">
          <a-button v-if="showPrint" type="primary" ghost @click="onPrint">打印</a-button>
          <a-button v-if="showDetail" type="primary" ghost @click="onGoToDetail">详情</a-button>
        </div>
      </template>
      <!-- 排序显示图表 -->
      <template v-for="(type, index) of chartTypes" :key="index">
        <LineMulti v-if="type === 'line'" v-bind="lineProps" />
        <BarMulti v-if="type === 'bar'" v-bind="barProps" />
        <Pie v-if="type === 'pie'" v-bind="pieProps" />
      </template>
    </a-card>
    <!-- 数据列表 -->
    <a-card v-if="hasTable" v-bind="tableCardProps">
      <a-row>
        <a-col :ignore-print="true">
          <a-button v-bind="exportButtonProps" @click="onExportXls">{{ exportButtonProps.text }}</a-button>
          <a-switch v-bind="pageSwitchProps" v-model:checked="pageSwitch" />
        </a-col>
        <a-col>
          <a-table v-bind="tableProps" />
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script lang="ts">
  import { ChartRenderCommon, useChartRender } from '../../hooks/useChartRender';

  export default {
    name: 'ChartSingleRender',
    ...ChartRenderCommon,
    setup: useChartRender,
  };
</script>

<style lang="less" scoped></style>
