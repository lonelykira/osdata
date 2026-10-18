import { nextTick, watch } from 'vue';
import { JVxeDataProps, JVxeRefs, JVxeTableMethods } from '../types';

export function useDataSource(props, data: JVxeDataProps, methods: JVxeTableMethods, refs: JVxeRefs) {
  watch(
    () => props.dataSource,
    async () => {
      data.disabledRowIds = [];
      // update-begin--author:liaozhiyang---date:20260316---for:【QQYUN-13751】jVxetable优化
      data.vxeDataSource.value = props.dataSource.map(row => ({ ...row }));
      // update-end--author:liaozhiyang---date:20260316---for:【QQYUN-13751】jVxetable优化
      data.vxeDataSource.value.forEach((row, rowIndex) => {
        // 判断是否是禁用行
        if (methods.isDisabledRow(row, rowIndex)) {
          data.disabledRowIds.push(row.id);
        }
        // 处理联动回显数据
        methods.handleLinkageBackData(row);
      });
      await waitRef(refs.gridRef);
      methods.recalcSortNumber();
    },
    { immediate: true }
  );
}

function waitRef($ref) {
  return new Promise<any>((resolve) => {
    (function next() {
      if ($ref.value) {
        resolve($ref);
      } else {
        nextTick(() => next());
      }
    })();
  });
}
