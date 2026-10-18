import {unref} from 'vue'
import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * popupDict
 */
export default class PopupDictWidget extends IFormSchema {
  dictCode: string;
  multi: boolean;
  constructor(key, data) {
    super(key, data);
    this.dictCode = `${data['code']},${data['destFields']},${data['orgFields']}`;
    this.multi = data['popupMulti'];
  }

  getItem(): FormSchema {
    const item = super.getItem();
    const componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      component: 'JPopupDict',
      componentProps,
    });
  }

  getComponentProps() {
    const props = {
      dictCode: this.dictCode,
      multi: this.multi,
    };
    // 解决表单设计器高级查询 popup组件弹窗导致高级查询pop关闭
    if (this.inPopover) {
      props['getContainer'] = () => {
        return this.getModalAsContainer();
      };
    }

    // 获取表单数据
    props['getFormValues'] = () => unref(this.formRef).getFieldsValue();

    return props;
  }
}
