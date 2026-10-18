<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'openapi:open_api_auth:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button type="primary" v-auth="'openapi:open_api_auth:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button type="primary" v-auth="'openapi:open_api_auth:importExcel'" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'openapi:open_api_auth:deleteBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
        <!-- 高级查询 -->
        <super-query :config="superQueryConfig" @search="handleSuperQuery" />
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="{ column, record, index, text }">
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <OpenApiAuthDrawer @register="registerAuthDrawer" @success="handleSuccess" />
    <AuthDrawer @register="registerPermDrawer" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="openapi-openApiAuth" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useDrawer } from '/@/components/Drawer';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { columns, searchFormSchema, superQuerySchema } from './OpenApiAuth.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl, getGenAKSK, saveOrUpdate } from './OpenApiAuth.api';
  import OpenApiAuthDrawer from './components/OpenApiAuthDrawer.vue';
  import AuthDrawer from './components/AuthDrawer.vue';

  const queryParam = reactive<any>({});
  const { createMessage } = useMessage();
  const [registerAuthDrawer, { openDrawer: openAuthDrawer }] = useDrawer();
  const [registerPermDrawer, { openDrawer: openPermDrawer }] = useDrawer();

  //注册table数据
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: '授权管理',
      api: list,
      columns,
      canResize: false,
      formConfig: {
        schemas: searchFormSchema,
        autoSubmitOnEnter: true,
        showAdvancedButton: true,
        fieldMapToNumber: [],
        fieldMapToTime: [],
      },
      actionColumn: {
        width: 220,
        fixed: 'right',
      },
      beforeFetch: (params) => {
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: '授权管理',
      url: getExportUrl,
      params: queryParam,
    },
    importConfig: {
      url: getImportUrl,
      success: handleSuccess,
    },
  });
  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  // 高级查询配置
  const superQueryConfig = reactive(superQuerySchema);

  /**
   * 高级查询事件
   */
  function handleSuperQuery(params) {
    Object.keys(params).map((k) => {
      queryParam[k] = params[k];
    });
    reload();
  }

  /**
   * 新增事件
   */
  function handleAdd() {
    openAuthDrawer(true, {
      isUpdate: false,
      showFooter: true,
    });
  }

  /**
   * 编辑事件
   */
  function handleEdit(record: Recordable) {
    openAuthDrawer(true, {
      record,
      isUpdate: true,
      showFooter: true,
    });
  }

  /**
   * 授权事件
   */
  function handleAuth(record: Recordable) {
    openPermDrawer(true, { record });
  }

  /**
   * 重置AK/SK
   */
  async function handleReset(record: Recordable) {
    const AKSKObj = await getGenAKSK({});
    record.ak = AKSKObj[0];
    record.sk = AKSKObj[1];
    await saveOrUpdate(record, true);
    reload();
  }

  /**
   * 详情
   */
  function handleDetail(record: Recordable) {
    openAuthDrawer(true, {
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
   * 操作栏
   */
  /**
   * 复制密钥
   */
  function handleCopyKeys(record: Recordable) {
    const text = `访问密钥（AK）: ${record.ak}\n签名密钥（SK）: ${record.sk}`;
    const isSuccess = copyTextToClipboard(text);
    if (isSuccess) {
      createMessage.success('密钥已复制到剪贴板');
    } else {
      createMessage.error('复制失败，请手动复制');
    }
  }

  function getTableAction(record) {
    return [
      {
        label: '复制密钥',
        onClick: handleCopyKeys.bind(null, record),
      },
      {
        label: '分配接口',
        onClick: handleAuth.bind(null, record),
        auth: 'openapi:open_api_auth:edit',
      },
    ];
  }

  /**
   * 下拉操作栏
   */
  function getDropDownAction(record) {
    return [
      {
        label: '修改对象',
        onClick: handleEdit.bind(null, record),
        auth: 'openapi:open_api_auth:edit',
      },
      {
        label: '重置密钥',
        popConfirm: {
          title: '原密钥将失效，确认重置？',
          confirm: handleReset.bind(null, record),
          placement: 'topLeft',
        },
        auth: 'openapi:open_api_auth:edit',
      },
      {
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        },
        auth: 'openapi:open_api_auth:delete',
      },
    ];
  }
</script>

<style lang="less" scoped>
  :deep(.ant-picker),:deep(.ant-input-number) {
    width: 100%;
  }
</style>
