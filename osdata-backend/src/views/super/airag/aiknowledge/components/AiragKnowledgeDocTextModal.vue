<!--手动录入text-->
<template>
  <div class="p-2">
    <BasicModal destroyOnClose @register="registerModal" width="600px" :title="title" @ok="handleOk" @cancel="handleCancel">
      <div v-show="currentStep === 0">
        <BasicForm @register="registerForm"></BasicForm>
        <div v-if="showWebContent" class="web-content-preview">
          <div class="web-content-label">解析内容（只读）</div>
          <div class="web-content-body">
            <pre>{{ webContentText }}</pre>
          </div>
        </div>
      </div>
      <div v-show="currentStep === 1">
        <!-- 知识库有默认分段策略时，显示选择来源 -->
        <div v-if="knowledgeDefaultSegment" style="margin-bottom: 16px">
          <div style="margin-bottom: 8px; font-weight: 500; color: rgba(0,0,0,0.85)">分段策略来源</div>
          <a-radio-group v-model:value="useKnowledgeDefault" button-style="solid">
            <a-radio-button value="default">使用知识库默认</a-radio-button>
            <a-radio-button value="custom">自定义</a-radio-button>
          </a-radio-group>
          <div style="margin-top: 6px; font-size: 12px; color: rgba(0,0,0,0.45)">
            <span v-if="useKnowledgeDefault === 'default'">直接保存，文档将使用知识库配置的分段策略</span>
            <span v-else>忽略知识库默认策略，为该文档单独配置分段参数</span>
          </div>
          <!-- 只读展示知识库默认策略 -->
          <div v-if="useKnowledgeDefault === 'default'" class="default-segment-info">
            <a-descriptions :column="2" size="small" bordered style="margin-top: 12px">
              <a-descriptions-item label="分段模式">{{ knowledgeDefaultSegment.segmentStrategy === 'custom' ? '自定义' : '自动分段与清洗' }}</a-descriptions-item>
              <a-descriptions-item label="最大长度">{{ knowledgeDefaultSegment.maxSegment }}</a-descriptions-item>
              <a-descriptions-item label="重叠度%">{{ knowledgeDefaultSegment.overlap }}</a-descriptions-item>
              <a-descriptions-item v-if="knowledgeDefaultSegment.segmentStrategy === 'custom'" label="分段标识符">{{ knowledgeDefaultSegment.separator === 'custom' ? knowledgeDefaultSegment.customSeparator : knowledgeDefaultSegment.separator }}</a-descriptions-item>
            </a-descriptions>
          </div>
        </div>
        <BasicForm v-show="!knowledgeDefaultSegment || useKnowledgeDefault === 'custom'" @register="registerSegmentForm"></BasicForm>
      </div>
    </BasicModal>
  </div>
</template>

<script lang="ts">
  import { ref, unref, computed } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModalInner } from '@/components/Modal';

  import BasicForm from '@/components/Form/src/BasicForm.vue';
  import { useForm } from '@/components/Form';
  import { docSegmentSchema, docTextSchema } from '../AiKnowledgeBase.data';
  import { knowledgeSaveDoc } from '../AiKnowledgeBase.api';

  export default {
    name: 'AiragKnowledgeDocModal',
    components: {
      BasicForm,
      BasicModal,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const title = ref<string>('创建知识库');
      const currentStep = ref(0);
      const step1Values = ref({});
      //自定义分词的数据
      const segmentMetadataRef = ref<any>({});
      // 知识库默认分段策略（有值表示知识库配置了默认分段）
      const knowledgeDefaultSegment = ref<any>(null);
      // 分段策略来源：'default' 使用知识库默认，'custom' 自定义
      const useKnowledgeDefault = ref<'default' | 'custom'>('default');
      // 知识库类型：'knowledge' | 'memory'
      const knowledgeType = ref<string>('knowledge');

      //保存或修改
      const isUpdate = ref<boolean>(false);
      //知识库id
      const knowledgeId = ref<string>();
      //网页解析内容（只读展示）
      const webContentText = ref<string>('');
      const docType = ref<string>('');
      const showWebContent = computed(() => docType.value === 'web' && isUpdate.value && webContentText.value);
      //表单配置
      const [registerForm, { resetFields, setFieldsValue, validate, clearValidate, updateSchema }] = useForm({
        schemas: docTextSchema,
        showActionButtonGroup: false,
        layout: 'vertical',
        wrapperCol: { span: 24 },
      });

      const [registerSegmentForm, { resetFields: resetSegmentFields, validate: validateSegment, setFieldsValue: setSegmentFieldsValue }] = useForm({
        schemas: docSegmentSchema,
        showActionButtonGroup: false,
        layout: 'vertical',
        wrapperCol: { span: 24 },
        labelCol: { span: 24 },
      });

      //注册modal
      const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
        //重置表单
        await resetFields();
        await resetSegmentFields();
        currentStep.value = 0;
        webContentText.value = '';
        docType.value = '';
        knowledgeType.value = data?.knowledgeType || 'knowledge';
        setModalProps({ confirmLoading: false, okText: knowledgeType.value === 'memory' ? '保存' : '下一步' });
        isUpdate.value = !!data?.isUpdate;
        title.value = isUpdate.value ? '编辑文档' : '创建文档';

        //update-begin---wangshuai---date:20260414  for：【QQYUN-14932】创建知识库时，可以创建一个分段策略，知识库里面的文档默认使用知识库的分段策略------------
        // 解析知识库默认分段策略
        knowledgeDefaultSegment.value = null;
        useKnowledgeDefault.value = 'default';
        if (data?.knowledgeMetadata) {
          try {
            const kmeta = JSON.parse(data.knowledgeMetadata);
            if (kmeta.enableSegment) {
              knowledgeDefaultSegment.value = kmeta;
            }
          } catch (_e) {}
        }
        //update-end---wangshuai---date:20260414  for：【QQYUN-14932】创建知识库时，可以创建一个分段策略，知识库里面的文档默认使用知识库的分段策略------------

        if (unref(isUpdate)) {
          docType.value = data.record.type || '';
          if(data.record.type === 'file' && data.record.metadata){
            data.record.filePath = JSON.parse(data.record.metadata).filePath;
          }
          if(data.record.type === 'web' && data.record.metadata){
            data.record.website = JSON.parse(data.record.metadata).website;
          }
          if(data.record.type === 'web' && data.record.content){
            webContentText.value = data.record.content;
          }
          //表单赋值
          await setFieldsValue({
            ...data.record,
          });

          // 解析metadata并准备给第二步表单
          if (data.record.metadata) {
            const meta = JSON.parse(data.record.metadata);
            //update-begin---wangshuai---date:20260414  for：【QQYUN-14932】创建知识库时，可以创建一个分段策略，知识库里面的文档默认使用知识库的分段策略------------
            // 如果文档保存时使用了知识库默认策略，回显时恢复选项
            // 兼容老数据：若知识库已不存在默认分段策略，则降级为自定义（展示默认值）
            if (meta.useKnowledgeDefault && knowledgeDefaultSegment.value) {
              useKnowledgeDefault.value = 'default';
            } else {
              useKnowledgeDefault.value = 'custom';
              // update-begin--author:wangshuai--date:2026-04-09--for:【issue/9418】AI知识库上传文件太大向量化失败
              let strategy = meta.segmentStrategy || (meta.separator ? 'custom' : 'auto');
              // update-end--author:wangshuai--date:2026-04-09--for:【issue/9418】AI知识库上传文件太大向量化失败
              segmentMetadataRef.value = {
                segmentStrategy: strategy,
                maxSegment: meta.maxSegment,
                overlap: meta.overlap,
                textRules: meta.textRules,
                separator: meta.separator,
                preprocessingRules: meta.preprocessingRules,
                customSeparator: strategy === 'custom' ? meta.customSeparator : '',
              };
            }
            //update-end---wangshuai---date:20260414  for：【QQYUN-14932】创建知识库时，可以创建一个分段策略，知识库里面的文档默认使用知识库的分段策略------------
          } else {
            useKnowledgeDefault.value = knowledgeDefaultSegment.value ? 'default' : 'custom';
            segmentMetadataRef.value = { segmentStrategy: 'auto', separator: '\\n', maxSegment: 800, overlap: 10 };
          }
        } else {
          knowledgeId.value = data.knowledgeId;
          docType.value = data.type || '';
          // 新建时：有知识库默认策略则默认选中"使用知识库默认"
          useKnowledgeDefault.value = knowledgeDefaultSegment.value ? 'default' : 'custom';
          segmentMetadataRef.value = { segmentStrategy: 'auto', separator: '\\n', maxSegment: 800, overlap: 10 };
          await setFieldsValue({ type: data.type });
        }
        setModalProps({ bodyStyle: { padding: '10px' } });
      });

      /**
       * 保存
       */
      async function handleOk() {
        try {
          if (currentStep.value === 0 && knowledgeType.value !== 'memory') {
            step1Values.value = await validate();
            currentStep.value = 1;
            setModalProps({ okText: '保存', minHeight: 400 });
            if (segmentMetadataRef.value) {
              await setSegmentFieldsValue({ ...segmentMetadataRef.value });
            }
            return;
          }

          if (currentStep.value === 0 && knowledgeType.value === 'memory') {
            step1Values.value = await validate();
          }

          setModalProps({ confirmLoading: true });
          let values: any = { ...step1Values.value };

          let metadata: any = {};

          //update-begin---wangshuai---date:20260414  for：【QQYUN-14932】创建知识库时，可以创建一个分段策略，知识库里面的文档默认使用知识库的分段策略------------
          if (knowledgeType.value === 'memory') {
            // 记忆库不需要分段策略，metadata 留空
          } else if (useKnowledgeDefault.value === 'default' && knowledgeDefaultSegment.value) {
            // 使用知识库默认分段策略，标记即可，后端读取知识库 metadata
            metadata = { useKnowledgeDefault: true };
          } else {
            // update-begin--author:wangshuai--date:2026-04-09--for:【issue/9418】AI知识库上传文件太大向量化失败
            const segmentFormValues = await validateSegment();
            metadata = {
              segmentStrategy: segmentFormValues.segmentStrategy,
              maxSegment: segmentFormValues.maxSegment,
              overlap: segmentFormValues.overlap,
            };
            if (segmentFormValues.segmentStrategy === 'custom') {
              metadata = {
                ...metadata,
                separator: segmentFormValues.separator,
                customSeparator: segmentFormValues.customSeparator,
                textRules: segmentFormValues.textRules,
              };
            }
            // update-end--author:wangshuai--date:2026-04-09--for:【issue/9418】AI知识库上传文件太大向量化失败
          }
          //update-end---wangshuai---date:20260414  for：【QQYUN-14932】创建知识库时，可以创建一个分段策略，知识库里面的文档默认使用知识库的分段策略------------
          if (!unref(isUpdate)) {
            values.knowledgeId = knowledgeId.value;
          }
          if(values.filePath){
            metadata.filePath = values.filePath;
            delete values.filePath;
          }
          if(values.website){
            metadata.website = values.website;
            delete values.website;
          }
          values.metadata = JSON.stringify(metadata);
          await knowledgeSaveDoc(values);
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
        registerSegmentForm,
        currentStep,
        title,
        handleOk,
        handleCancel,
        showWebContent,
        webContentText,
        knowledgeDefaultSegment,
        useKnowledgeDefault,
        knowledgeType,
      };
    },
  };
</script>

<style scoped lang="less">
  .pointer {
    cursor: pointer;
  }
  .web-content-preview {
    margin-top: 8px;
    .web-content-label {
      font-weight: 500;
      margin-bottom: 6px;
      color: rgba(0, 0, 0, 0.85);
    }
    .web-content-body {
      max-height: 300px;
      overflow-y: auto;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
      padding: 12px;
      background: #f5f5f5;
      pre {
        margin: 0;
        white-space: pre-wrap;
        word-wrap: break-word;
        font-size: 13px;
        line-height: 1.6;
        color: #333;
      }
    }
  }
</style>
