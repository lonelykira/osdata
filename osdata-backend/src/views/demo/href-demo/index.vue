<template>
  <div class="href-demo-page">
    <a-card title="Href 参数接收示例" :bordered="false">
      <template #extra>
        <a-tag color="blue">Online 表单 Href 跳转</a-tag>
      </template>

      <a-divider orientation="left">路由参数 (route.query)</a-divider>
      <a-descriptions bordered :column="1" size="middle">
        <a-descriptions-item v-for="(value, key) in queryParams" :key="key" :label="key">
          <a-tag v-if="value" color="green">{{ value }}</a-tag>
          <a-tag v-else color="default">（空值）</a-tag>
        </a-descriptions-item>
      </a-descriptions>

      <a-empty v-if="Object.keys(queryParams).length === 0" description="未接收到任何参数" />

      <a-divider orientation="left">完整信息</a-divider>
      <a-descriptions bordered :column="2" size="small">
        <a-descriptions-item label="完整路径">{{ route.fullPath }}</a-descriptions-item>
        <a-descriptions-item label="路由名称">{{ route.name || '（无）' }}</a-descriptions-item>
        <a-descriptions-item label="参数来源">{{ Object.keys(props).filter(k => props[k]).length > 0 ? '内部组件 (props)' : '路由跳转 (query)' }}</a-descriptions-item>
        <a-descriptions-item label="参数个数">{{ Object.keys(queryParams).length }}</a-descriptions-item>
      </a-descriptions>

      <a-divider />
      <a-space>
        <a-button @click="goBack">返回上一页</a-button>
        <a-button type="primary" ghost @click="copyInfo">复制参数信息</a-button>
      </a-space>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { computed, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useMessage } from '/@/hooks/web/useMessage';

  const props = defineProps({
    name: { type: String, default: '' },
    code: { type: String, default: '' },
  });

  const route = useRoute();
  const router = useRouter();
  const { createMessage } = useMessage();

  // 合并 props 和 query 参数
  const queryParams = computed(() => {
    const params: Record<string, string> = {};
    // 路由 query 参数
    Object.entries(route.query).forEach(([k, v]) => {
      params[k] = String(v ?? '');
    });
    // 覆盖内部组件 props（同名 key 用 props 值）
    Object.entries(props).forEach(([k, v]) => {
      if (v) params[k] = String(v);
    });
    return params;
  });

  // 监听参数变化
  watch(
    () => route.query,
    (newQuery) => {
      console.log('===== Online Href $route props =====');
      console.log('query params:', newQuery);
      Object.entries(newQuery).forEach(([key, value]) => {
        console.log(`$route ${key}: ${value}`);
      });
    },
    { immediate: true, deep: true }
  );

  function goBack() {
    router.back();
  }

  function copyInfo() {
    const info = Object.entries(queryParams.value)
      .map(([k, v]) => `${k}=${v}`)
      .join('&');
    navigator.clipboard.writeText(info).then(() => {
      createMessage.success('参数已复制到剪贴板');
    });
  }
</script>

<style lang="less" scoped>
  .href-demo-page {
    padding: 24px;
    max-width: 800px;
    margin: 0 auto;
  }
</style>
