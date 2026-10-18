export const keywords: any = {
  list: [
    //------  列表api -------
    // 属性
    { text: '.acceptHrefParams', displayText: 'acceptHrefParams', superiors: 'this', desc: '获取地址栏上的条件' },
    { text: '.currentPage', displayText: 'currentPage', superiors: 'this', desc: '获取当前页数，默认1' },
    { text: '.currentTableName', displayText: 'currentTableName', desc: '获取当前表名' },
    { text: '.description', displayText: 'description', superiors: 'this', desc: '获取当前表描述' },
    { text: '.hasChildrenField', displayText: 'hasChildrenField', superiors: 'this', desc: '如果是树形列表，获取是否有子节点字段名' },
    { text: '.ID', displayText: 'ID', superiors: 'this', desc: '获取当前表的配置ID' },
    { text: '.pageSize', displayText: 'pageSize', superiors: 'this', desc: '获取当前每页条数，默认10' },
    { text: '.queryParam', displayText: 'queryParam', superiors: 'this', desc: '获取查询表单的查询条件' },
    { text: '.selectedRowKeys', displayText: 'selectedRowKeys', superiors: 'this', desc: '获取选中行的id的数组' },
    { text: '.selectedRows', displayText: 'selectedRows', superiors: 'this', desc: '获取选中行的数据数组' },
    { text: '.sortField', displayText: 'sortField', superiors: 'this', desc: '获取排序字段，默认‘id’' },
    { text: '.sortType', displayText: 'sortType', superiors: 'this', desc: '获取排序类型，默认升序‘asc’' },
    { text: '.total', displayText: 'total', superiors: 'this', desc: '获取总条数' },
    { text: '.loading', displayText: 'loading', superiors: 'this', desc: '设置/获取loading' },
    // 方法
    { text: '.loadData()', displayText: 'loadData()', superiors: 'this', desc: '加载数据' },
    { text: '.clearSelectedRow()', displayText: 'clearSelectedRow()', superiors: 'this', desc: '清除选中的行' },
    {
      text: '.getLoadDataParams()',
      displayText: 'getLoadDataParams()',
      superiors: 'this',
      desc: '获取所有的查询条件，返回一个对象，包括：查询表单，高级查询，地址栏参数，分页信息，排序信息等',
    },
    { text: '.isTree()', displayText: 'isTree()', superiors: 'this', desc: '判断当前表是不是树，返回布尔值' },
    // 事件(前置)
    {
      text: `beforeEdit(row){
  return new Promise((resolve, reject) => {
    if(row.字段名 == '字段值'){
      reject('测试~');
    }else{
      resolve();
    }
  })     
}`,
      displayText: 'beforeEdit(row){}',
      desc: '点击操作列下的编辑按钮触发，返回promise对象',
    },
    {
      text: `beforeDelete(row){
	return new Promise((resolve, reject) => {
  	if(row.字段名 == '字段值'){
    	reject('测试~');
    }else{
    	resolve();
    }
  })     
}`,
      displayText: 'beforeDelete(row){}',
      desc: '点击操作列下的删除按钮触发，返回promise对象',
    },
    { text: 'console.log()', displayText: 'console.log()', desc: '打印日志' },
  ],
  form: [
    //------ 表单api -------
    // 属性
    { text: '.loading', displayText: 'loading', superiors: 'this', desc: '是否加载中，返回的是一个ref对象' },
    { text: '.isUpdate', displayText: 'isUpdate', superiors: 'this', desc: '是否是编辑页面，返回的是一个ref对象' },
    { text: '.onlineFormRef', displayText: 'onlineFormRef', superiors: 'this', desc: '主表/单表表单的ref对象' },
    { text: '.refMap', displayText: 'refMap', superiors: 'this', desc: '子表表单/子表table的ref对象map，key为子表表名' },
    { text: '.subActiveKey', displayText: 'subActiveKey', superiors: 'this', desc: '子表的激活的tab索引值对应的字符串，从‘0’开始，返回的是一个ref对象' },
    { text: '.sh', displayText: 'sh', superiors: 'this', desc: '单表/主表字段的显示隐藏状态' },
    { text: '.submitFlowFlag', displayText: 'submitFlowFlag', superiors: 'this', desc: '是否提交表单后自动提交流程，返回一个ref对象' },
    { text: '.subFormHeight', displayText: 'subFormHeight', superiors: 'this', desc: '一对一子表表单的高度，不需要设置，返回一个ref对象' },
    { text: '.subTableHeight', displayText: 'subTableHeight', superiors: 'this', desc: '一对多子表table的高度，不需要设置，返回一个ref对象' },
    { text: '.tableName', displayText: 'tableName', superiors: 'this', desc: '当前表名，返回的是一个ref对象' },
    { text: '.$nextTick', displayText: '$nextTick', superiors: 'this', desc: '调用的是vue3的nextTick' },
    { text: '.字段名_load', displayText: '字段名_load', superiors: 'this', desc: '控制字段的加载与否，设置为false表示当前字段不加载' },
    { text: '.字段名_disabled', displayText: '字段名_disabled', superiors: 'this', desc: '控制字段的禁用与否，设置为true表示当前字段禁用' },
    // 方法
    { text: '.addSubRows(tableName, rows)', displayText: 'addSubRows(tableName, rows)', superiors: 'this', desc: '往一对多子表table里添加数据' },
    {
      text: '.changeOptions(field, options)',
      texdisplayTextt: 'changeOptions(field, options)',
      superiors: 'this',
      desc: '改变单表/主笔 下拉控件的下拉选项',
    },
    { text: '.clearSubRows(tableName)', displayText: 'clearSubRows(tableName)', superiors: 'this', desc: '清空一对多子表table的数据' },
    {
      text: '.clearThenAddRows(tableName, rows)',
      displayText: 'clearThenAddRows(tableName, rows)',
      superiors: 'this',
      desc: '先清空一对多子表table的数据，再往里添加数据',
    },
    { text: '.getFieldsValue()', displayText: 'getFieldsValue()', superiors: 'this', desc: '获取主表/单表 所有字段的值' },
    {
      text: '.getSubTableInstance(tableName)',
      displayText: 'getSubTableInstance(tableName)',
      superiors: 'this',
      desc: '获取子表的实例对象，这个对象可以调用子表table的方法',
    },
    { text: '.setFieldsValue(row)', displayText: 'setFieldsValue(row)', superiors: 'this', desc: '设置主表/单表 字段的值' },
    {
      text: '.triggleChangeValues(values,id,target)',
      displayText: 'triggleChangeValues(values,id,target)',
      superiors: 'this',
      desc: '改变单表/主表/子表 字段的值，一般用于change事件，其中id，target需要通过change事件的内置参数获取，如果不传id，target的值，则改变的是主表的字段',
    },
    { text: '.triggleChangeValue(field, value)', displayText: 'triggleChangeValue(field, value)', superiors: 'this', desc: '设置单表/主表 字段的值' },
    {
      text: '.onlineFormValueChange(field, value, otherValus)',
      displayText: 'onlineFormValueChange(field, value, otherValus)',
      superiors: 'this',
      desc: '定义后，当表单值改变的时候会触发该方法（因js增强hook方式不支持原来的onlChange，所以定义此方法）',
    },
    {
      text: '.changeSubTableOptions(tableName，field，options)',
      displayText: 'changeSubTableOptions(tableName，field，options)',
      superiors: 'this',
      desc: '改变一对一子表下拉框options',
    },
    {
      text: '.changeSubFormbleOptions(tableName，field，options)',
      displayText: 'changeSubFormbleOptions(tableName，field，options)',
      superiors: 'this',
      desc: '改变一对多子表下拉框options',
    },
    {
      text: '.changeRemoteOptions({ field, dict, label, type?, subTableName? })',
      displayText: 'changeRemoteOptions({ field, dict, label, type?, subTableName? })',
      superiors: 'this',
      desc: '改变动态下拉框options',
    },
    {
      text: '.submitFormAndFlow()',
      displayText: 'submitFormAndFlow()',
      superiors: 'this',
      desc: '表单提交且发起流程',
    },
    // 提交前置事件
    {
      text: `beforeSubmit(row){
	return new Promise((resolve, reject)=>{
    //此处模拟等待时间，可能需要发起请求
    setTimeout(()=>{
      if(row.字段名 == '字段值'){
        // 当某个字段不满足要求的时候可以reject 
        reject('测试~');
      }else{
        resolve();
      }
    },3000)
  })
}`,
      displayText: 'beforeSubmit(row){}',
      desc: '提交前置事件',
    },
    // 表单加载事件
    {
      text: `loaded(){
  this.$nextTick(()=>{
    // let text = '测试js增强设置默认值';
    // if(this.isUpdate.value === true){
    //   text = '测试js增强修改表单值';
    // }
    this.setFieldsValue({
      字段名: 修改的值
    })
  })
}`,
      displayText: 'loaded(){}',
      desc: '表单加载事件',
    },
    // 单表#表单值改变事件
    {
      text: `onlChange(){
  return {
    字段名(){
      let value = event.value
      console.log(value)
      this.triggleChangeValues({'字段名': '修改后的值'})
    }
  }
 }`,
      displayText: 'onlChange(){}',
      desc: '单表#表单值改变事件',
    },
    // 子表#表单值改变事件
    {
      text: `子表名_onlChange(){
  return {
    字段名(){
      let value = event.value;
      console.log(value);
      let row = {'字段名': '测试一对多值改变：'+value};
      this.triggleChangeValues(row, event.row.id, event.target)
  }
  }
}`,
      displayText: '子表名_onlChange(){}',
      desc: '子表#表单值改变事件',
    },
    // 子改主#表单值改变事件
    {
      text: `子表名_onlChange(){
  return {
    子表字段01(){
      this.getSubTableInstance('子表名').getValues((err,values)=>{
        this.triggleChangeValues({'主表字段名': '修改后的值'})
      }) 
    },
  }
}
`,
      displayText: '子表名_onlChange(){}',
      desc: '子改主#表单值改变事件',
    },
    // js增强实现下拉联动
    {
      text: `onlChange(){
  return {
    字段名01(){
      let value = event.value
      this.changeOptions('字段名02', '修改后的值');
    }
    字段名02(){
      let value = event.value
      this.changeOptions('字段名03', '修改后的值');
    }
  }
}`,
      displayText: 'changeOptions()',
      desc: 'js增强实现下拉联动',
    },
    { text: 'console.log()', displayText: 'console.log()', desc: '打印日志' },
  ],
  common: [
    // JS增强 http请求
    {
      text: `getAction('请求url', { 'key': 'value'}).then(res => {
  console.log(res)
})`,
      displayText: 'getAction(url, param)',
      desc: 'get请求',
    },
    {
      text: `postAction('请求url', { 'key': 'value'}).then(res => {
  console.log(res)
})`,
      displayText: 'postAction(url, param)',
      desc: 'post请求',
    },
    {
      text: `putAction('请求url', { 'key': 'value'}).then(res => {
  console.log(res)
})`,
      displayText: 'putAction(url, param)',
      desc: 'put请求',
    },
    {
      text: `deleteAction('请求url', { 'key': 'value'}).then(res => {
  console.log(res)
})`,
      displayText: 'deleteAction(url, param)',
      desc: 'delete请求',
    },
    { text: 'this', displayText: 'this', desc: '上下文' },
    {
      text: '.openCustomModal({title,width,row,formComponent,requestUrl,hide,show})',
      displayText: 'openCustomModal({title,width,row,formComponent,requestUrl,hide,show})',
      desc: '打开一个弹窗-参考 Js增强打开自定义弹窗',
    },
  ],
};
