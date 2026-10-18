<template>
  <JVxeTable
    ref="tableRef"
    rowNumber
    keyboardEdit
    :class="[prefixCls]"
    :maxHeight="tableHeight.noToolbar"
    :loading="loading"
    :columns="columns"
    :dataSource="dataSource"
    :disabledRows="{ dbFieldName: ['id', 'has_child'] }"
    v-bind="tableProps"
  >
    <template #fieldValidType="props">
      <a-row type="flex" :class="['row-valid-type', { full: !isCustomRegexp(props.value) }]">
        <a-col :class="['left']" @click="isCustomRegexp(props.value) ? onChangeCustomRegexp(props) : undefined">
          <a-select :value="props.value" :options="validTypeOptions" placeholder="空" style="width: 100%" @change="props.triggerChange" :virtual="false" :open="isCustomRegexp(props.value) ? false : undefined">
            <template #dropdownRender="{ menuNode }">
              <div class="menu">
                <VNodes :vnodes="menuNode" />
              </div>
              <div
                v-show="!isCustomRegexp(props.value)"
                class="custom-option-list rc-virtual-list-holder-inner"
                style="border-top: 1px solid #dfdfdf"
              >
                <div class="ant-select-item ant-select-item-option" title="使用自定义正则表达式作为校验规则" @click="onAddCustomRegexp(props)" @mousedown="e => e.preventDefault()">
                  正则表达式
                </div>
              </div>
            </template>
          </a-select>
        </a-col>
        <a-col class="right" title="修改自定义正则表达式">
          <a-button preIcon="ant-design:edit" @click="() => onChangeCustomRegexp(props)" />
        </a-col>
      </a-row>
    </template>
  </JVxeTable>
</template>

<script lang="ts">
  import { ref, defineComponent, computed } from 'vue';
  import { JVxeTypes, JVxeColumn } from '/@/components/jeecg/JVxeTable/types';
  import { useTableSync } from '../../hooks/useTableSync';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useJPrompt } from '/@/components/jeecg/JPrompt';
  import { useDesign } from '/@/hooks/web/useDesign';

  export default defineComponent({
    name: 'CheckDictTable',
    components: {
      VNodes: (_, { attrs }) => attrs.vnodes,
    },
    setup() {
      const { prefixCls } = useDesign('cgform-check-dict-table');
      const { createMessage: $message } = useMessage();
      // 定义列信息
      const columns = ref<JVxeColumn[]>([
        { title: '字段名称', key: 'dbFieldName', width: 100 },
        { title: '字段备注', key: 'dbFieldTxt', width: 100 },
        {
          title: '字段Href',
          key: 'fieldHref',
          width: 130,
          type: JVxeTypes.textarea,
          defaultValue: '',
        },
        {
          title: '验证规则',
          key: 'fieldValidType',
          width: 170,
          type: JVxeTypes.slot,
          slotName: 'fieldValidType',
          allowInput: true,
          defaultValue: '',
          placeholder: '空',
        },
        {
          title: '校验必填',
          key: 'fieldMustInput',
          width: 80,
          type: JVxeTypes.checkbox,
          align: 'center',
          customValue: ['1', '0'],
          defaultChecked: false,
        },
        {
          title: '定义转换器',
          key: 'converter',
          width: 350,
          type: JVxeTypes.input,
          defaultValue: '',
        },
      ]);
      
      const setup = useTableSync(columns);
      const { tableRef } = setup;
      
      const validTypeOptions = ref([
        { label: '空', value: '' },
        { label: '唯一校验', value: 'only' },
        { label: '6到16位数字', value: 'n6-16' },
        { label: '6到18位字母', value: 's6-18' },
        { label: '6到16位任意字符', value: '*6-16' },
        { label: '网址', value: 'url' },
        { label: '电子邮件', value: 'e' },
        { label: '手机号码', value: 'm' },
        { label: '邮政编码', value: 'p' },
        { label: '字母', value: 's' },
        { label: '数字', value: 'n' },
        { label: '整数', value: 'z' },
        { label: '非空', value: '*' },
        { label: '金额', value: 'money' },
      ]);
      const validTypeValues = computed(() => {
        return validTypeOptions.value.map((opt) => opt.value);
      });

      const { createJPrompt } = useJPrompt();

      /**
       * 判断是否是自定义正则表达式
       * @param value
       */
      function isCustomRegexp(value) {
        return value != null && !validTypeValues.value.includes(value);
      }

      // 添加自定义正则表达式
      function onAddCustomRegexp(props) {
        createJPrompt({
          title: '自定义正则表达式',
          placeholder: '请输入正则表达式',
          rules: [{ required: true, message: '正则表达式不能为空！' }, { validator: validatorCustomRegexp }],
          onOk: (value) => {
            props.triggerChange(value);
            $message.success('添加成功');
          },
        });
      }

      // 更改自定义正则表达式
      function onChangeCustomRegexp(props) {
        createJPrompt({
          title: '修改自定义正则表达式',
          defaultValue: props.value,
          placeholder: '请输入正则表达式',
          rules: [{ required: true, message: '正则表达式不能为空！' }, { validator: validatorCustomRegexp }],
          onOk: (value) => {
            props.triggerChange(value);
            if (value !== props.value) {
              $message.success('修改成功');
            }
          },
        });
      }

      // 校验自定义正则表达式
      function validatorCustomRegexp(_, value) {
        if (isCustomRegexp(value)) {
          return Promise.resolve();
        } else {
          return Promise.reject('当前校验已存在');
        }
      }
      // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8485】数据库不允许为空，校验默认相应勾上
      function syncFieldMustInput(row) {
        if (row.dbIsNull === '0') {
          tableRef.value!.setValues([
            {
              rowKey: row.id,
              values: { fieldMustInput: '1' },
            },
          ]);
        }
      }
      // update-end--author:liaozhiyang---date:20240313---for：【QQYUN-8485】数据库不允许为空，校验默认相应勾上
      return {
        ...setup,
        prefixCls,
        columns,
        isCustomRegexp,
        validTypeOptions,
        validTypeValues,

        onAddCustomRegexp,
        onChangeCustomRegexp,
        syncFieldMustInput,
      };
    },
  });
</script>
<style lang="less" scoped>
  //noinspection LessUnresolvedVariable
  // --------------------- begin 自定义正则表达式插槽样式 ------------------
  .row-valid-type {
    .left,
    .right {
      &:hover {
        z-index: 3;
      }
    }

    &.full {
      .right {
        display: none;
      }
    }

    .left {
      flex: 1;
      z-index: 2;
      width: calc(100% - 33px);

      :deep(.ant-select){
        .ant-select-selector {
          border-width: 0 !important;
          border-color: transparent !important;
          box-shadow: none !important;
        }
        .ant-select-arrow {
          display: none;
        }
      }

      :deep(.ant-select:not(.ant-select-open)){
        .ant-select-selector {
          background-color: transparent !important;
        }

        .ant-select-arrow {
          display: none;
        }
      }
    }

    .right {
      width: 33px;
      z-index: 1;

      :deep(.ant-btn){
        padding: 4px 8px;
        border-top-left-radius: 0;
        border-bottom-left-radius: 0;
        border: none;
        background-color: transparent;
        box-shadow: none !important;
      }
    }
  }
  // update-begin--author:liaozhiyang---date:20240807---for：【TV360X-497】表单校验类型选择最后项，二次打开偶尔显示不全
  .menu {
    :deep(.rc-virtual-list-holder) {
      overflow-y: hidden !important;
      &:hover {
        overflow-y: auto !important;
      }
      &::-webkit-scrollbar-track {
        background-color: transparent;
      }
    }
  }
  // update-end--author:liaozhiyang---date:20240807---for：【TV360X-497】表单校验类型选择最后项，二次打开偶尔显示不全
  // --------------------- end 自定义正则表达式插槽样式 ------------------
</style>
