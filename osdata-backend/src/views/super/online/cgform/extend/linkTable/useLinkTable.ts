import { defHttp } from '/@/utils/http/axios';
import { ref, watchEffect, computed, reactive, inject, type Ref } from 'vue'
import { pick } from 'lodash-es';
import { filterMultiDictText } from '/@/utils/dict/JDictSelectUtil';
import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';

function queryTableData(tableName, params){
  const url = '/online/cgform/api/getData/'+tableName;
  return defHttp.get({ url, params });
}

function queryTableColumns(tableName, params){
  const url = '/online/cgform/api/getColumns/'+tableName;
  return defHttp.get({ url, params });
}

// update-begin--author:liusq---date:20260811---for：【online一对多】缓存关联表列配置供展示态复用关联记录转换逻辑
const linkTableColumnCache = new Map<string, Promise<any>>();

export function getLinkTableColumns(tableName: string, selectFields: string) {
  const cacheKey = `${tableName}:${selectFields}`;
  if (!linkTableColumnCache.has(cacheKey)) {
    linkTableColumnCache.set(cacheKey, queryTableColumns(tableName, { linkTableSelectFields: selectFields }));
  }
  return linkTableColumnCache.get(cacheKey)!;
}
// update-end--author:liusq---date:20260811---for：【online一对多】缓存关联表列配置供展示态复用关联记录转换逻辑

function queryAllTableData(params: {
  tableName: string;
  showFields: string;
  valueField: string;
  keyValues?: string;
  pageNo?: number;
  pageSize?: number;
}) {
  return defHttp.get({ url: '/sys/dict/queryTableDataForLinkRecord', params });
}

// update-begin--author:liusq---date:20260811---for：【online一对多】关联记录编辑态和展示态复用同一翻译逻辑
export function transLinkTableRecord(data, columns, dictInfo) {
  for (const column of columns || []) {
    const { dataIndex, customRender } = column;
    if ((data[dataIndex] || data[dataIndex] === 0) && customRender === dataIndex && dictInfo?.[customRender]) {
      data[dataIndex] = filterMultiDictText(dictInfo[customRender], data[dataIndex]);
      continue;
    }
    const dictText = data[dataIndex + '_dictText'];
    if (dictText) {
      data[dataIndex] = dictText;
    }
  }
  return data;
}
// update-end--author:liusq---date:20260811---for：【online一对多】关联记录编辑态和展示态复用同一翻译逻辑

export function useLinkTable(props) {
  // 当前页面已授权的 Online 表单编码，后端据此校验它是否真的配置了目标关联表。
  const sourceFormCode = inject<Ref<string | null>>('tableId');
  
  //TODO 目前只支持查询第一页的数据，可以输入关键字搜索
  const pageNo = ref('1');
  // 查询列
  const baseParam = ref<any>({});
  // 搜素条件
  const searchParam = ref<any>({});
  // 第一个文本列
  const mainContentField = ref('');
  //权限数据
  const auths = reactive({
    add: true,
    update: true
  });

  //显示列
  const textFieldArray = computed(()=>{
    if(props.textField){
      return props.textField.split(',')
    }
    return []
  });
  const otherColumns = ref<any[]>([]);
  // 展示的列 配置的很多列，但是只展示三行
  const realShowColumns = computed(()=>{
    let columns = otherColumns.value;
    if(props.multi == true){
      return columns.slice(0, 3)
    }else{
      return columns.slice(0, 6)
    }
  });

  watchEffect(async ()=>{
    let table = props.tableName;
    if(table){
      // tableId 由 Online 页面挂载时写入；存在上下文时等待其就绪，避免先发出缺少源表单标识的请求。
      if (sourceFormCode && !sourceFormCode.value) {
        return;
      }
      if (props.queryMode === 'table') {
        const textFields = (props.textField || '').split(',').filter(Boolean);
        mainContentField.value = textFields[0] || '';
        otherColumns.value = textFields.slice(1).map((f) => ({ title: f, dataIndex: f }));
        tableColumns.value = textFields.map((f) => ({ title: f, dataIndex: f }));
        dictOptions.value = {};
        return;
      }
      let valueField = props.valueField || '';
      let textField = props.textField || '';
      let arr:any[] = [];
      if(valueField){
        arr.push(valueField)
      }
      if(textField){
        let temp = textField.split(',')
        mainContentField.value = temp[0]
        for(let field of temp){
          arr.push(field)
        }
      }
      let imageField = props.imageField || '';
      if(imageField){
        arr.push(imageField)
      }
      baseParam.value = {
        linkTableSelectFields: arr.join(','),
        // 非 Online 页面没有 tableId，保持原请求不变，继续按目标表自身权限处理。
        ...(sourceFormCode?.value ? { linkTableSourceCode: sourceFormCode.value } : {}),
      };
      await resetTableColumns()
      await reloadTableLinkOptions()
    }
  });
  
  const otherFields = computed(()=>{
    let textField = props.textField || '';
    let others:any[] = [];
    let labelField = ''
    if(textField){
      let temp = textField.split(',');
      labelField = temp[0];
      for(let i=0;i<temp.length;i++){
        if(i>0){
          others.push(temp[i])
        }
      }
    }
    return {
      others,
      labelField
    };
  });

  // 选项
  const selectOptions = ref<any[]>([]);
  const tableColumns = ref<any[]>([]);
  const dictOptions = ref<any>({});
  //const tableTitle = ref('')

  async function resetTableColumns(){
    let params = baseParam.value;
    const data = await queryTableColumns(props.tableName, params);
    tableColumns.value = data.columns;
    if(data.columns){
      let imageField = props.imageField;
      let arr = data.columns.filter(c=>c.dataIndex!=mainContentField.value && c.dataIndex!=imageField)
      otherColumns.value = arr;
    }
    dictOptions.value = data.dictOptions;
    // 权限数据
    console.log('隐藏的按钮', data.hideColumns);
    if(data.hideColumns){
      let hideCols = data.hideColumns;
      if(hideCols.indexOf('add')>=0){
        auths.add = false
      }else{
        auths.add = true
      }
      if(hideCols.indexOf('update')>=0){
        auths.update = false
      }else{
        auths.update = true
      }
    }
  }
  
  async function reloadTableLinkOptions(){
    let params = getLoadDataParams();
    const data = await queryTableData(props.tableName, params);
    let records = data.records;
    //tableTitle.value = data.head.tableTxt;
    let dataList:any[] = [];
    let { others, labelField } = otherFields.value;
    let imageField = props.imageField;
    if(records && records.length>0){
      for(let rd of records){
        let temp = {...rd};
        transData(temp);
        let result = Object.assign({}, pick(temp, others), {id:temp.id, label: temp[labelField], value: temp[props.valueField]});
        if(imageField){
          result[imageField] = temp[imageField]
        }
        dataList.push(result);
      }
    }
    //添加一个空对象 为add操作占位
    // update-begin--author:liaozhiyang---date:20240607---for：【TV360X-1095】高级查询关联记录去掉编辑按钮及去掉记录按钮
    props.editBtnShow && dataList.push({});
    // update-end--author:liaozhiyang---date:20240607---for：【TV360X-1095】高级查询关联记录去掉编辑按钮及去掉记录按钮
    selectOptions.value = dataList;
  }

  /**
   * 数据简单翻译-字典
   * @param data
   */
  function transData(data) {
    transLinkTableRecord(data, tableColumns.value, dictOptions.value);
  }


  //获取加载数据的查询条件
  function getLoadDataParams(){
    let params = Object.assign({pageSize: 100, pageNo: pageNo.value}, baseParam.value, searchParam.value);
    return params;
  }

  //设置查询条件
  function addQueryParams(text){
    if(!text){
      searchParam.value = {}
    }else{
      let arr = textFieldArray.value;
      let params:any[] = []
      let fields:any[] = []
      for(let i=0;i<arr.length;i++){
        if(i<=1){
          fields.push(arr[i]);
          params.push({field: arr[i], rule: 'like', val: text})
        }
      }
      // params[arr[i]] = `*${text}*`
      // params['selectConditionFields'] = fields.join(',')
      // searchParam.value = params;
      params['superQueryMatchType'] = 'or';
      params['superQueryParams'] = encodeURI(JSON.stringify(params));
      searchParam.value = params;
    }
  }
  
  async function loadOne(value){
    if(!value){
      return []
    }

    if (props.queryMode === 'table') {
      const textFields = (props.textField || '').split(',').filter(Boolean);
      const imageField = props.imageField || '';
      const fieldSet = new Set<string>(textFields);
      if (imageField) fieldSet.add(imageField);
      const showFields = Array.from(fieldSet).join(',');
      if (!showFields) return [];
      try {
        const data = await queryAllTableData({
          tableName: props.tableName,
          showFields,
          valueField: props.valueField,
          keyValues: value,
          pageNo: 1,
          pageSize: 200,
        });
        const records: any[] = data?.records || [];
        if (records.length > 0) return records;
        const firstTextField = textFields[0] || props.valueField;
        return (value + '').split(',').filter(Boolean).map((v) => ({ [props.valueField]: v, [firstTextField]: v }));
      } catch (e) {
        console.error('[useLinkTable] table loadOne 失败', e);
        return [];
      }
    }

    let valueFieldName = props.valueField;
    let params = {
      ...baseParam.value,
      pageSize: 100, 
      pageNo: pageNo.value,
    };
    params['superQueryMatchType'] = 'and';
    let valueCondition = [{field: valueFieldName, rule: 'in', val: value}]
    params['superQueryParams'] = encodeURI(JSON.stringify(valueCondition));
    const data = await queryTableData(props.tableName, params);
    let records = data.records;
    //tableTitle.value = data.head.tableTxt;
    let dataList:any[] = [];
    if(records && records.length>0){
      for(let item of records){
        let temp = {...item}
        transData(temp);
        dataList.push(temp);
      }
    }
    return dataList;
  }


  /**
   * true:数据一致；false:数据不一致
   * @param arr
   * @param value
   */
  function compareData(arr, value){
    if(!arr || arr.length==0){
      return false
    }
    let valueArray = value.split(',');
    if(valueArray.length!=arr.length){
      return false;
    }
    let flag = true;
    for(let item of arr){
      let temp = item[props.valueField];
      if(valueArray.indexOf(temp)<0){
        flag = false;
      }
    }
    return flag;
  }

  function formatData(formData){
    Object.keys(formData).map(k=>{
      if(formData[k] instanceof Array){
        formData[k] = formData[k].join(',')
      }
    })
  }

  function initFormData(formData, linkFieldArray, record){
    if(!record){
      record = {}
    }
    if(linkFieldArray && linkFieldArray.length>0){
      for(let str of linkFieldArray){
        let arr = str.split(',')
        //["表单字段,表字典字段"]
        let field = arr[0];
        let dictField = arr[1];
        if(!formData[field]){
          let value = record[dictField] || '';
          formData[field] = [value]
        }else{
          formData[field].push(record[dictField])
        }
      }
    }
  }


  // 获取图片地址
  function getImageSrc(item){
    if(props.imageField){
      let url = item[props.imageField];
      // update-begin--author:liaozhiyang---date:20250517---for：【TV360X-38】关联记录空间，被关联数据优多个图片时，封面图片不展示
      if(typeof url === 'string') {
        // 有多张图时默认取第一张
        url = url.split(',')[0]
      }
      // update-end--author:liaozhiyang---date:20250517---for：【TV360X-38】关联记录空间，被关联数据优多个图片时，封面图片不展示
      return getFileAccessHttpUrl(url);
    }
    return ''
  }
  const showImage = computed(()=>{
    if(props.imageField){
      return true;
    }else{
      return false;
    }
  });
  
  
  return {
    pageNo,
    otherColumns,
    realShowColumns,
    selectOptions,
    reloadTableLinkOptions,
    textFieldArray,
    addQueryParams,
    tableColumns,
    transData,
    mainContentField,
    loadOne,
    compareData,
    formatData,
    initFormData,
    getImageSrc,
    showImage,
    auths
  }
}
