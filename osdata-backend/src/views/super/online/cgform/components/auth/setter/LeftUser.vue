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
    name: 'LeftUser',
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
            { title: '账号', dataIndex: 'username', width: 200 },
            { title: '姓名', dataIndex: 'realname', width: 200 },
          ],
          rowSelection: {
            type: 'radio',
            onChange(selectedRowKeys) {
              if (selectedRowKeys.length > 0) {
                emit('select', selectedRowKeys[0]);
              }
            },
          },
          formConfig: {
            schemas: [
              {
                label: '账号',
                field: 'username',
                component: 'JInput',
                colProps: { class: 'onl-auth-user-query-item' },
                componentProps: {
                  placeholder: '输入账号',
                },
              },
              {
                label: '姓名',
                field: 'realname',
                component: 'JInput',
                colProps: { class: 'onl-auth-user-query-item' },
                componentProps: {
                  placeholder: '输入姓名',
                },
              },
            ],
            actionColOptions: { class: 'onl-auth-user-query-actions' },
          },
          canResize: false,
          clickToRowSelect: true,
          showActionColumn: false,
          showTableSetting: false,
        },
      });
      const [registerTable, { clearSelectedRowKeys }, { rowSelection }] = tableContext;

      async function loadData(params) {
        let { code, success, result, message } = await defHttp.get(
          {
            url: '/sys/user/list',
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
