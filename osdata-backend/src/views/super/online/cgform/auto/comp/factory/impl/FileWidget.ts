import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * 文件
 */
export default class FileWidget extends IFormSchema {
  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      component: 'JUpload',
      componentProps,
    });
  }

  getComponentProps() {
    let json = this.getExtendData();
    if (json && json.uploadnum) {
      return {
        maxCount: Number(json.uploadnum),
      };
    }
    return {};
  }
}
