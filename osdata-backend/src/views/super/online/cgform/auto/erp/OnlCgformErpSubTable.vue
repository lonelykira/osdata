<template>

  <!-- 加载中骨架屏 -->
  <a-skeleton v-if="tableReloading" active />

  <!-- 查询条件 -->
  <online-query-form
      v-if="mainTableSelectedRowRcord"
      v-show="!tableReloading"
      ref="onlineQueryFormOuter"
      :id="ID"
      :queryBtnCfg="getQueryButtonCfg"
      :resetBtnCfg="getResetButtonCfg"
      @search="queryWithCondition"
  />

  <!-- 列表 -->
  <BasicTable
    v-if="!tableReloading"
    ref="onlineTable"
    rowKey="jeecg_row_key"
    :canResize="false"
    :bordered="true"
    :showIndexColumn="false"
    :loading="loading"
    :columns="columns"
    :dataSource="dataSource"
    :pagination="pagination"
    :rowSelection="rowSelection"
    :actionColumn="actionColumn"
    :showTableSetting="true"
    :clickToRowSelect="false"
    :scroll="tableScroll"
    @table-redo="reload"
    :class="{ 'j-table-force-nowrap': enableScrollBar }"
    :tableSetting="tableSetting"
    @change="handleChangeInTable"
    :minHeight="300"
  >
    <template #tableTitle>
      <a-button
          v-if="btnShow && buttonSwitch.add && cgBIBtnMap['add'].enabled"
          type="primary"
          :preIcon="cgBIBtnMap['add'].buttonIcon"
          @click="handleAddRcord"
      >
        <span>{{cgBIBtnMap['add'].buttonName}}</span>
      </a-button>
      <a-button
          v-if="btnShow && buttonSwitch.import && cgBIBtnMap['import'].enabled"
          type="primary"
          :preIcon="cgBIBtnMap['import'].buttonIcon"
          @click="handleImportExcel"
      >
        <span>{{cgBIBtnMap['import'].buttonName}}</span>
      </a-button>
      <a-button
          v-if="btnShow && buttonSwitch.export && cgBIBtnMap['export'].enabled"
          type="primary"
          :preIcon="cgBIBtnMap['export'].buttonIcon"
          :loading="exportLoading"
          @click="onExportExcelOverride"
      >
        <span>{{cgBIBtnMap['export'].buttonName}}</span>
      </a-button>

      <!-- 自定义按钮 -->
      <template v-if="btnShow && cgTopButtonList && cgTopButtonList.length > 0" v-for="(item, index) in cgTopButtonList">
        <a-button
          v-if="item.optType == 'js'"
          :key="'cgbtn' + index"
          @click="cgButtonJsHandler(item.buttonCode)"
          type="primary"
          :preIcon="item.buttonIcon ? 'ant-design:' + item.buttonIcon : ''"
        >
          {{ item.buttonName }}
        </a-button>
        <a-button
          v-else-if="item.optType == 'action'"
          :key="'cgbtn' + index"
          @click="cgButtonActionHandler(item.buttonCode)"
          type="primary"
          :preIcon="item.buttonIcon ? 'ant-design:' + item.buttonIcon : ''"
        >
          {{ item.buttonName }}
        </a-button>
      </template>

      <a-button
          v-show="selectedKeys.length > 0"
          v-if="buttonSwitch.batch_delete && cgBIBtnMap['batch_delete'].enabled"
          :preIcon="cgBIBtnMap['batch_delete'].buttonIcon"
          @click="handleBatchDelete"
      >
        <span>{{cgBIBtnMap['batch_delete'].buttonName}}</span>
      </a-button>
    </template>

    <template #fileSlot="{ text, record, column }">
      <span v-if="!text" style="font-size: 12px; font-style: italic">无文件</span>
      <a-button v-else :ghost="true" type="primary" preIcon="ant-design:download" size="small" @click="downloadRowFile(text, record, column, ID)">
        下载
      </a-button>
    </template>

    <template #imgSlot="{ text }">
      <span v-if="!text" style="font-size: 12px; font-style: italic">无图片</span>
      <img v-else :src="getImgView(text)" alt="图片不存在" class="online-cell-image" @click="viewOnlineCellImage(text)" />
    </template>

    <template #htmlSlot="{ text, column, record }">
      <!-- update-begin--author:liaozhiyang---date:20240517---for：【TV360X-129】增加富文本控件配置href跳转 -->
      <template v-if="column.fieldHref">
        <a v-html="sanitizeRichText(text)" @click="handleClickFieldHref(column.fieldHref, record)"></a>
      </template>
      <div v-else v-html="sanitizeRichText(text)"></div>
      <!-- update-end--author:liaozhiyang---date:20240517---for：【TV360X-129】增加富文本控件配置href跳转 -->
    </template>

    <template #pcaSlot="{ text, column }">
      <div :title="getPcaText(text, column)">{{ getPcaText(text, column) }}</div>
    </template>

    <template #dateSlot="{ text, column }">
      <span>{{ getFormatDate(text, column) }}</span>
    </template>

    <template #action="{ record }">
      <TableAction :actions="getActions(record)" :dropDownActions="getDropDownActions(record)" />
    </template>
  </BasicTable>

  <!-- 表单新增、修改弹框 -->
  <OnlineAutoModal
      @register="registerModal"
      :id="ID"
      :source="ERPSUBTABLE"
      :cgBIBtnMap="cgBIBtnMap"
      :buttonSwitch="buttonSwitch"
      :confirmBtnCfg="getFormConfirmButtonCfg"
      @success="reload"
      @formConfig="handleFormConfig"
  />

  <!-- 详情弹框 -->
  <online-detail-modal :source="ERPSUBTABLE" :id="ID" @register="registerDetailModal" />

  <!-- 导入 -->
  <JImportModal @register="registerImportModal" :url="importUrl()" @ok="reload" online></JImportModal>

  <!-- 跳转Href的动态组件方式 -->
  <a-modal v-bind="hrefComponent.model" v-on="hrefComponent.on">
    <component :is="hrefComponent.is" v-bind="hrefComponent.params" />
  </a-modal>

  <!-- 自定义弹窗 -->
  <online-custom-modal @register="registerCustomModal" @success="reload" />

  <!-- 弹窗给href到另外一张表单用-详情表单 -->
  <online-detail-modal :id="hrefMainTableId" @register="registerOnlineHrefModal" :defaultFullscreen="false" />

  <!-- 弹窗到另外一张表单用-可编辑表单-关联记录的字段可在列表上打开modal编辑数据 -->
  <online-pop-modal ref="onlinePopModalRef" :id="popTableId" @register="registerPopModal" @success="reload" request topTip></online-pop-modal>

  <!-- 流程图查看modal -->
  <BpmGraphicModal @register="registerBpmModal"></BpmGraphicModal>
</template>

<script setup>
  import { ref, computed, provide } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  import OnlineAutoModal from '../default/OnlineAutoModal.vue';
  import OnlineCustomModal from '../default/OnlineCustomModal.vue';
  import OnlineDetailModal from '../default/OnlineDetailModal.vue';
  import { watch } from 'vue';
  import JImportModal from '/@/components/Form/src/jeecg/components/JImportModal.vue';
  import { useOnlineTableContext } from '../../hooks/auto/useOnlineTableContext';
  import { useListButton } from '../../hooks/auto/useListButton';
  import { useTableColumns } from '../../hooks/auto/useTableColumns';
  import { useEnhance } from '../../hooks/auto/useEnhance';
  import { getRefPromise } from '../../hooks/auto/useAutoForm';
  import OnlineQueryForm from '../comp/OnlineQueryForm.vue';
  import { useOnlineListPopEvent } from '../../hooks/auto/useOnlinePopEvent';
  import OnlinePopModal from '../comp/OnlinePopModal.vue';
  import { ERP, ERPSUBTABLE } from '../../util/constant';
  import { cloneDeep } from 'lodash-es';
  import { sanitizeRichText } from '/@/utils/htmlSanitizer';
  const props = defineProps(['data', 'mainTableSelectedRowRcord']);
  const emit = defineEmits(['getSource']);
  const btnShow = ref(false);
  // update-begin--author:liaozhiyang---date:20240611---for：【TV360X-1004】erp风格列设置之后再次刷新展示内容不对
  const tableSetting = {
    cacheKey: `online_erp_subTable_${props.data.currentTableName}`,
  };
  // update-end--author:liaozhiyang---date:20240611---for：【TV360X-1004】erp风格列设置之后再次刷新展示内容不对
  const { createMessage: $message } = useMessage();
  // 这行代码应该在每次进入新的路由都会走，不管该路由有没有被缓存--
  const {
    ID,
    onlineTableContext,
    onlineQueryFormOuter,
    loading,
    reload,
    dataSource,
    pagination,
    handleSpecialConfig,
    getColumnList,
    handleChangeInTable,
    loadData,
    onlineExtConfigJson,
    handleFormConfig,
    registerCustomModal,
    tableReloading,
  } = useOnlineTableContext({ code: props.data.code, themeTemplate: ERP });
  onlineTableContext['isErpSubTable'] = true;

  // 处理增强
  let { initCgEnhanceJs } = useEnhance(onlineTableContext);
  // 处理列表button
  const {
    buttonSwitch,
    cgLinkButtonList,
    cgBIBtnMap,
    getQueryButtonCfg,
    getResetButtonCfg,
    getFormConfirmButtonCfg,
    cgTopButtonList,
    importUrl,
    registerModal,
    handleAdd,
    handleEdit,
    handleBatchDelete,
    registerImportModal,
    onImportExcel,
    onExportExcel,
    cgButtonJsHandler,
    cgButtonActionHandler,
    cgButtonLinkHandler,
    handleSubmitFlow,
    getDropDownActions,
    getActions,
    initButtonList,
    initButtonSwitch,
    registerDetailModal,
    registerBpmModal,
  } = useListButton(onlineTableContext, onlineExtConfigJson);

  const exportLoading = ref(false);

  // 重写导出方法，防止频繁点击
  async function onExportExcelOverride() {
    try {
      exportLoading.value = true;
      await onExportExcel();
    } finally {
      // 防止频繁点击，延迟1.5s关闭loading
      setTimeout(() => (exportLoading.value = false), 1500);
    }
  }

  // 处理 BasicTable 的配置
  const {
    columns,
    actionColumn,
    selectedKeys,
    rowSelection,
    enableScrollBar,
    tableScroll,
    downloadRowFile,
    getImgView,
    getPcaText,
    getFormatDate,
    handleColumnResult,
    hrefComponent,
    viewOnlineCellImage,
    hrefMainTableId,
    registerOnlineHrefModal,
    registerPopModal,
    openPopModal,
    onlinePopModalRef,
    popTableId,
    handleClickFieldHref,
  } = useTableColumns(onlineTableContext, onlineExtConfigJson);

  initAutoList(cloneDeep(props.data));

  const foreignkeyRef = ref(null);
  const foreignKeys = props.data.foreignKeys;
  let $key;
  if (foreignKeys?.length) {
    // 外键只有一个，但是给的是数组形式，只取第一个即可.
    const item = foreignKeys[0];
    const field = item.field;
    $key = item.key;
    onlineTableContext['foreignKeyField'] = field;
  } else {
    onlineTableContext['foreignKeyField'] = null;
    onlineTableContext['foreignKeyValue'] = null;
  }
  provide('foreignkey', foreignkeyRef);
  watch(
    () => props.mainTableSelectedRowRcord,
    (newVal) => {
      // 切换重置状态
      // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-188】erp风格子表分页改成不显示分页，网页报错无响应
      if (pagination.value) pagination.value.current = 1;
      // update-end--author:liaozhiyang---date:20260713---for：【LHZP-188】erp风格子表分页改成不显示分页，网页报错无响应
      selectedKeys.value = [];
      // update-begin--author:liaozhiyang---date:20240523---for：【TV360X-124】erp风格，切换主表数据时，子表查询条件未清空
      onlineQueryFormOuter.value?.clearSearch();
      // update-end--author:liaozhiyang---date:20240523---for：【TV360X-124】erp风格，切换主表数据时，子表查询条件未清空
      // update-begin--author:liusq---date:20260113---for：【LHZP-118】主子表联动兜底：只有有效选中行(id !== undefined)才走加载分支
      if (newVal && newVal.id !== undefined) {
        if (onlineTableContext['foreignKeyField']) {
          const value = newVal[$key];
          onlineTableContext['foreignKeyValue'] = value;
          foreignkeyRef.value = { field: onlineTableContext['foreignKeyField'], value };
        }
        btnShow.value = true;
        loading.value = true;
        loadData().finally(() => {
          loading.value = false;
        });
      } else {
        btnShow.value = false;
        // 显式清空 foreignKeyValue，避免 loadData 命中旧值仍去查询子表
        onlineTableContext['foreignKeyValue'] = null;
        dataSource.value = [];
      }
      // update-end--author:liusq---date:20260113---for：【LHZP-118】主子表联动兜底：只有有效选中行(id !== undefined)才走加载分支
    },
    {
      immediate: true,
    }
  );
  // update-begin--author:liaozhiyang---date:20231128---for：【QQYUN-7260】erp主表编辑时保存子表记录
  watch(
    () => dataSource.value,
    () => {
      emit('getSource', props.data.currentTableName, dataSource.value);
    },
    { immediate: true }
  );
  // update-end--author:liaozhiyang---date:20231128---for：【QQYUN-7260】erp主表编辑时保存子表记录
  /**重新加载online配置*/
  async function initAutoList(data) {
    // 1.列配置信息
    handleTableConfig(data);
    loading.value = false;
    // 2.加载数据
    // await loadData();
    // loading.value = false;
    // 3.执行js增强 setup
    onlineTableContext.execButtonEnhance('setup');
  }

  // 将查询结果转成 table渲染需要的配置
  function handleTableConfig(result) {
    // js增强初始化
    let EnhanceJS = initCgEnhanceJs(result.enhanceJs);
    onlineTableContext['EnhanceJS'] = EnhanceJS;
    // 自定义按钮设置
    initButtonList(result.cgButtonList);
    // 页面按钮显示隐藏状态设置
    initButtonSwitch(result.hideColumns);
    // 列配置
    handleColumnResult(result);
    // 表配置
    handleSpecialConfig(result);
  }
  
  /**
   * 查询控件 事件-执行查询
   * @param data
   */
  function queryWithCondition(data) {
    onlineTableContext['queryParam'] = data;
    reload({mode:'search'});
  }

  /**
   * list页面打开 其他表单弹窗
   * @param params
   */
  function openOnlinePopModal(params) {
    console.log('openOnlinePopModal', params);
    popTableId.value = params.id;
    let data = {
      title: params.describe,
    };
    if (params.record && params.record.id) {
      data['record'] = params.record;
      data['isUpdate'] = true;
    }
    openPopModal(true, data);
  }
  //绑定弹窗事件
  useOnlineListPopEvent(openOnlinePopModal);
  const handleAddRcord = () => {
    if(props.data.relationType ==1 && dataSource.value.length) {
      $message.warning('一对一的表只能新增一条数据');
    } else {
      handleAdd();
    }

  }

  // 导入事件 - 一对一关系校验（前端做 UX 拦截，真正的约束由后端把关）
  const handleImportExcel = () => {
    if (props.data.relationType == 1 && dataSource.value.length) {
      $message.warning(`一对一的表只能导入一条数据！`);
    } else {
      onImportExcel();
    }
  }
</script>

