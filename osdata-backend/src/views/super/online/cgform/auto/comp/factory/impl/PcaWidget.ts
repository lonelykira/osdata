import IFormSchema from '../IFormSchema';
import { FormSchema } from '/@/components/Form';

/**
 * 省市区
 */
export default class PcaWidget extends IFormSchema {
  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: 'JAreaSelect',
    });
  }
}
