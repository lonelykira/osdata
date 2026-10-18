import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '名称',
    align: 'center',
    dataIndex: 'name',
  },
  {
    title: '功能描述',
    align: 'center',
    dataIndex: 'description',
  },
  // {
  //   title: '状态',
  //   align: 'center',
  //   dataIndex: 'status',
  // },
  // {
  //   title: '最近提交人',
  //   align: 'center',
  //   dataIndex: 'updateBy',
  // },
  // {
  //   title: '最近提交时间',
  //   align: 'center',
  //   dataIndex: 'updateTime',
  // },
  // {
  //   title: '创建时间',
  //   align: 'center',
  //   dataIndex: 'createTime',
  // }
];
//回收站列表数据
export const recycleColumns: BasicColumn[] = [
  {
    title: '名称',
    align: 'center',
    dataIndex: 'name',
  },
  {
    title: '功能描述',
    align: 'center',
    dataIndex: 'description',
  },
  {
    title: '创建人',
    align: 'center',
    dataIndex: 'createBy',
  },
  {
    title: '创建时间',
    align: 'center',
    dataIndex: 'createTime',
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
    {
      label: '名称',
      field: 'name',
      component: 'JInput',
    },
];
// 名称最大长度
export const NAME_MAX_LENGTH = 40;
// 编码最大长度
export const CODE_MAX_LENGTH = 50;
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '名称',
    field: 'name',
    component: 'Input',
    componentProps: () => {
      return {
        placeholder: '例如：SQL转换',
        maxlength: 40,
        showCount: true
      };
    },
    dynamicRules() {
      return [
        {required: true, message: '请输入提示词名称'},
        {
          max: NAME_MAX_LENGTH,
          message: `名称长度不能超过${NAME_MAX_LENGTH}个字符`,
        },
      ];
    }
  },
  {
    label: '提示词功能描述',
    field: 'description',
    component: 'InputTextArea',
    componentProps: {
      rows: 5,
    },
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
];

/**
 * 流程表单调用这个方法获取formSchema
 * @param param
 */
export function getBpmFormSchema(_formData): FormSchema[] {
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}
