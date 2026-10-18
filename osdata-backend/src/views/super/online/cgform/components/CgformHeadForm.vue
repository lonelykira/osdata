<template>
  <div class="cgform-fields" :class="{ 'cgform-fields--inline': isInlineLayout }">
    <CgformFieldItem
      v-for="item in baseFields"
      :key="item.field"
      :label="item.label"
      :type="item.type"
      :model-value="(formModel as Recordable)[item.field]"
      :required="item.required"
      :disabled="item.disabled"
      :allow-clear="item.allowClear"
      :options="item.options"
      :status="item.status"
      @update:model-value="(formModel as Recordable)[item.field] = $event"
      @change="item.onChange?.($event)"
    />
    <template v-if="[1, 2].includes(formModel.tableType)">
      <!-- 非附表条件字段（单表/主表） -->
      <CgformFieldItem
        v-for="item in conditionalFields"
        v-show="item.show !== false"
        :key="item.field"
        :label="item.label"
        :type="item.type"
        :model-value="(formModel as Recordable)[item.field]"
        :required="item.required"
        :disabled="item.disabled"
        :allow-clear="item.allowClear"
        :options="item.options"
        :status="item.status"
        @update:model-value="(formModel as Recordable)[item.field] = $event"
        @change="item.onChange?.($event)"
      />
      <!-- 非附表类型按钮 -->
      <div v-if="formModel.tableType !== 3" class="cgf-item cgf-item--buttons">
        <a-button preIcon="ant-design:setting" @click="onOpenExtConfig">扩展配置</a-button>
        <a-button type="link" :preIcon="expandStatus ? 'ant-design:up-outlined' : 'ant-design:down-outlined'" @click="expandStatus = !expandStatus">{{
          expandStatus ? '收起' : '展开'
        }}</a-button>
      </div>
    </template>
    <template v-if="formModel.tableType === 3">
      <!-- 附表类型展开：row2=显示复选框+表单风格+关联类型+序号 -->
      <template v-if="expandStatus">
        <CgformFieldItem
          v-for="item in preGroupFields"
          :key="item.field"
          :label="item.label"
          :type="item.type"
          :model-value="(formModel as Recordable)[item.field]"
          :options="item.options"
          @update:model-value="(formModel as Recordable)[item.field] = $event"
        />
        <div class="cgf-item cgf-item--group">
          <div class="cgf-item--group-left">
            <a-radio-group v-model:value="formModel.relationType" :options="relationTypeOptions" @change="onRelationTypeChange" />
            <label class="cgf-label cgf-label--inline">序号</label>
            <a-input-number v-model:value="formModel.tabOrderNum" />
          </div>
          <div class="cgf-item--group-right">
            <a-button preIcon="ant-design:setting" :disabled="formModel.relationType === 1" @click="onOpenExtConfig">扩展配置</a-button>
            <a-button type="link" preIcon="ant-design:up-outlined" @click="expandStatus = !expandStatus">收起</a-button>
          </div>
        </div>
        <CgformFieldItem
          v-for="item in postGroupFields"
          :key="item.field"
          :label="item.label"
          :type="item.type"
          :model-value="(formModel as Recordable)[item.field]"
          :options="item.options"
          @update:model-value="(formModel as Recordable)[item.field] = $event"
        />
      </template>
      <!-- 附表类型收起：col1+col2空格 + col3全部内容-->
      <template v-else>
        <div class="cgf-spacer"></div>
        <div class="cgf-spacer"></div>
        <div class="cgf-item cgf-item--group">
          <div class="cgf-item--group-left">
            <a-radio-group v-model:value="formModel.relationType" :options="relationTypeOptions" @change="onRelationTypeChange" />
            <label class="cgf-label cgf-label--inline">序号</label>
            <a-input-number v-model:value="formModel.tabOrderNum" style="width: 50px" />
          </div>
          <div class="cgf-item--group-right">
            <a-button preIcon="ant-design:setting" :disabled="formModel.relationType === 1" @click="onOpenExtConfig">扩展配置</a-button>
            <a-button type="link" preIcon="ant-design:down-outlined" @click="expandStatus = !expandStatus">展开</a-button>
          </div>
        </div>
      </template>
    </template>
  </div>
  <ExtendConfigModal @register="registerExtendConfigModal" :parentForm="formActionForModal" @ok="onExtConfigOk" />
</template>

<script setup lang="ts">
  import { computed, reactive, ref, toRaw, inject } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { duplicateValidate } from '/@/utils/helper/validator';
  import ExtendConfigModal from './ExtendConfigModal.vue';
  import CgformFieldItem from './CgformFieldItem.vue';
  import { ExtConfigDefaultJson } from '../cgform.data';
  import { parseExtConfigJson } from '../util/utils';
  import { CgformPageType } from '../types';

  interface FieldItem {
    label: string;
    field: string;
    type: 'input' | 'select' | 'input-number' | 'radio-group';
    required?: boolean;
    disabled?: boolean;
    allowClear?: boolean;
    options?: { label: string; value: any }[];
    onChange?: (value: any) => void;
    show?: boolean;
    status?: '' | 'error';
  }

  const fieldErrors = reactive<Record<string, '' | 'error'>>({
    tableName: '',
    tableTxt: '',
    treeFieldname: '',
  });

  const emit = defineEmits<{
    (e: 'tableTypeChange', value: number): void;
    (e: 'relationTypeChange', value: number): void;
    (e: 'isTreeChange', value: string): void;
    (e: 'extConfigSaved', values: Recordable): void;
  }>();

  const { createMessage: $message } = useMessage();

  const pageType = inject<CgformPageType>('cgformPageType', CgformPageType.normal);

  const expandStatus = ref(false);

  // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-328】视图进入的编辑弹窗中的表类型和是否树都需禁用
  const cgformPageType = inject('cgformPageType', CgformPageType.normal);
  const isCopyMode = computed(() => cgformPageType === CgformPageType.copy);
  // update-end--author:liaozhiyang---date:20260713---for：【LHZP-328】视图进入的编辑弹窗中的表类型和是否树都需禁用

  const tableTypeOptions = [
    { label: '单表', value: 1 },
    { label: '主表', value: 2 },
    { label: '附表', value: 3 },
  ];
  const relationTypeOptions = [
    { label: '一对多', value: 0 },
    { label: '一对一', value: 1 },
  ];
  const isCheckboxOptions = [
    { label: '显示', value: 'Y' },
    { label: '不显示', value: 'N' },
  ];
  const themeTemplateOptions = [
    { label: '默认主题', value: 'normal' },
    { label: 'ERP主题(一对多)', value: 'erp' },
    { label: '内嵌子表主题(一对多)', value: 'innerTable' },
    { label: 'TAB主题(一对多)', value: 'tab' },
  ];
  const formTemplateOptions = [
    { label: '一列', value: '1' },
    { label: '两列', value: '2' },
    { label: '三列', value: '3' },
    { label: '四列', value: '4' },
  ];
  const scrollOptions = [
    { label: '有', value: 1 },
    { label: '无', value: 0 },
  ];
  const isPageOptions = [
    { label: '显示', value: 'Y' },
    { label: '不显示', value: 'N' },
  ];
  const isTreeOptions = [
    { label: '是', value: 'Y' },
    { label: '否', value: 'N' },
  ];

  const DEFAULT_FORM_MODEL = {
    id: '',
    tableVersion: null as any,
    tableName: '',
    tableTxt: '',
    tableType: 1,
    relationType: 0,
    tabOrderNum: null as any,
    idSequence: '',
    isCheckbox: 'Y',
    themeTemplate: 'normal',
    formTemplate: '1',
    scroll: 1,
    isPage: 'Y',
    isTree: 'N',
    treeParentIdField: '',
    treeIdField: '',
    treeFieldname: '',
    subTableStr: '',
    extConfigJson: '',
  };
  const formModel = reactive({ ...DEFAULT_FORM_MODEL });

  function onTableTypeChange(value: number) {
    if (value === 1) {
      formModel.themeTemplate = 'normal';
    } else if (value === 3) {
      formModel.isTree = 'N';
      emit('isTreeChange', 'N');
    }
    emit('tableTypeChange', value);
  }

  function onRelationTypeChange(e: any) {
    const value = e?.target?.value ?? e;
    emit('relationTypeChange', value);
  }

  function onIsTreeChange(value: string) {
    emit('isTreeChange', value);
  }

  let _tableNameLastMsg = '';
  function validateTableNameSync(val: string) {
    let msg = '';
    if (!val) {
      fieldErrors.tableName = 'error';
    } else if (/[\u4E00-\u9FA5]/g.test(val)) {
      fieldErrors.tableName = 'error';
      msg = '表名不允许输入中文';
    } else if (val.length > 50) {
      fieldErrors.tableName = 'error';
      msg = '表名最长50个字符';
    } else {
      fieldErrors.tableName = '';
    }
    if (msg && msg !== _tableNameLastMsg) {
      $message.warning(msg);
    }
    _tableNameLastMsg = msg;
  }

  function validateTableTxtSync(val: string) {
    fieldErrors.tableTxt = !val || val.length > 200 ? 'error' : '';
  }

  // 基础字段（始终显示）
  const baseFields = computed<FieldItem[]>(() => [
    {
      label: '表名',
      field: 'tableName',
      type: 'input',
      required: true,
      disabled: pageType === CgformPageType.copy || !!(formModel.tableVersion && formModel.tableVersion != 1),
      allowClear: true,
      status: fieldErrors.tableName,
      onChange: (e: any) => validateTableNameSync(e?.target?.value ?? e ?? ''),
    },
    {
      label: '表描述',
      field: 'tableTxt',
      type: 'input',
      required: true,
      allowClear: true,
      status: fieldErrors.tableTxt,
      onChange: (e: any) => validateTableTxtSync(e?.target?.value ?? e ?? ''),
    },
    {
      label: '表类型',
      field: 'tableType',
      type: 'select',
      options: tableTypeOptions,
      onChange: onTableTypeChange,
      disabled: isCopyMode.value,
    },
  ]);

  // 附表字段
  const subTableField: FieldItem = {
    label: '附表',
    field: 'subTableStr',
    type: 'input',
    disabled: true,
  };

  // 关联类型字段
  // const relationTypeField: FieldItem = {
  //   label: '关联类型',
  //   field: 'relationType',
  //   type: 'radio-group',
  //   options: relationTypeOptions,
  //   onChange: onRelationTypeChange,
  // };

  // 序号字段
  // const tabOrderNumField: FieldItem = {
  //   label: '序号',
  //   field: 'tabOrderNum',
  //   type: 'input-number',
  // };

  // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-183】主题模板为ERP时禁用复选框配置
  function onThemeTemplateChange(value: string) {
    // ERP 风格列表固定需要行选中，复选框强制显示且不可改
    if (value === 'erp') {
      formModel.isCheckbox = 'Y';
    }
  }
  // update-end--author:liaozhiyang---date:20260804---for：【LHZP-183】主题模板为ERP时禁用复选框配置

  // 展开后的通用字段
  const expandCommonFields = computed<FieldItem[]>(() => [
    // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-183】主题模板为ERP时禁用复选框配置
    { label: '复选框', field: 'isCheckbox', type: 'select', options: isCheckboxOptions, disabled: formModel.themeTemplate === 'erp' },
    // update-end--author:liaozhiyang---date:20260804---for：【LHZP-183】主题模板为ERP时禁用复选框配置
    { label: '表单风格', field: 'formTemplate', type: 'select', options: formTemplateOptions },
    { label: '滚动条', field: 'scroll', type: 'select', options: scrollOptions },
    { label: '分页', field: 'isPage', type: 'select', options: isPageOptions },
  ]);

  // 条件字段，根据 tableType 和 expandStatus 动态变化
  const conditionalFields = computed<FieldItem[]>(() => {
    const { tableType, isTree } = formModel;
    const expanded = expandStatus.value;

    // 附表 (tableType === 3)：完全由模板中的 preGroupFields/postGroupFields 处理
    if (tableType === 3) return [];

    // 主表 (tableType === 2)
    if (tableType === 2) {
      const fields: FieldItem[] = [{ ...subTableField, show: !!formModel.subTableStr }];
      if (expanded) {
        // update-begin--author:liaozhiyang---date:20260804---for【LHZP-183】主题模板为ERP时禁用复选框配置
        fields.push(
          ...expandCommonFields.value,
          { label: '主题模板', field: 'themeTemplate', type: 'select', options: themeTemplateOptions, onChange: onThemeTemplateChange }
        );
        // update-end--author:liaozhiyang---date:20260804---for：【LHZP-183】主题模板为ERP时禁用复选框配置
      }
      return fields;
    }

    // 单表 (tableType === 1)
    if (expanded) {
      const fields: FieldItem[] = [
        ...expandCommonFields.value,
        { label: '是否树', field: 'isTree', type: 'select', options: isTreeOptions, onChange: onIsTreeChange, disabled: isCopyMode.value },
      ];
      if (isTree === 'Y') {
        fields.push(
          { label: '树父ID', field: 'treeParentIdField', type: 'input' },
          { label: '树表单列', field: 'treeFieldname', type: 'input', required: true, status: fieldErrors.treeFieldname }
        );
      }
      return fields;
    }

    return [];
  });

  // 附表展开：关联类型+序号 group 前面的字段（isCheckbox, formTemplate → row2 col1-2）
  const preGroupFields = computed<FieldItem[]>(() => {
    if (formModel.tableType !== 3 || !expandStatus.value) return [];
    return expandCommonFields.value.slice(0, 2);
  });

  // 附表展开：关联类型+序号 group 后面的字段（scroll, isPage → row3 col1-2）
  const postGroupFields = computed<FieldItem[]>(() => {
    if (formModel.tableType !== 3 || !expandStatus.value) return [];
    return expandCommonFields.value.slice(2);
  });

  // 按钮是否与基础字段在同一行（无条件字段时）
  const isInlineLayout = computed(() => {
    if (expandStatus.value) return false;
    if (formModel.tableType === 1) return true;
    if (formModel.tableType === 2 && !formModel.subTableStr) return true;
    return false;
  });

  const FORM_FIELD_KEYS = Object.keys(DEFAULT_FORM_MODEL);

  function resetFields() {
    Object.assign(formModel, DEFAULT_FORM_MODEL);
    fieldErrors.tableName = '';
    fieldErrors.tableTxt = '';
  }

  function setFieldsValue(values: Recordable) {
    // update-begin--author:liaozhiyang---date:20260520---for:【QQYUN-15469】新加的表，创建时间是已打开过的表单的时间
    // 只接收白名单内的字段，避免被编辑的 record 的多余字段污染 formModel
    if (!values) return;
    for (const key of FORM_FIELD_KEYS) {
      if (key in values) {
        (formModel as Recordable)[key] = values[key];
      }
    }
    // update-end--author:liaozhiyang---date:20260520---for:【QQYUN-15469】新加的表，创建时间是已打开过的表单的时间
    // update-begin--author:liaozhiyang---date:20260804---for：主题模板为ERP时禁用复选框配置
    if (formModel.themeTemplate === 'erp') {
      formModel.isCheckbox = 'Y';
    }
    // update-end--author:liaozhiyang---date:20260804---for：主题模板为ERP时禁用复选框配置
  }

  function getFieldsValue(fields?: string[]) {
    if (fields) {
      return Object.fromEntries(fields.map((f) => [f, (formModel as Recordable)[f]]));
    }
    return { ...toRaw(formModel) };
  }

  async function validate() {
    fieldErrors.tableName = '';
    fieldErrors.tableTxt = '';
    fieldErrors.treeFieldname = '';
    if (!formModel.tableName) {
      fieldErrors.tableName = 'error';
      return Promise.reject('请输入表名');
    }
    if (/[\u4E00-\u9FA5]/g.test(formModel.tableName)) {
      fieldErrors.tableName = 'error';
      return Promise.reject('表名不允许输入中文');
    }
    if (formModel.tableName.length > 50) {
      fieldErrors.tableName = 'error';
      return Promise.reject('表名最长50个字符');
    }
    try {
      await duplicateValidate('onl_cgform_head', 'table_name', formModel.tableName, formModel.id);
    } catch (e: any) {
      fieldErrors.tableName = 'error';
      return Promise.reject('表名已存在');
    }
    if (!formModel.tableTxt) {
      fieldErrors.tableTxt = 'error';
      return Promise.reject('请输入表描述');
    }
    if (formModel.tableTxt.length > 200) {
      fieldErrors.tableTxt = 'error';
      return Promise.reject('表描述最长200个字');
    }
    if (formModel.isTree === 'Y' && formModel.tableType === 1 && !formModel.treeFieldname) {
      fieldErrors.treeFieldname = 'error';
      expandStatus.value = true;
      return Promise.reject('请填写树表单列');
    }
    if (extConfigJson.joinQuery && ['erp'].includes(formModel.themeTemplate)) {
      return Promise.reject('ERP不支持联合查询功能');
    }
    if (extConfigJson.joinQuery && ['innerTable'].includes(formModel.themeTemplate)) {
      return Promise.reject('内嵌子表不支持联合查询功能');
    }
    return getFieldsValue();
  }

  // 扩展配置 JSON
  let extConfigJson: Recordable = {};
  const [registerExtendConfigModal, extendConfigModal] = useModal();

  // ExtendConfigModal 需要的 parentForm 接口
  const formActionForModal = {
    getFieldsValue,
    setFieldsValue,
    validateFields: (_fields?: string[]) => Promise.resolve(),
  };

  function initialExtConfigJson(record: Recordable) {
    const parseJSON = parseExtConfigJson(record);
    extConfigJson = Object.assign({}, ExtConfigDefaultJson, parseJSON, {
      isDesForm: record.isDesForm || 'N',
      desFormCode: record.desFormCode || '',
      // update-begin--author:jeecg---date:20260512---for：【QQYUN-15337】online表单支持多数据源（dbSource 为 onl_cgform_head 独立列）
      dbSource: record.dbSource || '',
      enableMultiDataSource: record.dbSource ? 1 : 0,
      // update-end--author:jeecg---date:20260512---for：【QQYUN-15337】online表单支持多数据源
    });
  }

  function getExtConfigJson() {
    return extConfigJson;
  }

  function onOpenExtConfig() {
    extendConfigModal.openModal(true, { extConfigJson });
  }

  async function onExtConfigOk(values: Recordable) {
    extConfigJson = values;
    emit('extConfigSaved', values);
  }

  defineExpose({ formModel, resetFields, setFieldsValue, getFieldsValue, validate, initialExtConfigJson, getExtConfigJson });
</script>

<style lang="less" scoped>
  .cgform-fields {
    display: flex;
    flex-wrap: wrap;
    padding-top: 5px;
    :deep(.cgf-item) {
      width: 33.333%;
      display: flex;
      align-items: center;
      padding: 4px 8px;
      .cgf-label {
        flex-shrink: 0;
        width: 75px;
        text-align: right;
        padding-right: 8px;
        font-size: 14px;
        .cgf-required {
          color: #ff4d4f;
          margin-right: 2px;
        }
      }
    }
    .cgf-item--buttons {
      flex: 1;
      min-width: 33.333%;
      justify-content: flex-end;
      display: flex;
      align-items: center;
      padding: 4px 8px;
      // 附表收起时固定宽度（不 flex-grow）
      &-fixed {
        flex: 0 0 33.333%;
        min-width: unset;
      }
    }
    .cgf-spacer {
      width: 33.333%;
    }
    .cgf-item--group {
      width: 33.333%;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 4px 8px;
      padding-left: 34px;
      .cgf-label {
        width: 50px;
      }
      .cgf-item--group-left {
        display: flex;
        align-items: center;
      }
      .cgf-item--group-right {
        display: flex;
        align-items: center;
      }
    }
    // 无条件字段时，按钮与基础字段同行（单表收起 / 主表无附表收起）
    &--inline {
      flex-wrap: nowrap;
      :deep(.cgf-item) {
        flex: 1 0 0;
        width: auto;
        min-width: 0;
      }
      .cgf-item--buttons {
        flex: 0 0 auto;
        min-width: unset;
      }
    }
  }
  :deep(.ant-btn) {
    &.ant-btn-link {
      margin-right: -5px;
      padding-right: 5px;
    }
    & > .anticon + span {
      margin-left: 4px;
    }
  }
</style>
