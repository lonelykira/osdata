<template>
  <div :id="printId">
    <a-card v-bind="chartCardProps">
      <template #extra>
        <div :ignore-print="true">
          <a-button v-if="showPrint" type="primary" ghost @click="onPrint">打印</a-button>
          <a-button v-if="showDetail" type="primary" ghost @click="onGoToDetail">详情</a-button>
        </div>
      </template>
      <!-- 展示双列数据并排序 -->
      <a-row v-for="(type, index) of chartTypes" :key="type + index">
        <template v-if="index % 2 === 0" v-for="idx of [index, index + 1]" :key="idx">
          <a-col v-if="idx < chartTypes.length" v-bind="buildSpan(idx)">
            <LineMulti v-if="chartTypes[idx] === 'line'" v-bind="lineProps" />
            <BarMulti v-if="chartTypes[idx] === 'bar'" v-bind="barProps" />
            <Pie v-if="chartTypes[idx] === 'pie'" v-bind="pieProps" />
          </a-col>
        </template>
      </a-row>
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
  import { defineComponent } from 'vue';
  import { ChartRenderCommon, useChartRender } from '../../hooks/useChartRender';

  export default defineComponent({
    name: 'ChartTabsRender',
    ...ChartRenderCommon,
    setup(props, ctx) {
      let setup = useChartRender(props, ctx);

      function buildSpan(idx) {
        let length = setup.chartTypes.value.length;
        // 如果当前项是最后一个且length是奇数，则扩展到全屏
        let span = idx + 1 === length && length % 2 !== 0 ? 24 : 12;
        return { xs: 24, md: span };
      }

      return { ...setup, buildSpan };
    },
  });
</script>

<style lang="less" scoped></style>
