import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '国家编码',
    align:"center",
    dataIndex: 'countryCode'
   },
   {
    title: '指标编码',
    align:"center",
    dataIndex: 'indicatorCode'
   },
   {
    title: '年份',
    align:"center",
    dataIndex: 'year'
   },
   {
    title: '指标数值',
    align:"center",
    dataIndex: 'value'
   },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '国家编码',
    field: 'countryCode',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入国家编码，如CHN,USA!'},
          ];
     },
  },
  {
    label: '指标编码',
    field: 'indicatorCode',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入指标编码!'},
          ];
     },
  },
  {
    label: '年份',
    field: 'year',
    component: 'InputNumber',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入年份!'},
          ];
     },
  },
  {
    label: '指标数值',
    field: 'value',
    component: 'InputNumber',
    componentProps: { precision: 8 },
  },
	// TODO 主键隐藏字段，目前写死为ID
	{
	  label: '',
	  field: 'id',
	  component: 'Input',
	  show: false
	},
];

// 高级查询数据
export const superQuerySchema = {
  countryCode: {title: '国家编码，如CHN,USA',order: 0,view: 'text', type: 'string',},
  indicatorCode: {title: '指标编码',order: 1,view: 'text', type: 'string',},
  year: {title: '年份',order: 2,view: 'number', type: 'number',},
  value: {title: '指标数值',order: 3,view: 'number', type: 'number',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}