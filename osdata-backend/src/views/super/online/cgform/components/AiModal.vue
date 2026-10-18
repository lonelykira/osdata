<template>
  <BasicModal
    :height="180"
    :title="title"
    :width="600"
    :maskClosable="false"
    v-bind="$attrs"
    @register="registerModal"
    :footer="null"
    @cancel="handleCancel"
  >
    <div class="aiWrap">
      <div class="titleArea">
        <svg
          t="1707100353985"
          class="icon"
          viewBox="0 0 1024 1024"
          version="1.1"
          xmlns="http://www.w3.org/2000/svg"
          p-id="4235"
          width="26"
          height="26"
        >
          <path
            d="M512 64C264.8 64 64 264.8 64 512s200.8 448 448 448 448-200.8 448-448S759.2 64 512 64z m32 704h-64v-64h64v64z m11.2-203.2l-5.6 4.8c-3.2 2.4-5.6 8-5.6 12.8v58.4h-64v-58.4c0-24.8 11.2-48 29.6-63.2l5.6-4.8c56-44.8 83.2-68 83.2-108C598.4 358.4 560 320 512 320c-49.6 0-86.4 36.8-86.4 86.4h-64C361.6 322.4 428 256 512 256c83.2 0 150.4 67.2 150.4 150.4 0 72.8-49.6 112.8-107.2 158.4z"
            p-id="4236"
            fill="currentColor"
          ></path>
        </svg>
        <h3>创建工作表字段需要专业建议?试试AI智能推荐吧</h3>
      </div>
      <p class="tip">可尝试添加修饰词，如:智能家居行业的生产计划表</p>
      <div class="content">
        <a-input v-model:value.trim="inputValue" placeholder="请输入修饰词" /><a-button :loading="loading" type="primary" @click="handleCreate"
          >生成表</a-button
        >
      </div>
    </div>
  </BasicModal>
</template>

<script setup>
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ref } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  const configUrl = {
    aigc: '/online/cgform/api/aigc',
  };
  const emit = defineEmits(['register', 'success']);
  const [registerModal, { closeModal }] = useModalInner();
  const { createMessage } = useMessage();
  const inputValue = ref('');
  const loading = ref(false);
  const title = ref('ai创建表');
  
  const handleCancel = () => {
    closeModal();
  };
  const handleCreate = () => {
    if (inputValue.value.trim() === '') {
      createMessage.warning('请输入修饰词~');
    } else {
      loading.value = true;
      defHttp
        // timeout：超时时间 5 分钟
        .post({ url: `${configUrl.aigc}?prompt=${inputValue.value}`, timeout: 1e3 * 60 * 5 })
        .then((res) => {
          loading.value = false;
          handleCancel();
          emit('success');
          inputValue.value = '';
        })
        .catch((err) => {
          loading.value = false;
        });
    }
  };
</script>

<style lang="less" scoped>
  html[data-theme="light"] {
    .aiWrap {
      h3 {color: #333;}
      .tip { color: #666;}
    }
  }
  .aiWrap {
    padding: 30px 20px 20px;
    .titleArea {
      position: relative;
      color: #ffb308;
      width: fit-content;
      margin:0 auto;
      svg {
        position: absolute;
        left: -30px;
        margin-right: 8px;
      }
    }
    h3 {
      text-align: center;
      margin-bottom: 0;
    }
    .tip {
      text-align: center;
      margin-bottom: 26px;
    }
    .content {
      display: flex;
    }
    .ant-btn {
      margin-left: 16px;
    }
  }
</style>
