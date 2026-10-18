<template>
  <div class="depart-tree-container">
    <a-tree
      v-if="treeData.length > 0"
      showIcon
      autoExpandParent
      :treeData="treeData"
      :selectedKeys="selectedKeys"
      v-model:expandedKeys="expandedKeys"
      @select="onSelect"
    >
      <template #icon="{ selected }">
        <a-icon :style="{ color: selected ? 'blue' : '' }" type="apartment" />
      </template>
    </a-tree>
  </div>
</template>

<script lang="ts">
  import { ref, defineComponent } from 'vue';
  import { queryTreeList } from '/@/api/common/api';

  export default defineComponent({
    name: 'LeftDepart',
    emits: ['select'],
    setup(_, { emit }) {
      const treeData = ref<Recordable[]>([]);
      const selectedKeys = ref<string[]>([]);
      const expandedKeys = ref<string[]>([]);

      function onSelect(_, e) {
        let record = e.node.dataRef;
        selectedKeys.value = [record.key];
        emit('select', record.id);
      }

      loadTree();

      async function loadTree() {
        let result = await queryTreeList();
        treeData.value = [];
        result.forEach((node) => initialNode(node));
      }

      function initialNode(node, level = 1) {
        if (level === 1) {
          treeData.value.push(node);
          expandedKeys.value.push(node.id);
        }
        if (node.children && node.children.length > 0) {
          for (const childNode of node.children) {
            initialNode(childNode, level + 1);
          }
        }
      }

      function clearSelected() {
        selectedKeys.value = [];
      }

      return {
        treeData,
        expandedKeys,
        selectedKeys,
        clearSelected,
        onSelect,
      };
    },
  });
</script>

<style scoped lang="less">
  .depart-tree-container {
    max-height: 520px;
    overflow-y: auto;
  }
</style>
