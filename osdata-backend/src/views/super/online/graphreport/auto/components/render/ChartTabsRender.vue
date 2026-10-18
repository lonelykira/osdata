<template>
  <div :id="printId">
    <a-card v-bind="chartCardProps">
      <template #extra>
        <div :ignore-print="true">
          <a-button v-if="showPrint" type="primary" ghost @click="onPrint">打印</a-button>
          <a-button v-if="showDetail" type="primary" ghost @click="onGoToDetail">详情</a-button>
        </div>
      </template>
      <div>
        <a-radio-group buttonStyle="solid" v-model:value="activeKey" :ignore-print="true">
          <!-- 排序显示图标 -->
          <template v-for="(type, index) of chartTypes">
            <template v-if="type === 'line'">
              <a-radio-button v-if="hasLine" value="line" :key="index">曲线图</a-radio-button>
            </template>
            <template v-if="type === 'bar'">
              <a-radio-button v-if="hasBar" value="bar" :key="index">柱状图</a-radio-button>
            </template>
            <template v-if="type === 'pie'">
              <a-radio-button v-if="hasPie" value="pie" :key="index">饼图</a-radio-button>
            </template>
          </template>
        </a-radio-group>
        <a-tabs v-model:activeKey="activeKey" size="small" :tabBarStyle="{ display: 'none' }">
          <a-tab-pane v-if="hasLine" tab="曲线图" key="line">
            <LineMulti v-bind="lineProps" />
          </a-tab-pane>
          <a-tab-pane v-if="hasBar" tab="柱状图" key="bar">
            <BarMulti v-bind="barProps" />
          </a-tab-pane>
          <a-tab-pane v-if="hasPie" tab="饼图" key="pie">
            <Pie v-bind="pieProps" />
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-card>
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
    name: 'ChartTabsRender',
    ...ChartRenderCommon,
    setup: useChartRender,
  };
</script>

<style lang="less" scoped></style>
