import {ExtConfigDefaultJson} from "../cgform.data";

// 初始化扩展JSON
export function parseExtConfigJson(record: Recordable) {
  // 解析扩展JSON
  let parseJSON = {};
  if (record.extConfigJson) {
    try {
      parseJSON = JSON.parse(record.extConfigJson);
    } catch (e) {
      console.error('online扩展JSON转换失败：', e);
    }
  }
  // 从数据库中取值，并合并
  return Object.assign({}, ExtConfigDefaultJson, parseJSON, {
    isDesForm: record.isDesForm || 'N',
    desFormCode: record.desFormCode || '',
  });
}
