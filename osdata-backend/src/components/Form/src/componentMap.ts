/**
 * 目前实现了异步加载的组件清单（antd 组件已改为 createAsyncComponent 异步加载）
 */
import type { Component } from 'vue';
import type { ComponentType } from './types/index';
import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
/**
 * Component list, register here to setting it in the form
 */

const componentMap = new Map<ComponentType, Component>();

componentMap.set('Time', createAsyncComponent(() => import('/@/components/Time/src/Time.vue')));
componentMap.set('Input', createAsyncComponent(() => import('ant-design-vue/es/input')));
componentMap.set('InputGroup', createAsyncComponent(() => import('ant-design-vue/es/input/Group')));
componentMap.set('InputPassword', createAsyncComponent(() => import('ant-design-vue/es/input/Password')));
componentMap.set('InputSearch', createAsyncComponent(() => import('ant-design-vue/es/input/Search')));
componentMap.set('InputTextArea', createAsyncComponent(() => import('ant-design-vue/es/input/TextArea')));
componentMap.set('InputNumber', createAsyncComponent(() => import('ant-design-vue/es/input-number')));
componentMap.set('AutoComplete', createAsyncComponent(() => import('ant-design-vue/es/auto-complete')));

componentMap.set('Select', createAsyncComponent(() => import('ant-design-vue/es/select')));
componentMap.set('ApiSelect', createAsyncComponent(() => import('./components/ApiSelect.vue')));
componentMap.set('JTabsSelectUser', createAsyncComponent(() => import('@/components/jeecg/JTabsSelectUser/index.vue')));
componentMap.set('TreeSelect', createAsyncComponent(() => import('ant-design-vue/es/tree-select')));
componentMap.set('ApiTreeSelect', createAsyncComponent(() => import('./components/ApiTreeSelect.vue')));
componentMap.set('ApiRadioGroup', createAsyncComponent(() => import('./components/ApiRadioGroup.vue')));
componentMap.set('Switch', createAsyncComponent(() => import('ant-design-vue/es/switch')));
componentMap.set('RadioButtonGroup', createAsyncComponent(() => import('./components/RadioButtonGroup.vue')));
componentMap.set('RadioGroup', createAsyncComponent(() => import('ant-design-vue/es/radio/Group')));
componentMap.set('Checkbox', createAsyncComponent(() => import('ant-design-vue/es/checkbox')));
componentMap.set('CheckboxGroup', createAsyncComponent(() => import('ant-design-vue/es/checkbox/Group')));
componentMap.set('Cascader', createAsyncComponent(() => import('ant-design-vue/es/cascader')));
componentMap.set('Slider', createAsyncComponent(() => import('ant-design-vue/es/slider')));
componentMap.set('Rate', createAsyncComponent(() => import('ant-design-vue/es/rate')));

componentMap.set('DatePicker', createAsyncComponent(() => import('ant-design-vue/es/date-picker')));
componentMap.set('MonthPicker', createAsyncComponent(() => import('ant-design-vue/es/date-picker').then((m) => m.default.MonthPicker)));
componentMap.set('RangePicker', createAsyncComponent(() => import('ant-design-vue/es/date-picker').then((m) => m.default.RangePicker)));
componentMap.set('WeekPicker', createAsyncComponent(() => import('ant-design-vue/es/date-picker').then((m) => m.default.WeekPicker)));
componentMap.set('TimePicker', createAsyncComponent(() => import('ant-design-vue/es/time-picker')));
componentMap.set('DatePickerInFilter', createAsyncComponent(() => import('@/components/InFilter/DatePickerInFilter.vue')));
componentMap.set('JDatePickerMultiple', createAsyncComponent(() => import('./jeecg/components/JDatePickerMultiple.vue')));
componentMap.set('StrengthMeter', createAsyncComponent(() => import('/@/components/StrengthMeter/src/StrengthMeter.vue')));
componentMap.set('IconPicker', createAsyncComponent(() => import('/@/components/Icon/src/IconPicker.vue')));
componentMap.set('InputCountDown', createAsyncComponent(() => import('/@/components/CountDown/src/CountdownInput.vue')));

componentMap.set('Upload', createAsyncComponent(() => import('/@/components/Upload/src/BasicUpload.vue')));
componentMap.set('Divider', createAsyncComponent(() => import('ant-design-vue/es/divider')));

//注册自定义组件

componentMap.set('JAreaLinkage', createAsyncComponent(() => import('./jeecg/components/JAreaLinkage.vue')));
componentMap.set('JSelectPosition', createAsyncComponent(() => import('./jeecg/components/JSelectPosition.vue')));
componentMap.set('JSelectUser', createAsyncComponent(() => import('./jeecg/components/JSelectUser.vue')));
componentMap.set('JSelectRole', createAsyncComponent(() => import('./jeecg/components/JSelectRole.vue')));
componentMap.set('JImageUpload', createAsyncComponent(() => import('./jeecg/components/JImageUpload.vue')));
componentMap.set('JDictSelectTag', createAsyncComponent(() => import('./jeecg/components/JDictSelectTag.vue')));
componentMap.set('JSelectDept', createAsyncComponent(() => import('./jeecg/components/JSelectDept.vue')));
componentMap.set('JAreaSelect', createAsyncComponent(() => import('./jeecg/components/JAreaSelect.vue')));
// update-begin--author:liaozhiyang---date:20260227---for:【QQYUN-14751】tinymce富文本、JEasyCron、JLinkTableCard异步加载
componentMap.set('JLinkTableCard', createAsyncComponent(() => import('./jeecg/components/JLinkTableCard/JLinkTableCard.vue'), { loading: true }));
componentMap.set('JEditor', createAsyncComponent(() => import('./jeecg/components/JEditor.vue')));
// update-end--author:liaozhiyang---date:20260227---for:【QQYUN-14751】tinymce富文本、JEasyCron、JLinkTableCard异步加载
componentMap.set('JMarkdownEditor', createAsyncComponent(() => import('./jeecg/components/JMarkdownEditor.vue')));
componentMap.set('JSelectInput', createAsyncComponent(() => import('./jeecg/components/JSelectInput.vue')));
componentMap.set('JCodeEditor', createAsyncComponent(() => import('./jeecg/components/JCodeEditor.vue')));
componentMap.set('JCategorySelect', createAsyncComponent(() => import('./jeecg/components/JCategorySelect.vue')));
componentMap.set('JSelectMultiple', createAsyncComponent(() => import('./jeecg/components/JSelectMultiple.vue')));
componentMap.set('JSelectSingle', createAsyncComponent(() => import('./jeecg/components/JSelectSingle.vue')));
componentMap.set('JPopup', createAsyncComponent(() => import('./jeecg/components/JPopup.vue')));
// 代码逻辑说明: 【QQYUN-7961】popupDict字典
componentMap.set('JPopupDict', createAsyncComponent(() => import('./jeecg/components/JPopupDict.vue')));
componentMap.set('JSwitch', createAsyncComponent(() => import('./jeecg/components/JSwitch.vue')));
componentMap.set('JTreeDict', createAsyncComponent(() => import('./jeecg/components/JTreeDict.vue')));
componentMap.set('JInputPop', createAsyncComponent(() => import('./jeecg/components/JInputPop.vue')));
componentMap.set('JEasyCron', createAsyncComponent(() => import('./jeecg/components/JEasyCron/EasyCronInput.vue')));
componentMap.set('JCheckbox', createAsyncComponent(() => import('./jeecg/components/JCheckbox.vue')));
componentMap.set('JInput', createAsyncComponent(() => import('./jeecg/components/JInput.vue')));
componentMap.set('JTreeSelect', createAsyncComponent(() => import('./jeecg/components/JTreeSelect.vue')));
componentMap.set('JEllipsis', createAsyncComponent(() => import('./jeecg/components/JEllipsis.vue')));
componentMap.set('JSelectUserByDept', createAsyncComponent(() => import('./jeecg/components/JSelectUserByDept.vue')));
componentMap.set('JSelectUserByDepartment', createAsyncComponent(() => import('./jeecg/components/JSelectUserByDepartment.vue')));
componentMap.set('JUpload', createAsyncComponent(() => import('./jeecg/components/JUpload/JUpload.vue')));
componentMap.set('JSearchSelect', createAsyncComponent(() => import('./jeecg/components/JSearchSelect.vue')));
componentMap.set('JAddInput', createAsyncComponent(() => import('./jeecg/components/JAddInput.vue')));
componentMap.set('JRangeNumber', createAsyncComponent(() => import('./jeecg/components/JRangeNumber.vue')));
componentMap.set('CascaderPcaInFilter', createAsyncComponent(() => import('@/components/InFilter/CascaderPcaInFilter.vue')));
componentMap.set('UserSelect', createAsyncComponent(() => import('./jeecg/components/userSelect/index.vue')));
componentMap.set('RangeDate', createAsyncComponent(() => import('./jeecg/components/JRangeDate.vue')));
componentMap.set('RangeTime', createAsyncComponent(() => import('./jeecg/components/JRangeTime.vue')));
componentMap.set('RoleSelect', createAsyncComponent(() => import('./jeecg/components/roleSelect/RoleSelectInput.vue')));
componentMap.set('JInputSelect', createAsyncComponent(() => import('./jeecg/components/JInputSelect.vue')));
componentMap.set('JAuthRuleValue', createAsyncComponent(() => import('./jeecg/components/JAuthRuleValue.vue')));
componentMap.set('JMultiTextTagInput', createAsyncComponent(() => import('./jeecg/components/JMultiTextTagInput.vue')));
componentMap.set('JSelectDepartPost', createAsyncComponent(() => import('./jeecg/components/JSelectDepartPost.vue')));
componentMap.set('JSelectUserByDeptPost', createAsyncComponent(() => import('./jeecg/components/JSelectUserByDeptPost.vue')));


export function add(compName: ComponentType, component: Component) {
  componentMap.set(compName, component);
}

export function del(compName: ComponentType) {
  componentMap.delete(compName);
}

export { componentMap };
