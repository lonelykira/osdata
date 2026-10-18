<template>
  <a-tree-select
    v-if="show"
    allow-clear
    label-in-value
    show-checked-strategy="SHOW_ALL"
    style="width: 100%"
    :get-popup-container="(node) => node?.parentNode"
    :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
    :placeholder="cellAttrs.placeholder || '请选择'"
    :disabled="cellAttrs.disabled"
    :multiple="isMultiple"
    :load-data="asyncLoadTreeData"
    :value="treeValue"
    :tree-data="treeData"
    @change="onChange"
  />
</template>

<script lang="ts">
  /**
   * 列配置须包含：dict（"表名,文本字段,存储字段"）、pidField（父级字段名）、
   * pidValue（根节点值，可选）、hasChildField（是否有子节点字段，可选）。
   * 支持多选：列配置加 multiple: true，存储值为逗号分隔字符串。
   */
  import { ref, computed, watch, defineComponent } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';

  const API_LOAD_TREE = '/sys/dict/loadTreeData';
  const API_LOAD_ITEM = '/sys/dict/loadDictItem/';

  // 模块级标签缓存，key 为 "tableName:id"，在选中和回显时写入，translate.handler 优先读缓存
  const _labelCache = new Map<string, string>();

  function _cacheLabels(tableName: string, ids: string[], labels: string[]) {
    ids.forEach((id, i) => _labelCache.set(`${tableName}:${id}`, String(labels[i])));
  }

  export default defineComponent({
    name: 'JVxeTreeSelectCell',
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, originColumn, handleChangeCommon } = useJVxeComponent(props);

      // cellProps 内部以 {} 声明，无具体属性类型；统一转换后供模板直接使用，避免模板内 `as` 断言
      const cellAttrs = computed(() => cellProps.value as Record<string, any>);

      // 树形节点列表
      const treeData = ref<any[]>([]);
      // 当前选中的带 label 的值（labelInValue 格式，多选为数组）
      const treeValue = ref<any>(null);
      // 用于重置组件（dict 变更时重新挂载）
      const show = ref(true);

      // 是否多选（由列配置 multiple 控制）
      const isMultiple = computed(() => !!originColumn.value?.multiple);

      /**
       * 从列配置中解析树相关参数
       * dict 格式为 "tableName,textField,codeField"
       */
      const treeConfig = computed(() => {
        const col = originColumn.value;
        const dictArr = (col?.dict || '').split(',');
        return {
          tableName: dictArr[0] || '',
          text: dictArr[1] || '',
          code: dictArr[2] || '',
          pidField: col?.pidField || 'pid',
          pidValue: col?.pidValue || '',
          hasChildField: col?.hasChildField || '',
          condition: col?.condition || '',
        };
      });

      // 外部值变化时回显 label
      watch(
        () => innerValue.value,
        (val) => loadItemByCode(val),
        { immediate: true }
      );

      // dict 配置变化时重新加载根节点
      watch(
        () => treeConfig.value.tableName,
        async (tableName) => {
          if (tableName) {
            treeData.value = [];
            show.value = false;
            await loadRoot();
            show.value = true;
          }
        },
        { immediate: true }
      );

      /** 加载根节点列表 */
      async function loadRoot() {
        const { tableName, text, code, pidField, pidValue, hasChildField, condition } = treeConfig.value;
        if (!tableName) return;
        const res = await defHttp.get(
          { url: API_LOAD_TREE, params: { tableName, text, code, pid: pidValue, pidField, hasChildField, condition } },
          { isTransformResponse: false }
        );
        if (res.success && res.result) {
          treeData.value = res.result.map((item) => ({
            ...item,
            value: item.key,
            isLeaf: !!item.leaf,
          }));
        }
      }

      /** 根据存储值（逗号分隔 ID）查询对应的 label，用于回显 */
      async function loadItemByCode(val) {
        if (!val) {
          treeValue.value = isMultiple.value ? [] : { label: null, value: null };
          return;
        }
        const { tableName, text, code } = treeConfig.value;
        if (!tableName) {
          treeValue.value = isMultiple.value ? val.split(',').map((id) => ({ key: id, value: id, label: id })) : { label: val, value: val };
          return;
        }
        const ids = val.split(',');
        // 若缓存已全部命中，直接用缓存构建 treeValue，跳过 API 请求
        const cachedLabels = ids.map((id) => _labelCache.get(`${tableName}:${id}`));
        if (cachedLabels.every((l) => l !== undefined)) {
          treeValue.value = isMultiple.value
            ? ids.map((id, i) => ({ key: id, value: id, label: cachedLabels[i] }))
            : { key: val, value: val, label: cachedLabels[0] };
          return;
        }
        const res = await defHttp.get({ url: `${API_LOAD_ITEM}${tableName},${text},${code}`, params: { key: val } }, { isTransformResponse: false });
        if (isMultiple.value) {
          // 多选：拆分 ID，逐一映射为 {key, value, label}
          treeValue.value = ids.map((id, index) => ({
            key: id,
            value: id,
            label: res.success ? (res.result?.[index] ?? id) : id,
          }));
          if (res.success && res.result?.length) _cacheLabels(tableName, ids, res.result);
        } else {
          const label = res.success && res.result?.length ? res.result[0] : val;
          treeValue.value = { key: val, value: val, label };
          if (res.success && res.result?.length) _cacheLabels(tableName, ids, res.result);
        }
      }

      /** 异步懒加载子节点 */
      async function asyncLoadTreeData(treeNode) {
        if (treeNode.dataRef.children) return Promise.resolve();
        const { tableName, text, code, pidField, hasChildField, condition } = treeConfig.value;
        const pid = treeNode.dataRef.key;
        const res = await defHttp.get(
          { url: API_LOAD_TREE, params: { tableName, text, code, pid, pidField, hasChildField, condition } },
          { isTransformResponse: false }
        );
        if (res.success) {
          const children = res.result.map((item) => ({
            ...item,
            value: item.key,
            isLeaf: !!item.leaf,
          }));
          addChildren(pid, children, treeData.value);
          treeData.value = [...treeData.value];
        }
        return Promise.resolve();
      }

      /** 递归将子节点插入到对应父节点下 */
      function addChildren(pid, children, arr) {
        if (!arr?.length) return;
        for (const item of arr) {
          if (item.key === pid) {
            item.children = children.length ? children : undefined;
            if (!children.length) item.isLeaf = true;
            break;
          }
          addChildren(pid, children, item.children);
        }
      }

      /** 选中或清空事件 */
      function onChange(value) {
        const { tableName } = treeConfig.value;
        if (!value || (Array.isArray(value) && value.length === 0)) {
          // 清空
          treeValue.value = isMultiple.value ? [] : null;
          handleChangeCommon('');
        } else if (Array.isArray(value)) {
          // 多选：数组 → 逗号分隔字符串；同时缓存 id->label 供非编辑模式直接使用
          treeValue.value = value;
          if (tableName)
            _cacheLabels(
              tableName,
              value.map((item) => item.value),
              value.map((item) => item.label)
            );
          handleChangeCommon(value.map((item) => item.value).join(','));
        } else {
          // 单选
          treeValue.value = value;
          if (tableName && value.value != null) _cacheLabels(tableName, [value.value], [value.label]);
          handleChangeCommon(value.value);
        }
      }

      return { cellAttrs, treeData, treeValue, show, isMultiple, asyncLoadTreeData, onChange };
    },
    // 【组件增强】非编辑模式：优先读缓存，避免重复 API 请求
    enhanced: {
      switches: { editRender: true, visible: false },
      translate: {
        enabled: true,
        async handler(value, ctx) {
          if (!value) return '';
          const col = ctx?.context.originColumn.value;
          const dictArr = (col?.dict || '').split(',');
          const tableName = dictArr[0];
          if (!tableName) return value;
          const ids = String(value).split(',');
          const cachedLabels = ids.map((id) => _labelCache.get(`${tableName}:${id}`));
          if (cachedLabels.every((l) => l !== undefined)) {
            return cachedLabels.join(',');
          }
          try {
            const res = await defHttp.get(
              { url: `${API_LOAD_ITEM}${tableName},${dictArr[1] || ''},${dictArr[2] || ''}`, params: { key: value } },
              { isTransformResponse: false }
            );
            if (res.success && res.result?.length) {
              _cacheLabels(tableName, ids, res.result);
              return res.result.join(',');
            }
          } catch {}
          return value;
        },
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>
