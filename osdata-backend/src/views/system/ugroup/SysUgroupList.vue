<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'system:sys_ugroup:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button type="primary" v-auth="'system:sys_ugroup:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button type="primary" v-auth="'system:sys_ugroup:importExcel'" preIcon="ant-design:import-outlined" @click="onImportXls"
          >导入</j-upload-button
        >
        <a-tooltip placement="right" :overlayInnerStyle="{ width: '520px' }">
          <template #title>
            <div>用户组应用于以下功能中：</div>
            <div>1. 流程办理时指定下一步操作人，根据用户组选择人员。</div>
            <div>2. 表单设计器人员组件选择人员时，根据用户组选择人员。</div>
            <div>3. OA模块中协同工作新增发起事项选择人员时，根据用户组选择人员。</div>
          </template>
          <Icon icon="ant-design:question-circle-outlined" style="color: #8c8c8c; cursor: help" />
        </a-tooltip>

        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'system:sys_ugroup:deleteBatch'"
            >批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <SysUgroupModal @register="registerModal" @success="handleSuccess"></SysUgroupModal>
    <!--角色用户表格-->
    <GroupUserTable @register="groupUserDrawer" />
  </div>
</template>

<script lang="ts" name="system-sysUgroup" setup>
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns, searchFormSchema } from './ugroup.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './ugroup.api';
  import SysUgroupModal from './components/SysUgroupModal.vue';
  import GroupUserTable from './components/GroupUserTable.vue';
  import { useDrawer } from '/@/components/Drawer';
  //注册model
  const [registerModal, { openModal }] = useModal();
  //注册table数据
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: '用户组表',
      api: list,
      columns,
      canResize: true,
      formConfig: {
        schemas: searchFormSchema,
        autoSubmitOnEnter: true,
        showAdvancedButton: true,
      },
      actionColumn: {
        width: 150,
        fixed: 'right',
      },
    },
    exportConfig: {
      name: '用户组',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
      success: handleSuccess,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;
  const [groupUserDrawer, { openDrawer: openGroupUserDrawer }] = useDrawer();
  /**
   * 新增事件
   */
  function handleAdd() {
    openModal(true, {
      isUpdate: false,
      showFooter: true,
    });
  }
  /**
   * 编辑事件
   */
  function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
      showFooter: true,
    });
  }
  /**
   * 详情
   */
  function handleDetail(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
      showFooter: false,
    });
  }
  /**
   * 删除事件
   */
  async function handleDelete(record) {
    await deleteOne({ id: record.id }, handleSuccess);
  }
  /**
   * 批量删除事件
   */
  async function batchHandleDelete() {
    await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
  }
  /**
   * 成功回调
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }
  /**
   * 用户组用户
   */
  function handleUser(record) {
    openGroupUserDrawer(true, record);
  }
  /**
   * 操作栏
   */
  function getTableAction(record) {
    return [
      {
        label: '用户',
        onClick: handleUser.bind(null, record),
      },
    ];
  }
  /**
   * 下拉操作栏
   */
  function getDropDownAction(record) {
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        auth: 'system:sys_ugroup:edit',
      },
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
        auth: 'system:sys_ugroup:delete',
      },
    ];
  }
</script>

<style lang="less" scoped>
  :deep(.ant-picker),
  :deep(.ant-input-number) {
    width: 100%;
  }
</style>
