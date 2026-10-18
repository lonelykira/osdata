import axios from 'axios'
import { message } from 'ant-design-vue'

/**
 * axios 全局封装
 * - baseURL 统一为 /api
 * - 请求拦截器：全局 loading 计数
 * - 响应拦截器：统一解包业务数据、统一错误提示
 */

// ===== 全局 loading 状态（供布局组件订阅，显示顶部加载条） =====
export const loadingState = {
  count: 0, // 当前进行中的请求数
  visible: false // 是否显示加载条
}

// 开启 loading（计数 +1）
function showLoading() {
  loadingState.count++
  loadingState.visible = true
}

// 关闭 loading（计数 -1，归零后隐藏）
function hideLoading() {
  loadingState.count = Math.max(0, loadingState.count - 1)
  if (loadingState.count === 0) {
    loadingState.visible = false
  }
}

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api', // 统一接口前缀，由 vite 代理转发到后端
  timeout: 30000 // 超时时间 30s（数据查询可能较慢）
})

// ===== 请求拦截器 =====
service.interceptors.request.use(
  (config) => {
    // 是否需要 loading：可在请求配置中传 silent: true 关闭
    if (!config.silent) {
      showLoading()
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// ===== 响应拦截器 =====
service.interceptors.response.use(
  (response) => {
    // 关闭对应请求的 loading
    if (!response.config.silent) {
      hideLoading()
    }

    const res = response.data

    // 后端统一响应结构：{ code, message, data } 或 jeecg Result：{ code, message, result }
    // 约定 code === 200 表示成功
    if (res && res.code !== undefined) {
      if (res.code === 200) {
        // jeecg Result 结构返回 result 字段，标准结构返回 data 字段
        if (res.data !== undefined) return res.data
        if (res.result !== undefined) return res.result
        return res
      }
      // 业务错误：统一提示并抛出
      message.error(res.message || '请求失败，请稍后重试')
      return Promise.reject(new Error(res.message || 'Error'))
    }

    // 非标准结构（如直接返回数组/文件流），原样返回
    return res
  },
  (error) => {
    // 关闭对应请求的 loading
    if (error.config && !error.config.silent) {
      hideLoading()
    }

    // HTTP 错误统一提示
    let msg = '网络异常，请稍后重试'
    if (error.response) {
      const status = error.response.status
      if (status === 404) msg = '请求的资源不存在(404)'
      else if (status === 500) msg = '服务器内部错误(500)'
      else if (status === 502 || status === 503) msg = '服务暂时不可用，请稍后重试'
      else msg = `请求出错(${status})`
    } else if (error.code === 'ECONNABORTED') {
      msg = '请求超时，请稍后重试'
    }
    message.error(msg)
    return Promise.reject(error)
  }
)

export default service
