<template>
  <!--引用表格-->
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <!--插槽:table标题-->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="createUser" :disabled="!orgCode">新建用户</a-button>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="selectAddUser" :disabled="!orgCode || props.data?.orgCategory === '3'"
        >添加已有用户</a-button
      >
    </template>
    <!-- 插槽：行内操作按钮 -->
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" />
    </template>
  </BasicTable>
  <UserDrawer @register="registerDrawer" @success="onUserDrawerSuccess" />
  <UserSelectModal ref="userSelectModalRef" rowKey="id" @register="registerSelUserModal" @getSelectResult="onSelectUserOk" />
</template>

<script lang="ts" setup>
  import { computed, h, inject, ref, watch } from 'vue';
  import { Tag } from 'ant-design-vue';
  import { ActionItem, BasicColumn, BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';

  import UserDrawer from '/@/views/system/user/UserDrawer.vue';
  import UserSelectModal from '/@/components/Form/src/jeecg/components/modal/UserSelectModal.vue';
  import { queryDepartPostByOrgCode } from '../depart.api';
  import { ColEx } from '/@/components/Form/src/types';
  import { userColumns } from '@/views/system/depart/depart.data';
  import { linkDepartUserBatch } from '@/views/system/departUser/depart.user.api';

  const prefixCls = inject('prefixCls');
  const props = defineProps({
    data: { require: true, type: Object },
  });
  const userSelectModalRef: any = ref(null);
  // 当前选中的部门code，可能会为空，代表未选择部门
  const orgCode = computed(() => props.data?.orgCode);
  // 当前部门id
  const departId = computed(() => props.data?.id);
  const departUserColumns: BasicColumn[] = [
    {
      ...userColumns[0],
      customRender: ({ record, text }) =>
        h('span', [text, record.orgCode === orgCode.value ? h(Tag, { color: 'blue', style: { marginLeft: '6px' } }, () => '本级') : null]),
    },
    ...userColumns.slice(1),
  ];

  // 自适应列配置
  const adaptiveColProps: Partial<ColEx> = {
    xs: 24, // <576px
    sm: 24, // ≥576px
    md: 24, // ≥768px
    lg: 12, // ≥992px
    xl: 12, // ≥1200px
    xxl: 8, // ≥1600px
  };
  // 列表页面公共参数、方法
  const { tableContext, createMessage } = useListPage({
    tableProps: {
      api: queryDepartPostByOrgCode,
      columns: departUserColumns,
      canResize: false,
      rowKey: 'id',
      formConfig: {
        // schemas: userInfoSearchFormSchema,
        baseColProps: adaptiveColProps,
        labelAlign: 'left',
        labelCol: {
          xs: 24,
          sm: 24,
          md: 24,
          lg: 9,
          xl: 7,
          xxl: 5,
        },
        // 操作按钮配置
        actionColOptions: {
          ...adaptiveColProps,
          style: { textAlign: 'left' },
        },
      },
      tableSetting: { cacheKey: 'depart_user_userInfo' },
      // 请求之前对参数做处理
      beforeFetch(params) {
        return Object.assign(params, { orgCode: orgCode.value });
      },
      immediate: !!orgCode.value,
    },
  });

  // 注册 ListTable
  const [registerTable, { reload, setProps, setLoading, updateTableDataRecord }, { rowSelection, selectedRowKeys }] = tableContext;

  watch(
    () => props.data,
    () => reload()
  );
  //注册drawer
  const [registerDrawer, { openDrawer, setDrawerProps }] = useDrawer();
  const [registerUserAuthDrawer, userAuthDrawer] = useDrawer();
  // 注册用户选择 modal
  const [registerSelUserModal, selUserModal] = useModal();

  // 清空选择的行
  function clearSelection() {
    selectedRowKeys.value = [];
  }

  // 创建用户
  async function createUser() {
    if (!departId.value) {
      createMessage.warning('请先选择一个部门');
    } else {
      let mainDepPostId = '';
      let selecteddeparts = departId.value;
      if (props.data?.orgCategory === '3') {
        mainDepPostId = departId.value;
        selecteddeparts = props.data.parentId;
      }
      openDrawer(true, {
        isUpdate: false,
        //update-begin---author:wangshuai ---date:20260811  for：[LHZP-1124]【系统管理】部门管理-用户列表 在这里编辑用户时 部门和角色没展示出来------------
        departDisabled: true,
        //update-end---author:wangshuai ---date:20260811  for：[LHZP-1124]【系统管理】部门管理-用户列表 在这里编辑用户时 部门和角色没展示出来------------
        // 初始化负责部门
        nextDepartOptions: { value: props.data?.key, label: props.data?.title },
        //初始化岗位
        record: {
          mainDepPostId: mainDepPostId,
          activitiSync: 1,
          userIdentity: 1,
          selecteddeparts: selecteddeparts,
        },
      });
    }
  }

  // 查看用户详情
  function showUserDetail(record) {
    record.activitiSync = record.activitiSync? Number(record.activitiSync) : 1;
    openDrawer(true, {
      record,
      isUpdate: true,
      showFooter: false,
    });
  }

  // 编辑用户信息
  function editUserInfo(record) {
    record.activitiSync = record.activitiSync? Number(record.activitiSync) : 1;
    //update-begin---author:wangshuai ---date:20260811  for：[LHZP-1124]【系统管理】部门管理-用户列表 在这里编辑用户时 部门和角色没展示出来------------
    openDrawer(true, { isUpdate: true, record, departDisabled: true, roleDisabled: true, departPostDisabled: true });
    //update-end---author:wangshuai ---date:20260811  for：[LHZP-1124]【系统管理】部门管理-用户列表 在这里编辑用户时 部门和角色没展示出来------------
  }

  // 选择添加已有用户
  function selectAddUser() {
    userSelectModalRef.value.rowSelection.selectedRowKeys = [];
    selUserModal.openModal();
  }

  // 选择用户成功
  async function onSelectUserOk(options, userIdList) {
    if (userIdList.length > 0) {
      try {
        setLoading(true);
        await linkDepartUserBatch(departId.value, userIdList);
        reload();
      } finally {
        setLoading(false);
      }
    }
  }

  /**
   * 用户抽屉表单成功回调
   */
  function onUserDrawerSuccess({ isUpdate, values }) {
    isUpdate ? updateTableDataRecord(values.id, values) : reload();
  }

  /**
   * 操作栏
   */
  function getTableAction(record): ActionItem[] {
    return [
      { label: '编辑', onClick: editUserInfo.bind(null, record) },
      { label: '详情', onClick: showUserDetail.bind(null, record) },
    ];
  }
</script>
