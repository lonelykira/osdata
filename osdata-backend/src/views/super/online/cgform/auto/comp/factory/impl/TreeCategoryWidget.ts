import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * 分类字典
 */
export default class TreeCategoryWidget extends IFormSchema {
  pid: string;
  multi: boolean;
  textField: string;
  pcode: string;

  constructor(key, data) {
    super(key, data);
    this.multi = false;
    this.pid = data['pidValue'];
    this.pcode = data['pcode'];
    this.textField = data['textField'];
  }

  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      componentProps,
      component: 'JCategorySelect',
    });
  }

  /**
   * 1. 不带返回值的
   * 2. 带文本返回的
   */
  getComponentProps() {
    // VUEN-1049 分类字典保存后，列表不展示 单表 树表 --> 配错编码后，表单界面还显示分类字典选项，可直接不显示字典选项
    let param = {
      placeholder: '请选择' + this.label
    }
    if(this.pcode){
      param['pcode'] = this.pcode;
    }else{
      let pidValue = this.pid || 'EMPTY_PID';
      param['pid'] = pidValue;
    }
    if (!this.textField) {
      return {
        multiple: this.multi,
        ...param
      };
    } else {
      return {
        loadTriggleChange: true,
        multiple: this.multi,
        ...param,
        back: this.textField,
        onChange: (val, backVal) => {
          if (this.formRef) {
            this.formRef.value.setFieldsValue(backVal);
            this.formRef.value.$formValueChange(this.field, val)
          }
        },
      };
    }
  }

  getRelatedHideFields(): string[] {
    let arr: string[] = [];
    if (this.textField) {
      arr.push(this.textField);
    }
    return arr;
  }
}
