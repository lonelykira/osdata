<template>
    <BasicModal @register="registerModal" :width="popModalFixedWidth" :dialogStyle="{top: '70px'}" :bodyStyle="popBodyStyle" :title="modalTitle" wrapClassName="jeecg-online-pop-list-modal">
        <template #footer>
            <div style="display: inline-block;width: calc(100% - 140px);text-align: left;">
                <a-button v-if="addAuth" style="border-radius: 50px" type="primary" @click="handleAdd"><PlusOutlined/>新增记录</a-button>
            </div>
            <a-button key="back" @click="handleCancel">关闭</a-button>
            <a-button :disabled="submitDisabled" key="submit" type="primary" @click="handleSubmit" :loading="submitLoading">确定</a-button>
        </template>
        
        
        <BasicTable @register="registerTable" :rowSelection="rowSelection">
            
            <!-- update-begin-author:taoyan date:2023-7-11 for: issues/4992 online表单开发 字段控件类型是关联记录 新增的时候选择列表可以添加查询么 -->
            <template #tableTitle>
                <a-input-search v-model:value="searchText" @search="onSearch"  placeholder="请输入关键词，按回车搜索"  style="width: 240px" />
            </template>
            <!-- update-end-author:taoyan date:2023-7-11 for: issues/4992 online表单开发 字段控件类型是关联记录 新增的时候选择列表可以添加查询么 -->
            
            <!--操作栏-->
            <template #action="{ record }">
                <TableAction :actions="getTableAction(record)">
                </TableAction>
            </template>

            <template #fileSlot="{ text }">
                <span v-if="!text" style="font-size: 12px; font-style: italic">无文件</span>
                <a-button v-else :ghost="true" type="primary" preIcon="ant-design:download" size="small" @click="downloadRowFile(text)"> 下载 </a-button>
            </template>

            <template #imgSlot="{ text }">
                <span v-if="!text" style="font-size: 12px; font-style: italic">无图片</span>
                <img v-else :src="getImgView(text)" alt="图片不存在" class="online-cell-image" @click="viewOnlineCellImage(text)" />
            </template>

            <template #htmlSlot="{ text }">
                <div v-html="sanitizeRichText(text)"></div>
            </template>

            <template #pcaSlot="{ text, column }">
                <div :title="getPcaText(text, column)">{{ getPcaText(text, column) }}</div>
            </template>

            <template #dateSlot="{ text, column }">
                <span>{{ getFormatDate(text, column) }}</span>
            </template>
            
        </BasicTable>
    </BasicModal>

    <!-- 弹窗到另外一张表单用-可编辑表单 -->
    <online-pop-modal :id="id" @register="registerPopModal" @success="handleDataSave" topTip></online-pop-modal>
</template>

<script lang="ts">
  import { defineComponent, watch, ref, toRaw, computed, inject, type Ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { defHttp } from '/@/utils/http/axios';
  import { useTableColumns } from '../../hooks/auto/useTableColumns';
  import { PlusOutlined } from '@ant-design/icons-vue';
  import { useModal } from '/@/components/Modal';
  import OnlinePopModal from './OnlinePopModal.vue';
  import { useFixedHeightModal } from '../../hooks/auto/useAutoModal';
  import { sanitizeRichText } from '/@/utils/htmlSanitizer';
  
  export default defineComponent({
    name: 'OnlinePopListModal',
    props: {
      /**可以是表名 可以是ID*/
      id: {
        type: String,
        default: '',
      },
      multi:{
        type: Boolean,
        default: false
      },
      addAuth:{
        type: Boolean,
        default: true
      }
    },
    components: {
      BasicModal,
      BasicTable,
      TableAction,
      PlusOutlined,
      OnlinePopModal
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const { createMessage: $message } = useMessage();
      // 弹窗仍属于当前 Online 表单上下文，携带源表单编码后才能走受控的关联记录只读授权。
      const sourceFormCode = inject<Ref<string | null>>('tableId');
      // 弹窗高度控制
      const { popModalFixedWidth, resetBodyStyle, popBodyStyle } = useFixedHeightModal();
      const searchText = ref('');
      const modalWidth = ref(800);
      //useModalInner
      const [registerModal, {closeModal}] = useModalInner((data) => {
        searchText.value = '';
        // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-43】修复关联记录可以添加重复数据
        selectedRowKeys.value = data.selectedRowKeys;
        selectedRows.value = data.selectedRows;
        // update-end--author:liaozhiyang---date:20240517---for：【TV360X-43】修复关联记录可以添加重复数据
        setPagination({current:1})
        reload();
        resetBodyStyle();
      });

      // 用于 online表单中 弹出别的表单
      const [registerPopModal, { openModal: openPopModal }] = useModal();
      
      function handleCancel() {
        closeModal();
      }
      const submitDisabled = computed(()=>{
        const arr = selectedRowKeys.value;
        if(arr && arr.length>0){
          return false;
        }
        return true;
      })
      const submitLoading = ref(false);
      function handleSubmit(){
        submitLoading.value = true;
        let arr = toRaw(selectedRows.value);
        if(arr && arr.length>0){
          emit('success', arr)
          closeModal();
        }
        setTimeout(()=>{
          submitLoading.value = false
        }, 200);
      }
      
      //---------------------列表------------------------
      /**
       * 只为 Online 表单内的关联记录请求补充源表单编码；无上下文时完全保留原参数。
       */
      function getLinkTableParams(params = {}) {
        return sourceFormCode?.value ? { ...params, linkTableSourceCode: sourceFormCode.value } : params;
      }

      function queryTableData(params){
        const url = '/online/cgform/api/getData/'+props.id;
        return defHttp.get({ url, params: getLinkTableParams(params) });
      }
      
      function list(params){
        params['column'] = 'id';
        return new Promise(async (resolve, _reject) => {
          const aa = await queryTableData(params)
          resolve(aa);
        })
      }

      const onlineTableContext = {
        isPopList: true,
        reloadTable(){
          console.log('reloadTable')
        },
        isTree(){
          return false;
        }
      };
      const extConfigJson = ref<any>({});

      // 处理 BasicTable 的配置
      const {
        columns,
        downloadRowFile,
        getImgView,
        getPcaText,
        getFormatDate,
        handleColumnResult,
        hrefComponent,
        viewOnlineCellImage,
      } = useTableColumns(onlineTableContext, extConfigJson);


      /**
       * 查询table列信息 及其他配置
       */
      function getColumnList() {
        const url = '/online/cgform/api/getColumns/'+props.id;
        return new Promise((resolve, reject) => {
          defHttp.get({url, params: getLinkTableParams()}, { isTransformResponse: false }).then((res) => {
            if (res.success) {
              resolve(res.result);
            } else {
              $message.warning(res.message);
              reject();
            }
          })
        });
      }

      const modalTitle = ref('')
      watch(()=>props.id, async ()=>{
        let columnResult:any = await getColumnList();
        handleColumnResult(columnResult);
        modalTitle.value = columnResult.description;
      }, {immediate: true})

      const { tableContext } = useListPage({
        designScope: 'process-design',
        pagination: true,
        tableProps: {
          title: '',
          api: list,
          clickToRowSelect: true,
          columns: columns,
          showTableSetting: false,
          immediate:false,
          //showIndexColumn: true,
          canResize: false,
          showActionColumn: false,
          actionColumn: {
            dataIndex: 'action',
            slots: { customRender: 'action' },
          },
          useSearchForm: false,
          beforeFetch: (params) => {
            return addQueryParams(params);
          },
        },
      });
      const [registerTable, { reload, setPagination }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;
      watch(()=>props.multi, (val)=>{
        if(val==true){
          rowSelection.type = 'checkbox'
        }else{
          rowSelection.type = 'radio'
        }
      }, {immediate: true});

      /**
       * 操作栏
       */
      function getTableAction(record) {
        return [
          {
            label: '编辑',
            onClick: handleUpdate.bind(null, record),
          }
        ];
      }
  
      function handleUpdate(record){
        console.log('handleUpdate', record)
      }
     
      function onSearch(){
        reload();
      }
      const eqConditonTypes = ['int', 'double', 'long', 'Date', 'Datetime', 'BigDecimal']
      function addQueryParams(params){
        let text = searchText.value;
        if(!text){
          params['superQueryMatchType'] = 'or';
          params['superQueryParams'] = ''
          return params;
        }
        let arr = columns.value;
        let conditions:any[] = []
        if(arr && arr.length>0){
          for(let item of arr){
            if(item.dbType){
              if(item.dbType == 'string'){
                conditions.push({field: item.dataIndex,type:item.dbType.toLowerCase(), rule: 'like', val: text})
              }else if(item.dbType == 'Date'){
                if(text.length=='2020-10-10'.length){
                  conditions.push({field: item.dataIndex,type:item.dbType.toLowerCase(), rule: 'eq', val: text})
                }
              }else if(item.dbType == 'Datetime'){
                if(text.length=='2020-10-10 10:10:10'.length){
                  conditions.push({field: item.dataIndex,type:item.dbType.toLowerCase(), rule: 'eq', val: text})
                }
              }else if(eqConditonTypes.indexOf(item.dbType)){
                conditions.push({field: item.dataIndex, type:item.dbType.toLowerCase(), rule: 'eq', val: text})
              }else{
                //text blob不做处理
              }
            }
          }
        }
        params['superQueryMatchType'] = 'or';
        params['superQueryParams'] = encodeURI(JSON.stringify(conditions));
        return params;
      }
  
      function handleAdd(){
        openPopModal(true, {})
      }
      
      // modal数据新增完成 直接关闭list，将新增的数据带回表单
      function handleDataSave(data){
        console.log('handleDateSave' ,data)
        // update-begin--author:liaozhiyang---date:20250429---for：【issues/8163】关联记录新增丢失
        let arr = [data, ...selectedRows.value];
        // update-end--author:liaozhiyang---date:20250429---for：【issues/8163】关联记录新增丢失
        emit('success', arr);
        closeModal();
        //reload();
      }
      
      return {
        registerModal,
        modalWidth,
        handleCancel,
        submitDisabled,
        submitLoading,
        handleSubmit,

        registerTable,
        getTableAction,
        searchText,
        onSearch,

        downloadRowFile,
        getImgView,
        getPcaText,
        getFormatDate,
        sanitizeRichText,
        hrefComponent,
        viewOnlineCellImage,
        rowSelection,
        modalTitle,

        registerPopModal,
        handleAdd,
        reload,

        popModalFixedWidth,
        popBodyStyle,
        handleDataSave
      };
    },
  });
</script>

<style lang="less" scoped>
.online-cell-image {
  max-height: 30px;
  max-width: 50px;
  object-fit: contain;
  cursor: pointer;
}
</style>
