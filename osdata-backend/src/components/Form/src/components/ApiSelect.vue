<template>
  <Select
    v-bind="attrs_"
    v-model:value="state"
    :options="getOptions"
    show-search
    :filter-option="getFilterOption"
    @change="handleChange"
    @dropdownVisibleChange="handleFetch"
    @popupScroll="handlePopupScroll"
    @search="handleSearch"
  >
    <template #[item]="data" v-for="item in Object.keys($slots)">
      <slot :name="item" v-bind="data || {}"></slot>
    </template>
    <template #suffixIcon v-if="loading">
      <LoadingOutlined spin />
    </template>
    <template #notFoundContent v-if="loading">
      <span>
        <LoadingOutlined spin class="mr-1" />
        {{ t('component.form.apiSelectNotFound') }}
      </span>
    </template>
  </Select>
</template>
<script lang="ts">
  import { defineComponent, PropType, ref, watchEffect, computed, unref, watch } from 'vue';
  import { Select } from 'ant-design-vue';
  import { isFunction } from '/@/utils/is';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { get, omit } from 'lodash-es';
  import { LoadingOutlined } from '@ant-design/icons-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { propTypes } from '/@/utils/propTypes';
  import { isNumber } from '/@/utils/is';

  type OptionsItem = { label: string; value: string; disabled?: boolean };
  //文档 https://help.jeecg.com/ui/apiSelect#pageconfig%E5%8F%82%E6%95%B0%E9%85%8D%E7%BD%AE
  export default defineComponent({
    name: 'ApiSelect',
    components: {
      Select,
      LoadingOutlined,
    },
    inheritAttrs: false,
    props: {
      value: [Array, String, Number],
      numberToString: propTypes.bool,
      api: {
        type: Function as PropType<(arg?: Recordable) => Promise<OptionsItem[]>>,
        default: null,
      },
      // api params
      params: {
        type: Object as PropType<Recordable>,
        default: () => ({}),
      },
      //分页配置
      pageConfig: {
        type: Object as PropType<Recordable>,
        default: () => ({ isPage: false }),
      },
      // support xxx.xxx.xx
      resultField: propTypes.string.def(''),
      labelField: propTypes.string.def('label'),
      valueField: propTypes.string.def('value'),
      immediate: propTypes.bool.def(true),
      searchDebounce: propTypes.number.def(300),
    },
    emits: ['options-change', 'change'],
    setup(props, { emit }) {
      const options = ref<OptionsItem[]>([]);
      const loading = ref(false);
      const isFirstLoad = ref(true);
      const emitData = ref<any[]>([]);
      const attrs = useAttrs();
      const searchValue = ref('');
      let searchTimer: ReturnType<typeof setTimeout> | null = null;
      const { t } = useI18n();
      // 代码逻辑说明: 【QQYUN-11831】ApiSelect 分页下拉方案 #7883
      const hasMore = ref(true);
      const pagination = ref({
        pageNo: 1,
        pageSize: 10,
        total: 0,
      });
      // update-begin--author:liaozhiyang---date:20260617---for:【issue/9689】开启分页 + 搜索,过滤不到未加载数据
      const defPageConfig = { isPage: false, pageField: 'pageNo', pageSizeField: 'pageSize', totalField: 'total', listField: 'records', searchField: '' };
      const mergedPageConfig = computed(() => ({ ...defPageConfig, ...props.pageConfig }));
      const isRemoteSearch = computed(() => {
        const { isPage, searchField } = mergedPageConfig.value;
        return !!(isPage && searchField);
      });
      // update-end--author:liaozhiyang---date:20260617---for:【issue/9689】开启分页 + 搜索,过滤不到未加载数据
      // update-end--author:liusq---date:20250407---for：【QQYUN-11831】ApiSelect 分页下拉方案 #7883
      // Embedded in the form, just use the hook binding to perform form verification
      const [state, setState] = useRuleFormItem(props, 'value', 'change', emitData);
      // 代码逻辑说明: 【QQYUN-6308】解决警告
      let vModalValue: any;
      const attrs_ = computed(() => {
        let obj: any = unref(attrs) || {};
        if (obj && obj['onUpdate:value']) {
          vModalValue = obj['onUpdate:value'];
          delete obj['onUpdate:value'];
        }
        // 代码逻辑说明: 【issues/5467】ApiSelect修复覆盖了用户传递的方法
        if (obj['filterOption'] === undefined) {
          // 代码逻辑说明: 【issues/5305】无法按照预期进行搜索
          obj['filterOption'] = (inputValue, option) => {
            if (typeof option['label'] === 'string') {
              return option['label'].toLowerCase().indexOf(inputValue.toLowerCase()) != -1;
            } else {
              return true;
            }
          };
        }
        return obj;
      });
      // 代码逻辑说明: 【QQYUN-6308】解决警告
      const getOptions = computed(() => {
        const { labelField, valueField, numberToString } = props;
        return unref(options).reduce((prev, next: Recordable) => {
          if (next) {
            const value = next[valueField];
            prev.push({
              // update-begin--author:liaozhiyang---date:20260226---for:【issues/9326】ApiSelect组件，返回数据中包含 options 这个名称导致组件渲染失败
              ...omit(next, [labelField, valueField, 'options']),
              // update-end--author:liaozhiyang---date:20260226---for:【issues/9326】ApiSelect组件，返回数据中包含 options 这个名称导致组件渲染失败
              label: next[labelField],
              value: numberToString ? `${value}` : value,
            });
          }
          return prev;
        }, [] as OptionsItem[]);
      });
      // 代码逻辑说明: 【issues/8527】apiSelect分页加载重复请求
      watch(
        () => props.immediate,
        () => {
          props.immediate && fetch();
        },
        { immediate: true }
      );
      watch(
        () => [props.api, props.pageConfig, props.resultField, props.params],
        () => {
          props.immediate && fetch();
        },
        { deep: true }
      );

      watch(
        () => props.params,
        () => {
          !unref(isFirstLoad) && fetch();
        },
        { deep: true }
      );
      //监听数值修改，查询数据
      watchEffect(() => {
        props.value && handleFetch();
      });
      /**
       * 筛选流程
       * @param input
       * @param option
       */
      const filterOption = (input: string, option: any) => {
        return option.value.toLowerCase().indexOf(input.toLowerCase()) >= 0 || option.label.indexOf(input) >= 0;
      };
      // update-begin--author:liaozhiyang---date:20260617---for:【issue/9689】开启分页 + 搜索,过滤不到未加载数据
      const getFilterOption = computed(() => {
        return isRemoteSearch.value ? false : filterOption;
      });
      function handleSearch(input: string) {
        if (!isRemoteSearch.value) return;
        if (searchTimer) clearTimeout(searchTimer);
        searchTimer = setTimeout(() => {
          if (searchValue.value === input) return;
          searchValue.value = input;
          pagination.value.pageNo = 1;
          hasMore.value = true;
          fetch();
        }, props.searchDebounce);
      }
      // update-end--author:liaozhiyang---date:20260617---for:【issue/9689】开启分页 + 搜索,过滤不到未加载数据
      async function fetch() {
        const api = props.api;
        if (!api || !isFunction(api)) return;
        // 代码逻辑说明: 【QQYUN-11831】ApiSelect 分页下拉方案 #7883
        if (!props.pageConfig.isPage || pagination.value.pageNo == 1) {
          options.value = [];
        }
        try {
          loading.value = true;
          let { isPage, pageField, pageSizeField, totalField, listField, searchField } = mergedPageConfig.value;
          let params = isPage
            ? { ...props.params, [pageField]: pagination.value.pageNo, [pageSizeField]: pagination.value.pageSize }
            : { ...props.params };
          // update-begin--author:liaozhiyang---date:20260617---for:【issue/9689】开启分页 + 搜索,过滤不到未加载数据
          // 携带远程搜索关键字到后端
          if (isPage && searchField && searchValue.value) {
            params[searchField] = searchValue.value;
          }
          // update-end--author:liaozhiyang---date:20260617---for:【issue/9689】开启分页 + 搜索,过滤不到未加载数据
          const res = await api(params);
          if (isPage) {
            // 代码逻辑说明: 【QQYUN-11831】ApiSelect 分页下拉方案 #7883
            options.value = [...options.value, ...res[listField]];
            pagination.value.total = res[totalField] || 0;
            hasMore.value = res[totalField] ? options.value.length < res[totalField] : res[listField] < pagination.value.pageSize;
          } else {
            if (Array.isArray(res)) {
              options.value = res;
              emitChange();
              return;
            }
            if (props.resultField) {
              options.value = get(res, props.resultField) || [];
            }
          }
          emitChange();
        } catch (error) {
          console.warn(error);
        } finally {
          loading.value = false;
          ['multiple', 'tags'].includes(unref(attrs).mode) && !Array.isArray(unref(state)) && setState([]);
          initValue();
        }
      }

      function initValue() {
        let value = props.value;
        // 代码逻辑说明: 【issues/8037】初始化值单选的值被错误地写入数组值
        if (['multiple', 'tags'].includes(unref(attrs).mode)) {
          if (value && typeof value === 'string' && value != 'null' && value != 'undefined') {
            state.value = value.split(',');
          } else if (isNumber(value)) {
            state.value = [value];
          }
        } else {
          state.value = value;
        }
      }

      async function handleFetch() {
        if (!props.immediate && unref(isFirstLoad)) {
          await fetch();
          isFirstLoad.value = false;
        }
      }

      function emitChange() {
        emit('options-change', unref(getOptions));
      }

      function handleChange(_, ...args) {
        vModalValue && vModalValue(_);
        emitData.value = args;
      }
      // 滚动加载更多
      function handlePopupScroll(e) {
        const { scrollTop, scrollHeight, clientHeight } = e.target;
        const isNearBottom = scrollHeight - scrollTop <= clientHeight + 20;
        if (props.pageConfig.isPage && isNearBottom && hasMore.value && !loading.value) {
          pagination.value.pageNo += 1;
          fetch();
        }
      }
      return { state, attrs_, attrs, getOptions, loading, t, handleFetch, handleChange, handlePopupScroll, filterOption, getFilterOption, handleSearch };
    },
  });
</script>
