import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * slot
 */
export default class SlotWidget extends IFormSchema {
  slot: string;
  picker: string | undefined;
  precision: number | undefined;

  constructor(key, data) {
    super(key, data);
    this.slot = '';
    // update-begin--author:liaozhiyang---date:20240520---for：【TV360X-180】范围查询年，年月，周，季度
    let fieldExtendJson = data.fieldExtendJson;
    if (data.view == 'date' && fieldExtendJson) {
      fieldExtendJson = JSON.parse(fieldExtendJson);
      if (fieldExtendJson.picker && fieldExtendJson.picker != 'default') {
        this.picker = fieldExtendJson.picker;
      } else {
        this.picker = undefined;
      }
    }
    // update-end--author:liaozhiyang---date:20240520---for：【TV360X-180】范围查询年，年月，周，季度
    // update-begin--author:liaozhiyang---date:20240606---for：【TV360X-214】范围查询控件没有根据配置格式化
    this.precision = data.dbPointLength;
    // update-end--author:liaozhiyang---date:20240606---for：【TV360X-214】范围查询控件没有根据配置格式化
  }

  getItem(): FormSchema {
    let item = super.getItem();
    let slot = this.slot;
    const componentProps: any = {};
    this.picker && (componentProps.picker = this.picker);
    // update-begin--author:liaozhiyang---date:20240606---for：【TV360X-214】范围查询控件没有根据配置格式化
    this.precision && (componentProps.precision = this.precision);
    // update-end--author:liaozhiyang---date:20240606---for：【TV360X-214】范围查询控件没有根据配置格式化
    // update-begin--author:liaozhiyang---date:20240520---for：【TV360X-180】范围查询年，年月，周，季度
    return Object.assign({}, item, {
      slot,
      componentProps,
    });
    // update-end--author:liaozhiyang---date:20240520---for：【TV360X-180】范围查询年，年月，周，季度
  }

  groupDate() {
    this.slot = 'groupDate';
    return this;
  }

  groupDatetime() {
    this.slot = 'groupDatetime';
    return this;
  }

  groupTime() {
    // update-begin--author:liaozhiyang---date:20240517---for：【QQYUN-9348】增加online查询区域时间范围查询功能
    this.slot = 'groupTime';
    return this;
    // update-end--author:liaozhiyang---date:20240517---for：【QQYUN-9348】增加online查询区域时间范围查询功能
  }

  groupNumber() {
    this.slot = 'groupNumber';
    return this;
  }
}
