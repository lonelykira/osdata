import IFormSchema from '../IFormSchema';
import { FormSchema } from '/@/components/Form';

/**
 * 用户选择
 */
export default class SelectUser2Widget extends IFormSchema {

  multi: boolean;
  store: string;
  query: boolean;

  constructor(key, data) {
    super(key, data);
    this.multi = data.multi === true ? true : false;
    this.store = data.store||'';
    // 是否是查询条件，查询条件显示为输入框样式
    this.query = data.query||false;
  }
  
  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      component: 'UserSelect',
      componentProps
    });
  }

  getComponentProps() {
    let props = {
      multi: this.multi,
      store: this.store,
      query: this.query,
    }
    // 解决表单设计器高级查询 popup组件弹窗导致高级查询pop关闭
    if(this.inPopover === true){
      props['getContainer'] = ()=>{
        return this.getModalAsContainer();
      }
    }
    return props;
  }
}
