/**
 * 通用工具函数
 */

// 千分位格式化数字
export function formatNumber(value, digits = 2) {
  if (value === null || value === undefined || value === '' || isNaN(value)) return '-'
  return Number(value).toLocaleString('zh-CN', {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits
  })
}

// 智能格式化数值：超大数转亿/万单位，小数保留有效位
export function formatSmartNumber(value) {
  if (value === null || value === undefined || value === '' || isNaN(value)) return '-'
  const num = Number(value)
  const abs = Math.abs(num)
  if (abs >= 1e12) return (num / 1e12).toFixed(2) + ' 万亿'
  if (abs >= 1e8) return (num / 1e8).toFixed(2) + ' 亿'
  if (abs >= 1e4) return (num / 1e4).toFixed(2) + ' 万'
  return String(num)
}

// 将数组转为 ECharts 数据映射格式 { year: { country: value } }
export function groupQueryRows(rows) {
  const map = {}
  rows.forEach((row) => {
    if (!map[row.year]) map[row.year] = {}
    map[row.year][row.countryName || row.countryCode] = row.value
  })
  return map
}

// 防抖
export function debounce(fn, wait = 300) {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn.apply(this, args), wait)
  }
}

// 生成简易唯一 id（用于本地方案暂存）
export function genId() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 8)
}

// 日期时间格式化（dayjs 已全局可用性不强，这里独立封装简单格式化）
export function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

// 触发浏览器下载 Blob（导出文件用）
export function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}
