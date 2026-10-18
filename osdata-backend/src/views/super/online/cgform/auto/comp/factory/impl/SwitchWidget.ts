import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';
import { isArray, isObject } from '/@/utils/is';

/**
 * 开关
 */
export default class SwitchWidget extends IFormSchema {
  constructor(key, data) {
    super(key, data);
    // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-54】开关只读未生效
    // this.hasChange = false;
    // update-end--author:liaozhiyang---date:20240517---for：【TV360X-54】开关只读未生效
  }
  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      component: 'JSwitch',
      componentProps,
    });
  }

  getComponentProps() {
    let { fieldExtendJson } = this._data;
    let options = ['Y', 'N'];
    if (fieldExtendJson) {
      if (typeof fieldExtendJson == 'string') {
        // update-begin--author:liaozhiyang---date:20240522---for：【TV360X-25】扩展参数配置中增加开关是否选项配置
        const json = JSON.parse(fieldExtendJson);
        if (isArray(json) && json.length == 2) {
          options = json;
        } else if (isObject(json) && isArray(json.switchOptions)) {
          options = json.switchOptions;
        }
        // update-end--author:liaozhiyang---date:20240522---for：【TV360X-25】扩展参数配置中增加开关是否选项配置
      }
    }
    return {
      options,
    };
  }
}
