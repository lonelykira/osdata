<template>
  <JPopupDict v-bind="popupProps" @change="onChange" @options-change="onOptionsChange" />
</template>
<script lang="ts">
  import { computed, defineComponent } from 'vue';
  import JPopupDict from '/@/components/Form/src/jeecg/components/JPopupDict.vue';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';
  import { dispatchEvent } from '/@/components/jeecg/JVxeTable/utils';
  import { defHttp } from '/@/utils/http/axios';

  // 解析 dictCode（"code,labelField,valueField"）
  function parseDictCode(dictCode: string | undefined) {
    const arr = (dictCode || '').split(',');
    return { code: arr[0] || '', labelField: arr[1] || '', valueField: arr[2] || '' };
  }
  let resultOptions = [];
  const CG_RP_CONFIG_ID_SESSION_PREFIX = 'jeecg:popupDict:cgRpConfigId:';
  const cgRpConfigIdCache = new Map<string, string>();
  function safeSessionGet(key: string) {
    return sessionStorage.getItem(key) || '';
  }
  function safeSessionSet(key: string, value: string) {
    sessionStorage.setItem(key, value);
  }

  // 拿 cgRpConfigId（会话缓存 + 内存缓存）
  async function getCgRpConfigId(code: string) {
    if (!code) return '';
    const sessionKey = `${CG_RP_CONFIG_ID_SESSION_PREFIX}${code}`;
    const cached = cgRpConfigIdCache.get(code) || safeSessionGet(sessionKey);
    if (cached) {
      if (!cgRpConfigIdCache.has(code)) cgRpConfigIdCache.set(code, cached);
      return cached;
    }
    const res = await defHttp.get({ url: `/online/cgreport/api/getRpColumns/${code}` }, { isTransformResponse: false, successMessageMode: 'none' });
    const id = (res.success && res.result?.cgRpConfigId ? String(res.result.cgRpConfigId) : '') || '';
    if (id) {
      cgRpConfigIdCache.set(code, id);
      safeSessionSet(sessionKey, id);
    }
    return id;
  }

  export default defineComponent({
    name: 'JVxePopupDictCell',
    components: { JPopupDict },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, originColumn, cellProps, handleChangeCommon } = useJVxeComponent(props);

      // 仅支持 fieldConfig：cgreport报表code,显示字段(label),存储字段(value)
      const computedFieldConfig = computed(() => {
        const col = originColumn.value;
        return (col as any).fieldConfig || '';
      });

      const popupProps = computed(() => ({
        ...cellProps.value,
        value: innerValue.value ?? '',
        dictCode: computedFieldConfig.value,
        multi: originColumn.value.multi ?? originColumn.value.popupMulti ?? false,
        param: originColumn.value.params ?? originColumn.value.param ?? {},
        sorter: originColumn.value.sorter,
        useInput: true,
      }));

      function onChange(val) {
        setTimeout(() => {
          handleChangeCommon(val ?? '');
        }, 0);
      }
      function onOptionsChange(options) {
        resultOptions = options;
      }

      return { popupProps, onChange, onOptionsChange };
    },
    // 【组件增强】非编辑模式下把 value(username) 翻译成 label(realname)
    enhanced: {
      switches: { editRender: true, visible: false },
      aopEvents: {
        editActived({ $event }) {
          dispatchEvent({
            $event,
            props: this.props,
            className: '.ant-select',
            isClick: true,
          });
        },
      },
      translate: {
        enabled: true,
        async handler(value, ctx) {
          if (value == null || value === '') return '';
          const col = ctx?.context.originColumn.value;
          const fieldConfig = (col as any)?.fieldConfig || '';
          const { code, labelField, valueField } = parseDictCode(fieldConfig);
          if (!code || !labelField || !valueField) return value;
          if (resultOptions?.length) {
            // 刚点击完弹窗，直接返回结果options
            const result = resultOptions.map((item: any) => item.text);
            resultOptions = [];
            return result.join(',');
          }
          const ids = String(value).split(',');
          try {
            const cgRpConfigId = await getCgRpConfigId(code);
            if (!cgRpConfigId) return value;
            const params = {
              [`force_${valueField}`]: value,
            };
            const res = await defHttp.get(
              { url: `/online/cgreport/api/getData/${cgRpConfigId}`, params },
              { isTransformResponse: false, successMessageMode: 'none' }
            );
            const records = res?.result?.records;
            if (records?.length) {
              const labels = ids.map((id) => {
                const found = records.find((r) => String(r[valueField]) === id);
                return found?.[labelField] ?? id;
              });
              return labels.join(',');
            }
          } catch {}
          return value;
        },
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>
<style lang="less" scoped>
  .JPopupDict {
    :deep(.ant-select) {
      width: 100%;
    }
  }
</style>
