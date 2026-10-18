import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * 下拉联动- 原理是：
 * 使用JDictSelectTag组件(2022-03-09测试可行版 后续如有改动请注意)
 * 监听表单的change事件，清空下级表单值，并改变props
 * 问题在于1.没有code的时候 不需要设置选项
 * 优势在于： 可以不考虑组件位置（但是需要改后台接口）
 */
export default class LinkDownWidget extends IFormSchema {
  /*title-value*/
  options: any[];
  next: string;
  type: string;
  table: string;
  txt: string;
  store: string;
  pidField: string;
  idField: string;
  origin: boolean;
  condition: string;

  constructor(key, data) {
    super(key, data);
    const { dictTable, dictText, dictCode, pidField, idField, origin, condition } = data;
    this.table = dictTable;
    this.txt = dictText;
    this.store = dictCode;
    this.idField = idField;
    this.pidField = pidField;
    this.origin = origin;
    this.condition = condition;
    // 都是空数组
    this.options = [];
    this.next = data.next || '';
    this.type = data.type;
  }

  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      component: 'OnlineSelectCascade',
      componentProps,
    });
  }

  getComponentProps() {
    let baseProp = {
      table: this.table,
      txt: this.txt,
      store: this.store,
      pidField: this.pidField,
      idField: this.idField,
      origin: this.origin,
      pidValue: '-1',
      style: {
        width: '100%',
      },
      onChange: (value) => {
        console.log('级联组件-onChange', value);
        this.valueChange(value);
      },
      onNext: (pidValue) => {
        console.log('级联组件-onNext', pidValue);
        this.nextOptionsChange(pidValue);
      },
    };
    if (this._data.origin === true) {
      baseProp['condition'] = this.condition;
    }
    return baseProp;
  }

  async nextOptionsChange(pidValue) {
    if (!this.formRef) {
      console.error('表单引用找不到');
      return;
    }
    if (!this.next) {
      return;
    }
    let ref = this.formRef.value;
    await ref.updateSchema({
      field: this.next,
      componentProps: {
        pidValue,
      },
    });
  }

  async valueChange(value) {
    if (!this.formRef) {
      console.error('表单引用找不到');
      return;
    }
    // update-begin--author:liaozhiyang---date:20240717---for：【TV360X-1856】联动组件最后一个js增强onchang方法不生效
    let ref = this.formRef.value;
    // 触发form层级的change事件
    ref.$formValueChange(this.field, value);
    if (this.next) {
      // 重置value
      await ref.setFieldsValue({ [this.next]: '' });
    }
    // update-end--author:liaozhiyang---date:20240717---for：【TV360X-1856】联动组件最后一个js增强onchang方法不生效
  }
}
