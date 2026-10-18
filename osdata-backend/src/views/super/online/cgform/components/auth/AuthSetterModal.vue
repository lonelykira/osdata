<template>
  <BasicModal
    title="Online权限授权"
    :width="900"
    :maskClosable="false"
    defaultFullscreen
    wrapClassName="onl-auth-setter-modal"
    :okButtonProps="{ style: { display: 'none' } }"
    cancelText="关闭"
    @cancel="closeModal"
    @register="registerModal"
    @open-change="hanldeOpenChange"
  >
    <a-spin wrapperClassName="authsetting-container" :spinning="loading">
      <a-row v-if="contentShow">
        <a-col :span="12">
          <a-tabs v-model:activeKey="authMode" @change="onAuthModeChange">
            <a-tab-pane tab="角色授权" key="role" forceRender>
              <LeftRole ref="roleRef" @select="onSelectRole" />
            </a-tab-pane>
            <a-tab-pane tab="部门授权" key="depart" forceRender>
              <LeftDepart ref="departRef" @select="onSelectDepart" />
            </a-tab-pane>
            <a-tab-pane tab="人员授权" key="user" forceRender>
              <LeftUser ref="userRef" @select="onSelectUser" />
            </a-tab-pane>
          </a-tabs>
        </a-col>
        <a-col :span="1"></a-col>
        <a-col :span="11">
          <a-tabs v-model:activeKey="activeKey" @change="onAuthTypeChange">
            <a-tab-pane tab="字段权限" key="field" forceRender>
              <AuthFieldTree class="authFieldTree" ref="fieldRef" :cgformId="cgformId" />
            </a-tab-pane>
            <template v-if="hasDataAuth">
              <a-tab-pane tab="按钮权限" key="button" forceRender>
                <AuthButtonTree class="authButtonTree" ref="buttonRef" :cgformId="cgformId" />
              </a-tab-pane>
              <a-tab-pane tab="数据权限" key="data" forceRender>
                <AuthDataTree class="authDataTree" ref="dataRef" :cgformId="cgformId" />
              </a-tab-pane>
            </template>
          </a-tabs>
        </a-col>
      </a-row>
    </a-spin>
  </BasicModal>
</template>

<script lang="ts">
  import LeftRole from './setter/LeftRole.vue';
  import LeftDepart from './setter/LeftDepart.vue';
  import LeftUser from './setter/LeftUser.vue';
  import AuthFieldTree from './setter/AuthFieldTree.vue';
  import AuthButtonTree from './setter/AuthButtonTree.vue';
  import AuthDataTree from './setter/AuthDataTree.vue';

  import { ref, computed, nextTick, defineComponent } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';

  export default defineComponent({
    name: 'AuthSetterModal',
    components: {
      BasicModal,
      LeftRole,
      LeftDepart,
      LeftUser,
      AuthFieldTree,
      AuthButtonTree,
      AuthDataTree,
    },
    props: {
      // 1单表 2主表 3附表
      tableType: { type: Number, default: 1 },
    },
    setup(props) {
      const cgformId = ref('');
      const loading = ref(false);
      const activeKey = ref('field');
      const authMode = ref('role');
      const refs = {
        fieldRef: ref(),
        buttonRef: ref(),
        dataRef: ref(),
        roleRef: ref(),
        userRef: ref(),
        departRef: ref(),
      };
      const activeRole = ref('');
      const hasDataAuth = computed(() => props.tableType == 1 || props.tableType == 2);
      const contentShow = ref(true);
      // 表单赋值
      const [registerModal, { closeModal }] = useModalInner((data) => {
        activeKey.value = 'field';
        // update-begin--author:liaozhiyang---date:20240520---for：【TV360X-147】权限设置重置到角色tab
        authMode.value = 'role';
        // update-end--author:liaozhiyang---date:20240520---for：【TV360X-147】权限设置重置到角色tab
        // QQYUN-4285【online表单】字段权限 勾选后不保存，关闭再次打开 还是选中状态
        cgformId.value = data.cgformId;
        reset();
      });

      function getActiveRef<T = any>(key = activeKey.value): T {
        return refs[key + 'Ref']?.value;
      }

      async function reset() {
        await nextTick();
        clearLeftCurrentTabSelect();
        getActiveRef().clear();
      }

      // 选中角色事件
      function onSelectRole(roleId) {
        activeRole.value = roleId;
        onAuthTypeChange(activeKey.value);
        clearLeftOtherTabSelect();
      }

      // 选中部门事件
      function onSelectDepart(departid) {
        activeRole.value = departid;
        onAuthTypeChange(activeKey.value);
        clearLeftOtherTabSelect();
      }

      // 选中用户事件
      function onSelectUser(userId) {
        activeRole.value = userId;
        onAuthTypeChange(activeKey.value);
        clearLeftOtherTabSelect();
      }

      // 清空左侧选中
      function clearLeftOtherTabSelect() {
        if (authMode.value == 'role') {
          refs.departRef.value.clearSelected();
          refs.userRef.value.clearSelected();
        } else if (authMode.value == 'depart') {
          refs.roleRef.value.clearSelected();
          refs.userRef.value.clearSelected();
        } else if (authMode.value == 'user') {
          refs.departRef.value.clearSelected();
          refs.roleRef.value.clearSelected();
        }
      }

      function clearLeftCurrentTabSelect() {
        if (authMode.value == 'role') {
          refs.roleRef.value.clearSelected();
        } else if (authMode.value == 'depart') {
          refs.departRef.value.clearSelected();
        } else if (authMode.value == 'user') {
          refs.userRef.value.clearSelected();
        }
        getActiveRef().clearChecked();
        // update-begin--author:liaozhiyang---date:20231226---for：【QQYUN-7543】左侧没选中角色、部门、人员时，右侧展示空
        activeRole.value = '';
        // update-end--author:liaozhiyang---date:20231226---for：【QQYUN-7543】左侧没选中角色、部门、人员时，右侧展示空
      }

      // 右侧授权类型切换 事件
      async function onAuthTypeChange(key) {
        // 切换 右侧tab 如果当前选中角色信息需要加载tab内选中信息
        await nextTick();
        if (activeRole.value) {
          getActiveRef(key).loadChecked(activeRole.value, authMode.value);
        }
      }

      // 左侧授权方式切换 事件
      function onAuthModeChange() {
        clearLeftCurrentTabSelect();
      }
      // update-begin--author:liaozhiyang---date:20240523---for：【TV360X-239】权限授权页面角色分页再次打开弹框时没有初始化为第一页
      const hanldeOpenChange = (open: boolean) => {
        contentShow.value = open;
      };
      // update-end--author:liaozhiyang---date:20240523---for：【TV360X-239】权限授权页面角色分页再次打开弹框时没有初始化为第一页
      return {
        ...refs,
        cgformId,
        loading,
        activeKey,
        hasDataAuth,
        authMode,
        onAuthModeChange,
        onAuthTypeChange,
        closeModal,
        onSelectRole,
        onSelectDepart,
        onSelectUser,
        registerModal,
        hanldeOpenChange,
        contentShow,
      };
    },
  });
</script>

<style lang="less" scoped>
  // update-begin--author:liaozhiyang---date:20231226---for：【QQYUN-7540】角色权限弹窗右侧内容较长时局部滚动
  .authsetting-container {
    height: 100%;
    :deep(.ant-spin-container) {
      height: 100%;
    }
    .ant-row,
    .ant-col {
      height: 100%;
    }
    .ant-tabs {
      height: 100%;
      :deep(.ant-tabs-content) {
        height: 100%;
      }
      .ant-tabs-tabpane {
        height: 100%;
        .authFieldTree, .authButtonTree, .authDataTree {
          height: 100%;
          display: flex;
          flex-direction: column;
          :deep(.ant-tree) {
            flex: 1;
            min-height: 0;
            overflow: auto;
          }
        }
      }
    }
  }
  // update-end--author:liaozhiyang---date:20231226---for：【QQYUN-7540】角色权限弹窗右侧内容较长时局部滚动
</style>

<style lang="less">
  .onl-auth-setter-modal:not(.fullscreen-modal) {
    .onl-auth-user-query-item {
      flex: 0 0 50%;
      max-width: 50%;
    }

    .onl-auth-user-query-actions {
      flex: 0 0 100%;
      max-width: 100%;
    }

    .onl-auth-tree-btns {
      display: grid;
      grid-template-columns: repeat(4, max-content);
      gap: 8px;
      margin-left: 10px;

      button {
        margin: 0;
      }
    }
  }
</style>
