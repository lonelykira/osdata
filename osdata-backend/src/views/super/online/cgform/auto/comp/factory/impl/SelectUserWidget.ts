import IFormSchema from '../IFormSchema';
import { FormSchema } from '/@/components/Form';

/**
 * 用户选择
 */
export default class SelectUserWidget extends IFormSchema {

  showButton: boolean;

  constructor(key, data) {
    super(key, data);
    this.showButton = data.showButton === false ? false : true;
  }
  
  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      component: 'JSelectUser',
      componentProps,
    });
  }

  getComponentProps() {
    let extend = this.getExtendData();
    let props = {
      showSelected: false,
      allowClear: true,
      isRadioSelection: false,
      showButton: this.showButton
    };
    if (extend.text) {
      props['labelKey'] = extend.text;
    }
    if (extend.store) {
      props['rowKey'] = extend.store;
    }
    if (extend.multiSelect === false) {
      //props['multiple'] = false
      props['isRadioSelection'] = true;
    }
    props['maxTagCount'] = 3;

    // 解决表单设计器高级查询 popup组件弹窗导致高级查询pop关闭
    if(this.inPopover === true){
      props['getContainer'] = ()=>{
        return this.getModalAsContainer();
      }
    }

    return props;
  }
}
