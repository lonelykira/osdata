<script setup>
/**
 * ECharts 通用图表组件（按需引入 echarts/core）
 * 支持：折线图、柱状图、饼图等已注册图表类型
 * 通过 props 传入 option，自动响应尺寸变化
 */
import { ref, watch, onMounted, onBeforeUnmount, shallowRef } from 'vue'
import * as echarts from 'echarts/core'
// 按需注册图表类型
import { LineChart, BarChart, PieChart } from 'echarts/charts'
// 按需注册组件
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DataZoomComponent,
  DatasetComponent,
  TransformComponent
} from 'echarts/components'
import { LabelLayout, UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'

// 注册 echarts 所需模块（仅一次）
echarts.use([
  LineChart,
  BarChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DataZoomComponent,
  DatasetComponent,
  TransformComponent,
  LabelLayout,
  UniversalTransition,
  CanvasRenderer
])

const props = defineProps({
  // ECharts 配置项
  option: {
    type: Object,
    default: () => ({})
  },
  // 图表高度
  height: {
    type: String,
    default: '400px'
  },
  // 是否处于加载状态
  loading: {
    type: Boolean,
    default: false
  }
})

const chartRef = ref(null)
// 图表实例使用 shallowRef，避免深度响应式影响性能
const chartInstance = shallowRef(null)
let resizeObserver = null

// 初始化图表
onMounted(() => {
  chartInstance.value = echarts.init(chartRef.value)
  if (props.option && Object.keys(props.option).length) {
    chartInstance.value.setOption(props.option)
  }

  // 监听容器尺寸变化自动重绘
  resizeObserver = new ResizeObserver(() => {
    chartInstance.value && chartInstance.value.resize()
  })
  resizeObserver.observe(chartRef.value)
})

// option 变化时增量更新
watch(
  () => props.option,
  (newOption) => {
    if (chartInstance.value && newOption) {
      chartInstance.value.setOption(newOption, true)
    }
  },
  { deep: true }
)

// loading 状态切换
watch(
  () => props.loading,
  (val) => {
    if (chartInstance.value) {
      val ? chartInstance.value.showLoading() : chartInstance.value.hideLoading()
    }
  }
)

// 销毁前清理
onBeforeUnmount(() => {
  resizeObserver && resizeObserver.disconnect()
  chartInstance.value && chartInstance.value.dispose()
})
</script>

<template>
  <div ref="chartRef" class="echart-box" :style="{ height }"></div>
</template>

<style lang="scss" scoped>
.echart-box {
  width: 100%;
}
</style>
