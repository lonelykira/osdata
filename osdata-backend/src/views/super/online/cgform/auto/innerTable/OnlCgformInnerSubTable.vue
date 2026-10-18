<template>
  <!-- 列表 -->
  <BasicTable
    v-if="!tableReloading && (loading || columns.length)"
    ref="onlineTable"
    rowKey="jeecg_row_key"
    :canResize="true"
    :bordered="true"
    :showIndexColumn="false"
    :loading="loading"
    :columns="columns"
    :dataSource="dataSource"
    :pagination="false"
    :showActionColumn="false"
    :showTableSetting="false"
    :clickToRowSelect="false"
    :scroll="tableScroll"
    @table-redo="reload"
    :class="{ 'j-table-force-nowrap': enableScrollBar }"
    @change="handleChangeInTable"
  >
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
  </BasicTable>

  <!-- update-begin--author:liaozhiyang---date:20260810---for:子表字段全部隐藏时展示友好的空状态 -->
  <div v-else-if="!tableReloading" class="inner-sub-table-empty">
    <a-empty :image="Empty.PRESENTED_IMAGE_SIMPLE" description="暂无可展示内容" />
  </div>
  <!-- update-end--author:liaozhiyang---date:20260810---for:子表字段全部隐藏时展示友好的空状态 -->

  <!-- 详情弹框 -->
  <online-detail-modal :id="ID" @register="registerDetailModal" :source="INNER_TABLE"/>

  <!-- 导入 -->
  <JImportModal @register="registerImportModal" :url="importUrl()" @ok="reload" online></JImportModal>

  <!-- 跳转Href的动态组件方式 -->
  <a-modal v-bind="hrefComponent.model" v-on="hrefComponent.on">
    <component :is="hrefComponent.is" v-bind="hrefComponent.params" />
  </a-modal>

  <!-- 自定义弹窗 -->
  <online-custom-modal @register="registerCustomModal" @success="reload" />

  <!-- 弹窗给href到另外一张表单用-详情表单 -->
  <online-detail-modal :id="hrefMainTableId" @register="registerOnlineHrefModal" :defaultFullscreen="false" :source="INNER_TABLE" />

  <!-- 弹窗到另外一张表单用-可编辑表单-关联记录的字段可在列表上打开modal编辑数据 -->
  <online-pop-modal ref="onlinePopModalRef" :id="popTableId" @register="registerPopModal" @success="reload" request topTip></online-pop-modal>

  <!-- 流程图查看modal -->
  <BpmGraphicModal @register="registerBpmModal"></BpmGraphicModal>
</template>

<script setup name="OnlCgformInnerSubTable">
  import { ref, watch } from 'vue';
  import { Empty } from 'ant-design-vue';
  import { BasicTable } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  import OnlineCustomModal from '../default/OnlineCustomModal.vue';
  import OnlineDetailModal from '../default/OnlineDetailModal.vue';
  import JImportModal from '/@/components/Form/src/jeecg/components/JImportModal.vue';
  import { useOnlineTableContext } from '../../hooks/auto/useOnlineTableContext';
  import { useListButton } from '../../hooks/auto/useListButton';
  import { useTableColumns } from '../../hooks/auto/useTableColumns';
  import { useEnhance } from '../../hooks/auto/useEnhance';
  import { useOnlineListPopEvent } from '../../hooks/auto/useOnlinePopEvent';
  import OnlinePopModal from '../comp/OnlinePopModal.vue';
  import { INNER_TABLE } from '../../util/constant';
  import { ACTION_COLUMN_FLAG } from '/@/components/Table/src/const';
  import { sanitizeRichText } from '/@/utils/htmlSanitizer';

  /**
    1.内嵌子表单没有分页
    2.内嵌子表单没有查询区域
    3.内嵌子表单没有导出导入等按钮
    4.查询始终都是主表数据id（外键不起作用）
    5.内嵌子表单没有新增和编辑，没有操作列
  */
  const props = defineProps(['subTableId', 'subTableName', 'mTableSelectedRcordId', 'subTableSuperQuery']);
  const { createMessage: $message } = useMessage();
  // 这行代码应该在每次进入新的路由都会走，不管该路由有没有被缓存--
  const {
    ID,
    onlineTableContext,
    loading,
    reload,
    dataSource,
    handleSpecialConfig,
    getColumnList,
    handleChangeInTable,
    loadData,
    onlineExtConfigJson,
    registerCustomModal,
    tableReloading,
  } = useOnlineTableContext({ code: props.subTableId });
  onlineTableContext['isInnerSubTable'] = true;
  onlineTableContext['innerSubTableName'] = props.subTableName;
  onlineTableContext['innerSubTableId'] = ID.value;
  onlineTableContext['mTableSelectedRcordId'] = props.mTableSelectedRcordId;

  // 判断 若ID不存在就终止后续逻辑
  if (!ID.value) {
    $message.warning('地址错误, 配置ID不存在!');
  }
  // 处理增强
  let { initCgEnhanceJs } = useEnhance(onlineTableContext);
  // 处理列表button
  const {
    importUrl,
    registerImportModal,
    initButtonList,
    initButtonSwitch,
    registerDetailModal,
    registerBpmModal,
  } = useListButton(onlineTableContext, onlineExtConfigJson);

  
  // 处理 BasicTable 的配置
  const {
    columns,
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
  // 监听表单配置ID 与 子表高级查询条件
  // 首次进入拉取列配置 + 数据，后续过滤条件变化只重新 loadData
  let subTableInited = false;
  watch(
    [ID, () => props.subTableSuperQuery],
    () => {
      console.log('watched id or subTableSuperQuery is change...');
      // 把主表拆分出来的子表高级查询条件合并到子表 context
      if (props.subTableSuperQuery) {
        onlineTableContext['superQuery'] = props.subTableSuperQuery;
      }
      if (!subTableInited) {
        subTableInited = true;
        initAutoList();
      } else {
        loadData();
      }
    },
    { immediate: true, deep: true }
  );
  /**重新加载online配置*/
  async function initAutoList() {
    loading.value = true;
    // 1.列配置信息
    let columnResult = await getColumnList(INNER_TABLE);
    handleTableConfig(columnResult);
    // update-begin--author:liaozhiyang---date:20240514---for：【QQYUN-9340】内嵌子表数据都查出来了
    if (columnResult.foreignKeys?.length) {
      onlineTableContext['innerSubTableFk'] = columnResult.foreignKeys[0].field;
    }
    // update-end--author:liaozhiyang---date:20240514---for：【QQYUN-9340】内嵌子表数据都查出来了
    // 2.加载数据
    await loadData();
    loading.value = false;
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
</script>

<style lang="less">
  /** [表格主题样式一] 表格强制列不换行 */
  .j-table-force-nowrap {
    td,
    th {
      white-space: nowrap;
    }

    .ant-table-selection-column {
      padding: 12px 22px !important;
    }

    /** 列自适应，弊端会导致列宽失效 */
    &.ant-table-wrapper .ant-table-content {
      overflow-x: auto;
    }
  }
  .online-cell-image {
    height: 25px !important;
    margin: 0 auto;
    max-width: 80px;
    font-size: 12px;
    font-style: italic;
    cursor: pointer;
  }
  // update-begin--author:liaozhiyang---date:20260810---for:子表字段全部隐藏时展示友好的空状态
  .inner-sub-table-empty {
    display: flex;
    min-height: 100px;
    align-items: center;
    justify-content: center;
    border: 1px dashed #d9d9d9;
    border-radius: 2px;
    background: #fafafa;
  }
  // update-end--author:liaozhiyang---date:20260810---for:子表字段全部隐藏时展示友好的空状态
</style>
