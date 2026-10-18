import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '指标代码',
    align:"center",
    dataIndex: 'seriesCode'
   },
   {
    title: '年份',
    align:"center",
    dataIndex: 'year'
   },
   {
    title: '年份覆盖说明',
    align:"center",
    dataIndex: 'description'
  },
  {
    title: '年份覆盖说明中文',
    align:"center",
    dataIndex: 'descriptionCn'
   },

];
//查询数据
export const searchFormSchema: FormSchema[] = [
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '指标代码',
    field: 'seriesCode',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入指标代码!'},
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
    label: '年份覆盖说明',
    field: 'description',
    component: 'InputTextArea',
  },
  {
    label: '年份覆盖说明中文',
    field: 'descriptionCn',
    component: 'InputTextArea',
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
  seriesCode: {title: '指标代码',order: 0,view: 'text', type: 'string',},
  year: {title: '年份',order: 1,view: 'number', type: 'number',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}