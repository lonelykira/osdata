import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '指标编码',
    align:"center",
    dataIndex: 'indicatorCode'
   },
   {
    title: '指标英文名称',
    align:"center",
    dataIndex: 'indicatorEn'
   },
   {
    title: '指标中文翻译',
    align:"center",
    dataIndex: 'indicatorCn'
   },
   {
    title: '单位',
    align:"center",
    dataIndex: 'unit'
   },
   {
    title: '量级',
    align:"center",
    dataIndex: 'scale'
   },
   {
    title: '主题编码',
    align:"center",
    dataIndex: 'topicCode'
   },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: 'WEO指标编码，如NGDP,PCPI',
    field: 'indicatorCode',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入WEO指标编码，如NGDP,PCPI!'},
          ];
     },
  },
  {
    label: '指标英文全称',
    field: 'indicatorEn',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入指标英文全称!'},
          ];
     },
  },
  {
    label: '指标中文翻译',
    field: 'indicatorCn',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入指标中文翻译!'},
          ];
     },
  },
  {
    label: '单位',
    field: 'unit',
    component: 'Input',
  },
  {
    label: '量级，Billions/Units等',
    field: 'scale',
    component: 'Input',
  },
  {
    label: '主题编码',
    field: 'topicCode',
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
  indicatorCode: {title: 'WEO指标编码，如NGDP,PCPI',order: 0,view: 'text', type: 'string',},
  indicatorEn: {title: '指标英文全称',order: 1,view: 'text', type: 'string',},
  indicatorCn: {title: '指标中文翻译',order: 2,view: 'text', type: 'string',},
  unit: {title: '单位',order: 3,view: 'text', type: 'string',},
  scale: {title: '量级，Billions/Units等',order: 4,view: 'text', type: 'string',},
  topicCode: {title: '主题编码',order: 5,view: 'text', type: 'string',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}