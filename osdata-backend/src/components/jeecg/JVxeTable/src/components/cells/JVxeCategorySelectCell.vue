<template>
  <a-tree-select
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
   * 列配置须包含：pcode（分类根编码，优先）或 pid（分类根 ID）。
   * 支持多选：列配置加 multiple: true，存储值为逗号分隔字符串。
   */
  import { ref, computed, watch, defineComponent } from 'vue';
  import { loadDictItem, loadTreeData } from '/@/api/common/api';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';

  // 模块级标签缓存，key 为分类节点 id，在选中和回显时写入，translate.handler 优先读缓存
  const _catLabelCache = new Map<string, string>();

  function _cacheCatLabels(ids: string[], labels: string[]) {
    ids.forEach((id, i) => _catLabelCache.set(id, String(labels[i])));
  }

  export default defineComponent({
    name: 'JVxeCategorySelectCell',
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, originColumn, handleChangeCommon } = useJVxeComponent(props);

      // cellProps 内部以 {} 声明，无具体属性类型；统一转换后供模板直接使用，避免模板内 `as` 断言
      const cellAttrs = computed(() => cellProps.value as Record<string, any>);

      // 树形节点列表
      const treeData = ref<any[]>([]);
      // 当前选中的带 label 的值（labelInValue 格式，多选为数组）
      const treeValue = ref<any>(null);

      // 是否多选（由列配置 multiple 控制）
      const isMultiple = computed(() => !!originColumn.value?.multiple);

      /** 从列配置中解析分类树参数，优先使用 pcode */
      const catConfig = computed(() => {
        const col = originColumn.value;
        return {
          pcode: col?.pcode || '',
          pid: col?.pid || col?.pidValue || '',
          condition: col?.condition || '',
        };
      });

      // 外部值变化时回显 label
      watch(() => innerValue.value, (val) => loadItemByCode(val), { immediate: true });

      // 分类配置变化时重新加载根节点
      watch(() => catConfig.value.pcode, () => loadRoot(), { immediate: true });

      /** 加载分类根节点列表 */
      async function loadRoot() {
        const { pcode, pid, condition } = catConfig.value;
        const param = pcode ? { pcode, condition } : { pid: pid || '0', pcode: '0', condition };
        const res = await loadTreeData(param);
        if (res?.length) {
          treeData.value = res.map((item) => ({
            ...item,
            value: item.key,
            isLeaf: item.leaf === true,
          }));
        }
      }

      /** 根据存储值（逗号分隔 ID）查询对应的 label，用于回显 */
      async function loadItemByCode(val) {
        if (!val) {
          treeValue.value = isMultiple.value ? [] : { label: null, value: null };
          return;
        }
        const ids = val.split(',');
        // 若缓存已全部命中，直接用缓存构建 treeValue，跳过 API 请求
        const cachedLabels = ids.map((id) => _catLabelCache.get(id));
        if (cachedLabels.every((l) => l !== undefined)) {
          treeValue.value = isMultiple.value
            ? ids.map((id, i) => ({ key: id, value: id, label: cachedLabels[i] }))
            : { key: val, value: val, label: cachedLabels[0] };
          return;
        }
        const res = await loadDictItem({ ids: val });
        if (isMultiple.value) {
          // 多选：拆分 ID，逐一映射为 {key, value, label}
          treeValue.value = ids.map((id, index) => ({
            key: id,
            value: id,
            label: res?.[index] ?? id,
          }));
          if (res?.length) _cacheCatLabels(ids, res);
        } else {
          const label = res?.length ? res[0] : val;
          treeValue.value = { key: val, value: val, label };
          if (res?.length) _cacheCatLabels(ids, res);
        }
      }

      /** 异步懒加载子节点 */
      async function asyncLoadTreeData(treeNode) {
        if (treeNode.dataRef.children) return Promise.resolve();
        const { condition } = catConfig.value;
        const pid = treeNode.dataRef.key;
        const res = await loadTreeData({ pid, condition });
        if (res?.length) {
          const children = res.map((item) => ({
            ...item,
            value: item.key,
            isLeaf: item.leaf === true,
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
        if (!value || (Array.isArray(value) && value.length === 0)) {
          // 清空
          treeValue.value = isMultiple.value ? [] : null;
          handleChangeCommon('');
        } else if (Array.isArray(value)) {
          // 多选：数组 → 逗号分隔字符串；同时缓存 id->label 供非编辑模式直接使用
          treeValue.value = value;
          _cacheCatLabels(value.map((item) => item.value), value.map((item) => item.label));
          handleChangeCommon(value.map((item) => item.value).join(','));
        } else {
          // 单选
          treeValue.value = value;
          if (value.value != null) _cacheCatLabels([value.value], [value.label]);
          handleChangeCommon(value.value);
        }
      }

      return { cellAttrs, treeData, treeValue, isMultiple, asyncLoadTreeData, onChange };
    },
    // 【组件增强】非编辑模式：优先读缓存，避免重复 API 请求
    enhanced: {
      switches: { editRender: true, visible: false },
      translate: {
        enabled: true,
        async handler(value) {
          if (!value) return '';
          const ids = String(value).split(',');
          const cachedLabels = ids.map((id) => _catLabelCache.get(id));
          if (cachedLabels.every((l) => l !== undefined)) {
            return cachedLabels.join(',');
          }
          try {
            const res = await loadDictItem({ ids: value });
            if (res?.length) {
              _cacheCatLabels(ids, res);
              return res.join(',');
            }
          } catch {}
          return value;
        },
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>
