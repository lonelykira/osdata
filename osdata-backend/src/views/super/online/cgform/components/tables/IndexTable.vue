<template>
  <JVxeTable
    ref="tableRef"
    rowNumber
    rowSelection
    dragSort
    keyboardEdit
    sortKey="orderNum"
    :maxHeight="tableHeight.normal"
    :loading="loading"
    :columns="columns"
    :dataSource="dataSource"
    :toolbar="actionButton"
    :insertRow="actionButton"
    v-bind="tableProps"
  />
</template>

<script lang="ts">
  import { ref, defineComponent, Ref } from 'vue';
  import { JVxeTypes, JVxeColumn } from '/@/components/jeecg/JVxeTable/types';
  import { CgformModal } from '../../types';
  import { useTableSync } from '../../hooks/useTableSync';

  export default defineComponent({
    name: 'IndexTable',
    components: {},
    props: {
      actionButton: {
        type: Boolean,
        default: true,
        required: false,
      },
    },
    setup() {
      const columns = ref<JVxeColumn[]>([
        {
          title: '索引名称',
          key: 'indexName',
          width: 330,
          type: JVxeTypes.input,
          defaultValue: '',
          placeholder: '请输入${title}',
          validateRules: [
            { required: true, message: '${title}不能为空' },
            // update-begin--author:liaozhiyang---date:20240807---for：【TV360X-1974】online索引名称增加校验
            {
              pattern: /^[a-zA-Z][a-zA-Z0-9_-]*$/,
              message: '命名规则：只能由字母、数字、下划线组成；必须以字母开头',
            },
            // update-end--author:liaozhiyang---date:20240807---for：【TV360X-1974】online索引名称增加校验
          ],
        },
        {
          title: '索引栏位',
          key: 'indexField',
          width: 330,
          type: JVxeTypes.selectMultiple,
          options: [],
          defaultValue: '',
          placeholder: '请选择${title}',
          validateRules: [{ required: true, message: '请选择${title}' }],
        },
        {
          title: '索引类型',
          key: 'indexType',
          width: 330,
          type: JVxeTypes.select,
          options: [
            { title: 'normal', value: 'normal' },
            { title: 'unique', value: 'unique' },
          ],
          defaultValue: 'normal',
          placeholder: '请选择${title}',
          validateRules: [{ required: true, message: '请选择${title}' }],
        },
      ]);

      const setup = useTableSync(columns);
      const { tableRef, loading, dataSource, tableHeight, tableProps, setDataSource, validateData } = setup;

      /** 同步列表 */
      function syncTable(dbTable: Ref<CgformModal.DBAttributeTableType>) {
        let options: any[] = [];
        let data = dbTable.value!.tableRef!.getTableData();
        data.forEach((value) => {
          if (value.dbFieldName) {
            options.push({
              title: value.dbFieldTxt,
              value: value.dbFieldName,
            });
          }
        });
        columns.value[1].options = options;
      }

      return { tableRef, loading, dataSource, columns, tableHeight, tableProps, syncTable, setDataSource, validateData };
    },
  });
</script>

<style scoped></style>
