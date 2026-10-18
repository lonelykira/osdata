<template>
  <detail-form :schemas="detailFormSchemas" :data="subFormData" :span="formSpan"></detail-form>
</template>

<script lang="ts">
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ref, watch } from 'vue';
  import { BasicForm } from '/@/components/Form/index';
  import { defHttp } from '/@/utils/http/axios';
  import { getRefPromise } from '../../hooks/auto/useAutoForm';
  import { Loading } from '/@/components/Loading';
  import DetailForm from '../../extend/form/DetailForm.vue';
  import { getDetailFormSchemas } from '../../hooks/auto/useAutoForm';
  import { getLinkTableColumns, transLinkTableRecord } from '../../extend/linkTable/useLinkTable';

  const baseUrl = '/online/cgform/api/subform';
  export default {
    name: 'OnlineSubFormDetail',
    components: {
      BasicForm,
      Loading,
      DetailForm,
    },
    props: {
      properties: {
        type: Object,
        required: true,
      },
      mainId: {
        type: String,
        default: '',
      },
      table: {
        type: String,
        default: '',
      },
      formTemplate: {
        type: Number,
        default: 1,
      },
    },
    emits: ['formChange'],
    setup(props) {
      // 表单是否渲染完成
      const formRendered = ref(false);
      const { createMessage: $message } = useMessage();
      const tableName = ref('');
      const subFormData = ref<any>({});
      const { detailFormSchemas, createFormSchemas, formSpan } = getDetailFormSchemas(props);

      watch(
        () => props.table,
        () => {
          tableName.value = props.table;
        },
        { immediate: true }
      );

      //监听配置改变事件
      watch(
        () => props.properties,
        () => {
          //重新渲染表单
          console.log('主表properties改变', props.properties);
          formRendered.value = false;
          createFormSchemas(props.properties);
          formRendered.value = true;
        },
        { deep: true, immediate: true }
      );

      //监听主表数据ID
      watch(
        () => props.mainId,
        () => {
          //重新加载子表数据
          console.log('主表ID改变', props.mainId);
          // 此处延迟100毫秒是为了让properties的监听先执行
          setTimeout(() => {
            resetSubForm();
          }, 100);
        },
        { immediate: true }
      );

      /**
       * 当主表数据ID发生改变，子表重现获取数据
       * @returns {Promise<void>}
       */
      async function resetSubForm() {
        await getRefPromise(formRendered);
        subFormData.value = {};
        const { table, mainId } = props;
        if (!table || !mainId) {
          return;
        }
        // update-begin--author:liaozhiyang---date:20260413---for：【QQYUN-14951】一对一他表字段详情没值
        let data: any = (await loadData(table, mainId)) || {};
        await fillLinkTableFields(data);
        subFormData.value = data;
        // update-end--author:liaozhiyang---date:20260413---for：【QQYUN-14951】一对一他表字段详情没值
      }
      // update-begin--author:liaozhiyang---date:20260413---for：【QQYUN-14951】一对一他表字段详情没值
      /**
       * 详情页一对一子表中，根据关联记录字段(link_table)的值查询关联数据，自动填充他表字段(link_table_field)的值
       */
      async function fillLinkTableFields(data) {
        const schemas = detailFormSchemas.value;
        for (const schema of schemas) {
          if (schema.view === 'link_table' && (schema as any).linkFields?.length > 0) {
            const fieldValue = data[schema.field];
            if (fieldValue) {
              const linkFields = (schema as any).linkFields as string[];
              const valueField = (schema as any).dictCode || 'id';
              const vals = String(fieldValue).split(',');
              const selectFields = [...new Set([valueField, ...linkFields.map((item) => item.split(',')[1]).filter(Boolean)])].join(',');
              const params = {
                pageSize: vals.length,
                pageNo: 1,
                superQueryMatchType: 'and',
                superQueryParams: encodeURI(JSON.stringify([{ field: valueField, rule: 'in', val: fieldValue }])),
              };
              try {
                // update-begin--author:liusq---date:20260811---for：【online一对一详情】他表字段复用关联记录转换逻辑
                const linkTableName = (schema as any).dictTable;
                const [result, columns] = await Promise.all([
                  defHttp.get({ url: '/online/cgform/api/getData/' + linkTableName, params }),
                  getLinkTableColumns(linkTableName, selectFields),
                ]);
                const records = result?.records || [];
                records.forEach((record) => transLinkTableRecord(record, columns?.columns, columns?.dictOptions));
                for (const linkField of linkFields) {
                  const [formField, tableField] = linkField.split(',');
                  if (records.length > 0) {
                    data[formField] = records.map((r: any) => r[tableField] ?? '').join(',');
                  } else {
                    data[formField] = '';
                  }
                }
                // update-end--author:liusq---date:20260811---for：【online一对一详情】他表字段复用关联记录转换逻辑
              } catch (e) {
                console.warn('填充他表字段失败:', e);
              }
            }
          }
        }
      }
      // update-end--author:liaozhiyang---date:20260413---for：【QQYUN-14951】一对一他表字段详情没值
      

      async function loadData(table, mainId) {
        let url = `${baseUrl}/${table}/${mainId}`;
        return new Promise((resolve, reject) => {
          defHttp.get({ url }, { isTransformResponse: false }).then((res) => {
            console.log(res);
            if (res.success) {
              resolve(res.result);
            } else {
              reject(res.message);
            }
          });
        }).catch((e) => {
          console.warn('子表获取数据失败:', e);
          return Promise.resolve({});
        });
      }

      return {
        detailFormSchemas,
        subFormData,
        formSpan,
      };
    },
  };
</script>

<style scoped></style>
