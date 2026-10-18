import {unref} from 'vue'
import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * popup
 */
export default class PopupWidget extends IFormSchema {
  code: string;
  multi: boolean;
  fieldConfig: any[];

  constructor(key, data) {
    super(key, data);
    this.code = data['code'];
    this.multi = data['popupMulti'];
    this.fieldConfig = this.getFieldConfig(data);
  }

  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      component: 'JPopup',
      componentProps,
    });
  }

  getComponentProps() {
    let props = {
      code: this.code,
      multi: this.multi,
      fieldConfig: this.fieldConfig,
    };
    if (this.formRef) {
      props['formElRef'] = this.formRef;
    } else {
      props['setFieldsValue'] = this.setFieldsValue;
    }
    // 解决表单设计器高级查询 popup组件弹窗导致高级查询pop关闭
    if(this.inPopover === true){
      props['getContainer'] = ()=>{
        return this.getModalAsContainer();
      }
    }

    // 获取表单数据
    props['getFormValues'] = () => unref(this.formRef).getFieldsValue();

    return props;
  }

  getFieldConfig(data) {
    let { destFields, orgFields, dictText } = data;
    if (!destFields || destFields.length == 0) {
      return [];
    }
    let arr1 = destFields.split(',');
    let arr2 = orgFields.split(',');
    let arr3 = dictText ? dictText.split(',') : null;
    let config: any[] = [];
    const pre = this.pre;
    for (let i = 0; i < arr1.length; i++) {
      config.push({
        target: pre + arr1[i],
        source: arr2[i],
        label: arr3 ? arr3[i] : void 0,
      });
    }
    return config;
  }
}
