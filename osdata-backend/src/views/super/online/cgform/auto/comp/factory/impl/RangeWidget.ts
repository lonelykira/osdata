import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * 日期、时间、数值-范围
 */
export default class RangeWidget extends IFormSchema {
  
  componentType: string;
  datetime: boolean;
  format: string;

  constructor(key, data) {
    super(key, data);
    let view = data.view;
    this.format = data.format;
    this.datetime = false;
    if('rangeNumber'===view){
      this.componentType = 'JRangeNumber'
    }else if('rangeTime'===view){
      this.componentType = 'RangeTime'
    }else{
      this.componentType = 'RangeDate'
      if(data.datetime===true){
        this.datetime = true;
      }
    }
  }

  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: this.componentType,
      componentProps: {
        datetime: this.datetime,
        format: this.format,
        getPopupContainer: (_node) => {
          return this.getModalAsContainer();
        },
      },
    });
  }
}
