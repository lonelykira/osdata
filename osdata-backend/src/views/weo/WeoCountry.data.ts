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
    title: '国家英文名称',
    align:"center",
    dataIndex: 'countryEn'
   },
   {
    title: '国家中文名称',
    align:"center",
    dataIndex: 'countryCn'
   },
   {
    title: '是否聚合经济体',
    align:"center",
    dataIndex: 'isAggregate'
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
                 { required: true, message: '请输入WEO国家编码，如CHN,USA!'},
          ];
     },
  },
  {
    label: '国家英文名称',
    field: 'countryEn',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入国家英文名称!'},
          ];
     },
  },
  {
    label: '国家中文名称',
    field: 'countryCn',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入国家中文名称!'},
          ];
     },
  },
  {
    label: '是否聚合经济体',
    field: 'isAggregate',
    component: 'Select',
    componentProps: {
      options: [
        { label: '否', value: 0 },
        { label: '是', value: 1 },
      ],
    },
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
  countryEn: {title: '国家英文名称',order: 1,view: 'text', type: 'string',},
  countryCn: {title: '国家中文名称',order: 2,view: 'text', type: 'string',},
  isAggregate: {title: '是否聚合经济体',order: 3,view: 'number', type: 'number',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}