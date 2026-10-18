import { defHttp } from '/@/utils/http/axios';

enum Api {
  saveTenantJoinUser = '/sys/tenant/saveTenantJoinUser',
  joinTenantByHouseNumber = '/sys/tenant/joinTenantByHouseNumber',
}

/**
 * 保存租户
 * @param params
 */
export const saveTenantJoinUser = (params) => {
  return defHttp.post({ url: Api.saveTenantJoinUser, params }, { isTransformResponse: false });
};

/**
 * 加入租户
 * @param params
 */
export const joinTenantByHouseNumber = (params) => {
  return defHttp.post({ url: Api.joinTenantByHouseNumber, params }, { isTransformResponse: false });
};
