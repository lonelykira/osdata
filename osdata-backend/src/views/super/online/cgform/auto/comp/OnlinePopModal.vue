<template>
  <BasicModal :width="popModalFixedWidth" :wrapperFooterOffset="26" v-bind="$attrs" :footer="modalFooter" cancelText="关闭" @register="registerModal" wrapClassName="jeecg-online-pop-modal" @ok="handleSubmit">
        <template #title>
            {{title}}
            <j-modal-tip v-if="showTopTip" :visible="topTipVisible" @save="handleSaveData" @cancel="handleRecover"></j-modal-tip>
        </template>
        
        <online-pop-form
            ref="onlineFormCompRef"
            :id="id"
            :disabled="disableSubmit"
            :form-template="formTemplate"
            :isTree="isTreeForm"
            :pidField="pidFieldName"
            :request="request"
            :isVxeTableData="isVxeTableData"
            @rendered="renderSuccess"
            @success="handleSuccess"
            @data-change="handleDataChange"
            modal-class="jeecg-online-pop-modal"
        >
        </online-pop-form>
    </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, watch, watchEffect, ref, computed, h } from 'vue';
  import { BasicModal } from '/@/components/Modal';
  import OnlinePopForm from './OnlinePopForm.vue';
  import { useAutoModal } from '../../hooks/auto/useAutoModal';
  import JModalTip from '../../extend/linkTable/JModalTip.vue'
  import { Button } from 'ant-design-vue';

  export default defineComponent({
    name: 'OnlinePopModal',
    props: {
      /**可以是表名 可以是ID*/
      id: {
        type: String,
        default: '',
      },
      /*展示字段名*/
      showFields:{
        type: Array,
        default: ()=>[],
      },
      /*隐藏字段名*/
      hideFields:{
        type: Array,
        default: ()=>[],
      },
      topTip:{
        type: Boolean,
        default: false,
      },
      request:{
        type: Boolean,
        default: true,
      },
      saveClose:{
        type: Boolean,
        default: false
      },
      // 是否是vxeTable上方按钮点击打开的表单数据
      isVxeTableData:{
        type: Boolean,
        default: false
      },
      formTableType:{
        type: String,
        default: '',
      },
      // -update-begin--author:liaozhiyang---date:20240613---for：【TV360X-1000】流程一对多走流程的接口
      // 有taskId即是流程
      taskId: {
        type: String,
      },
      tableName: {
        type: String,
      },
      // -update-end--author:liaozhiyang---date:20240613---for：【TV360X-1000】流程一对多走流程的接口
    },
    components: {
      BasicModal,
      OnlinePopForm,
      JModalTip,
      Button
    },
    emits: ['success', 'register', 'formConfig'],
    setup(props, { emit }) {
      console.log('进入表单弹框》》》》modal');
      
      const {
        title,
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
        handleSuccess,
        topTipVisible,
        successThenClose,
        isUpdate,
        popModalFixedWidth,
        getFormStatus
      } = useAutoModal(false, { emit });

      // 监听id变化 表单重新渲染
      watch(() => props.id, renderFormItems, { immediate: true });
      async function renderFormItems() {
        formRendered.value = false;
        if (!props.id) {
          return;
        }
        console.log('重新渲染表单》》》》modal');
        
        //update-begin-author:taoyan date:2023-4-10 for: issues/4655 online在线表单（一对多），对子表记录进行新增或编辑时，无法获取到表单信息 #4655
        let params = {}
        if(props.formTableType){
          params['tabletype'] = props.formTableType
        }
        // -update-begin--author:liaozhiyang---date:20240613---for：【TV360X-1000】流程一对多走流程的接口
        if (props.taskId) {
          await handleFormConfig(props.id, params,null,props.taskId, props.tableName );
        } else {
          await handleFormConfig(props.id, params);
        }
        // -update-end--author:liaozhiyang---date:20240613---for：【TV360X-1000】流程一对多走流程的接口
        //update-end-author:taoyan date:2023-4-10 for: issues/4655 online在线表单（一对多），对子表记录进行新增或编辑时，无法获取到表单信息 #4655
        
      }
      
      // 上方保存按钮触发
      function handleSaveData() {
        //如果props的saveClose没有设置为true则弹窗不会关闭
        if(props.saveClose === false){
          successThenClose.value = false;
        }
        handleSubmit();
      }
      
      // 上方取消按钮触发
      function handleRecover(){
        topTipVisible.value = false;
        onlineFormCompRef.value.recoverFormData()
      }
      
      // 表单数据改变触发modal事件
      function handleDataChange(){
        topTipVisible.value = true;
      }
      
      // 只有编辑页面才需要显示顶部保存按钮
      const showTopTip = computed(()=>{
        // update-begin--author:liaozhiyang---date:20250318---for：【issues/7930】表格列表中支持关联记录配置是否只读
        if (disableSubmit.value) {
          return false;
        }
        // update-end--author:liaozhiyang---date:20250318---for：【issues/7930】表格列表中支持关联记录配置是否只读
        if(!isUpdate.value){
          return false;
        }
        return props.topTip
      });

      // 编辑页面没有底部按钮
      const modalFooter = computed(()=>{
        if(isUpdate.value==true){
          return null;
        }else{
          let flag = submitLoading.value;
          const defaultFooter:any[] = [
            h(Button, { type: 'primary', loading: flag, onClick: handleSubmit },()=>'确定'),
            h(Button, { onClick:handleCancel },()=>'关闭')
          ];
          return defaultFooter
        }
      });
      
      const that = {
        title,
        topTipVisible,
        handleSaveData,
        handleRecover,
        onlineFormCompRef,
        renderSuccess,
        registerModal,
        handleSubmit,
        handleSuccess,
        handleCancel,
        formTemplate,
        disableSubmit,
        cgButtonList,
        handleCgButtonClick,
        isTreeForm,
        pidFieldName,
        submitLoading,
        handleDataChange,
        isUpdate,
        showTopTip,
        modalFooter,
        popModalFixedWidth,
        getFormStatus
      };

      return that;
    },
  });
</script>

<style lang="less">
  .jeecg-online-pop-modal {
    > .ant-modal {
      top: 30px !important;
    }
  }
</style>
