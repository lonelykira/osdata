import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * 输入框- 密码
 */
export default class PasswordWidget extends IFormSchema {
  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: 'InputPassword',
    });
  }
}
