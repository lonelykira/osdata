<template>
  <BasicDrawer @register="registerBaseDrawer" title="用户组用户" width="800" destroyOnClose>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" @click="handleSelect"> 添加用户</a-button>
        <a-dropdown v-if="checkedKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="bx:bx-unlink"></Icon>
                取消关联
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >批量操作
            <Icon icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <!--用户操作抽屉-->
    <UserDrawer @register="registerDrawer" @success="handleSuccess" />
    <!--用户选择弹窗-->
    <UseSelectModal @register="registerModal" @select="selectOk" />
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, watch, unref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { BasicDrawer, useDrawer, useDrawerInner } from '/@/components/Drawer';
  import { useModal } from '/@/components/Modal';
  import UserDrawer from '../../user/UserDrawer.vue';
  import UseSelectModal from '@/views/system/role/components/UseSelectModal.vue';
  import { userList, deleteUserGroup, batchDeleteUserGroup, addUserGroup } from '../ugroup.api';
  import { userColumns, searchUserFormSchema } from '../ugroup.data';

  const emit = defineEmits(['register', 'hideUserList']);
  const props = defineProps({
    disableUserEdit: { type: Boolean, default: false },
  });

  const checkedKeys = ref<Array<string | number>>([]);
  const groupId = ref('');
  const [registerBaseDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    groupId.value = data.id;
    setProps({ searchInfo: { groupId: data.id } });
    reload();
  });
  //注册drawer
  const [registerDrawer, { openDrawer }] = useDrawer();
  //注册drawer
  const [registerModal, { openModal }] = useModal();
  const [registerTable, { reload, updateTableDataRecord, setProps }] = useTable({
    title: '用户列表',
    api: userList,
    columns: userColumns,
    formConfig: {
      // 代码逻辑说明: 【QQYUN-5685】3、租户角色下,查询居左显示
      labelWidth: 60,
      schemas: searchUserFormSchema,
      autoSubmitOnEnter: true,
    },
    striped: true,
    useSearchForm: true,
    showTableSetting: true,
    clickToRowSelect: false,
    bordered: true,
    showIndexColumn: false,
    // 【issues/1064】列设置的 cacheKey
    tableSetting: { fullScreen: true, cacheKey: 'group_user_table' },
    canResize: false,
    rowKey: 'id',
    actionColumn: {
      width: 180,
      title: '操作',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: undefined,
    },
  });

  /**
   * 选择列配置
   */
  const rowSelection = {
    type: 'checkbox',
    columnWidth: 50,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
  };

  /**
   * 选择事件
   */
  function onSelectChange(selectedRowKeys: (string | number)[], selectionRows) {
    checkedKeys.value = selectedRowKeys;
  }

  /**
   * 编辑
   */
  async function handleEdit(record: Recordable) {
    openDrawer(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * 删除事件
   */
  async function handleDelete(record) {
    await deleteUserGroup({ userId: record.id, groupId: groupId.value }, reload);
  }

  /**
   * 批量删除事件
   */
  async function batchHandleDelete() {
    await batchDeleteUserGroup({ userIds: checkedKeys.value.join(','), groupId: groupId.value }, () => {
      // 代码逻辑说明: 【TV360X-1655】批量取消关联之后清空选中记录
      reload();
      checkedKeys.value = [];
    });
  }

  /**
   * 成功回调
   */
  function handleSuccess({ isUpdate, values }) {
    isUpdate ? updateTableDataRecord(values.id, values) : reload();
  }
  /**
   * 选择已有用户
   */
  function handleSelect() {
    openModal(true);
  }
  /**
   * 添加已有用户
   */
  async function selectOk(val) {
    await addUserGroup({ groupId: groupId.value, userIdList: val }, reload);
  }
  /**
   * 操作栏
   */
  function getTableAction(record) {
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        ifShow: () => !props.disableUserEdit,
      },
      {
        label: '取消关联',
        popConfirm: {
          title: '是否确认取消关联',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>

<style scoped>
  /*update-begin---author:wangshuai ---date:20230703  for：【QQYUN-5685】3、租户角色下,查询居左显示*/
  :deep(.ant-form-item-control-input-content) {
    text-align: left;
  }
  /*update-end---author:wangshuai ---date:20230703  for：【QQYUN-5685】3、租户角色下,查询居左显示*/
</style>
