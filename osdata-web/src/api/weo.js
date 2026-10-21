import request from '@/utils/request'

/**
 * WEO 世界经济展望指标接口（世界经济组织（IMF））
 * 所有接口走统一封装的 request 实例（baseURL=/api），全部免登录
 */

// ===== 主题与指标 =====

// 获取指标主题树（后端真实接口：一级主题+子指标列表 [{topicCode, topicEn, topicCn, children}]）
export function getTopicTree() {
  return request.get('/webapi/weo/topic/tree')
}

// 指标分页检索（后端真实接口：支持主题筛选 topicCode 与关键词 keyword，返回 IPage<IndicatorPageVO>）
export function getIndicatorPage(params) {
  return request.get('/webapi/weo/indicator/page', { params })
}

// 指标详情（后端真实接口：按指标ID查询，返回单位/数量级/年份覆盖）
export function getIndicatorDetail(indicatorId) {
  return request.get(`/webapi/weo/indicator/${indicatorId}`)
}

// 指标批量查询（后端真实接口：body {indicatorCodeList} → List<IndicatorPageVO>，
// 无效编码对应位置为 null，此处直接过滤，调用方无需再判空）
export function getIndicatorBatch(indicatorCodeList) {
  return request
    .post('/webapi/weo/indicator/batch', { indicatorCodeList })
    .then((list) => (list || []).filter(Boolean))
}

// 指标可用国家列表（后端真实接口：返回 [{countryCode, countryEn, countryCn, isAggregate}]）
export function getIndicatorCountries(indicatorCode) {
  return request.get(`/webapi/weo/indicator/${indicatorCode}/country`)
}

// 指标可用年份范围（后端真实接口：body {indicatorCodeList}，返回交集 {minYear, maxYear}，交集为空时为 null）
export function getIndicatorYearRange(indicatorCodeList) {
  return request.post('/webapi/weo/indicator/year-range', { indicatorCodeList })
}

// ===== 国家 / 年份 =====

// 国家分组树（后端真实接口：固定两组 COUNTRY=国家 / AGGREGATE=聚合经济体，一次返回全部国家，前端拍平使用）
export function getCountryTree() {
  return request.get('/webapi/weo/country/tree')
}

// 国家分页检索（后端真实接口：支持关键词 keyword 与 isAggregate 过滤）
export function getCountryPage(params) {
  return request.get('/webapi/weo/country/page', { params })
}

// 国家批量查询（后端真实接口：body {countryCodeList}，无效编码对应位置为 null，此处直接过滤）
export function getCountryBatch(countryCodeList) {
  return request
    .post('/webapi/weo/country/batch', { countryCodeList })
    .then((list) => (list || []).filter(Boolean))
}

// ===== 数据查询 =====

/**
 * WEO面板数据查询（后端真实接口：TABLE_LONG长表视图 / TABLE_WIDE宽表透视视图）
 * @param {Object} data { indicatorCodeList, countryCodeList, startYear, endYear, viewType }
 */
export function queryPanelData(data) {
  return request.post('/webapi/weo/data/query', data, { timeout: 60000 })
}

// ===== 统计概览 =====

// 获取WEO统计概览（后端真实接口：指标总数/国家总数/数据年份跨度/数据记录总数）
export function getStatisticsSummary() {
  return request.get('/webapi/weo/statisticsSummary')
}
