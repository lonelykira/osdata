<template>
  <BasicModal @register="registerModal" title="SQL增强" :width="1200" defaultFullscreen @cancel="onCancel">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button @click="onAdd" type="primary" preIcon="ant-design:plus">新增</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="onBatchDelete">
                <a-icon type="delete" />
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button style="margin-left: 8px">
            批量操作
            <a-icon type="down" />
          </a-button>
        </a-dropdown>
      </template>

      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>

    <template #footer>
      <a-button @click="onCancel">关闭</a-button>
      <div v-if="aiTestMode" style="float: left">
        <a-button @click="onGenEnhanceSqlData">生成测试数据</a-button>
      </div>
    </template>

    <!-- 表单区域 -->
    <BasicModal v-bind="formModalProps">
      <a-spin :spinning="formModalProps.confirmLoading">
        <BasicForm class="popupForm" @register="registerForm" />
      </a-spin>
    </BasicModal>
  </BasicModal>
</template>

<script lang="ts">
  import { ref, reactive, computed, nextTick, defineComponent } from 'vue';
  import { BasicModal, useModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { TableAction, BasicTable, ActionItem } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useOnlineTest } from '../../hooks/aitest/useOnlineTest';
  import { useSqlColumns, useSqlFormSchemas } from './enhance.data';
  import { doEnhanceSqlBatchDelete, getEnhanceSqlByCode, saveEnhanceSql } from './enhance.api';

  export default defineComponent({
    name: 'EnhanceSqlModal',
    components: { BasicModal, BasicTable, BasicForm, TableAction },
    emits: ['register'],
    setup() {
      const code = ref('');
      const tableName = ref('');
      const btnList = ref<any[]>([]);
      const { columns } = useSqlColumns(btnList);
      // 列表页面公共参数、方法
      const { doRequest, doDeleteRecord, tableContext } = useListPage({
        tableProps: {
          api: async (params) => {
            let { dataSource, btnList: $btnList } = await getEnhanceSqlByCode(code.value, params);
            btnList.value = $btnList;
            return dataSource;
          },
          columns,
          canResize: false,
          useSearchForm: false,
          beforeFetch(params) {
            return Object.assign(params, { column: 'orderNum', order: 'asc' });
          },
        },
      });
      // 注册table数据
      const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;
      // 注册弹窗
      const [registerModal, { closeModal }] = useModalInner(async (data) => {
        code.value = data.row.id;
        tableName.value = data.row.tableName;
        reload();
      });
      // useOnlineAiTest
      const { aiTestMode, genEnhanceSqlData } = useOnlineTest({}, { reload }, null);
      // 注册 form 弹窗
      const [registerFormModal, formModal] = useModal();
      const isUpdate = ref(false);
      const formModalProps = reactive({
        onRegister: registerFormModal,
        title: computed(() => (isUpdate?.value ? '修改' : '新增')),
        width: 800,
        confirmLoading: false,
        onOk: onSubmit,
        onCancel: formModal.closeModal,
      });
      let formRecord: Recordable = {};
      const { formSchemas } = useSqlFormSchemas(btnList);
      // 注册 form
      const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        // update-begin--author:liaozhiyang---date:20231017---for：【issues/790】弹窗内文本框不居中问题
        labelWidth: 80,
        labelCol: null,
        wrapperCol: null,
        // update-end--author:liaozhiyang---date:20231017---for：【issues/790】弹窗内文本框不居中问题
      });

      function onCancel() {
        closeModal();
      }

      async function openFormModal(data) {
        isUpdate.value = data.isUpdate;
        formRecord = { ...(data.record ?? {}) };
        // update-begin--author:jeecg---date:20260513---for：【QQYUN-15337】online表单SQL增强支持指定数据源：dbSource 为空/null 时归一为 ''，下拉回显「跟随表单数据源」
        formRecord.dbSource = formRecord.dbSource || '';
        // update-end--author:jeecg---date:20260513---for：【QQYUN-15337】online表单SQL增强支持指定数据源
        formModal.openModal();
        await nextTick();
        await resetFields();
        setFieldsValue(formRecord);
      }

      function onAdd() {
        openFormModal({ isUpdate: false });
      }

      // 编辑按钮
      function onEdit(record) {
        openFormModal({ isUpdate: true, record });
      }

      function onGenEnhanceSqlData() {
        genEnhanceSqlData(code.value, tableName.value);
      }

      // 批量删除事件
      async function onBatchDelete() {
        doRequest(() => doEnhanceSqlBatchDelete(selectedRowKeys.value));
      }

      async function onSubmit() {
        try {
          formModalProps.confirmLoading = true;
          let values = await validate();
          values = Object.assign({}, formRecord, values);
          await saveEnhanceSql(code.value, values, isUpdate.value);
          reload();
          formModal.closeModal();
        } finally {
          formModalProps.confirmLoading = false;
        }
      }

      /**
       * 操作栏
       */
      function getTableAction(record) {
        return [
          {
            label: '编辑',
            onClick: () => onEdit(record),
          },
        ];
      }

      /**
       * 下拉操作栏
       */
      function getDropDownAction(record): ActionItem[] {
        return [
          {
            label: '删除',
            popConfirm: {
              title: '确定删除吗？',
              placement: 'left',
              confirm: () => doDeleteRecord(() => doEnhanceSqlBatchDelete([record.id])),
            },
          },
        ];
      }

      return {
        rowSelection,
        selectedRowKeys,
        aiTestMode,
        onCancel,
        onAdd,
        onGenEnhanceSqlData,
        onBatchDelete,
        getTableAction,
        getDropDownAction,
        formModalProps,
        registerModal,
        registerTable,
        registerForm,
      };
    },
    computed: {
      tableScroll() {
        let height = window.innerHeight;
        return {
          y: height - 320,
        };
      },
    },
  });
</script>
<style lang="less" scoped>
.popupForm{ padding-left: 40px;padding-right:40px; }
</style>
