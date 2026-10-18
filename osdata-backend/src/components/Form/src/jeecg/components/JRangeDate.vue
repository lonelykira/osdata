<template>
    <a-range-picker v-model:value="rangeValue" @change="handleChange" :show-time="datetime" :placeholder="placeholder" :picker="picker" :format="format || valueFormat" :valueFormat="valueFormat"/>
</template>

<script>
    import { defineComponent, ref, watch, computed } from 'vue';
    import { propTypes } from '/@/utils/propTypes';
    import { Form } from 'ant-design-vue';
    import dayjs from 'dayjs';
    import isoWeek from 'dayjs/plugin/isoWeek';
    import quarterOfYear from 'dayjs/plugin/quarterOfYear';

    dayjs.extend(isoWeek);
    dayjs.extend(quarterOfYear);

    const placeholder = ['开始日期', '结束日期']
    /**
     * 用于范围查询
     */
    export default defineComponent({
        name: "JRangeDate",
        props:{
            value: propTypes.string.def(''),
            datetime: propTypes.bool.def(false),
            placeholder: propTypes.string.def(''),
            picker: propTypes.string.def('date'),
            format: propTypes.string.def(''),
            valueFormat: propTypes.string.def(''),
        },
        emits:['change', 'update:value'],
        setup(props, {emit}){
            const rangeValue = ref([])
            const formItemContext = Form.useInjectFormItemContext();

            watch(()=>props.value, (val)=>{
                if(val){
                    rangeValue.value = val.split(',')
                }else{
                    rangeValue.value = []
                }
            }, {immediate: true});

            const valueFormat = computed(()=>{
                if(props.valueFormat){
                    return props.valueFormat
                }
                if(props.datetime === true){
                    return 'YYYY-MM-DD HH:mm:ss'
                }else{
                    return 'YYYY-MM-DD'
                }
            });

            function handleChange(arr){
                let str = ''
                if(arr && arr.length>0){
                  // 代码逻辑说明: [issues/6368] rangeDate去掉判断允许起始项或结束项为空兼容allowEmpty
                  // update-begin--author:wangshuai---date:20260819---for：【LHZP-322】【表单设计器】支持周、季度
                  str = arr.map(normalizePickerValue).join(',')
                  // update-end--author:wangshuai---date:20260819---for：【LHZP-322】【表单设计器】支持周、季度
                }
                emit('change', str);
                emit('update:value', str);
                formItemContext.onFieldChange();
            }

          /**
           * 返回季度 周 格式化之后的代码
           * @param value
           * @returns {*|string}
           */
            function normalizePickerValue(value){
                if(!value){
                    return value
                }
                if(props.picker === 'quarter'){
                    return dayjs(value).startOf('quarter').format('YYYY-MM-DD')
                }
                if(props.picker === 'week'){
                    return dayjs(value).startOf('isoWeek').format('YYYY-MM-DD')
                }
                return value
            }
            
            return {
                rangeValue,
                placeholder,
                valueFormat,
                handleChange
            }
        }
    });
</script>

<style scoped>

</style>
