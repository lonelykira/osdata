import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * radio
 * 没有现成的 只能借用JDictSelectTag
 */
export default class RadioWidget extends IFormSchema {
  dictTable: string;
  dictText: string;
  dictCode: string;

  constructor(key, data) {
    super(key, data);
    // 可以从这个里面取 但是换成临时加载的
    //this.options = this.getOptions(data['enum'])
    this.dictTable = data['dictTable'];
    this.dictText = data['dictText'];
    this.dictCode = data['dictCode'];
  }

  setFormRef(ref) {
    super.setFormRef(ref);
    this.handleDictTableParams();
  }

  updateDictTable(dictTable: string) {
    this.formRef.value.updateSchema(({
      field: this.field,
      componentProps: {
        dictCode: this.genDictTableCode(dictTable, this.dictText, this.dictCode),
      }
    }))
  }

  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      component: 'JDictSelectTag',
      componentProps,
    });
  }

  getComponentProps() {
    if (!this.dictTable && !this.dictCode) {
      // 字典表 和 字典 都没填数据
      return {};
    } else {
      if (!this.dictTable) {
        return {
          // update-begin--author:liaozhiyang---date:20230110---for：【QQYUN-7799】字典组件（原生组件除外）加上颜色配置
          useDicColor: true,
          // update-end--author:liaozhiyang---date:20230110---for：【QQYUN-7799】字典组件（原生组件除外）加上颜色配置
          dictCode: this.dictCode,
          type: 'radio',
        };
      } else {
        return {
          dictCode: this.genDictTableCode(this.dictTable, this.dictText, this.dictCode),
          type: 'radio',
        };
      }
    }
  }
}
