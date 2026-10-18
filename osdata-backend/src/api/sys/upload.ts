import { UploadApiResult } from './model/uploadModel';
import { defHttp } from '/@/utils/http/axios';
import { UploadFileParams } from '/#/axios';
import { useGlobSetting } from '/@/hooks/setting';

const { uploadUrl = '' } = useGlobSetting();

/**
 * @description: Upload interface
 */
export function uploadApi(params: UploadFileParams, onUploadProgress: (progressEvent: ProgressEvent) => void) {
  return defHttp.uploadFile<UploadApiResult>(
    {
      url: `${uploadUrl}/sys/common/upload`,
      onUploadProgress,
    },
    params,
    // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-1385】修复上传示例报错
    // 返回上传接口完整响应，避免调用方拿到 undefined 被当成失败
    { isReturnResponse: true }
    // update-end--author:liaozhiyang---date:20260804---for：【LHZP-1385】修复上传示例报错
  );
}
/**
 * @description: Upload interface
 */
export function uploadImg(params: UploadFileParams, onUploadProgress: (progressEvent: ProgressEvent) => void) {
  return defHttp.uploadFile<UploadApiResult>(
    {
      url: `${uploadUrl}/sys/common/upload`,
      onUploadProgress,
    },
    params,
    { isReturnResponse: true }
  );
}
