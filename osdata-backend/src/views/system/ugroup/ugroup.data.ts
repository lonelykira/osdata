import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
// 名称最大长度
export const NAME_MAX_LENGTH = 40;

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '用户组名称',
    align: 'center',
    dataIndex: 'groupName',
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
    label: '用户组名称',
    field: 'groupName',
    component: 'JInput',
  },
];

//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '用户组名称',
    field: 'groupName',
    component: 'Input',
    dynamicRules() {
      return [
        {required: true, message: '请输入用户组名称'},
        {
          max: NAME_MAX_LENGTH,
          message: `名称长度不能超过${NAME_MAX_LENGTH}个字符`,
        },
      ];
    }
  },
  {
    label: '描述',
    field: 'description',
    component: 'InputTextArea',
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
];

/**
 * 角色用户搜索form
 */
export const searchUserFormSchema: FormSchema[] = [
  {
    field: 'username',
    label: '用户账号',
    component: 'Input',
    colProps: { span: 8 },
    labelWidth: 74,
  },
  {
    field: 'realname',
    label: '用户名称',
    component: 'Input',
    colProps: { span: 8 },
    labelWidth: 74,
  },
];
/**
 * 角色用户Columns
 */
export const userColumns = [
  {
    title: '用户账号',
    dataIndex: 'username',
  },
  {
    title: '用户姓名',
    dataIndex: 'realname',
  },
  {
    title: '状态',
    dataIndex: 'status_dictText',
    width: 80,
  },
];
