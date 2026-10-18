<template>
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <template #tableTop><span></span></template>
  </BasicTable>
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { BasicTable } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';

  export default defineComponent({
    name: 'LeftRole',
    components: { BasicTable },
    emits: ['select'],
    setup(_, { emit }) {
      const { tableContext, createMessage: $message } = useListPage({
        tableProps: {
          api: loadData,
          rowKey: 'id',
          size: 'small',
          bordered: true,
          columns: [
            { title: '角色编码', align: 'center', dataIndex: 'roleCode' },
            { title: '角色名称', align: 'center', dataIndex: 'roleName' },
          ],
          rowSelection: {
            type: 'radio',
            onChange(selectedRowKeys) {
              if (selectedRowKeys.length > 0) {
                emit('select', selectedRowKeys[0]);
              }
            },
          },

          canResize: false,
          clickToRowSelect: true,
          useSearchForm: false,
          showActionColumn: false,
          showTableSetting: false,
        },
      });
      const [registerTable, { clearSelectedRowKeys }, { rowSelection }] = tableContext;

      async function loadData(params) {
        let { code, success, result, message } = await defHttp.get(
          {
            url: '/sys/role/list',
            params,
          },
          { isTransformResponse: false }
        );
        if (success) {
          return result;
        }
        if (code === 510) {
          $message.warning(message);
        }
        return {};
      }

      function clearSelected() {
        clearSelectedRowKeys();
      }

      return { rowSelection, registerTable, clearSelected };
    },
  });
</script>

<style scoped></style>
