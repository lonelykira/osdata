import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '授权对象',
    align: 'center',
    dataIndex: 'name',
  },
  {
    title: '访问密钥（AK）',
    align: 'center',
    dataIndex: 'ak',
    ellipsis: true,
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
    label: '授权对象',
    field: 'name',
    component: 'JInput',
  },
  {
    label: '访问密钥',
    field: 'ak',
    component: 'JInput',
  },
];

//授权表单数据
export const authFormSchema: FormSchema[] = [
  {
    label: '授权对象',
    field: 'name',
    component: 'Input',
    required: true,
  },
  {
    label: '',
    field: 'ak',
    component: 'Input',
    show: false,
  },
  {
    label: '',
    field: 'sk',
    component: 'Input',
    show: false,
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: '',
    field: 'systemUserId',
    component: 'Input',
    show: false,
  },
];

// 高级查询数据
export const superQuerySchema = {
  name: { title: '授权对象', order: 0, view: 'text', type: 'string' },
  ak: { title: '访问密钥（AK）', order: 1, view: 'text', type: 'string' },
  sk: { title: '签名密钥（SK）', order: 2, view: 'text', type: 'string' },
  createBy: { title: '创建人', order: 3, view: 'text', type: 'string' },
  createTime: { title: '创建时间', order: 4, view: 'datetime', type: 'string' },
};
