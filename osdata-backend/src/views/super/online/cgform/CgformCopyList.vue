<template>
  <div :class="prefixCls">
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button @click="onShowCustomButton" type="primary" preIcon="ant-design:highlight">自定义按钮</a-button>
        <a-button @click="onShowEnhanceJs" type="primary" preIcon="ant-design:strikethrough">JS增强</a-button>
        <a-button @click="onShowEnhanceSql" type="primary" preIcon="ant-design:filter">SQL增强</a-button>
        <a-button @click="onShowEnhanceJava" type="primary" preIcon="ant-design:tool">Java增强</a-button>
      </template>

      <template #dbSync="{ text }">
        <span v-if="text === 'Y'" style="color: limegreen">已同步</span>
        <span v-if="text === 'N'" style="color: red">未同步</span>
      </template>

      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
  </div>
  <CgformModal @register="registerCgformModal" :actionButton="false" @success="reload" />
  <EnhanceJsModal @register="registerEnhanceJsModal" />
  <EnhanceJavaModal @register="registerEnhanceJavaModal" />
  <EnhanceSqlModal @register="registerEnhanceSqlModal" />
  <DbToOnlineModal @register="registerDbToOnlineModal" @success="reload" />
  <CustomButtonList @register="registerCustomButtonModal" />
  <AuthManagerDrawer @register="registerAuthManagerDrawer" />
  <AuthSetterModal @register="registerAuthSetterModal" />
  <CgformAddressModal @register="registerAddressModal" />
</template>

<script lang="ts">
  import { watch, provide, defineComponent } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import CgformModal from './components/CgformModal.vue';
  import DbToOnlineModal from './components/DbToOnlineModal.vue';
  import CustomButtonList from './components/button/CustomButtonList.vue';
  import EnhanceJsModal from './components/enhance/EnhanceJsModal.vue';
  import EnhanceJavaModal from './components/enhance/EnhanceJavaModal.vue';
  import EnhanceSqlModal from './components/enhance/EnhanceSqlModal.vue';
  import AuthManagerDrawer from './components/auth/AuthManagerDrawer.vue';
  import AuthSetterModal from './components/auth/AuthSetterModal.vue';
  import CgformAddressModal from "./components/CgformAddressModal.vue";
  import { useCgformList } from './hooks/useCgformList';
  import { CgformPageType } from './types';

  // noinspection JSUnusedGlobalSymbols
  export default defineComponent({
    name: 'CgformCopyList',
    components: {
      BasicTable,
      TableAction,
      CgformModal,
      DbToOnlineModal,
      CustomButtonList,
      EnhanceJsModal,
      EnhanceJavaModal,
      EnhanceSqlModal,
      AuthManagerDrawer,
      AuthSetterModal,
      CgformAddressModal,
    },
    setup() {
      const pageType = CgformPageType.copy;
      provide('cgformPageType', pageType);
      const {
        router,
        pageContext,
        getTableAction,
        getDropDownAction,
        onShowCustomButton,
        onShowEnhanceJs,
        onShowEnhanceSql,
        onShowEnhanceJava,
        registerCustomButtonModal,
        registerEnhanceJsModal,
        registerEnhanceSqlModal,
        registerEnhanceJavaModal,
        registerAuthManagerDrawer,
        registerAuthSetterModal,
        registerCgformModal,
        registerDbToOnlineModal,
        registerAddressModal,
      } = useCgformList({
        pageType,
        designScope: 'online-cgform-list',
        columns: [
          { title: '视图表名', dataIndex: 'tableName' },
          { title: '视图表描述', dataIndex: 'tableTxt' },
          { title: '原表版本', dataIndex: 'copyVersion' },
          { title: '视图版本', dataIndex: 'tableVersion' },
        ],
        formSchemas: [{ label: '表名', field: 'tableName', component: 'JInput' }],
      });
      const { prefixCls, tableContext } = pageContext;
      const [registerTable, { reload }, { rowSelection }] = tableContext;
      watch(router.currentRoute, () => reload());
      return {
        prefixCls,
        reload,
        rowSelection,
        getTableAction,
        getDropDownAction,
        onShowCustomButton,
        onShowEnhanceJs,
        onShowEnhanceSql,
        onShowEnhanceJava,
        registerCustomButtonModal,
        registerEnhanceJsModal,
        registerEnhanceSqlModal,
        registerEnhanceJavaModal,
        registerAuthManagerDrawer,
        registerAuthSetterModal,
        registerTable,
        registerCgformModal,
        registerDbToOnlineModal,
        registerAddressModal,
      };
    },
  });
</script>

<style scoped></style>
