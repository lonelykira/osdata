import { FormSchema } from '@/components/Form';

/**
 * AI换衣 - 生成图片表单
 */
export const clothImageFormSchema: FormSchema[] = [
  {
    field: 'drawModelId',
    label: '模型',
    component: 'JDictSelectTag',
    required: true,
    helpMessage: ['1、需要选择已激活的图像模型', '2、当前推荐通义万象模型 (wan2.5-i2i-preview)', '3、建议上传清晰的模特图和服装图以获得最佳效果'],
    componentProps: {
      dictCode: "airag_model where model_type = 'IMAGE' and activate_flag = 1,name,id",
      placeholder: '请选择图像模型',
    },
  },
  {
    field: 'modelImage',
    label: '模特图片',
    component: 'JImageUpload',
    required: true,
    componentProps: {
      fileMax: 1,
      text: '上传模特',
    },
    helpMessage: ['上传模特图片，建议使用全身照，正面清晰'],
  },

  {
    field: 'clothUpload',
    label: '服装',
    slot: 'clothUpload',
    component: 'Input',
    required: false,
  },
  {
    field: 'userPrompt',
    label: '提示词',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
      placeholder: '在此输入你的提示词，或使用示例快速填充',
    },
    required: true,
  },
];

/**
 * AI换衣 - 生成视频表单
 */
export const clothVideoFormSchema: FormSchema[] = [
  {
    field: 'drawModelId',
    label: '模型',
    component: 'JDictSelectTag',
    required: true,
    helpMessage: ['1、需要选择已激活的视频模型', '2、建议选择支持图生视频的模型'],
    componentProps: {
      dictCode: "airag_model where model_type = 'VIDEO' and activate_flag = 1,name,id",
      placeholder: '请选择视频模型',
    },
  },
  {
    field: 'modelImage',
    label: '模特图片',
    component: 'JImageUpload',
    componentProps: {
      fileMax: 1,
      text: '上传模特',
    },
    helpMessage: ['上传模特图片，建议使用全身照，正面清晰'],
  },
  {
    field: 'clothUpload',
    label: '',
    slot: 'clothUpload',
    component: 'Input',
    required: false,
  },
  {
    field: 'userPrompt',
    label: '自定义提示词',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
      placeholder: '在此输入你的提示词，或使用下方示例快速填充',
    },
    required: false,
  },
];
