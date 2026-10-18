import type { Ref, ComputedRef } from 'vue';
import { ref, computed, nextTick, inject } from 'vue';
import { CgformModal } from '../types';
import { JVxeColumn, JVxeTableInstance } from '/@/components/jeecg/JVxeTable/types';
import { VALIDATE_FAILED } from '../cgform.data';
import { pick } from 'lodash-es';

export function useTableSync(columns: Ref<JVxeColumn[]>) {
  const tables = inject<CgformModal.TablesRef>('tables');
  const fullScreenRef = inject<ComputedRef<boolean>>('fullScreenRef');
  const vxetableHeight = inject<ComputedRef<number>>('vxetableHeight');
  const tableRef = ref<JVxeTableInstance>();
  const loading = ref(false);
  const dataSource = ref<Recordable[]>([]);
  // 表格动态高度
  const tableHeight = computed(() => ({
    // 正常表格高度
    normal: fullScreenRef?.value ? vxetableHeight?.value : 260,
    // 没有 toolbar 的表格高度
    noToolbar: fullScreenRef?.value ? vxetableHeight?.value : 320,
  }));

  // 当前表的所有列字段key
  const columnKeys = computed<string[]>(() => ['id'].concat(columns.value.map((col) => col.key)));

  // 表格其他props
  const tableProps = computed(() => {
    return {
      // 针对Online表单对虚拟滚动做出优化
      // 虚拟滚动配置，y轴（行数）大于xx条数据时启用虚拟滚动
      // update-begin--author:liaozhiyang---date:20231025---for：【QQYUN-6808】online编辑字段多了卡顿
      scrollY: {
        enabled: true,
        gt: 15,
      },
      // 列数
      scrollX: {
        enabled: true,
        gt: 20,
      },
      // update-begin--author:liaozhiyang---date:20231025---for：【QQYUN-6808】online编辑字段多了卡顿
    };
  });

  // 校验并获取表格数据
  async function validateData(activeKey: string) {
    let instance = tableRef.value!;
    let errMap = await instance.fullValidateTable();
    if (errMap) {
      throw { code: VALIDATE_FAILED, activeKey };
    }
    // 过滤掉当前表中不存在的字段，以防止多个表冲突
    let tableData = instance.getTableData().map((data) => pick(data, columnKeys.value));
    // 获取被删除的ID
    let deleteIds = instance.getDeleteData().map((d) => d.id);
    return { tableData, deleteIds };
  }

  /**
   * 设置数据源
   * @param data
   * @param insert
   */
  async function setDataSource(data, insert = false) {
    if (insert) {
      dataSource.value = [];
      await nextTick();
      await tableRef.value!.addOrInsert(data, 0, null, { setActive: false });
      await nextTick();
      tableRef.value!.recalcDisableRows();
    } else {
      dataSource.value = data;
      // update-begin--author:liaozhiyang---date:20240705---for：【TV360X-1762】解决编辑时id可删除
      await nextTick();
      tableRef.value!.recalcDisableRows();
      // update-end--author:liaozhiyang---date:20240705---for：【TV360X-1762】解决编辑时id可删除
    }
  }

  /**
   * 同步列表，可以同步新增、修改、删除
   * @param dbTable
   */
  function syncTable(dbTable: Ref<CgformModal.DBAttributeTableType | undefined>) {
    let targetTable = tableRef.value!;
    let sourceTable = dbTable.value!.tableRef!;

    let removeIds = dbTable.value!.getRemoveIds();
    let sourceData = sourceTable.getXTable().internalData.tableFullData;
    let targetData = targetTable.getXTable().internalData.tableFullData;
    // update-begin--author:liaozhiyang---date:20260316---for:【QQYUN-13751】jVxetable优化
    // 用 Map 索引 targetData，将查找从 O(N) 降到 O(1)
    const targetMap = new Map<string, Recordable>();
    targetData.forEach((targetValue) => {
      if (targetValue.id) {
        targetMap.set(targetValue.id, targetValue);
      }
    });
    // 用 Set 索引 removeIds，将查找从 O(K) 降到 O(1)
    const removeIdSet = new Set<string>(removeIds);
    // update-begin--author:liaozhiyang---date:20240724---for：【TV360X-1852】新增时删除所有字段再新增一个字段，保存报错
    // 先收集需要删除的ID，最后统一删除，避免在遍历中修改数组
    const toRemoveIds: string[] = [];
    // update-end--author:liaozhiyang---date:20240724---for：【TV360X-1852】新增时删除所有字段再新增一个字段，保存报错
    // 批量收集需要修改的值，最后一次性调用 setValues
    const batchSetValues: { rowKey: string; values: Recordable }[] = [];
    // update-begin--author:liaozhiyang---date:20250407---for：【QQYUN-11801】ai建表字段
    // 提前缓存列默认值映射，避免新增每行时重复遍历 columns
    const columnDefaults: { key: string; defaultValue: any }[] = [];
    columns.value.forEach((column) => {
      if (column.key !== 'dbFieldName' && column.key !== 'dbFieldTxt') {
        columnDefaults.push({ key: column.key, defaultValue: column.defaultValue });
      }
    });
    // update-end--author:liaozhiyang---date:20250407---for：【QQYUN-11801】ai建表字段
    sourceData.forEach((sourceValue) => {
      const targetValue = targetMap.get(sourceValue.id);
      if (targetValue) {
        // 判断是否修改了值
        let dbFieldName = targetValue['dbFieldName'];
        let dbFieldTxt = targetValue['dbFieldTxt'];
        if (sourceValue.dbFieldName !== dbFieldName || sourceValue.dbFieldTxt !== dbFieldTxt) {
          // 收集修改字段，稍后批量同步
          batchSetValues.push({
            rowKey: targetValue.id,
            values: {
              dbFieldName: sourceValue.dbFieldName,
              dbFieldTxt: sourceValue.dbFieldTxt,
            },
          });
        }
      } else {
        // target中不存在，说明是新增的
        let record = Object.assign({}, sourceValue);
        // update-begin--author:liaozhiyang---date:20250407---for：【QQYUN-11801】ai建表字段
        for (const { key, defaultValue } of columnDefaults) {
          if (record[key] == undefined) {
            record[key] = defaultValue;
          }
        }
        // update-end--author:liaozhiyang---date:20250407---for：【QQYUN-11801】ai建表字段
        targetTable.addRows(record);
      }
    });
    // 批量同步修改
    if (batchSetValues.length > 0) {
      targetTable.setValues(batchSetValues);
    }
    // 处理删除：target中存在但已被删除的行
    // update-begin--author:liaozhiyang---date:20240724---for：【TV360X-1852】新增时删除所有字段再新增一个字段，保存报错
    targetData.forEach((targetValue) => {
      if (targetValue.id && removeIdSet.has(targetValue.id)) {
        toRemoveIds.push(targetValue.id);
      }
    });
    if (toRemoveIds.length > 0) {
      setTimeout(() => {
        toRemoveIds.forEach((id) => targetTable.removeRowsById(id));
      }, 0);
    }
    // update-end--author:liaozhiyang---date:20240724---for：【TV360X-1852】新增时删除所有字段再新增一个字段，保存报错
    // update-end--author:liaozhiyang---date:20260316---for:【QQYUN-13751】jVxetable优化
    return nextTick();
  }

  return { tables, tableRef, loading, dataSource, columnKeys, tableHeight, tableProps, syncTable, validateData, setDataSource };
}
