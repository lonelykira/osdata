import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: '调用时间',
    dataIndex: 'callTime',
    align: 'center',
    width: 170,
  },
  {
    title: '请求方式',
    dataIndex: 'requestMethod',
    align: 'center',
    width: 90,
  },
  {
    title: '请求路径',
    dataIndex: 'requestPath',
    align: 'left',
    width: 220,
    ellipsis: true,
  },
  {
    title: '调用者AK',
    dataIndex: 'callerAk',
    align: 'center',
    width: 180,
    ellipsis: true,
  },
  {
    title: '来源IP',
    dataIndex: 'ip',
    align: 'center',
    width: 140,
  },
  {
    title: '状态码',
    dataIndex: 'responseCode',
    align: 'center',
    width: 90,
  }
];

export const searchFormSchema: FormSchema[] = [
  {
    label: '调用者AK',
    field: 'callerAk',
    component: 'JInput',
  },
  {
    label: '来源IP',
    field: 'ip',
    component: 'JInput',
  },
  {
    label: '请求路径',
    field: 'requestPath',
    component: 'JInput',
  },
  {
    label: '请求方式',
    field: 'requestMethod',
    component: 'JSearchSelect',
    componentProps: {
      dictOptions: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH','HEAD','OPTIONS','TRACE'].map((v) => ({ text: v, value: v })),
    },
  },
  {
    label: '响应状态码',
    field: 'responseCode',
    component: 'Input',
  },
  {
    label: '调用时间',
    field: 'callTime',
    component: 'RangePicker',
    componentProps: { valueType: 'Date', showTime: true },
  },
];
