import InputWidget from './impl/InputWidget';
import { FormSchema } from '/@/components/Form';
import DateWidget from './impl/DateWidget';
import SelectWidget from './impl/SelectWidget';
import PasswordWidget from './impl/PasswordWidget';
import FileWidget from './impl/FileWidget';
import ImageWidget from './impl/ImageWidget';
import TextAreaWidget from './impl/TextAreaWidget';
import SelectMultiWidget from './impl/SelectMultiWidget';
import SelectSearchWidget from './impl/SelectSearchWidget';
import PopupWidget from './impl/PopupWidget';
// update-begin--author:liaozhiyang---date:20240130---for：【QQYUN-7961】popupDict字典
import PopupDictWidget from './impl/PopupDictWidget';
// update-end--author:liaozhiyang---date:20240130---for：【QQYUN-7961】popupDict字典
import TreeCategoryWidget from './impl/TreeCategoryWidget';
import SelectDepartWidget from './impl/SelectDepartWidget';
import SelectUserWidget from './impl/SelectUserWidget';
import EditorWidget from './impl/EditorWidget';
import MarkdownWidget from './impl/MarkdownWidget';
import PcaWidget from './impl/PcaWidget';
import AreaLinkage from './impl/AreaLinkage';
import TreeSelectWidget from './impl/TreeSelectWidget';
import RadioWidget from './impl/RadioWidget';
import CheckboxWidget from './impl/CheckboxWidget';
import SwitchWidget from './impl/SwitchWidget';
import TimeWidget from './impl/TimeWidget';
import LinkDownWidget from './impl/LinkDownWidget';
import SlotWidget from './impl/SlotWidget';
import NumberWidget from './impl/NumberWidget';
import LinkTableWidget from './impl/LinkTableWidget'
import LinkTableFieldWidget from './impl/LinkTableFieldWidget'
import LinkTableForQueryWidget from './impl/LinkTableForQueryWidget'
import CascaderPcaForQueryWidget from './impl/CascaderPcaForQueryWidget'
import SelectUser2Widget from './impl/SelectUser2Widget'
import RangeWidget from "./impl/RangeWidget";
import CategoryLinkageForQueryWidget from './impl/CategoryLinkageForQueryWidget';

export default class FormSchemaFactory {
  static createFormSchema(key, data, queryItem) {
    let view = data.view;
    switch (view) {
      case 'password':
        //2.密码输入框
        return new PasswordWidget(key, data);
      case 'list':
        //3.下拉框
        return new SelectWidget(key, data);
      case 'radio':
        // 4. 单选
        return new RadioWidget(key, data);
      case 'checkbox':
        // 5.多选
        return new CheckboxWidget(key, data);
      case 'date':
      case 'datetime':
        // 6.日期
        // 7.日期时间
        return new DateWidget(key, data, queryItem);
      case 'time':
        // 8 时间
        return new TimeWidget(key, data);
      case 'file':
        // 9.文件
        return new FileWidget(key, data);
      case 'image':
        // 10.图片
        return new ImageWidget(key, data);
      case 'textarea':
        // 11.多行文本
        return new TextAreaWidget(key, data);
      case 'list_multi':
        // 12.下拉多选框
        return new SelectMultiWidget(key, data);
      case 'sel_search':
        // 13.下拉搜索框
        return new SelectSearchWidget(key, data);
      case 'popup':
        // 14. popup
        return new PopupWidget(key, data);
      case 'cat_tree':
        // 15.分类字典树
        return new TreeCategoryWidget(key, data);
      case 'sel_depart':
        // 16.部门选择
        return new SelectDepartWidget(key, data);
      case 'sel_user':
        // 17.用户选择
        return new SelectUserWidget(key, data);
      case 'umeditor':
        // 18.富文本
        return new EditorWidget(key, data);
      case 'markdown':
        // 19.MarkDown
        return new MarkdownWidget(key, data);
      case 'pca':
        // 20.省市区
        // update-begin--author:liaozhiyang---date:20240607---for：【TV360X-501】省市区换新组件
        //  return new PcaWidget(key, data);
        return new AreaLinkage(key, data);
      // update-end--author:liaozhiyang---date:20240607---for：【TV360X-501】省市区换新组件
      case 'link_down':
        // 21.联动组件
        return new LinkDownWidget(key, data);
      case 'sel_tree':
        // 22.自定义树控件
        return new TreeSelectWidget(key, data);
      case 'switch':
        // 23.开关组件
        return new SwitchWidget(key, data);
      case 'link_table':
        // 24.关联记录
        return new LinkTableWidget(key, data);
      case 'link_table_field':
        // 25.他表字段
        return new LinkTableFieldWidget(key, data);
      // update-begin--author:liaozhiyang---date:20240130---for：【QQYUN-7961】popupDict字典
      case 'popup_dict':
        // 14. popup字典
        return new PopupDictWidget(key, data);
      // update-end--author:liaozhiyang---date:20240130---for：【QQYUN-7961】popupDict字典
      case 'slot':
        // slot
        return new SlotWidget(key, data);
      case 'LinkTableForQuery':
        return new LinkTableForQueryWidget(key, data);
      case 'CascaderPcaForQuery':
        return new CascaderPcaForQueryWidget(key, data, queryItem);
      // update-begin--author:wangshuai---date:20260819---for：【LHZP-1983】【表单设计器】支持联动组件
      case 'JCategoryLinkage':
        return new CategoryLinkageForQueryWidget(key, data);
      // update-end--author:wangshuai---date:20260819---for：【LHZP-1983】【表单设计器】支持联动组件
      case 'select_user2':
        return new SelectUser2Widget(key, data);
      case 'rangeDate':
      case 'rangeTime':
      case 'rangeNumber':
        return new RangeWidget(key, data);
      case 'hidden':
        // 隐藏的控件  如分类树的文本
        return new InputWidget(key, data).isHidden();
      default:
        if (data.type == 'number') {
          return new NumberWidget(key, data);
        } else {
          //1.普通输入框
          return new InputWidget(key, data);
        }
    }
  }

  static createSlotFormSchema(key, data) {
    let slotFs = new SlotWidget(key, data);
    let view = data.view;
    if ('date' == view) {
      slotFs.groupDate();
    } else if ('datetime' == view) {
      slotFs.groupDatetime();
    } else if ('time' == view) {
      // update-begin--author:liaozhiyang---date:20240517---for：【QQYUN-9348】增加online查询区域时间范围查询功能
      slotFs.groupTime();
      // update-end--author:liaozhiyang---date:20240517---for：【QQYUN-9348】增加online查询区域时间范围查询功能
    } else {
      let type = data.type;
      if (type == 'number' || type == 'integer') {
        slotFs.groupNumber();
      }
    }
    return slotFs;
  }

  /**
   * 表单ID 默认是隐藏的
   */
  static createIdField(): FormSchema {
    return {
      label: '',
      field: 'id',
      component: 'Input',
      show: false,
    };
  }
}
