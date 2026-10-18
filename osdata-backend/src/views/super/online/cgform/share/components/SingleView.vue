<template>
  <div ref="boxRef" :class="['view-box', {'is-mobile': isMobile}]" :style="boxStyle">
    <template v-if="!loading">
      <!-- 详情弹框 -->
      <OnlineDetailModal v-if="isDetail" v-bind="getModalProps" :isShare="true">
        <template #footerBtn>
          <a-button @click="emit('goEdit')">编辑该数据</a-button>
        </template>
      </OnlineDetailModal>
      <!-- 编辑弹框 -->
      <OnlineAutoModal v-else v-bind="getModalProps" :isShare="true"/>
    </template>
  </div>
</template>

<script setup lang="ts">
import type {ModalMethods} from "@/components/Modal";
import {computed, nextTick, ref} from "vue";
import {getRefPromise} from "@/utils";
import {useShareStore} from "../store/shareStore";
import OnlineAutoModal from "../../auto/default/OnlineAutoModal.vue";
import {useOnlineTableContext} from "../../hooks/auto/useOnlineTableContext";
import {useListButton} from "../../hooks/auto/useListButton";
import {useEnhance} from "../../hooks/auto/useEnhance";
import {useTableColumns} from "../../hooks/auto/useTableColumns";
import OnlineDetailModal from "../../auto/default/OnlineDetailModal.vue";

const props = defineProps({
  ID: String,
  formName: String,
  buttonSwitch: Object,
  cgBIBtnMap: Object,
  confirmBtnCfg: Object,
  isUpdate: Boolean,
  isDetail: Boolean,
  isMobile: Boolean,
})
const emit = defineEmits(['ok', 'goEdit'])

const shareStore = useShareStore()

const boxRef = ref()

// box样式
const boxStyle = computed(() => {
  let paddingTop = 20;
  let paddingLeft = 100;
  // 兼容窄屏幕、移动端样式，不动态刷新
  if (window.innerWidth < 1200) {
    paddingTop = 0;
    paddingLeft = 0;
  }
  return {
    padding: `${paddingTop}px ${paddingLeft}px`,
  };
});

const getModalProps = computed(() => {
  // 业务属性
  const titlePrefix = props.isDetail ? '详情' : (props.isUpdate ? '修改' : '新增');
  const modalProps: Recordable = {
    id: props.ID,
    title: titlePrefix + ` [${props.formName}]`,
    onRegister: registerModal,
  }
  if (!props.isDetail) {
    // 新增、编辑参数
    Object.assign(modalProps, {
      cgBIBtnMap: props.cgBIBtnMap,
      buttonSwitch: props.buttonSwitch,
      confirmBtnCfg: props.confirmBtnCfg,
      onSuccess: onSuccess,
      onFormConfig: handleFormConfig,
    });
  }
  // 自定义属性
  Object.assign(modalProps, {
    cancelBtnCfg: {enabled: false},

    width: 1200,
    getContainer: () => boxRef.value,
    mask: false,
    maskClosable: false,
    bodyStyle: {height: bodyHeight.value},
    draggable: false,
    canFullscreen: false,
    closeFunc: closeFunc,
  })

  return modalProps;
});

// 动态计算高度
const bodyHeight = computed(() => {
  let offset = 140;
  if (props.isMobile) {
    offset = 110;
  }
  return (boxRef.value?.offsetHeight ?? window.innerHeight) - offset + 'px';
});

const {
  loading,
  handleFormConfig,
  onlineTableContext,
  onlineExtConfigJson,
} = useOnlineTableContext();

// 处理列表button
const {
  registerModal: registerModalOrigin,

  handleAdd,
  handleEdit,
} = useListButton(onlineTableContext, onlineExtConfigJson);

async function registerModal(modalMethod: ModalMethods, uuid: string) {
  registerModalOrigin(modalMethod, uuid);
  await nextTick()
  if (props.isUpdate) {
    const dataRecord = await getDataRecordPromise();
    handleEdit(dataRecord);
  } else {
    handleAdd({});
  }
}

async function getDataRecordPromise() {
  return await getRefPromise(computed(() => shareStore.getDataRecord));
}

// 处理 BasicTable 的配置
const {} = useTableColumns(onlineTableContext, onlineExtConfigJson);
// 处理增强
const {} = useEnhance(onlineTableContext);

function onSuccess(record: Recordable) {
  let dataId: string;
  if (props.isUpdate) {
    dataId = shareStore.getDataRecord!.id;
  } else {
    dataId = record.flow_submit_id
  }
  if (!dataId) {
    return;
  }
  emit('ok', dataId)
}

function closeFunc() {
  alert('不允许关闭')
  return false
}

</script>

<style lang="less">
// noinspection LessUnresolvedVariable
@prefix-cls: ~'@{namespace}-auto-desform-data-view';

.view-box {
  height: calc(100vh - 50px);
  background-color: #f0f2f5;

  .jeecg-online-modal, .jeecg-online-detail-modal {
    top: 80px !important;

    > .ant-modal {
      top: 0 !important;
      // 取消动画
      animation: none !important;

      .jeecg-basic-title {
        // 取消拖拽手势
        cursor: default !important;
      }

      .ant-modal-content {
        // 无阴影
        box-shadow: none !important;

        // 隐藏右上角 ×
        > .ant-modal-close {
          display: none;
        }
      }
    }
  }

  &.is-mobile {
    height: 100vh;

    .jeecg-online-modal, .jeecg-online-detail-modal {
      top: 0 !important;
    }
  }

}

</style>
