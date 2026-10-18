import { computed, ref, Ref, unref } from 'vue';
import { useDebounceFn } from '@vueuse/core';
import { defHttp } from '/@/utils/http/axios';

/** 触发「加载更多」的滚动剩余距离阈值（px） */
const SCROLL_LOAD_THRESHOLD = 10;
/** 搜索输入防抖时间（ms） */
const SEARCH_DEBOUNCE_MS = 300;

export interface ScrollLoadDictProps {
  dictCode?: string;
  pageSize: number;
  scrollLoad: boolean;
}

export function useScrollLoadDict(props: ScrollLoadDictProps, dictOptions: Ref<any[]>, arrayValue: Ref<any[]>) {
  // --- 状态 ---
  const loading = ref(false);
  const pageNo = ref(1);
  const isHasData = ref(true);
  const scrollLoading = ref(false);
  const searchKeyword = ref('');

  // --- 计算：是否表字典、是否启用滚动加载 ---
  const isDictTable = computed(() => {
    if (!props.dictCode) return false;
    return props.dictCode.split(',').length >= 2;
  });
  const useLoadDict = computed(() => props.scrollLoad && isDictTable.value);

  /**
   * 拉取一页字典数据（loadDict 接口）。
   * @param pageNoToLoad 页码（从 1 开始）
   * @param isAppend true=追加到当前列表，false=替换
   * @param keyword 搜索关键字，不传则用内部的 searchKeyword
   */
  function fetchLoadDictPage(pageNoToLoad: number, isAppend: boolean, keyword?: string) {
    const kw = keyword !== undefined ? keyword : searchKeyword.value;
    return defHttp
      .get({
        url: `/sys/dict/loadDict/${props.dictCode}`,
        params: { pageNo: pageNoToLoad, pageSize: props.pageSize, keyword: kw || '', order: 'asc' },
      })
      .then((res: any) => {
        const items = (res || []).map((item: any) => ({
          value: item.value,
          label: item.text || item.label,
          text: item.text || item.label,
          color: item.color,
        }));
        if (items.length > 0) {
          if (isAppend) {
            // 追加时按 value 去重，避免与回显补的项重复
            const existValues = new Set(dictOptions.value.map((o) => String(o.value)));
            const newItems = items.filter((it: any) => !existValues.has(String(it.value)));
            if (newItems.length > 0) {
              dictOptions.value = dictOptions.value.concat(newItems);
            }
          } else {
            // 有选中的值且optinos中存在时，需要把选中的值在options中存在且不在新数据中的项保留（防止多次请求）
            if (unref(arrayValue).length && unref(dictOptions).length) {
              unref(arrayValue).forEach((val: any) => {
                const existOption = unref(dictOptions).find((o: any) => String(o.value) === String(val));
                if (existOption && !items.some((item: any) => String(item.value) === String(val))) {
                  items.push(existOption);
                }
              });
            }
            dictOptions.value = items;
          }
          pageNo.value = pageNoToLoad + 1;
        } else {
          if (!isAppend) dictOptions.value = [];
          isHasData.value = false;
        }
      });
  }

  function fetchDictItemByValue(val: any) {
    if (val == null || !props.dictCode) return Promise.resolve(null);
    return defHttp
      .get({ url: `/sys/dict/loadDictItem/${props.dictCode}`, params: { key: val } })
      .then((res: any) => {
        if (Array.isArray(res) && res.length > 0) {
          const first = res[0];
          if (typeof first === 'string') {
            return { value: val, label: first, text: first, color: undefined };
          }
          return {
            value: first.value ?? val,
            label: first.text ?? first.label,
            text: first.text ?? first.label ?? '',
            color: first.color,
          };
        }
        return null;
      })
      .catch(() => null);
  }

  function ensureValueInOptions() {
    if (!useLoadDict.value) return;
    const vals = arrayValue.value;
    if (!vals || vals.length === 0) return;
    const existSet = new Set(dictOptions.value.map((o) => String(o.value)));
    const missing = vals.filter((v) => !existSet.has(String(v)));
    if (missing.length === 0) return;
    Promise.all(missing.map((v) => fetchDictItemByValue(v))).then((items) => {
      const newItems = items.filter(Boolean);
      if (newItems.length > 0) {
        dictOptions.value = [...dictOptions.value, ...newItems];
      }
    });
  }

  /** 搜索：带防抖，用 keyword 拉取第一页并替换列表 */
  const handleSearch = useDebounceFn((keyword: string) => {
    if (!useLoadDict.value) return;
    searchKeyword.value = keyword || '';
    pageNo.value = 1;
    isHasData.value = true;
    loading.value = true;
    fetchLoadDictPage(1, false, searchKeyword.value).finally(() => {
      loading.value = false;
      ensureValueInOptions();
    });
  }, SEARCH_DEBOUNCE_MS);

  function handleDropdownVisibleChange(open: boolean) {
    if (!useLoadDict.value || !open) return;
    if (!searchKeyword.value) return;
    searchKeyword.value = '';
    pageNo.value = 1;
    isHasData.value = true;
    loading.value = true;
    fetchLoadDictPage(1, false, '').finally(() => {
      loading.value = false;
      ensureValueInOptions();
    });
  }

  /** 初始加载字典：拉取第一页 */
  function loadDictOptions() {
    if (!useLoadDict.value) return;
    pageNo.value = 1;
    isHasData.value = true;
    loading.value = true;
    fetchLoadDictPage(1, false).finally(() => {
      loading.value = false;
      ensureValueInOptions();
    });
  }

  /** 下拉内滚动触底时加载下一页，按 value 去重后追加 */
  function handlePopupScroll(e: Event) {
    if (!useLoadDict.value) return;
    const target = e.target as HTMLElement;
    const { scrollTop, scrollHeight, clientHeight } = target;
    const nearBottom = scrollTop + clientHeight >= scrollHeight - SCROLL_LOAD_THRESHOLD;
    if (!scrollLoading.value && isHasData.value && nearBottom) {
      scrollLoading.value = true;
      fetchLoadDictPage(pageNo.value, true)
        .finally(() => {
          scrollLoading.value = false;
        })
        .catch(() => {
          if (pageNo.value > 1) pageNo.value--;
        });
    }
  }

  return {
    isDictTable,
    useLoadDict,
    loading,
    loadDictOptions,
    ensureValueInOptions,
    handleSearch,
    handleDropdownVisibleChange,
    handlePopupScroll,
  };
}
