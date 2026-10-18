import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * 他表字段
 */
export default class LinkTableFieldWidget extends IFormSchema {

  dictTable: string;
  dictText: string;

  constructor(key, data) {
    super(key, data);
    this.dictTable = data['dictTable'];
    this.dictText = data['dictText'];
  }
  
  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      componentProps: {
        readOnly: true,
        allowClear: false,
        disabled: true,
        style:{
          background: 'none',
          color:'rgba(0, 0, 0, 0.85)',
          border:'none'
        }
      }
    });
    return item;
  }

  /**
   * 获取他表字段的关联信息
   */
  getLinkFieldInfo(){
    let arr = [this.dictTable, `${this.field},${this.dictText}`];
    return arr;
  }
}
