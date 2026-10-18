import { FormSchema } from '@/components/Form';

// 左侧语音控制表单
export const voiceFormSchemas: FormSchema[] = [
  /*  {
    label: '模型',
    field: 'model',
    component: 'JDictSelectTag',
    required: true,
    defaultValue: 'voice-generation-1',
    componentProps: {
      placeholder: '请选择语音模型',
      dictCode: "airag_model where model_type = 'VOICE' and activate_flag = 1,name,id",
    },
  },*/
  {
    label: '倍速',
    field: 'speed',
    component: 'Slider',
    defaultValue: 1,
    colProps: {
      span: 24,
    },
    componentProps: {
      min: 0.25,
      max: 4,
      step: 0.1,
      marks: {
        0.5: '0.5x',
        1: '1x',
        1.5: '1.5x',
        2: '2x',
        3: '3x',
        4: '4x',
      },
      tooltip: {
        formatter: (value: number) => `${value.toFixed(1)}x`,
      },
    },
  },
  {
    label: '音量增益(dB)',
    field: 'volume',
    component: 'Slider',
    defaultValue: 0,
    colProps: {
      span: 24,
    },
    componentProps: {
      min: -10,
      max: 10,
      step: 1,
      marks: {
        '-10': '-10',
        0: '0',
        10: '10',
      },
    },
  },
  {
    label: '声色',
    field: 'voice',
    component: 'Select',
    required: true,
    defaultValue: 'tongtong',
    componentProps: {
      options: [
        { label: '彤彤', value: 'tongtong' },
        { label: '锤锤', value: 'chuichui' },
        { label: '小陈', value: 'xiaochen' },
        { label: 'Jam', value: 'jam' },
        { label: 'Kazi', value: 'kazi' },
        { label: 'Douji', value: 'douji' },
        { label: 'Luodo', value: 'luodo' },
      ],
      placeholder: '请选择声色',
    },
  },
  {
    label: '文案',
    field: 'text',
    component: 'InputTextArea',
    required: true,
    colProps: {
      span: 24,
    },
    componentProps: {
      rows: 6,
      placeholder: '请输入要合成的文案内容',
      maxlength: 500,
      showCount: true,
    },
  },
];

/**
 * 历史记录表格列配置
 */
export const historyColumns = [
  {
    title: '文案',
    dataIndex: 'text',
    width: 100,
    ellipsis: true,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 100,
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 100,
    fixed: 'right',
  },
];
