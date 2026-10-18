import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * 下拉多选框
 */
export default class SelectMultiWidget extends IFormSchema {
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
        dictCode: this.genDictTableCode(dictTable, this.dictText, this.dictCode)
      }
    }))
  }

  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      component: 'JSelectMultiple',
      componentProps: componentProps,
    });
  }

  getComponentProps() {
    if (!this.dictTable && !this.dictCode) {
      // 字典表 和 字典 都没填数据
      return {};
    } else {
      let props = {};
      if (!this.dictTable) {
        props['dictCode'] = this.dictCode;
        // update-begin--author:liaozhiyang---date:20230110---for：【QQYUN-7799】字典组件（原生组件除外）加上颜色配置
        props['useDicColor'] = true;
        // update-end--author:liaozhiyang---date:20230110---for：【QQYUN-7799】字典组件（原生组件除外）加上颜色配置
      } else {
        props['dictCode'] = this.genDictTableCode(this.dictTable, this.dictText, this.dictCode);
        // update-begin--author:liaozhiyang---date:20260204---for:【issues/9307】online下拉加载表字典需滚动加载
        // 默认滚动加载字典表数据
        props['scrollLoad'] = true;
        // update-end--author:liaozhiyang---date:20260204---for:【issues/9307】online下拉加载表字典需滚动加载
      }
      props['triggerChange'] = true;
      props['popContainer'] = this.getPopContainer();
      return props;
    }
  }
}
