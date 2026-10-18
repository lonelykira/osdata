import { BasicColumn, FormSchema } from '/@/components/Table';
import { getDictItemsByCode } from '/@/utils/dict';
import { filterDictText } from '/@/utils/dict/JDictSelectUtil';
import { buildUUID } from '/@/utils/uuid';

// 校验失败 flag
export const VALIDATE_FAILED = 'validate-failed';

export const columns: BasicColumn[] = [
  {
    title: '表类型',
    align: 'center',
    sorter: true,
    dataIndex: 'tableType',
    width: 140,
    customRender({ text, record }) {
      let tableTypeDictOptions = getDictItemsByCode('cgform_table_type');
      let tbTypeText = filterDictText(tableTypeDictOptions, text);
      if (record.isTree === 'Y') {
        tbTypeText += '(树)';
      }
      if (record.themeTemplate === 'innerTable') {
        tbTypeText += '(内嵌)';
      } else if (record.themeTemplate === 'erp') {
        tbTypeText += '(ERP)';
      } else if (record.themeTemplate === 'tab') {
        tbTypeText += '(TAB)';
      }
      return tbTypeText;
    },
  },
  {
    title: '表名',
    sorter: true,
    align: 'center',
    dataIndex: 'tableName',
    width: 240,
  },
  {
    title: '表描述',
    align: 'center',
    dataIndex: 'tableTxt',
    width: 220,
  },
  {
    title: '版本',
    align: 'center',
    dataIndex: 'tableVersion',
    width: 120,
  },
  {
    title: '同步状态',
    align: 'center',
    sorter: true,
    dataIndex: 'isDbSynch',
    slots: { customRender: 'dbSync' },
    width: 120,
  },
  {
    title: '创建时间',
    align: 'center',
    sorter: true,
    dataIndex: 'createTime',
    width: 240,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: '表名',
    field: 'tableName',
    component: 'JInput',
  },
  {
    label: '表类型',
    field: 'tableType_MultiString',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'cgform_table_type',
      mode: 'multiple',
    },
  },
  {
    label: '表描述',
    field: 'tableTxt',
    component: 'JInput',
  },
];

/** 扩展JSON默认值 */
export const ExtConfigDefaultJson = {
  // 对接报表打印
  reportPrintShow: 0,
  reportPrintUrl: '',
  joinQuery: 0,
  modelFullscreen: 0,
  modalMinWidth: '',
  commentStatus: 0,
  tableFixedAction: 1,
  tableFixedActionType: 'right',
  // Excel 图片导出方式（embed 嵌入图片，path 图片路径）
  imageExportMode: 'path',
  // update-begin--author:liaozhiyang---date:20240329---for：【QQYUN-7872】online表单label较长优化
  formLabelLengthShow: 0,
  formLabelLength: null,
  // update-begin--author:liaozhiyang---date:20240329---for：【QQYUN-7872】online表单label较长优化
  // 是否启用外部链接
  enableExternalLink: 0,
  externalLinkActions: 'add,edit,detail',
  // update-begin--author:jeecg---date:20260512---for：【QQYUN-15337】online表单支持多数据源
  // 是否开启多数据源（仅UI状态，关闭时清空 dbSource；最终持久化到 onl_cgform_head.db_source 列）
  enableMultiDataSource: 0,
  dbSource: '',
  // update-end--author:jeecg---date:20260512---for：【QQYUN-15337】online表单支持多数据源
};

/** 获取主表的初始化数据 */
export function useInitialData() {
  let initialData = [
    {
      dbFieldName: 'id',
      dbFieldTxt: '主键',
      dbLength: 36,
      dbPointLength: 0,
      dbDefaultVal: '',
      dbType: 'string',
      dbIsKey: '1',
      dbIsNull: '0',
      // table2
      isShowForm: '0',
      isShowList: '0',
      isReadOnly: '1',
      fieldShowType: 'text',
      fieldLength: '200',
      queryMode: 'single',
      dbIsSync: '1'
    },
    {
      dbFieldName: 'create_by',
      dbFieldTxt: '创建人',
      dbLength: 50,
      dbPointLength: 0,
      dbDefaultVal: '',
      dbType: 'string',
      dbIsKey: '0',
      dbIsNull: '1',
      // table2
      isShowForm: '0',
      isShowList: '0',
      fieldShowType: 'text',
      fieldLength: '200',
      queryMode: 'single',
      dbIsSync: '1'
    },
    {
      dbFieldName: 'create_time',
      dbFieldTxt: '创建日期',
      dbLength: 0,
      dbPointLength: 0,
      dbDefaultVal: '',
      dbType: 'Datetime',
      dbIsKey: '0',
      dbIsNull: '1',
      // table2
      isShowForm: '0',
      isShowList: '0',
      fieldShowType: 'datetime',
      fieldLength: '200',
      queryMode: 'single',
      dbIsSync: '1'
    },
    {
      dbFieldName: 'update_by',
      dbFieldTxt: '更新人',
      dbLength: 50,
      dbPointLength: 0,
      dbDefaultVal: '',
      dbType: 'string',
      dbIsKey: '0',
      dbIsNull: '1',
      // table2
      isShowForm: '0',
      isShowList: '0',
      fieldShowType: 'text',
      fieldLength: '200',
      queryMode: 'single',
      dbIsSync: '1'
    },
    {
      dbFieldName: 'update_time',
      dbFieldTxt: '更新日期',
      dbLength: 0,
      dbPointLength: 0,
      dbDefaultVal: '',
      dbType: 'Datetime',
      dbIsKey: '0',
      dbIsNull: '1',
      // table2
      isShowForm: '0',
      isShowList: '0',
      fieldShowType: 'datetime',
      fieldLength: '200',
      queryMode: 'single',
      dbIsSync: '1'
    },
    {
      dbFieldName: 'sys_org_code',
      dbFieldTxt: '所属部门',
      dbLength: 64,
      dbPointLength: 0,
      dbDefaultVal: '',
      dbType: 'string',
      dbIsKey: '0',
      dbIsNull: '1',
      // table2
      isShowForm: '0',
      isShowList: '0',
      fieldShowType: 'text',
      fieldLength: '200',
      queryMode: 'single',
      dbIsSync: '1'
    },
    // {
    //   dbFieldName: 'sys_org_code',
    //   dbFieldTxt: '所属部门',
    //   dbLength: 50,
    //   dbPointLength: 0,
    //   dbDefaultVal: '',
    //   dbType: 'string',
    //   dbIsKey: false,
    //   dbIsNull: true
    // },
    // {
    //   dbFieldName: 'sys_company_code',
    //   dbFieldTxt: '所属公司',
    //   dbLength: 50,
    //   dbPointLength: 0,
    //   dbDefaultVal: '',
    //   dbType: 'string',
    //   dbIsKey: false,
    //   dbIsNull: true
    // },
    // {
    //   dbFieldName: 'bpm_status',
    //   dbFieldTxt: '流程状态',
    //   dbLength: 32,
    //   dbPointLength: 0,
    //   dbDefaultVal: '',
    //   dbType: 'string',
    //   dbIsKey: false,
    //   dbIsNull: true
    // }
  ];
  // 临时 id，不保存到数据库
  let tempIds: string[] = [];
  initialData.forEach((record) => {
    record['id'] = buildUUID();
    tempIds.push(record['id']);
  });
  return { initialData, tempIds };
}

/** 获取树的初始化数据 */
export function useTreeNeedFields() {
  return [
    {
      dbFieldName: 'pid',
      dbFieldTxt: '父级节点',
      dbLength: 32,
      dbPointLength: 0,
      dbDefaultVal: '',
      dbType: 'string',
      dbIsKey: '0',
      dbIsNull: '1',
      // table2
      isShowForm: '1',
      isShowList: '0',
      fieldShowType: 'text',
      fieldLength: '200',
      queryMode: 'single',
      dbIsSync: '1'
    },
    {
      dbFieldName: 'has_child',
      dbFieldTxt: '是否有子节点',
      dbLength: 3,
      dbPointLength: 0,
      dbDefaultVal: '',
      dbType: 'string',
      dbIsKey: '0',
      dbIsNull: '1',
      // table2
      isShowForm: '0',
      isShowList: '0',
      fieldShowType: 'list',
      fieldLength: '200',
      queryMode: 'single',
      // table3
      dictField: 'yn',
      dbIsSync: '1'
    },
  ];
}

/**
 * online 默认按钮
 */
export const onlineDefaultButton = [
  { code: 'add', title: '新增', status: 0 },
  { code: 'edit', title: '编辑', status: 0 },
  { code: 'delete', title: '删除', status: 0 },
  { code: 'export', title: '导出', status: 0 },
  { code: 'import', title: '导入', status: 0 },
  { code: 'query', title: '查询', status: 0 },
];
