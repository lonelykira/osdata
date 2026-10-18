import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * 输入框-textarea
 */
export default class TextAreaWidget extends IFormSchema {
  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: 'InputTextArea',
      componentProps:{
        autoSize : {
          minRows: 4, maxRows: 10
        }
      }
    });
  }
}
