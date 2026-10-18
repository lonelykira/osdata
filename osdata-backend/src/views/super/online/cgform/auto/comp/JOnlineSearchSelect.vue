<!-- online查询条件中的下拉搜索 -->
<template>
  <a-select
    :value="selected"
    :placeholder="placeholder"
    show-search
    :default-active-first-option="false"
    :show-arrow="true"
    :filter-option="false"
    :not-found-content="null"
    @search="handleSearch"
    @change="handleChange"
    @popupScroll="handlePopupScroll"
    allowClear
  >
    <a-select-option v-for="d in selectOptions" :key="d.value">
      {{ d.text }}
    </a-select-option>
  </a-select>
</template>

<script>
  import { useDebounceFn } from '@vueuse/core';
  import { defHttp } from '/@/utils/http/axios';
  import { useMessage } from '/@/hooks/web/useMessage';
  const { createMessage: $message } = useMessage();
  import { watch, ref } from 'vue';

  export default {
    name: 'JOnlineSearchSelect',
    props: {
      placeholder: {
        type: String,
        default: '',
        required: false,
      },
      value: {
        type: String,
        required: false,
      },
      // online CgReport item id
      fieldId: {
        type: String,
        required: true,
      },
      // online CgReport id，用于按具体报表菜单鉴权
      reportId: {
        type: String,
        required: false,
      },
    },
    emits: ['update:value'],
    setup(props, { emit }) {
      let selected = ref('');
      let selectOptions = ref([]);
      let isHasData = true;
      let scrollLoading = false;
      let searchKeyword = '';
      const pageNo = ref(1);

      watch(
        () => props.value,
        (newVal) => {
          if (!newVal) {
            selected.value = undefined;
          } else {
            selected.value = newVal;
          }
        },
        { immediate: true }
      );

      watch(
        () => props.fieldId,
        () => {
          resetOptions();
        },
        { immediate: true }
      );
      /**
       * 2024-07-17
       * liaozhiyang
       * 【TV360X-1813】online报表查询支持滚动加载
       * */
      const handleSearch = useDebounceFn((keyword) => {
        searchKeyword = keyword;
        pageNo.value = 1;
        isHasData = true;
        searchByKeyword(keyword);
      }, 800);

      /**
       * 2024-07-17
       * liaozhiyang
       * 【TV360X-1813】online报表查询支持滚动加载
       * */
      async function searchByKeyword(keyword = '') {
        //update-begin---author:scott ---date:20260811  for：【LHZP-1148】查询字典接口按报表菜单权限鉴权-----------
        if (!props.reportId) {
          return;
        }
        let params = {
          keyword: keyword,
          fieldId: props.fieldId,
          pageSize: 10,
          pageNo: pageNo.value,
        };
        let url = `/online/cgreport/api/getReportDictList/${props.reportId}`;
        //update-end---author:scott ---date:20260811  for：【LHZP-1148】查询字典接口按报表菜单权限鉴权-----------
        await defHttp
          .get({ url: url, params }, { isTransformResponse: false })
          .then((res) => {
            if (res.success) {
              if (res.result && res.result.length > 0) {
                if (pageNo.value == 1) {
                  selectOptions.value = [...res.result];
                } else {
                  selectOptions.value.push(...res.result);
                }
                pageNo.value++;
              } else {
                if (pageNo.value == 1) {
                  selectOptions.value = [];
                }
                isHasData = false;
              }
            } else {
              $message.warning(res.message);
            }
          })
          .catch(() => {
            pageNo.value != 1 && pageNo.value--;
          });
      }

      function handleChange(value) {
        emit('update:value', value);
        //点击clear按钮,重置下拉项
        if (!value || value == '') {
          resetOptions();
        }
      }
      function resetOptions() {
        selectOptions.value = [];
        // update-begin--author:liaozhiyang---date:20240717---for：【TV360X-1813】online报表查询支持滚动加载
        pageNo.value = 1;
        isHasData = true;
        searchKeyword = '';
        // update-end--author:liaozhiyang---date:20240717---for：【TV360X-1813】online报表查询支持滚动加载
        searchByKeyword();
      }
      /**
       * 2024-07-17
       * liaozhiyang
       * 【TV360X-1813】online报表查询支持滚动加载
       * */
      const handlePopupScroll = async (e) => {
        const { target } = e;
        const { scrollTop, scrollHeight, clientHeight } = target;
        if (!scrollLoading && isHasData && scrollTop + clientHeight >= scrollHeight - 10) {
          scrollLoading = true;
          searchByKeyword(searchKeyword).finally(() => {
            scrollLoading = false;
          });
        }
      };
      return {
        selectOptions,
        handleSearch,
        handleChange,
        selected,
        handlePopupScroll,
      };
    },
  };
</script>

<style scoped></style>
