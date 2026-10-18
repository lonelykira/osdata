<template>
  <div class="base-collapse">
    <div class="header">飞书集成</div>

    <!-- 步骤1：获取对接信息 -->
    <a-collapse expand-icon-position="right" :bordered="false">
      <a-collapse-panel key="1">
        <template #header>
          <div style="font-size: 16px">1. 获取对接信息</div>
        </template>
        <div class="base-desc"> 登录飞书开放平台创建企业自建应用，获取 App ID 和 App Secret 后即可开始集成 </div>
        <div style="margin-top: 5px">
          <a href="https://open.feishu.cn/app" target="_blank">前往飞书开放平台创建应用</a>
        </div>
        <div style="margin-top: 8px" class="base-desc">
          <ul style="list-style-type: disc; margin-left: 20px">
            <li>进入应用详情 → 凭证与基础信息，获取 App ID 和 App Secret</li>
          </ul>
        </div>
      </a-collapse-panel>
    </a-collapse>

    <!-- 步骤2：对接信息录入 -->
    <a-collapse expand-icon-position="right" :bordered="false">
      <a-collapse-panel key="2">
        <template #header>
          <div style="width: 100%; justify-content: space-between; display: flex">
            <div style="font-size: 16px">2. 对接信息录入及解绑</div>
          </div>
        </template>
        <div class="base-desc">填入 App ID 和 App Secret 后，即可开启飞书 OAuth2 扫码登录</div>
        <div class="flex-flow">
          <div class="base-title">App ID</div>
          <div class="base-message">
            <a-input-password v-model:value="appConfigData.clientId" readonly />
          </div>
        </div>
        <div class="flex-flow">
          <div class="base-title">App Secret</div>
          <div class="base-message">
            <a-input-password v-model:value="appConfigData.clientSecret" readonly />
          </div>
        </div>
        <div style="margin-top: 20px; width: 100%; text-align: right">
          <a-button @click="feishuEditClick">编辑</a-button>
          <a-button v-if="appConfigData.id" @click="cancelBindClick" danger style="margin-left: 10px">取消绑定</a-button>
        </div>
      </a-collapse-panel>
    </a-collapse>

    <!-- 步骤3：数据同步 -->
    <div class="sync-padding">
      <div style="font-size: 16px; width: 100%">3. 数据同步</div>
      <div style="margin-top: 20px" class="base-desc">
        从飞书同步到本系统（飞书→本地）
        <ul style="list-style-type: disc; margin-left: 20px">
          <li>同步飞书通讯录中的部门结构到本地</li>
          <li>
            同步部门下的用户到本地，并自动创建系统账号
            <a-tooltip title="需在飞书应用权限管理中开通 contact:department:read 和 contact:user.base:readonly 权限，并发布应用">
              <QuestionCircleOutlined class="sync-tip-icon" />
            </a-tooltip>
          </li>
          <li>已存在的用户和部门将跳过，不会重复创建</li>
        </ul>
        <div style="float: right; padding-bottom: 20px">
          <a-button :loading="btnLoading" :disabled="!appConfigData.id" @click="syncFeishu">
            {{ btnLoading ? '同步中...' : '同步通讯录' }}
          </a-button>
        </div>
      </div>
    </div>
  </div>

  <ThirdAppConfigModal @register="registerAppConfigModal" @success="handleSuccess" />
</template>

<script lang="ts">
  //update-begin---author:jeecg ---date:2026-05-13  for：【QQYUN-12767】飞书集成
  import { defineComponent, h, onMounted, ref } from 'vue';
  import { deleteThirdAppConfig, getThirdConfigByTenantId, syncFeishuDepartUserToLocal } from './ThirdApp.api';
  import { useModal } from '/@/components/Modal';
  import ThirdAppConfigModal from './ThirdAppConfigModal.vue';
  import { Modal } from 'ant-design-vue';
  import { getTenantId } from '/@/utils/auth';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { QuestionCircleOutlined } from '@ant-design/icons-vue';
  import { useGlobSetting } from '@/hooks/setting';

  export default defineComponent({
    name: 'ThirdAppFeishuConfigForm',
    components: {
      ThirdAppConfigModal,
      QuestionCircleOutlined,
    },
    setup() {
      const { createMessage } = useMessage();
      const globSetting = useGlobSetting();
      const btnLoading = ref<boolean>(false);
      const appConfigData = ref<any>({
        clientId: '',
        clientSecret: '',
      });
      const baseApiUrl = globSetting.domainUrl;
      const [registerAppConfigModal, { openModal }] = useModal();

      async function feishuEditClick() {
        let tenantId = getTenantId();
        openModal(true, {
          tenantId: tenantId,
          thirdType: 'feishu',
        });
      }

      async function initThirdAppConfigData(params) {
        let values = await getThirdConfigByTenantId(params);
        appConfigData.value = values || { clientId: '', clientSecret: '' };
      }

      function handleSuccess() {
        let tenantId = getTenantId();
        initThirdAppConfigData({ tenantId: tenantId, thirdType: 'feishu' });
      }

      /**
       * 同步飞书通讯录（飞书→本地）
       */
      async function syncFeishu() {
        if (!appConfigData.value.id) {
          createMessage.warning('请先配置飞书应用信息');
          return;
        }
        btnLoading.value = true;
        await syncFeishuDepartUserToLocal()
          .then((res) => {
            const options: any = {};
            if (res.result) {
              options.width = 600;
              options.title = res.message;
              options.content = () => {
                const nodes: any[] = [];
                const successList = res.result.successInfo || [];
                const failList = res.result.failInfo || [];
                if (failList.length > 0) {
                  nodes.push('失败信息如下：');
                  nodes.push(renderTextarea(h, failList.map((v, i) => `${i + 1}. ${v}`).join('\n')));
                  nodes.push(h('br'));
                }
                if (successList.length > 0) {
                  nodes.push('成功信息如下：');
                  nodes.push(renderTextarea(h, successList.map((v, i) => `${i + 1}. ${v}`).join('\n')));
                } else {
                  nodes.push('无成功信息');
                }
                return nodes;
              };
            }
            if (res.success) {
              options.title ? Modal.success(options) : createMessage.success(res.message || '同步成功');
            } else {
              options.title ? Modal.warning(options) : createMessage.warning(res.message || '同步完成（含失败项）');
            }
          })
          .catch(() => {
            createMessage.error('同步请求失败，请检查飞书应用配置及网络');
          })
          .finally(() => {
            btnLoading.value = false;
          });
      }

      function renderTextarea(h, value) {
        return h(
          'div',
          {
            style: {
              minHeight: '100px',
              border: '1px solid #d9d9d9',
              fontSize: '14px',
              maxHeight: '250px',
              whiteSpace: 'pre',
              overflow: 'auto',
              padding: '10px',
              borderRadius: '4px',
            },
          },
          value
        );
      }

      function cancelBindClick() {
        if (!appConfigData.value.id) {
          createMessage.warning('请先绑定飞书应用！');
          return;
        }
        Modal.confirm({
          title: '取消绑定',
          content: '是否要解除当前租户的飞书应用配置绑定？',
          okText: '确认',
          cancelText: '取消',
          onOk: () => {
            deleteThirdAppConfig({ id: appConfigData.value.id }, handleSuccess);
          },
        });
      }

      onMounted(() => {
        let tenantId = getTenantId();
        initThirdAppConfigData({ tenantId: tenantId, thirdType: 'feishu' });
      });

      return {
        appConfigData,
        baseApiUrl,
        btnLoading,
        registerAppConfigModal,
        feishuEditClick,
        handleSuccess,
        syncFeishu,
        cancelBindClick,
      };
    },
  });
  //update-end---author:jeecg ---date:2026-05-13  for：【QQYUN-12767】飞书集成
</script>

<style lang="less" scoped>
  .header {
    align-items: center;
    box-sizing: border-box;
    display: flex;
    height: 50px;
    font-weight: 700;
    font-size: 18px;
    color: @text-color;
  }

  .flex-flow {
    display: flex;
    min-height: 0;
  }

  .sync-padding {
    padding: 12px 0 16px;
    color: @text-color;
  }

  .sync-tip-icon {
    margin-left: 4px;
    cursor: pointer;
    position: relative;
    top: 2px;
    color: #8c8c8c;
  }

  .base-collapse {
    margin-top: 20px;
    padding: 0 24px;
    font-size: 20px;

    .base-desc {
      color: #a1a1a1;
      font-size: 14px;

      code {
        color: #1677ff;
        background: #f0f5ff;
        padding: 0 4px;
        border-radius: 2px;
        font-size: 12px;
      }
    }

    .base-title {
      width: 100px;
      text-align: left;
      height: 50px;
      line-height: 50px;
    }

    .base-message {
      width: 100%;
      height: 50px;
      line-height: 50px;
    }

    :deep(.ant-collapse-header) {
      padding: 12px 0 16px;
    }

    :deep(.ant-collapse-content-box) {
      padding-left: 0;
    }
  }
  /*begin 兼容暗夜模式*/
  [data-theme='dark'] .base-collapse .ant-collapse {
    border: none !important;
  }
  /*end 兼容暗夜模式*/
  :deep(.ant-collapse-borderless > .ant-collapse-item:last-child) {
    border-bottom-width: 1px;
  }
</style>
