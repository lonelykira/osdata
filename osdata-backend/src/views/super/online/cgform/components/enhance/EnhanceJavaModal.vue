<template>
  <BasicModal @register="registerModal" title="JAVA增强" :width="1200" defaultFullscreen @cancel="onCancel">
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
        <a-button @click="onGenEnhanceJavaData">生成测试数据</a-button>
      </div>
    </template>

    <!-- 表单区域 -->
    <BasicModal v-bind="formModalProps">
      <a-spin :spinning="formModalProps.confirmLoading">
        <BasicForm @register="registerForm" />
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
  import { useJavaColumns, useJavaFormSchemas } from './enhance.data';
  import { doEnhanceJavaBatchDelete, getEnhanceJavaByCode, saveEnhanceJava } from './enhance.api';

  export default defineComponent({
    name: 'EnhanceJavaModal',
    components: { BasicModal, BasicTable, BasicForm, TableAction },
    emits: ['register'],
    setup() {
      const code = ref('');
      const btnList = ref<any[]>([]);
      const { columns } = useJavaColumns(btnList);
      // 列表页面公共参数、方法
      const { doRequest, doDeleteRecord, tableContext } = useListPage({
        tableProps: {
          api: async (params) => {
            let { dataSource, btnList: $btnList } = await getEnhanceJavaByCode(code.value, params);
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
      const [registerTable, { reload, getDataSource }, { rowSelection, selectedRowKeys }] = tableContext;
      // 注册弹窗
      const [registerModal, { closeModal }] = useModalInner(async (data) => {
        code.value = data.row.id;
        reload();
      });
      // useOnlineAiTest
      const { aiTestMode, genEnhanceJavaData } = useOnlineTest({}, { reload, getDataSource }, null);
      // 注册 form 弹窗
      const [registerFormModal, formModal] = useModal();
      const isUpdate = ref(false);
      const formModalProps = reactive({
        onRegister: registerFormModal,
        title: computed(() => (isUpdate?.value ? '修改' : '新增')),
        width: 800,
        confirmLoading: false,
        bodyStyle: { height: '350px' },
        onOk: onSubmit,
        onCancel: formModal.closeModal,
      });
      let formRecord = {};
      const { formSchemas } = useJavaFormSchemas(btnList);
      // 注册 form
      const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        // update-begin--author:liaozhiyang---date:20231017---for：【issues/790】弹窗内文本框不居中问题
        labelCol: { xs: 24, sm: 5 },
        wrapperCol: { xs: 24, sm: 16 },
        // update-end--author:liaozhiyang---date:20231017---for：【issues/790】弹窗内文本框不居中问题
      });

      function onCancel() {
        closeModal();
      }

      async function openFormModal(data) {
        isUpdate.value = data.isUpdate;
        formRecord = { ...(data.record ?? {}) };
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

      function onGenEnhanceJavaData() {
        // update-begin--author:liaozhiyang---date:20260714---for：【LHZP-250】生成测试数据需要过滤掉页面中已添加按钮的java增强
        const existingButtonCodes = (getDataSource?.() || []).map((item) => item.buttonCode).filter(Boolean);
        // 把 btnList 转为 {buttonCode: buttonName} map，提示时用中文名更友好
        const buttonNameMap = (btnList.value || []).reduce((acc, item) => {
          if (item.buttonCode) acc[item.buttonCode] = item.buttonName || item.buttonCode;
          return acc;
        }, {});
        genEnhanceJavaData(code.value, existingButtonCodes, buttonNameMap);
        // update-end--author:liaozhiyang---date:20260714---for：【LHZP-250】生成测试数据需要过滤掉页面中已添加按钮的java增强
      }

      // 批量删除事件
      async function onBatchDelete() {
        doRequest(() => doEnhanceJavaBatchDelete(selectedRowKeys.value));
      }

      async function onSubmit() {
        try {
          formModalProps.confirmLoading = true;
          let values = await validate();
          values = Object.assign({}, formRecord, values);
          await saveEnhanceJava(code.value, values, isUpdate.value);
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
              confirm: () => doDeleteRecord(() => doEnhanceJavaBatchDelete([record.id])),
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
        onGenEnhanceJavaData,
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
<style lang="less" scoped></style>
