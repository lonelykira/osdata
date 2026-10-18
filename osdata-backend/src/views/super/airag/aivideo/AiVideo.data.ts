import { FormSchema } from '@/components/Form';
import { h } from 'vue';
import { Button } from 'ant-design-vue';

/**
 * 视频生成表单配置
 */
export const videoFormSchemas: FormSchema[] = [
  // {
  //   label: '模型',
  //   field: 'model',
  //   component: 'JDictSelectTag',
  //   required: true,
  //   defaultValue: 'video-generation-1',
  //   componentProps: {
  //     dictCode: "airag_model where model_type = 'VIDEO' and activate_flag = 1,name,id",
  //     placeholder: '请选择视频生成模型',
  //   },
  // },
  {
    label: '视频尺寸',
    field: 'size',
    component: 'Select',
    defaultValue: '1920x1080',
    componentProps: {
      options: [
        { label: '1280x720 (720P)', value: '1280x720' },
        { label: '720x1280', value: '720x1280' },
        { label: '1024x1024', value: '1024x1024' },
        { label: '1920x1080 (1080P)', value: '1920x1080' },
        { label: '1080x1920', value: '1080x1920' },
        { label: '2048x1080 (2K)', value: '2048x1080' },
        { label: '3840x2160 (4K)', value: '3840x2160' },
      ],
      placeholder: '请选择视频尺寸',
    },
  },
  {
    label: '视频帧率',
    field: 'fps',
    component: 'Select',
    defaultValue: 30,
    componentProps: {
      options: [
        { label: '30 FPS', value: 30 },
        { label: '60 FPS', value: 60 },
      ],
      placeholder: '请选择视频帧率',
    },
  },
  {
    label: '视频时长',
    field: 'duration',
    component: 'Select',
    defaultValue: 5,
    componentProps: {
      options: [
        { label: '5秒', value: 5 },
        { label: '10秒', value: 10 },
      ],
      placeholder: '请选择视频时长',
    },
  },
  {
    label: '是否ai合成音效',
    field: 'izAiAudio',
    component: 'Select',
    defaultValue: 0,
    componentProps: {
      options: [
        { label: '否', value: 0 },
        { label: '是', value: 1 },
      ],
    },  
  }
];
