import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

enum DateFormat {
  datetime = 'YYYY-MM-DD HH:mm:ss',
  date = 'YYYY-MM-DD',
}

/**
 * 日期、时间
 */
export default class DateWidget extends IFormSchema {
  format: string;
  showTime: boolean;
  picker: string | undefined;

  allowSelectRange: boolean;

  constructor(key, data, queryItem) {
    super(key, data);
    this.format = DateFormat[data.view];
    this.showTime = data.view == 'date' ? false : true;
    // update-begin--author:liaozhiyang---date:20240430---for：【issues/6094】online 日期(年月日)控件增加年、年月，年周，年季度等格式
    let fieldExtendJson = data.fieldExtendJson;
    if (data.view == 'date' && fieldExtendJson) {
      fieldExtendJson = JSON.parse(fieldExtendJson);
      if (fieldExtendJson.picker && fieldExtendJson.picker != 'default') {
        this.picker = fieldExtendJson.picker;
      } else {
        this.picker = undefined;
      }
    }
    // update-end--author:liaozhiyang---date:20240430---for：【issues/6094】online 日期(年月日)控件增加年、年月，年周，年季度等格式
    // 只有等于和不等于才能选择预设范围（今天、昨天、本周等）
    this.allowSelectRange = ['eq', 'ne'].includes(queryItem?.rule)
  }

  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: 'DatePickerInFilter',
      componentProps: {
        placeholder: `请选择${this.label}`,
        showTime: this.showTime,
        valueFormat: this.format,
        allowSelectRange: this.allowSelectRange,
        // update-begin--author:liaozhiyang---date:20240430---for：【issues/6094】online 日期(年月日)控件增加年、年月，年周，年季度等格式
        picker: this.picker,
        // update-end--author:liaozhiyang---date:20240430---for：【issues/6094】online 日期(年月日)控件增加年、年月，年周，年季度等格式
        style: {
          width: '100%',
        },
        getPopupContainer: (_node) => {
          return this.getModalAsContainer();
        },
      },
    });
  }
}
