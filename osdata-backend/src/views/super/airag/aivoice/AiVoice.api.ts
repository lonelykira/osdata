import { defHttp } from '@/utils/http/axios';

enum Api {
  generate = '/airag/voice/generate',
  generateAsync = '/airag/voice/generateAsync',
  queryTask = '/airag/voice/queryTask',
  listByUser = '/airag/voice/listByUser',
  deleteVoiceRecord = '/airag/voice/deleteVoiceRecord',
}

/**
 * 提交语音生成任务（同步，保留兼容）
 */
export const submitVoiceTask = (params) => defHttp.post({ url: Api.generate, params }, { isTransformResponse: false });

/**
 * 异步提交语音生成任务，立即返回 taskId
 */
export const generateVoiceAsync = (params) => defHttp.post({ url: Api.generateAsync, params }, { isTransformResponse: false });

/**
 * 查询异步语音任务结果
 */
export const queryVoiceTask = (taskId: string) => defHttp.get({ url: `${Api.queryTask}/${taskId}` }, { isTransformResponse: false });

/**
 * 根据用户id查询语音列表
 */
export const getVoiceListByUser = (params: { userId: string }) => defHttp.get({ url: Api.listByUser, params },{ isTransformResponse: false });

/**
 * 删除语音记录
 */
export const deleteVoiceRecord = (params) => defHttp.delete({ url: Api.deleteVoiceRecord, params }, { isTransformResponse: false, joinParamsToUrl: true });