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
    title: '主题(英文)',
    align:"center",
    dataIndex: 'topic'
   },
   {
    title: '主题中文(翻译字段)',
    align:"center",
    dataIndex: 'topicCn'
   },
   {
    title: '指标名称(英文)',
    align:"center",
    dataIndex: 'indicatorName'
   },
   {
    title: '指标名称中文',
    align:"center",
    dataIndex: 'indicatorNameCn'
   },
   {
    title: '长定义(英文)',
    align:"center",
    dataIndex: 'longDefinition'
   },
   {
    title: '长定义中文(翻译字段)',
    align:"center",
    dataIndex: 'longDefinitionCn'
   },
   {
    title: '计量单位(英文)',
    align:"center",
    dataIndex: 'unit'
   },
   {
    title: '计量单位中文(翻译字段)',
    align:"center",
    dataIndex: 'unitCn'
   },
   {
    title: '数据频率(年/季/月)',
    align:"center",
    dataIndex: 'periodicity'
   },
   {
    title: '基期',
    align:"center",
    dataIndex: 'basePeriod'
   },
   {
    title: '其他说明',
    align:"center",
    dataIndex: 'otherNotes'
   },
   {
    title: '加总方法',
    align:"center",
    dataIndex: 'aggregationMethod'
   },
   {
    title: '局限与例外',
    align:"center",
    dataIndex: 'limitationsExceptions'
   },
   {
    title: '原始来源备注',
    align:"center",
    dataIndex: 'notesFromOriginalSource'
   },
   {
    title: '一般性注释',
    align:"center",
    dataIndex: 'generalComments'
   },
   {
    title: '数据来源机构(英文)',
    align:"center",
    dataIndex: 'source'
   },
   {
    title: '数据来源机构中文(翻译字段)',
    align:"center",
    dataIndex: 'sourceCn'
   },
   {
    title: '统计口径与方法',
    align:"center",
    dataIndex: 'statisticalConceptMethodology'
   },
   {
    title: '发展相关性',
    align:"center",
    dataIndex: 'developmentRelevance'
   },
   {
    title: '相关来源链接',
    align:"center",
    dataIndex: 'relatedSourceLinks'
   },
   {
    title: '其他网页链接',
    align:"center",
    dataIndex: 'otherWebLinks'
   },
   {
    title: '关联指标',
    align:"center",
    dataIndex: 'relatedIndicators'
   },
   {
    title: '许可类型(如 CC BY 4.0)',
    align:"center",
    dataIndex: 'licenseType'
   },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '指标代码(如 SP.POP.TOTL)',
    field: 'seriesCode',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入指标代码(如 SP.POP.TOTL)!'},
          ];
     },
  },
  {
    label: '主题(英文)',
    field: 'topic',
    component: 'Input',
  },
  {
    label: '主题中文',
    field: 'topicCn',
    component: 'Input',
  },
  {
    label: '指标名称(英文)',
    field: 'indicatorName',
    component: 'Input',
  },
  {
    label: '指标名称中文',
    field: 'indicatorNameCn',
    component: 'Input',
  },
  {
    label: '短定义(英文)',
    field: 'shortDefinition',
    component: 'InputTextArea',
  },
  {
    label: '短定义中文(翻译字段)',
    field: 'shortDefinitionCn',
    component: 'InputTextArea',
  },
  {
    label: '长定义(英文)',
    field: 'longDefinition',
    component: 'InputTextArea',
  },
  {
    label: '长定义中文(翻译字段)',
    field: 'longDefinitionCn',
    component: 'InputTextArea',
  },
  {
    label: '计量单位(英文)',
    field: 'unit',
    component: 'Input',
  },
  {
    label: '计量单位中文(翻译字段)',
    field: 'unitCn',
    component: 'Input',
  },
  {
    label: '数据频率(年/季/月)',
    field: 'periodicity',
    component: 'Input',
  },
  {
    label: '基期',
    field: 'basePeriod',
    component: 'Input',
  },
  {
    label: '其他说明',
    field: 'otherNotes',
    component: 'InputTextArea',
  },
  {
    label: '加总方法',
    field: 'aggregationMethod',
    component: 'Input',
  },
  {
    label: '局限与例外',
    field: 'limitationsExceptions',
    component: 'InputTextArea',
  },
  {
    label: '原始来源备注',
    field: 'notesFromOriginalSource',
    component: 'InputTextArea',
  },
  {
    label: '一般性注释',
    field: 'generalComments',
    component: 'InputTextArea',
  },
  {
    label: '数据来源机构(英文)',
    field: 'source',
    component: 'Input',
  },
  {
    label: '数据来源机构中文(翻译字段)',
    field: 'sourceCn',
    component: 'Input',
  },
  {
    label: '统计口径与方法',
    field: 'statisticalConceptMethodology',
    component: 'InputTextArea',
  },
  {
    label: '发展相关性',
    field: 'developmentRelevance',
    component: 'InputTextArea',
  },
  {
    label: '相关来源链接',
    field: 'relatedSourceLinks',
    component: 'InputTextArea',
  },
  {
    label: '其他网页链接',
    field: 'otherWebLinks',
    component: 'InputTextArea',
  },
  {
    label: '关联指标',
    field: 'relatedIndicators',
    component: 'Input',
  },
  {
    label: '许可类型(如 CC BY 4.0)',
    field: 'licenseType',
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
  seriesCode: {title: '指标代码(如 SP.POP.TOTL)',order: 0,view: 'text', type: 'string',},
  topic: {title: '主题(英文)',order: 1,view: 'text', type: 'string',},
  topicCn: {title: '主题中文',order: 2,view: 'text', type: 'string',},
  indicatorName: {title: '指标名称(英文)',order: 3,view: 'text', type: 'string',},
  indicatorNameCn: {title: '指标名称中文',order: 4,view: 'text', type: 'string',},
  shortDefinition: {title: '短定义(英文)',order: 5,view: 'textarea', type: 'string',},
  shortDefinitionCn: {title: '短定义中文(翻译字段)',order: 6,view: 'textarea', type: 'string',},
  longDefinition: {title: '长定义(英文)',order: 7,view: 'textarea', type: 'string',},
  longDefinitionCn: {title: '长定义中文(翻译字段)',order: 8,view: 'textarea', type: 'string',},
  unit: {title: '计量单位(英文)',order: 9,view: 'text', type: 'string',},
  unitCn: {title: '计量单位中文(翻译字段)',order: 10,view: 'text', type: 'string',},
  periodicity: {title: '数据频率(年/季/月)',order: 11,view: 'text', type: 'string',},
  basePeriod: {title: '基期',order: 12,view: 'text', type: 'string',},
  otherNotes: {title: '其他说明',order: 13,view: 'textarea', type: 'string',},
  aggregationMethod: {title: '加总方法',order: 14,view: 'text', type: 'string',},
  limitationsExceptions: {title: '局限与例外',order: 15,view: 'textarea', type: 'string',},
  notesFromOriginalSource: {title: '原始来源备注',order: 16,view: 'textarea', type: 'string',},
  generalComments: {title: '一般性注释',order: 17,view: 'textarea', type: 'string',},
  source: {title: '数据来源机构(英文)',order: 18,view: 'text', type: 'string',},
  sourceCn: {title: '数据来源机构中文(翻译字段)',order: 19,view: 'text', type: 'string',},
  statisticalConceptMethodology: {title: '统计口径与方法',order: 20,view: 'textarea', type: 'string',},
  developmentRelevance: {title: '发展相关性',order: 21,view: 'textarea', type: 'string',},
  relatedSourceLinks: {title: '相关来源链接',order: 22,view: 'textarea', type: 'string',},
  otherWebLinks: {title: '其他网页链接',order: 23,view: 'textarea', type: 'string',},
  relatedIndicators: {title: '关联指标',order: 24,view: 'text', type: 'string',},
  licenseType: {title: '许可类型(如 CC BY 4.0)',order: 25,view: 'text', type: 'string',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}