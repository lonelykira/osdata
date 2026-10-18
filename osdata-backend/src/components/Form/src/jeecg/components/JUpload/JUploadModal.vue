<template>
  <BasicModal @register="registerModal" :title="modalTitle" :width="width" :minHeight="160" @ok="handleOk" v-bind="$attrs">
    <div class="j-upload-modal-content">
      <JUpload ref="uploadRef" :value="value" v-bind="uploadBinds.props" @change="emitValue" />
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, unref, reactive, computed, nextTick } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import JUpload from './JUpload.vue';
  import { UploadTypeEnum } from './upload.data';
  import { propTypes } from '/@/utils/propTypes';

  const emit = defineEmits(['change', 'update:value', 'register']);
  defineProps({
    value: propTypes.oneOfType([propTypes.string, propTypes.array]),
    width: propTypes.number.def(520),
  });

  const uploadRef = ref();
  const uploadBinds = reactive({ props: {} as any });
  const modalTitle = computed(() => (uploadBinds.props?.fileType === UploadTypeEnum.image ? '图片上传' : '文件上传'));

  // 注册弹窗
  const [registerModal, { closeModal }] = useModalInner(async (data) => {
    uploadBinds.props = unref(data) || {};
    if ([UploadTypeEnum.image, 'img', 'picture'].includes(uploadBinds.props?.fileType)) {
      uploadBinds.props.fileType = UploadTypeEnum.image;
    } else {
      uploadBinds.props.fileType = UploadTypeEnum.file;
    }
    nextTick(() => uploadRef.value.addActionsListener());
  });

  function handleOk() {
    closeModal();
  }

  function emitValue(value) {
    emit('change', value);
    emit('update:value', value);
  }
</script>

<style lang="less">
  .j-upload-modal-content {
    .ant-upload-list-text {
      display: flex;
      flex-direction: column;
      gap: 6px;
      margin-top: 12px;

      .ant-upload-list-item-container {
        margin: 0;
      }

      .ant-upload-list-item {
        min-height: 38px;
        margin: 0;
        padding: 6px 10px;
        border: 1px solid #f0f0f0;
        border-radius: 6px;
        background-color: #fafafa;
        transition:
          border-color 0.2s,
          background-color 0.2s;

        &:hover {
          border-color: fade(@primary-color, 25%);
          background-color: fade(@primary-color, 4%);
        }
      }

      .ant-upload-list-item-name {
        min-width: 0;
        overflow: hidden;
        color: rgba(0, 0, 0, 0.65);
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .ant-upload-text-icon {
        color: rgba(0, 0, 0, 0.45);
      }

      .ant-upload-list-item:hover {
        .ant-upload-list-item-name,
        .ant-upload-text-icon {
          color: @primary-color;
        }
      }
    }
  }
</style>
