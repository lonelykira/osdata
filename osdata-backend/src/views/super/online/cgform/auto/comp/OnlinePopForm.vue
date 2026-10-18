<template>
  <div :id="tableName + '_form'" class="onlinePopFormWrap" :class="[`formTemplate_${formTemplate}`]">
    <BasicForm ref="onlineFormRef" @register="registerForm" />
  </div>
</template>

<script lang="ts">
  import { useMessage } from '/@/hooks/web/useMessage';
  import { computed, ref, unref, nextTick, toRaw, reactive, inject } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { SUBMIT_FLOW_KEY, VALIDATE_FAILED } from '../../types/onlineRender';
  import { defHttp } from '/@/utils/http/axios';
  import { pick } from 'lodash-es';
  import { useFormItems, getRefPromise, useOnlineFormContext } from '../../hooks/auto/useAutoForm';
  import { Loading } from '/@/components/Loading';
  import { useEnhance } from '../../hooks/auto/useEnhance';
  import OnlineSubForm from './OnlineSubForm.vue';
  import { loadFormFieldsDefVal } from '../../util/FieldDefVal';
  import { getToken } from '/@/utils/auth';
  import { goJmReportViewPage } from '/@/utils'
  import { PrinterOutlined, DiffOutlined, FormOutlined } from '@ant-design/icons-vue';
  import { useModal } from '/@/components/Modal';
  import { Method } from 'axios';
  import { isObject } from '/@/utils/is';

  const urlObject = {
    optPre: '/online/cgform/api/form/',
    urlButtonAction: '/online/cgform/api/doButton',
  };
  export default {
    name: 'OnlinePopForm',
    components: {
      BasicForm,
      Loading,
      OnlineSubForm,
      PrinterOutlined,
      DiffOutlined,
      FormOutlined
    },
    props: {
      id: {
        type: String,
        default: '',
      },
      formTemplate: {
        type: Number,
        default: 1,
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      isTree: {
        type: Boolean,
        default: false,
      },
      pidField: {
        type: String,
        default: '',
      },
      submitTip:{
        type: Boolean,
        default: true
      },
      modalClass:{
        type: String,
        default: '',
      },
      //是否发送请求-即表单的保存/编辑请求，false则只将表单数据抛出去
      request: {
        type: Boolean,
        default: true
      },
      // 是否是vxeTable上方按钮点击打开的表单数据
      isVxeTableData: {
        type: Boolean,
        default: false
      }
    },
    emits: ['success', 'rendered', 'dataChange'],
    setup(props, { emit }) {
      console.log('onlineForm-setup》》');
      const { createMessage: $message } = useMessage();

      const [registerVxeFormModal, { openModal:openVxeFormModal }] = useModal();
      const vxeTableId = ref('');
      // 表单ref
      const onlineFormRef = ref(null);
      const single = ref(true);
      // 加载状态
      const loading = ref(false);
      const tableType = ref(1);
      // 表单提交且提交流程
      const submitFlowFlag = ref(false);
      const isUpdate = ref(false);

      /**
       * online表单扩展配置
       */
      const onlineExtConfigJson = reactive({
        reportPrintShow: 0,
        reportPrintUrl: '',
        joinQuery: 0,
        modelFullscreen: 0,
        modalMinWidth: '',
      });

      const { onlineFormContext, resetContext } = useOnlineFormContext();

      // update-begin--author:liaozhiyang---date:20260519---for：【QQYUN-9773】online一对多内部弹窗新增js增强
      // 由父 OnlineForm 注入；非弹窗使用场景（如关联记录单独弹窗）下为 null，所有打通逻辑自动 no-op
      const parentOnlineForm: any = inject('parentOnlineForm', null);
      // update-end--author:liaozhiyang---date:20260519---for：【QQYUN-9773】online一对多内部弹窗新增js增强

      const {
        formSchemas,
        defaultValueFields,
        changeDataIfArray2String,
        tableName,
        dbData,
        checkOnlyFieldValue,
        hasSubTable,
        subTabInfo,
        refMap,
        subDataSource,
        baseColProps,
        createFormSchemas,
        fieldDisplayStatus,
        labelCol,
        wrapperCol,
        labelWidth
      } = useFormItems(props, onlineFormRef);
      let { EnhanceJS, initCgEnhanceJs } = useEnhance(onlineFormContext, false);

      //表单配置
      const [registerForm, { setProps, validate, resetFields, setFieldsValue, updateSchema, getFieldsValue, scrollToField }] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        baseColProps: baseColProps,
        // update-begin--author:liaozhiyang---date:20240329---for：【QQYUN-7872】online表单label较长优化
        labelWidth,
        // update-end--author:liaozhiyang---date:20240329---for：【QQYUN-7872】online表单label较长优化
        // update-begin--author:liaozhiyang---date:20240105---for：【QQYUN-7499】多列风格富文本、markdown增加独占一行功能
        labelCol,
        wrapperCol
        // update-end--author:liaozhiyang---date:20240105---for：【QQYUN-7499】多列风格富文本、markdown增加独占一行功能
      });

      // 表单禁用
      const onlineFormDisabled = ref(false);
      function handleFormDisabled() {
        let flag = props.disabled;
        onlineFormDisabled.value = flag;
        setProps({ disabled: flag });
      }

      /**
       * status: 是否是修改页面
       * record: 列表页面的行数据
       * param： 树形列表添加子节点 传入的父级节点id
       * */
      async function show(status, record, param) {
        console.log('onlinepopform新增编辑进入表单》》form', record);
        await resetFields();
        dbData.value = '';
        let flag = unref(status);
        isUpdate.value = flag;
        if (flag) {
          // 编辑页面
          await edit(record);
        }
        await nextTick(() => {
          if (!flag && param) {
            //如果是新增页面 且 param传入有值 需要设置表单
            setFieldsValue(param);
          }
          handleDefaultValue();
          // 所有信息加载完毕 触发loaded事件
          handleCgButtonClick('js', 'loaded');
          replayParentMainOnlChange();
          //处理表单的禁用效果
          handleFormDisabled();
        });
      }
     

      /**
       * 当前表单默认值逻辑-进入新增页面触发
       */
      function handleDefaultValue() {
        if (unref(isUpdate) === false) {
          let fieldProperties = toRaw(defaultValueFields[tableName.value]);
          loadFormFieldsDefVal(fieldProperties, (values) => {
            setFieldsValue(values);
          });
        }
      }

      async function edit(record) {
        // 查询数据库
        let formData:any = await getFormData(record.id);
        if(!formData || Object.keys(formData).length==0){
          //没有查询出数据
          formData = {...toRaw(record)}
        }
        dbData.value = Object.assign({}, formData);
        //表单赋值
        let arr = realFormFieldNames.value;
        let values = pick(formData, ...arr);
        // 如果是vxetable上方按钮打开的表单，那么表单值以record为主，而不是数据库查询的数据,否则第一次修改后第二次打开表单，表单值和数据库一致但是和当前页面不一致
        if(props.isVxeTableData === true){
          values = Object.assign({},values, record)
        }
        await setFieldsValue(values);
        // editSubVxeTableData(formData);
      }

      function editSubVxeTableData(record) {
        if (!record) {
          // 新增页面需要清空子表数据
          record = {};
        }
        let keys = Object.keys(subDataSource.value);
        if (keys && keys.length > 0) {
          let obj = {};
          for (let key of keys) {
            obj[key] = record[key] || [];
          }
          subDataSource.value = obj;
        }
      }

      let realFormFieldNames = computed(() => {
        let arr = formSchemas.value;
        let names = [];
        for (let a of arr) {
          names.push(a.field);
        }
        return names;
      });

      function getFormData(dataId) {
        let url = `${urlObject.optPre}${props.id}/${dataId}`;
        return new Promise((resolve, reject) => {
          defHttp
            .get({ url }, { isTransformResponse: false })
            .then((res) => {
              if (res.success) {
                resolve(res.result);
              } else {
                reject();
                $message.warning(res.message);
              }
            })
            .catch(() => {
              reject();
            });
        });
      }

      // 渲染表单
      async function createRootProperties(data) {
        tableType.value = data.head.tableType;
        tableName.value = data.head.tableName;
        single.value = data.head.tableType == 1;
        handleExtConfigJson(data.head.extConfigJson);

        createFormSchemas(data.schema.properties, data.schema.required, checkOnlyFieldValue, onlineExtConfigJson);
        EnhanceJS = initCgEnhanceJs(data.enhanceJs);
        emit('rendered', onlineExtConfigJson);

        //监听表单改变事件
        let formRefObject:any = await getRefPromise(onlineFormRef);
        formRefObject.$formValueChange = (field, value, changeFormData) => {
          onValuesChange(field, value);
          if(changeFormData){
            //如果存在其他表单控件的数据，直接设置该值
            setFieldsValue(changeFormData)
          }
        };
      }

      /**
       * 处理扩展配置
       */
      function handleExtConfigJson(jsonStr) {
        let extConfigJson = { reportPrintShow: 0, reportPrintUrl: '', joinQuery: 0, modelFullscreen: 1, modalMinWidth: '', formLabelLength: null };
        if (jsonStr) {
          extConfigJson = JSON.parse(jsonStr);
        }
        Object.keys(extConfigJson).map((k) => {
          onlineExtConfigJson[k] = extConfigJson[k];
        });
      }
      

      function handleSubmit() {
        if (single.value === true) {
          handleSingleSubmit();
        } else {
          handleOne2ManySubmit();
        }
      }

      function handleOne2ManySubmit() {
        validateAll().then((formData) => {
          handleApplyRequest(formData);
        });
      }

      // 触发所有表单验证
      function validateAll() {
        let temp = {};
        return new Promise((resolve, reject) => {
          // 验证主表表单
          validate().then(
            (values) => resolve(values),
            ({ errorFields }) => {
              reject({
                code: VALIDATE_FAILED,
                key: tableName.value,
                // 滚动到未通过校验的字段上
                scrollToField: () => errorFields[0] && scrollToField(errorFields[0].name, { behavior: 'smooth', block: 'center' }),
              });
            }
          );
        })
          .then((result) => {
            Object.assign(temp, changeDataIfArray2String(result));
            return validateSubTableFields();
          })
          .then((allTableData) => {
            Object.assign(temp, allTableData);
            return Promise.resolve(temp);
          })
          .catch((e) => {
            if (e === VALIDATE_FAILED || e?.code === VALIDATE_FAILED) {
              $message.warning('校验未通过');
              if (e.key) {
                changeTab(e.key);
                if (e.scrollToField) {
                  setTimeout(() => e.scrollToField(), 150)
                }
              }
            } else {
              console.error(e);
            }
            return Promise.reject(null);
          });
      }

      /**
       * 切换tab到出现校验错误的页面
       * */
      function changeTab(key) {
        let arr = subTabInfo.value;
        for (let i = 0; i < arr.length; i++) {
          if (key == arr[i].key) {
            subActiveKey.value = i + '';
            break;
          }
        }
      }

      // 验证子表
      function validateSubTableFields() {
        return new Promise(async (resolve, reject) => {
          let subData = {};
          try {
            let arr = subTabInfo.value;
            for (let i = 0; i < arr.length; i++) {
              let key = arr[i].key;
              let instance = refMap[key].value;
              // 兼容写法:如果取到的是一个数组类型，则取第一个元素
              if (instance instanceof Array) {
                instance = instance[0];
              }
              if (arr[i].relationType == 1) {
                try {
                  let subFormData = await instance.getAll();
                  subData[key] = [];
                  subData[key].push(subFormData);
                } catch (e) {
                  return reject({code: VALIDATE_FAILED, key, ...e});
                }
              } else {
                let errMap = await instance.fullValidateTable();
                if (errMap) {
                  return reject({code: VALIDATE_FAILED, key});
                }
                subData[key] = instance.getTableData();
              }
            }
          } catch (e) {
            reject(e);
          }
          resolve(subData);
        });
      }

      /**
       * 表单提交-单表
       */
      async function handleSingleSubmit() {
        try {
          let values = await validate();
          values = Object.assign({}, dbData.value, values);
          values = changeDataIfArray2String(values);
          loading.value = true;
          handleApplyRequest(values);
        } catch (error) {
          // update-begin--author:liaozhiyang---date:20240524---for：【TV360X-420】关联记录校验不通过的项在可视区外时点击保存没任何效果
          if (isObject(error)) {
            const errorFields = error.errorFields;
            if (errorFields?.length && errorFields[0].errors) {
              $message.warning(errorFields[0].errors[0]);
              scrollToField(errorFields[0].name, { behavior: 'smooth', block: 'center' });
            }
          }
          console.log(error);
          // update-end--author:liaozhiyang---date:20240524---for：【TV360X-420】关联记录校验不通过的项在可视区外时点击保存没任何效果
        } finally {
          loading.value = false;
        } 
      }

      //提交数据前 先走一下自定义的JS校验
      function handleApplyRequest(formData) {
        customBeforeSubmit(context, formData)
          .then(() => {
            doApplyRequest(formData);
          })
          .catch((msg) => {
            $message.warning(msg);
          });
      }


      function triggleChangeValues(values, id, target) {
        if (id && target) {
          if (target.setValues) {
            //一对一子表
            target.setValues(values);
          } else {
            //一对多子表
            target.setValues([
              {
                rowKey: id,
                values: values,
              },
            ]);
          }
        } else {
          //主表
          setFieldsValue(values);
        }
      }
      function triggleChangeValue(field, value) {
        let obj = {};
        obj[field] = value;
        setFieldsValue(obj);
      }

      // 一对多子表Tab的Key，用于校验未通过时自动跳转
      const subActiveKey = ref('0');
      const subFormHeight = ref(300);
      // 子表表格高度
      // 【VUEN-803】一对多子表固定340高度，修复自定义列组件被遮挡的问题
      const subTableHeight = ref(340);

      function getSubTableForeignKeyValue(key) {
        if (isUpdate.value === true) {
          let formData = dbData.value;
          return getValueIgnoreCase(formData, key);
        }
        return '';
      }

      /**
       * VUEN-1056 30、生成的一对多，编辑的时候，子表数据挂不上
       */
      function getValueIgnoreCase(data, key) {
        if (data) {
          let temp = data[key];
          if (!temp && temp !== 0) {
            temp = data[key.toLowerCase()];
            if (!temp && temp !== 0) {
              temp = data[key.toUpperCase()];
            }
          }
          return temp;
        }
        return '';
      }

      //处理一对一子表的表单改变事件
      function handleSubFormChange(valueObj, tableKey) {
        if (EnhanceJS && EnhanceJS[tableKey + '_onlChange']) {
          let tableChangeObj = EnhanceJS[tableKey + '_onlChange']();
          let columnKey = Object.keys(valueObj)[0];
          if (tableChangeObj[columnKey]) {
            let subRef = refMap[tableKey].value;
            if (subRef instanceof Array) {
              subRef = subRef[0];
            }
            let formEvent = subRef.getFormEvent();
            let event = {
              column: { key: columnKey },
              value: valueObj[columnKey],
              ...formEvent,
            };
            tableChangeObj[columnKey].call(onlineFormContext, onlineFormContext, event);
          }
        }
      }

      //处理一对多子表的改变事件
      function handleValueChange(event, tableKey) {
        if (EnhanceJS && EnhanceJS[tableKey + '_onlChange']) {
          let tableChangeObj = EnhanceJS[tableKey + '_onlChange'](onlineFormContext);
          if (tableChangeObj[event.column.key]) {
            tableChangeObj[event.column.key].call(onlineFormContext, onlineFormContext, event);
          }
        }
      }

      // 当行编辑新增完成后触发的事件
      function handleAdded(sub, event) {
        console.log('handleAdded', sub, event)
        //update-begin-author:taoyan date:2022-6-26 for: 控制台警告 这里直接调用函数 不触发事件了
       // event.target.emit('executeFillRule', event);
        //update-end-author:taoyan date:2022-6-26 for: 控制台警告 这里直接调用函数 不触发事件了
      }

      function getSubTableAuthPre(table) {
        return 'online_' + table + ':';
      }

      //监听表单改变事件
      async function onValuesChange(columnKey, value) {
        //console.log('columnKey-value', `${columnKey}-${value}`)
        // 将老数据和新数据比较 如果不同 往外抛出改变事件
        let oldFormData = dbData.value;
        if(oldFormData[columnKey]!=value){
          emit('dataChange', columnKey);
        }

        if (!columnKey) {
          return false;
        }

        // 1) 弹窗自己（子表 head）的 enhanceJs.onlChange —— 保留原有行为
        if (EnhanceJS && EnhanceJS['onlChange']) {
          //let tableChangeObj = EnhanceJS["onlChange"].call(onlineFormContext);
          let tableChangeObj = EnhanceJS['onlChange']();
          if (tableChangeObj[columnKey]) {
            let formData = await getFieldsValue();
            let event = {
              row: formData,
              column: { key: columnKey },
              value: value,
            };
            tableChangeObj[columnKey].call(onlineFormContext, onlineFormContext, event);
          }
        }

        await triggerParentSubOnlChange(columnKey, value);
      }

      // update-begin--author:liaozhiyang---date:20260519---for：【QQYUN-9773】online一对多内部弹窗新增js增强
      // 父组件用 provide 注入 onlineFormContext + enhanceJs + 当前子表 key，弹窗 inject
      // 后做两件事：
      //   ① 弹窗字段变化   → 调主表「子表名_onlChange[字段]」（与 VxeTable 行内编辑等价）
      //   ② 弹窗打开 loaded → 重放主表「onlChange[已选字段]」（让"主表分类→当前子表 options"联动立刻生效）
      // 两条路径都用 wrapParentCtxForPop 包装 this：写"当前行字段"的 API 自动落到弹窗，避免污染主表。
      function wrapParentCtxForPop(parentCtx, currentSubKey) {
        const popOverrides: Record<string, Function> = {
          triggleChangeValues: (values, rowId?, vxeTarget?) => {
            if (vxeTarget === undefined) return setFieldsValue(values);
            return parentCtx.triggleChangeValues?.(values, rowId, vxeTarget);
          },
          changeSubTableOptions: (tableName, field, options) => {
            if (tableName === currentSubKey) return changeOptions(field, options);
            return parentCtx.changeSubTableOptions?.(tableName, field, options);
          },
          changeRemoteOptions: (param) => {
            if (param?.subTableName !== currentSubKey) {
              return parentCtx.changeRemoteOptions?.(param);
            }
            // 等价于父表"单表/主表分支"：用本弹窗 updateSchema 改远程下拉的 dict
            const _dict = String(param.dict || '').split(',').map(encodeURIComponent).join(',');
            const schemaPatch: any = { field: param.field, componentProps: { dict: _dict } };
            if (param.label) schemaPatch.label = param.label;
            updateSchema(schemaPatch);
          },
        };

        return new Proxy(parentCtx, {
          get: (target, prop: string) =>
            prop in popOverrides ? popOverrides[prop] : Reflect.get(target, prop),
        });
      }
      /** 弹窗字段变化时，调用主表的「子表名_onlChange[字段]」—— 与 VxeTable 行内编辑等价 */
      async function triggerParentSubOnlChange(columnKey, value) {
        const subKey = unref(parentOnlineForm?.subTableKey);
        if (!subKey) return;

        const subOnlChange = parentOnlineForm.getEnhanceJs?.()?.[`${subKey}_onlChange`];
        if (typeof subOnlChange !== 'function') return;

        const wrappedCtx = wrapParentCtxForPop(parentOnlineForm.context, subKey);
        const handler = subOnlChange(wrappedCtx)?.[columnKey];
        if (typeof handler !== 'function') return;

        const event = {
          row: await getFieldsValue(),
          column: { key: columnKey },
          value,
          target: undefined,   // 弹窗模式标记：无 VxeTable 实例
          type: undefined,
        };
        try {
          handler.call(wrappedCtx, wrappedCtx, event);
        } catch (e) {
          console.error(`[OnlinePopForm] 调用主表 ${subKey}_onlChange.${columnKey} 失败：`, e);
        }
      }

      /**
       * 弹窗打开时重放一次主表「onlChange[主表已选字段]」。
       */
      function replayParentMainOnlChange() {
        const subKey = unref(parentOnlineForm?.subTableKey);
        if (!subKey) return;

        const mainOnlChange = parentOnlineForm.getEnhanceJs?.()?.['onlChange'];
        if (typeof mainOnlChange !== 'function') return;

        let handlers;
        try {
          handlers = mainOnlChange();
        } catch (e) {
          console.error('[OnlinePopForm] 解析主表 onlChange 失败：', e);
          return;
        }
        if (!handlers || typeof handlers !== 'object') return;

        const parentCtx = parentOnlineForm.context;
        const wrappedCtx = wrapParentCtxForPop(parentCtx, subKey);
        const mainValues = parentCtx?.getFieldsValue?.() || {};

        for (const [field, handler] of Object.entries(handlers)) {
          const val = mainValues[field];
          // 主表该字段没值就不重放（避免空触发，也跳过新增模式下未填的字段）
          if (val === undefined || val === null || val === '') continue;
          if (typeof handler !== 'function') continue;

          const event = { row: mainValues, column: { key: field }, value: val, target: undefined, type: undefined };
          try {
            (handler as Function).call(wrappedCtx, wrappedCtx, event);
          } catch (e) {
            console.error(`[OnlinePopForm] 重放主表 onlChange.${field} 失败：`, e);
          }
        }
      }
      // update-end--author:liaozhiyang---date:20260519---for：【QQYUN-9773】online一对多内部弹窗新增js增强

      // 自定义按钮 增强触发事件
      function handleCgButtonClick(optType, buttonCode) {
        if ('js' == optType) {
          if (EnhanceJS && EnhanceJS[buttonCode]) {
            EnhanceJS[buttonCode].call(onlineFormContext, onlineFormContext);
          }
        } else if ('action' == optType) {
          let formData = dbData.value;
          let params = {
            formId: props.id,
            buttonCode: buttonCode,
            dataId: formData.id,
            uiFormData: Object.assign({}, formData),
          };
          //console.log("自定义按钮请求后台参数：",params)
          defHttp
            .post(
              {
                url: `${urlObject.urlButtonAction}`,
                params,
              },
              { isTransformResponse: false }
            )
            .then((res) => {
              if (res.success) {
                $message.success('处理完成!');
              } else {
                $message.warning('处理失败!');
              }
            });
        }
      }

      // --------------------增强-------------------------

      /**
       * 清除子表数据
       * @param tbname
       */
      function clearSubRows(tbname) {
        let instance = refMap[tbname].value;
        let rows = [...instance.getNewDataWithId(), ...subDataSource.value[tbname]];
        if (!rows || rows.length == 0) {
          return false;
        }
        let ids = [];
        for (let i of rows) {
          ids.push(i.id);
        }
        instance.removeRowsById(ids);
      }

      /**
       * 添加子表数据
       * @param tbname
       * @param rows 可以是数组也可以是对象
       * @returns {boolean}
       */
      function addSubRows(tbname, rows) {
        if (!rows) {
          return false;
        }
        let instance = refMap[tbname].value;
        if (typeof rows == 'object') {
          instance.addRows(rows, true);
        } else {
          $message.error('添加子表数据,参数不识别!');
        }
      }

      /**
       * 先删除后添加
       * @param tbname
       * @param rows
       */
      function clearThenAddRows(tbname, rows) {
        clearSubRows(tbname);
        addSubRows(tbname, rows);
      }

      /**
       * 修改下拉框的下拉选项
       * @param field
       * @param options
       */
      function changeOptions(field, options) {
        if (!options && options.length <= 0) {
          options = [];
        }
        options.map((item) => {
          if (!item.hasOwnProperty('label')) {
            item['label'] = item.text;
          }
        });
        updateSchema({
          field,
          componentProps: {
            options,
          },
        });
      }

      /**
       * 表单提交前事件
       * @param that
       * @param formData
       * @returns {Promise<void>|*}
       */
      function customBeforeSubmit(that, formData) {
        if (EnhanceJS && EnhanceJS['beforeSubmit']) {
          return EnhanceJS['beforeSubmit'](that, formData);
        } else {
          return Promise.resolve();
        }
      }

      /**
       * 处理自定义弹框 字段的显示隐藏
       * @param show
       * @param hide
       */
      function handleCustomFormSh(show, hide) {
        let plain = toRaw(fieldDisplayStatus);
        if (show && show.length > 0) {
          Object.keys(plain).map((k) => {
            if (!k.endsWith('_load') && show.indexOf(k) < 0) {
              fieldDisplayStatus[k] = false;
            }
          });
        } else if (hide && hide.length > 0) {
          Object.keys(plain).map((k) => {
            if (hide.indexOf(k) >= 0) {
              fieldDisplayStatus[k] = false;
            }
          });
        }
      }

      async function handleCustomFormEdit(record) {
        console.log('自定义弹窗打开online表单》》form', record);
        await resetFields();
        dbData.value = '';
        isUpdate.value = true;
        // 编辑数据
        await edit(record);
        await nextTick(() => {
          // 所有信息加载完毕 触发loaded事件
          handleCgButtonClick('js', 'loaded');
        });
      }

      /**
       * VUEN-1036
       * 获取子表的实例对象 可以直接调用子表的方法
       */
      function getSubTableInstance(tableName) {
        let instance = refMap[tableName].value;
        if (instance instanceof Array) {
          instance = instance[0];
        }
        return instance;
      }
      
      //跳转至积木报表页面
      function onOpenReportPrint(){
        let url = onlineExtConfigJson.reportPrintUrl;
        let id = dbData.value.id;
        let token = getToken();
        goJmReportViewPage(url, id, token)
      }
      
      //----
      function openSubFormModalForAdd(sub){
        console.log(sub)
        vxeTableId.value = sub.id;
        openVxeFormModal(true, )
      }

      function openSubFormModalForEdit(sub){
        console.log(sub)
      }

      /**
       * 保存数据
       * @param formData
       */
      function doApplyRequest(formData) {
        // 数组没有元素直接置空
        Object.keys(formData).map((key) => {
          if (Array.isArray(formData[key])) {
            if (formData[key].length == 0) {
              formData[key] = '';
            }
          }
        });
        console.log('提交pop表单数据》》》form:', formData);
        if(props.request == false){
          emit('success', formData);
        }else{
          let url = `${urlObject.optPre}${props.id}?tabletype=${tableType.value}`;

          console.log('提交pop表单url》》》url:', url);
          // 如果需要提交流程 需要额外设置一个参数
          if (submitFlowFlag.value === true) {
            formData[SUBMIT_FLOW_KEY] = 1;
          }
          let method:Method = isUpdate.value === true ? 'put' : 'post';
          defHttp.request({ url, method, params: formData }, { isTransformResponse: false })
            .then((res) => {
              //console.log('表单提交完成', res)
              if (res.success) {
                if (res.result) {
                  //formData[SUBMIT_FLOW_ID] = res.result;
                  if(!formData.id){
                    formData['id'] = res.result;
                  }
                }
                //刷新列表
                emit('success', formData);
                dbData.value = formData;
                isUpdate.value = true;
                $message.success('操作成功!')
                // 工单申请提交的表单也会走这个逻辑，保存成功不需要提示信息
              } else {
                $message.warning(res.message);
              }
            })
            .finally(() => {
              loading.value = false;
            });
        }
      }


      /**
       * 数据恢复到 dbdata
       */
      async function recoverFormData(){
        let record = dbData.value;
        let arr = realFormFieldNames.value;
        let values = pick(record, ...arr);
        if(record){
          await setFieldsValue(values);
        }else{
          let temp:any = {}
          for(let key of arr){
            temp[key] = ''
          }
          await setFieldsValue(temp);
        }
      }

      let context = {
        tableName,
        loading,
        subActiveKey,
        onlineFormRef,
        getFieldsValue,
        setFieldsValue,
        submitFlowFlag,
        subFormHeight,
        subTableHeight,
        refMap,
        triggleChangeValues,
        triggleChangeValue,
        sh: fieldDisplayStatus,
        clearSubRows,
        addSubRows,
        clearThenAddRows,
        changeOptions,
        isUpdate,
        getSubTableInstance,
      };
      resetContext(context);

      return {
        //主表
        tableName,
        onlineFormRef,
        registerForm,
        loading,

        //子表
        subActiveKey,
        hasSubTable,
        subTabInfo,
        refMap,

        //一对一子表
        subFormHeight,
        getSubTableForeignKeyValue,
        isUpdate,
        handleSubFormChange,

        //一对多子表
        subTableHeight,
        onlineFormDisabled,
        subDataSource,
        getSubTableAuthPre,
        handleAdded,
        handleValueChange,
        openSubFormModalForAdd,
        openSubFormModalForEdit,
        registerVxeFormModal,
        vxeTableId,

        //父组件调用
        show,
        createRootProperties,
        handleSubmit,
        sh: fieldDisplayStatus,
        handleCgButtonClick,
        handleCustomFormSh,
        handleCustomFormEdit,
        //跳转
        dbData,
        onOpenReportPrint,
        onlineExtConfigJson,
        recoverFormData
      };
    },
  };
</script>

<style lang="less" scoped>
  .onlinePopFormWrap {
    // update-begin--author:liaozhiyang---date:20240429---for：【QQYUN-7632】 label栅格改成labelwidth固宽
    padding: 0 1.5%;
    // update-begin--author:liaozhiyang---date:20240506---for：【QQYUN-9229】间隔调整
    &.formTemplate_1 {
      > form {
        padding-left: 5%;
        padding-right: 5%;
      }
    }
    &.formTemplate_2 {
      > form {
        padding-left: 1%;
        padding-right: 1%;
      }
    }
    // update-end--author:liaozhiyang---date:20240506---for：【QQYUN-9229】间隔调整
    :deep(.ant-form) {
      > .ant-row {
        > .ant-col {
          padding: 0 6px;
        }
      }
    }
    // update-end--author:liaozhiyang---date:20240429---for：【QQYUN-7632】 label栅格改成labelwidth固宽
  }
</style>
