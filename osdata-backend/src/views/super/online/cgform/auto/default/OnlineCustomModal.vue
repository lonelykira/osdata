<template>
  <BasicModal
    v-bind="modalProps"
    :style="modalStyle"
    @register="registerModal"
    wrapClassName="jeecg-online-modal2"
    @ok="handleSubmit"
  >
    <template #footer>
      <a-button key="submit" type="primary" @click="handleSubmit"> {{modalProps.okText ?? '确定'}}</a-button>
      <a-button key="back" @click="handleCancel">{{ modalProps.cancelText ?? '关闭' }}</a-button>
    </template>

    <a-spin :spinning="confirmLoading">
      <!-- online表单 -->
      <online-form
        v-if="isOnlineForm"
        ref="onlineFormCompRef"
        :id="id"
        :form-template="formTemplate"
        @rendered="renderSuccess"
        @success="handleSuccess"
        modalClass="jeecg-online-modal2"
      >
      </online-form>

      <!-- 自定义表单 -->
      <component
        v-else
        ref="customFormRef"
        :url="customFormComponent.url"
        :is="customFormComponent.is"
        :row="customFormComponent.row"
        @close="handleSuccess"
      >
      </component>
    </a-spin>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, reactive, ref, watch, nextTick, defineAsyncComponent, markRaw } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { getRefPromise } from '../../hooks/auto/useAutoForm';
  import { defHttp } from '/@/utils/http/axios';
  import OnlineForm from '../comp/OnlineForm.vue';
  import { importViewsFile } from '/@/utils';
  import { omit } from 'lodash-es';

  /**
   * open函数的参数
   */
  interface ModalConfig {
    row: any;
    hide?: string[];
    show?: string[];
    requestUrl?: string;
    tableType?: string;
    foreignKeys?: string;
    formComponent?: string;
    code?: string;
  }

  /**
   * 默认配置
   */
  const DEF_CONFIG: ModalConfig = {
    code: '',
    row: {},
    hide: [],
    show: [],
    requestUrl: '',
    tableType: '',
    foreignKeys: '',
    formComponent: '',
  };
  /**
   * 自定义弹窗
   */
  export default defineComponent({
    name: 'OnlineCustomModal',
    components: {
      OnlineForm,
      BasicModal,
    },
    setup(_props, { emit, attrs }) {
      const onlineFormCompRef = ref();
      const modalProps = ref({});
      // online表单的配置id
      const id = ref('');

      let onlineHideFields = [];
      let onlineShowFields = [];
      let onlineFormEditUrl = '';
      // 当前编辑的数据
      let currentRowData = {};
      const url = {
        loadFormItems: '/online/cgform/api/getFormItem/',
        optPre: '/online/cgform/api/form/',
      };
      const modalStyle = { position: 'relative' };

      const confirmLoading = ref(false);
      // 表单是否渲染完成
      const formRendered = ref(false);
      // 渲染完成改变状态
      function renderSuccess() {
        formRendered.value = true;
      }

      //是否是online表单
      const isOnlineForm = ref(true);

      // 弹框显示 触发onlineFormCompRef---show
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (params) => {
        setModalProps({ confirmLoading: false });
        resetParams(params);
        await nextTick(async () => {
          if (!params.formComponent) {
            //没有申明组件 走online表单
            await showOnlineForm();
          } else {
            showCustomForm(params.formComponent);
          }
        });
      });

      /**
       * 打开弹窗
       * @param params
       */
      function resetParams(params) {
        // code row formComponent hide show
        let options = Object.assign({}, DEF_CONFIG, params);
        id.value = options.code;
        // update-begin--author:liaozhiyang---date:20250818---for：【issues/8672】js增强弹窗支持basicModal的props
        modalProps.value = {
          title: '自定义弹框',
          width: 600,
          ...attrs,
          ...omit(params, ['row', 'formComponent', 'hide', 'show', 'requestUrl']),
        };
        // update-end--author:liaozhiyang---date:20250818---for：【issues/8672】js增强弹窗支持basicModal的props
        onlineHideFields = options.hide || [];
        onlineShowFields = options.show || [];
        onlineFormEditUrl = getOnlineFormEditUrl(options.requestUrl);
        currentRowData = options.row;
      }

      /**
       * 获取表单数据编辑地址-对两个表单都有效
       */
      function getOnlineFormEditUrl(requestUrl) {
        if (requestUrl) {
          return requestUrl;
        } else {
          return url.optPre + id.value;
        }
      }

      const submitLoading = ref(false);
      function handleSubmit() {
        submitLoading.value = true;
        if (isOnlineForm.value === true) {
          onlineFormCompRef.value.handleSubmit();
        } else {
          customFormRef.value.handleSubmit();
        }
        setTimeout(() => {
          submitLoading.value = true;
        }, 3500);
      }

      function handleCancel() {
        closeModal();
      }

      function handleSuccess(formData) {
        emit('success', formData);
        closeModal();
      }

      /*-------------------------------------online表单-----------------------------------------------*/

      /**
       * 显示online表单
       */
      async function showOnlineForm() {
        isOnlineForm.value = true;
        await getRefPromise(formRendered);
        //处理字段显示隐藏
        onlineFormCompRef.value.handleCustomFormSh(onlineShowFields, onlineHideFields);
        //回显表单数据
        onlineFormCompRef.value.handleCustomFormEdit(currentRowData, onlineFormEditUrl);
      }

      // 模板风格-默认1列
      const formTemplate = ref(1);
      // 监听id变化 表单重新渲染
      watch(id, renderFormItems, { immediate: true });
      async function renderFormItems() {
        formRendered.value = false;
        if (!id.value) {
          return;
        }
        console.log('重新渲染表单》》》》modal');
        let result: any = await loadFormItems();
        // modal页面只处理按钮、JS增强、弹框宽度
        let dataFormTemplate = result.head.formTemplate;
        formTemplate.value = dataFormTemplate ? Number(dataFormTemplate) : 1;
        nextTick(async () => {
          let myForm = (await getRefPromise(onlineFormCompRef)) as any;
          myForm.createRootProperties(result);
        });
      }

      function loadFormItems() {
        let url = `/online/cgform/api/getFormItem/${id.value}`;
        return new Promise((resolve, reject) => {
          defHttp
            .get({ url }, { isTransformResponse: false })
            .then((res) => {
              console.log('表单结果》》modal:', res);
              if (res.success) {
                resolve(res.result);
              } else {
                reject(res.message);
              }
            })
            .catch(() => {
              reject();
            });
        });
      }
      /*-------------------------------------online表单-----------------------------------------------*/

      /*-------------------------------------自定义表单-----------------------------------------------*/
      const customFormRef = ref();

      const customFormComponent = reactive({
        url: '',
        is: '',
        row: {},
      });

      /**
       * 显示自定义表单
       */
      function showCustomForm(formComponent) {
        isOnlineForm.value = false;
        customFormComponent.url = onlineFormEditUrl;
        customFormComponent.row = currentRowData;
        customFormComponent.is = markRaw(defineAsyncComponent(() => importViewsFile(formComponent)));
      }
      /*-------------------------------------自定义表单-----------------------------------------------*/

      return {
        //modal
        registerModal,
        modalProps,
        modalStyle,
        handleSubmit,
        handleCancel,

        // online表单
        id,
        onlineFormCompRef,
        formTemplate,
        renderSuccess,

        //自定义表单
        customFormRef,
        customFormComponent,

        //通用
        open,
        isOnlineForm,
        confirmLoading,
        submitLoading,
        handleSuccess,
      };
    },
  });
</script>

<style scoped></style>
