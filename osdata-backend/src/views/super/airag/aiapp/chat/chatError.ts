/**
 * 提取AI聊天接口响应中的业务错误文案。
 *
 * @param payload 普通Result响应或SSE事件
 * @return 错误文案；非错误响应返回空字符串
 * @author scott
 * @since 2026-07-21 【issues/9787】无效分享链接错误提示
 */
export function getChatErrorMessage(payload: any): string {
  if (!payload) {
    return '';
  }
  if (payload.success === false) {
    return payload.message || '请求出错，请稍后重试！';
  }
  if (payload.event === 'ERROR') {
    return payload.data?.message || '请求出错，请稍后重试！';
  }
  if (payload.event === 'FLOW_FINISHED' && payload.data?.success === false) {
    return payload.data?.message || '请求出错，请稍后重试！';
  }
  return '';
}
