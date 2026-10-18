<template>
  <div :class="prefixCls">
    <!--引用表格-->
    <BasicTable ref="tableRef" @register="registerTable" :rowSelection="rowSelection" v-bind="$attrs">
      <template #tableTitle>
        <a-button :class="newAddBtn" @click="onAdd" type="primary" preIcon="ant-design:plus">新增</a-button>
        <a-button :class="aiCreateTable" @click="onAiCreateTable" type="primary" preIcon="ant-design:plus">AI建表</a-button>
        <a-button :class="customBtn" @click="onShowCustomButton" type="primary" preIcon="ant-design:highlight">自定义按钮</a-button>
        <a-button :class="enhanceJsBtn" @click="onShowEnhanceJs" type="primary" preIcon="ant-design:strikethrough">JS增强</a-button>
        <a-button :class="enhanceSqlBtn" @click="onShowEnhanceSql" type="primary" preIcon="ant-design:filter" v-auth="'online:form:enhanceSql:save'"> SQL增强 </a-button>
        <a-button :class="enhanceJavaBtn" @click="onShowEnhanceJava" type="primary" preIcon="ant-design:tool">JAVA增强</a-button>
        <a-button :class="exportDbBtn" @click="onImportDbTable" type="primary" preIcon="ant-design:database" v-auth="'online:form:importTable'">导入数据库表</a-button>
        <a-button :class="codeGeneratorBtn" @click="onGenerateCode" type="primary" preIcon="bx:bx-code-alt" v-auth="'online:form:generateCode'"> 代码生成 </a-button>
        <!--        <j-super-query :fieldList="superQueryFieldList" ref="superQueryModal"-->
        <!--                       @handleSuperQuery="handleSuperQuery"></j-super-query>-->
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="onDeleteBatch">
                <a-icon type="delete" />
                <span>删除</span>
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            <span>批量操作</span>
            <a-icon type="down" />
          </a-button>
        </a-dropdown>
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
  <CgformModal @register="registerCgformModal" @success="onSuccess" />
  <DbToOnlineModal @register="registerDbToOnlineModal" @success="onSuccess" />
  <CodeGeneratorModal @register="registerCodeGeneratorModal" />
  <CustomButtonList @register="registerCustomButtonModal" />
  <EnhanceJsModal @register="registerEnhanceJsModal" />
  <EnhanceJavaModal @register="registerEnhanceJavaModal" />
  <EnhanceSqlModal @register="registerEnhanceSqlModal" />
  <AuthManagerDrawer @register="registerAuthManagerDrawer" />
  <AuthSetterModal @register="registerAuthSetterModal" />
  <AiModal @register="registerAiToOnlineModal" @success="onCreateAiTable"/>
  <CgformAddressModal @register="registerAddressModal" />
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import CgformModal from './components/CgformModal.vue';
  import DbToOnlineModal from './components/DbToOnlineModal.vue';
  import CodeGeneratorModal from './components/CodeGeneratorModal.vue';
  import CustomButtonList from './components/button/CustomButtonList.vue';
  import EnhanceJsModal from './components/enhance/EnhanceJsModal.vue';
  import EnhanceJavaModal from './components/enhance/EnhanceJavaModal.vue';
  import EnhanceSqlModal from './components/enhance/EnhanceSqlModal.vue';
  import AuthManagerDrawer from './components/auth/AuthManagerDrawer.vue';
  import AuthSetterModal from './components/auth/AuthSetterModal.vue';
  import AiModal from './components/AiModal.vue';
  import CgformAddressModal from "./components/CgformAddressModal.vue";
  import { useCgformList } from './hooks/useCgformList';
  import { CgformPageType } from './types';
  import { columns, searchFormSchema } from './cgform.data';
  import { useGuide } from './hooks/useGuide';
  import { createLocalStorage } from '@/utils/cache';
  import { useMessage } from '@/hooks/web/useMessage';
  const $ls = createLocalStorage();
  const { createMessage: $message } = useMessage();
  // noinspection JSUnusedGlobalSymbols
  export default defineComponent({
    name: 'CgformIndex',
    components: {
      BasicTable,
      TableAction,
      CgformModal,
      DbToOnlineModal,
      CodeGeneratorModal,
      CustomButtonList,
      EnhanceJsModal,
      EnhanceJavaModal,
      EnhanceSqlModal,
      AuthManagerDrawer,
      AuthSetterModal,
      AiModal,
      CgformAddressModal,
    },
    setup() {
      const {
        pageContext,
        onAdd,
        onAiCreateTable,
        onSuccess,
        onCreateAiTable,
        onDeleteBatch,
        onImportDbTable,
        onGenerateCode,
        onShowCustomButton,
        onShowEnhanceJs,
        onShowEnhanceSql,
        onShowEnhanceJava,
        getTableAction,
        getDropDownAction,
        registerAuthManagerDrawer,
        registerAuthSetterModal,
        registerCustomButtonModal,
        registerEnhanceJsModal,
        registerEnhanceSqlModal,
        registerEnhanceJavaModal,
        registerCgformModal,
        registerDbToOnlineModal,
        registerCodeGeneratorModal,
        registerAiToOnlineModal,
        registerAddressModal,
        tableRef,
      } = useCgformList({
        pageType: CgformPageType.normal,
        designScope: 'online-cgform-list',
        columns: columns,
        formSchemas: searchFormSchema,
      });
      const { prefixCls, tableContext } = pageContext;
      const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

      // 代码直接生成到前端，页面会自动加载刷新，给成功后提示
      const genenrate_msg = $ls.get('code.genenrate.success.msg');
      console.log('genenrate_msg:', genenrate_msg);
      if (genenrate_msg) {
        $message.success({
          content: genenrate_msg,
          duration: 10, // 停留10秒
        });
      }

      const {
        newAddBtn,
        customBtn,
        enhanceJsBtn,
        enhanceSqlBtn,
        exportDbBtn,
        enhanceJavaBtn,
        codeGeneratorBtn,
        aiCreateTable
      } = useGuide();
      
      return {
        prefixCls,
        reload,
        rowSelection,
        selectedRowKeys,
        onAdd,
        onAiCreateTable,
        onSuccess,
        onDeleteBatch,
        onImportDbTable,
        onGenerateCode,
        onShowCustomButton,
        onShowEnhanceJs,
        onShowEnhanceSql,
        onShowEnhanceJava,
        onCreateAiTable,
        getTableAction,
        getDropDownAction,
        registerAuthManagerDrawer,
        registerAuthSetterModal,
        registerCustomButtonModal,
        registerEnhanceJsModal,
        registerEnhanceSqlModal,
        registerEnhanceJavaModal,
        registerTable,
        registerCgformModal,
        registerDbToOnlineModal,
        registerCodeGeneratorModal,
        registerAiToOnlineModal,
        registerAddressModal,
        newAddBtn,
        customBtn,
        enhanceJsBtn,
        enhanceSqlBtn,
        enhanceJavaBtn,
        exportDbBtn,
        codeGeneratorBtn,
        aiCreateTable,
        tableRef,
      };
    },
  });
</script>

<style scoped></style>
