import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '国家代码',
    align:"center",
    dataIndex: 'countryCode'
   },
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
    title: '脚注内容(英文)',
    align:"center",
    dataIndex: 'footnoteText'
   },
   {
    title: '脚注内容中文(翻译字段)',
    align:"center",
    dataIndex: 'footnoteTextCn'
   },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '国家代码',
    field: 'countryCode',
    component: 'Input',
  },
  {
    label: '指标代码',
    field: 'seriesCode',
    component: 'Input',
  },
  {
    label: '年份',
    field: 'year',
    component: 'InputNumber',
  },
  {
    label: '脚注内容(英文)',
    field: 'footnoteText',
    component: 'InputTextArea',
  },
  {
    label: '脚注内容中文(翻译字段)',
    field: 'footnoteTextCn',
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
  countryCode: {title: '国家代码',order: 0,view: 'text', type: 'string',},
  seriesCode: {title: '指标代码',order: 1,view: 'text', type: 'string',},
  year: {title: '年份',order: 2,view: 'number', type: 'number',},
  footnoteText: {title: '脚注内容(英文)',order: 3,view: 'textarea', type: 'string',},
  footnoteTextCn: {title: '脚注内容中文(翻译字段)',order: 4,view: 'textarea', type: 'string',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}