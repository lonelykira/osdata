<template>
  <div :class="['p-2']" class="online-wrap">
    <Card :bordered="false">
      <spin :spinning="loading">
        <div :class="['wrap', `online-detail-${ID}`]" ref="wrapRef">
          <OnlineForm v-if="false"></OnlineForm>
          <!-- 详情弹框 -->
          <OnlineDetailModal
            v-if="token"
            :id="ID"
            :maskClosable="false"
            @register="registerDetailModal"
            @success="success"
            :getContainer="getContainer"
            :height="contentHeight"
          />
        </div>
      </spin>
    </Card>
  </div>
</template>

<script setup lang="ts">
  import { ref, watch, unref } from 'vue';
  import { Card, Spin } from 'ant-design-vue';
  // OnlineForm 这个必须引用，setup模式必须在template里面使用。否则url进入会报错
  import OnlineForm from '../comp/OnlineForm.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useRoute } from 'vue-router';
  import OnlineDetailModal from './OnlineDetailModal.vue';
  import { useOnlineTableContext } from '../../hooks/auto/useOnlineTableContext';
  import { useListButton } from '../../hooks/auto/useListButton';
  import { useEnhance } from '../../hooks/auto/useEnhance';
  import { useFormUrl } from '../../hooks/auto/useFormUrl';
  const route = useRoute();
  const { ID, onlineTableContext, onlineExtConfigJson } = useOnlineTableContext();
  const { createMessage: $message } = useMessage();
  const wrapRef = ref(null);
  const contentHeight = ref(400);
  const loading = ref(true);

  const { token } = useFormUrl();
  if (!unref(token)) {
    throw new Error('token不存在~');
  }
  if (!ID.value || !route.params.dataId) {
    $message.warning('地址错误, 配置ID不存在!');
    throw new Error('地址错误, 配置ID不存在!');
  }
  watch(
    () => wrapRef.value,
    (elem: HTMLElement) => {
      // 获取页面实际高度
      contentHeight.value = elem.offsetHeight;
    }
  );
  // 处理增强
  let { initCgEnhanceJs } = useEnhance(onlineTableContext);
  // 处理列表button
  const { registerDetailModal, openDetailModal } = useListButton(onlineTableContext, onlineExtConfigJson);
  const getContainer = (node) => {
    return document.querySelector(`.online-detail-${ID.value}`);
  };
  setTimeout(() => {
    openDetailModal(true, {
      isUpdate: true,
      disableSubmit: true,
      record: {
        id: route.params.dataId,
      },
    });
    loading.value = false;
  }, 1e3);
</script>

<style lang="less" scoped>
  .online-wrap {
    height: 100%;
    position: relative;
    :deep(.ant-card) {
      padding: 0;
      height: 100%;
      border: none;
      .ant-card-body {
        height: 100%;
        padding: 5px;
      }
    }
    :deep(.ant-spin-nested-loading) {
      height: 100%;
      width: 100%;
      .ant-spin-container {
        height: 100%;
      }
    }
  }
  .wrap {
    height: 100%;
    > div {
      height: 100%;
    }
    :deep(.ant-modal-root) {
      height: 100%;
      .ant-modal-mask {
        height: 0;
      }
      .ant-modal-wrap {
        position: static;
        height: 100%;
      }
      .ant-modal {
        position: static;
        height: 100%;
        width: 100% !important;
        height: 100% !important;
        transform: scale(1) !important;
        opacity: 1;
        animation-name: none !important;
      }
      .ant-modal-content {
        position: static;
        box-shadow: none;
        .scroll-container {
          padding: 0;
          .scrollbar__view > div {
            overflow: auto;
          }
        }
      }
      .ant-modal-header,
      .ant-modal-footer {
        display: none;
      }
      .ant-modal-close {
        display: none;
      }
    }
  }
</style>
