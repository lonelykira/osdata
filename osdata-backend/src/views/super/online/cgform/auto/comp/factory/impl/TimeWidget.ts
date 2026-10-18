import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * 日期、时间
 */
export default class TimeWidget extends IFormSchema {
  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: 'TimePicker',
      componentProps: {
        placeholder: `请选择${this.label}`,
        valueFormat: 'HH:mm:ss',
        getPopupContainer: (_node) => {
          return this.getModalAsContainer();
        },
        style: {
          width: '100%',
        },
      },
    });
  }
}
