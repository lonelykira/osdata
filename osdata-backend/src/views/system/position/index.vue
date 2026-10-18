<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">新增</a-button>
        <a-popover placement="bottomLeft" trigger="hover">
          <template #content>
            <div class="position-level-popover">
              <div class="position-level-popover__title">流程职级表达式仅识别 6 个预设职级</div>
              <div class="position-level-popover__hint">职务等级名称及等级序号须严格匹配，数字越小职级越高。</div>
              <div class="position-level-popover__warning">
                这 6 个预设职级不允许修改或删除。其他职务可以正常维护，但不能用于流程职级表达式，否则流程将无法匹配审批人。
              </div>
              <div class="position-level-popover__rules">
                <div><span>配置</span>候选职级只能配置在部门上，不能直接关联用户。</div>
                <div><span>概念</span>用户资料中的“职务”来自字典，与这里的职务等级是两个独立概念。</div>
              </div>
              <div class="position-level-popover__grid">
                <div v-for="item in positionLevelTips" :key="item.level" class="position-level-popover__item">
                  <span class="position-level-popover__level">{{ item.level }}</span>
                  <span class="position-level-popover__name">{{ item.name }}</span>
                  <span :class="['position-level-popover__type', `is-${item.type}`]">{{ item.typeName }}</span>
                </div>
              </div>
            </div>
          </template>
          <a-button class="position-level-help" preIcon="ant-design:info-circle-outlined">流程仅支持 6 个预设职级</a-button>
        </a-popover>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
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
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <PositionModal @register="registerModal" @success="reload" />
  </div>
</template>
<script lang="ts" name="system-position" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { getPositionList, deletePosition, batchDeletePosition, customUpload, getExportUrl, getImportUrl } from './position.api';
  import { columns, searchFormSchema } from './position.data';
  import PositionModal from './PositionModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';
  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const positionLevelTips = [
    { id: '1958471111989067778', name: '董事长', level: 1, type: 'leader', typeName: '领导层级' },
    { id: '1958471074953363458', name: '总经理', level: 2, type: 'leader', typeName: '领导层级' },
    { id: '1958471030867034113', name: '副总经理', level: 3, type: 'leader', typeName: '领导层级' },
    { id: '1958470912214368258', name: '部长', level: 4, type: 'staff', typeName: '职员层级' },
    { id: '1958470865577902082', name: '副部长', level: 5, type: 'staff', typeName: '职员层级' },
    { id: '1958470823064436737', name: '职员', level: 6, type: 'staff', typeName: '职员层级' },
  ];

  // 列表页面公共参数、方法
  const { prefixCls, tableContext } = useListPage({
    designScope: 'position-template',
    tableProps: {
      title: '职务列表',
      api: getPositionList,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
        labelWidth: 80,
      },
      actionColumn: {
        width: 180,
      },
      showIndexColumn: true,
      //update-begin---author:scott ---date:20260806  for：【LHZP-1183】限制流程预设职级修改和删除-----------
      rowSelection: {
        type: 'checkbox',
        getCheckboxProps: (record) => ({
          disabled: isDefaultPosition(record),
        }),
      },
      //update-end---author:scott ---date:20260806  for：【LHZP-1183】限制流程预设职级修改和删除-----------
      defSort: {
        column: "",
        order: ""
      }
    },
    exportConfig: {
      name: '职务列表',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  /**
   * 操作列定义
   * @param record
   */
  function getActions(record) {
    //update-begin---author:scott ---date:20260806  for：【LHZP-1183】限制流程预设职级修改和删除-----------
    const isDefault = isDefaultPosition(record);
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        disabled: isDefault,
        tooltip: isDefault ? '流程预设职级不允许修改' : undefined,
      },
      {
        label: '删除',
        disabled: isDefault,
        tooltip: isDefault ? '流程预设职级不允许删除' : undefined,
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
    //update-end---author:scott ---date:20260806  for：【LHZP-1183】限制流程预设职级修改和删除-----------
  }

  /**
   * 根据系统初始化数据ID判断是否为流程内置职级
   * @author scott
   * @since 2026-08-06 LHZP-1183
   */
  function isDefaultPosition(record) {
    return positionLevelTips.some((item) => item.id === record?.id);
  }

  /**
   * 新增事件
   */
  function handleAdd() {
    openModal(true, {
      isUpdate: false,
    });
  }

  /**
   * 编辑事件
   */
  function handleEdit(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * 删除事件
   */
  async function handleDelete(record) {
    await deletePosition({ id: record.id }, reload);
  }

  /**
   * 批量删除事件
   */
  async function batchHandleDelete() {
    await batchDeletePosition({ ids: selectedRowKeys.value }, () => {
      // 代码逻辑说明: 【QQYUN-8334】批量删除之后，按钮未隐藏，选中记录还在
      selectedRowKeys.value = [];
      reload();
    });
  }
</script>
<style lang="less" scoped>
  .position-level-help {
    color: #ad6800;
    border-color: #ffd591;
    background: #fffaf0;

    &:hover,
    &:focus {
      color: #873800;
      border-color: #faad14;
      background: #fff7e6;
    }
  }

  .position-level-popover {
    width: 480px;
    max-width: calc(100vw - 48px);
    padding: 4px;

    &__title {
      color: #262626;
      font-weight: 600;
    }

    &__hint {
      margin-top: 4px;
      color: #8c8c8c;
      font-size: 12px;
    }

    &__grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 8px;
      margin-top: 12px;
    }

    &__warning {
      margin-top: 10px;
      padding: 7px 9px;
      color: #ad4e00;
      font-size: 12px;
      line-height: 1.6;
      border-left: 3px solid #faad14;
      border-radius: 0 4px 4px 0;
      background: #fffbe6;
    }

    &__rules {
      display: grid;
      gap: 6px;
      margin-top: 10px;
      color: #595959;
      font-size: 12px;
      line-height: 1.6;

      span {
        display: inline-block;
        margin-right: 7px;
        padding: 0 5px;
        color: #0958d9;
        line-height: 18px;
        border-radius: 3px;
        background: #e6f4ff;
      }
    }

    &__item {
      display: flex;
      align-items: center;
      min-width: 0;
      padding: 7px 8px;
      border: 1px solid #f0f0f0;
      border-radius: 6px;
      background: #fafafa;
    }

    &__level {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      flex: 0 0 22px;
      height: 22px;
      margin-right: 8px;
      color: #fff;
      font-size: 12px;
      font-weight: 600;
      border-radius: 50%;
      background: #1677ff;
    }

    &__name {
      flex: 1;
      color: #262626;
      white-space: nowrap;
    }

    &__type {
      margin-left: 6px;
      padding: 1px 5px;
      font-size: 11px;
      white-space: nowrap;
      border-radius: 4px;

      &.is-leader {
        color: #0958d9;
        background: #e6f4ff;
      }

      &.is-staff {
        color: #237804;
        background: #f6ffed;
      }
    }
  }
</style>
