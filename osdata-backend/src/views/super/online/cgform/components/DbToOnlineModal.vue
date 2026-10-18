<template>
  <BasicModal @register="registerModal" :height="modalHeight" :width="500" title="从数据库导入表单" :confirmLoading="confirmLoading" @cancel="handleCancel" :wrapClassName="wrapClassName">
    <a-spin :spinning="confirmLoading">
      <BasicTable @register="registerTable" :rowSelection="rowSelection" @tableRedo="queryTables">
        <template #tableTitle>
          <div>
            注：导入表会排除配置前缀表
            <a href="https://help.jeecg.com/java/online/more/exclusionTable?_highlight=%E6%95%B0%E6%8D%AE%E5%BA%93" target="_blank"> 参考文档</a>
          </div>
        </template>
      </BasicTable>
    </a-spin>

    <template #footer>
      <a-button @click="handleCancel">关闭</a-button>
      <a-button @click="handleTrans" type="primary" preIcon="ant-design:swap" :loading="confirmLoading || btnLoading"> 生成表单 </a-button>
    </template>
  </BasicModal>
</template>

<script lang="ts">
  import { ref, defineComponent } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { BasicTable } from '/@/components/Table';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useListTable } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useDesign } from '/@/hooks/web/useDesign';

  enum Api {
    query = '/online/cgform/head/queryTables',
    trans = '/online/cgform/head/transTables/',
  }

  export default defineComponent({
    name: 'TransDb2Online',
    components: { BasicModal, BasicTable },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage: $message } = useMessage();
      const emptyText = ref('暂无数据');
      const confirmLoading = ref(false);
      const btnLoading = ref(false);
      const metaSource = ref<Recordable[]>([]);
      const dataSource = ref<Recordable[]>([]);

      const { prefixCls } = useDesign('online-db-import-form-modal');
      const wrapClassName = prefixCls;
      const clientHeight = document.documentElement.clientHeight;
      const modalH = clientHeight - 180;
      const modalHeight = modalH > 690 ? 690 : modalH;

      const [registerTable, { setPagination, getForm }, { rowSelection, selectedRowKeys }] = useListTable({
        bordered: true,
        columns: [{ title: '表名', align: 'left', dataIndex: 'id' }],
        dataSource: dataSource,
        maxHeight: clientHeight - 400,
        locale: { emptyText: emptyText },
        pagination: {
          showQuickJumper: false,
          showSizeChanger: false,
        },
        clickToRowSelect: true,
        showIndexColumn: true,
        showActionColumn: false,
        formConfig: {
          schemas: [
            {
              label: '表名',
              field: 'tableName',
              component: 'Input',
              componentProps: {
                style: { width: '100%' },
                placeholder: '请输入表名以模糊筛选',
                onChange: (e) => searchFilter(e.target.value),
              },
              disabledLabelWidth: true,
              itemProps: {
                labelCol: { sm: 0, md: 0 },
                wrapperCol: { sm: 24, md: 20 },
              },
            },
          ],
          baseColProps: { xs: 24, sm: 24, md: 24, lg: 24, xl: 24, xxl: 24 },
          showActionButtonGroup: false,
        },
      });

      const [registerModal, { closeModal }] = useModalInner(() => {
        // 重置form
        getForm()?.resetFields();

        btnLoading.value = false;
        emptyText.value = '暂无数据';
        selectedRowKeys.value = [];
        queryTables();
      });

      function queryTables() {
        confirmLoading.value = true;
        return defHttp
          .get(
            {
              url: Api.query,
            },
            {
              errorMessageMode: 'none',
            }
          )
          .then(
            (result) => {
              dataSource.value = result;
              metaSource.value = [...result];
              return result;
            },
            (e) => {
              if (e.message == 'noadminauth') {
                emptyText.value = '非admin用户无权限操作！';
                $message.warn(emptyText.value);
              } else {
                console.error(e);
              }
              dataSource.value = [];
              metaSource.value = [];
            }
          )
          .finally(() => {
            confirmLoading.value = false;
          });
      }

      function searchFilter(keyword) {
        if (metaSource.value.length === 0) return;
        if (!keyword) {
          dataSource.value = [...metaSource.value];
        } else {
          dataSource.value = metaSource.value.filter((item) => item.id.toLowerCase().includes(keyword.toLowerCase()));
          emptyText.value = dataSource.value.length === 0 ? '无筛选结果' : '暂无数据';
        }
        setPagination({ current: 1 });
      }

      function handleCancel() {
        closeModal();
      }

      function handleTrans() {
        if (!selectedRowKeys.value || selectedRowKeys.value.length == 0) {
          $message.warning('请选择一张表');
          return;
        } else {
          btnLoading.value = true;
          let tbNames = selectedRowKeys.value.join(',');
          defHttp
            .post({url: Api.trans + tbNames}, {errorMessageMode: "modal"})
            .then(() => {
              closeModal();
            })
            .finally(() => {
              btnLoading.value = false
              emit('success')
            });
        }
      }

      return {
        emptyText,
        confirmLoading,
        btnLoading,
        metaSource,
        handleTrans,
        handleCancel,
        queryTables,
        registerModal,
        registerTable,
        rowSelection,
        selectedRowKeys,
        wrapClassName,
        modalHeight,
      };
    },
  });
</script>

<style lang="less">
  // update-begin--author:liaozhiyang---date:20240110---for：【QQYUN-7838】online导入到表界面优化
  @prefix-cls: ~'@{namespace}-online-db-import-form-modal';
  .@{prefix-cls} {
    .ant-table-wrapper .ant-table-pagination.ant-pagination {
      margin-bottom: 0;
    }
    .jeecg-basic-table-form-container {
      padding-bottom: 0;
      .ant-form {
        padding: 0;
      }
    }
    .jeecg-basic-table .ant-table-wrapper {
      padding: 0;
    }
    .ant-form-item-label {width:40px;}
    .ant-modal {top: 20px;}
  }
  // update-end--author:liaozhiyang---date:20240110---for：【QQYUN-7838】online导入到表界面优化
</style>
