import {computed, watch} from 'vue'
import { FormSchema, Rule } from '/@/components/Form';
import { FieldExtends, POP_CONTAINER } from '../../../types/onlineRender';
import { LABELLENGTH } from '../../../util/constant';
import {replaceUserInfoByExpression} from "@/utils/common/compUtils";
/**
 * 1.部门选择/用户选择 无：单选配置
 * 控件类
 */
export default abstract class IFormSchema {
  _data;
  field: string;
  label: string;
  labelLength: number;
  formRef: any;
  hidden: boolean;
  order: number;
  required: boolean;
  onlyValidator: any;
  hasChange: boolean;
  pre: string;
  setFieldsValue: any;
  schemaProp: any;
  searchForm: boolean;
  disabled: boolean;
  popContainer: string;
  inPopover: boolean;
  onlyInteger: boolean;

  constructor(key, data) {
    // 考虑不需要存data
    this._data = data;
    this.field = key;
    this.label = data.title;
    this.hidden = false;
    this.order = data.order || 999;
    this.required = false;
    this.onlyValidator = '';
    this.setFieldsValue = '';
    this.hasChange = true;
    if (key.indexOf('@') > 0) {
      this.pre = key.substring(0, key.indexOf('@') + 1);
    } else {
      this.pre = '';
    }
    this.schemaProp = {};
    this.searchForm = false;
    this.disabled = false;
    this.popContainer = '';
    this.handleWidgetAttr(data);
    this.inPopover = false;
    this.onlyInteger = false;
    this.labelLength = LABELLENGTH;
    this.initLabelLength();
  }

  /**
   * 获取最终的表单配置项，外面获取调用此方法
   */
  getFormItemSchema(): FormSchema {
    let schema = this.getItem();
    this.addDefaultChangeEvent(schema);
    return schema;
  }

  /**
   * 获取表单配置，子类重写此方法
   */
  getItem(): FormSchema {
    let fs: FormSchema = {
      field: this.field,
      label: this.label,
      labelLength: this.labelLength,
      component: 'Input',
      itemProps:{
        labelCol:{
          class: 'online-form-label'
        }
      }
    };
    let rules = this.getRule();
    if (rules.length > 0 && this.onlyValidator) {
      fs['rules'] = rules;
    }
    if (this.hidden === true) {
      fs['show'] = false;
    }
    return fs;
  }

  /**
   * 设置表单ref
   * popup、分类树需要关联设置其他表单值的时候用到
   * @param ref
   */
  setFormRef(ref) {
    this.formRef = ref;
  }

  /**
   * 设置表单元素隐藏
   */
  isHidden() {
    this.hidden = true;
    return this;
  }

  /**
   * 设置是否必填项
   * @param array
   */
  isRequired(array) {
    // 子表必填   TODO
    if (array && array.length > 0) {
      if (array.indexOf(this.field) >= 0) {
        this.required = true;
      }
    }
    return this;
  }

  /**
   * 初始化 label长度
   */
  initLabelLength(){
    let obj = this.getExtendData()
    if(obj && obj.labelLength){
      this.labelLength = obj.labelLength;
    }
  }

  /**
   * 获取扩展参数
   */
  getExtendData() {
    let extend: FieldExtends = {};
    let { fieldExtendJson } = this._data;
    if (fieldExtendJson) {
      if (typeof fieldExtendJson == 'string') {
        try {
          let json = JSON.parse(fieldExtendJson);
          extend = { ...json };
        } catch (e) {
          console.error(e);
        }
      }
    }
    return extend;
  }

  /***
   * 获取和此字段相关的其他字段 需要设置其为隐藏
   */
  getRelatedHideFields(): string[] {
    return [];
  }

  /**
   * placeholder
   */
  getPlaceholder(view) {
    let text = '请输入';
    // update-begin--author:liaozhiyang---date:20240521---for：【TV360X-218】针对组件分别提示对应的校验语
    if (
      [
        'list',
        'radio',
        'checkbox',
        'date',
        'datetime',
        'time',
        'list_multi',
        'sel_search',
        'popup',
        'cat_tree',
        'sel_depart',
        'sel_user',
        'pca',
        'link_down',
        'sel_tree',
        'switch',
        'link_table',
        'link_table_field',
        'popup_dict',
        'LinkTableForQuery',
        'CascaderPcaForQuery',
        'select_user2',
        'rangeDate',
        'rangeTime',
        'rangeNumber',
      ].includes(view)
    ) {
      text = '请选择';
    } else if (['file', 'image'].includes(view)) {
      text = '请上传';
    }
    // update-end--author:liaozhiyang---date:20240521---for：【TV360X-218】针对组件分别提示对应的校验语
    return text + this.label;
  }

  /**
   * 唯一校验
   */
  setOnlyValidateFun(validateFun) {
    if (validateFun) {
      this.onlyValidator = async (rule, value) => {
        let error = await validateFun(rule, value);
        if (!error) {
          return Promise.resolve();
        } else {
          return Promise.reject(error);
        }
      };
    }
  }

  /**
   * 获取校验规则
   */
  getRule(): any[] {
    let rules: Rule[] = [];
    const { view, errorInfo, pattern, type, fieldExtendJson } = this._data;
    if (this.required === true) {
      let msg = this.getPlaceholder(view);
      // update-begin--author:liaozhiyang---date:20240520---for：【TV360X-80】扩展参数配置中的校验提示不生效
      if (fieldExtendJson) {
        const json = JSON.parse(fieldExtendJson);
        if (json.validateError) {
          msg = json.validateError;
        }
      }
      // update-end--author:liaozhiyang---date:20240520---for：【TV360X-80】扩展参数配置中的校验提示不生效
      if (errorInfo) {
        msg = errorInfo;
      }
      if (view == 'sel_depart' || view == 'sel_user') {
        //如果是部门和用户组件 使用 required：true
        this.schemaProp['required'] = true;
        // update-begin--author:liaozhiyang---date:20240429---for：【QQYUN-9109】online使用部门和用户组件必填时label前面没有必填的*号
        rules.push({ required: true, message: msg });
        // update-end--author:liaozhiyang---date:20240429---for：【QQYUN-9109】online使用部门和用户组件必填时label前面没有必填的*号
      } else {
        rules.push({ required: true, message: msg });
      }
    }
    if ('sel_user' == view) {
      if (pattern === 'only' && this.onlyValidator) {
        rules.push({ validator: this.onlyValidator });
      }
    }
    if ('list' === view || 'radio' === view || 'markdown' === view || 'pca' === view || view.indexOf('sel') >= 0 || 'time' === view) {
      return rules;
    }
    if (view.indexOf('upload') >= 0 || view.indexOf('file') >= 0 || view.indexOf('image') >= 0) {
      return rules;
    }
    if (pattern) {
      if (pattern === 'only') {
        if (this.onlyValidator) {
          rules.push({ validator: this.onlyValidator });
        }
      } else if (pattern === 'z') {
        if (type == 'number' || type == 'integer') {
          this.onlyInteger = true;
          rules.push({ pattern: /^-?\d+$/, message: '请输入整数' });
        } else {
          rules.push({ pattern: /^-?\d+$/, message: '请输入整数' });
        }
      } else {
        let msg = errorInfo || '正则校验失败';
        let reg
        try {
          reg = new RegExp(pattern);
          if (!reg) {
            reg = pattern;
          }
        } catch {
          reg = pattern;
        }
        rules.push({ pattern: reg, message: msg });
      }
    }
    return rules;
  }

  /**
   * 添加默认的change事件
   * @param schema
   */
  addDefaultChangeEvent(schema) {
    if (this.hasChange) {
      if (!schema.componentProps) {
        schema.componentProps = {};
      }
      //update-begin-author:taoyan date:2022-5-24 for: VUEN-1095 只读未控制住
      if (this.disabled == true) {
        schema.componentProps.disabled = true;
      }
      //update-end-author:taoyan date:2022-5-24 for: VUEN-1095 只读未控制住
      if (!schema.componentProps.hasOwnProperty('onChange')) {
        schema.componentProps['onChange'] = (value, formData) => {
          if (value instanceof Event) {
            // 输入框 value是event对象
            value = (value.target as any).value;
          }
          // 部门组件抛出事件的value是数组
          if (value instanceof Array) {
            value = value.join(',');
          }
          // VUEN-1467【vue3 工作流】流程处理 一对多表单 子表tab切换后，关闭不了 导致整个浏览器无法操作 多操作几次，不一定每次必现---
          if(!this.formRef || !this.formRef.value || !this.formRef.value.$formValueChange){
            console.log('当前表单无法触发change事件,field：'+this.field)
          }else{
            this.formRef.value.$formValueChange(this.field, value, formData)
          }
        };
        // update-begin--author:liaozhiyang---date:20251011---for：【issues/8791】js增强popup弹框的onlChange()没生效
        if (schema.component === 'JPopup') {
          schema.componentProps['onPopUpChange'] = schema.componentProps['onChange']
        }
        // update-end--author:liaozhiyang---date:20251011---for：【issues/8791】js增强popup弹框的onlChange()没生效
      }
    }
    // 顺带处理其他的 schemaProp
    Object.keys(this.schemaProp).map((k) => {
      schema[k] = this.schemaProp[k];
    });
  }

  noChange() {
    this.hasChange = false;
  }

  updateField(field) {
    this.field = field;
  }

  /**
   * 高级查询 没有表单ref对象 手动设置setFieldValue方法用于 popup设置表单值
   */
  setFunctionForFieldValue(func) {
    if (func) {
      this.setFieldsValue = func;
    }
  }

  asSearchForm() {
    this.searchForm = true;
  }

  /**获取modal作为类下拉组件pop的父容器*/
  getModalAsContainer() {
    let ele = this.getPopContainer();
    // update-begin--author:liaozhiyang---date:20231205---for：【QQYUN-7150】online缓存路由打开多页导致下拉类型的组件打不开
    if (ele != 'body') {
      const elems = document.querySelectorAll(ele);
      if (elems && elems.length > 1) {
        const data: HTMLElement[] = [];
        elems.forEach((item: HTMLElement) => {
          if (!(item.offsetWidth == 0 && item.offsetHeight == 0)) {
            data.push(item);
          }
        });
        if (data.length === 1) {
          return data[0];
        }
      }
    }
    // update-end--author:liaozhiyang---date:20231205---for：【QQYUN-7150】online缓存路由打开多页导致下拉类型的组件打不开
    return document.querySelector(ele);
  }

  /**区分modal表单和查询表单*/
  getPopContainer() {
    if (this.searchForm === true) {
      return 'body';
    } else if(this.inPopover === true){
      return `.${this.popContainer}`;
    }else if(this.popContainer){
      return `.${this.popContainer} .ant-modal-content`
    }else {
      return POP_CONTAINER;
    }
  }

  handleWidgetAttr(data) {
    if (data.ui) {
      if (data.ui.widgetattrs) {
        if (data.ui.widgetattrs.disabled == true) {
          this.disabled = true;
        }
      }
    }
  }

  /**
   * 设置 popContainer
   */
  setCustomPopContainer(modalClass){
    this.popContainer = modalClass;
  }

  //update-begin-author:taoyan date:2022-8-5 for: 他表字段/关联记录用
  // 获取他表字段的 配置信息
  getLinkFieldInfo():any{
    return '';
  }

  // 1.将他表字段的配置信息设置到关联记录字段上
  setOtherInfo(_arg){
  }
  //update-end-author:taoyan date:2022-8-5 for: 他表字段/关联记录用
  
  // 表单设计器高级查询用
  isInPopover(){
    this.inPopover = true;
  }

  handleDictTableParams() {
    if (!this.formRef.value) {
      return
    }
    const dictTable = this._data.dictTable as string
    if (!dictTable) {
      return
    }
    const matches = dictTable.match(/\${([^}]+)}/g)
    if (!matches || matches.length == 0) {
      return
    }
    // 去除 ${}
    const keys = matches.map((item: string) => item.replace('${', '').replace('}', ''))
    const values = computed(() => {
      const formModel = this.formRef.value.formModel
      return keys.map((key) => formModel[key]).join('');
    })
    let timer: ReturnType<typeof setTimeout> | null = null;
    watch(values, () => {
      if (timer) {
        clearTimeout(timer)
      }
      timer = setTimeout(() => {
        const formModel = this.formRef.value.formModel
        // 替换动态参数，如果有 ${xxx} 则替换为实际值
        let tempDictTable = dictTable.replace(/\${([^}]+)}/g, (_$0, $1) => {
          if (formModel[$1] == null) {
            return ''
          }
          return formModel[$1]
        });
        this.updateDictTable(tempDictTable)
      }, 150)
    }, {immediate: true})
  }

  updateDictTable(_dictTable: string) {
    console.log('请在子类实现 updateDictTable 方法')
  }

  /**
   * 获取表字典的编码，可替换系统变量
   * @param dictTable
   * @param dictText
   * @param dictCode
   */
  genDictTableCode(dictTable: string, dictText: string, dictCode: string) {
    // 替换系统变量
    dictTable = replaceUserInfoByExpression(dictTable)
    return encodeURI(`${dictTable},${dictText},${dictCode}`);
  }

}
