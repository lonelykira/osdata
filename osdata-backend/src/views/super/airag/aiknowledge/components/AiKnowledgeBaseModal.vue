<template>
  <div class="p-2">
    <BasicModal destroyOnClose @register="registerModal" :canFullscreen="false" width="600px" :title="title" @ok="handleOk" @cancel="handleCancel">
      <template #title>
         <span style="display: flex">
          {{title}}
          <a-tooltip title="AI知识库文档">
            <a style="color: unset" href="https://help.jeecg.com/aigc/guide/knowledge" target="_blank">
              <Icon style="position:relative;left:2px;top:1px" icon="ant-design:question-circle-outlined"></Icon>
            </a>
          </a-tooltip>
        </span>
      </template>
      <BasicForm @register="registerForm"></BasicForm>
    </BasicModal>
  </div>
</template>

<script lang="ts">
  import { ref, unref } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModal, useModalInner } from '@/components/Modal';

  import BasicForm from '@/components/Form/src/BasicForm.vue';
  import { useForm } from '@/components/Form';
  import { formSchema } from '../AiKnowledgeBase.data';
  import { saveKnowledge, editKnowledge, queryById } from '../AiKnowledgeBase.api';
  import { useMessage } from '/@/hooks/web/useMessage';

  export default {
    name: 'KnowledgeBaseModal',
    components: {
      BasicForm,
      BasicModal,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const title = ref<string>('创建知识库');

      //保存或修改
      const isUpdate = ref<boolean>(false);

      //表单配置
      const [registerForm, { resetFields, setFieldsValue, validate, clearValidate, updateSchema }] = useForm({
        schemas: formSchema,
        showActionButtonGroup: false,
        layout: 'vertical',
        wrapperCol: { span: 24 },
        labelCol: { span: 24 },
      });

      //注册modal
      const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
        //重置表单
        await resetFields();
        setModalProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;
        title.value = isUpdate.value ? '编辑知识库' : '创建知识库';
        if (unref(isUpdate)) {
          let values = await queryById({ id: data.id });
          //update-begin---wangshuai---date:20260414  for：【QQYUN-14932】创建知识库时，可以创建一个分段策略，知识库里面的文档默认使用知识库的分段策略------------
          let record = { ...values.result };
          // 解析 metadata 中的分段策略
          if (record.metadata) {
            try {
              const meta = JSON.parse(record.metadata);
              const hasSegment = !!(meta.enableSegment || meta.segmentStrategy || meta.maxSegment);
              record.enableSegment = hasSegment;
              if (hasSegment) {
                record.segmentStrategy = meta.segmentStrategy || 'auto';
                record.maxSegment = meta.maxSegment;
                record.overlap = meta.overlap;
                record.separator = meta.separator;
                record.customSeparator = meta.customSeparator;
                record.textRules = meta.textRules;
              }
            } catch (_e) {}
          }
          //表单赋值
          await setFieldsValue(record);
          //update-end---wangshuai---date:20260414  for：【QQYUN-14932】创建知识库时，可以创建一个分段策略，知识库里面的文档默认使用知识库的分段策略------------
        }
        setModalProps({ minHeight: 500, bodyStyle: { padding: '10px' } });
      });

      /**
       * 保存
       */
      async function handleOk() {
        try {
          setModalProps({ confirmLoading: true });
          let values = await validate();
          //update-begin---wangshuai---date:20260414  for：【QQYUN-14932】创建知识库时，可以创建一个分段策略，知识库里面的文档默认使用知识库的分段策略------------
          // 将分段策略字段打包到 metadata
          const { enableSegment, segmentStrategy, separator, customSeparator, maxSegment, overlap, textRules, ...rest } = values;
          let params: any = { ...rest };
          if (enableSegment) {
            const meta: any = {
              enableSegment: true,
              segmentStrategy: segmentStrategy || 'auto',
              maxSegment,
              overlap,
            };
            if (segmentStrategy === 'custom') {
              meta.separator = separator;
              meta.customSeparator = customSeparator;
              meta.textRules = textRules;
            }
            params.metadata = JSON.stringify(meta);
          } else {
            params.metadata = null;
          }

          if (!unref(isUpdate)) {
            await saveKnowledge(params);
          } else {
            await editKnowledge(params);
        //update-end---wangshuai---date:20260414  for：【QQYUN-14932】创建知识库时，可以创建一个分段策略，知识库里面的文档默认使用知识库的分段策略------------
          }
          //关闭弹窗
          closeModal();
          //刷新列表
          emit('success');
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      /**
       * 取消
       */
      function handleCancel() {
        closeModal();
      }

      return {
        registerModal,
        registerForm,
        title,
        handleOk,
        handleCancel,
      };
    },
  };
</script>

<style scoped lang="less">
  .pointer {
    cursor: pointer;
  }
</style>
