// AI Video API 接口配置
import { defHttp } from '@/utils/http/axios';

enum Api {
  submit = '/airag/video/submit',
  query = '/airag/video/query',
  listByUser = '/airag/video/listByUser',
  deleteRecord = '/airag/video/deleteVideoRecord',
}

/**
 * 提交视频生成任务
 */
export const submitVideoTask = (params: any) => {
  return defHttp.post({ url: Api.submit, params }, { isTransformResponse: false });
};

/**
 * 查询视频生成任务状态
 */
export const queryVideoTask = (taskId: string) => {
  return defHttp.get({ url: `${Api.query}/${taskId}` }, { isTransformResponse: false });
};

/**
 * 根据用户id查询视频列表
 */
export const getVideoListByUser = (params: { userId: string }) => {
  return defHttp.get({ url: Api.listByUser, params }, { isTransformResponse: false });
};

/**
 * 删除视频记录
 */
export const deleteVideoRecord = (params) => {
  return defHttp.delete({ url: Api.deleteRecord, params }, { isTransformResponse: false, joinParamsToUrl: true });
};
