import { defineStore } from 'pinia'

/**
 * WDI 数据查询状态仓库
 * 跨页面共享：已选指标、国家、年份、查询条件、查询结果
 */
export const useWdiStore = defineStore('wdi', {
  state: () => ({
    // ===== 查询条件 =====
    selectedIndicators: [], // 已选指标代码数组，如 ['NY.GDP.MKTP.CD']
    selectedIndicatorObjects: [], // 已选指标完整对象（含名称），用于回显
    selectedCountries: [], // 已选国家代码数组，如 ['CHN', 'USA']
    selectedCountryObjects: [], // 已选国家完整对象（含名称）
    yearRange: [], // 已选年份范围 [startYear, endYear]

    // ===== 查询结果 =====
    queryResult: {
      columns: [], // 动态表头 [{ title, dataIndex, key }]
      rows: [], // 数据行 [{ countryName, countryCode, indicatorName, indicatorCode, year, value }]
      total: 0 // 总行数
    },
    lastQueryTime: null, // 最近一次查询时间

    // ===== 页面间联动 =====
    activeTopic: null, // 指标浏览页当前激活的主题（用于浏览页 -> 查询页联动）
    pendingScheme: null // 待加载的查询方案对象（方案页 -> 查询页联动）
  }),

  getters: {
    // 查询条件是否完整（至少 1 个指标、1 个国家、年份范围）
    isConditionReady(state) {
      return (
        state.selectedIndicators.length > 0 &&
        state.selectedCountries.length > 0 &&
        Array.isArray(state.yearRange) &&
        state.yearRange.length === 2 &&
        state.yearRange[0] <= state.yearRange[1]
      )
    },
    // 条件摘要文本（用于展示）
    conditionSummary(state) {
      const y =
        state.yearRange && state.yearRange.length === 2
          ? `${state.yearRange[0]}-${state.yearRange[1]}`
          : '未选择'
      return `指标 ${state.selectedIndicators.length} 个 / 国家 ${state.selectedCountries.length} 个 / 年份 ${y}`
    }
  },

  actions: {
    // ===== 指标 =====

    // 设置已选指标（对象数组 [{code, name}]）
    setIndicators(list) {
      this.selectedIndicatorObjects = list || []
      this.selectedIndicators = (list || []).map((i) => i.code || i.indicatorCode)
    },

    // 清空指标
    clearIndicators() {
      this.selectedIndicators = []
      this.selectedIndicatorObjects = []
    },

    // ===== 国家 =====

    // 设置已选国家（对象数组 [{code, name}]）
    setCountries(list) {
      this.selectedCountryObjects = list || []
      this.selectedCountries = (list || []).map((c) => c.code || c.countryCode)
    },

    // 清空国家
    clearCountries() {
      this.selectedCountries = []
      this.selectedCountryObjects = []
    },

    // ===== 年份 =====

    // 设置年份范围 [start, end]
    setYearRange(range) {
      this.yearRange = range
    },

    // ===== 结果 =====

    // 写入查询结果
    setResult(result) {
      this.queryResult = {
        columns: result?.columns || [],
        rows: result?.rows || [],
        total: result?.total ?? (result?.rows?.length || 0)
      }
      this.lastQueryTime = Date.now()
    },

    // 清空查询结果
    clearResult() {
      this.queryResult = { columns: [], rows: [], total: 0 }
      this.lastQueryTime = null
    },

    // ===== 联动 =====

    // 设置当前主题（浏览页使用）
    setActiveTopic(topic) {
      this.activeTopic = topic
    },

    // 设置待加载方案（查询页进入时读取并消费）
    setPendingScheme(scheme) {
      this.pendingScheme = scheme
    },

    // 应用查询方案到当前条件（方案页/查询页共用）
    applyScheme(scheme) {
      if (!scheme) return
      const condition = scheme.condition || scheme
      this.selectedIndicators = condition.indicators || []
      this.selectedIndicatorObjects = condition.indicatorObjects || []
      this.selectedCountries = condition.countries || []
      this.selectedCountryObjects = condition.countryObjects || []
      this.yearRange = condition.yearRange || []
    },

    // 重置全部查询条件
    resetAll() {
      this.selectedIndicators = []
      this.selectedIndicatorObjects = []
      this.selectedCountries = []
      this.selectedCountryObjects = []
      this.yearRange = []
      this.queryResult = { columns: [], rows: [], total: 0 }
      this.lastQueryTime = null
      this.pendingScheme = null
    },

    // 导出当前条件为纯对象（用于保存方案）
    getConditionSnapshot() {
      return {
        indicators: [...this.selectedIndicators],
        indicatorObjects: JSON.parse(JSON.stringify(this.selectedIndicatorObjects)),
        countries: [...this.selectedCountries],
        countryObjects: JSON.parse(JSON.stringify(this.selectedCountryObjects)),
        yearRange: this.yearRange ? [...this.yearRange] : []
      }
    }
  }
})
