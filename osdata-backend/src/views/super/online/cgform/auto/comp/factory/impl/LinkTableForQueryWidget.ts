import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * 表单设计器-关联记录查询 使用下拉搜索
 */
export default class LinkTableForQueryWidget extends IFormSchema {

  code: string;
  titleField: string;
  multi: boolean;
  
  constructor(key, data) {
    super(key, data);
    this.code = data['code'];
    this.titleField = data['titleField'];
    this.multi = data['multi']||false;
  }

  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: 'LinkTableForQuery',
      componentProps:{
        code: this.code,
        multi: this.multi,
        field: this.titleField,
        style: {
          width: '100%',
        }
      }
    });
  }

}
