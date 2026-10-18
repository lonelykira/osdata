import { defHttp } from '/@/utils/http/axios';

enum Api {
  sqlPageExecute = '/airag/mcp/database/sqlPageExecute',
  sqlExportXls = '/airag/mcp/database/sqlExportXls',
}

/**
 * 分页执行 SQL 查询
 */
export function sqlPageExecute(params: { sql: string; dbSource?: string; pageNo: number; pageSize: number }) {
  return defHttp.post<Recordable>(
    {
      url: Api.sqlPageExecute,
      params: {
        sql: params.sql,
        dbSourceKey: params.dbSource || '',
        pageNo: params.pageNo,
        pageSize: params.pageSize,
      },
    },
    { isTransformResponse: false }
  );
}

/**
 * 导出图表原始数据为 Excel
 */
export function sqlExportXls(params: { sql: string; dbSource?: string; columns?: Recordable }) {
  return defHttp.post(
    {
      url: Api.sqlExportXls,
      params: {
        sql: params.sql,
        dbSourceKey: params.dbSource || '',
        columns: params.columns || {},
      },
      responseType: 'blob',
      timeout: 5 * 60 * 1000,
    },
    { isTransformResponse: false, isReturnNativeResponse: true }
  );
}
