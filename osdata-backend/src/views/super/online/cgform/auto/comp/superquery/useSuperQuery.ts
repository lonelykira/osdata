import { useModalInner } from '/@/components/Modal';
import { randomString } from '/@/utils/common/compUtils';
import { reactive, ref, toRaw, watch } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { Modal } from 'ant-design-vue';
import { createLocalStorage } from '/@/utils/cache';
import { useRoute } from 'vue-router';
import FormSchemaFactory from '../factory/FormSchemaFactory';
import {useExtendComponent} from '../../../hooks/auto/useExtendComponent'
import { cloneDeep } from 'lodash-es';
/**
 * 表单类型转换成查询类型
 * 普通查询和高级查询组件区别 ：高级查询不支持联动组件
 */
const FORM_VIEW_TO_QUERY_VIEW = {
  "password": "text",
  "file": "text",
  "image": "text",
  "textarea": "text",
  "umeditor": "text",
  "markdown": "text",
  "checkbox": "list_multi",
  "radio": "list",
}

// 查询条件存储编码前缀
const SAVE_CODE_PRE = 'JSuperQuerySaved_';

/**
 * 查询项
 * */
interface SuperQueryItem {
  field: string|undefined;
  rule: string|undefined;
  val: string|number;
  key: string;
  // 解决inputNumber组件对不齐样式问题
  curLineAlign: string | undefined;
  fileType: string;
  // update-begin--author:liaozhiyang---date:20240611---for：【TV360X-461】字段类型是string，控件是text，则默认模糊查询
  view: string;
  // 最先原始的组件类型；view字段可能会被改变
  originView?: string;
  // update-end--author:liaozhiyang---date:20240611---for：【TV360X-461】字段类型是string，控件是text，则默认模糊查询
}
/**
 * 查询项-第一个控件树model
 * */
interface TreeModel {
  title: string,
  value: string,
  isLeaf?: boolean,
  disabled?: boolean,
  children?: TreeModel[],
  order?: number,
  fieldType?: string;
  // update-begin--author:liaozhiyang---date:20240611---for：【TV360X-461】字段类型是string，控件是text，则默认模糊查询
  view: string;
  originView?: string;
  // update-end--author:liaozhiyang---date:20240611---for：【TV360X-461】字段类型是string，控件是text，则默认模糊查询
}

/**
 * 查询信息保存结构
 * */
interface SaveModel{
  title: string,
  content: string,
  type: string,
}

export function useSuperQuery(props){
  // 添加表单组件
  const {linkTableCard2Select} = useExtendComponent();
  
  const { createMessage: $message } = useMessage();
  /** 表单ref*/
  const formRef = ref<any>();
  
  /** 数据*/
  const dynamicRowValues = reactive<{ values: SuperQueryItem[] }>({
    values: [],
  });
  /** and/or */
  const matchType = ref('and');

  // 保存查询弹窗确定按钮loading状态
  const saveModalLoading = ref(false);
  // 弹框显示
  const [registerModal, {setModalProps}] = useModalInner(() => {
    setModalProps({confirmLoading: false});
  })
  
  // 高级查询类型不支持联动组件，需要额外设置联动组件的view为text
  const view2QueryViewMap = Object.assign({}, {"link_down":"text"}, FORM_VIEW_TO_QUERY_VIEW)
  
  /**
   * 确认按钮事件
   */
  function handleSubmit() {
    console.log('handleSubmit', dynamicRowValues.values)
  }

  /**
   * 关闭按钮事件
   */
  function handleCancel() {
    //closeModal();
  }
  
  /**
   * val组件赋值
   */
  function setFormModel(key: string, value: any, item: any) {
    console.log('setFormModel', key, value)
   // formModel[key] = value;
    item['val'] = value;
  }

  // 字段-Properties
  const fieldProperties = ref<any>({})
  // 字段-左侧查询项-树控件数据
  const fieldTreeData = ref<any>([])
  // update-begin--author:liaozhiyang---date:20240607---for：【TV360X-503】过滤图片，文件、密码组件
  const filterComponent = (data) => {
    const { properties = {} } = data;
    Object.entries(properties).forEach(([field, value]) => {
      if (value.view === 'table') {
        filterComponent(value);
      }
      // update-begin--author:liaozhiyang---date:20260713---for：过滤掉他表字段(link_table_field)，不作为高级查询条件
      if (['link_down', 'link_table_field'].includes(value.originView || value.view)) {
        delete properties[field];
      }
      // update-end--author:liaozhiyang---date:20260713---for：过滤掉他表字段(link_table_field)，不作为高级查询条件
    });
  };
  // update-end--author:liaozhiyang---date:20240607---for：【TV360X-503】过滤图片，文件、密码组件
  /**
   * 初始化数据-最开始的方法
   * 1.获取 表名@字段名-->配置 这样的一个map
   * 2.获取树形结构的数据 显示:文本； 存储:表名@字段名
   * 当树改变时，及时获取配置更新表单
   * @param json
   */
  function init(json) {
    console.log('=============')
    console.log('=============', json)
    console.log('=============')
    // update-begin--author:liaozhiyang---date:20240607---for：【TV360X-503】过滤图片，文件、密码组件
    filterComponent(json);
    // update-end--author:liaozhiyang---date:20240607---for：【TV360X-503】过滤图片，文件、密码组件
    let { allFields, treeData } = getAllFields(json);
    fieldProperties.value = allFields;
    // update-end--author:liaozhiyang---date:20240612---for：【TV360X-1005】有子表时结构化主表且超长省略
    const properties = json.properties ?? {};
    const subTable: string[] = [];
    const tableName = json.table;
    Object.entries(properties).forEach(([key, value]: [string, any]) => {
      if (value.view === 'table') {
        subTable.push(key);
      }
    });
    if (subTable.length) {
      let arr: TreeModel[] = [];
      arr = treeData.filter((item) => !subTable.includes(item.value));
      for (let i = 0, len = treeData.length; i < len; i++) {
        const item = treeData[i];
        if (!subTable.includes(item.value)) {
          treeData.splice(i, 1);
          i--;
          len--;
        }
      }
      treeData.unshift({ title: '主表', value: tableName, disabled: true, order: 200, children: arr, view: 'table' });
    }
    // update-end--author:liaozhiyang---date:20240612---for：【TV360X-1005】有子表时结构化主表且超长省略
    fieldTreeData.value = treeData;
  }

  /**
   * 左侧查询项 添加一行
   * @param index
   */
  function addOne(index) {
    let item = {
      field: undefined,
      rule: 'eq',
      val:'',
      key: randomString(16)
    }
    if(index===false){
      // 重置后需要调用
      dynamicRowValues.values = []
      dynamicRowValues.values.push(item)
    }else if(index===true){
      // 打开弹框是需要调用
      if(dynamicRowValues.values.length==0){
        dynamicRowValues.values.push(item)
      }
    }else{
      // 其余就是 正常的点击加号增加行
      dynamicRowValues.values.splice(++index, 0, item)
    }
  }

  /**
   * 左侧查询项 删除一行
   */
  function removeOne(item: SuperQueryItem) {
    let arr = toRaw(dynamicRowValues.values);
    let index = -1;
    for(let i=0;i<arr.length;i++){
      if(item.key == arr[i].key){
        index = i;
        break;
      }
    }
    if(index != -1){
      dynamicRowValues.values.splice(index, 1);
    }
  }

  // 默认的输入框
  const defaultInput = {
    field: "val",
    label: "测试",
    component:"Input"
  }

  /**
   * 左侧查询项 val组件 schema获取, 替代左侧字段树的change事件
   * @param item
   * @param index
   */
  function getSchema(item, index) {
    let map = fieldProperties.value
    let prop = map[item.field]
    if(!prop){
      return defaultInput
    }
    if(view2QueryViewMap[prop.view]){
      // 如果出现查询条件联动组件出来的场景，请跟踪此处
      prop.view = view2QueryViewMap[prop.view]
    }
    let temp = FormSchemaFactory.createFormSchema(item.field, prop)
   // temp.setFormRef(formRef)
    temp.noChange()
    // 查询条件中的 下拉框popContainer为parentNode
    temp.asSearchForm();
    temp.updateField(item.field+index)
    const setFieldValue = (values)=>{
      item['val'] = values[item.field]
    }
    temp.setFunctionForFieldValue(setFieldValue)
    let schema = temp.getFormItemSchema()
    //schema['valueField'] = 'val'
    // 特殊规则，需要禁用组件
    // 为空、不为空
    if (['empty', 'not_empty'].includes(item.rule)) {
      schema.componentProps = { ...schema.componentProps, disabled: true };
    }
    linkTableCard2Select(schema);
    // update-begin--author:liaozhiyang---date:20240607---for：【TV360X-389】普通查询关联记录去掉编辑按钮
    if (schema.component === 'LinkTableSelect') {
      let componentProps = schema.componentProps ?? {};
      schema.componentProps = { ...componentProps, editBtnShow: false };
      // update-begin--author:scott---date:20260713---for：高级查询多选-关联记录在"在...中"/"等于"下语义是包含多个值，强制多选，不受字段单选配置限制
      if (['in', 'eq'].includes(item.rule)) {
        schema.componentProps.multi = true;
      }
      // update-end--author:scott---date:20260713---for：高级查询多选-关联记录在"在...中"/"等于"下语义是包含多个值，强制多选，不受字段单选配置限制
    }
    // update-end--author:liaozhiyang---date:20240607---for：【TV360X-389】普通查询关联记录去掉编辑按钮
    // update-begin--author:liaozhiyang---date:20231219---for：【QQYUN-7640】高级查询数字组件会偏移
    if (schema && schema.component === 'InputNumber') {
      item.curLineAlign = 'start';
    }
    // update-end--author:liaozhiyang---date:20231219---for：【QQYUN-7640】高级查询数字组件会偏移
    // update-begin--author:liaozhiyang---date:20240223---for：【QQYUN-8229】高级选择自定义树下拉显示不全
    if (schema?.component === 'JTreeSelect') {
      let componentProps: any = schema.componentProps;
      if (componentProps) {
        componentProps.getPopupContainer = () => document.body
      } else {
        schema.componentProps = { getPopupContainer: () => document.body }
      };
    }
    // update-end--author:liaozhiyang---date:20240223---for：【QQYUN-8229】高级选择自定义树下拉显示不全
    // update-begin--author:liaozhiyang---date:20240529---for：【TV360X-499】高级查询开关组件换成下拉，用户组件不显示按钮
    if (schema?.component === 'JSwitch') {
      const componentProps = schema.componentProps ?? {};
      schema.componentProps = { ...componentProps, query: true };
    }
    if (schema?.component === 'JSelectUser') {
      const componentProps = schema.componentProps ?? {};
      schema.componentProps = { ...componentProps, showButton: false };
    }
    // update-end--author:liaozhiyang---date:20240529---for：【TV360X-499】高级查询开关换成下拉，用户组件不显示按钮
    return schema
  }

  /*-----------------------右侧保存信息相关-begin---------------------------*/

  /**
   * 右侧树 的 数据
   */
  const saveTreeData = ref<any>('')
  // 本地缓存
  const $ls = createLocalStorage();
  //需要保存的信息（一条）
  const saveInfo = reactive({
    visible: false,
    title: '',
    content: '',
    saveCode: ''
  });
  //按钮loading
  const loading = ref(false)

  // 当前页面路由
  const route = useRoute();
  // update-begin--author:liaozhiyang---date:20240514---for：【issues/6205】高级查询组件增加保存条件自定义存储方式
  if (props.isCustomSave) {
    watch(props.saveSearchData, () => {
      currentPageSavedArray.value = props.saveSearchData;
    });
  } else {
    // 监听路由信息，路由发生改变，则重新获取保存的查询信息-->currentPageSavedArray
    watch(()=>route.fullPath, (val)=>{
      console.log('fullpath', val);
        initSaveQueryInfoCode();
    });
  }
  // update-end--author:liaozhiyang---date:20240514---for：【issues/6205】高级查询组件增加保存条件自定义存储方式
  
  // 当前页面存储的 查询信息
  const currentPageSavedArray = ref<SaveModel[]>([]);
  // 监听当前页面是否有新的数据保存了，然后更新右侧数据->saveTreeData
  watch(()=>currentPageSavedArray.value, (val)=>{
    let temp:any[] = []
    if(val && val.length>0){
      val.map(item=>{
        let key = randomString(16)
        temp.push({
          title: item.title,
          slots: { icon: 'custom' },
          value: key
        })
      })
    }
    saveTreeData.value = temp
  }, {immediate:true, deep: true})


  // 重新获取保存的查询信息
  function initSaveQueryInfoCode(){
    // update-begin--author:liaozhiyang---date:20240514---for：【issues/6205】高级查询组件增加保存条件自定义存储方式
    if (props.isCustomSave) {
      currentPageSavedArray.value = cloneDeep(props.saveSearchData);
    } else {
      let code = SAVE_CODE_PRE + route.fullPath;
      saveInfo.saveCode = code;
      let list = $ls.get(code);
      if(list && list instanceof Array){
        currentPageSavedArray.value = list
      }
    }
    // update-end--author:liaozhiyang---date:20240514---for：【issues/6205】高级查询组件增加保存条件自定义存储方式
  }

  // 执行一次 获取保存的查询信息
  initSaveQueryInfoCode();
  
  /**
   * 保存按钮事件
   */
  function handleSave(){
    // 获取实际数据转成字符串
    let fieldArray = getQueryInfo();
    if(!fieldArray){
      $message.warning('空条件不能保存')
      return;
    }
    let content = JSON.stringify(fieldArray)
    openSaveInfoModal(content)
  }

  // 输入保存标题 弹框显示
  function openSaveInfoModal(content){
    saveInfo.visible = true;
    saveInfo.title = '';
    saveInfo.content = content
  }

  /**
   * 确认保存查询信息
   */
  function doSaveQueryInfo(){
    let { title, content, saveCode } = saveInfo;
    let index = getTitleIndex(title);
    const saveSearchCondition = (type) => {
      // update-begin--author:liaozhiyang---date:20240514---for：【issues/6205】高级查询组件增加保存条件自定义存储方式
      const curPageSave: any = cloneDeep(currentPageSavedArray.value);
      saveModalLoading.value = true;
      if (type) {
        // 覆盖已有
        curPageSave.splice(index, 1, {
          content,
          title,
          type: matchType.value,
        });
      } else {
        curPageSave.push({
          content,
          title,
          type: matchType.value,
        });
      }
      const run = () => {
        saveInfo.visible = false;
        $message.success('保存成功');
        currentPageSavedArray.value = curPageSave;
        saveModalLoading.value = false;
      };
      if (props.isCustomSave) {
        props
          .save(curPageSave, 0)
          .then(() => {
            run();
          })
          .catch((err) => {
            saveModalLoading.value = false;
          });
      } else {
        // update-begin--author:liaozhiyang---date:20240306---for：【QQYUN-8357】高级查询保存的查询条件缓存改成一个月
        const expire = 60 * 60 * 24 * 30;
        // update-end--author:liaozhiyang---date:20240306---for：【QQYUN-8357】高级查询保存的查询条件缓存改成一个月
        $ls.set(saveCode, curPageSave, expire);
        run();
      }
      // update-end--author:liaozhiyang---date:20240514---for：【issues/6205】高级查询组件增加保存条件自定义存储方式
    };
    if (index >= 0) {
      // 已存在是否覆盖
      Modal.confirm({
        title: '提示',
        content: `${title} 已存在，是否覆盖？`,
        okText: '确认',
        cancelText: '取消',
        onOk: () => {
          saveSearchCondition(1);
        },
      });
    } else {
      saveSearchCondition(0);
    }
  }

  // 根据填入的 title找本地存储的信息，如果有需要询问是否覆盖
  function getTitleIndex(title){
    let savedArray = currentPageSavedArray.value
    let index = -1;
    for(let i=0;i<savedArray.length;i++){
      if(savedArray[i].title == title){
        index = i;
        break;
      }
    }
    return index;
  }
  
  /**
   * 获取左侧所有查询条件，如果没有/或者条件无效则返回false
   */
  function getQueryInfo(isEmit = false){
    let arr = dynamicRowValues.values;
    if(!arr || arr.length==0){
      return false;
    }
    let fieldArray:any = [];
    let fieldProps = fieldProperties.value;
    for(let item of arr){
      let allowEmpty = ['empty', 'not_empty'].includes(item.rule!)
      if(item.field && (allowEmpty || item.val || item.val===0) && item.rule){
        let prop = fieldProps[item.field]
        // 自定义格式化数据
        let formatValue = prop?.formatValue ?? (v => v)
        let tempVal:any = toRaw(item.val)
        if(tempVal instanceof Array){
          tempVal = tempVal.map(v => formatValue(v)).join(',')
        } else {
          tempVal = formatValue(tempVal)
        }
        let fieldName = getRealFieldName(item)
        let obj = {
          field: fieldName,
          rule: item.rule,
          val: tempVal,
          fileType: item.fileType ,
        };
        if(isEmit===true){
          //如果当前数据用于emit事件，需要设置dbtype和type
          let prop = fieldProps[item.field]
          if(prop){
            obj['type'] = prop.view
            obj['dbType'] = prop.type
          }
        }
        fieldArray.push(obj)
      }
    }
    if(fieldArray.length==0){
      return false;
    }
    return fieldArray
  }

  //update-begin-author:taoyan date:2022-5-31 for: VUEN-1148 主子联动下，高级查询查子表数据，无效
  /**
   * 高级查询参数 字段名
   * 获取后台需要的 字段名格式：表名,字段名
   * @param item
   */
  function getRealFieldName(item){
    let fieldName = item.field
    if(fieldName.indexOf('@')>0){
      fieldName = fieldName.replace('@', ',')
    }
    return fieldName;
  }
  //update-end-author:taoyan date:2022-5-31 for: VUEN-1148 主子联动下，高级查询查子表数据，无效

  /**
   * 右侧数据 点击事件，重新将数据显示到左侧
   * @param key
   * @param node
   */
  function handleTreeSelect(key, {node}){
    console.log(key, node)
    let title = node.dataRef.title
    let arr = currentPageSavedArray.value.filter(item=>item.title==title)
    if(arr && arr.length>0){
      // 拿到数据渲染
      let { content, type } = arr[0]
      let data = JSON.parse(content)
      let rowsValues: SuperQueryItem[] = []
      for(let item of data){
        // update-begin--author:liaozhiyang---date:20240108---for：【issues/962】高级查询保存的查询是子表，下次查询不出结果
        item.field = item.field.replace(',','@');
        // update-end--author:liaozhiyang---date:20240108---for：【issues/962】高级查询保存的查询是子表，下次查询不出结果
        rowsValues.push(Object.assign({}, {key: randomString(16)}, item))
      }
      dynamicRowValues.values = rowsValues
      matchType.value = type
    }
  }

  /**
   * 右侧数据 删除事件
   */
  function handleRemoveSaveInfo(title){
    console.log(title)
    let index = getTitleIndex(title)
    if(index>=0){
      // update-begin--author:liaozhiyang---date:20240513---for：【issues/6205】高级查询组件增加保存条件自定义存储方式
      if (props.isCustomSave) {
        const curPageSave = cloneDeep(currentPageSavedArray.value);
        curPageSave.splice(index, 1);
        props
          .save(curPageSave, 1)
          .then(() => {
            currentPageSavedArray.value = curPageSave;
          })
          .catch((err) => {
            console.log(`删除是吧~，${err}`);
          });
      } else {
        currentPageSavedArray.value.splice(index, 1);
        $ls.set(saveInfo.saveCode, currentPageSavedArray.value);
      }
      // update-end--author:liaozhiyang---date:20240513---for：【issues/6205】高级查询组件增加保存条件自定义存储方式
    }
  }

  /*-----------------------右侧保存信息相关-end---------------------------*/

  // 获取所有字段配置信息
  function getAllFields(properties){
    // 获取所有配置 查询字段 是否联合查询
   // const {properties, table, title } = json;
    let allFields = {}
    let order = 1;
    let treeData:TreeModel[] = []
 /*   let mainNode:TreeModel = {
      title,
      value: table,
      disabled: true,
      children: []
    };*/
    //treeData.push(mainNode)
    if(properties.properties){
      properties = properties.properties
    }
    Object.keys(properties).map(field=>{
      let item = properties[field]
      if(item.view == 'table'){
        // 子表字段
        // 联合查询开启才需要子表字段作为查询条件
        let subProps = item['properties'] || item['fields']
        let subTableOrder = order * 100;
        let subNode:TreeModel = {
          title: item.title,
          value: field,
          disabled: true,
          children: [],
          order: subTableOrder,
          // update-begin--author:liaozhiyang---date:20240306---for：【TV360X-461】字段类型是string，则默认模糊查询
          fieldType: item.type,
          // update-end--author:liaozhiyang---date:20240306---for：【TV360X-461】字段类型是string，则默认模糊查询
        }
        Object.keys(subProps).map(subField=>{
          let subItem = subProps[subField];
          // 保证排序统一
          subItem['order'] = subTableOrder + subItem['order']
          let subFieldKey = field+'@'+subField
          allFields[subFieldKey] = subItem
          subNode.children!.push({
            title: subItem.title,
            value: subFieldKey,
            isLeaf: true,
            order: subItem['order'],
            // update-begin--author:liaozhiyang---date:20240306---for：【TV360X-461】字段类型是string，则默认模糊查询
            fieldType: subItem.type,
            view: subItem.view,
            originView: subItem.view,
            // update-end--author:liaozhiyang---date:20240306---for：【TV360X-461】字段类型是string，则默认模糊查询
          })
        });
        orderField(subNode);
        treeData.push(subNode);
        order++;
      }else{
        // 主表字段
        //let fieldKey = table+'@'+field
        let fieldKey = field
        allFields[fieldKey] = item
        treeData.push({
          title: item.title,
          value: fieldKey,
          isLeaf: true,
          order: item.order,
          // update-begin--author:liaozhiyang---date:20240306---for：【TV360X-461】字段类型是string，则默认模糊查询
          fieldType: item.type,
          view: item.view,
          originView: item.view,
          // update-end--author:liaozhiyang---date:20240306---for：【TV360X-461】字段类型是string，则默认模糊查询
        });
      }
    });
    orderField(treeData);
    return {allFields, treeData}
  }
  
  //根据字段的order重新排序
  function orderField(data){
    let arr = data.children || data;
    arr.sort(function (a, b) {
      return a.order - b.order
    });
  }

  function initDefaultValues(values) {
    const { params, matchType } = values
    if(params){
      let rowsValues: SuperQueryItem[] = []
      for(let item of params){
        rowsValues.push(Object.assign({}, {key: randomString(16)}, item))
      }
      dynamicRowValues.values = rowsValues
      matchType.value = matchType
    }
  }
  
  return {
    formRef,
    init,
    dynamicRowValues,
    matchType,
    registerModal,
    handleSubmit,
    handleCancel,
    handleSave,
    doSaveQueryInfo,
    saveInfo,
    saveTreeData,
    handleRemoveSaveInfo,
    handleTreeSelect,
    fieldTreeData,
    addOne,
    removeOne,
    setFormModel,
    getSchema,
    loading,
    getQueryInfo,
    initDefaultValues,
    saveModalLoading,
    fieldProperties,
  }
}
