import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: 'topic英文名称',
    align:"center",
    dataIndex: 'topicEn'
   },
   {
    title: 'topic中文翻译',
    align:"center",
    dataIndex: 'topicCn'
   },
   {
    title: '排序',
    align:"center",
    dataIndex: 'sortOrder'
   },
   {
    title: '备注',
    align:"center",
    dataIndex: 'remark'
   },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: 'topic英文名称',
    field: 'topicEn',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入topic英文名称(官方)!'},
          ];
     },
  },
  {
    label: 'topic中文翻译',
    field: 'topicCn',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入topic中文翻译!'},
          ];
     },
  },
  {
    label: '排序',
    field: 'sortOrder',
    component: 'InputNumber',
  },
  {
    label: '备注',
    field: 'remark',
    component: 'Input',
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
  topicEn: {title: 'topic英文名称',order: 0,view: 'text', type: 'string',},
  topicCn: {title: 'topic中文翻译',order: 1,view: 'text', type: 'string',},
  sortOrder: {title: '排序',order: 2,view: 'number', type: 'number',},
  remark: {title: '备注',order: 3,view: 'text', type: 'string',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}