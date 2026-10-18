<template>
  <BasicForm ref="onlineFormRef" @register="registerForm" />
</template>

<script>
  import { useMessage } from '/@/hooks/web/useMessage';
  import { computed, defineComponent, ref, unref, watch, nextTick, toRaw } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { SUBMIT_FLOW_ID, SUBMIT_FLOW_KEY, VALIDATE_FAILED } from '../../types/onlineRender';
  import { defHttp } from '/@/utils/http/axios';
  import { pick } from 'lodash-es';
  import { useFormItems, getRefPromise } from '../../hooks/auto/useAutoForm';
  import { Loading } from '/@/components/Loading';
  import { loadFormFieldsDefVal } from '../../util/FieldDefVal';

  const urlObject = {
    optPre: '/online/cgform/api/form/',
    urlButtonAction: '/online/cgform/api/doButton',
  };
  const baseUrl = '/online/cgform/api/subform';
  export default {
    name: 'OnlineSubForm',
    components: {
      BasicForm,
      Loading,
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
      requiredFields: {
        type: Array,
        default: [],
      },
      isUpdate: {
        type: Boolean,
        default: false,
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      id: {
        type: String,
        default: '',
      },
      // update-begin--author:liusq---date:20260731---for:【工单授权BPM】一对一子表接收主表传来的 modalClass，否则下拉/日期类组件的 popup 会挂到外层同名 modal 上导致点击无反应
      modalClass: {
        type: String,
        default: '',
      },
      // update-end--author:liusq---date:20260731---for:【工单授权BPM】一对一子表接收主表传来的 modalClass，否则下拉/日期类组件的 popup 会挂到外层同名 modal 上导致点击无反应
    },
    emits: ['formChange'],
    setup(props, { emit }) {
      console.log('进入online子表表单页面》》》》' + props.table);

      // 表单ref
      const onlineFormRef = ref(null);
      // 表单是否渲染完成
      const formRendered = ref(false);
      const { createMessage: $message } = useMessage();
      const {
        formSchemas,
        defaultValueFields,
        changeDataIfArray2String,
        tableName,
        dbData,
        checkOnlyFieldValue,
        fieldDisplayStatus,
        createFormSchemas,
        baseColProps,
        labelCol,
        wrapperCol,
        labelWidth,
      } = useFormItems(props, onlineFormRef);
      //表单配置
      const [registerForm, { setProps, validate, resetFields, setFieldsValue, getFieldsValue, updateSchema, scrollToField }] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        baseColProps: baseColProps,
        // update-begin--author:liaozhiyang---date:20240429---for：【QQYUN-7632】 label栅格改成labelwidth固宽
        labelWidth,
        // update-end--author:liaozhiyang---date:20240429---for：【QQYUN-7632】 label栅格改成labelwidth固宽
        // update-begin--author:liaozhiyang---date:20240105---for：【QQYUN-7499】多列风格富文本、markdown增加独占一行功能
        labelCol,
        wrapperCol
        // update-end--author:liaozhiyang---date:20240105---for：【QQYUN-7499】多列风格富文本、markdown增加独占一行功能
      });
      const getFormItem = () => {
        return new Promise((resolve, reject) => {
          defHttp.get({ url: `online/cgform/api/getFormItem/${props.id}` }, { isTransformResponse: false }).then((res) => {
            resolve(res.result);
          });
        });
      }
      let extConfigJson;
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
        async (valueObj) => {
          //重新渲染表单
          console.log('主表properties改变', props.properties);
          formRendered.value = false;
          addFormChangeEvent();
          // update-begin--author:liaozhiyang---date:20260318---for:【issues/9414】一对一子表设置label长度不生效
          if (!extConfigJson) {
              try {
              const data = await getFormItem();
              extConfigJson = JSON.parse(data.head.extConfigJson);
              extConfigJson = {
                formLabelLength: extConfigJson.formLabelLength,
                formLabelLengthShow: extConfigJson.formLabelLengthShow,
              };
            } catch (error) {
              console.log(error);
            }
          }
          // update-end--author:liaozhiyang---date:20260318---for:【issues/9414】一对一子表设置label长度不生效
          createFormSchemas(props.properties, props.requiredFields, checkOnlyFieldValue, extConfigJson);
          formRendered.value = true;
        },
        { deep: true, immediate: true }
      );

      //监听主表数据ID
      watch(
        () => props.mainId,
        (valueObj) => {
          //重新加载子表数据
          console.log('主表ID改变', props.mainId);
          // 此处延迟100毫秒是为了让properties的监听先执行
          setTimeout(() => {
            resetSubForm();
          }, 100);
        },
        { immediate: true }
      );

      watch(
        () => props.disabled,
        (val) => {
          setProps({ disabled: val });
        }
      );

      /**
       * 监听表单改变事件
       */
      async function addFormChangeEvent() {
        let formRefObject = await getRefPromise(onlineFormRef);
        formRefObject.$formValueChange = (field, value, changeFormData) => {
          let emitArgument = { [field]: value };
          // update-begin--author:liaozhiyang---date:20260317---for:【QQYUN-9441】online一对多加上关联记录和他表字段
          // 一对一子表 关联记录和他表字段
          if(changeFormData){
            setFieldsValue(changeFormData);
          }
          // update-end--author:liaozhiyang---date:20260317---for:【QQYUN-9441】online一对多加上关联记录和他表字段
          emit('formChange', emitArgument);
        };
      }

      /**
       * 当前表单默认值逻辑-进入新增页面触发
       */
      function handleDefaultValue() {
        if (unref(props.isUpdate) === false) {
          let fieldProperties = toRaw(defaultValueFields[tableName.value]);
          loadFormFieldsDefVal(fieldProperties, (values) => {
            setFieldsValue(values);
          });
        }
      }

      /**
       * 当主表数据ID发生改变，子表重现获取数据
       * @returns {Promise<void>}
       */
      async function resetSubForm() {
        //TODO 填值规则
        // update-begin--author:sunjianlei --- date:20191111 --- for: 每次加载数据的时候都重新执行一遍填值规则 -----------
        //this.$emit('executeFillRule', {form:this.form, target: this})
        // update-end--author:sunjianlei --- date:20191111 --- for: 每次加载数据的时候都重新执行一遍填值规则 -----------
        await getRefPromise(formRendered);
        await resetFields();
        handleDefaultValue();
        const { table, mainId } = props;
        if (!table || !mainId) {
          return;
        }
        try {
          const values = await loadData(table, mainId);
          dbData.value = values;
          // VUEN-1033
          await setFieldsValue(values);
        } catch (error) {
          const message = error?.message || '子表数据加载失败';
          console.error('子表数据加载失败:', error);
          $message.error(message);
        }
      }

      async function loadData(table, mainId) {
        let url = `${baseUrl}/${table}/${mainId}`;
        //console.log("loadData 子表数据 url:", url);
        try {
          const res = await defHttp.get({ url }, { isTransformResponse: false });
          if (!res?.success) {
            throw new Error(res?.message || '子表数据加载失败');
          }
          return res.result;
        } finally {
          //resetFields()
          dbData.value = '';
        }
      }

      function getAll() {
        return new Promise((resolve, reject) => {
          validate()
            .then(() => {
              let formData = getFieldsValue();
              formData = changeDataIfArray2String(formData);
              resolve(formData);
            })
            .catch((e) => {
              if (e.errorFields) {
                e.scrollToField = () => e.errorFields[0] && scrollToField(e.errorFields[0].name, { behavior: 'smooth', block: 'center' });
              }
              reject(e);
            });
        });
      }

      //获取表单事件对象 监听表单改变用到
      function getFormEvent() {
        let row = getFieldsValue();
        if (!row.id) {
          row.id = 'sub-change-temp-id';
        }
        return {
          row,
          target: context,
        };
      }

      //设置表单的值
      function setValues(values) {
        setFieldsValue(values);
      }

      function executeFillRule() {
        let formData = getFieldsValue();
        let fieldProperties = toRaw(defaultValueFields[tableName.value]);
        loadFormFieldsDefVal(fieldProperties, (values) => {
          setFieldsValue(values);
        }, formData);
      }

      // update-begin--author:liaozhiyang---date:20260714---for：【LHZP-249】第二次打开新增弹窗，一对一子表的默认值没了
      async function resetAndApplyDefaultValue() {
        await resetFields();
        let fieldProperties = toRaw(defaultValueFields[tableName.value]);
        loadFormFieldsDefVal(fieldProperties, (values) => {
          setFieldsValue(values);
        });
      }
      // update-end--author:liaozhiyang---date:20260714---for：【LHZP-249】第二次打开新增弹窗，一对一子表的默认值没了

      const context = {
        onlineFormRef,
        baseColProps,
        formSchemas,
        registerForm,
        setFieldsValue,
        getFieldsValue,
        getFormEvent,
        setValues,
        getAll,
        executeFillRule,
        resetAndApplyDefaultValue,
        sh: fieldDisplayStatus,
        resetFields,
        updateSchema,
      };
      return context;
    },
  };
</script>

<style lang="less" scoped>
  // update-begin--author:liaozhiyang---date:20240527---for：【TV360X-263】tab风格一对一子表上传组件有数据没渲染出来
  :deep(.ant-upload-list-item-container) {
    &.ant-motion-collapse {
      height: auto !important;
    }
  }
  // update-end--author:liaozhiyang---date:20240527---for：【TV360X-263】tab风格一对一子表上传组件有数据没渲染出来
</style>
