import { BasicColumn } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: '配置项',
    dataIndex: 'key',
    width: 120,
    align: 'left',
    customRender: ({ text }) => {
      return text;
    },
  },
  {
    title: '说明',
    dataIndex: 'description',
    width: 200,
    align: 'left',
    ellipsis: true,
  },
  {
    title: '值',
    dataIndex: 'value',
    width: 80,
    align: 'right',
  },
];
