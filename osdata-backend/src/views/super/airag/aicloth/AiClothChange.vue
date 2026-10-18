<template>
  <div class="ai-cloth-change-page">
    <!-- 顶部标题区 -->
    <div class="page-header">
      <h1 class="page-title">AI 换衣</h1>
      <div class="header-desc">将模特图片和服装图片上传，AI自动生成换衣效果</div>
    </div>

    <div class="cloth-change-wrapper">
      <!-- 左侧配置面板 -->
      <div class="config-panel">
        <!-- 顶部 Tab：生成图片 / 生成视频 -->
        <div class="config-tabs">
          <a-tabs v-model:activeKey="genType" :tabBarStyle="{ margin: 0 }">
            <a-tab-pane key="image" tab="生成图片" />
            <a-tab-pane key="video" tab="生成视频" />
          </a-tabs>
        </div>

        <div class="form-scroll">
          <div class="examples-row" role="toolbar" aria-label="示例">
            <a-button class="example-btn" type="default" @click.prevent="useExample1">示例：换整体服装</a-button>
            <a-button class="example-btn" type="default" @click.prevent="useExample2">示例：上衣/裤子</a-button>
          </div>
          <!-- 模型选择 -->
          <BasicForm @register="registerForm">
            <!-- 上传区域（单一上传组件支持多张图片） -->
            <template #clothUpload>
              <div class="section-block">
                <JImageUpload v-model:value="clothUploads" :fileMax="2" text="上传服装" />
              </div>
            </template>
          </BasicForm>

          <!-- 视频提示 -->
          <div v-if="genType === 'video'" class="ai-notice">
            <a-alert message="视频生成可能需要较长时间，请耐心等待~" type="info" show-icon :closable="false" />
          </div>
        </div>

        <!-- 底部生成按钮 -->
        <div class="action-bar">
          <a-button type="primary" size="large" block class="gen-btn" :loading="loading" @click="handleGenerate"> 生成 </a-button>
        </div>
      </div>

      <!-- 右侧结果展示 -->
      <div class="preview-panel">
        <div class="preview-content">
          <div v-if="!generatedResult && !loading" class="empty-state">
            <Icon icon="ant-design:picture-outlined" size="64" color="#ccc" />
            <p>在左侧配置后点击「生成」</p>
          </div>

          <div v-if="loading" class="loading-state">
            <a-spin size="large" :tip="`正在${genType === 'image' ? '生成图片' : '生成视频'}，请稍候...`" />
          </div>

          <!-- 图片结果 -->
          <div v-if="genType === 'image' && generatedResult && !loading" class="result-image-wrapper group">
            <img :src="generatedResult" class="result-image" alt="换衣结果" />
            <div class="hover-actions">
              <a-button type="primary" ghost @click="previewVisible = true"> <Icon icon="ant-design:eye-outlined" /> 预览 </a-button>
              <a-button type="primary" ghost @click="handleDownload"> <Icon icon="ant-design:download-outlined" /> 下载 </a-button>
            </div>
          </div>

          <!-- 视频结果 -->
          <div v-if="genType === 'video' && generatedResult && !loading" class="result-video-wrapper group">
            <video ref="videoRef" :src="generatedResult" controls class="result-video" />
            <div class="hover-actions">
              <a-button type="primary" ghost @click="handleDownload"> <Icon icon="ant-design:download-outlined" /> 下载 </a-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <ImageViewer v-if="previewVisible" :imageUrl="generatedResult" @hide="previewVisible = false" />
</template>

<script lang="ts" setup>
  import { ref, watch, onMounted, onUnmounted } from 'vue';
  import { BasicForm, useForm, JImageUpload } from '@/components/Form';
  import { clothImageFormSchema, clothVideoFormSchema } from './AiClothChange.data';
  import ImageViewer from '../aiapp/chat/components/ImageViewer.vue';
  import { useMessage } from '@/hooks/web/useMessage';
  import { Icon } from '@/components/Icon';
  import { defHttp } from '@/utils/http/axios';
  import { useGlobSetting } from '@/hooks/setting';

  const TASK_ID_KEY = 'ai_cloth_task_id';

  const { createMessage } = useMessage();
  const { domainUrl } = useGlobSetting();

  //  状态
  // 生成类型
  const genType = ref<'image' | 'video'>('image');

  // 上传的服装图片（JImageUpload 返回逗号拼接的路径字符串）
  const clothUploads = ref<string>('');
  const loading = ref(false);
  const generatedResult = ref('');
  const previewVisible = ref(false);
  const videoRef = ref<HTMLVideoElement | null>(null);
  let pollTimer: ReturnType<typeof setTimeout> | null = null;

  // 表单
  const [registerForm, { validate, resetSchema, getFieldsValue, setFieldsValue }] = useForm({
    schemas: clothImageFormSchema,
    showActionButtonGroup: false,
    wrapperCol: { span: 24 },
    labelCol: { span: 24 },
  });

  // 不再区分单件/多件，提示词可由用户自定义

  watch(genType, (val) => {
    generatedResult.value = '';
    resetSchema(val === 'image' ? clothImageFormSchema : clothVideoFormSchema);
  });

  /**
   * 构建提示词
   * @param values
   */
  function buildPrompt(values: any, clothCount: number): string {
    const userPrompt: string = (values.userPrompt || '').toString().trim();
    const modelImageNumber = clothCount + 1;
    const clothImageNumbers = Array.from({ length: clothCount }, (_, index) => `[图${index + 1}]`).join('、');
    const fixedPrompt =
      `图片角色：${clothImageNumbers}是必须使用的服装素材，[图${modelImageNumber}]是模特目标图。` +
      `以[图${modelImageNumber}]为唯一画布，将${clothImageNumbers}中的服装准确穿到模特身上。` +
      '必须保留服装素材的款式、颜色、图案、领口、袖型和装饰细节，不得自行设计或替换成其他服装；' +
      '仅修改模特的服装区域，保持人物身份、面部、发型、姿态、身材、背景和构图不变，服装贴合自然，光影一致，写实高清。';
    return userPrompt ? `${fixedPrompt}\n补充要求：${userPrompt}` : fixedPrompt;
  }
  
  //update-begin---wangshuai---date:20260415  for：[QQYUN-14944]AI 改成异步的，支持切换菜单------------
  /** 轮询查询任务结果 */
  function startPolling(taskId: string) {
    const poll = () => {
      defHttp
        .get({ url: `/airag/chat/getAiPosterResult/${taskId}` }, { isTransformResponse: false })
        .then((res) => {
          if (res.success) {
            if (res.result === 'pending' || res.result === null) {
              pollTimer = setTimeout(poll, 3000);
            } else {
              const reg = /#\s*{\s*domainURL\s*}/g;
              generatedResult.value = (res.result as string).replace(reg, domainUrl + '/sys/common/static');
              loading.value = false;
              localStorage.removeItem(TASK_ID_KEY);
              createMessage.success(genType.value === 'image' ? '图片生成成功！' : '视频生成成功！');
            }
          } else {
            loading.value = false;
            localStorage.removeItem(TASK_ID_KEY);
            createMessage.warning(res.message || '生成失败！');
          }
        })
        .catch(() => {
          pollTimer = setTimeout(poll, 3000);
        });
    };
    poll();
  }
  //update-end---wangshuai---date:20260415  for：[QQYUN-14944]AI 改成异步的，支持切换菜单------------

  /**
   * 生成
   */
  async function handleGenerate() {
    // 校验服装图（从 clothUploads 解析）
    const validCloth = (clothUploads.value || '').split(',').filter(Boolean);
    if (validCloth.length === 0) {
      createMessage.warning('请上传服装图片');
      return;
    }

    try {
      const values = await validate();
      loading.value = true;
      generatedResult.value = '';

      // 多图融合以最后一张图作为目标画布：服装素材在前，模特图在最后
      const modelImage = (values.modelImage || '').toString().split(',')[0];
      const imgUrls: string[] = [...validCloth, modelImage];

      if (genType.value === 'image') {
        values.imageSize = '720*1280';
      } else {
        createMessage.info('敬请期待');
        loading.value = false;
        return;
      }
      const prompt = buildPrompt(values, validCloth.length);

      const params: Record<string, any> = {
        drawModelId: values.drawModelId,
        //update-begin---wangshuai---date:20260415  for：[QQYUN-14944]AI 改成异步的，支持切换菜单------------
        content: prompt,
        imageUrl: imgUrls.join(','),
        //update-end---author:wangshuai ---date:20260415  for：[QQYUN-14944]AI 改成异步的，支持切换菜单------------
        type: genType.value === 'image' ? 'cloth_image' : 'cloth_video',
        imageSize: values.imageSize,
      };

      //update-begin---wangshuai---date:20260415  for：[QQYUN-14944]AI 改成异步的，支持切换菜单------------
      const res = await defHttp.post(
        { url: '/airag/chat/genAiPosterAsync', params },
        { isTransformResponse: false },
      );
      if (res.success && res.result) {
        const taskId = res.result as string;
        localStorage.setItem(TASK_ID_KEY, taskId);
        startPolling(taskId);
      } else {
        loading.value = false;
        createMessage.warning('提交任务失败！');
      }
      //update-end---wangshuai---date:20260415  for：[QQYUN-14944]AI 改成异步的，支持切换菜单------------
    } catch {
      loading.value = false;
    }
  }

  //update-begin---wangshuai---date:20260415  for：[QQYUN-14944]AI 改成异步的，支持切换菜单------------
  onMounted(() => {
    const savedTaskId = localStorage.getItem(TASK_ID_KEY);
    if (savedTaskId) {
      loading.value = true;
      generatedResult.value = '';
      startPolling(savedTaskId);
    }
  });

  onUnmounted(() => {
    if (pollTimer) {
      clearTimeout(pollTimer);
      pollTimer = null;
    }
  });
  //update-end---wangshuai---date:20260415  for：[QQYUN-14944]AI 改成异步的，支持切换菜单------------

  /**
   * 下载
   */
  function handleDownload() {
    if (!generatedResult.value) {
      return;
    }
    const a = document.createElement('a');
    a.href = generatedResult.value;
    a.download = `ai-cloth-change-${Date.now()}.${genType.value === 'image' ? 'jpg' : 'mp4'}`;
    a.target = '_blank';
    a.click();
  }

  // 示例提示词操作
  function useExample1() {
    const example =
      '完整替换模特原有服装，优先还原服装素材的版型、颜色、纹理和装饰细节。';
    const modelImage = 'https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/model_1772695749704.jpg';
    clothUploads.value = 'https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/dress_1772700962866.jpg';
    setFieldsValue({ userPrompt: example, modelImage: modelImage });
  }

  function useExample2() {
    const example =
      '图1为上衣素材，图2为下装素材；分别准确替换模特的上衣和下装，组成完整穿搭，不得混淆两件服装。';
    const modelImage = 'https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/model_1772695749704.jpg';
    clothUploads.value =
      'https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/jacket_1772701290346.jpg,https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/pants_1772701320192.jpg';
    setFieldsValue({ userPrompt: example, modelImage: modelImage });
  }
</script>

<style lang="less" scoped>
  @import 'AiClothChange.less';
</style>
