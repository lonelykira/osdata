import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/airag/prompts/list',
  queryById = '/airag/prompts/queryById',
  save='/airag/prompts/add',
  edit='/airag/prompts/edit',
  deleteOne = '/airag/prompts/delete',
  deleteBatch = '/airag/prompts/deleteBatch',
  importExcel = '/airag/prompts/importExcel',
  exportXls = '/airag/prompts/exportXls',

  promptExperiment = '/airag/prompts/experiment',
  recycleBinList = '/airag/prompts/recycleBinList',
  revertRecycleBin = '/airag/prompts/revertRecycleBin',
  deleteRecycleBin = '/airag/prompts/deleteRecycleBin',
}
/**
 * 导出api
 * @param params
 */
export const getExportUrl = Api.exportXls;
/**
 * 导入api
 */
export const getImportUrl = Api.importExcel;
/**
 * 列表接口
 * @param params
 */
export const list = (params) =>
  defHttp.get({url: Api.list, params});

/**
 * 根据ID查询提示词详情
 * @param id 提示词ID
 */
export const queryById = (id: string) =>
  defHttp.get({url: Api.queryById, params: {id}});

/**
 * 删除单个
 */
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}
/**
 * 批量删除
 * @param params
 */
export const batchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({url: Api.deleteBatch, data: params}, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}
/**
 * 保存或者更新
 * @param params
 */
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({url: url, params});
}
/**
 * 实验
 * @param params
 */
export const promptExperiment = (params) => {
  return defHttp.post({url: Api.promptExperiment, params},{ isTransformResponse: false });
}
/**
 * 回收站列表
 */
export const getRecycleBinList = (params) => defHttp.get({ url: Api.recycleBinList, params });
/**
 * 还原
 */
export const putRecycleBin = (params, handleSuccess) => {
  return defHttp.put({ url: Api.revertRecycleBin, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * 彻底删除
 */
export const deleteRecycleBin = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteRecycleBin, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
