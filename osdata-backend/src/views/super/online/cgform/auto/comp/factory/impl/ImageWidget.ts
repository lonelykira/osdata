import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';
import { UploadTypeEnum } from '/@/components/Form/src/jeecg/components/JUpload';

/**
 * 图片
 */
export default class ImageWidget extends IFormSchema {
  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      component: 'JUpload',
      componentProps,
    });
  }

  getComponentProps() {
    let props = {
      fileType: UploadTypeEnum.image,
    };
    let json = this.getExtendData();
    if (json && json.uploadnum) {
      props['maxCount'] = Number(json.uploadnum);
    }
    return props;
  }
}
