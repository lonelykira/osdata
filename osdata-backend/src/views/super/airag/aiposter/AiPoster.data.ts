import { FormSchema } from '@/components/Form';

export const formSchema: FormSchema[] = [
  {
    field: 'drawModelId',
    label: '模型',
    component: 'JDictSelectTag',
    required: true,
    helpMessage: [
      '1、需要选择在模型中已有的图像模型',
      '2、智普语言模型不支持尺寸设置',
      "3、openAi旧版模型如(dall-e-2)需要选择尺寸，新版模型直接输入'竖版: 9:16即可'",
      '4、当前只有千问万象模型(wanx2.1-imageedit,wan2.5-i2i-preview)支持图生图',
      '5、wan2.5-i2i-preview支持多张图片',
      '6、当前文生图openAi效果最佳',
    ],
    componentProps: {
      dictCode: "airag_model where model_type = 'IMAGE' and activate_flag = 1,name,id",
    },
  },
  {
    field: 'content',
    label: '提示词',
    component: 'InputTextArea',
    required: true,
    componentProps: {
      rows: 10,
      placeholder: '请输入提示词，例如：一只可爱的猫咪，赛博朋克风格',
    },
  },
  {
    field: 'imageUrl',
    label: '参考图',
    component: 'JImageUpload',
    componentProps: {
      fileMax: 2,
    },
  },
  {
    field: 'imageSize',
    label: '图片尺寸',
    component: 'Select',
    defaultValue: '720*1280',
    componentProps: {
      options: [
        { label: '1:1 (1024x1024)', value: '1024*1024' },
        { label: '16:9 (1280x720)', value: '1280*720' },
        { label: '9:16 (720x1280)', value: '720*1280' },
        { label: '4:3 (1024x768)', value: '1024*768' },
        { label: '3:4 (768x1024)', value: '768*1024' },
      ],
    },
  },
];

/**
 * 混图表单
 */
export const mixFormSchema: FormSchema[] = [
  {
    field: 'drawModelId',
    label: '模型',
    component: 'JDictSelectTag',
    required: true,
    helpMessage: [
      '1、需要选择在模型中已有的图像模型',
      '2、当前支持通义万象模型wan2.5-i2i-preview',
    ],
    componentProps: {
      dictCode: "airag_model where model_type = 'IMAGE' and activate_flag = 1,name,id",
    },
  },
  {
    field: 'imageSize',
    label: '尺寸',
    component: 'Select',
    defaultValue: '720*1280',
    componentProps: {
      options: [
          { label: '1:1 (1024x1024)', value: '1024*1024' },
          { label: '16:9 (1280x720)', value: '1280*720' },
          { label: '9:16 (720x1280)', value: '720*1280' },
          { label: '4:3 (1024x768)', value: '1024*768' },
          { label: '3:4 (768x1024)', value: '768*1024' },
      ],
    },
  },
  {
    field: 'imageUrl',
    label: '上传图像',
    component: 'JImageUpload',
    required: true,
    componentProps: {
      fileMax: 3,
      text: '上传图像',
    },
    rules: [
      {
        required: true,
        validator: async (_, value) => {
          if (!value) {
            return Promise.reject('请上传图像');
          }
          const images = value.split(',');
          if (images.length < 2) {
            return Promise.reject('合成至少2张图片');
          }
          return Promise.resolve();
        },
      },
    ],
  },
  {
    field: 'content',
    label: '提示词',
    component: 'InputTextArea',
    defaultValue:
      '以图2作为唯一的主体底图和最终画面，只将图1人物所穿的服装转移到图2人物身上。必须完整保留图2人物的身份、人脸、五官、发型、体型、身体姿态以及图2的构图、环境和背景，不得使用或复制图1的人脸、头部、身体姿态、人物身份和背景。图1仅作为服装参考，准确还原其服装款式、颜色、纹理和材质，使服装自然贴合图2人物，保持图2原有光影和画面风格。最终结果必须是图2中的人穿上图1的衣服，而不是生成图1中的人物。高分辨率，写实风格',
    componentProps: {
      rows: 4,
      placeholder: '如将图一的话花瓶放到图二中',
    },
  },
];

/**
 * 绘画的表单
 */
export const drawFormSchema: FormSchema[] = [
  {
    field: 'drawModelId',
    label: '模型',
    component: 'JDictSelectTag',
    required: true,
    helpMessage: [
      '1、需要选择在模型中已有的图像模型',
      '2、智普语言模型不支持尺寸设置',
      "3、openAi旧版模型如(dall-e-2)需要选择尺寸，新版模型直接输入'竖版: 9:16即可'",
      '4、当前只有千问万象模型(wanx2.1-imageedit,wan2.5-i2i-preview)支持图生图',
      '5、wan2.5-i2i-preview支持多张图片',
      '6、当前文生图openAi效果最佳',
    ],
    componentProps: {
      dictCode: "airag_model where model_type = 'IMAGE' and activate_flag = 1,name,id",
    },
  },
  {
    field: 'content',
    label: '提示词',
    component: 'InputTextArea',
    required: true,
    componentProps: {
      rows: 5,
      placeholder: '请输入提示词，例如：一只可爱的猫咪',
    },
  },
  {
    field: 'imageSize',
    label: '图片尺寸',
    component: 'Select',
    defaultValue: '720*1280',
    componentProps: {
      options: [
        { label: '1:1 (1024x1024)', value: '1024*1024' },
        { label: '16:9 (1280x720)', value: '1280*720' },
        { label: '9:16 (720x1280)', value: '720*1280' },
        { label: '4:3 (1024x768)', value: '1024*768' },
        { label: '3:4 (768x1024)', value: '768*1024' },
      ],
    },
  },
  {
    field: 'style',
    label: '风格',
    component: 'Select',
    defaultValue: 'modernOrganic',
    componentProps: {
      options: [
        { label: '赛博朋克', value: 'cyberpunk' },
        { label: '星际', value: 'star' },
        { label: '动漫', value: 'anime' },
        { label: '日本漫画', value: 'japaneseComicsManga' },
        { label: '水墨画风格', value: 'inkWashPaintingStyle' },
        { label: '原创', value: 'original' },
        { label: '风景画', value: 'landscape' },
        { label: '插画', value: 'illustration' },
        { label: '漫画', value: 'manga' },
        { label: '现代自然', value: 'modernOrganic' },
        { label: '创世纪', value: 'genesis' },
        { label: '海报风格', value: 'posterstyle' },
        { label: '超现实主义', value: 'surrealism' },
        { label: '素描', value: 'sketch' },
        { label: '写实', value: 'realism' },
        { label: '水彩画', value: 'watercolorPainting' },
        { label: '立体主义', value: 'cubism' },
        { label: '黑白', value: 'blackAndWhite' },
        { label: '胶片摄影风格', value: 'fmPhotography' },
        { label: '电影化', value: 'cinematic' },
        { label: '清晰的面部特征', value: 'clearFacialFeatures' },
      ],
    },
  },
  {
    field: 'visualAngle',
    label: '视角',
    component: 'Select',
    defaultValue: 'frontView',
    componentProps: {
      options: [
        { label: '宽视角', value: 'wideView' },
        { label: '鸟瞰视角', value: 'birdView' },
        { label: '顶视角', value: 'topView' },
        { label: '仰视角', value: 'upview' },
        { label: '正面视角', value: 'frontView' },
        { label: '头部特写', value: 'headshot' },
        { label: '超广角视角', value: 'ultrawideshot' },
        { label: '中景', value: 'mediumShot' },
        { label: '远景', value: 'longShot' },
        { label: '景深', value: 'depthOfField' },
      ],
    },
  },
  {
    field: 'characterShot',
    label: '人物镜头',
    component: 'Select',
    defaultValue: 'fullLengthShot',
    componentProps: {
      options: [
        { label: '脸部特写', value: 'faceShot' },
        { label: '大特写', value: 'bigCloseUp' },
        { label: '特写', value: 'closeUp' },
        { label: '腰部以上', value: 'waistShot' },
        { label: '膝盖以上', value: 'kneeShot' },
        { label: '全身照', value: 'fullLengthShot' },
        { label: '极远景', value: 'extraLongShot' },
      ],
    },
  },
  {
    field: 'lighting',
    label: '灯光',
    component: 'Select',
    defaultValue: 'naturalLight',
    componentProps: {
      options: [
        { label: '冷光', value: 'coldLight' },
        { label: '暖光', value: 'warmLight' },
        { label: '硬光', value: 'hardLighting' },
        { label: '戏剧性光线', value: 'dramaticLight' },
        { label: '反射光', value: 'reflectionLight' },
        { label: '薄雾', value: 'mistyFoggy' },
        { label: '自然光', value: 'naturalLight' },
        { label: '阳光', value: 'sunLight' },
        { label: '情绪化', value: 'moody' },
      ],
    },
  },
];

/**
 * 换脸表单
 */
export const faceSwapFormSchema: FormSchema[] = [
  {
    field: 'drawModelId',
    label: '模型',
    component: 'JDictSelectTag',
    required: true,
    helpMessage: [
      '1、需要选择在模型中已有的图像模型',
      '2、当前只支持通义万象模型(wan2.5-i2i-preview)'
    ],
    componentProps: {
      dictCode: "airag_model where model_type = 'IMAGE' and activate_flag = 1,name,id",
    },
  },
  {
    field: 'sourceImage',
    label: '你的头像',
    component: 'JImageUpload',
    required: true,
    componentProps: {
      fileMax: 1,
      text: '上传头像',
    },
  },
  {
    field: 'targetImage',
    label: '明星图',
    component: 'JImageUpload',
    required: true,
    componentProps: {
      fileMax: 1,
      text: '上传明星图',
    },
  },
  {
    field: 'content',
    label: '提示词',
    component: 'InputTextArea',
    required: true,
    defaultValue:
      '将图2的人脸替换到图1的人脸区域，保留图2的面部特征和五官细节，保持图1的身体姿态、发型、服装、构图和背景不变，面部融合自然，光影与肤色协调，高分辨率，写实风格',
    componentProps: {
      rows: 4,
      placeholder: '请输入换脸提示词',
    },
  },
  {
    field: 'imageSize',
    label: '图片尺寸',
    component: 'Select',
    defaultValue: '720*1280',
    componentProps: {
      options: [
        { label: '1:1 (1024x1024)', value: '1024*1024' },
        { label: '16:9 (1280x720)', value: '1280*720' },
        { label: '9:16 (720x1280)', value: '720*1280' },
        { label: '4:3 (1024x768)', value: '1024*768' },
        { label: '3:4 (768x1024)', value: '768*1024' },
      ],
    },
  },
];
