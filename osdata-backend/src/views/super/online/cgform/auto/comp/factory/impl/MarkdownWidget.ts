import IFormSchema from '../IFormSchema';
import { FormSchema } from '/@/components/Form';

/**
 * markdown
 */
export default class MarkdownWidget extends IFormSchema {
  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: 'JMarkdownEditor',
      componentProps: {
        // height: 300,
      },
    });
  }
}
