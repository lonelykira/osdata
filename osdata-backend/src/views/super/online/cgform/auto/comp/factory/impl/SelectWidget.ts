import { FormSchema } from '/@/components/Form';
import { h } from 'vue';
import IFormSchema from '../IFormSchema';

/**
 * 下拉框
 * //待处理： 表字典取数据可以考虑传参前端再请求
 */
export default class SelectWidget extends IFormSchema {
  schema: Recordable;
  /*title-value*/
  options: any[];
  dictTable: string;
  dictText: string;
  dictCode: string;
  multi: boolean;

  constructor(key, data) {
    super(key, data);
    this.schema = data;
    // 静态数据选项（enum）转换为 JSelectSingle 所需格式
    this.options = data['enum'] ? this.getOptions(data['enum'], '') : [];
    this.dictTable = data['dictTable'];
    this.dictText = data['dictText'];
    this.dictCode = data['dictCode'];
    this.multi = data['multi'] || false;
  }

  getItem(): FormSchema {
    let item = super.getItem();
    let component = this.getFormComponent()
    let componentProps = this.getComponentProps()
    return Object.assign({}, item, {
      component,
      componentProps,
      renderComponentContent: this.getSlots(componentProps),
    });
  }

  getFormComponent(){
    // update-begin--author:liaozhiyang---date:20260204---for:【issues/9307】online下拉加载表字典需滚动加载
    // if(this.options.length>0){
    //   return  'Select'
    // }else{
    //   return 'JDictSelectTag'
    // }
    return 'JSelectSingle'
    // update-end--author:liaozhiyang---date:20260204---for:【issues/9307】online下拉加载表字典需滚动加载
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

  getComponentProps() {
    let mode = this.multi===true?'multiple':'combobox'
    let props: any = {
      allowClear: true,
      mode,
      style: {
        width: '100%',
      },
      getPopupContainer: (_node) => {
        return this.getModalAsContainer();
      },
       // update-begin--author:liaozhiyang---date:20260203---for:【issues/9307】online下拉加载表字典需滚动加载
      // 下拉框展开/关闭的回调
      // onDropdownVisibleChange: (visible: boolean)=> {
      //   if (visible && typeof this.schema.updateOptions === 'function') {
      //     this.schema.updateOptions()
      //   }
      // },
      // update-end--author:liaozhiyang---date:20260203---for:【issues/9307】online下拉加载表字典需滚动加载
    }
    // 是否启用远程搜索+滚动加载（查询工作表模式）
    const loadMore = this.schema['loadMore'] || this._data['loadMore'];
    if (typeof loadMore === 'function') {
      props['remoteLoad'] = true;
      props['onLoad'] = (params: { keyword: string; pageNo: number; pageSize: number }) => {
        return loadMore(params.keyword, params.pageNo);
      };
      // 透传真实每页条数，否则 JSelectSingle 按默认值 10 判断末页，会多发一次空页请求
      const pageSize = this.schema['pageSize'] || this._data['pageSize'];
      if (pageSize) {
        props['pageSize'] = pageSize;
      }
      delete props.options; // remoteLoad 模式下不用静态 options
    } else {
      // 普通搜索模式
      const filterable = this.schema['filterable'] || this._data['filterable'];
      if (filterable) {
        props['showSearch'] = true;
      }
    }
    // update-begin--author:liaozhiyang---date:20260203---for:【issues/9307】online下拉加载表字典需滚动加载
    if (!this.dictTable) {
      props['dictCode'] = this.dictCode;
      // update-begin--author:liaozhiyang---date:20230110---for：【QQYUN-7799】字典组件（原生组件除外）加上颜色配置
      props['useDicColor'] = true;
      // update-end--author:liaozhiyang---date:20230110---for：【QQYUN-7799】字典组件（原生组件除外）加上颜色配置
      // 静态数据（无 dictCode）时，将 enum 选项直接传给组件
      if (!this.dictCode && !loadMore && this.options.length > 0) {
        props['options'] = this.options;
      }
    } else {
      props['dictCode'] = this.genDictTableCode(this.dictTable, this.dictText, this.dictCode);
      props['scrollLoad'] = true;
      delete props.onDropdownVisibleChange;
    }
    // update-end--author:liaozhiyang---date:20260203---for:【issues/9307】online下拉加载表字典需滚动加载
    return props
  }

  getSlots(componentProps: Recordable) {
    const {useDicColor} = componentProps;
    return function () {
      return {
        option(option: Recordable) {
          const style: Recordable = {};
          if (useDicColor && option.color) {
            style.color = '#fff';
            style.height = '20px';
            style.lineHeight = '20px';
            style.padding = '0 6px';
            style.fontSize = '12px';
            style.borderRadius = '8px';
            style.backgroundColor = option.color;
            style.display = 'inline-block';
          }
          return h('span', {
            style,
          }, option.text || option.label);
        },
      };
    }
  }

  getOptions(array, type) {
    if (!array || array.length == 0) {
      return [];
    }
    let isNum = 'number' == type;
    let arr: any[] = [];
    for (let item of array) {
      // update-begin--author:liaozhiyang---date:20240517---for：【QQYUN-9359】加强判断，防止数据有null报错
      if (item == null) break;
      // update-end--author:liaozhiyang---date:20240517---for：【QQYUN-9359】加强判断，防止数据有null报错
      let value = item.value;
      if(isNum){
        value = parseInt(value)
      }
      arr.push({
        ...item,
        value,
        label: item.title,
      });
    }
    return arr;
  }
}
