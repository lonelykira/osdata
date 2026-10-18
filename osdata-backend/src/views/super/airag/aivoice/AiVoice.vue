<template>
  <div class="ai-voice-page">
    <!-- 头部区域 -->
    <div class="page-header">
      <div class="title">AI语音</div>
      <div class="subtitle">将文本快速转换为自然流畅的语音</div>
    </div>

    <div class="content-wrapper">
      <!-- 左侧语音控制 -->
      <div class="control-panel">
        <div class="panel-title">语音控制</div>
        <div class="form-container">
          <BasicForm @register="registerForm" :schemas="leftFormSchemas" />
        </div>
      </div>

      <!-- 中间区域：试听 + 文案 + 常用场景 + 生成按钮 -->
      <div class="middle-wrapper">
        <!-- 试听区域 -->
        <div class="preview-panel">
          <div class="panel-title">试听区域</div>
          <div class="preview-content">
            <div v-if="!currentAudioUrl" class="empty-state">
              <Icon icon="ant-design:customer-service-outlined" size="72" color="#c0c4cc" />
              <p>填写下方文案并点击「开始合成」</p>
              <p class="tip">支持调整倍速、音量增益和声色，生成更加个性化的语音效果</p>
            </div>

            <div v-else class="audio-player-wrapper">
              <audio ref="audioRef" :src="currentAudioUrl" controls class="audio-control" />
            </div>
          </div>
          <!-- 当前播放音频信息 -->
          <div v-if="currentAudioUrl" class="audio-info-section">
            <div class="current-audio-info">
              <span class="info-label">当前语音：</span>
              <span class="info-text">{{ currentText }}</span>
            </div>
          </div>
        </div>

        <!-- 文案输入和常用场景 -->
        <div class="input-section">
          <!-- 文案输入框 -->
          <div class="form-item-group">
            <label class="form-label">文案</label>
            <a-textarea v-model:value="formText" :rows="4" :maxlength="500" show-count placeholder="请输入要合成的文案内容" />
          </div>

          <!-- 常用场景 -->
          <div class="preset-group">
            <div class="preset-label">常用场景</div>
            <div class="preset-items">
              <div v-for="item in presetTexts" :key="item" class="preset-item" @click.prevent="handleApplyPreset(item)">
                {{ item }}
              </div>
            </div>
          </div>

          <!-- 开始合成按钮 -->
          <div class="action-btn-group">
            <a-button type="primary" size="large" block :loading="generating" @click="handleSynthesize">
              <Icon icon="ant-design:sound-outlined" />
              开始合成
            </a-button>
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
            <div v-for="item in historyList" :key="item.id" class="history-item">
              <div class="item-header">
                <span class="item-title" :title="item.content">{{ item.content }}</span>
                <span class="item-time">{{ item.createTime }}</span>
              </div>
              <div class="item-actions">
                <a-button type="text" size="small" @click="handlePlay(item)">
                  <Icon icon="ant-design:sound-outlined" />
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
  import { ref, onMounted, onUnmounted } from 'vue';
  import { BasicForm, useForm } from '@/components/Form';
  import { Icon } from '@/components/Icon';
  import { useMessage } from '@/hooks/web/useMessage';
  import { voiceFormSchemas } from './AiVoice.data';
  import { generateVoiceAsync, queryVoiceTask, getVoiceListByUser, deleteVoiceRecord } from '@/views/super/airag/aivoice/AiVoice.api';
  import { getFileAccessHttpUrl } from '@/utils/common/compUtils';
  import { useUserStore } from '@/store/modules/user';

  const TASK_ID_KEY = 'ai_voice_task_id';

  const { createMessage } = useMessage();
  const userStore = useUserStore();

  // 左侧表单：只包含模型、倍速、音量、声色，不包含文案
  const leftFormSchemas = voiceFormSchemas.filter((item) => !['text'].includes(item.field));

  const [registerForm, { validate }] = useForm({
    schemas: leftFormSchemas,
    showActionButtonGroup: false,
    wrapperCol: { span: 24 },
    labelCol: { span: 24 },
  });

  const generating = ref(false);
  const audioRef = ref<HTMLAudioElement | null>(null);
  const currentAudioUrl = ref<string>('');
  const currentText = ref<string>('');
  const formText = ref<string>(''); // 文案输入框的独立状态
  const historyList = ref<any[]>([]);
  const isPresetApplying = ref(false); // 防抖标志
  let pollTimer: ReturnType<typeof setTimeout> | null = null;

  /**
   * 根据当前用户id加载语音列表
   */
  async function loadVoiceList() {
    try {
      const userId = userStore.getUserInfo?.id;
      if (!userId) {
        return;
      }
      await getVoiceListByUser({ userId }).then((res) =>{
        if(res && res.result){
          for (const rs of res.result) {
              if (rs.voiceUrl) {
                rs.audioUrl = getFileAccessHttpUrl(rs.voiceUrl) || '';
              }
          }
          historyList.value = res.result || [];
        }
      });
    } catch (e) {
     
    }
  }

  // 页面加载时获取历史列表
  loadVoiceList();

  const presetTexts = [
    '欢迎来到我们的平台，祝您使用愉快！',
    '今天的天气非常好，适合出门散步。',
    '尊敬的客户，您的订单已发货，请注意查收。',
    'Welcome to our platform, we hope you enjoy the experience!',
  ];

  /**
   * 应用预设文案
   */
  function handleApplyPreset(text: string) {
    if (isPresetApplying.value) return;

    isPresetApplying.value = true;
    formText.value = text;

    // 300ms 后取消防抖标志
    setTimeout(() => {
      isPresetApplying.value = false;
    }, 300);
  }

  /**
   * 开始合成（异步轮询模式）
   */
  async function handleSynthesize() {
    try {
      const values = await validate();

      if (!formText.value.trim()) {
        createMessage.warn('请输入文案内容');
        return;
      }

      generating.value = true;
      currentAudioUrl.value = '';
      values.content = formText.value.trim();

     //update-begin---wangshuai---date:20260415  for：[QQYUN-14944]AI 改成异步的，支持切换菜单------------
      const res = await generateVoiceAsync(values);
      if (res.success && res.result) {
        const taskId = res.result as string;
        localStorage.setItem(TASK_ID_KEY, taskId);
        startPolling(taskId);
      } else {
        generating.value = false;
        createMessage.error(res.message || '提交任务失败');
      }
    } catch (e) {
      generating.value = false;
    }
  }

  /** 轮询查询语音任务结果 */
  function startPolling(taskId: string) {
    const poll = () => {
      queryVoiceTask(taskId)
  //update-end---wangshuai---date:20260415  for：[QQYUN-14944]AI 改成异步的，支持切换菜单------------
        .then((res) => {
          if (res.success) {
            //update-begin---wangshuai---date:20260415  for：[QQYUN-14944]AI 改成异步的，支持切换菜单------------
            if (res.result === 'pending' || res.result === null) {
              pollTimer = setTimeout(poll, 3000);
            } else {
              generating.value = false;
              localStorage.removeItem(TASK_ID_KEY);
              currentAudioUrl.value = getFileAccessHttpUrl(res.result.voiceUrl) || '';
              currentText.value = formText.value;
              createMessage.success('语音合成完成');
              loadVoiceList();
            }
          } else {
            generating.value = false;
            localStorage.removeItem(TASK_ID_KEY);
            createMessage.error(res.message || '语音合成失败');
          }
        })
        .catch(() => {
          pollTimer = setTimeout(poll, 3000);
        });
    };
    poll();
    //update-end---wangshuai---date:20260415  for：[QQYUN-14944]AI 改成异步的，支持切换菜单------------
  }

  //update-begin---wangshuai---date:20260415  for：[QQYUN-14944]AI 改成异步的，支持切换菜单------------
  onMounted(() => {
    const savedTaskId = localStorage.getItem(TASK_ID_KEY);
    if (savedTaskId) {
      generating.value = true;
      currentAudioUrl.value = '';
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
   * 从历史记录中播放
   */
  function handlePlay(record: any) {
    currentAudioUrl.value = getFileAccessHttpUrl(record.voiceUrl) || '';
    currentText.value = record.content;
    setTimeout(() => {
      (audioRef.value as any)?.play?.();
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
   * 下载语音文件
   */
  function handleDownload(record: any) {
    const url = getFileAccessHttpUrl(record.voiceUrl);
    if (!url) {
      createMessage.error('下载地址不存在');
      return;
    }
    const link = document.createElement('a');
    link.href = url;
    link.download = record.fileName || 'voice.wav';
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
      const res = await deleteVoiceRecord({ userId: userId, recordId: id });
      if (res.success) {
        createMessage.success('已删除');
        loadVoiceList();
      } else {
        createMessage.error(res.message || '删除失败');
      }
    } catch (e) {
      createMessage.error('删除失败');
    }
  }
</script>

<style lang="less" scoped>
  @import './AiVoice.less';
</style>
