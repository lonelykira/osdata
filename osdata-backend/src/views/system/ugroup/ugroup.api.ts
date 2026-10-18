import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";
import { Modal } from 'ant-design-vue';
const { createConfirm } = useMessage();

enum Api {
  list = '/sys/ugroup/list',
  save='/sys/ugroup/add',
  edit='/sys/ugroup/edit',
  deleteOne = '/sys/ugroup/delete',
  deleteBatch = '/sys/ugroup/deleteBatch',
  importExcel = '/sys/ugroup/importExcel',
  exportXls = '/sys/ugroup/exportXls',

  userList = '/sys/user/groupUserList',
  deleteUserGroup = '/sys/user/deleteGroupUser',
  batchDeleteUserGroup = '/sys/user/deleteUserGroupBatch',
  addUserGroup = '/sys/user/addSysUserGroup',
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
export const saveOrUpdate = (params, isUpdate,showTip = true) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({url: url, params},{successMessageMode:showTip?'success':'none'});
}
/**
 * 角色列表接口
 * @param params
 */
export const userList = (params) => defHttp.get({ url: Api.userList, params });
/**
 * 删除角色用户
 */
export const deleteUserGroup = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteUserGroup, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * 批量删除角色用户
 * @param params
 */
export const batchDeleteUserGroup = (params, handleSuccess) => {
  Modal.confirm({
    title: '确认删除',
    content: '是否确认取消关联选中用户？',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({ url: Api.batchDeleteUserGroup, params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};
/**
 * 添加已有用户
 */
export const addUserGroup = (params, handleSuccess) => {
  return defHttp.post({ url: Api.addUserGroup, params }).then(() => {
    handleSuccess();
  });
};
