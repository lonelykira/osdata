<!-- 此文件已没有使用的地方 -->
<template>
  <a-form-item :labelCol="labelCol" :class="'jeecg-online-search'">
    <template #label>
      <span :title="item.label" class="label-text">{{ item.label }}</span>
    </template>

    <!-- 1.日期 -->
    <template v-if="item.view == 'date'">
      <template v-if="single_mode === item.mode">
        <a-date-picker
          style="width: 100%"
          :showTime="false"
          valueFormat="YYYY-MM-DD"
          :placeholder="'请选择' + item.label"
          v-model:value="innerValue"
        ></a-date-picker>
      </template>
      <template v-else>
        <a-date-picker
          :showTime="false"
          valueFormat="YYYY-MM-DD"
          placeholder="开始日期"
          v-model:value="beginValue"
          style="width: calc(50% - 15px)"
        ></a-date-picker>
        <span class="group-query-strig">~</span>
        <a-date-picker
          :showTime="false"
          valueFormat="YYYY-MM-DD"
          placeholder="结束日期"
          v-model:value="endValue"
          style="width: calc(50% - 15px)"
        ></a-date-picker>
      </template>
    </template>

    <!-- 2.时间 -->
    <template v-else-if="item.view == 'datetime'">
      <template v-if="single_mode === item.mode">
        <a-date-picker
          style="width: 100%"
          :showTime="true"
          valueFormat="YYYY-MM-DD hh:mm:ss"
          :placeholder="'请选择' + item.label"
          v-model:value="innerValue"
        ></a-date-picker>
      </template>
      <template v-else>
        <a-date-picker
          :showTime="true"
          valueFormat="YYYY-MM-DD hh:mm:ss"
          placeholder="开始时间"
          v-model:value="beginValue"
          style="width: calc(50% - 15px)"
        ></a-date-picker>
        <span class="group-query-strig">~</span>
        <a-date-picker
          :showTime="true"
          valueFormat="YYYY-MM-DD hh:mm:ss"
          placeholder="结束时间"
          v-model:value="endValue"
          style="width: calc(50% - 15px)"
        ></a-date-picker>
      </template>
    </template>

    <!-- 3 TODO 时分秒 -->

    <!-- 4.简单下拉框 -->
    <template v-else-if="isEasySelect()">
      <JDictSelectTag v-if="item.config === '1'" :placeholder="'请选择' + item.label" v-model:value="innerValue" :dictCode="getDictCode()" />
      <a-select v-else :placeholder="'请选择' + item.label" v-model:value="innerValue">
        <template v-for="(obj, index) in dictOptions[getDictOptionKey(item)]" :key="index">
          <a-select-option :value="obj.value"> {{ obj.text }}</a-select-option>
        </template>
      </a-select>
    </template>

    <!-- 5.下拉树 -->
    <template v-else-if="item.view === 'sel_tree'">
      <JTreeSelect
        :placeholder="'请选择' + item.label"
        v-model:value="innerValue"
        :dict="item.dict"
        :pidField="item.pidField"
        :pidValue="item.pidValue"
        :hasChildField="item.hasChildField"
        load-triggle-change
      >
      </JTreeSelect>
    </template>

    <!-- 6.分类树  -->
    <template v-else-if="item.view === 'cat_tree'">
      <JCategorySelect
        @change="handleCategoryTreeChange"
        :loadTriggleChange="true"
        :pcode="item.pcode"
        v-model:value="innerValue"
        :placeholder="'请选择' + item.label"
      />
    </template>

    <!-- 7.下拉搜索 -->
    <template v-else-if="item.view === 'sel_search'">
      <JDictSelectTag v-if="item.config === '1'" v-model:value="innerValue" :placeholder="'请选择' + item.label" :dict="getDictCode()" />
      <JOnlineSearchSelect v-else v-model:value="innerValue" :placeholder="'请选择' + item.label" :sql="getSqlByDictCode()" />
    </template>

    <!-- 8.用户 -->
    <JSelectUser
      v-else-if="item.view == 'sel_user'"
      v-bind="userSelectProp"
      v-model:value="innerValue"
      :placeholder="'请选择' + item.label"
    ></JSelectUser>

    <!-- 9.部门 -->
    <JSelectDept
      v-else-if="item.view == 'sel_depart'"
      :showButton="false"
      v-bind="depSelectProp"
      v-model:value="innerValue"
      :placeholder="'请选择' + item.label"
    />

    <!-- 10.popup -->
    <JPopup
      v-else-if="item.view == 'popup'"
      :placeholder="'请选择' + item.label"
      v-model:value="innerValue"
      :code="item.dictTable"
      :setFieldsValue="setFieldsValue"
      :field-config="getPopupFieldConfig(item)"
      :multi="true"
    >
    </JPopup>

    <!-- 11.省市区 -->
    <JAreaSelect v-else-if="item.view == 'pca'" :placeholder="'请选择' + item.label" v-model:value="innerValue" />

    <!-- 12.下拉多选 -->
    <template v-else-if="item.view == 'checkbox' || item.view == 'list_multi'" :label="item.label">
      <JSelectMultiple :dictCode="getDictCode()" :placeholder="'请选择' + item.label" v-model:value="innerValue"></JSelectMultiple>

      <!--<JDictSelectTag mode="multiple" @change="handleSelectChange"  :dictCode="getDictCode()"/>-->
    </template>

    <!-- 13.普通输入框 -->
    <template v-else>
      <template v-if="single_mode === item.mode">
        <a-input :placeholder="'请选择' + item.label" v-model:value="innerValue"></a-input>
      </template>
      <template v-else>
        <a-input placeholder="开始值" v-model:value="beginValue" style="width: calc(50% - 15px)"></a-input>
        <span class="group-query-strig">~</span>
        <a-input placeholder="结束值" v-model:value="endValue" style="width: calc(50% - 15px)"></a-input>
      </template>
    </template>
  </a-form-item>
</template>

<script lang="ts">
  import { defineComponent, nextTick, ref, unref, watch, toRaw } from 'vue';
  import {
    JDictSelectTag,
    JTreeSelect,
    JSearchSelect,
    JCategorySelect,
    JSelectUserByDept,
    JSelectDept,
    JPopup,
    JAreaLinkage,
    JSelectUser,
    JSelectMultiple,
    JAreaSelect,
    FormActionType,
  } from '/@/components/Form';
  import JOnlineSearchSelect from '../../auto/comp/JOnlineSearchSelect.vue';

  export default defineComponent({
    name: 'OnlineSearchFormItem',
    components: {
      JOnlineSearchSelect,
      JDictSelectTag,
      JTreeSelect,
      JCategorySelect,
      JSelectUser,
      JSelectUserByDept,
      JSelectDept,
      JPopup,
      JAreaLinkage,
      JAreaSelect,
      JSelectMultiple,
    },
    props: {
      value: {
        type: String,
        default: '',
      },
      item: {
        type: Object,
        default: () => {},
        required: true,
      },
      dictOptions: {
        type: Object,
        default: () => {},
        required: false,
      },
      onlineForm: {
        type: Object,
        default: () => {},
        required: false,
      },
    },
    emits: ['update:value', 'change'],
    setup(props, { emit }) {
      // 定义查询条件 文本label的最大宽度 比起单纯的控制字体个数更好
      const labelTextMaxWidth = '120px';
      const labelCol = {
        style: {
          'max-width': labelTextMaxWidth,
        },
      };
      const single_mode = 'single';
      let innerValue = ref<string | undefined | []>('');
      let beginValue = ref('');
      let endValue = ref('');

      watch(
        () => props.value,
        () => {
          if (isEasySelect()) {
            // 下拉框这里设置空数组 不知道为什么会有警告
            innerValue.value = !!props.value ? props.value : undefined;
          } else {
            innerValue.value = props.value;
          }
          if (!props.value) {
            beginValue.value = '';
            endValue.value = '';
          }
        },
        { deep: true, immediate: true }
      );

      watch(
        innerValue,
        (newVal) => {
          console.log('innerValue-change', newVal);
          emit('update:value', newVal);
        },
        { immediate: true }
      );

      watch(beginValue, (newVal) => {
        emit('change', props.item.field + '_begin', newVal);
        emit('update:value', '1');
      });

      watch(endValue, (newVal) => {
        emit('change', props.item.field + '_end', newVal);
        emit('update:value', '1');
      });

      function getDictOptionKey(item) {
        console.log('ddictOptions', props.dictOptions);
        if (item.dbField) {
          return item.dbField;
        } else {
          return item.field;
        }
      }

      function isEasySelect() {
        let item = props.item;
        if (!item) {
          return false;
        }
        return item.view == 'list' || item.view == 'radio' || item.view == 'switch';
      }

      function getDictCode() {
        let item = props.item;
        if (item.dictTable && item.dictTable.length > 0) {
          return item.dictTable + ',' + item.dictText + ',' + item.dictCode;
        } else {
          return item.dictCode;
        }
      }

      function getSqlByDictCode() {
        let item = props.item;
        let { dictTable, dictCode, dictText } = item;
        let temp = dictTable.toLowerCase();
        let arr = temp.split('where');
        let condition = '';
        if (arr.length > 1) {
          condition = ' where' + arr[1];
        }
        let sql = 'select ' + dictCode + " as 'value', " + dictText + " as 'text' from " + arr[0] + condition;
        console.log('sql', sql);
        return sql;
      }

      function getPopupFieldConfig(item) {
        let { dictText: destFields, dictCode: orgFields } = item;
        if (!destFields || destFields.length == 0) {
          return [];
        }
        let arr1 = destFields.split(',');
        let arr2 = orgFields.split(',');
        let config: any[] = [];
        for (let i = 0; i < arr1.length; i++) {
          config.push({
            target: arr1[i],
            source: arr2[i],
          });
        }
        return config;
      }

      function setFieldsValue<T>(values: T) {
        let { dictText: destFields } = props.item;
        let arr1 = destFields.split(',');
        let field = arr1[0];
        emit('change', field, values[field]);
      }

      function handleCategoryTreeChange(value) {
        emit('update:value', value);
      }

      function getComponentProps(item, labelKey, rowKey) {
        let props = {
          labelKey,
          rowKey,
        };
        let fieldExtendJson = item.fieldExtendJson;
        if (fieldExtendJson) {
          if (typeof fieldExtendJson == 'string') {
            let json = JSON.parse(fieldExtendJson);
            let extend = { ...json };
            if (extend.text) {
              props['labelKey'] = extend.text;
            }
            if (extend.store) {
              props['rowKey'] = extend.store;
            }
          }
        }
        return props;
      }

      let userSelectProp = getComponentProps(props.item, 'realname', 'username');
      console.log('userSelectProp', userSelectProp);
      let depSelectProp = getComponentProps(props.item, 'departName', 'id');

      function handleSelectChange(array) {
        if (array && array.length > 0) {
          emit('update:value', array.join(','));
        } else {
          emit('update:value', '');
        }
      }

      return {
        getPopupFieldConfig,
        userSelectProp,
        depSelectProp,
        handleSelectChange,
        setFieldsValue,
        innerValue,
        beginValue,
        endValue,
        isEasySelect,
        getDictOptionKey,
        getDictCode,
        labelTextMaxWidth,
        labelCol,
        single_mode,
        getSqlByDictCode,
        handleCategoryTreeChange,
      };
    },
  });
</script>

<style lang="less" scoped>
  .group-query-strig {
    width: 30px;
    text-align: center;
    display: inline-block;
  }
  /* 查询条件左对齐样式设置 */
  .jeecg-online-search :deep(.ant-form-item-label) {
    flex: 0 0 auto !important;
    width: auto;
  }
  .jeecg-online-search :deep(.ant-form-item-control) {
    max-width: 100%;
    padding-right: 16px;
  }

  /* label显示宽度 超出显示... */
  .jeecg-online-search :deep(.label-text) {
    max-width: v-bind(labelTextMaxWidth);
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow-wrap: break-word;
  }
</style>
