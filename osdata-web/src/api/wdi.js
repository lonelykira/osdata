import request from '@/utils/request'

/**
 * WDI 世界发展指标接口
 * 所有接口走统一封装的 request 实例（baseURL=/api）
 */

// ===== 主题与指标 =====

// 获取指标主题树（后端真实接口：一级主题+子指标列表 [{topic, topicCn, children}]）
export function getTopicTree() {
  return request.get('/webapi/wdi/topic/tree')
}

// 指标分页检索（后端真实接口：支持主题筛选 topicCode 与关键词 keyword，返回 IPage<SeriesPageVO>）
export function getSeriesPage(params) {
  return request.get('/webapi/wdi/series/page', { params })
}

// 指标详情（后端真实接口：按指标ID查询，返回完整定义/单位/来源/年份覆盖）
export function getSeriesDetail(seriesId) {
  return request.get(`/webapi/wdi/series/${seriesId}`)
}

// ===== 国家 / 年份 =====

// 获取国家（地区）列表
export function getCountryList() {
  return request.get('/wdi/countries')
}

// 国家分组树（后端真实接口：按区域分组，一次返回全部国家，前端拍平使用）
export function getCountryTree() {
  return request.get('/webapi/wdi/country/tree')
}

// 获取可用年份范围
export function getYearRange() {
  return request.get('/wdi/years')
}

// 指标可用年份范围（后端真实接口：选中指标年份集合交集，返回 {minYear, maxYear}）
export function getSeriesYearRange(seriesCodeList) {
  return request.post('/webapi/wdi/series/year-range', { seriesCodeList })
}

// ===== 数据查询 =====

/**
 * 执行数据查询
 * @param {Object} data { indicators: ['NY.GDP.MKTP.CD'], countries: ['CHN'], years: [2020, 2021] }
 */
export function queryWdiData(data) {
  return request.post('/wdi/query', data, { timeout: 60000 })
}

/**
 * WDI面板数据查询（后端真实接口：TABLE_LONG长表视图）
 * @param {Object} data { seriesCodeList, countryCodeList, startYear, endYear, viewType }
 */
export function queryPanelData(data) {
  return request.post('/webapi/wdi/data/query', data, { timeout: 60000 })
}

// ===== 查询方案 =====

// 获取查询方案列表
export function getSchemeList() {
  return request.get('/wdi/schemes')
}

// 创建查询方案
export function createScheme(data) {
  return request.post('/wdi/schemes', data)
}

// 更新查询方案
export function updateScheme(id, data) {
  return request.put(`/wdi/schemes/${id}`, data)
}

// 删除查询方案
export function deleteScheme(id) {
  return request.delete(`/wdi/schemes/${id}`)
}

// ===== 导出任务 =====

// 获取导出任务列表
export function getExportList(params) {
  return request.get('/wdi/exports', { params })
}

/**
 * 创建导出任务
 * @param {Object} data { name, indicators, countries, years, format }
 */
export function createExportTask(data) {
  return request.post('/wdi/exports', data)
}

// 删除导出任务
export function deleteExportTask(id) {
  return request.delete(`/wdi/exports/${id}`)
}

// 获取导出文件下载地址
export function getExportDownloadUrl(id) {
  return `/api/wdi/exports/${id}/download`
}

// ===== 首页统计 =====

// 获取平台概览统计（指标数、国家数、年份跨度等）
export function getOverviewStats() {
  return request.get('/wdi/overview')
}

// 获取WDI统计概览（后端真实接口：指标总数/国家总数/数据年份跨度/数据记录总数，Redis缓存）
export function getStatisticsSummary() {
  return request.get('/webapi/wdi/statisticsSummary')
}
