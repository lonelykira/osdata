<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="title"
    width="90%"
    destroyOnClose
    @ok="handleSubmit"
    :showFooter="showFooter"
  >
    <!-- 上部：基本信息表单 -->
    <BasicForm @register="registerForm" ref="formRef">
      <template #requestUrlSlot="{ model, field }">
        <a-input-group compact style="display: flex">
          <a-input v-model:value="model[field]" disabled style="flex: 1; min-width: 0" />
          <a-select
            v-model:value="model['requestMethod']"
            style="width: 130px; flex-shrink: 0"
            :options="requestMethodOptions"
            :disabled="formDisabled"
            placeholder="请求方式"
          />
        </a-input-group>
      </template>
      <template #whiteListSlot="{ model, field }">
        <a-textarea
          v-model:value="model[field]"
          :rows="5"
          placeholder="示例：&#10;192.168.1.100&#10;10.0.0.0/8&#10;172.16.*.*"
          :disabled="formDisabled"
        />
        <!-- 标签预览 -->
        <div v-if="model[field]" style="margin-top: 8px">
          <a-tag
            v-for="item in parseWhiteList(model[field])"
            :key="item"
            color="green"
            style="margin-bottom: 4px"
          >
            {{ item }}
          </a-tag>
        </div>
        <!-- 整理按钮 -->
        <div v-if="model[field] && !formDisabled" style="margin-top: 4px; text-align: right">
          <a-button size="small" @click="formatWhiteList(model, field)">整 理</a-button>
        </div>
      </template>
    </BasicForm>

    <!-- 下部：Tabs -->
    <a-tabs v-model:activeKey="activeTab" style="margin-top: 16px">
      <a-tab-pane key="headers" tab="请求头">
        <JVxeTable
          keep-source
          ref="openApiHeader"
          :loading="openApiHeaderTable.loading"
          :columns="openApiHeaderTable.columns"
          :dataSource="openApiHeaderTable.dataSource"
          :height="240"
          :disabled="formDisabled"
          :rowNumber="true"
          :rowSelection="true"
          :toolbar="true"
          asyncRemove
          @removed="onHeaderRemoved"
        />
      </a-tab-pane>

      <a-tab-pane key="params" tab="请求参数">
        <JVxeTable
          keep-source
          ref="openApiParam"
          :loading="openApiParamTable.loading"
          :columns="openApiParamTable.columns"
          :dataSource="openApiParamTable.dataSource"
          :height="240"
          :disabled="formDisabled"
          :rowNumber="true"
          :rowSelection="true"
          :toolbar="true"
        />
      </a-tab-pane>

      <a-tab-pane key="body" tab="请求体">
        <div style="margin-bottom: 8px" v-if="!formDisabled">
          <a-space>
            <a-button size="small" @click="toggleBodyExample">
              {{ showBodyExample ? '隐藏示例' : '查看示例' }}
            </a-button>
            <a-button size="small" type="primary" ghost @click="fillBodyExample" :disabled="formDisabled">填入示例</a-button>
            <a-button size="small" danger @click="clearBody" :disabled="formDisabled">清空</a-button>
          </a-space>
        </div>
        <div
          v-if="showBodyExample"
          style="margin-bottom: 12px; padding: 12px; background: #f5f5f5; border-radius: 4px; border-left: 3px solid #1890ff"
        >
          <div style="margin-bottom: 8px; color: #666; font-size: 12px">
            <strong>示例参考：</strong>以下为常见 JSON 请求体示例，请根据实际接口字段调整后填入下方编辑器。
          </div>
          <pre
            style="margin: 0; white-space: pre-wrap; word-break: break-all; font-family: 'Courier New', monospace; font-size: 12px; color: #333"
          >{{ bodyExample }}</pre>
        </div>
        <div style="border: 1px solid #d9d9d9; border-radius: 4px; min-height: 300px">
          <CodeEditor v-model:value="bodyContent" mode="application/json" :readonly="formDisabled" />
        </div>
      </a-tab-pane>

      <a-tab-pane key="response" tab="响应配置" v-if="false">
        <div style="margin-bottom: 16px">
          <h4 style="margin-bottom: 8px">响应示例</h4>
          <div style="border: 1px solid #d9d9d9; border-radius: 4px; min-height: 200px">
            <CodeEditor v-model:value="responseExample" mode="application/json" :readonly="formDisabled" />
          </div>
        </div>
        <div>
          <h4 style="margin-bottom: 8px">响应字段说明</h4>
          <JVxeTable
            keep-source
            ref="responseField"
            :loading="responseFieldTable.loading"
            :columns="responseFieldTable.columns"
            :dataSource="responseFieldTable.dataSource"
            :height="240"
            :disabled="formDisabled"
            :rowNumber="true"
            :rowSelection="true"
            :toolbar="true"
          />
        </div>
      </a-tab-pane>
    </a-tabs>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { ref, computed, unref, reactive } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { CodeEditor } from '/@/components/CodeEditor';
  import {
    formSchema,
    openApiHeaderJVxeColumns,
    openApiParamJVxeColumns,
    responseFieldJVxeColumns,
    requestMethodOptions,
    FIXED_HEADER_KEYS,
  } from '../OpenApi.data';
  import { saveOrUpdate, getGenPath } from '../OpenApi.api';
  import { useMessage } from '/@/hooks/web/useMessage';

  const emit = defineEmits(['register', 'success']);
  const $message = useMessage();
  // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-1266】默认请求头 appKey/signature/timestamp 删除时校验拦截且必填禁用
  // 默认请求头 appKey/signature/timestamp 删除时校验拦截

  /** 批量删除请求头时拦截默认参数 */
  async function onHeaderRemoved(event) {
    const deleteRows = event?.deleteRows || [];
    const blockedRows = deleteRows.filter((row) => FIXED_HEADER_KEYS.includes(row.headerKey));
    const allowRows = deleteRows.filter((row) => !FIXED_HEADER_KEYS.includes(row.headerKey));

    if (blockedRows.length) {
      const names = blockedRows.map((row) => row.headerKey).join('、');
      $message.createMessage.warning(`默认请求头 ${names} 不允许删除`);
    }

    // 全部都是默认请求头，直接取消删除
    if (!allowRows.length) {
      return;
    }

    // 仅删除允许删除的行
    if (blockedRows.length) {
      await openApiHeader.value?.removeRows(allowRows);
      await openApiHeader.value?.clearSelection?.();
      return;
    }

    // 没有默认请求头，走正常确认删除
    if (typeof event.confirmRemove === 'function') {
      await event.confirmRemove();
    }
  }
  // update-end--author:liaozhiyang---date:20260804---for：【LHZP-1266】默认请求头 appKey/signature/timestamp 删除时校验拦截且必填禁用
  const isUpdate = ref(true);
  const formDisabled = ref(false);
  const showFooter = ref(true);
  const activeTab = ref('headers');
  const bodyContent = ref('');
  const responseExample = ref('');
  const showBodyExample = ref(false);

  // 请求体示例代码（供前端参考/一键填入）
  const bodyExample = `{
  "name": "jeecg",
  "remember": true,
  "extra": {
    "deviceId": "abc123",
    "platform": "web"
  },
  "roles": ["jeecg", "user"]
}`;

  const openApiHeader = ref();
  const openApiParam = ref();
  const responseField = ref();

  const openApiHeaderTable = reactive({
    loading: false,
    dataSource: [] as any[],
    columns: openApiHeaderJVxeColumns,
  });
  const openApiParamTable = reactive({
    loading: false,
    dataSource: [] as any[],
    columns: openApiParamJVxeColumns,
  });
  const responseFieldTable = reactive({
    loading: false,
    dataSource: [] as any[],
    columns: responseFieldJVxeColumns,
  });

  const [registerForm, { setProps, resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 100,
    schemas: formSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 12 },
  });

  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await reset();
    showFooter.value = !!data?.showFooter;
    setDrawerProps({ confirmLoading: false, showFooter: showFooter.value });
    isUpdate.value = !!data?.isUpdate;
    formDisabled.value = !data?.showFooter;

    if (unref(isUpdate)) {
      await setFieldsValue({
        ...data.record,
      });
      openApiHeaderTable.dataSource = data.record.headersJson ? JSON.parse(data.record.headersJson) : [];
      openApiParamTable.dataSource = data.record.paramsJson ? JSON.parse(data.record.paramsJson) : [];
      bodyContent.value = data.record.requestBody || '';
      responseExample.value = data.record.responseExample || '';
      responseFieldTable.dataSource = data.record.responseFieldsJson ? JSON.parse(data.record.responseFieldsJson) : [];
    } else {
      const requestUrlObj = await getGenPath({});
      await setFieldsValue({
        requestUrl: requestUrlObj.result,
      });
      openApiHeaderTable.dataSource = [
        { headerKey: 'appKey', required: '1', paramType: 'string', defaultValue: '', example: '', note: '' },
        { headerKey: 'signature', required: '1', paramType: 'string', defaultValue: '', example: '', note: '' },
        { headerKey: 'timestamp', required: '1', paramType: 'string', defaultValue: '', example: '', note: '' },
      ];
    }
    setProps({ disabled: !data?.showFooter });
  });

  const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(formDisabled) ? '编辑' : '详情'));

  /** 解析白名单文本为条目数组 */
  function parseWhiteList(text: string): string[] {
    if (!text) return [];
    return text
      .split(/[,\n]/)
      .map((s) => s.trim())
      .filter(Boolean);
  }

  /** 整理白名单：去空行、去重、每行一个 */
  function formatWhiteList(model: any, field: string) {
    const items = parseWhiteList(model[field]);
    const unique = [...new Set(items)];
    model[field] = unique.join('\n');
  }

  async function reset() {
    await resetFields();
    activeTab.value = 'headers';
    openApiHeaderTable.dataSource = [];
    openApiParamTable.dataSource = [];
    responseFieldTable.dataSource = [];
    bodyContent.value = '';
    responseExample.value = '';
    showBodyExample.value = false;
  }

  /** 切换请求体示例展示 */
  function toggleBodyExample() {
    showBodyExample.value = !showBodyExample.value;
  }

  /** 将示例代码填入请求体编辑器 */
  function fillBodyExample() {
    bodyContent.value = bodyExample;
  }

  /** 清空请求体 */
  function clearBody() {
    bodyContent.value = '';
  }

  async function handleSubmit() {
    try {
      const values = await validate();
      setDrawerProps({ confirmLoading: true });

      // Collect JVxeTable data（getTableData 直接返回数组）
      const headerData = openApiHeader.value?.getTableData();
      const paramData = openApiParam.value?.getTableData();
      const responseFieldData = responseField.value?.getTableData();

      const headersJson = headerData?.length ? JSON.stringify(headerData) : null;
      // update-begin--author:liusq---date:20260805---for：【请求参数】删除全部数据后保存，重新打开仍存在
      // 空数组需要明确提交，避免 null 被请求参数序列化忽略，导致后端保留原数据
      const paramsJson = JSON.stringify(paramData || []);
      // update-end--author:liusq---date:20260805---for：【请求参数】删除全部数据后保存，重新打开仍存在
      const responseFieldsJson = responseFieldData?.length ? JSON.stringify(responseFieldData) : null;

      // Validate body JSON
      if (bodyContent.value) {
        try {
          if (typeof JSON.parse(bodyContent.value) != 'object') {
            $message.createMessage.error('JSON格式化错误,请检查输入数据');
            return;
          }
        } catch (e) {
          $message.createMessage.error('JSON格式化错误,请检查输入数据');
          return;
        }
      }

      // Validate response example JSON
      if (responseExample.value) {
        try {
          JSON.parse(responseExample.value);
        } catch (e) {
          $message.createMessage.error('响应示例JSON格式错误,请检查输入数据');
          return;
        }
      }

      const submitValues = {
        ...values,
        headersJson,
        paramsJson,
        requestBody: bodyContent.value || null,
        responseExample: responseExample.value || null,
        responseFieldsJson,
      };

      await saveOrUpdate(submitValues, isUpdate.value);
      closeDrawer();
      emit('success');
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-calendar-picker) {
    width: 100%;
  }
</style>
