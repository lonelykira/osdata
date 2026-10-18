import { defHttp } from '/@/utils/http/axios';

export enum Api {
  authField = '/online/cgform/api/authColumn',
  authButton = '/online/cgform/api/authButton',
  authData = '/online/cgform/api/authData',
  authPage = '/online/cgform/api/authPage',
  roleAuth = '/online/cgform/api/roleAuth',
  saveButton = '/online/cgform/api/roleButtonAuth',
  saveData = '/online/cgform/api/roleDataAuth',
  validData = '/online/cgform/api/validAuthData',
  saveField = '/online/cgform/api/roleColumnAuth',
  batchAuthField = '/online/cgform/api/authColumn/batch',
  batchAuthButton = '/online/cgform/api/authButton/batch',
}

// 字段权限，查询数据
export const authFieldLoadData = (cgformId, params?) => defHttp.get({ url: `${Api.authField}/${cgformId}`, params });
// 字段权限，更新启用状态
export const authFieldUpdateStatus = (params) => defHttp.put({ url: Api.authField, params });
// 字段权限，更新权限状态
export const authFieldUpdateCheckbox = (params) => defHttp.post({ url: Api.authField, params });

// 字段权限，批量更新启用状态
export const batchAuthFieldUpdateStatus = (params) => defHttp.put({ url: Api.batchAuthField, params });
// 字段权限，批量更新权限状态
export const batchAuthFieldUpdateCheckbox = (params) => defHttp.post({ url: Api.batchAuthField, params });


// 按钮权限，查询数据
export const authButtonLoadData = (cgformId, params?) => defHttp.get({ url: `${Api.authButton}/${cgformId}`, params });
// 按钮权限，启用
export const authButtonEnable = (params) => defHttp.post({ url: Api.authButton, params });
// 按钮权限，禁用
export const authButtonDisable = (id: string, params?) => defHttp.put({ url: `${Api.authButton}/${id}`, params });
// 按钮权限，批量更新启用状态
export const batchAuthButtonUpdateStatus = (params) => defHttp.put({ url: Api.batchAuthButton, params });

// 数据权限，查询数据
export const authDataLoadData = (cgformId, params?) => defHttp.get({ url: `${Api.authData}/${cgformId}`, params });
// 数据权限，更新启用状态
export const authDataUpdateStatus = (params) => defHttp.put({ url: Api.authData, params });
// 数据权限，保存或修改
export const authDataSaveOrUpdate = (params, isUpdate: boolean) => {
  if (isUpdate) {
    return defHttp.put({ url: Api.authData, params });
  } else {
    return defHttp.post({ url: Api.authData, params });
  }
};
// 数据权限，删除
export const authDataDelete = (id: string, params?) => defHttp.delete({ url: `${Api.authData}/${id}`, params });

export const authFieldLoadTree = (cgformId: string, authType: number, params?) => {
  let url = `${Api.authPage}/${cgformId}/${authType}`;
  return defHttp.get({ url, params });
};

export const authDataLoadTree = (cgformId: string, params?) => {
  let url = `${Api.validData}/${cgformId}`;
  return defHttp.get({ url, params });
};

export const authButtonLoadTree = (cgformId: string, authType: number, params?) => {
  let url = `${Api.authPage}/${cgformId}/${authType}`;
  return defHttp.get({ url, params });
};

export const loadRoleAuthChecked = (params) => defHttp.get({ url: Api.roleAuth, params });

export const saveAuthField = (roleId: string, cgformId: string, params?) => {
  let url = `${Api.saveField}/${roleId}/${cgformId}`;
  return defHttp.post({ url, params });
};

export const saveAuthData = (roleId: string, cgformId: string, params?) => {
  let url = `${Api.saveData}/${roleId}/${cgformId}`;
  return defHttp.post({ url, params });
};

export const saveAuthButton = (roleId: string, cgformId: string, params?) => {
  let url = `${Api.saveButton}/${roleId}/${cgformId}`;
  return defHttp.post({url, params}, {successMessageMode: 'none', isTransformResponse: false});
};
