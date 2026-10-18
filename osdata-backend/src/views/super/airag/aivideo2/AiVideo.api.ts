import { defHttp } from '@/utils/http/axios';

enum Api {
  submit = '/airag/video/submit',
  query = '/airag/video/query',
  prompts = '/airag/video/prompts',
}

/**
 * 提交视频生成任务
 */
export const submitVideoTask = (params: { prompt: string; category?: string }) => defHttp.post({ url: Api.submit, params });

/**
 * 查询视频生成任务状态
 */
export const queryVideoTask = (taskId: string) => defHttp.get({ url: `${Api.query}/${taskId}` });

/**
 * 获取预设提示词
 */
export const getPresetPrompts = () => defHttp.get({ url: Api.prompts });
