<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="调用日志详情" :width="800" :footer="null" :minHeight="600">
    <a-descriptions bordered :column="2" size="small" :labelStyle="{ whiteSpace: 'nowrap', width: '90px' }">
      <a-descriptions-item label="调用时间" :span="2">{{ record.callTime || '-' }}</a-descriptions-item>
      <a-descriptions-item label="请求方式">
        <a-tag :color="getMethodColor(record.requestMethod)">{{ record.requestMethod || '-' }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="状态码">
        <a-tag :color="getStatusColor(record.responseCode)">{{ record.responseCode || '-' }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="请求路径" :span="2">{{ record.requestPath || '-' }}</a-descriptions-item>
      <a-descriptions-item label="调用者AK">{{ record.callerAk || '-' }}</a-descriptions-item>
      <a-descriptions-item label="来源IP">{{ record.ip || '-' }}</a-descriptions-item>
      <a-descriptions-item label="耗时(ms)">{{ record.usedTime ?? '-' }}</a-descriptions-item>
      <a-descriptions-item label="失败原因">{{ record.errorMsg || '-' }}</a-descriptions-item>
      <a-descriptions-item label="请求头" :span="2">
        <pre class="detail-pre">{{ formatJson(record.requestHeaders) }}</pre>
      </a-descriptions-item>
      <a-descriptions-item label="请求参数" :span="2">
        <pre class="detail-pre">{{ formatJson(record.requestParams) }}</pre>
      </a-descriptions-item>
      <a-descriptions-item v-if="record.responseBody" label="响应内容" :span="2">
        <pre class="detail-pre">{{ formatJson(record.responseBody) }}</pre>
      </a-descriptions-item>
    </a-descriptions>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicModal, useModalInner } from '/src/components/Modal';

  const record = ref<Record<string, any>>({});

  const [registerModal] = useModalInner((data) => {
    record.value = data.record || {};
  });

  function getStatusColor(code: number) {
    if (!code) return 'default';
    if (code >= 200 && code < 300) return 'success';
    if (code >= 400 && code < 500) return 'warning';
    if (code >= 500) return 'error';
    return 'default';
  }

  const methodColorMap: Record<string, string> = {
    GET: 'blue',
    POST: 'green',
    PUT: 'orange',
    DELETE: 'red',
    PATCH: 'purple',
  };
  function getMethodColor(method: string) {
    return methodColorMap[method] ?? 'default';
  }

  function formatJson(val: any) {
    if (!val) return '-';
    try {
      return JSON.stringify(JSON.parse(val), null, 2);
    } catch {
      return val;
    }
  }
</script>

<style scoped>
  .detail-pre {
    margin: 0;
    white-space: pre-wrap;
    word-break: break-all;
    font-size: 12px;
    max-height: 300px;
    overflow-y: auto;
    background: #f5f5f5;
    padding: 8px;
    border-radius: 4px;
  }
</style>
