<template>
  <div>
    <BasicTable @register="registerTable" :loading="loading">
      <template #tableTitle>
        <a-button v-if="showAdd" @click="onAdd" type="primary" preIcon="ant-design:plus">新增</a-button>
      </template>

      <template #switch="{ record }">
        <a-switch size="small" :checked="record.status === 1" @click="() => onUpdateStatus(record)" />
      </template>

      <template #description="{ record }">
        <div class="rule-description">
          <template v-if="record.ruleOperator === USE_SQL_RULES">
            <span class="rule-operator rule-operator-sql">自定义 SQL</span>
          </template>
          <template v-else>
            <span class="rule-field">{{ getRuleFieldLabel(record.ruleColumn) }}</span>
            <span class="rule-operator">{{ getRuleOperatorLabel(record.ruleOperator) }}</span>
          </template>
          <a-tooltip v-if="hasRuleValue(record)" :title="getRuleValueText(record)">
            <span class="rule-value" :class="{ 'rule-value-system': isSysVarValue(record) }">{{ getRuleValueText(record, true) }}</span>
          </a-tooltip>
        </div>
      </template>

      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <!-- 子表单 -->
    <BasicModal v-bind="formModalProps" @open-change="handleOpenChange">
      <a-spin :spinning="formModalProps.confirmLoading">
        <BasicForm @register="registerForm" />
      </a-spin>
    </BasicModal>
  </div>
</template>

<script lang="ts">
  import { watch, computed, defineComponent, ref, reactive, nextTick } from 'vue';
  import { BasicTable, TableAction, ActionItem, useTable } from '/@/components/Table';
  import { BasicModal, useModal } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { ajaxGetDictItems, getDictItemsByCode } from '/@/utils/dict';
  import { defHttp } from '/@/utils/http/axios';
  import { authButtonLoadData, authDataDelete, authDataLoadData, authDataSaveOrUpdate, authDataUpdateStatus } from '../auth.api';
  import {
    authDataColumns,
    useAuthDataFormSchemas,
    USE_SQL_RULES,
    getRuleValueWidget,
    getRuleValueDictCode,
    getRuleValueDisplayText,
    isFuzzyRuleOperator,
    getPopupDictConfig,
    getSysVarLabel,
    getUserDepartExtend,
    DEFAULT_RULE_VALUE_WIDGET,
    wrapRuleValueWidget,
    normalizeDateRuleValue,
  } from '../auth.data';

  export default defineComponent({
    name: 'AuthDataConfig',
    components: { BasicTable, TableAction, BasicModal, BasicForm },
    props: {
      cgformId: { type: String, required: true },
      authFields: { type: Array, required: true },
      tableType: { type: Number, default: 1 },
    },
    setup(props) {
      const loading = ref(false);
      const mainThemeTemplate = ref('');
      const showAdd = computed(() => props.tableType !== 3 || ['erp', 'innerTable'].includes(mainThemeTemplate.value));
      const [registerTable, { reload, setLoading }] = useTable({
        api: (params) => authDataLoadData(props.cgformId, params),
        rowKey: 'id',
        bordered: true,
        columns: authDataColumns,
        showIndexColumn: false,
        // 操作列
        actionColumn: {
          width: 120,
          title: '操作',
          fixed: false,
          dataIndex: 'action',
          slots: { customRender: 'action' },
        },
      });
      watch(loading, (l) => setLoading(l));
      const [registerModal, { openModal, closeModal }] = useModal();
      const formModalProps = reactive({
        title: '',
        width: 800,
        confirmLoading: false,
        onOk: onSubmit,
        onCancel: closeModal,
        onRegister: registerModal,
      });
      let isUpdate = false;
      let formRecord = {};
      let isManualEnter = false;
      const dictLabelMaps = reactive<Record<string, Record<string, string>>>({});
      const loadingDictCodes = new Set<string>();
      const popupLabelMaps = reactive<Record<string, Record<string, string>>>({});
      const popupConfigIdCache = new Map<string, string>();
      const loadingPopupConfigIds = new Map<string, Promise<string>>();
      const loadingPopupQueries = new Set<string>();
      const userLabelMaps = reactive<Record<string, Record<string, string>>>({});
      const departLabelMaps = reactive<Record<string, Record<string, string>>>({});
      const loadingUserQueries = new Set<string>();
      const loadingDepartQueries = new Set<string>();
      const { formSchemas } = useAuthDataFormSchemas(props, {
        onRuleOperatorChange,
        onRuleColumnChange,
        onRuleNameChange,
      });
      // 表单配置
      const [registerForm, { validate, resetFields, setFieldsValue, getFieldsValue, clearValidate, updateSchema }] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        labelAlign: 'right',
      });

      watch(
        () => props.cgformId,
        () => {
          reload().catch(() => null);
          if (props.tableType === 3) {
            mainThemeTemplate.value = '';
            authButtonLoadData(props.cgformId, { pageNo: 1, pageSize: 1 })
              .then((result) => (mainThemeTemplate.value = result.mainThemeTemplate || ''))
              .catch(() => (mainThemeTemplate.value = ''));
          }
        },
        { immediate: true }
      );

      /** 加载普通字典的显示文本，失败时写入空缓存并继续展示原值。 */
      async function loadRuleValueDict(field) {
        const dictCode = getRuleValueDictCode(field);
        if (!dictCode || dictLabelMaps[dictCode] || loadingDictCodes.has(dictCode)) return;
        loadingDictCodes.add(dictCode);
        try {
          const cachedOptions = getDictItemsByCode(dictCode);
          const options: any = cachedOptions || (await ajaxGetDictItems(dictCode, undefined, { errorMessageMode: 'none' }));
          dictLabelMaps[dictCode] = (options || []).reduce((result, item) => {
            result[String(item.value)] = item.text || item.label || String(item.value);
            return result;
          }, {});
        } catch (_error) {
          dictLabelMaps[dictCode] = {};
        } finally {
          loadingDictCodes.delete(dictCode);
        }
      }

      /**
       * 获取 Popup 报表对应的内部配置 ID。
       * Promise 也参与缓存，确保同一报表在并发渲染多条规则时只请求一次列配置。
       */
      async function getPopupConfigId(code: string) {
        if (popupConfigIdCache.has(code)) return popupConfigIdCache.get(code) || '';
        const loadingRequest = loadingPopupConfigIds.get(code);
        if (loadingRequest) return loadingRequest;
        const request = defHttp
          .get({ url: `/online/cgreport/api/getRpColumns/${code}` }, { isTransformResponse: false, successMessageMode: 'none' })
          .then((res) => {
            const configId = res.success && res.result?.cgRpConfigId ? String(res.result.cgRpConfigId) : '';
            if (configId) popupConfigIdCache.set(code, configId);
            return configId;
          })
          .catch(() => '')
          .finally(() => loadingPopupConfigIds.delete(code));
        loadingPopupConfigIds.set(code, request);
        return request;
      }

      /** 按当前规则值查询 Popup 报表，并把存值字段映射为显示字段后合并进响应式缓存。 */
      async function loadRuleValuePopupDict(field, values: string[]) {
        const config = getPopupDictConfig(field);
        if (!config || !values.length) return;
        const cacheKey = config.dictCode;
        const queryKey = `${cacheKey}:${values.slice().sort().join(',')}`;
        if (loadingPopupQueries.has(queryKey)) return;
        loadingPopupQueries.add(queryKey);
        try {
          const configId = await getPopupConfigId(config.code);
          if (!configId) return;
          const res = await defHttp.get(
            {
              url: `/online/cgreport/api/getData/${configId}`,
              params: { [`force_${config.valueField}`]: values.join(',') },
            },
            { isTransformResponse: false, successMessageMode: 'none' }
          );
          const labelMap = popupLabelMaps[cacheKey] || {};
          (res?.result?.records || []).forEach((item) => {
            labelMap[String(item[config.valueField])] = item[config.labelField] ?? String(item[config.valueField]);
          });
          popupLabelMaps[cacheKey] = { ...labelMap };
        } catch (_error) {
          // 查询失败时保留原编码展示，不阻断数据权限列表
        } finally {
          loadingPopupQueries.delete(queryKey);
        }
      }

      /**
       * 解析规则值列表（统一处理字符串/数组，去空）。
       */
      function normalizeRuleValues(value): string[] {
        const raw = Array.isArray(value) ? value.join(',') : String(value ?? '');
        return raw
          .split(',')
          .map((item) => String(item).trim())
          .filter(Boolean);
      }

      /** 按用户选择控件的存储字段批量查询显示名称，失败时保留原值。 */
      async function loadRuleValueUser(field, values: string[]) {
        const extend = getUserDepartExtend(field);
        const storeField = extend.store || 'username';
        const textField = extend.text || 'realname';
        const cacheKey = `${storeField}_${textField}`;
        const labelMap = userLabelMaps[cacheKey] || {};
        const missingValues = values.filter((value) => !labelMap[value]);
        if (!missingValues.length) return;
        const queryKey = `${cacheKey}:${missingValues.slice().sort().join(',')}`;
        if (loadingUserQueries.has(queryKey)) return;
        loadingUserQueries.add(queryKey);
        try {
          await Promise.all(
            missingValues.map(async (value) => {
              const params: any = { pageNo: 1, pageSize: 1 };
              params[storeField] = value;
              try {
                const res = await defHttp.get(
                  { url: '/sys/user/queryUserComponentData', params },
                  { isTransformResponse: false, successMessageMode: 'none' }
                );
                const record = res?.result?.records?.[0];
                labelMap[value] = record ? String(record[textField] ?? value) : value;
              } catch (_error) {
                labelMap[value] = value;
              }
            })
          );
          userLabelMaps[cacheKey] = { ...labelMap };
        } catch (_error) {
          // 查询失败时保留原编码展示，不阻断数据权限列表
        } finally {
          loadingUserQueries.delete(queryKey);
        }
      }

      /** 按部门选择控件的存储字段批量查询显示名称，失败时保留原值。 */
      async function loadRuleValueDepart(field, values: string[]) {
        const extend = getUserDepartExtend(field);
        const storeField = extend.store || 'id';
        const textField = extend.text || 'departName';
        const cacheKey = `${storeField}_${textField}`;
        const labelMap = departLabelMaps[cacheKey] || {};
        const missingValues = values.filter((value) => !labelMap[value]);
        if (!missingValues.length) return;
        const queryKey = `${cacheKey}:${missingValues.slice().sort().join(',')}`;
        if (loadingDepartQueries.has(queryKey)) return;
        loadingDepartQueries.add(queryKey);
        try {
          // 后端仅支持按 id 或 orgCode 批量查询
          const primaryKey = storeField === 'orgCode' ? 'orgCode' : 'id';
          const res = await defHttp.get(
            {
              url: '/sys/sysDepart/queryDepartTreeSync',
              params: { ids: missingValues.join(','), primaryKey },
            },
            { isTransformResponse: false, successMessageMode: 'none' }
          );
          const labelMapCopy = { ...labelMap };
          (res?.result || []).forEach((item) => {
            const value = item[storeField];
            if (value != null) {
              labelMapCopy[String(value)] = String(item[textField] ?? item.title ?? value);
            }
          });
          // 未查询到的值也写入缓存，避免反复请求
          missingValues.forEach((value) => {
            if (labelMapCopy[value] === undefined) {
              labelMapCopy[value] = value;
            }
          });
          departLabelMaps[cacheKey] = labelMapCopy;
        } catch (_error) {
          // 查询失败时保留原编码展示，不阻断数据权限列表
        } finally {
          loadingDepartQueries.delete(queryKey);
        }
      }

      async function openFormModal(data) {
        isUpdate = data.isUpdate ?? false;
        formModalProps.title = data.title;
        openModal();
        await nextTick();
        await resetFields();
        formRecord = Object.assign({}, data.record);
        await setFieldsValue(formRecord);
        // -update-begin--author:scott---date:20260722---for：【数据权限】规则值根据规则字段控件类型自动切换下拉/字典选择
        // 每次打开均按当前规则重新初始化，避免复用上一次弹窗遗留的规则值组件
        await applyRuleValueWidget(formRecord['ruleColumn'], formRecord['ruleOperator']);
        // -update-end--author:scott---date:20260722---for：【数据权限】规则值根据规则字段控件类型自动切换下拉/字典选择
      }

      function onAdd() {
        openFormModal({ title: '新增' });
      }

      function onEdit(record) {
        openFormModal({ title: '编辑', record, isUpdate: true });
      }

      function onDelete(id) {
        loading.value = true;
        authDataDelete(id)
          .then(reload)
          .finally(() => (loading.value = false));
      }

      async function onSubmit() {
        try {
          formModalProps.confirmLoading = true;
          let formData = await validate();
          formData = Object.assign({}, formRecord, formData);
          // 多选组件的规则值为数组，统一转为逗号分隔字符串存储（后端按逗号拆分拼 in 条件）
          if (Array.isArray(formData.ruleValue)) {
            formData.ruleValue = formData.ruleValue.join(',');
          }
          //update-begin--author:wangshuai---date:20260723---for：【LHZP-1267】年/年月规则值保存为当年/当月第一天
          const ruleField: any = props.authFields.find((item: any) => item.value === formData.ruleColumn);
          formData.ruleValue = normalizeDateRuleValue(ruleField, formData.ruleValue);
          //update-end--author:wangshuai---date:20260723---for：【LHZP-1267】年/年月规则值保存为当年/当月第一天
          delete formData.createTime;
          delete formData.createBy;
          delete formData.updateTime;
          delete formData.updateBy;
          if (formData.ruleOperator == USE_SQL_RULES) {
            formData.ruleColumn = '';
          }
          formData.cgformId = props.cgformId;
          await authDataSaveOrUpdate(formData, isUpdate);
          reload();
          closeModal();
        } finally {
          formModalProps.confirmLoading = false;
        }
      }

      function onUpdateStatus(record) {
        loading.value = true;
        let status = Math.abs(record.status - 1);
        authDataUpdateStatus({ ...record, status })
          .then(() => {
            record.status = status;
          })
          .finally(() => {
            loading.value = false;
          });
      }

      async function onRuleOperatorChange(val) {
        if (val == USE_SQL_RULES) {
          await setFieldsValue({
            ruleColumn: '',
            ruleValue: '',
          });
          await applyRuleValueWidget('', val);
          await clearValidate(['ruleValue']);
        } else {
          // -update-begin--author:scott---date:20260722---for：【数据权限】规则值根据规则字段控件类型自动切换下拉/字典选择
          await applyRuleValueWidget(getFieldsValue().ruleColumn, val);
          // -update-end--author:scott---date:20260722---for：【数据权限】规则值根据规则字段控件类型自动切换下拉/字典选择
        }
      }
      // -update-begin--author:scott---date:20260722---for：【数据权限】规则值根据规则字段控件类型自动切换下拉/字典选择
      // 根据"规则字段"对应的控件类型与"条件规则"，切换"规则值"的输入组件（字典类字段用下拉选择，其他保持原输入框；IN 条件时支持多选）
      async function applyRuleValueWidget(ruleColumn, ruleOperator?) {
        const operator = ruleOperator ?? getFieldsValue().ruleOperator;
        if (operator === USE_SQL_RULES) {
          await updateSchema({
            field: 'ruleValue',
            component: 'InputTextArea',
            componentProps: { customComponent: null },
          });
          return;
        }
        const findItem: any = props.authFields.find((item: any) => item.value === ruleColumn);
        const innerWidget = getRuleValueWidget(findItem, operator);
        const widget = innerWidget ? wrapRuleValueWidget(innerWidget) : DEFAULT_RULE_VALUE_WIDGET;
        // updateSchema 会深度合并 componentProps，先显式清空旧的内部控件，避免字段切换后残留 dictCode、picker 等配置
        await updateSchema({
          field: 'ruleValue',
          component: 'JAuthRuleValue',
          componentProps: { customComponent: null },
        });
        await updateSchema({
          field: 'ruleValue',
          component: widget.component,
          componentProps: widget.componentProps,
        });
        adaptRuleValueType(widget, operator === 'IN');
      }
      // 切换规则值组件后校正值类型：IN 多选字典组件需要数组值；单选用户/部门组件只保留单个值，避免回显异常
      function adaptRuleValueType(widget, isIn) {
        const value = getFieldsValue().ruleValue;
        // 取出 JAuthRuleValue 内部嵌套的实际组件
        const customComponent = widget.componentProps?.customComponent;
        if (isIn) {
          // IN 多选：字典多选组件需要数组值；用户/部门/文本组件天然兼容逗号分隔字符串，无需处理
          if (customComponent?.component === 'JDictSelectTag' && typeof value === 'string') {
            setFieldsValue({ ruleValue: value ? value.split(',') : [] });
          }
          return;
        }
        if (Array.isArray(value)) {
          setFieldsValue({ ruleValue: value[0] ?? '' });
        } else if (
          typeof value === 'string' &&
          value.indexOf(',') > -1 &&
          (customComponent?.component === 'JSelectUser' || customComponent?.component === 'JSelectDept')
        ) {
          setFieldsValue({ ruleValue: value.split(',')[0] });
        }
      }
      // -update-end--author:scott---date:20260722---for：【数据权限】规则值根据规则字段控件类型自动切换下拉/字典选择
      // -update-begin--author:liaozhiyang---date:20240607---for：【TV360X-536】数据权限配置配置优化及新增JInputSelect组件
      async function onRuleColumnChange(val) {
        const values = getFieldsValue();
        if (!values.ruleName || (values.ruleName && !isManualEnter)) {
          const findItem: any = props.authFields.find((item: any) => item.value === val);
          const text = findItem ? findItem.text : val;
          await setFieldsValue({
            ruleName: text,
          });
        }
        // -update-begin--author:scott---date:20260722---for：【数据权限】规则值根据规则字段控件类型自动切换下拉/字典选择
        // 切换规则字段时，规则值控件随之切换，旧值类型可能不匹配，需清空
        await setFieldsValue({ ruleValue: '' });
        await applyRuleValueWidget(val);
        // -update-end--author:scott---date:20260722---for：【数据权限】规则值根据规则字段控件类型自动切换下拉/字典选择
      }
      function onRuleNameChange(e) {
        if (e.target.value.length) {
          isManualEnter = true;
        } else {
          isManualEnter = false;
        }
      }
      function handleOpenChange(visible) {
        if (visible) {
          isManualEnter = false;
        }
      }
      // -update-end--author:liaozhiyang---date:20240607---for：【TV360X-536】数据权限配置配置优化及新增JInputSelect组件
      const ruleOperatorLabels = {
        '=': '等于',
        '!=': '不等于',
        '>': '大于',
        '>=': '大于等于',
        '<': '小于',
        '<=': '小于等于',
        LIKE: '模糊',
        RIGHT_LIKE: '以…开始',
        LEFT_LIKE: '以…结尾',
        IN: '在…中',
        EMPTY: '为空',
        NOT_EMPTY: '不为空',
      };
      const noValueOperators = ['EMPTY', 'NOT_EMPTY'];

      function getRuleFieldLabel(ruleColumn) {
        return props.authFields.find((item: any) => item.value === ruleColumn)?.text || ruleColumn || '未指定字段';
      }

      function getRuleOperatorLabel(ruleOperator) {
        return ruleOperatorLabels[ruleOperator] || ruleOperator;
      }

      function getRuleValueText(record, ellipsisValue = false) {
        const field: any = props.authFields.find((item: any) => item.value === record.ruleColumn);
        const sysVarLabel = getSysVarLabel(record.ruleValue);
        if (sysVarLabel) return sysVarLabel;
        if (isFuzzyRuleOperator(record.ruleOperator)) {
          return Array.isArray(record.ruleValue) ? record.ruleValue.join('、') : String(record.ruleValue ?? '');
        }
        if (field?.view === 'popup_dict') {
          const config = getPopupDictConfig(field);
          const values = Array.isArray(record.ruleValue) ? record.ruleValue : String(record.ruleValue ?? '').split(',');
          const normalizedValues = values.map((value) => String(value).trim()).filter(Boolean);
          if (!config) return normalizedValues.join('、');
          const labelMap = popupLabelMaps[config.dictCode] || {};
          const missingValues = normalizedValues.filter((value) => !labelMap[value]);
          if (missingValues.length) loadRuleValuePopupDict(field, missingValues);
          return normalizedValues.map((value) => labelMap[value] || value).join('、');
        }
        // 用户选择：按字段扩展配置的存储字段查询展示名称，格式为"展示名称（真实值）"，名称缺失时只显示真实值
        if (field?.view === 'sel_user') {
          const extend = getUserDepartExtend(field);
          const storeField = extend.store || 'username';
          const textField = extend.text || 'realname';
          const cacheKey = `${storeField}_${textField}`;
          const normalizedValues = normalizeRuleValues(record.ruleValue);
          const labelMap = userLabelMaps[cacheKey] || {};
          const missingValues = normalizedValues.filter((value) => !labelMap[value]);
          if (missingValues.length) loadRuleValueUser(field, missingValues);
          return normalizedValues
            .map((value) => {
              const label = labelMap[value];
              const displayValue = ellipsisValue ? ellipsisText(value, 5) : value;
              return label ? `${label}(${displayValue})` : displayValue;
            })
            .join('、');
        }
        // 部门选择：按字段扩展配置的存储字段查询展示名称，格式为"展示名称（真实值）"，名称缺失时只显示真实值
        if (field?.view === 'sel_depart') {
          const extend = getUserDepartExtend(field);
          const storeField = extend.store || 'id';
          const textField = extend.text || 'departName';
          const cacheKey = `${storeField}_${textField}`;
          const normalizedValues = normalizeRuleValues(record.ruleValue);
          const labelMap = departLabelMaps[cacheKey] || {};
          const missingValues = normalizedValues.filter((value) => !labelMap[value]);
          if (missingValues.length) loadRuleValueDepart(field, missingValues);
          return normalizedValues
            .map((value) => {
              const label = labelMap[value];
              const displayValue = ellipsisValue ? ellipsisText(value, 5) : value;
              return label ? `${label}(${displayValue})` : displayValue;
            })
            .join('、');
        }
        const dictCode = getRuleValueDictCode(field);
        if (!dictCode) {
          return getRuleValueDisplayText(field, record.ruleValue, record.ruleOperator);
        }
        const labelMap = dictLabelMaps[dictCode];
        const values = Array.isArray(record.ruleValue) ? record.ruleValue : String(record.ruleValue ?? '').split(',');
        if (!labelMap) {
          loadRuleValueDict(field);
          return values.join('、');
        }
        return values.map((value) => labelMap[String(value).trim()] || value).join('、');
      }

      /** 文本截断：超过 maxLen 个字符时显示前 maxLen 个字符 + ... */
      function ellipsisText(text: string | number, maxLen = 5) {
        const str = text == null ? '' : String(text);
        if (str.length <= maxLen) return str;
        return str.substring(0, maxLen) + '...';
      }

      function isSysVarValue(record) {
        return !!getSysVarLabel(record.ruleValue);
      }

      function hasRuleValue(record) {
        return (
          !noValueOperators.includes(record.ruleOperator) && record.ruleValue !== null && record.ruleValue !== undefined && record.ruleValue !== ''
        );
      }

      /**
       * 操作栏
       */
      function getTableAction(record) {
        return [
          {
            label: '编辑',
            onClick: () => onEdit(record),
          },
        ];
      }

      /**
       * 下拉操作栏
       */
      function getDropDownAction(record): ActionItem[] {
        return [
          {
            label: '删除',
            popConfirm: {
              title: '确定删除吗？',
              placement: 'left',
              confirm: () => onDelete(record.id),
            },
          },
        ];
      }

      return {
        loading,
        showAdd,
        formModalProps,
        onAdd,
        onUpdateStatus,
        getTableAction,
        getDropDownAction,
        registerTable,
        registerModal,
        registerForm,
        handleOpenChange,
        USE_SQL_RULES,
        getRuleFieldLabel,
        getRuleOperatorLabel,
        getRuleValueText,
        isSysVarValue,
        hasRuleValue,
      };
    },
  });
</script>

<style lang="less" scoped>
  .rule-description {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
    padding: 0 8px;
    white-space: nowrap;
  }

  .rule-field {
    flex: 0 0 auto;
    color: @text-color;
    font-weight: 500;
  }

  .rule-operator {
    flex: 0 0 auto;
    padding: 1px 8px;
    border-radius: 10px;
    background: fade(@primary-color, 10%);
    color: @primary-color;
    font-size: 12px;
    line-height: 20px;
  }

  .rule-operator-sql {
    background: fade(@warning-color, 12%);
    color: @warning-color;
  }

  .rule-value {
    display: block;
    min-width: 0;
    overflow: hidden;
    color: @text-color-secondary;
    text-overflow: ellipsis;
  }

  .rule-value-system {
    flex: 0 1 auto;
    padding: 1px 8px;
    border: 1px solid fade(@primary-color, 18%);
    border-radius: 4px;
    background: fade(@primary-color, 6%);
    color: @primary-color;
    font-size: 13px;
    line-height: 22px;

    &::before {
      margin-right: 6px;
      color: fade(@primary-color, 65%);
      font-family: monospace;
      content: '{ }';
    }
  }
</style>
