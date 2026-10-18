import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import {JVxeTypes,JVxeColumn} from '/@/components/jeecg/JVxeTable/types'
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '接口名称',
    align:"center",
    dataIndex: 'name'
   },
   {
    title: '接口地址',
    align:"center",
    dataIndex: 'requestUrl',
    width: 120,
   },
   {
    title: '请求方式',
    align:"center",
    dataIndex: 'requestMethod',
    width: 100,
   },
   {
    title: '原始接口',
    align:"center",
    dataIndex: 'originUrl',
    ellipsis: true,
   },
   {
    title: 'IP 白名单',
    align:"center",
    dataIndex: 'whiteList',
    ellipsis: true,
    customRender: ({ text }) => {
      if (!text) return '不限制';
      const count = text.split(/[,\n]/).filter(item => item.trim()).length;
      return count + ' 条规则';
    }
   },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: "接口名称",
    field: "name",
    component: 'JInput',
  },
  {
    label: "接口地址",
    field: "requestUrl",
    component: 'JInput',
  },
];
// 请求方式选项（供表单 slot 复用）
export const requestMethodOptions = [
  { label: 'POST', value: 'POST' },
  { label: 'GET', value: 'GET' },
  { label: 'HEAD', value: 'HEAD' },
  { label: 'PUT', value: 'PUT' },
  { label: 'PATCH', value: 'PATCH' },
  { label: 'DELETE', value: 'DELETE' },
  { label: 'OPTIONS', value: 'OPTIONS' },
  { label: 'TRACE', value: 'TRACE' },
];

//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '接口名称',
    field: 'name',
    component: 'Input',
    dynamicRules: () => {
          return [
                 { required: true, message: '请输入接口名称!'},
          ];
     },
  },
  {
    label: '接口地址',
    field: 'requestUrl',
    component: 'Input',
    slot: 'requestUrlSlot',
    dynamicDisabled: true,
  },
  {
    label: '请求方式',
    field: 'requestMethod',
    defaultValue:'GET',
    component: 'Input',
    show: false,
  },
  {
    label: '原始接口',
    field: 'originUrl',
    component: 'Input',
    componentProps: {
      placeholder: '当前系统的原始接口地址，如 /sys/user/list',
    },
    helpMessage: '当前系统中被代理的原始接口路径',
    colProps: { span: 24 },
    dynamicRules: () => {
      return [
        { required: true, message: '请输入原始接口路径!' },
        {
          validator: (_, value) => {
            if (value && !value.startsWith('/')) {
              return Promise.reject('原始接口路径必须以 / 开头');
            }
            if (value && value.includes('//')) {
              return Promise.reject('原始接口路径不能包含 //');
            }
            if (value && value.includes('..')) {
              return Promise.reject('原始接口路径不能包含 ..');
            }
            return Promise.resolve();
          },
        },
      ];
    },
  },

  {
    label: 'IP 白名单',
    field: 'whiteList',
    helpMessage: '支持精确IP、CIDR网段（如192.168.1.0/24）、通配符（如10.2.3.*），每行一个或逗号分隔，为空则不限制',
    component: 'InputTextArea',
    slot: 'whiteListSlot',
    componentProps: {
      rows: 5,
      placeholder: '示例：\n192.168.1.100\n10.0.0.0/8\n172.16.*.*',
    },
    colProps: { span: 24 },
  },
  {
    label: '备注',
    field: 'remarks',
    component: 'InputTextArea',
    componentProps: {
      rows: 2,
      placeholder: '请输入白名单备注说明',
    },
    colProps: { span: 24 },
  },
  {
    label: '接口描述',
    field: 'description',
    component: 'InputTextArea',
    componentProps: {
      rows: 3,
      placeholder: '请输入接口描述',
    },
    colProps: { span: 24 },
  },
  {
    label: '删除标识',
    field: 'delFlag',
    component: 'Input',
    defaultValue:0,
    show:false
  },
  {
    label: '状态',
    field: 'status',
    component: 'Input',
    defaultValue:"1",
    show:false
  },
	// TODO 主键隐藏字段，目前写死为ID
	{
	  label: '',
	  field: 'id',
	  component: 'Input',
	  show: false
	},
];
//子表单数据
//子表列表数据
export const openApiHeaderColumns: BasicColumn[] = [
   // {
   //  title: 'apiId',
   //  align:"center",
   //  dataIndex: 'apiId'
   // },
   {
    title: '请求头Key',
    align:"center",
    dataIndex: 'headerKey'
   },
   {
    title: '是否必填',
    align:"center",
    dataIndex: 'required_dictText'
   },
   {
    title: '默认值',
    align:"center",
    dataIndex: 'defaultValue'
   },
   {
    title: '备注',
    align:"center",
    dataIndex: 'note'
   },
];
//子表列表数据
export const openApiParamColumns: BasicColumn[] = [
   // {
   //  title: 'apiId',
   //  align:"center",
   //  dataIndex: 'apiId'
   // },
   {
    title: '参数Key',
    align:"center",
    dataIndex: 'paramKey'
   },
   {
    title: '是否必填',
    align:"center",
    dataIndex: 'required_dictText'
   },
   {
    title: '默认值',
    align:"center",
    dataIndex: 'defaultValue'
   },
   {
    title: '备注',
    align:"center",
    dataIndex: 'note'
   },
];
//子表表格配置
// update-begin--author:liaozhiyang---date:20260804---for：【LHZP-1266】默认请求头 appKey/signature/timestamp 删除时校验拦截且必填禁用
export const FIXED_HEADER_KEYS = ['appKey', 'signature', 'timestamp'];
// update-end--author:liaozhiyang---date:20260804---for：【LHZP-1266】默认请求头 appKey/signature/timestamp 删除时校验拦截且必填禁用
export const openApiHeaderJVxeColumns: JVxeColumn[] = [
    // {
    //   title: 'apiId',
    //   key: 'apiId',
    //   type: JVxeTypes.input,
    //   width:"200px",
    //   placeholder: '请输入${title}',
    //   defaultValue:'',
    // },
    {
      title: '请求头Key',
      key: 'headerKey',
      type: JVxeTypes.input,
      width:"200px",
      placeholder: '请输入${title}',
      defaultValue:'',
    },
    {
      title: '是否必填',
      key: 'required',
      type: JVxeTypes.checkbox,
      options:[],
      // dictCode:"yn",
      width:"100px",
      placeholder: '请输入${title}',
      defaultValue:'',
      customValue: ['1','0'],
      // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-1266】默认请求头 appKey/signature/timestamp 删除时校验拦截且必填禁用
      props: {
        isDisabledCell: ({ row }) => FIXED_HEADER_KEYS.includes(row.headerKey),
      },
      // update-end--author:liaozhiyang---date:20260804---for：【LHZP-1266】默认请求头 appKey/signature/timestamp 删除时校验拦截且必填禁用
    },
    {
      title: '参数类型',
      key: 'paramType',
      type: JVxeTypes.select,
      width: '120px',
      options: [
        { title: 'string', value: 'string' },
        { title: 'integer', value: 'integer' },
        { title: 'number', value: 'number' },
        { title: 'boolean', value: 'boolean' },
        { title: 'array', value: 'array' },
        { title: 'object', value: 'object' },
      ],
      defaultValue: 'string',
    },
    {
      title: '默认值',
      key: 'defaultValue',
      type: JVxeTypes.input,
      width:"200px",
      placeholder: '请输入${title}',
      defaultValue:'',
    },
    {
      title: '示例值',
      key: 'example',
      type: JVxeTypes.input,
      width: '200px',
      placeholder: '请输入${title}',
      defaultValue: '',
    },
    {
      title: '备注',
      key: 'note',
      type: JVxeTypes.input,
      placeholder: '请输入${title}',
      defaultValue:'',
    },
  ]
export const openApiParamJVxeColumns: JVxeColumn[] = [
    // {
    //   title: 'apiId',
    //   key: 'apiId',
    //   type: JVxeTypes.input,
    //   width:"200px",
    //   placeholder: '请输入${title}',
    //   defaultValue:'',
    // },
    {
      title: '参数Key',
      key: 'paramKey',
      type: JVxeTypes.input,
      width:"200px",
      placeholder: '请输入${title}',
      defaultValue:'',
    },
    {
      title: '是否必填',
      key: 'required',
      type: JVxeTypes.checkbox,
      options:[],
      // dictCode:"yn",
      width:"100px",
      placeholder: '请输入${title}',
      defaultValue:'',
      customValue: ['1','0']
    },
    {
      title: '参数类型',
      key: 'paramType',
      type: JVxeTypes.select,
      width: '120px',
      options: [
        { title: 'string', value: 'string' },
        { title: 'integer', value: 'integer' },
        { title: 'number', value: 'number' },
        { title: 'boolean', value: 'boolean' },
        { title: 'array', value: 'array' },
        { title: 'object', value: 'object' },
      ],
      defaultValue: 'string',
    },
    {
      title: '默认值',
      key: 'defaultValue',
      type: JVxeTypes.input,
      width:"200px",
      placeholder: '请输入${title}',
      defaultValue:'',
    },
    {
      title: '示例值',
      key: 'example',
      type: JVxeTypes.input,
      width: '200px',
      placeholder: '请输入${title}',
      defaultValue: '',
    },
    {
      title: '备注',
      key: 'note',
      type: JVxeTypes.input,
      placeholder: '请输入${title}',
      defaultValue:'',
    },
  ]

export const responseFieldJVxeColumns: JVxeColumn[] = [
  {
    title: '字段名',
    key: 'fieldName',
    type: JVxeTypes.input,
    width: '200px',
    placeholder: '请输入${title}',
    defaultValue: '',
  },
  {
    title: '类型',
    key: 'fieldType',
    type: JVxeTypes.select,
    width: '120px',
    options: [
      { title: 'string', value: 'string' },
      { title: 'integer', value: 'integer' },
      { title: 'number', value: 'number' },
      { title: 'boolean', value: 'boolean' },
      { title: 'array', value: 'array' },
      { title: 'object', value: 'object' },
    ],
    defaultValue: 'string',
  },
  {
    title: '说明',
    key: 'fieldDesc',
    type: JVxeTypes.input,
    placeholder: '请输入${title}',
    defaultValue: '',
  },
];

// 高级查询数据
export const superQuerySchema = {
  name: {title: '接口名称',order: 0,view: 'text', type: 'string',},
  requestMethod: {title: '请求方式',order: 1,view: 'list', type: 'string',dictCode: '',},
  requestUrl: {title: '接口地址',order: 2,view: 'text', type: 'string',},
  whiteList: {title: 'IP 白名单',order: 3,view: 'text', type: 'string',},
  status: {title: '状态',order: 5,view: 'number', type: 'number',},
  createBy: {title: '创建人',order: 6,view: 'text', type: 'string',},
  createTime: {title: '创建时间',order: 7,view: 'datetime', type: 'string',},
  //子表高级查询
  openApiHeader: {
    title: '请求头表',
    view: 'table',
    fields: {
        // apiId: {title: 'apiId',order: 0,view: 'text', type: 'string',},
        headerKey: {title: '请求头Key',order: 1,view: 'text', type: 'string',},
        required: {title: '是否必填',order: 2,view: 'number', type: 'number',dictCode: 'yn',},
        defaultValue: {title: '默认值',order: 3,view: 'text', type: 'string',},
        note: {title: '备注',order: 4,view: 'text', type: 'string',},
    }
  },
  openApiParam: {
    title: '请求参数部分',
    view: 'table',
    fields: {
        // apiId: {title: 'apiId',order: 0,view: 'text', type: 'string',},
        paramKey: {title: '参数Key',order: 1,view: 'text', type: 'string',},
        required: {title: '是否必填',order: 2,view: 'number', type: 'number',dictCode: 'yn',},
        defaultValue: {title: '默认值',order: 3,view: 'text', type: 'string',},
        note: {title: '备注',order: 4,view: 'text', type: 'string',},
    }
  },
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}
