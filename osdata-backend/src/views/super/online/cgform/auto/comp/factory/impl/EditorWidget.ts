import IFormSchema from '../IFormSchema';
import { FormSchema } from '/@/components/Form';

/**
 * 富文本
 */
export default class EditorWidget extends IFormSchema {
  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: 'JEditor',
      componentProps: {
        //update-begin-author:taoyan date:2022-6-1 for: VUEN-1159 第一次加载时，点击第一个输入框，光标会跑到富文本输入框
        options: {
          auto_focus: false,
        },
        //update-end-author:taoyan date:2022-6-1 for: VUEN-1159 第一次加载时，点击第一个输入框，光标会跑到富文本输入框
        // fileMax:1,
        // showImageUpload:false,
        // width:"966px",
        // height:"200px"
      },
    });
  }
}
