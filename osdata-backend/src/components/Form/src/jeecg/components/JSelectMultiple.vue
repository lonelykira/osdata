<!--字典下拉多选-->
<template>
  <a-select
    :value="arrayValue"
    @change="onChange"
    mode="multiple"
    :filter-option="useLoadDict ? false : filterOption"
    :disabled="disabled"
    :placeholder="placeholder"
    allowClear
    showSearch
    :getPopupContainer="getParentContainer"
    :notFoundContent="loading ? undefined : null"
    @search="handleSearch"
    @dropdown-visible-change="handleDropdownVisibleChange"
    @popupScroll="handlePopupScroll"
  >
    <template #notFoundContent>
      <a-spin v-if="loading" size="small" />
    </template>
    <template v-for="item of getOptions" :key="item.key">
      <a-select-option :value="item.value" :getPopupContainer="getParentContainer">
        <span :class="item.class" :style="item.style">{{ item.text }}</span>
      </a-select-option>
    </template>
  </a-select>
</template>
<script lang="ts">
  import { computed, defineComponent, onMounted, ref, nextTick, watch } from 'vue';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { initDictOptions } from '/@/utils/dict/index';
  import { setPopContainer } from '/@/utils';
  import { useScrollLoadDict } from '../hooks/useSelectMultipleScrollLoad';

  export default defineComponent({
    name: 'JSelectMultiple',
    components: {},
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      placeholder: {
        type: String,
        default: '请选择',
        required: false,
      },
      readOnly: {
        type: Boolean,
        required: false,
        default: false,
      },
      options: {
        type: Array,
        default: () => [],
        required: false,
      },
      triggerChange: {
        type: Boolean,
        required: false,
        default: true,
      },
      spliter: {
        type: String,
        required: false,
        default: ',',
      },
      popContainer: {
        type: String,
        default: '',
        required: false,
      },
      dictCode: {
        type: String,
        required: false,
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      useDicColor: {
        type: Boolean,
        default: false,
      },
      // update-begin--author:liaozhiyang---date:20260204---for:【issues/9307】online下拉加载表字典需滚动加载
      // 分页时每页条数，仅当 scrollLoad 为 true 且 dictCode 为字典表格式时生效
      pageSize: { type: Number, default: 10 },
      // 是否滚动加载，为 true 且 dictCode 为字典表格式(table,text,code)时走 /sys/dict/loadDict/，否则走 initDictOptions
      scrollLoad: { type: Boolean, default: false },
      // update-begin--author:liaozhiyang---date:20260204---for:【issues/9307】online下拉加载表字典需滚动加载
    },
    emits: ['options-change', 'change', 'input', 'update:value'],
    setup(props, { emit }) {
      //console.info(props);
      const emitData = ref<any[]>([]);
      const arrayValue = ref<any[]>(
        !props.value ? [] : Array.isArray(props.value) ? props.value : (props.value as string).split(props.spliter)
      );
      const dictOptions = ref<any[]>([]);
      const attrs = useAttrs();
      const [state, , , formItemContext] = useRuleFormItem(props, 'value', 'change', emitData);
      // update-begin--author:liaozhiyang---date:20260204---for:【issues/9307】online下拉加载表字典需滚动加载
      const scrollLoadApi = useScrollLoadDict(props, dictOptions, arrayValue);
      const { useLoadDict, loading, loadDictOptions: loadDictOptionsScroll, ensureValueInOptions, handleSearch, handleDropdownVisibleChange, handlePopupScroll, isDictTable } = scrollLoadApi;
      // update-end--author:liaozhiyang---date:20260204---for:【issues/9307】online下拉加载表字典需滚动加载
      // 处理下拉选项
      const getOptions = computed(() => {
        if (!Array.isArray(dictOptions.value)) {
          return [];
        }
        return dictOptions.value.map((item, index) => {
          const {useDicColor} = props;
          const text = item.text || item.label || item.label || '';
          const key = item.value + '_' + text + '_' + index;
          return {
            key: key,
            text: text,
            value: item.value,
            color: item.color,
            class: [useDicColor && item.color ? 'colorText' : ''],
            style: {backgroundColor: `${useDicColor && item.color}`},
          };
        });
      });

      onMounted(() => {
        if (props.dictCode) {
          loadDictOptions();
        } else {
          dictOptions.value = props.options;
        }
      });

      watch(
          () => props.dictCode,
          () => {
            if (props.dictCode) {
              loadDictOptions();
            } else {
              dictOptions.value = props.options;
            }
          }
      );

      watch(
        () => props.value,
        (val) => {
          if (!val) {
            arrayValue.value = [];
          } else {
            arrayValue.value = Array.isArray(props.value) ? props.value : (props.value as string).split(props.spliter);
          }
          if (useLoadDict.value) ensureValueInOptions();
        }
      );

      //适用于 动态改变下拉选项的操作
      watch(()=>props.options, ()=>{
        if (props.dictCode) {
          // update-begin--author:liaozhiyang---date:20260325---for:【QQYUN-15021】online js增强修改下拉不生效
          // online js增强改变options
          dictOptions.value = props.options;
          // update-end--author:liaozhiyang---date:20260325---for:【QQYUN-15021】online js增强修改下拉不生效
        } else {
          dictOptions.value = props.options;
        }
      });

      function onChange(selectedValue) {
        if (props.triggerChange) {
          emit('change', selectedValue.join(props.spliter));
          emit('update:value', selectedValue.join(props.spliter));
        } else {
          emit('input', selectedValue.join(props.spliter));
          emit('update:value', selectedValue.join(props.spliter));
        }
        // 代码逻辑说明: 【QQYUN-9110】组件有值校验没消失
        nextTick(() => {
          formItemContext?.onFieldChange();
        });
      }

      function getParentContainer(node) {
        if (!props.popContainer) {
          return node?.parentNode;
        } else {
          // 代码逻辑说明: 【QQYUN-9339】有多个modal弹窗内都有下拉字典多选和下拉搜索组件时，打开另一个modal时组件的options不展示
          return setPopContainer(node, props.popContainer);
        }
      }

      async function loadDictOptions() {
        // update-begin--author:liaozhiyang---date:20260204---for:【issues/9307】online下拉加载表字典需滚动加载
        if (useLoadDict.value) {
          loadDictOptionsScroll();
        } else {
          const code = props.dictCode ?? '';
          try {
            const dictData = await initDictOptions(code);
            dictOptions.value = dictData;
          } catch (error) {
            console.error('initDictOptions error:', error);
            dictOptions.value = [];
          }
        }
        // update-end--author:liaozhiyang---date:20260204---for:【issues/9307】online下拉加载表字典需滚动加载
      }

      // 代码逻辑说明: VUEN-1145 下拉多选，搜索时，查不到数据
      function filterOption(input, option) {
        return option.children()[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      }

      return {
        state,
        attrs,
        getOptions,
        dictOptions,
        onChange,
        arrayValue,
        getParentContainer,
        filterOption,
        isDictTable,
        useLoadDict,
        loading,
        handlePopupScroll,
        handleSearch,
        handleDropdownVisibleChange,
      };
    },
  });
</script>
<style scoped lang='less'>
.colorText{
  display: inline-block;
    height: 20px;
    line-height: 20px;
    padding: 0 6px;
    border-radius: 8px;
    background-color: red;
    color: #fff;
    font-size: 12px;
}
</style>
