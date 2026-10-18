import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * 表单设计器-省市区查询
 */
export default class CascaderPcaForQueryWidget extends IFormSchema {

  schema: Recordable;
  // 省市县联动级别
  areaLevel: number;
  // 是否允许更改级别
  allowChangeLevel: boolean;

  constructor(key: string, data: Recordable, queryItem: Recordable) {
    super(key, data);
    this.schema = data
    this.areaLevel = data['areaLevel'] ?? 3;
    // 只有等于和不等于才能更改级别
    this.allowChangeLevel = ['eq', 'ne'].includes(queryItem?.rule)
  }

  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: 'CascaderPcaInFilter',
      componentProps:{
        areaLevel: this.areaLevel,
        allowChangeLevel: this.allowChangeLevel,
        placeholder: '请选择…',
        style: {
          width: '100%',
        }
      }
    });
  }

}
