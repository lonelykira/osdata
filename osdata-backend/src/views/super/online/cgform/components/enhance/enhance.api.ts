import { defHttp } from '/@/utils/http/axios';
import { isArray } from '/@/utils/is';

export enum Api {
  enhanceJs = '/online/cgform/head/enhanceJs/',
  enhanceButton = '/online/cgform/head/enhanceButton/',
}

export enum EnhanceJavaApi {
  enhanceJava = '/online/cgform/head/enhanceJava',
  deleteBatch = '/online/cgform/head/deleteBatchEnhanceJava',
}

export enum EnhanceSqlApi {
  enhanceSql = '/online/cgform/head/enhanceSql',
  deleteBatch = '/online/cgform/head/deletebatchEnhanceSql',
}

/**
 * 获取JS增强
 * @param code
 * @param type
 * @param params
 */
export async function getEnhanceJsByCode(code: string, type: string, params?) {
  let { success, result } = await defHttp.get(
    {
      url: Api.enhanceJs + code,
      params: {
        ...params,
        type,
      },
    },
    { isTransformResponse: false }
  );
  if (!success) {
    result = { cgJs: '' };
  }
  return result;
}

/**
 * 保存 JS 增强
 * @param code
 * @param params
 * @param isUpdate 是否更新
 */
export const saveEnhanceJs = (code: string, params, isUpdate: boolean) => {
  let url = `${Api.enhanceJs}${code}`;
  if (isUpdate) {
    return defHttp.put({ url, params }, { successMessageMode: 'none' });
  } else {
    return defHttp.post({ url, params }, { successMessageMode: 'none' });
  }
};

// 加载Java增强数据
export async function getEnhanceJavaByCode(code: string, params) {
  // 先加载按钮
  let btnRes = await defHttp.get({ url: Api.enhanceButton + code }, { isTransformResponse: false });
  let btnList = [];
  if (btnRes.success && isArray(btnRes.result)) {
    // 按钮过滤 java增强只看action按钮
    btnList = btnRes.result.filter((item) => item.optType == 'action');
  }
  // 再加载数据
  let path = `${EnhanceJavaApi.enhanceJava}/${code}`;
  let dataSource = await defHttp.get({ url: path, params });
  return { btnList, dataSource };
}

// 执行删除操作
export function doEnhanceJavaBatchDelete(idList: string[]) {
  return defHttp.delete(
    {
      url: EnhanceJavaApi.deleteBatch,
      params: {
        ids: idList.join(','),
      },
    },
    { joinParamsToUrl: true }
  );
}

/**
 * 保存 Java 增强
 * @param code
 * @param params
 * @param isUpdate 是否更新
 */
export const saveEnhanceJava = (code: string, params, isUpdate: boolean) => {
  let url = `${EnhanceJavaApi.enhanceJava}/${code}`;
  if (isUpdate) {
    return defHttp.put({ url, params });
  } else {
    return defHttp.post({ url, params });
  }
};

// 加载Sql增强数据
export async function getEnhanceSqlByCode(code: string, params) {
  // 先加载按钮
  let btnRes = await defHttp.get({ url: Api.enhanceButton + code }, { isTransformResponse: false });
  let btnList = [];
  if (btnRes.success && isArray(btnRes.result)) {
    // 按钮过滤 java增强只看action按钮
    btnList = btnRes.result.filter((item) => item.optType == 'action');
  }
  // 再加载数据
  let path = `${EnhanceSqlApi.enhanceSql}/${code}`;
  let dataSource = await defHttp.get({ url: path, params });
  return { btnList, dataSource };
}

// 执行删除操作
export function doEnhanceSqlBatchDelete(idList: string[]) {
  return defHttp.delete(
    {
      url: EnhanceSqlApi.deleteBatch,
      params: {
        ids: idList.join(','),
      },
    },
    { joinParamsToUrl: true }
  );
}

/**
 * 保存 Java 增强
 * @param code
 * @param params
 * @param isUpdate 是否更新
 */
export const saveEnhanceSql = (code: string, params, isUpdate: boolean) => {
  let url = `${EnhanceSqlApi.enhanceSql}/${code}`;
  if (isUpdate) {
    return defHttp.put({ url, params });
  } else {
    return defHttp.post({ url, params });
  }
};
