import IFormSchema from '../IFormSchema';
import { FormSchema } from '/@/components/Form';

/**
 * 部门选择
 */
export default class SelectDepartWidget extends IFormSchema {
  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      component: 'JSelectDept',
      componentProps,
    });
  }

  getComponentProps() {
    let extend = this.getExtendData();
    let props = {
      // update-begin--author:liaozhiyang---date:20260414---for:【QQYUN-9801】修复online点击展开全部，树节点没全部展开
      sync: false,
      // update-end--author:liaozhiyang---date:20260414---for:【QQYUN-9801】修复online点击展开全部，树节点没全部展开
      checkStrictly: true,
      showButton: false,
    };
    if (extend.text) {
      props['labelKey'] = extend.text;
    }
    if (extend.store) {
      props['rowKey'] = extend.store;
    }
    if (extend.multiSelect === false) {
      props['multiple'] = false;
    }
    
    if (extend.multiSelect === true) {
      props['multiple'] = true;
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
