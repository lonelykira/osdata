<template>
  <BasicModal
    :title="title"
    @cancel="
      () => {
        restTabIndex();
      }
    "
    :width="modalWidth"
    :maxHeight="600"
    :enableComment="enableComment"
    :defaultFullscreen="false"
    v-bind="$attrs"
    @register="registerModal"
    wrapClassName="jeecg-online-detail-modal"
  >
    <template #title>
      <div class="titleArea">
        <div class="title">{{ title }}</div>
        <div class="right">
          <a-radio-group v-model:value="tabIndex">
            <a-radio-button v-for="(item, index) in tabNav" :value="tabValue(index)" :key="item.tableName">{{ item.tableTxt }}</a-radio-button>
          </a-radio-group>
        </div>
      </div>
    </template>
    <template #footer>
      <a-button
        key="back"
        @click="
          () => {
            handleCancel();
            restTabIndex();
          }
        "
        >关闭</a-button
      >
    </template>
    <online-form-detail
      ref="onlineFormCompRef"
      :id="id"
      :form-template="formTemplate"
      :show-sub="showSub"
      :themeTemplate="themeTemplate"
      :tabIndex="tabIndex"
      @rendered="renderSuccess"
    />

    <template #comment>
      <comment-panel ref="commentPanelRef" :tableId="tableId" :tableName="tableName" :dataId="formDataId"></comment-panel>
    </template>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, watch, ref } from 'vue';
  import { BasicModal } from '/@/components/Modal';
  import OnlineFormDetail from './OnlineTabFormDetail.vue';
  import { useAutoModal } from '../../../hooks/auto/useAutoModal';
  import CommentPanel from '/@/components/jeecg/comment/CommentPanel.vue';
  import { TAB } from '../../../util/constant';

  export default defineComponent({
    name: 'OnlineTabDetailModal',
    props: {
      id: {
        type: String,
        required: false,
        default: '',
      },
    },
    components: {
      BasicModal,
      OnlineFormDetail,
      CommentPanel,
    },
    emits: ['success', 'register', 'formConfig'],
    setup(props, { emit }) {
      console.log('进入表单弹框》》》》modal');

      const commentPanelRef = ref();
      const tabNav = ref<any>([]);
      const tabIndex = ref<string>('-1');
      function reloadComment() {
        if (commentPanelRef.value) commentPanelRef.value.reload();
      }

      const {
        title,
        modalWidth,
        registerModal,
        cgButtonList,
        handleCgButtonClick,
        disableSubmit,
        handleSubmit,
        submitLoading,
        handleCancel,
        handleFormConfig,
        onlineFormCompRef,
        formTemplate,
        isTreeForm,
        pidFieldName,
        renderSuccess,
        formRendered,
        showSub,
        tableName,
        tableId,
        formDataId,
        enableComment,
        themeTemplate,
      } = useAutoModal(false, { emit }, reloadComment);

      // 监听id变化 表单重新渲染
      watch(() => props.id, renderFormItems, { immediate: true });
      async function renderFormItems() {
        formRendered.value = false;
        if (!props.id) {
          return;
        }
        console.log('重新渲染表单》》》》modal');
        await handleFormConfig(props.id, {}, (result) => {
          const nav: any = [];
          const sub: any = [];
          const { head, schema } = result;
          const { properties } = schema;
          nav.push({ tableName: head.tableName, tableTxt: head.tableTxt });
          Object.entries(properties).forEach(([key, value]: any) => {
            if (value.view == 'tab') {
              //update-begin---author:chenrui ---date:2025/8/27  for：[issues/8760]online表单中，主题模板为“TAB主题”时，附表TAB页面的标题和内容不一致 #8760------------
              sub.push({ tableName: key, tableTxt: value.describe,order: value.order });
              //update-end---author:chenrui ---date:2025/8/27  for：[issues/8760]online表单中，主题模板为“TAB主题”时，附表TAB页面的标题和内容不一致 #8760------------
            }
          });
          sub.sort((a, b) => a.order - b.order);
          tabNav.value = [...nav, ...sub];
        });
      }
      const tabValue = (index) => {
        return String(index - 1);
      };
      const restTabIndex = () => {
        // 关闭的时候把索引重置第一位（防止下一条数据打开还是同样的tab）
        setTimeout(() => {
          tabIndex.value = '-1';
        }, 500);
      };

      const that = {
        title,
        onlineFormCompRef,
        renderSuccess,
        registerModal,
        handleSubmit,
        handleCancel,
        modalWidth,
        formTemplate,
        disableSubmit,
        cgButtonList,
        handleCgButtonClick,
        isTreeForm,
        pidFieldName,
        submitLoading,
        showSub,
        tableName,
        tableId,
        formDataId,
        enableComment,
        commentPanelRef,
        themeTemplate,
        tabNav,
        tabValue,
        tabIndex,
        TAB,
        restTabIndex,
      };

      return that;
    },
  });
</script>

<style lang="less" scoped>
  .titleArea {
    display: flex;
    align-content: center;
    padding-right: 70px;
    .title {
      margin-right: 16px;
      line-height: 32px;
    }
    .right {
      overflow-x: auto;
      overflow-y: hidden;
      flex: 1;
      white-space: nowrap;
    }
  }
</style>
