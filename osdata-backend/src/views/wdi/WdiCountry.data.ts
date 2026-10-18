import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '经济体代码',
    align:"center",
    dataIndex: 'countryCode'
   },
   {
    title: '英文',
    align:"center",
    dataIndex: 'shortName'
   },
   {
    title: '中文名',
    align:"center",
    dataIndex: 'countryNameCn'
   },
   {
    title: '两位字母代码',
    align:"center",
    dataIndex: 'alpha2Code'
   },
   {
    title: '货币单位',
    align:"center",
    dataIndex: 'currencyUnit'
   },
   {
    title: '特别说明',
    align:"center",
    dataIndex: 'specialNotes'
   },
   {
    title: '区域(英文)',
    align:"center",
    dataIndex: 'region'
   },
   {
    title: '区域中文(翻译字段)',
    align:"center",
    dataIndex: 'regionCn'
   },
   {
    title: '收入分组(英文)',
    align:"center",
    dataIndex: 'incomeGroup'
   },
   {
    title: '收入分组中文(翻译字段)',
    align:"center",
    dataIndex: 'incomeGroupCn'
   },
   {
    title: '国民账户基年',
    align:"center",
    dataIndex: 'naBaseYear'
   },
   {
    title: '国民账户参考年',
    align:"center",
    dataIndex: 'naReferenceYear'
   },
   {
    title: 'SNA价格核算方式',
    align:"center",
    dataIndex: 'snaPriceValuation'
   }
];
//查询数据
export const searchFormSchema: FormSchema[] = [
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '世行国家/经济体代码',
    field: 'countryCode',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入世行国家/经济体代码!'},
          ];
     },
  },
  {
    label: '简称(英文)',
    field: 'shortName',
    component: 'Input',
  },
  {
    label: '国家中文名(翻译字段)',
    field: 'countryNameCn',
    component: 'Input',
  },
  {
    label: '表名',
    field: 'tableName',
    component: 'Input',
  },
  {
    label: '全称(英文)',
    field: 'longName',
    component: 'Input',
  },
  {
    label: '两位字母代码',
    field: 'alpha2Code',
    component: 'Input',
  },
  {
    label: '货币单位',
    field: 'currencyUnit',
    component: 'Input',
  },
  {
    label: '特别说明',
    field: 'specialNotes',
    component: 'Input',
  },
  {
    label: '区域(英文)',
    field: 'region',
    component: 'Input',
  },
  {
    label: '区域中文(翻译字段)',
    field: 'regionCn',
    component: 'Input',
  },
  {
    label: '收入分组(英文)',
    field: 'incomeGroup',
    component: 'Input',
  },
  {
    label: '收入分组中文(翻译字段)',
    field: 'incomeGroupCn',
    component: 'Input',
  },
  {
    label: 'WB-2代码',
    field: 'wb2Code',
    component: 'Input',
  },
  {
    label: '国民账户基年',
    field: 'naBaseYear',
    component: 'Input',
  },
  {
    label: '国民账户参考年',
    field: 'naReferenceYear',
    component: 'Input',
  },
  {
    label: 'SNA价格核算方式',
    field: 'snaPriceValuation',
    component: 'Input',
  },
  {
    label: '贷款类别',
    field: 'lendingCategory',
    component: 'Input',
  },
  {
    label: '其他分组',
    field: 'otherGroups',
    component: 'Input',
  },
  {
    label: '国民账户体系',
    field: 'systemOfNationalAccounts',
    component: 'Input',
  },
  {
    label: '备选换算因子',
    field: 'alternativeConversionFactor',
    component: 'Input',
  },
  {
    label: 'PPP调查年份',
    field: 'pppSurveyYear',
    component: 'Input',
  },
  {
    label: '国际收支手册版本',
    field: 'balanceOfPaymentsManual',
    component: 'Input',
  },
  {
    label: '外债报告状态',
    field: 'externalDebtReportStatus',
    component: 'Input',
  },
  {
    label: '贸易体系',
    field: 'systemOfTrade',
    component: 'Input',
  },
  {
    label: '政府会计口径',
    field: 'governmentAccountingConcept',
    component: 'Input',
  },
  {
    label: 'IMF数据公布标准',
    field: 'imfDataDisseminationStandard',
    component: 'Input',
  },
  {
    label: '最近人口普查年份',
    field: 'latestPopulationCensus',
    component: 'Input',
  },
  {
    label: '最近住户调查年份',
    field: 'latestHouseholdSurvey',
    component: 'Input',
  },
  {
    label: '收入支出数据来源',
    field: 'sourceOfIncomeExpenditureData',
    component: 'Input',
  },
  {
    label: '生命登记完整性',
    field: 'vitalRegistrationComplete',
    component: 'Input',
  },
  {
    label: '最近农业普查年份',
    field: 'latestAgriculturalCensus',
    component: 'Input',
  },
  {
    label: '最近工业数据年份',
    field: 'latestIndustrialData',
    component: 'Input',
  },
  {
    label: '最近贸易数据年份',
    field: 'latestTradeData',
    component: 'Input',
  },
  {
    label: '最近取水量数据年份',
    field: 'latestWaterWithdrawalData',
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
  countryCode: {title: '世行国家/经济体代码(ISO3, 如 CHN/WLD)',order: 0,view: 'text', type: 'string',},
  shortName: {title: '简称(英文)',order: 1,view: 'text', type: 'string',},
  countryNameCn: {title: '国家中文名(翻译字段)',order: 2,view: 'text', type: 'string',},
  tableName: {title: '表名',order: 3,view: 'text', type: 'string',},
  longName: {title: '全称(英文)',order: 4,view: 'text', type: 'string',},
  alpha2Code: {title: '两位字母代码',order: 5,view: 'text', type: 'string',},
  currencyUnit: {title: '货币单位',order: 6,view: 'text', type: 'string',},
  specialNotes: {title: '特别说明',order: 7,view: 'text', type: 'string',},
  region: {title: '区域(英文)',order: 8,view: 'text', type: 'string',},
  regionCn: {title: '区域中文(翻译字段)',order: 9,view: 'text', type: 'string',},
  incomeGroup: {title: '收入分组(英文)',order: 10,view: 'text', type: 'string',},
  incomeGroupCn: {title: '收入分组中文(翻译字段)',order: 11,view: 'text', type: 'string',},
  wb2Code: {title: 'WB-2代码',order: 12,view: 'text', type: 'string',},
  naBaseYear: {title: '国民账户基年',order: 13,view: 'text', type: 'string',},
  naReferenceYear: {title: '国民账户参考年',order: 14,view: 'text', type: 'string',},
  snaPriceValuation: {title: 'SNA价格核算方式',order: 15,view: 'text', type: 'string',},
  lendingCategory: {title: '贷款类别',order: 16,view: 'text', type: 'string',},
  otherGroups: {title: '其他分组',order: 17,view: 'text', type: 'string',},
  systemOfNationalAccounts: {title: '国民账户体系',order: 18,view: 'text', type: 'string',},
  alternativeConversionFactor: {title: '备选换算因子',order: 19,view: 'text', type: 'string',},
  pppSurveyYear: {title: 'PPP调查年份',order: 20,view: 'text', type: 'string',},
  balanceOfPaymentsManual: {title: '国际收支手册版本',order: 21,view: 'text', type: 'string',},
  externalDebtReportStatus: {title: '外债报告状态',order: 22,view: 'text', type: 'string',},
  systemOfTrade: {title: '贸易体系',order: 23,view: 'text', type: 'string',},
  governmentAccountingConcept: {title: '政府会计口径',order: 24,view: 'text', type: 'string',},
  imfDataDisseminationStandard: {title: 'IMF数据公布标准',order: 25,view: 'text', type: 'string',},
  latestPopulationCensus: {title: '最近人口普查年份',order: 26,view: 'text', type: 'string',},
  latestHouseholdSurvey: {title: '最近住户调查年份',order: 27,view: 'text', type: 'string',},
  sourceOfIncomeExpenditureData: {title: '收入支出数据来源',order: 28,view: 'text', type: 'string',},
  vitalRegistrationComplete: {title: '生命登记完整性',order: 29,view: 'text', type: 'string',},
  latestAgriculturalCensus: {title: '最近农业普查年份',order: 30,view: 'text', type: 'string',},
  latestIndustrialData: {title: '最近工业数据年份',order: 31,view: 'text', type: 'string',},
  latestTradeData: {title: '最近贸易数据年份',order: 32,view: 'text', type: 'string',},
  latestWaterWithdrawalData: {title: '最近取水量数据年份',order: 33,view: 'text', type: 'string',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}