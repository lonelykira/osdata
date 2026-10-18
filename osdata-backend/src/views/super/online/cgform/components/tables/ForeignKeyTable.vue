<template>
  <JVxeTable
    ref="tableRef"
    rowNumber
    keyboardEdit
    :maxHeight="tableHeight.noToolbar"
    :loading="loading"
    :columns="columns"
    :dataSource="dataSource"
    :disabled="!actionButton"
    :disabledRows="{ dbFieldName: ['id', 'has_child'] }"
    v-bind="tableProps"
  />
</template>

<script lang="ts">
  import { ref, defineComponent } from 'vue';
  import { JVxeTypes, JVxeColumn } from '/@/components/jeecg/JVxeTable/types';
  import { useTableSync } from '../../hooks/useTableSync';

  export default defineComponent({
    name: 'ForeignKeyTable',
    props: {
      actionButton: { type: Boolean, default: true },
    },
    setup() {
      // 定义列信息
      const columns = ref<JVxeColumn[]>([
        { title: '字段名称', key: 'dbFieldName', width: 160 },
        { title: '字段备注', key: 'dbFieldTxt', width: 160 },
        {
          title: '主表名',
          key: 'mainTable',
          width: 280,
          type: JVxeTypes.input,
          defaultValue: '',
        },
        {
          title: '主表字段',
          key: 'mainField',
          width: 280,
          type: JVxeTypes.input,
          defaultValue: '',
        },
      ]);
      const setup = useTableSync(columns);
      return { ...setup, columns };
    },
  });
</script>

<style scoped></style>
