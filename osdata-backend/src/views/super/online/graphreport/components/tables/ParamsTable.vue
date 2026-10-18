<template>
  <a-alert type="info" showIcon message="目前只有SQL模式才支持报表参数！" />
  <br />
  <JVxeTable
    ref="tableRef"
    toolbar
    rowNumber
    rowSelection
    dragSort
    sortKey="orderNum"
    :loading="loading"
    :columns="columns"
    :dataSource="dataSource"
    :height="336"
  />
</template>

<script lang="ts">
  import { ref, defineComponent } from 'vue';
  import { JVxeTypes, JVxeColumn, JVxeTableInstance } from '/@/components/jeecg/JVxeTable/types';

  export default defineComponent({
    name: 'FieldTable',
    props: {
      loading: Boolean,
      dataSource: Array,
    },
    setup() {
      const tableRef = ref<JVxeTableInstance>();
      const columns = ref<JVxeColumn[]>([
        {
          title: '参数',
          key: 'paramName',
          width: 230,
          type: JVxeTypes.input,
          defaultValue: '',
          placeholder: '请输入${title}',
          validateRules: [{ required: true, message: '${title}不能为空' }],
        },
        {
          title: '参数文本',
          key: 'paramTxt',
          width: 230,
          type: JVxeTypes.input,
          defaultValue: '',
          placeholder: '请输入${title}',
          validateRules: [{ required: true, message: '${title}不能为空' }],
        },
        {
          title: '默认值',
          key: 'paramValue',
          type: JVxeTypes.input,
          defaultValue: '',
          placeholder: '请输入${title}',
        },
      ]);
      return { tableRef, columns };
    },
  });
</script>

<style scoped></style>
