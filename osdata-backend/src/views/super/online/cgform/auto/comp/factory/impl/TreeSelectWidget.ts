import IFormSchema from '../IFormSchema';
import { FormSchema } from '/@/components/Form';

/**
 * 自定义树
 */
export default class TreeSelectWidget extends IFormSchema {
  /*表名、显示字段、存储字段*/
  dict: string;
  /*父级ID的字段名*/
  pidField: string;
  /*父级ID的字段值*/
  pidValue: string;
  /*是否有子节点*/
  hasChildField: string;
  /*是否转换（取反）是否有子节点的值*/
  converIsLeafVal: number;

  constructor(key, data) {
    super(key, data);
    this.dict = data['dict'];
    this.pidField = data['pidField'];
    this.pidValue = data['pidValue'];
    // update-begin--author:liaozhiyang---date:20240509---for：【issues/6197】解决自定义树组件是否含有子节点功能不生效
    this.hasChildField = data['hasChildField'];
    // update-end--author:liaozhiyang---date:20240509---for：【issues/6197】解决自定义树组件是否含有子节点功能不生效
    this.converIsLeafVal = data['converIsLeafVal'];
  }

  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: 'JTreeSelect',
      componentProps: {
        dict: this.dict,
        pidField: this.pidField,
        pidValue: this.pidValue,
        // update-begin--author:liaozhiyang---date:20240509---for：【issues/6197】解决自定义树组件是否含有子节点功能不生效
        hasChildField: this.hasChildField,
        // update-end--author:liaozhiyang---date:20240509---for：【issues/6197】解决自定义树组件是否含有子节点功能不生效
        converIsLeafVal: this.converIsLeafVal,
      },
    });
  }
}
