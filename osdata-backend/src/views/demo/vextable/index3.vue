<template>
  <a-card :key="key">
    <div class="container">
      <p class="title">vxe-table 原生加载示例</p>
      <template v-if="isRegistered">
        <vxe-table :align="allAlign" :data="tableData1">
          <vxe-table-column type="seq" width="60"></vxe-table-column>
          <vxe-table-column field="name" title="Name"></vxe-table-column>
          <vxe-table-column field="sex" title="Sex"></vxe-table-column>
          <vxe-table-column field="age" title="Age"></vxe-table-column>
        </vxe-table>
      </template>
    </div>
  </a-card>
</template>
<script lang="ts" setup>
  import { ref } from 'vue';
  import { useVxeTableRegister } from '/@/components/jeecg/JVxeTable/useVxeTableRegister';
  const allAlign = ref<string | null>(null);
  const tableData1 = ref([
    { id: 10001, name: 'Test1', role: 'Develop', sex: 'Man', age: 28, address: 'vxe-table 从入门到放弃' },
    { id: 10002, name: 'Test2', role: 'Test', sex: 'Women', age: 22, address: 'Guangzhou' },
    { id: 10003, name: 'Test3', role: 'PM', sex: 'Man', age: 32, address: 'Shanghai' },
    { id: 10004, name: 'Test4', role: 'Designer', sex: 'Women', age: 24, address: 'Shanghai' },
  ]);
  const key = ref(0);
  const isRegistered = ref(false);
  useVxeTableRegister().then(() => {
    console.log('useVxeTableRegister');
    isRegistered.value = true;
    // vxetable放在插槽中需要更新key才能重新渲染
    key.value++;
  });
</script>

<style lang="less" scoped>
  .container {
    padding: 5px;
    background-color: #fff;
    .title {
      font-size: 16px;
      font-weight: bold;
      margin-bottom: 10px;
    }
  }
</style>
