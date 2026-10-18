import { BasicColumn, FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';

export const columns: BasicColumn[] = [
  // {
  //   title: '职务编码',
  //   dataIndex: 'code',
  //   width: 200,
  //   align: 'left',
  // },
  {
    title: '职务等级',
    dataIndex: 'name',
    align: 'left'
    // width: 200,
  },
  {
    title: '等级序号（数字越小，职级越高）',
    dataIndex: 'postLevel',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'name',
    label: '职务等级',
    component: 'JInput',
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: '主键',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'name',
    label: '职务等级',
    component: 'Input',
    required: true,
  },
  {
    label: '等级序号',
    field: 'postLevel',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 1,
      max: 99
    },
    itemProps: {
      extra: '数字越小，职级越高；流程仅识别 1—6 级预设职级，其他等级不能用于流程职级表达式。',
    },
    dynamicRules: ({ model, schema }) => {
      return [{ required: true, message: '请输入等级序号!' }];
    },
  },
  // {
  //   field: 'code',
  //   label: '职务编码',
  //   component: 'Input',
  //   required: true,
  //   dynamicDisabled: ({ values }) => {
  //     return !!values.id;
  //   },
  //   dynamicRules: ({ model, schema }) => {
  //     return rules.duplicateCheckRule('sys_position', 'code', model, schema, true);
  //   },
  // },
];
