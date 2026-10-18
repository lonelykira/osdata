<template>
  <div class="ai-video-page">
    <!-- 头部区域 -->
    <div class="page-header">
      <div class="title">AI视频</div>
      <div class="subtitle">将文本快速转换为生动的视频内容</div>
    </div>

    <div class="content-wrapper">
      <!-- 左侧：视频控制 + 文案 + 常用场景 + 生成按钮 -->
      <div class="control-panel">
        <div class="panel-title">视频配置</div>
        <div class="form-container">
          <BasicForm @register="registerForm" :schemas="leftFormSchemas" />

          <!-- 文案输入框 -->
          <div class="form-item-group">
            <label class="form-label">文案</label>
            <a-textarea v-model:value="formText" :rows="4" :maxlength="500" show-count placeholder="请输入要生成视频的文案内容" />
          </div>

          <!-- 常用场景 -->
          <div class="preset-group">
            <div class="preset-label">常用场景</div>
            <div class="preset-items">
              <div v-for="(item, index) in presetTexts" :key="index" class="preset-item" @click.prevent="handleApplyPreset(item.content)">
                {{ item.title }}
              </div>
            </div>
          </div>
        </div>

        <!-- 开始生成按钮 -->
        <div class="action-btn-group">
          <a-button type="primary" size="large" block :loading="generating" @click="handleGenerate">
            <Icon icon="ant-design:video-camera-outlined" />
            开始生成
          </a-button>
        </div>
      </div>

      <!-- 中间：视频预览 -->
      <div class="preview-panel">
        <div class="panel-title">预览区域</div>
        <div class="preview-content">
          <div v-if="!currentVideoUrl && !generating" class="empty-state">
            <Icon icon="ant-design:video-camera-outlined" size="72" color="#c0c4cc" />
            <p>填写左侧文案并点击「开始生成」</p>
            <p class="tip">支持调整视频长度、风格和效果，生成更加个性化的视频内容</p>
          </div>

          <div v-if="generating" class="loading-state">
            <a-spin size="large" />
            <div class="loading-text">
              <p>正在生成视频，请耐心等待...</p>
              <p class="elapsed-time">已等待 {{ elapsedTimeText }}</p>
              <p class="status-text">{{ statusText }}</p>
            </div>
          </div>

          <div v-if="currentVideoUrl && !generating" class="video-player-wrapper">
            <video ref="videoRef" :src="currentVideoUrl" controls class="video-control" />
          </div>
        </div>
        <!-- 当前播放视频信息 -->
        <div v-if="currentVideoUrl" class="video-info-section">
          <div class="current-video-info">
            <span class="info-label">当前视频：</span>
            <span class="info-text">{{ currentText }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧生成历史 - 列表风格 -->
      <div class="history-panel">
        <div class="panel-title">生成历史</div>
        <div class="history-list-wrapper">
          <div v-if="historyList.length === 0" class="empty-history">
            <p>暂无生成历史</p>
          </div>
          <div v-else class="history-list">
            <div v-for="(item, index) in historyList" :key="item.id" class="history-item">
              <div class="item-header">
                <span class="item-title" :title="item.content">{{ item.content }}</span>
                <span class="item-time">{{ formatTime(item) }}</span>
              </div>
              <div class="item-actions">
                <a-button type="text" size="small" @click="handlePlay(item)">
                  <Icon icon="ant-design:play-circle-outlined" />
                  播放
                </a-button>
                <a-button type="text" size="small" @click="handleUseText(item)">
                  <Icon icon="ant-design:copy-outlined" />
                  复用文案
                </a-button>
                <a-button type="text" size="small" @click="handleDownload(item)">
                  <Icon icon="ant-design:download-outlined" />
                  下载
                </a-button>
                <a-button type="text" size="small" danger @click="handleDelete(item.id)">
                  <Icon icon="ant-design:delete-outlined" />
                </a-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed, onMounted, onUnmounted } from 'vue';
  import { BasicForm, useForm } from '@/components/Form';
  import { Icon } from '@/components/Icon';
  import { useMessage } from '@/hooks/web/useMessage';
  import { videoFormSchemas } from './AiVideo.data';
  import { submitVideoTask, queryVideoTask, getVideoListByUser, deleteVideoRecord } from './AiVideo.api';
  import { getFileAccessHttpUrl } from '@/utils/common/compUtils';
  import { useUserStore } from '@/store/modules/user';

  const TASK_ID_KEY = 'ai_video_task_id';

  const { createMessage } = useMessage();
  const userStore = useUserStore();

  // 左侧表单：不包含文案
  const leftFormSchemas = videoFormSchemas.filter((item) => !['text'].includes(item.field));

  const [registerForm, { validate }] = useForm({
    schemas: leftFormSchemas,
    showActionButtonGroup: false,
    wrapperCol: { span: 24 },
    labelCol: { span: 24 },
  });

  const generating = ref(false);
  const videoRef = ref<HTMLVideoElement | null>(null);
  const currentVideoUrl = ref<string>('');
  const currentText = ref<string>('');
  const formText = ref<string>('');
  const historyList = ref<any[]>([]);
  const isPresetApplying = ref(false);
  const elapsedSeconds = ref(0);
  const statusText = ref('任务已提交，排队中...');

  let pollTimer: ReturnType<typeof setInterval> | null = null;
  let elapsedTimer: ReturnType<typeof setInterval> | null = null;

  const elapsedTimeText = computed(() => {
    const minutes = Math.floor(elapsedSeconds.value / 60);
    const seconds = elapsedSeconds.value % 60;
    if (minutes > 0) {
      return `${minutes}分${seconds}秒`;
    }
    return `${seconds}秒`;
  });

  const presetTexts = [
    { title: '沙滩金毛犬', content: '一只金毛犬在金色的沙滩上奔跑，海浪轻轻拍打着岸边，阳光明媚，慢动作镜头' },
    { title: '航拍山脉全景', content: '航拍壮丽的山脉全景，云雾缭绕在山峰之间，镜头缓缓推进。' },
    { title: '咖啡微距特写', content: '一杯咖啡被缓缓倒入透明玻璃杯中，咖啡与牛奶融合形成美丽的纹理，微距特写。' },
    { title: '极光延时摄影', content: '星空下的极光在天空中舞动，色彩绚烂，延时摄影效果。' },
    {
      title: '女讲师教学',
      content:
        '女讲师，站在 PPT 前手持教鞭指向内容，表情认真亲和，讲解自然流畅，手势得体大方，光线明亮清晰，背景干净整洁，1080P 高清，画面稳定流畅，适合知识讲解、课程教学，风格专业、清晰、有说服力',
    },
    {
      title: '口红带货主播',
      content:
        '画面主体： 一位美丽的年轻中国女主播，特写镜头，面对镜头微笑。\n' +
        '外貌着装： 她穿着浅色职业装，妆容精致无瑕。\n' +
        '核心动作： 她一只手握着一支高端口红，另一只手优雅地打开盖子，露出丝滑的膏体。她温柔地将口红涂抹在自己的嘴唇上，动作轻柔，目光专注且含笑。\n' +
        '背景环境： 身后是明亮整洁的直播工作室，有环形灯补光，背景呈柔和的虚化效果。\n' +
        '画面质感： 高色彩饱和度，电影级布光，4k分辨率，60fps。\n' +
        '特殊效果： 使用慢镜头捕捉口红涂抹瞬间的丝滑质感。\n'+
        '语言风格：中文，适合电商直播带货场景，风格专业、清晰、有说服力',
    },
  ];

  /**
   * 根据当前用户id加载视频列表
   */
  async function loadVideoList() {
    try {
      const userId = userStore.getUserInfo?.id;
      if (!userId) return;
      const res = await getVideoListByUser({ userId });
      if (res && res.result) {
        const list = Array.isArray(res.result) ? res.result : res.result?.records || [];
        historyList.value = list.map((item) => ({
          ...item,
          videoFullUrl: item.videoUrl ? getFileAccessHttpUrl(item.videoUrl) : '',
        }));
      }
    } catch (e) {
      // ignore
    }
  }

  // 页面加载时获取历史列表
  loadVideoList();

  /**
   * 应用预设文案
   */
  function handleApplyPreset(text: string) {
    if (isPresetApplying.value) return;
    isPresetApplying.value = true;
    formText.value = text;
    setTimeout(() => {
      isPresetApplying.value = false;
    }, 300);
  }

  /**
   * 清除所有定时器
   */
  function clearTimers() {
    if (pollTimer) {
      clearInterval(pollTimer);
      pollTimer = null;
    }
    if (elapsedTimer) {
      clearInterval(elapsedTimer);
      elapsedTimer = null;
    }
  }

  /**
   * 开始生成（异步轮询模式）
   */
  async function handleGenerate() {
    try {
      const values = await validate();

      if (!formText.value.trim()) {
        createMessage.warn('请输入文案内容');
        return;
      }

      // 重置状态
      generating.value = true;
      currentVideoUrl.value = '';
      elapsedSeconds.value = 0;
      statusText.value = '任务已提交，排队中...';
      values.prompt = formText.value.trim();

      // 启动计时器
      elapsedTimer = setInterval(() => {
        elapsedSeconds.value++;
      }, 1000);

      // 提交任务
      const submitResult = await submitVideoTask(values);
      if (!submitResult || !submitResult.success || !submitResult.result?.taskId) {
        createMessage.error(submitResult?.message || '提交任务失败');
        generating.value = false;
        clearTimers();
        return;
      }

      const taskId = submitResult.result.taskId;
      localStorage.setItem(TASK_ID_KEY, taskId);
      statusText.value = '视频生成中...';

      // 开始轮询
      pollTimer = setInterval(async () => {
        try {
          const queryResult = await queryVideoTask(taskId);
          if (queryResult?.success && queryResult.result) {
            const { status, videoUrl, message } = queryResult.result;
            if (status === 'SUCCESS') {
              clearTimers();
              generating.value = false;
              localStorage.removeItem(TASK_ID_KEY);
              currentVideoUrl.value = getFileAccessHttpUrl(videoUrl) || '';
              currentText.value = formText.value;
              createMessage.success('视频生成成功！');
              await loadVideoList();
            } else if (status === 'FAIL') {
              clearTimers();
              generating.value = false;
              localStorage.removeItem(TASK_ID_KEY);
              createMessage.error(message || '视频生成失败');
            }
            // PROCESSING 状态继续轮询
          }
        } catch (e: any) {
          clearTimers();
          generating.value = false;
          createMessage.error('查询任务状态失败: ' + (e.message || '未知错误'));
        }
      }, 5000);
    } catch (error: any) {
      clearTimers();
      generating.value = false;
      if (error?.errorFields) {
        return;
      }
      createMessage.error(error.message || '提交任务失败');
    }
  }

  /**
   * 从历史记录中播放
   */
  function handlePlay(record: any) {
    currentVideoUrl.value = record.videoFullUrl || getFileAccessHttpUrl(record.videoUrl) || '';
    currentText.value = record.content;
    setTimeout(() => {
      (videoRef.value as any)?.play?.();
    }, 0);
  }

  /**
   * 将历史记录的文案回填到文案输入框
   */
  function handleUseText(record: any) {
    formText.value = record.content;
    createMessage.success('已将文案填入输入框');
  }

  /**
   * 下载视频文件
   */
  function handleDownload(record: any) {
    const url = record.videoFullUrl || getFileAccessHttpUrl(record.videoUrl);
    if (!url) {
      createMessage.error('下载地址不存在');
      return;
    }
    const link = document.createElement('a');
    link.href = url;
    link.download = record.fileName || `video-${Date.now()}.mp4`;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  /**
   * 删除历史记录
   */
  async function handleDelete(id: string) {
    const userId = userStore.getUserInfo?.id;
    if (!userId) return;
    try {
      const res = await deleteVideoRecord({ userId: userId, recordId: id });
      if (res.success) {
        createMessage.success('已删除');
        await loadVideoList();
      } else {
        createMessage.error(res.message || '删除失败');
      }
    } catch (e) {
      createMessage.error('删除失败');
    }
  }

  /**
   * 格式化时间展示
   */
  function formatTime(item: any) {
    return item?.createTime || '';
  }

  //update-begin---wangshuai---date:20260415  for：[QQYUN-14944]AI 改成异步的，支持切换菜单------------
  onMounted(() => {
    const savedTaskId = localStorage.getItem(TASK_ID_KEY);
    if (savedTaskId) {
      generating.value = true;
      currentVideoUrl.value = '';
      elapsedSeconds.value = 0;
      statusText.value = '任务恢复中，继续等待...';
      elapsedTimer = setInterval(() => {
        elapsedSeconds.value++;
      }, 1000);
      pollTimer = setInterval(async () => {
        try {
          const queryResult = await queryVideoTask(savedTaskId);
          if (queryResult?.success && queryResult.result) {
            const { status, videoUrl, message } = queryResult.result;
            if (status === 'SUCCESS') {
              clearTimers();
              generating.value = false;
              localStorage.removeItem(TASK_ID_KEY);
              currentVideoUrl.value = getFileAccessHttpUrl(videoUrl) || '';
              createMessage.success('视频生成成功！');
              await loadVideoList();
            } else if (status === 'FAIL') {
              clearTimers();
              generating.value = false;
              localStorage.removeItem(TASK_ID_KEY);
              createMessage.error(message || '视频生成失败');
            }
          }
        } catch (e: any) {
          clearTimers();
          generating.value = false;
          createMessage.error('查询任务状态失败: ' + (e.message || '未知错误'));
        }
      }, 5000);
    }
  });
  //update-end---wangshuai---date:20260415  for：[QQYUN-14944]AI 改成异步的，支持切换菜单------------
  onUnmounted(() => {
    clearTimers();
  });
</script>

<style lang="less" scoped>
  @import './AiVideo.less';
</style>
