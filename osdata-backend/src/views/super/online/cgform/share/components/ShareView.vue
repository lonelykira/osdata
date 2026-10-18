<template>
  <div v-if="cgformRef != null" :class="[prefixCls]">
    <template v-if="showResult!=='none'">
      <a-result
          status="success"
          :title="`${isUpdate ? '修改': '新增'}成功`"
          subTitle="请选择下一步操作"
      >
        <template #extra>
          <a-space>
            <a-button type="primary" @click="onAddContinue">
              <span>继续{{ isUpdate ? '修改' : '新增' }}</span>
            </a-button>
            <a-space-compact block>
              <a-button @click="onOtherAction({key:'view'})">
                <span>查看</span>
                <span v-if="!isUpdate">新</span>
                <span>数据</span>
              </a-button>
              <a-dropdown :trigger="['click', 'hover']">
                <a-button preIcon="ant-design:down">
                </a-button>
                <template #overlay>
                  <a-menu @click="onOtherAction">
                    <a-menu-item v-if="isUpdate" key="add">添加数据</a-menu-item>
                    <a-menu-item v-if="!isUpdate" key="edit">编辑新数据</a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space-compact>
            <a-button @click="onClose">关闭</a-button>
          </a-space>
        </template>
      </a-result>
    </template>
    <!-- 单表页面 -->
    <!--    <template v-else-if="isSingle">-->
    <!--    </template>-->
    <!-- 主子表页面 -->
    <template v-else>
      <SingleView
          :ID="ID"
          :formName="formName"
          :isUpdate="isUpdate"
          :isDetail="isDetail"
          :isMobile="getIsMobile"
          :buttonSwitch="buttonSwitch"
          :cgBIBtnMap="cgBIBtnMap"
          :confirmBtnCfg="confirmBtnCfg"

          @ok="onOk"
          @goEdit="goEdit"
      />

      <div v-show="false">
        <OnlineAutoList ref="listRef"/>
      </div>

    </template>
  </div>
</template>

<script setup lang="ts">
import {computed, ref} from 'vue';
import {useShareStore} from "../store/shareStore";
import OnlineAutoList from "../../auto/default/OnlineAutoList.vue";
import {useDesign} from "@/hooks/web/useDesign";

import SingleView from "./SingleView.vue";
import {useGo} from "@/hooks/web/usePage";
import {useAppInject} from "@/hooks/web/useAppInject";

const {prefixCls} = useDesign('online-share-view-box');

const props = defineProps({
  isUpdate: Boolean,
  isDetail: Boolean,
})

const {getIsMobile} = useAppInject();

const shareStore = useShareStore()

const cgformRef = computed(() => shareStore.getCgformRecord);
const ID = computed(() => cgformRef.value?.id);
const formName = computed(() => cgformRef.value?.tableTxt);
// 是否是单表
// const isSingle = computed(() => cgformRef.value?.tableType == 1);

const listRef = ref()
const buttonSwitch = computed(() => listRef.value?.buttonSwitch);
const cgBIBtnMap = computed(() => listRef.value?.cgBIBtnMap);
const confirmBtnCfg = computed(() => listRef.value?.getFormConfirmButtonCfg);

const showResult = ref<'none' | 'ok'>('none');
const lastActionDataId = ref<string>();

function onOk(dataId: string) {
  showResult.value = 'ok';
  lastActionDataId.value = dataId;
  if (!props.isUpdate) {
    shareStore.setDataRecord(null);
  }
}

// 继续添加
function onAddContinue() {
  // window.location.reload();
  showResult.value = 'none';
  lastActionDataId.value = '';
}

const go = useGo();

function onOtherAction({key}) {
  const dataId = lastActionDataId.value!;
  if (key === 'add') {
    goDataAction('add', '');
  } else if (key === 'view') {
    goDetail(dataId);
  } else if (key === 'edit') {
    goEdit(dataId);
  }
  lastActionDataId.value = '';
  showResult.value = 'none';
}

function goEdit(dataId: string) {
  goDataAction('u', dataId);
}

function goDetail(dataId: string) {
  goDataAction('d', dataId);
}

function goDataAction(action, dataId) {
  if (action === 'add') {
    go(`/online/cgform/share/${ID.value}/${action}`);
    return;
  }
  if (!dataId) {
    dataId = shareStore.getDataRecord?.id;
  }

  go(`/online/cgform/share/${ID.value}/${action}/${dataId}`);
}

function onClose() {
  window.close()
  setTimeout(() => alert('当前页面无法通过按钮关闭，请手动关闭'), 1000)
}

</script>

<style scoped lang="less">

</style>