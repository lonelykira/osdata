import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * 输入框
 */
export default class InputWidget extends IFormSchema {
  getItem(): FormSchema {
    let item = super.getItem();
    if (this.hidden === true) {
      item['show'] = false;
    }
    return item;
  }
}
