<template>
  <LinkTableInput
    :value="innerValue"
    :valueField="originColumn.valueField || 'id'"
    :textField="originColumn.textField || ''"
    :tableName="originColumn.tableName || ''"
    :multi="true"
    :linkFields="originColumn.linkFields || []"
    :imageField="originColumn.imageField || ''"
    :editBtnShow="!originColumn.editBtnShow"
    :queryMode="originColumn.queryMode || 'online'"
    v-bind="cellProps"
    @change="handleChange"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';
import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';
import { vModel } from '/@/components/jeecg/JVxeTable/utils';
import LinkTableInput from '../../../../views/super/online/cgform/extend/linkTable/LinkTableInput.vue';
import { getLinkTableColumns, transLinkTableRecord } from '../../../../views/super/online/cgform/extend/linkTable/useLinkTable';
import { isEmpty } from '/@/utils/is';
import { defHttp } from '/@/utils/http/axios';

export default defineComponent({
  name: 'JVxeLinkTableCell',
  components: { LinkTableInput },
  props: useJVxeCompProps(),
  setup(props: JVxeComponent.Props) {
    const { innerValue, cellProps, originColumn, scrolling, handleChangeCommon, row } = useJVxeComponent(props);
    function handleChange(value, otherValues) {
      if (!isEmpty(value)) {
        Object.keys(otherValues).forEach((key) => {
          let currentValue = otherValues[key];
          vModel(currentValue, row, key);
        });
        handleChangeCommon(value);
      }
    }
    return {
      innerValue,
      cellProps,
      originColumn,
      scrolling,
      handleChange,
      row,
    };
  },
  enhanced: {
    aopEvents: {
    },
    interceptor: {
      'event.clearActived.className'(className) {
        if (
          className.includes('jeecg-online-pop-list-modal') ||
          className.includes('jeecg-online-pop-modal') ||
          className.includes('jeecg-all-table-pop-list-modal')
        ) {
          return false;
        }
      },
    },
    translate: {
      enabled: true,
      async handler(value, ctx) {
        if (!value) return '';
        if (!ctx) return value;
        const { originColumn, row } = ctx.context;
        const col = originColumn.value;
        const tableName = col.tableName;
        const textField = col.textField || '';
        const valueField = col.valueField || 'id';
        // 关联记录相关的他表字段
        const linkFields: string[] = col.linkFields || [];
        if (!tableName) return value;
        const vals = (value + '').split(',');
        const textKey = textField.split(',')[0];

        let records: any[] = [];
        if (col.queryMode === 'table') {
          const extraTableFields = linkFields.map((lf) => lf.split(',')[1]).filter(Boolean);
          const fieldSet = new Set([valueField, ...textField.split(',').filter(Boolean), ...extraTableFields]);
          const showFields = Array.from(fieldSet).join(',');
          const data = await defHttp.get({
            url: '/sys/dict/queryTableDataForLinkRecord',
            params: { tableName, showFields, valueField, keyValues: value, pageNo: 1, pageSize: vals.length },
          });
          records = data?.records || [];
        } else {
          const extraTableFields = linkFields.map((lf) => lf.split(',')[1]).filter(Boolean);
          const allSelectFields = [valueField, textField, ...extraTableFields].filter(Boolean);
          const selectFields = [...new Set(allSelectFields)].join(',');
          const [data, columns] = await Promise.all([
            defHttp.get({url: '/online/cgform/api/getData/' + tableName,
              params: {
                linkTableSelectFields: selectFields,
                pageSize: vals.length,
                pageNo: 1,
                superQueryMatchType: 'and',
                superQueryParams: encodeURI(JSON.stringify([{ field: valueField, rule: 'in', val: value }])),
              },
            }),
            getLinkTableColumns(tableName, selectFields),
          ]);
          records = data?.records || [];
          records.forEach((record) => transLinkTableRecord(record, columns?.columns, columns?.dictOptions));
        }

        const labels = vals.map((v) => {
          const record = records.find((r) => String(r[valueField]) === String(v));
          return record ? record[textKey] : v;
        });
        // 将他表字段的值同步写入行数据，使对应列能正确显示
        if (linkFields.length > 0 && row) {
          linkFields.forEach((lf) => {
            const [formField, tableField] = lf.split(',');
            const fieldValues = vals.map((v) => {
              const record = records.find((r) => String(r[valueField]) === String(v));
              return record ? (record[tableField] ?? '') : '';
            });
            vModel(fieldValues.join(','), row, formField);
          });
        }
        return labels.join(',');
      },
    },
  } as JVxeComponent.EnhancedPartial,
});
</script>
