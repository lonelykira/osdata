import {defHttp} from "@/utils/http/axios";

const enum Api {
  getCgformById = '/online/cgform/head/queryById',
  getCgformRecordById = '/online/cgform/api/form/{formId}/{recordId}',
}

export const getCgformById = (id: string) => defHttp.get({
  url: Api.getCgformById,
  params: {id}
}, {isTransformResponse: false});

export const getCgformRecordById = (formId: string, dataId: string) => defHttp.get({
  url: Api.getCgformRecordById.replace('{formId}', formId).replace('{recordId}', dataId),
  params: {}
}, {isTransformResponse: false});
