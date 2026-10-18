<template>
  <div class="tabsSelectUser" v-bind="$attrs">
    <JSelectBiz @handleOpen="openSelect" allowClear :placeholder="placeholder" :loading="loadingEcho" v-bind="attrs" @change="handleSelectChange"></JSelectBiz>
  </div>
  <teleport to="body">
    <select-user-modal @register="registerModal" :rowKey="rowKey" @selected="onSelected" />
  </teleport>
</template>

<script lang="ts" setup>
  /**
   * 选择用户组件 - 多标签切换选择用户
   */
  import { provide, reactive, ref, watch } from 'vue';
  import SelectUserModal from './SelectUserModal.vue';
  import { useModal } from '/src/components/Modal';
  import JSelectBiz from '@/components/Form/src/jeecg/components/base/JSelectBiz.vue';
  import { propTypes } from '@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { isArray, isString, isObject } from '/@/utils/is';
  import { getTableList as getTableListOrigin } from '/@/api/common/api';

  defineOptions({ name: 'JTabsSelectUser' });
  //定义props
  const props = defineProps({
    placeholder:{
      type: String,
      default: '选择人员',
    },
    value: propTypes.oneOfType([propTypes.string, propTypes.array]),
    rowKey: {
      type: String,
      default: 'username',
    },
    labelKey: {
      type: String,
      default: 'realname',
    },
  });
  //定义emit
  const emit = defineEmits(['options-change', 'change', 'update:value']);
  const attrs: any = useAttrs();
  const [registerModal, { openModal }] = useModal();
  //下拉框选项值
  const selectOptions: any = ref<SelectValue>([]);
  //下拉框选中值
  let selectValues: any = reactive<object>({
    value: [],
    change: false,
  });
  // 是否正在加载回显数据
  const loadingEcho = ref<boolean>(false);
  //下发 selectOptions,xxxBiz组件接收
  provide('selectOptions', selectOptions);
  //下发 selectValues,xxxBiz组件接收
  provide('selectValues', selectValues);
  //下发 loadingEcho,xxxBiz组件接收
  provide('loadingEcho', loadingEcho);

  function openSelect() {
    let arr = getModalData();
    openModal(true, {
      selected: arr,
    });
  }

  function getModalData() {
    //找rows
    let arr = selectOptions.value;
    //找username
    let arr2 = selectValues.value;
    let dataArray = arr.filter((item) => arr2.indexOf(item[props.rowKey]) >= 0);
    return dataArray;
  }

  function onSelected(data) {
    selectOptions.value = data.map((item) => {
      return {
        ...item,
        label: item[props.labelKey],
        value: item[props.rowKey],
      };
    });
    selectValues.value = data.map((item) => item[props.rowKey]);
    // 更新value
    emit('update:value', selectValues.value);
    // 触发change事件（不转是因为basicForm提交时会自动将字符串转化为数组）
    emit('change', selectValues.value,selectOptions.value);
  }

  const handleSelectChange = (data) => {
    selectOptions.value = selectOptions.value.filter((item) => data.includes(item[props.rowKey]));
    setValue(selectOptions.value);
  };
  // 设置下拉框的值
  const setValue = (data) => {
    selectOptions.value = data.map((item) => {
      return {
        ...item,
        label: item[props.labelKey],
        value: item[props.rowKey],
      };
    });
    selectValues.value = data.map((item) => item[props.rowKey]);
    // 更新value
    emit('update:value', selectValues.value);
    // 触发change事件（不转是因为basicForm提交时会自动将字符串转化为数组）
    emit('change', selectValues.value,selectOptions.value);
  };
  // 翻译
  const transform = () => {
    let value = props.value;
    let len;
    if (isArray(value) || isString(value)) {
      if (isArray(value)) {
        len = value.length;
        value = value.join(',');
      } else {
        len = value.split(',').length;
      }
      value = value.trim();
      if (value) {
        // 如果value的值在selectedUser中存在，则不请求翻译
        let isNotRequestTransform = false;
        isNotRequestTransform = value.split(',').every((value) => !!selectOptions.value.find((item) => item[props.rowKey] === value));
        if (isNotRequestTransform) {
          selectValues.value = value.split(',');
          return;
        }
        const params = { isMultiTranslate: true, pageSize: len, [props.rowKey]: value };
        if (isObject(attrs.params)) {
          Object.assign(params, attrs.params);
        }
        console.log('JTabsSelectUser params', params);
        getTableListOrigin(params).then((result: any) => {
          const records = result.records ?? [];
          selectValues.value = records.map((item) => item[props.rowKey]);
          selectOptions.value = records.map((item) => {
            return {
              ...item,
              label: item[props.labelKey],
              value: item[props.rowKey],
            };
          });
        });
      }
    } else {
      selectValues.value = [];
    }
  };
  // 监听value变化
  watch(
    () => props.value,
    () => {
      transform();
    },
    { deep: true, immediate: true }
  );
</script>

<style lang="less" scoped>
  // update-begin--author:liaozhiyang---date:20240605---for：【TV360X-1050】Safari浏览器指定下一步处理人页面控件没对齐
  .ant-select {
    vertical-align: middle;
  }
  // update-end--author:liaozhiyang---date:20240605---for：【TV360X-1050】Safari浏览器指定下一步处理人页面控件没对齐

  // 自定义按钮样式
  .custom-btn {
    height: 28px !important;
    padding: 0 12px !important;
    font-size: 13px !important;
  }
</style>
