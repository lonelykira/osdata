<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined" />
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'openapi:open_api_log:deleteBatch'">
            批量操作
            <Icon icon="mdi:chevron-down" />
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
      <template #bodyCell="{ column, text }">
        <template v-if="column.dataIndex === 'responseCode'">
          <a-tag :color="getStatusColor(text)">{{ text || '-' }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'requestMethod'">
          <a-tag :color="getMethodColor(text)">{{ text || '-' }}</a-tag>
        </template>
      </template>
    </BasicTable>
    <OpenApiLogDetailModal @register="registerDetailModal" />
  </div>
</template>

<script lang="ts" name="openapi-openApiLog" setup>
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useModal } from '/@/components/Modal';
  import { columns, searchFormSchema } from './OpenApiLog.data';
  import { list, deleteOne, batchDelete } from './OpenApiLog.api';
  import OpenApiLogDetailModal from './components/OpenApiLogDetailModal.vue';

  const { tableContext } = useListPage({
    tableProps: {
      title: '调用日志',
      api: list,
      columns,
      canResize: false,
      formConfig: {
        schemas: searchFormSchema,
        autoSubmitOnEnter: true,
        showAdvancedButton: true,
        fieldMapToTime: [['callTime', ['callTime_begin', 'callTime_end'], 'YYYY-MM-DD HH:mm:ss']],
      },
      actionColumn: {
        width: 120,
        fixed: 'right',
      },
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;
  const [registerDetailModal, { openModal: openDetailModal }] = useModal();

  function getStatusColor(code: number) {
    if (!code) return 'default';
    if (code >= 200 && code < 300) return 'success';
    if (code >= 400 && code < 500) return 'warning';
    if (code >= 500) return 'error';
    return 'default';
  }

  const methodColorMap: Record<string, string> = {
    GET: 'blue',
    POST: 'green',
    PUT: 'orange',
    DELETE: 'red',
    PATCH: 'purple',
  };
  function getMethodColor(method: string) {
    return methodColorMap[method] ?? 'default';
  }

  function handleDetail(record) {
    openDetailModal(true, { record });
  }

  async function handleDelete(record) {
    await deleteOne({ id: record.id }, handleSuccess);
  }

  async function batchHandleDelete() {
    await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
  }

  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }

  function getTableAction(record) {
    return [
      {
        label: '详情',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        },
        auth: 'openapi:open_api_log:delete',
      },
    ];
  }
</script>
