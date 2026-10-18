import { Ref } from 'vue';
import DBAttributeTable from '../components/tables/DBAttributeTable.vue';
import PageAttributeTable from '../components/tables/PageAttributeTable.vue';
import CheckDictTable from '../components/tables/CheckDictTable.vue';
import ForeignKeyTable from '../components/tables/ForeignKeyTable.vue';
import IndexTable from '../components/tables/IndexTable.vue';
import QueryTable from '../components/tables/QueryTable.vue';

// 定义弹窗form的类型
export namespace CgformModal {
  export type DBAttributeTableType = InstanceType<typeof DBAttributeTable>;
  export type PageAttributeTableType = InstanceType<typeof PageAttributeTable>;
  export type CheckDictTableType = InstanceType<typeof CheckDictTable>;
  export type ForeignKeyTableType = InstanceType<typeof ForeignKeyTable>;
  export type IndexTableType = InstanceType<typeof IndexTable>;
  export type QueryTableType = InstanceType<typeof QueryTable>;

  export type TablesRef = {
    dbTable: Ref<DBAttributeTableType | undefined>;
    pageTable: Ref<PageAttributeTableType | undefined>;
    checkTable: Ref<CheckDictTableType | undefined>;
    fkTable: Ref<ForeignKeyTableType | undefined>;
    idxTable: Ref<IndexTableType | undefined>;
    queryTable: Ref<QueryTableType | undefined>;
  };
}

// 定义页面类型，normal = 普通页面，copy=视图页面
export enum CgformPageType {
  normal,
  copy,
}

/**
 * Online扩展配置类型
 */
export type ExtConfigType = Partial<{
  // 是否启用积木报表打印（0否，1是）
  reportPrintShow: number,
  // 积木报表地址
  reportPrintUrl: string,
  // 是否启用联合查询（0否，1是）
  joinQuery: number,
  // 弹窗是否默认全屏（0否，1是）
  modelFullscreen: number,
  // 弹窗的最小宽度（px）
  modalMinWidth: string,
  // 是否固定操作列（0否，1是）
  tableFixedAction: number,
  // 操作列固定方式
  tableFixedActionType: 'left' | 'right',
  // 是否允许调整列表列宽
  canResizeColumn?: number,
  // Excel 图片导出方式
  imageExportMode?: 'embed' | 'path',
}>
