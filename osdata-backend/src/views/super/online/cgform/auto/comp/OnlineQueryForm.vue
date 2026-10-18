<template>
  <div class="jeecg-basic-table-form-container online-query-form p-0" v-if="formSchemas && formSchemas.length > 0">
    <BasicForm ref="onlineQueryFormRef" @register="registerForm">
      <!-- 范围查询：日期 -->
      <template #groupDate="{ model, field, schema }">
        <!-- update-begin--author:liaozhiyang---date:20240530---for：【TV360X-213】普通查询日期数值组件更换 -->
        <!-- <a-date-picker
          :showTime="false"
          valueFormat="YYYY-MM-DD"
          placeholder="开始日期"
          v-model:value="model[field + '_begin']"
          style="width: calc(50% - 15px);min-width: 100px;"
          v-bind="schema.componentProps"
        ></a-date-picker>
        <span class="group-query-string">~</span>
        <a-form-item-rest>
          <a-date-picker
            :showTime="false"
            valueFormat="YYYY-MM-DD"
            placeholder="结束日期"
            v-model:value="model[field + '_end']"
            style="width: calc(50% - 15px);min-width: 100px;"
            v-bind="schema.componentProps"
          ></a-date-picker>
        </a-form-item-rest> -->
        <a-range-picker :style="{ width: '100%' }" v-model:value="model[field]" v-bind="schema.componentProps" :placeholder="getGroupDatePlaceholder(schema.componentProps)" valueFormat="YYYY-MM-DD"/>
        <!-- update-end--author:liaozhiyang---date:20240530---for：【TV360X-213】普通查询日期数值组件更换 -->
      </template>

      <!-- 范围查询：时间 -->
      <template #groupDatetime="{ model, field }">
        <!-- update-begin--author:liaozhiyang---date:20240530---for：【TV360X-213】普通查询日期数值组件更换 -->
        <!-- <a-date-picker
          :showTime="true" 
          valueFormat="YYYY-MM-DD HH:mm:ss"
          placeholder="开始时间"
          v-model:value="model[field + '_begin']"
          style="min-width: 100px;width: calc(50% - 15px);"
        ></a-date-picker>
        <span class="group-query-string">~</span>
        <a-form-item-rest>
          <a-date-picker
            :showTime="true"
            valueFormat="YYYY-MM-DD HH:mm:ss"
            placeholder="结束时间"
            v-model:value="model[field + '_end']"
            style="min-width: 100px;width: calc(50% - 15px);"
          ></a-date-picker>
        </a-form-item-rest> -->
        <a-range-picker :style="{ width: '100%' }" v-model:value="model[field]" :show-time="true" valueFormat="YYYY-MM-DD HH:mm:ss"/>
        <!-- update-end--author:liaozhiyang---date:20240530---for：【TV360X-213】普通查询日期数值组件更换 -->
      </template>

      <!-- update-begin--author:liaozhiyang---date:20240517---for：【QQYUN-9348】增加online查询区域时间范围查询功能 -->
      <!-- 范围查询：时间 -->
      <template #groupTime="{ model, field }">
        <!-- update-begin--author:liaozhiyang---date:20240530---for：【TV360X-213】普通查询日期数值组件更换 -->
        <!-- <a-time-picker
          placeholder="开始时间"
          value-format="HH:mm:ss"
          v-model:value="model[field + '_begin']"
          style="min-width: 100px;width: calc(50% - 15px);"
        ></a-time-picker>
        <span class="group-query-string">~</span>
        <a-form-item-rest>
          <a-time-picker
            placeholder="结束时间"
            value-format="HH:mm:ss"
            v-model:value="model[field + '_end']"
            style="min-width: 100px;width: calc(50% - 15px);"
          ></a-time-picker>
        </a-form-item-rest> -->
        <a-time-range-picker :style="{ width: '100%' }" v-model:value="model[field]" value-format="HH:mm:ss" />
        <!-- update-end--author:liaozhiyang---date:20240530---for：【TV360X-213】普通查询日期数值组件更换 -->
      </template>
      <!-- update-end--author:liaozhiyang---date:20240517---for：【QQYUN-9348】增加online查询区域时间范围查询功能 -->

      <!-- 范围查询：数值 -->
      <template #groupNumber="{ model, field, schema }">
        <!-- update-begin--author:liaozhiyang---date:20240530---for：【TV360X-213】普通查询日期数值组件更换 -->
        <!-- <a-input-number placeholder="开始值" v-model:value="model[field + '_begin']" style="width: calc(50% - 15px)"></a-input-number>
        <span class="group-query-string">~</span>
        <a-form-item-rest>
          <a-input-number placeholder="结束值" v-model:value="model[field + '_end']" style="width: calc(50% - 15px)"></a-input-number>
        </a-form-item-rest> -->
        <JRangeNumber v-model:value="model[field]" v-bind="schema.componentProps" />
        <!-- update-end--author:liaozhiyang---date:20240530---for：【TV360X-213】普通查询日期数值组件更换 -->
      </template>

      <!-- 查询/重置按钮-->
      <template #formFooter>
        <a-col :md="6" :sm="8">
          <span
            class="table-page-search-submitButtons"
            :class="{ 'only-toggle': !queryBtnCfg.enabled && !resetBtnCfg.enabled }"
          >
            <a-button
                v-if="queryBtnCfg.enabled"
                type="primary"
                :preIcon="queryBtnCfg.buttonIcon"
                @click="doSearch"
            >
              <span>{{ queryBtnCfg.buttonName }}</span>
            </a-button>
            <a-button
                v-if="resetBtnCfg.enabled"
                type="primary"
                :preIcon="resetBtnCfg.buttonIcon"
                style="margin-left: 8px"
                @click="resetSearch"
            >
              <span>{{ resetBtnCfg.buttonName }}</span>
            </a-button>
            <a v-if="toggleButtonShow" @click="toggleSearchStatus = !toggleSearchStatus" style="margin-left: 8px">
              {{ toggleSearchStatus ? '收起' : '展开' }}
              <a-icon :type="toggleSearchStatus ? 'up' : 'down'" />
            </a>
          </span>
        </a-col>
      </template>
    </BasicForm>
  </div>
</template>

<script>
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { watch, ref, reactive, toRaw, isProxy } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { useMessage } from '/@/hooks/web/useMessage';
  import FormSchemaFactory from './factory/FormSchemaFactory';
  import IFormSchema from './factory/IFormSchema';
  import { handleLinkDown, getFieldIndex, getRefPromise, LINK_DOWN } from '../../hooks/auto/useAutoForm';
  import { ONL_QUERY_LABEL_COL, ONL_QUERY_WRAPPER_COL, FORM_VIEW_TO_QUERY_VIEW } from '../../types/onlineRender';
  import { loadOneFieldDefVal } from '../../util/FieldDefVal';
  import { useExtendComponent } from '../../hooks/auto/useExtendComponent';
  import { LABELLENGTH } from '../../util/constant';
  import dayjs from 'dayjs';
  import JRangeNumber from '/@/components/Form/src/jeecg/components/JRangeNumber.vue'
  import { useDebounceFn } from "@vueuse/core";
  
  export default {
    name: 'OnlineQueryForm',
    components: {
      BasicForm,
      JRangeNumber,
    },
    props: {
      id: {
        type: String,
        default: '',
      },
      queryBtnCfg: {
        type: Object,
        default: () => {
          return {
            enabled: true,
            buttonName: '查询',
            buttonIcon: 'ant-design:search',
          }
        }
      },
      resetBtnCfg: {
        type: Object,
        default: () => {
          return {
            enabled: true,
            buttonName: '重置',
            buttonIcon: 'ant-design:reload',
          }
        }
      },
    },
    emits: ['search', 'loaded'],
    setup(props, { emit }) {
      // 获取查询条件请求地址
      const LOAD_URL = '/online/cgform/api/getQueryInfoVue3/';
      // 表单ref
      const onlineQueryFormRef = ref(null);
      // 表单渲染用到的配置
      const formSchemas = ref([]);
      // 表单栅格 VUEN-2493【优化】online默认查询条件太宽了，参考online报表
      const baseColProps = ref({ xs:24, sm: 24, md: 12, lg:6, xl:6 });
      // 切换字段显示隐藏按钮是否显示
      const toggleButtonShow = ref(false);
      // 是否显示所有查询字段
      const toggleSearchStatus = ref(false);
      // 查询条件
      const queryParams = ref({});
      // 需要隐藏的字段
      const hideList = ref([]);
      const { createMessage: $message } = useMessage();
      const { linkTableCard2Select } = useExtendComponent();
      const formLabelWidth = ref(80);
      /**
       *  默认值分三种,param > cache > config
       *  1.表单配置-config
       *  2.路由缓存-cache
       *  3.地址栏参数-param
       *  当表单id发生改变，config改变，列表页传入cache，param;监听status change然后重置表单的值
       */
      const defaultValues = reactive({
        config: {},
        cache: {},
        param: {},
        status: false,
      });

      const debouncedCustomSetFieldsValue = useDebounceFn(customSetFieldsValue, 500);

      // update-begin--author:liaozhiyang---date:20260715---for：【LHZP-219】修复有默认值查询的场景下会调用两次接口，让只调用最后一次查询接口
      let resolveFirstQueryDecision = () => {};
      let firstQueryDecision = new Promise((r) => (resolveFirstQueryDecision = r));
      function resetFirstQueryDecision() {
        firstQueryDecision = new Promise((r) => (resolveFirstQueryDecision = r));
      }
      // 供父组件调用：等待"是否有默认查询值"的决策
      function whenFirstQueryDecided() {
        return firstQueryDecision;
      }
      // update-end--author:liaozhiyang---date:20260715---for：【LHZP-219】修复有默认值查询的场景下会调用两次接口，让只调用最后一次查询接口
      
      /**
       * 监听cacheFormValues
       */
      watch(
        () => defaultValues.status,
        async (val) => {
          console.log('-------------defaultValues发生改变,需要重置表单---------------');
          const { config, cache, param } = toRaw(defaultValues);
          let rawValues = Object.assign({}, config, cache, param);
          //update-begin---author:wangshuai---date:2025-10-11---for:【issues/8790】online 表单重大 bug，影响配置了查询 的所有表单---
          await debouncedCustomSetFieldsValue(rawValues);
          //update-end---author:wangshuai---date:2025-10-11---for:【issues/8790】online 表单重大 bug，影响配置了查询 的所有表单---
        },
        { immediate: true, deep: true }
      );

      /**
       * 设置默认值
       * @param values
       */
      async function initDefaultValues(cache, param) {
        defaultValues.cache = { ...cache };
        defaultValues.param = { ...param };
        defaultValues.status = !defaultValues.status;
      }

      const clearObj = (obj) => {
        Object.keys(obj).map((key) => {
          delete obj[key];
        });
      };

      // 监听
      watch(
        () => props.id,
        (val) => {
          if (val) {
            resetForm();
          } else {
            formSchemas.value = [];
            // update-begin--author:liaozhiyang---date:20260715---for：【LHZP-219】修复有默认值查询的场景下会调用两次接口，让只调用最后一次查询接口
            resolveFirstQueryDecision(false);
            // update-end--author:liaozhiyang---date:20260715---for：【LHZP-219】修复有默认值查询的场景下会调用两次接口，让只调用最后一次查询接口
          }
        },
        { immediate: true }
      );

      /**
       * 获取表单配置
       */
      async function initSchemas(formProperties, searchFieldOrderMap) {
        let arr = [];
        let configValue = {};
        // update-begin--author:liaozhiyang---date:20260807---for：【LHZP-487】查询字段的顺序按照接口中的searchFieldList数组中的顺序即设计时字段的排列顺序
        let keys = Object.keys(formProperties).sort(
          (a, b) => (searchFieldOrderMap.get(a) ?? 9999) - (searchFieldOrderMap.get(b) ?? 9999)
        );
        // update-end--author:liaozhiyang---date:20260807---for：【LHZP-487】查询字段的顺序按照接口中的searchFieldList数组中的顺序即设计时字段的排列顺序
        let setLabelLength = -1;
        for (let key of keys) {
          const item = formProperties[key];
          
          //update-begin-author:taoyan date:2023-7-19 for:QQYUN-5783 配置的数据字典参数，问题是在查询的部门选择组件那儿，表单定义不能更改传给后端org_code。看后端代码，给组件value传的也是id，这儿需要调整。
          if(key === 'sys_org_code'){
            if(!item.fieldExtendJson){
              item.fieldExtendJson = '{"store":"orgCode"}'
            }
          }
          //update-end-author:taoyan date:2023-7-19 for:QQYUN-5783 配置的数据字典参数，问题是在查询的部门选择组件那儿，表单定义不能更改传给后端org_code。看后端代码，给组件value传的也是id，这儿需要调整。
          
          let view = item.view;
          // update-begin--author:liaozhiyang---date:20240611---for：【TV360X-461】字段类型是string，控件是text，则默认模糊查询
          item.originView = item.view;
          // update-end--author:liaozhiyang---date:20240611---for：【TV360X-461】字段类型是string，控件是text，则默认模糊查询
          if (FORM_VIEW_TO_QUERY_VIEW[view]) {
            item.view = FORM_VIEW_TO_QUERY_VIEW[view];
          }
          await loadOneFieldDefVal(key, item, configValue);
          if (item.mode == 'group' && ('date' == view || 'datetime' == view || 'number' == view || 'time' == view )) {
            // 范围查询-日期，时间，数值
            let temp = FormSchemaFactory.createSlotFormSchema(key, item);
            arr.push(temp);
          } else {
            if (item.view === LINK_DOWN) {
              let array = handleLinkDown(item, key);
              for (let linkDownItem of array) {
                let temp = FormSchemaFactory.createFormSchema(linkDownItem.key, linkDownItem);
                let tempIndex = getFieldIndex(arr, linkDownItem.key);
                if (tempIndex == -1) {
                  arr.push(temp);
                } else {
                  arr[tempIndex] = temp;
                }
              }
            } else {
              let tempIndex = getFieldIndex(arr, key);
              if (tempIndex == -1) {
                let temp = FormSchemaFactory.createFormSchema(key, item);
                arr.push(temp);
              }
            }
          }
          // update-begin--author:liaozhiyang---date:20231205---for：【QQYUN-7140】online label默认显示6个
          let fieldExtendJson = item.fieldExtendJson;
          if (fieldExtendJson) {
            fieldExtendJson = JSON.parse(fieldExtendJson);
            if (fieldExtendJson.labelLength) {
              console.log(key, fieldExtendJson.labelLength);
              if (setLabelLength > -1) {
                // 取配置中设置的最大值，所有label的长度应一致，否则多行会对不齐
                setLabelLength = fieldExtendJson.labelLength > setLabelLength ? fieldExtendJson.labelLength : setLabelLength;
              } else {
                setLabelLength = fieldExtendJson.labelLength;
              }
            }
          }
          // update-end--author:liaozhiyang---date:20231205---for：【QQYUN-7140】online label默认显示6个
        }
        // update-begin--author:liaozhiyang---date:20231205---for：【QQYUN-7140】online label默认显示6个
        // 配置中没有设置,读取默认的长度
        if (setLabelLength == -1) {
          setLabelLength = LABELLENGTH;
        } else {
          // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-98】label展示的文字必须和labelLength配置一致
          arr.forEach(item=>{
            item.labelLength = setLabelLength;
          })
          // update-end--author:liaozhiyang---date:20240517---for：【TV360X-98】label展示的文字必须和labelLength配置一致
        }
        // update-end--author:liaozhiyang---date:20231205---for：【QQYUN-7140】online label默认显示6个
        // update-begin--author:liaozhiyang---date:20260807---for：【LHZP-487】查询字段的顺序按照接口中的searchFieldList数组中的顺序即设计时字段的排列顺序
        arr.sort(
          (a, b) => (searchFieldOrderMap.get(a.field) ?? 9999) - (searchFieldOrderMap.get(b.field) ?? 9999)
        );
        // update-end--author:liaozhiyang---date:20260807---for：【LHZP-487】查询字段的顺序按照接口中的searchFieldList数组中的顺序即设计时字段的排列顺序
        let schemaArray = [];
        if (arr.length > 2) {
          toggleButtonShow.value = true;
        }
        let hideFieldName = [];
        for (let i = 0; i < arr.length; i++) {
          let item = arr[i];
          item.setFormRef(onlineQueryFormRef);
          item.noChange();
          item.asSearchForm();
          if (i > 1) {
            hideFieldName.push(item.field);
            item.isHidden();
          }
          //update-begin-author:taoyan date:2022-10-24 for: VUEN-2493【优化】online默认查询条件太宽了，参考online报表
          let tempSchema = item.getFormItemSchema();
          if(item.slot == 'groupDatetime'){
            // update-begin--author:liaozhiyang---date:20240530---for：【TV360X-213】普通查询日期数值组件更换
            //如果是时间类型 重新设定colprops (小于3个时才重置，否则多行会导致对不齐)
            arr.length <= 3 && (tempSchema['colProps'] = { xs:24, sm: 24, md: 12, lg:8, xl:8 })
            // update-end--author:liaozhiyang---date:20240530---for：【TV360X-213】普通查询日期数值组件更换
          }
          // update-begin--author:liaozhiyang---date:20240522---for：【TV360X-250】查询区域把Switch开关组件改成select组件
          if (tempSchema.component === 'JSwitch') {
            const componentProps = tempSchema.componentProps ?? {};
            tempSchema.componentProps = { ...componentProps, query: true };
          }
          // update-end--author:liaozhiyang---date:20240522---for：【TV360X-250】查询区域把Switch开关组件改成select组件
          linkTableCard2Select(tempSchema);
          // update-begin--author:liaozhiyang---date:20240530---for：【TV360X-389】普通查询关联记录去掉编辑按钮
          if (tempSchema.component === 'LinkTableSelect') {
            let componentProps = tempSchema.componentProps ?? {};
            tempSchema.componentProps = { ...componentProps, editBtnShow: false };
          }
          // update-end--author:liaozhiyang---date:20240530---for：【TV360X-389】普通查询关联记录去掉编辑按钮
          // update-begin--author:liaozhiyang---date:20240614---for：【TV360X-1231】查询区域有的下拉组件显示不全
          const compProps = tempSchema.componentProps ?? {};
          if (!compProps.getPopupContainer) {
            tempSchema.componentProps = { ...compProps, getPopupContainer: () => document.body };
          }
          // update-end--author:liaozhiyang---date:20240614---for：【TV360X-1231】查询区域有的下拉组件显示不全
          // update-begin--author:liaozhiyang---date:20240725---for：【TV360X-1857】online查询增加模糊查询
           const fieldData = formProperties[tempSchema.field] ?? {};
          // 【TV360X-1966】页面属性控件配置了文本框且字段类型是string，个性化查询配置了用户组件，用户组件不生效
          if (fieldData.mode == 'like' && fieldData.view === 'text' && fieldData.originView === 'text') {
            tempSchema.component = 'JInput';
          }
          // update-end--author:liaozhiyang---date:20240725---for：【TV360X-1857】online查询增加模糊查询
          schemaArray.push(tempSchema);
          //update-end-author:taoyan date:2022-10-24 for: VUEN-2493【优化】online默认查询条件太宽了，参考online报表
        }
        hideList.value = hideFieldName;
        formSchemas.value = schemaArray;
        //设置表单默认值
        defaultValues.config = { ...configValue };
        defaultValues.status = !defaultValues.status;
        // update-begin--author:liaozhiyang---date:20231204---for：【QQYUN-7140】online label默认显示6个
        setTimeout(() => {
          // 14是文字size，24是间隙
          const w = setLabelLength * 14 + setLabelLength + 24;
          formLabelWidth.value = w;
        }, 0);
        // update-end--author:liaozhiyang---date:20231204---for：【QQYUN-7140】online label默认显示6个
      }
      /**
       * 2024-05-31
       * liaozhiyang
       * 【TV360X-415】个性化查询支持年、月、周、季度.
       * 解析特定view(组件)字段的值，把view字段值为date_year、date_month、date_week、date_quarter
       * 改成date，并组装fieldExtendJson
       */
      const analysisComponent = (res) => {
        const properties = res.properties;
        if (properties) {
          Object.entries(properties).forEach(([key, value]) => {
            const data = value;
            if (['date_year', 'date_month', 'date_week', 'date_quarter'].includes(data.view)) {
              const fieldExtendJson = data.fieldExtendJson ? JSON.parse(data.fieldExtendJson) : {};
              fieldExtendJson.picker = data.view.split('_')[1];
              data.fieldExtendJson = JSON.stringify(fieldExtendJson);
              data.view = 'date';
            }
          });
        }
      };
      async function resetForm() {
        // update-begin--author:liaozhiyang---date:20260715---for：【LHZP-219】修复有默认值查询的场景下会调用两次接口，让只调用最后一次查询接口
        resetFirstQueryDecision();
        // update-end--author:liaozhiyang---date:20260715---for：【LHZP-219】修复有默认值查询的场景下会调用两次接口，让只调用最后一次查询接口
        try {
          let json = await loadQueryInfo();
          // update-begin--author:liaozhiyang---date:20240531---for：【TV360X-415】个性化查询支持年、月、周、季度
          analysisComponent(json);
          // update-end--author:liaozhiyang---date:20240318---for：【TV360X-415】个性化查询支持年、月、周、季度
          // update-begin--author:liaozhiyang---date:20240524---for：【TV360X-516】高级查询过滤掉不支持查询的组件
          // filterComponent(json);
          // update-end--author:liaozhiyang---date:20240524---for：【TV360X-516】高级查询过滤掉不支持查询的组件
          // 获取所有字段配置 通过事件回传给高级查询组件
          let allFields = getAllFields(json);
          emit('loaded', json);
          // 获取查询条件表单页面配置
          let { formProperties, hasField, searchFieldOrderMap } = getQueryFormProperties(allFields, json);
          if (hasField == false) {
            formSchemas.value = [];
            // update-begin--author:liaozhiyang---date:20260715---for：【LHZP-219】修复有默认值查询的场景下会调用两次接口，让只调用最后一次查询接口
            resolveFirstQueryDecision(false);
            // update-end--author:liaozhiyang---date:20260715---for：【LHZP-219】修复有默认值查询的场景下会调用两次接口，让只调用最后一次查询接口
            return;
          }
          // 获取表单配置formSchemas
          await initSchemas(formProperties, searchFieldOrderMap);
          // update-begin--author:liaozhiyang---date:20260715---for：【LHZP-219】修复有默认值查询的场景下会调用两次接口，让只调用最后一次查询接口
          const { config, cache, param } = toRaw(defaultValues);
          const rawValues = Object.assign({}, config, cache, param);
          const values = transformGroupDefValus(rawValues);
          resolveFirstQueryDecision(Object.keys(values).length > 0);
          // update-end--author:liaozhiyang---date:20260715---for：【LHZP-219】修复有默认值查询的场景下会调用两次接口，让只调用最后一次查询接口
        } catch (e) {
          console.error(e);
          // update-begin--author:liaozhiyang---date:20260715---for：【LHZP-219】修复有默认值查询的场景下会调用两次接口，让只调用最后一次查询接口
          resolveFirstQueryDecision(false);
          // update-end--author:liaozhiyang---date:20260715---for：【LHZP-219】修复有默认值查询的场景下会调用两次接口，让只调用最后一次查询接口
        }
      }
      /**
       * 2024-05-24
       * liaozhiyang
       * 过滤掉不支持查询的组件（图片、文件、密码、关联记录、联动）
       */
      const filterComponent = (data) => {
        const { properties = {} } = data;
        Object.entries(properties).forEach(([field, value]) => {
          if (value.view === 'table') {
            filterComponent(value);
          }
          if (['image', 'password', 'file', 'link_table', 'link_down'].includes(value.view)) {
            delete properties[field];
          }
        });
      };
      /**
       * 设置表单的值
       */
      async function customSetFieldsValue(rawValues) {
        await getRefPromise(onlineQueryFormRef);
        console.log('rawValues', rawValues);
        // update-begin--author:liaozhiyang---date:20240618---for：online普通查询默认值范围查询不好使
        const values = transformGroupDefValus(rawValues);
        await setFieldsValue(values);
        // update-end--author:liaozhiyang---date:20240618---for：online普通查询默认值范围查询不好使
        if (Object.keys(values).length > 0) {
          doSearch();
        }
      }

      /**
       * liaozhiyang
       * 20260807
       * 【LHZP-487】查询字段的顺序按照接口中的searchFieldList数组中的顺序即设计时字段的排列顺序
       * 根据 searchFieldList 构建字段排序映射（仅查询表单使用，与字段配置 order 无关）
       */
      function buildSearchFieldOrderMap(searchFieldList, joinQuery, table) {
        const orderMap = new Map();
        searchFieldList.forEach((field, index) => {
          orderMap.set(field, index);
          if (joinQuery && field.indexOf('@') < 0) {
            orderMap.set(table + '@' + field, index);
          }
        });
        return orderMap;
      }
      /**
       * 转化
       */
      function getQueryFormProperties(allFields, json) {
        const { searchFieldList, joinQuery, table } = json;
        let hasField = false;
        let formProperties = {};
        // update-begin--author:liaozhiyang---date:20260807---for：【LHZP-487】查询字段的顺序按照接口中的searchFieldList数组中的顺序即设计时字段的排列顺序
        const searchFieldOrderMap = buildSearchFieldOrderMap(searchFieldList, joinQuery, table);
        if (allFields && searchFieldList.length) {
          // 按 searchFieldList 顺序构建查询字段
          searchFieldList.forEach((field) => {
            const fieldConfig = allFields[field];
            if (!fieldConfig) {
              return;
            }
            const item = { ...fieldConfig };
            if (joinQuery == true) {
              //判断是不是联合查询
              if (field.indexOf('@') < 0) {
                //没有@说明是主表字段, 手动拼接上@
                formProperties[table + '@' + field] = item;
                hasField = true;
              } else {
                //有@说明是子表字段，直接获取
                formProperties[field] = item;
                hasField = true;
              }
            } else {
              // 不是联合查询 只查主表字段
              if (field.indexOf('@') < 0) {
                formProperties[field] = item;
                hasField = true;
              }
            }
          });
        }
        // update-end--author:liaozhiyang---date:20260807---for：【LHZP-487】查询字段的顺序按照接口中的searchFieldList数组中的顺序即设计时字段的排列顺序
        return {
          formProperties,
          hasField,
          searchFieldOrderMap,
        };
      }

      /**
       * 获取查询条件表单配置
       * json结构：
       * 主表字段1：{配置1}
       * 主表字段2：{配置2}
       * 子表名@子表字段1：{子表字段配置1}
       * 子表名@子表字段2：{子表字段配置2}
       */
      function getAllFields(json) {
        // 获取所有配置 查询字段 是否联合查询
        const { properties, searchFieldList, joinQuery, table } = json;
        let allFields = {};
        let order = 1;
        let hasField = false;
        Object.keys(properties).map((field) => {
          let item = properties[field];
          if (item.view == 'table') {
            // 子表字段
            // 联合查询开启才需要子表字段作为查询条件
            let subProps = item['properties'];
            let subTableOrder = order * 100;
            Object.keys(subProps).map((subField) => {
              let subItem = subProps[subField];
              // 保证排序统一
              subItem['order'] = subTableOrder + Number(subItem['order']);
              let subFieldKey = field + '@' + subField;
              allFields[subFieldKey] = subItem;
            });
            order++;
          } else {
            // 主表字段
            item['order'] = Number(item['order']);
            allFields[field] = item;
          }
        });
        return allFields;
      }

      /**
       * 查询返回数据格式, 需要经过getQueryFormProperties转成表单配置
       * json结构：
       *    table(表名)
       *    title(表描述)
       *    properties(表字段)
       *        field1
       *        field2
       *        fieldxxx
       *        sub-table1(子表名1)
       *            title(子表描述1)
       *            properties(子表字段)
       *        sub-table2(子表名2)
       *            title(子表描述2)
       *            properties(子表字段)
       */
      function loadQueryInfo() {
        let url = `${LOAD_URL}${props.id}`;
        return new Promise((resolve) => {
          defHttp
            .get({ url }, { isTransformResponse: false })
            .then((res) => {
              // console.log("-online列表查询条件获取配置", res);
              if (res.success) {
                resolve(res.result);
              } else {
                resolve(false);
                $message.warning(res.message);
              }
            })
            .catch(() => {
              $message.warning('获取查询条件失败!');
              resolve(false);
            });
        });
      }
      
      //表单配置
      const [registerForm, { resetFields, setFieldsValue, updateSchema, getFieldsValue }] = useForm({
        name: 'online-query-form',
        schemas: formSchemas,
        showActionButtonGroup: false,
        baseColProps: baseColProps,
        autoSubmitOnEnter: true,
        labelWidth: formLabelWidth,
        wrapperCol: null,
        submitFunc() {
          doSearch();
        },
        /* labelCol: ONL_QUERY_LABEL_COL,
        wrapperCol: ONL_QUERY_WRAPPER_COL*/
      });

      /**
       * 执行查询
       */
      function doSearch() {
        let formValues = getFieldsValue();
         // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-28】年，年月，周查询出结果不准
        transformDateValus(formValues);
         // update-end--author:liaozhiyang---date:20240517---for：【TV360X-28】年，年月，周查询出结果不准
        // update-begin--author:liaozhiyang---date:20240530---for：【TV360X-213】普通查询日期数值组件更换
        transformGroupValus(formValues);
        // update-end--author:liaozhiyang---date:20240530---for：【TV360X-213】普通查询日期数值组件更换
        // 还需要把地址栏参数添加进去
        let data = Object.assign({}, toRaw(defaultValues.param), changeDataIfArray2String(formValues));
        emit('search', data, true);
      }
      /**
       * 2024-06-18
       * liaozhiyang
       * online普通查询默认值范围查询不好使
       * */
      const transformGroupDefValus = (obj) => {
        const values = { ...obj };
        const groupSchemas = formSchemas.value.filter((item) => ['groupTime', 'groupDatetime', 'groupNumber', 'groupDate'].includes(item.slot));
        if (groupSchemas.length) {
          Object.keys(values).forEach((filed) => {
            let key;
            const findItem = groupSchemas.find((item) => {
              if (item.field === filed) {
                key = filed;
                return true;
              }
              return false;
            });
            if (findItem) {
              const value = values[key];
              if (typeof value === 'string') {
                const arr = value.split(',');
                values[key] = [...arr];
              }
            }
          });
        }
        return values;
      };

      /**
       * 2024-05-20
       * liaozhiyang
       * 【TV360X-213】把groupDatetime,groupTime,groupDate,groupNumber等范围字段分割成两个字段
       */
      const transformGroupValus = (values) => {
        if (values) {
          const groupSchemas = formSchemas.value.filter((item) => ['groupTime', 'groupDatetime', 'groupDate', 'groupNumber'].includes(item.slot));
          if (groupSchemas.length) {
            Object.keys(values).forEach((filed) => {
              let key;
              const findItem = groupSchemas.find((item) => {
                if (item.field === filed) {
                  key = filed;
                  return true;
                }
                return false;
              });
              if (findItem) {
                const value = values[key];
                // update-begin--author:liaozhiyang---date:20260803---for：【LHZP-381】点击重置时，日期和日期时间默认值没设置上
                // 范围组件值为数组时也需拆成 begin/end
                if (Array.isArray(value)) {
                  values[`${key}_begin`] = value[0];
                  values[`${key}_end`] = value[1];
                  delete values[key];
                } else if (typeof value === 'string') {
                  const arr = value.split(',');
                  values[`${key}_begin`] = arr[0];
                  values[`${key}_end`] = arr[1];
                  delete values[key];
                }
                // update-end--author:liaozhiyang---date:20260803---for：【LHZP-381】点击重置时，日期和日期时间默认值没设置上
              }
            });
          }
        }
      };
      /**
      * 2024-05-20
      * liaozhiyang
      * 把年，年月，周等时间重置到当前格式的第一天，因为存的时候也是第一天 (【TV360X-180】兼容时间范围)
      */
      const transformDateValus = (values) => {
        const dateSchemas = formSchemas.value.filter((item) => item.componentProps?.picker && item.componentProps.picker != 'default');
        if (dateSchemas.length) {
          Object.keys(values).forEach((filed) => {
            let key;
            const findItem = dateSchemas.find((item) => {
              if (item.field === filed || `${item.field}_begin` === filed || `${item.field}_end` === filed) {
                key = filed;
                return true;
              }
              return false;
            });
            if (findItem) {
              const value = values[key];
              if (value) {
                // update-begin--author:liaozhiyang---date:20240530---for：【TV360X-213】普通查询日期数值组件更换
                const auto = (value, key, isEnd) => {
                  const picker = findItem.componentProps.picker;
                  if (picker === 'year') {
                    if (isEnd) {
                      values[key] = dayjs(value).endOf('year').format('YYYY-MM-DD');
                    } else {
                      values[key] = dayjs(value).startOf('year').format('YYYY-MM-DD');
                    }
                  } else if (picker === 'month') {
                    if (isEnd) {
                      values[key] = dayjs(value).endOf('month').format('YYYY-MM-DD');
                    } else {
                      values[key] = dayjs(value).startOf('month').format('YYYY-MM-DD');
                    }
                  } else if (picker === 'week') {
                    if (isEnd) {
                      values[key] = dayjs(value).endOf('week').format('YYYY-MM-DD');
                    } else {
                      values[key] = dayjs(value).startOf('week').format('YYYY-MM-DD');
                    }
                  } else if (picker === 'quarter') {
                    if (isEnd) {
                      values[key] = dayjs(value).endOf('quarter').format('YYYY-MM-DD');
                    } else {
                      values[key] = dayjs(value).startOf('quarter').format('YYYY-MM-DD');
                    }
                  }
                };
                if (findItem?.slot === 'groupDate') {
                  // update-begin--author:liaozhiyang---date:20260803---for：【LHZP-381】点击重置时，日期和日期时间默认值没设置上
                  // 兼容范围日期值为数组的场景
                  const arr = Array.isArray(value) ? value : String(value).split(',');
                  // update-end--author:liaozhiyang---date:20260803---for：【LHZP-381】点击重置时，日期和日期时间默认值没设置上
                  auto(arr[0], `${key}_begin`, false);
                  auto(arr[1], `${key}_end`, true);
                  delete values[key];
                } else {
                  auto(value, key, false);
                }
                // update-end--author:liaozhiyang---date:20240530---for：【TV360X-213】普通查询日期数值组件更换
              }
            }
          });
        }
      }

      /**
       * 重置是将 查询控件的值 重置到默认值
       * 2024-05-23
       * liaozhiyang
       * 【TV360X-124】提供clearSearch方法回到初始状态
       */
      async function clearSearch() {
        await resetFields();
        const { config, param } = toRaw(defaultValues);
        let rawValues = Object.assign({}, config, param);
        if (Object.keys(rawValues).length > 0) {
          // update-begin--author:liaozhiyang---date:20260803---for：【LHZP-381】点击重置时，日期和日期时间默认值没设置上
          // 重置查询时日期/日期时间等范围组件默认值需转为数组才能正确回显
          // 与 customSetFieldsValue 保持一致：范围查询默认值（如 "2024-01-01,2024-12-31"）需转换成数组
          const values = transformGroupDefValus(rawValues);
          await setFieldsValue(values);
          // update-end--author:liaozhiyang---date:20260803---for：【LHZP-381】点击重置时，日期和日期时间默认值没设置上
        }
        return rawValues;
      }

      async function resetSearch() {
        const rawValues = await clearSearch();
        // update-begin--author:liaozhiyang---date:20260803---for：【LHZP-381】点击重置时，日期和日期时间默认值没设置上
        // 重置后查询参数与点击查询保持一致（日期范围拆分 begin/end）
        const values = { ...rawValues };
        transformDateValus(values);
        transformGroupValus(values);
        emit('search', changeDataIfArray2String(values), false);
        // update-end--author:liaozhiyang---date:20260803---for：【LHZP-381】点击重置时，日期和日期时间默认值没设置上
      }

      /**
       * 有些数据是数组格式的 强转成字符串
       */
      function changeDataIfArray2String(data) {
        Object.keys(data).map((k) => {
          if (data[k]) {
            if (data[k] instanceof Array) {
              data[k] = data[k].join(',');
            }
          }
        });
        return data;
      }

      watch(
        () => toggleSearchStatus.value,
        (status) => {
          let names = hideList.value;
          if (names && names.length > 0) {
            let arr = [];
            for (let name of names) {
              arr.push({
                field: name,
                show: status,
              });
            }
            updateSchema(arr);
          }
        },
        { immediate: false }
      );
      /**
       * 2024-05-30
       * liaozhiyang
       * 【TV360X-392】日期placeholder修改
       * */
      const getGroupDatePlaceholder = (data) => {
        let result = ['开始日期', '结束日期'];
        console.log(data);
        if (data?.picker) {
          switch (data?.picker) {
            case 'year':
              result = ['开始年份', '结束年份'];
              break;
            case 'month':
              result = ['开始月份', '结束月份'];
              break;
            case 'week':
              result = ['开始周', '结束周'];
              break;
            case 'quarter':
              result = ['开始季度', '结束季度'];
              break;
            default:
              result = ['开始日期', '结束日期'];
          }
        }
        return result;
      };
      return {  
        onlineQueryFormRef,
        registerForm,
        initDefaultValues,
        toggleButtonShow,
        toggleSearchStatus,
        doSearch,
        resetSearch,
        queryParams,
        formSchemas,
        clearSearch,
        getGroupDatePlaceholder,
        // update-begin--author:liaozhiyang---date:20260715---for：【LHZP-219】修复有默认值查询的场景下会调用两次接口，让只调用最后一次查询接口
        whenFirstQueryDecided,
        // update-end--author:liaozhiyang---date:20260715---for：【LHZP-219】修复有默认值查询的场景下会调用两次接口，让只调用最后一次查询接口
      };
    },
  };
</script>

<style scoped lang="less">
  .group-query-string {
    width: 20px;
    display: inline-block;
    text-align: center;
  }
  // 查询条件的边距要和列表对齐，所以查询条件的边距要设为0
  .jeecg-basic-table-form-container.p-0 {
    padding: 0;
  }
  // update-begin--author:liaozhiyang---date:20240514---for：【QQYUN-9241】form表单上下间距大点
  .jeecg-basic-table-form-container {
    :deep(.ant-form-item) {
      &:not(.ant-form-item-with-help) {
        margin-bottom: 16px;
      }
    }
  }
  // update-end--author:liaozhiyang---date:20240514---for：【QQYUN-9241】form表单上下间距大点
  .online-query-form {
    overflow-x: hidden;
    :deep(.ant-form) {
      max-height: 40vh;
      overflow-y: auto;
      overflow-x: hidden;
    }
  }
  // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-326】无搜索重置按钮时展开文字垂直居中
  .table-page-search-submitButtons {
    display: inline-flex;
    align-items: center;
    min-height: 32px;
    margin-left: 10px;
    overflow: hidden;
    // 仅有展开/收起时，与同行查询控件垂直对齐
    &.only-toggle {
      line-height: 32px;
    }
  }
  // update-end--author:liaozhiyang---date:20260804---for：【LHZP-326】无搜索重置按钮时展开文字垂直居中
</style>
