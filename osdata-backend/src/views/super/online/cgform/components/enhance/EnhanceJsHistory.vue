<template>
  <BasicModal
    @register="registerModal"
    title="JS增强历史记录"
    :width="1200"
    :maskClosable="false"
    :confirmLoading="confirmLoading"
    defaultFullscreen
    @cancel="onCancel"
  >
    <a-spin :spinning="confirmLoading">
      <a-layout>
        <a-layout-sider theme="light">
          <a-list bordered :dataSource="dataList" :class="'enhance-list'">
            <template #header>
              <div>
                <a-divider style="margin: 0">保存时间</a-divider>
              </div>
            </template>
            <template #renderItem="{ item }">
              <a-list-item :class="activeIndex === item.index ? 'bg-blue' : ''">
                <!-- update-begin--author:liaozhiyang---date:20260713---for：【LHZP-235】修复js增强历史版本年份不显示及新增应用 -->
                <div class="history-item">
                  <a class="history-time" @click="fullCode(item)">{{ getFormatDate(item.date) }}</a>
                  <a-button type="link" size="small" class="history-apply" @click.stop="onApply(item)">应用</a-button>
                </div>
                <!-- update-end--author:liaozhiyang---date:20260713---for【LHZP-235】修复js增强历史版本年份不显示及新增应用 -->
              </a-list-item>
            </template>
          </a-list>
        </a-layout-sider>
        <a-layout>
          <a-layout-content :style="{ margin: '8px 8px', padding: '8px', background: '#fff', minHeight: '280px' }">
            <JCodeEditor ref="codeEditorRef" language="javascript" :fullScreen="true" :lineNumbers="false" :language-change="false" />
          </a-layout-content>
        </a-layout>
      </a-layout>
    </a-spin>

    <template #footer>
      <a-button @click="onCancel">关闭</a-button>
    </template>
  </BasicModal>
</template>

<script lang="ts">
  import { formatToDate } from '/@/utils/dateUtil';
  import { ref, nextTick, defineComponent } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { JCodeEditor } from '/@/components/Form';
  import { useEnhanceStore } from '../../store/enhance';

  export default defineComponent({
    name: 'EnhanceJsHistory',
    components: { BasicModal, JCodeEditor },
    emits: ['register', 'apply'],
    setup(_props, { emit }) {
      const enhanceStore = useEnhanceStore();
      const codeEditorRef = ref<InstanceType<typeof JCodeEditor>>();
      const confirmLoading = ref(false);
      const dataList = ref<any[]>([]);
      const jsStr = ref('');
      const activeIndex = ref(0);

      const [registerModal, { closeModal }] = useModalInner(async (data) => {
        show(data.code, data.type);
      });

      function show(code, type) {
        jsStr.value = '';
        dataList.value = [];
        let arr = enhanceStore.getEnhanceJs(code);
        let ls: any[] = [];
        let index = 0;
        for (let item of arr) {
          if (item.type === type) {
            index++;
            ls.push(Object.assign({}, item, { index }));
          }
        }
        if (ls && ls.length > 0) {
          ls.sort((a, b) => {
            return b.date - a.date;
          });
        }
        dataList.value = [...ls];
        nextTick(() => fullCode(ls[0]));
      }

      function onCancel() {
        closeModal();
      }

      function getFormatDate(date) {
        return formatToDate(date, 'YYYY-MM-DD HH:mm:ss');
      }

      function fullCode(item) {
        activeIndex.value = item.index;
        codeEditorRef.value!.setValue(item.str);
      }

      // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-235】修复js增强历史版本年份不显示及新增应用
      function onApply(item) {
        emit('apply', { type: item.type, str: item.str });
        closeModal();
      }
      // update-end--author:liaozhiyang---date:20260713---for：【LHZP-235】修复js增强历史版本年份不显示及新增应用

      return {
        codeEditorRef,
        fullCode,
        onApply,
        registerModal,
        getFormatDate,
        onCancel,
        confirmLoading,
        dataList,
        jsStr,
        activeIndex,
      };
    },
  });
</script>

<style scoped lang="less">
  .enhance-list {
    :deep(.ant-list-items) {
      .ant-list-item {
        position: relative;
        height: 42px;
        line-height: 42px;
        transition: background-color 300ms;
        padding: 0;
      }

      .bg-blue {
        //noinspection LessResolvedByNameOnly
        background-color: @primary-color;

        a {
          color: rgb(255, 255, 255);
        }

        .history-apply {
          &,
          &:hover,
          &:focus {
            color: rgb(255, 255, 255);
          }
        }
      }
    }

    :deep(.ant-list-header) {
      color: #000;
      padding-top: 5px;
      padding-bottom: 5px;
    }
  }

  // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-235】修复js增强历史版本年份不显示及新增应用
  .history-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    padding: 0 16px;

    .history-time {
      flex: 1;
      min-width: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      color: rgba(0, 0, 0, 0.85);
      cursor: pointer;
    }

    .history-apply {
      flex: none;
      padding: 0 4px;
      visibility: hidden;
      display: none;
      transition: opacity 200ms;
    }

    &:hover .history-apply {
      visibility: visible;
      display: block;
    }
  }
  // update-end--author:liaozhiyang---date:20260713---for：【LHZP-235】修复js增强历史版本年份不显示及新增应用
</style>
