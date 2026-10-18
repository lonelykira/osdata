import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * 关联记录
 */
export default class LinkTableWidget extends IFormSchema {
  dictTable: string;
  dictText: string;
  dictCode: string;
  view: string;
  componentString: string;
  linkFields: Array<string>;
  
  constructor(key, data) {
    super(key, data);
    this.dictTable = data.dictTable;
    this.dictText = data.dictText;
    this.dictCode = data.dictCode;
    this.view = data.view;
    this.componentString = ''
    this.linkFields = []
  }

  getItem(): FormSchema {
    let item = super.getItem();
    const componentProps = this.getComponentProps()
    return Object.assign({}, item, {
      component: this.componentString,
      componentProps: componentProps
    });
  }

  getComponentProps() {
    let props = {
      textField: this.dictText,
      tableName: this.dictTable,
      valueField: this.dictCode,
    };
    let extend = this.getExtendData();
    // 是否多选
    if (extend.multiSelect) {
      props['multi'] = true;
    }else{
      props['multi'] = false;
    }
    //封面图
    if (extend.imageField) {
      props['imageField'] = extend.imageField;
    }else{
      props['imageField'] = ''
    }
    //显示类型
    if (extend.showType=='select') {
      this.componentString = 'LinkTableSelect'
      let popContainer = this.getPopContainer();
      props['popContainer'] = popContainer
      // update-begin--author:liaozhiyang---date:20260807---for:【LHZP-604】关联记录下拉方式挂载在body上避免空间不足
      props['getPopupContainer'] = () => document.body;
      // update-end--author:liaozhiyang---date:20260807---for:【LHZP-604】关联记录下拉方式挂载在body上避免空间不足
    }else{
      this.componentString = 'LinkTableCard'
    }
    if(this.linkFields.length>0){
      props['linkFields'] = this.linkFields;
    }
    return props;
  }

  // 他表字段用于翻译
  setOtherInfo(arr){
    // ["表单字段,表字典字段","表单字段,表字典字段"]
      this.linkFields = arr;
  }
}
