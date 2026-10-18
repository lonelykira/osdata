import { FormSchema } from '@/components/Form';
import { BasicColumn } from '@/components/Table';

/**
 * 表单
 */
export const formSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: '知识库名称',
    field: 'name',
    required: true,
    componentProps: {
      placeholder: '请输入知识库名称',
      //是否展示字数
      showCount: true,
      maxlength: 64,
    },
    component: 'Input',
  },
  {
    label: '知识库描述',
    field: 'descr',
    component: 'InputTextArea',
    componentProps: {
      placeholder: '描述知识库的内容，详尽的描述将帮助AI能深入理解该知识库的内容，能更准确的检索到内容，提高该知识库的命中率。',
      //是否展示字数
      showCount: true,
      maxlength: 256,
    },
  },
  {
    label: '向量模型',
    field: 'embedId',
    required: true,
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "airag_model where model_type = 'EMBED' and activate_flag = 1,name,id",
    },
  },
  {
    label: '状态',
    field: 'status',
    required: true,
    component: 'JDictSelectTag',
    componentProps: {
      options: [
        { label: '启用', value: 'enable' },
        { label: '禁用', value: 'disable' },
      ],
      type: 'radioButton',
    },
    defaultValue: 'enable',
  },
  {
    label: '类型',
    field: 'type',
    required: true,
    component: 'JDictSelectTag',
    componentProps: {
      options: [
        { label: '知识库', value: 'knowledge' },
        { label: '记忆库', value: 'memory' },
      ],
      type: 'radioButton',
    },
    defaultValue: 'knowledge',
  },
  {
    label: '分段策略',
    field: 'enableSegment',
    component: 'Switch',
    defaultValue: false,
    ifShow: ({ values }) => values.type !== 'memory',
    componentProps: {
      checkedChildren: '开启',
      unCheckedChildren: '关闭',
    },
    helpMessage: '开启后，知识库里面的文档默认使用该分段策略；文档也可单独配置自己的分段策略',
  },
  {
    label: '分段模式',
    field: 'segmentStrategy',
    component: 'RadioGroup',
    defaultValue: 'auto',
    ifShow: ({ values }) => values.type !== 'memory' && values.enableSegment === true,
    componentProps: {
      options: [
        { label: '自动分段与清洗', value: 'auto' },
        { label: '自定义', value: 'custom' },
      ],
    },
  },
  {
    label: '分段标识符',
    field: 'separator',
    component: 'Select',
    defaultValue: '\\n',
    required: true,
    ifShow: ({ values }) => values.type !== 'memory' && values.enableSegment === true && values.segmentStrategy === 'custom',
    componentProps: {
      getPopupContainer: () => document.body,
      options: [
        { label: '换行', value: '\\n' },
        { label: '2个换行', value: '\\n\\n' },
        { label: '中文句号', value: '。' },
        { label: '中文叹号', value: '！' },
        { label: '中文问号', value: '？' },
        { label: '英文句号', value: '.' },
        { label: '英文叹号', value: '!' },
        { label: '英文问号', value: '?' },
        { label: '自定义', value: 'custom' },
      ],
    },
  },
  {
    label: '自定义分隔符',
    field: 'customSeparator',
    component: 'Input',
    required: true,
    ifShow: ({ values }) => values.type !== 'memory' && values.enableSegment === true && values.separator === 'custom' && values.segmentStrategy !== 'auto',
  },
  {
    label: '分段最大长度',
    field: 'maxSegment',
    component: 'InputNumber',
    defaultValue: 800,
    required: true,
    ifShow: ({ values }) => values.type !== 'memory' && values.enableSegment === true,
    componentProps: {
      min: 100,
      max: 5000,
    },
  },
  {
    label: '分段重叠度%',
    field: 'overlap',
    component: 'InputNumber',
    defaultValue: 10,
    required: true,
    ifShow: ({ values }) => values.type !== 'memory' && values.enableSegment === true,
    componentProps: {
      min: 0,
      max: 90,
    },
  },
  {
    label: '文本预处理规则',
    field: 'textRules',
    component: 'CheckboxGroup',
    defaultValue: [],
    ifShow: ({ values }) => values.type !== 'memory' && values.enableSegment === true && values.segmentStrategy === 'custom',
    componentProps: {
      options: [
        { label: '替换掉连续的空格、换行符和制表符', value: 'cleanSpaces' },
        { label: '删除所有 URL 和电子邮箱地址', value: 'removeUrlsEmails' },
      ],
    },
  },
];

//文档文本表单
export const docTextSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: '知识库id',
    field: 'knowledgeId',
    show: false,
    component: 'Input',
  },
  {
    label: '标题',
    field: 'title',
    required: true,
    component: 'Input',
  },
  {
    label: '类型',
    field: 'type',
    required: true,
    component: 'Input',
    show: false
  },
  {
    label: '内容',
    field: 'content',
    rules: [{ required: true, message: '请输入内容' }],
    component: 'JMarkdownEditor',
    componentProps: {
      placeholder: "请输入内容",
      preview:{ mode: 'view', action: [] }
    },
    ifShow:({ values})=>{
      if(values.type === 'text'){
        return true;
      }
      return false;
    }
  },
  {
    label: '文件',
    field: 'filePath',
    rules: [{ required: true, message: '请上传文件' }],
    component: 'JUpload',
    helpMessage:'支持txt、markdown、pdf、docx、xlsx、pptx',
    componentProps:{
      fileType: 'file',
      maxCount: 1,
      multiple: false,
      text: '上传文档'
    },
    ifShow:({ values })=>{
      if(values.type === 'file'){
        return true;
      }
      return false;
    }
  },
  {
    label: '网页地址',
    field: 'website',
    rules: [
      { required: true, message: '请输入网页URL' },
      { pattern: /^https?:\/\//, message: '请输入正确的网页地址，以http://或https://开头' },
    ],
    component: 'Input',
    componentProps: {
      placeholder: '请输入网页URL，例如：https://help.jeecg.com/',
    },
    ifShow:({ values })=>{
      if(values.type === 'web'){
        return true;
      }
      return false;
    }
  },
];

/**
 * 分段策略表单
 */
export const docSegmentSchema: FormSchema[] = [
  {
    label: '分段策略',
    field: 'segmentStrategy',
    component: 'RadioGroup',
    defaultValue: 'auto',
    componentProps: {
      options: [
        { label: '自动分段与清洗', value: 'auto' },
        { label: '自定义', value: 'custom' },
      ],
    },
  },
  {
    label: '分段标识符',
    field: 'separator',
    component: 'Select',
    defaultValue: '\\n',
    required: true,
    ifShow: ({ values }) => values.segmentStrategy === 'custom',
    componentProps: {
      getPopupContainer: () => document.body,
      options: [
        { label: '换行', value: '\\n' },
        { label: '2个换行', value: '\\n\\n' },
        { label: '中文句号', value: '。' },
        { label: '中文叹号', value: '！' },
        { label: '中文问号', value: '？' },
        { label: '英文句号', value: '.' },
        { label: '英文叹号', value: '!' },
        { label: '英文问号', value: '?' },
        { label: '自定义', value: 'custom' },
      ],
    },
  },
  {
    label: '',
    field: 'customSeparator',
    component: 'Input',
    required: true,
    ifShow: ({ values }) => values.separator === 'custom' && values.segmentStrategy !== 'auto',
  },
  {
    label: '分段最大长度',
    field: 'maxSegment',
    component: 'InputNumber',
    defaultValue: 800,
    required: true,
    componentProps: {
      min: 100,
      max: 5000,
    },
  },
  {
    label: '分段重叠度%',
    field: 'overlap',
    component: 'InputNumber',
    defaultValue: 10,
    componentProps: {
      min: 0,
      max: 90,
    },
    required: true,
  },
  {
    label: '文本预处理规则',
    field: 'textRules',
    component: 'CheckboxGroup',
    defaultValue: [],
    ifShow: ({ values }) => values.segmentStrategy === 'custom',
    componentProps: {
      options: [
        { label: '替换掉连续的空格、换行符和制表符', value: 'cleanSpaces' },
        { label: '删除所有 URL 和电子邮箱地址', value: 'removeUrlsEmails' },
      ],
    },
  },
];

