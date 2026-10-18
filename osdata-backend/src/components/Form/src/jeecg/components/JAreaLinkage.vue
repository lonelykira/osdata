<template>
  <JCascader
    v-bind="attrs"
    :disabled="disabled"
    :value="cascaderValue"
    :showLastLevelOnly="showLastLevelOnly"
    :options="getOptions"
    @change="handleChange"
  />
</template>
<script lang="ts">
  import { defineComponent, ref, watchEffect, computed } from 'vue';
  import JCascader from './JCascader.vue';
  import { Cascader } from 'ant-design-vue';
  import { provinceAndCityData, regionData, provinceOptions } from '../../utils/areaDataUtil';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { isArray } from '/@/utils/is';

  export default defineComponent({
    name: 'JAreaLinkage',
    components: {
      JCascader,
      Cascader,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.object, propTypes.array, propTypes.string]),
      // 显示层级：province 只显示省，city 显示省市，region 显示省市县（优先于 showArea）
      displayLevel: propTypes.oneOf(['province', 'city', 'region', 'all']),
      //是否显示区县
      showArea: propTypes.bool.def(true),
      // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-707】修复省市区禁用状态下还可以点击x删除
      disabled: propTypes.bool.def(false),
      // update-end--author:liaozhiyang---date:20260804---for：【LHZP-707】修复省市区禁用状态下还可以点击x删除
      // 存储数据 （all时：传递到外面的是数组；province, city, region传递外面的是字符串）
      saveCode: propTypes.oneOf(['province', 'city', 'region', 'all']).def('all'),
    },
    emits: ['options-change', 'change', 'update:value'],
    setup(props, { emit }) {
      // update-begin--author:liaozhiyang---date:20260204---for:【QQYUN-14694】online支持配置独立的省、市、县
      const showLastLevelOnly = computed(() => {
        return props.displayLevel === 'province' || props.displayLevel === 'city' || props.displayLevel === 'region';
      });
      // update-end--author:liaozhiyang---date:20260204---for:【QQYUN-14694】online支持配置独立的省、市、县
      const attrs = useAttrs();
      // const [state] = useRuleFormItem(props, 'value', 'change', emitData);
      const cascaderValue = ref<(string | number)[]>([]);
      const getOptions = computed(() => {
        // update-begin--author:liaozhiyang---date:20260204---for:【QQYUN-14694】online支持配置独立的省、市、县
        if (props.displayLevel) {
          if (props.displayLevel === 'all') {
            return regionData;
          } else if (props.displayLevel === 'province') {
            return provinceOptions;
          } else if (props.displayLevel === 'city') {
            return provinceAndCityData;
          } else if (props.displayLevel === 'region') {
            return regionData;
          }
        } else {
          if (props.showArea) {
            return regionData;
          } else {
            return provinceAndCityData;
          }
        }
        // update-end--author:liaozhiyang---date:20260204---for:【QQYUN-14694】online支持配置独立的省、市、县
      });
      /**
       * 监听value变化
       */
      watchEffect(() => {
        // 代码逻辑说明: 【TV360X-1223】省市区换新组件
        if (props.value) {
          initValue();
        } else {
          cascaderValue.value = [];
        }
      });

      /**
       * 老数据可能是区县码（如 120101），displayLevel 为 province 时需显示对应省，
       */
      function buildDisplayPathFromCode(code, displayLevel) {
        if (!code && code !== 0) return [];
        const str = String(code).trim();
        if (!str) return [];
        const provinceCode = str.length >= 2 ? str.substring(0, 2) + '0000' : str;
        const cityCode = str.length >= 4 ? str.substring(0, 4) + '00' : null;
        const regionCode = str.length >= 6 ? str : null;
        const fullPath = [provinceCode];
        if (cityCode && cityCode !== provinceCode) fullPath.push(cityCode);
        if (regionCode && regionCode !== cityCode) fullPath.push(regionCode);
        if (displayLevel === 'province') return fullPath.slice(0, 1);
        if (displayLevel === 'city') return fullPath.slice(0, 2);
        return fullPath;
      }
      /**
       * 将字符串值转化为数组
       */
      function initValue() {
        let value = props.value ? props.value : [];
        // 代码逻辑说明: 【TV360X-501】省市区换新组件
        if (value && typeof value === 'string' && value != 'null' && value != 'undefined') {
          const arr = value.split(',');
          if (props.displayLevel) {
            // update-begin--author:liaozhiyang---date:20260204---for:【QQYUN-14694】online支持配置独立的省、市、县
            const code = arr[0];
            cascaderValue.value = buildDisplayPathFromCode(code, props.displayLevel);
            // update-end--author:liaozhiyang---date:20260204---for:【QQYUN-14694】online支持配置独立的省、市、县
          } else {
            cascaderValue.value = transform(arr);
          }
        } else if (isArray(value)) {
          if (value.length) {
            if (props.displayLevel) {
              // update-begin--author:liaozhiyang---date:20260204---for:【QQYUN-14694】online支持配置独立的省、市、县
              // 老数据 saveCode 为 all 时存的是完整路径数组 [省,市,区]，直接按 displayLevel 截断
              if (value.length >= 2) {
                const len = props.displayLevel === 'province' ? 1 : props.displayLevel === 'city' ? 2 : Math.min(3, value.length);
                cascaderValue.value = value.slice(0, len);
              } else {
                const code = value[0];
                cascaderValue.value = buildDisplayPathFromCode(code, props.displayLevel);
              }
              // update-end--author:liaozhiyang---date:20260204---for:【QQYUN-14694】online支持配置独立的省、市、县
            } else {
              cascaderValue.value = transform(value);
            }
          } else {
            cascaderValue.value = [];
          }
        }
      }
      function transform(arr) {
        let result: any = [];
        if (props.saveCode === 'region') {
          const regionCode = arr[0];
          result = [`${regionCode.substring(0, 2)}0000`, `${regionCode.substring(0, 2)}${regionCode.substring(2, 4)}00`, regionCode];
        } else if (props.saveCode === 'city') {
          const cityCode = arr[0];
          result = [`${cityCode.substring(0, 2)}0000`, cityCode];
        } else if (props.saveCode === 'province') {
          const provinceCode = arr[0];
          result = [provinceCode];
        } else {
          result = arr;
        }
        return result;
      }
      /**
       * liaozhiyang
       * 2024-06-17
       * 【TV360X-1224】省市区组件默认传到外面的值是字符串逗号分隔
       * */
      const send = (data) => {
        let result = data;
        if (result) {
          if (props.saveCode === 'all') {
            // 传递的是数组
          } else {
            // 传递的是字符串
            result = data.join(',');
          }
        }
        emit('change', result);
        emit('update:value', result);
      };

      function handleChange(arr) {
        // 代码逻辑说明: 【TV360X-501】省市区换新组件
        if (arr?.length) {
          let result: any = [];
          if (props.saveCode === 'region') {
            // 可能只有两位（选择香港时，只有省区）
            result = [arr[arr.length - 1]];
          } else if (props.saveCode === 'city') {
            result = [arr[1]];
          } else if (props.saveCode === 'province') {
            result = [arr[0]];
          } else {
            result = arr;
          }
          send(result);
        } else {
          send(arr);
        }
        // emitData.value = args;
        // 上面改的v-model:value导致选中数据没有显示
        // state.value = result;
      }
      
      return {
        cascaderValue,
        attrs,
        regionData,
        getOptions,
        handleChange,
        showLastLevelOnly,
      };
    },
  });
</script>
