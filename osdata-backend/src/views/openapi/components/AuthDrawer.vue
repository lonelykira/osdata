<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    title="接口授权"
    width="720px"
    destroyOnClose
    showFooter
    @ok="handleSubmit"
  >
    <a-spin :spinning="confirmLoading">
      <a-row :gutter="[12, 12]">
        <a-col :span="12" v-for="item in apiList" :key="item.id">
          <a-card
            :class="['auth-api-card', { 'auth-api-card--checked': item.checked }]"
            hoverable
            :body-style="{ padding: '12px' }"
            @click="handleSelect(item)"
          >
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span class="auth-api-name">{{ item.name }}</span>
              <a-checkbox v-model:checked="item.checked" @click.stop @change="(e) => handleChange(e, item)" />
            </div>
            <div style="margin-top: 6px; color: #888; font-size: 12px">
              <a-tag :color="getMethodColor(item.requestMethod)">{{ item.requestMethod }}</a-tag>
              <span style="margin-left: 4px">{{ item.requestUrl }}</span>
            </div>
          </a-card>
        </a-col>
      </a-row>
      <div v-if="apiList.length === 0 && !confirmLoading" style="text-align: center; padding: 40px 0; color: #999">
        暂无接口数据
      </div>
      <div v-if="total > 0" style="margin-top: 16px; text-align: right">
        <a-pagination
          :current="pageNo"
          :page-size="pageSize"
          :page-size-options="['10', '20', '30']"
          :total="total"
          show-quick-jumper
          show-size-changer
          size="small"
          @change="handlePageChange"
        />
      </div>
    </a-spin>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { getApiList, getPermissionList, permissionAddFunction } from '../OpenApiAuth.api';
  import { useMessage } from '/@/hooks/web/useMessage';

  const emit = defineEmits(['register', 'success']);
  const { createMessage } = useMessage();
  const confirmLoading = ref(false);
  const apiAuthId = ref('');
  const apiList = ref<any[]>([]);
  const selectedRowKeys = ref<string[]>([]);
  const pageNo = ref(1);
  const pageSize = ref(10);
  const total = ref(0);

  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    selectedRowKeys.value = [];
    apiList.value = [];
    pageNo.value = 1;
    pageSize.value = 10;
    total.value = 0;
    apiAuthId.value = data.record?.id || '';

    // Load existing permissions
    try {
      const permRes = await getPermissionList({ apiAuthId: apiAuthId.value });
      if (permRes && permRes.length > 0) {
        permRes.forEach((item) => {
          if (item.ifCheckBox == '1') {
            selectedRowKeys.value.push(item.id);
          }
        });
      }
    } catch (_e) {
      // ignore
    }
    await reload();
  });

  async function reload() {
    confirmLoading.value = true;
    try {
      const res = await getApiList({
        pageNo: pageNo.value,
        pageSize: pageSize.value,
        column: 'createTime',
        order: 'desc',
      });
      if (res.success) {
        const records = res.result.records || [];
        records.forEach((item) => {
          item.checked = selectedRowKeys.value.includes(item.id);
        });
        apiList.value = records;
        total.value = res.result.total || 0;
      } else {
        apiList.value = [];
        total.value = 0;
      }
    } finally {
      confirmLoading.value = false;
    }
  }

  function handleSelect(item) {
    item.checked = !item.checked;
    toggleSelection(item.id, item.checked);
  }

  function handleChange(e, item) {
    toggleSelection(item.id, e.target.checked);
  }

  function toggleSelection(id: string, checked: boolean) {
    const idx = selectedRowKeys.value.indexOf(id);
    if (checked && idx === -1) {
      selectedRowKeys.value.push(id);
    } else if (!checked && idx !== -1) {
      selectedRowKeys.value.splice(idx, 1);
    }
  }

  function handlePageChange(page, current) {
    pageNo.value = page;
    pageSize.value = current;
    reload();
  }

  function getMethodColor(method: string) {
    const map = { GET: 'green', POST: 'blue', PUT: 'orange', DELETE: 'red', PATCH: 'purple' };
    return map[method] || 'default';
  }

  async function handleSubmit() {
    confirmLoading.value = true;
    try {
      setDrawerProps({ confirmLoading: true });
      const res = await permissionAddFunction({
        apiId: selectedRowKeys.value.join(','),
        apiAuthId: apiAuthId.value,
      });
      if (res.success) {
        createMessage.success(res.message);
        closeDrawer();
        emit('success');
      } else {
        createMessage.warning(res.message);
      }
    } finally {
      confirmLoading.value = false;
      setDrawerProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
  .auth-api-card {
    transition: all 0.2s;
    border: 1px solid #d9d9d9;

    &--checked {
      border-color: #1890ff;
      background-color: #e6f7ff;
    }
  }

  .auth-api-name {
    font-weight: 500;
    font-size: 14px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 200px;
    display: inline-block;
  }
</style>
