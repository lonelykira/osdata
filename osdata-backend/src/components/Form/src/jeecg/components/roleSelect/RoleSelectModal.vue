<template>
  <BasicModal
    @register="register"
    :getContainer="getContainer"
    :canFullscreen="false"
    :title="title"
    width="min(500px, 92vw)"
    destroyOnClose
    @ok="handleOk"
    wrapClassName="j-user-select-modal2"
  >
    <!--update-begin---author:wangshuai---date:20260807---for:【LHZP-141】【表单设计器】筛选 角色选择的弹框效果不太好-->
    <div class="role-modal-body">
      <!-- 搜索框 -->
      <div class="role-search-bar">
        <a-input v-model:value="searchText" class="role-search" allowClear placeholder="搜索角色名称">
          <template #prefix>
            <SearchOutlined class="search-icon" />
          </template>
        </a-input>
        <span class="selected-count">已选 {{ selectedList.length }}</span>
      </div>

      <div class="list-caption">
        <span>角色列表</span>
        <span>共 {{ showDataList.length }} 项</span>
      </div>

      <!-- 角色列表 -->
      <div class="modal-select-list-container">
        <template v-if="showDataList.length > 0">
          <label class="item" :class="{ checked: item.checked }" v-for="item in showDataList" :key="item.id" @click="(e) => onSelect(e, item)">
            <span class="role-mark">{{ item.name?.slice(0, 1) || '角' }}</span>
            <span class="role-info">
              <span class="role-name">{{ item.name }}</span>
              <span v-if="item.code" class="role-code">{{ item.code }}</span>
            </span>
            <a-checkbox v-model:checked="item.checked" class="role-checkbox" />
          </label>
        </template>
        <a-empty v-else description="暂无角色" :image-style="{ height: '60px' }" style="margin-top: 70px" />
      </div>

      <!-- 已选角色 -->
      <div v-if="selectedList.length > 0" class="selected-users-wrapper">
        <div class="selected-title">已选角色</div>
        <div class="selected-users">
          <SelectedUserItem v-for="item in selectedList" :key="item.id" :info="item" @unSelect="unSelect" />
        </div>
      </div>
    </div>
    <!--update-end---author:wangshuai---date:20260807---for:【LHZP-141】【表单设计器】筛选 角色选择的弹框效果不太好-->
  </BasicModal>
</template>

<script lang="ts">
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { SearchOutlined, CloseOutlined } from '@ant-design/icons-vue';
  import SelectedUserItem from '../userSelect/SelectedUserItem.vue';
  import { defHttp } from '/@/utils/http/axios';

  import { computed, ref, toRaw, watch } from 'vue';
  export default {
    name: 'RoleSelectModal',
    components: {
      BasicModal,
      SearchOutlined,
      CloseOutlined,
      SelectedUserItem,
    },
    props: {
      multi: {
        type: Boolean,
        default: true,
      },
      getContainer: {
        type: Function,
        default: null,
      },
      title:{
        type: String,
        default: '',
      },
      type: {
        type: String,
        default: 'sys_role',
      },
      appId: {
        type: String,
        default: '',
      }
    },
    emits: ['selected', 'register'],
    setup(props, { emit }) {

      const searchText = ref('');
      const selectedIdList = computed(() => {
        let arr = selectedList.value;
        if (!arr || arr.length == 0) {
          return [];
        } else {
          return arr.map((k) => k.id);
        }
      });

      watch(()=>props.appId, async (val)=>{
        if(val){
          await loadDataList();
        }
      }, {immediate: true});
      
      
      // 弹窗事件
      const [register] = useModalInner((data) => {
        let list = dataList.value;
        if(!list || list.length ==0 ){
        }else{
          let selectedIdList = data.list || [];
          //update-begin---author:wangshuai---date:20260807---for:【LHZP-141】【表单设计器】筛选 角色选择的弹框效果不太好
          selectedKeys.value = [...selectedIdList];
          //update-end---author:wangshuai---date:20260807---for:【LHZP-141】【表单设计器】筛选 角色选择的弹框效果不太好
          for(let item of list){
            if(selectedIdList.indexOf(item.id)>=0){
              item.checked = true;
            }else{
              item.checked = false;
            }
          }
        }
      });

      // 确定事件
      function handleOk() {
        let arr = toRaw(selectedIdList.value);
        emit('selected', arr, toRaw(selectedList.value));
      }
      
      const dataList = ref<any[]>([]);
      const showDataList = computed(()=>{
        let list = dataList.value;
        if(!list || list.length ==0 ){
          return []
        }
        let text = searchText.value;
        if(!text){
          return list
        }
        return list.filter(item=>item.name.indexOf(text)>=0)
      });

      const selectedKeys = ref<string[]>([]);
      const selectedList = computed(()=>{
        let list = dataList.value;
        if(!list || list.length ==0 ){
          return []
        }
        list = list.filter(item=>item.checked)
        // 根据 selectedKeys 的顺序排序
        let arr: any[] = [];
        for (let key of selectedKeys.value) {
          let item = list.find(item => item.id == key);
          if (item) {
            arr.push(item);
          }
        }
        return arr;
      });

      function unSelect(id) {
        let list = dataList.value;
        if(!list || list.length ==0 ){
          return;
        }
        // 代码逻辑说明: 【issues/8078】角色选择组件点击文字部分会一直选中
        let findItem = list.find((item) => item.id == id);
        findItem.checked = false;
        selectedKeys.value = selectedKeys.value.filter((key) => key != id);
      }
      
      async function loadDataList() {
        let params = {
          pageNo: 1,
          pageSize: 200,
          column: 'createTime',
          order: 'desc'
        };
        const url = '/sys/role/listByTenant';
        const data = await defHttp.get({ url, params }, { isTransformResponse: false });
        if (data.success) {
          const { records } = data.result;
          let arr:any[] = [];
          if(records && records.length>0){
            for(let item of records){
              arr.push({
                id: item.id,
                name: item.name || item.roleName,
                code: item.roleCode,
                selectType: props.type,
                checked: false
              })
            }
          }
          dataList.value = arr;
        } else {
          console.error(data.message);
        }
        console.log('loadDataList', data);
      }

      function onSelect(e, item) {
        prevent(e);
        // 单选模式下，先清除所有选中状态
        if (!props.multi) {
          dataList.value.forEach(dataItem => {
            if (dataItem.id != item.id) {
              dataItem.checked = false;
            }
          });
          // 清空已选择的keys
          selectedKeys.value = [];
        }

        // 切换当前项的选中状态
        item.checked = !item.checked;

        // 更新selectedKeys数组
        if (item.checked) {
          selectedKeys.value.push(item.id);
        } else {
          selectedKeys.value = selectedKeys.value.filter(key => key !== item.id);
        }
      }

      function prevent(e) {
        e.preventDefault();
        e.stopPropagation();
      }

      return {
        register,
        showDataList,
        searchText,
        handleOk,
        selectedList,
        selectedIdList,
        unSelect,
        onSelect
      
      };
    },
  };
</script>
<style scoped lang="less">
  //update-begin---author:wangshuai---date:20260807---for:【LHZP-141】【表单设计器】筛选 角色选择的弹框效果不太好
  .role-modal-body {
    position: relative;
  }

  .role-search-bar {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: linear-gradient(135deg, fade(@primary-color, 8%), fade(@primary-color, 2%));
    border: 1px solid fade(@primary-color, 12%);
    border-radius: 10px;
  }

  .role-search {
    flex: 1;
    height: 36px;
    border-radius: 7px;
    background-color: #fff;
    border-color: #e5e7eb;
    box-shadow: none;
    transition:
      border-color 0.2s,
      box-shadow 0.2s;

    &:hover {
      border-color: fade(@primary-color, 55%);
    }

    &:focus,
    &.ant-input-affix-wrapper-focused {
      background-color: #fff;
      border-color: @primary-color;
      box-shadow: 0 0 0 2px fade(@primary-color, 12%);
    }

    :deep(.ant-input) {
      background-color: transparent;
    }
  }

  .search-icon {
    color: #9ca3af;
  }

  .selected-count {
    flex: none;
    min-width: 58px;
    color: @primary-color;
    font-size: 12px;
    font-weight: 500;
    text-align: right;
    white-space: nowrap;
  }

  .list-caption {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 14px 2px 8px;
    color: #1f2937;
    font-size: 13px;
    font-weight: 500;

    span:last-child {
      color: #9ca3af;
      font-size: 12px;
      font-weight: 400;
    }
  }

  .modal-select-list-container {
    height: 360px;
    overflow-y: auto;
    border: 1px solid #e8eaed;
    border-radius: 10px;
    padding: 6px;
    scrollbar-width: thin;
    scrollbar-color: #d8dce3 transparent;

    .item {
      position: relative;
      display: flex;
      align-items: center;
      min-height: 54px;
      padding: 7px 12px 7px 10px;
      border: 1px solid transparent;
      border-radius: 8px;
      cursor: pointer;
      transition:
        background-color 0.2s,
        border-color 0.2s;

      & + .item {
        margin-top: 4px;
      }

      &:hover {
        background-color: #f7f9fc;
        border-color: #edf0f4;
      }

      &.checked {
        background-color: fade(@primary-color, 7%);
        border-color: fade(@primary-color, 22%);

        &::before {
          position: absolute;
          top: 12px;
          bottom: 12px;
          left: -1px;
          width: 3px;
          background: @primary-color;
          border-radius: 0 3px 3px 0;
          content: '';
        }

        .role-mark {
          color: #fff;
          background: @primary-color;
        }
      }
    }
  }

  .role-mark {
    display: inline-flex;
    flex: none;
    align-items: center;
    justify-content: center;
    width: 34px;
    height: 34px;
    margin-right: 11px;
    color: @primary-color;
    font-size: 14px;
    font-weight: 600;
    background: fade(@primary-color, 10%);
    border-radius: 9px;
    transition:
      color 0.2s,
      background-color 0.2s;
  }

  .role-info {
    display: flex;
    flex: 1;
    min-width: 0;
    flex-direction: column;
  }

  .role-name,
  .role-code {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .role-name {
    color: #20242a;
    font-size: 14px;
    line-height: 20px;
  }

  .role-code {
    margin-top: 1px;
    color: #9aa1ab;
    font-size: 11px;
    line-height: 16px;
  }

  .role-checkbox {
    flex: none;
    margin-left: 12px;
  }

  .selected-users-wrapper {
    margin-top: 12px;
    padding: 10px 12px 8px;
    background: #f8f9fb;
    border: 1px solid #eef0f3;
    border-radius: 8px;

    .selected-title {
      margin-bottom: 6px;
      color: #6b7280;
      font-size: 12px;
    }

    .selected-users {
      display: flex;
      flex-wrap: wrap;
      flex-direction: row;
      align-items: center;
      gap: 4px;
      padding-top: 0;

      // 弹窗内压缩已选标签行高（默认 30px 偏高）
      :deep(.user-selected-item) {
        height: 26px;
        line-height: 26px;
        margin-bottom: 2px;
      }
    }
  }
  //update-end---author:wangshuai---date:20260807---for:【LHZP-141】【表单设计器】筛选 角色选择的弹框效果不太好
</style>

<style lang="less">
  .j-user-select-modal2 {
    .scroll-container {
      padding-bottom: 0 !important;

      // 去掉内容底部多余的 18px 外边距，弹窗底部不再空一大块
      .scrollbar__wrap {
        margin-bottom: 0 !important;
      }
    }
  }
</style>
