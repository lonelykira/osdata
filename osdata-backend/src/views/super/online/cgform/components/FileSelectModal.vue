<template>
  <BasicModal @register="registerModal" title="选择目录" :width="500" @ok="onSubmit" @cancel="onCancel">
    <a-spin :spinning="loading">
      <div class="btnArea">
        <a-button @click="hanldeRefresh">刷新</a-button>
      </div>
      <a-directory-tree v-if="directoryTreeShow" :treeData="treeData" :loadData="onLoadData" @select="onSelect" />
    </a-spin>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  enum Api {
    fileTreeUrl = '/online/cgform/head/fileTree',
    rootFileUrl = '/online/cgform/head/rootFile',
  }

  export default defineComponent({
    name: 'FileSelectModal',
    components: { BasicModal },
    emits: ['select', 'register'],
    setup(_, { emit }) {
      const loading = ref(true);
      const treeData = ref<any[]>([]);
      const selectedKey = ref('');
      const directoryTreeShow = ref(false);

      const [registerModal, { closeModal }] = useModalInner(async () => {
        selectedKey.value = '';
        if (treeData.value.length === 0) {
          loadRoot();
        }
      });

      function onSubmit() {
        emit('select', selectedKey.value);
        closeModal();
      }

      function onCancel() {
        closeModal();
      }

      async function loadRoot() {
        loading.value = true;
        treeData.value = await defHttp.get({ url: Api.rootFileUrl }).finally(() => {
          loading.value = false;
          directoryTreeShow.value = true;
        });
      }

      async function onLoadData(treeNode) {
        if (treeNode.dataRef.children) {
          return;
        }
        let params = {
          parentPath: treeNode.dataRef.key,
        };
        treeNode.dataRef.children = await defHttp.get({ url: Api.fileTreeUrl, params });
        treeData.value = [...treeData.value];
      }

      function onSelect(selectedKeys) {
        selectedKey.value = selectedKeys[0];
      }
      // update-begin--author:liaozhiyang---date:20231017---for：【QQYUN-6102】新建文件夹需要刷新
      const hanldeRefresh = () => {
        selectedKey.value = '';
        directoryTreeShow.value = false;
        loadRoot();
      };
      // update-begin--author:liaozhiyang---date:20231017---for：【QQYUN-6102】新建文件夹需要刷新
      return { loading, treeData, onLoadData, onSelect, onSubmit, onCancel, registerModal, hanldeRefresh, directoryTreeShow };
    },
  });
</script>

<style lang="less" scoped>
  .btnArea {
    margin-bottom: 10px;
    text-align: left;
  }
</style>
