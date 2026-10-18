<template>
  <ChartTabsRender v-if="isTabs" v-bind="templateProps" />
  <ChartSingleRender v-else-if="isSingle" v-bind="templateProps" />
  <ChartDoubleRender v-else-if="isDouble" v-bind="templateProps" />
  <!--  <ChartCombinationRender v-else-if="isCombination" v-bind="templateProps"/>-->
</template>

<script lang="ts">
  import { ref, watch, computed, inject, defineComponent } from 'vue';
  import ErrorTip from '../ErrorTip.vue';
  // tabs 布局模板
  import ChartTabsRender from './ChartTabsRender.vue';
  // 单排布局模板
  import ChartSingleRender from './ChartSingleRender.vue';
  // 双排布局模板
  import ChartDoubleRender from './ChartDoubleRender.vue';
  // 组合布局
  // import ChartCombinationRender from './ChartCombinationRender.vue'

  export default defineComponent({
    name: 'ChartAutoRender',
    components: { ErrorTip, ChartTabsRender, ChartSingleRender, ChartDoubleRender },
    props: {
      // 图表数据
      chartsData: { type: Object, default: null },
      // 是否是组合模式
      isCombination: { type: Boolean, default: false },
    },
    setup(props) {
      const setErrorTip = inject('setErrorTip') as Fn;
      // 图表标题
      const chartTitle = ref(null);
      // 展示模板
      const displayTemplate = ref('tab');
      // 数据源
      const dataSource = ref<Recordable>({});
      const isTabs = computed(() => displayTemplate.value === 'tab');
      const isSingle = computed(() => displayTemplate.value === 'single');
      const isDouble = computed(() => displayTemplate.value === 'double');
      // const isCombination = computed(() => displayTemplate.value === 'combination')
      const templateProps = computed(() => {
        return {
          // 组件模式下不显示 title
          title: props.isCombination ? null : chartTitle.value,
          chartsData: props.chartsData,
          isCombination: props.isCombination,
          onError: (tip) => setErrorTip(tip),
        };
      });

      watch(
        () => props.chartsData,
        () => parseChartsData(props.chartsData),
        { immediate: true }
      );

      // 解析图表数据
      function parseChartsData(data) {
        if (data) {
          chartTitle.value = data.head.name;
          displayTemplate.value = data.head.displayTemplate;
          dataSource.value = data;
          if (!isTabs.value && !isDouble.value && !isSingle.value) {
            setErrorTip('未识别的布局模式');
          }
        }
      }

      return { isTabs, isSingle, isDouble, templateProps };
    },
  });
</script>

<style scoped></style>
