import { useModalInner } from '/@/components/Modal';
import { computed, nextTick, reactive, ref, unref } from 'vue';
import { getRefPromise } from '../../hooks/auto/useAutoForm';
import { defHttp } from '/@/utils/http/axios';
import {ONL_FORM_TABLE_NAME} from '../../types/onlineRender'
import { useAppInject } from '/@/hooks/web/useAppInject';
/**
 * 创建online表单弹窗用
 */
export function useAutoModal(isBpm?: boolean, { emit } = {} as any, callback?:any) {
  const onlineFormCompRef = ref<any>(null);
  // 是否隐藏确认按钮
  const disableSubmit = ref(false);
  //表单风格 1列 2列 3列 决定了弹框的宽度
  const formTemplate = ref(1);
  // 自定义按钮
  const cgButtonList = ref([]);
  // js增强
  //const enhanceJsObject = ref<any>('')
  // 表单是否渲染完成
  const formRendered = ref(false);
  // 表单弹框最小宽度 取决于扩展配置
  const modalMinWidth = ref(0);
  // 判断是否是树的表单- 【VUEN-1056 15、严重——online树表单，添加的时候，父亲节点是空的】
  const isTreeForm = ref(false);
  // 如果是树，父id字段对应的字段名-【VUEN-1056 15、严重——online树表单，添加的时候，父亲节点是空的】
  const pidFieldName = ref('');
  // VUEN-1105 表单 确定按钮可多次点击，保存多条数据
  const submitLoading = ref(false);
  // 是否是修改页面
  const isUpdate = ref(false);
  // 是否是单表
  const single = ref(true);
  // extConfigJson
  const extConfigJson = reactive<any>({});
  // 显示子表- 详情页面modal 也会调用此hook，给详情页面用
  const showSub = ref(true);
  const customTitle = ref('')
  // 表单保存完后是否关闭modal
  const successThenClose = ref(true);
  // 提示是否保存
  const topTipVisible = ref(false)
  // 弹窗高度控制
  const { popModalFixedWidth, resetBodyStyle, popBodyStyle } = useFixedHeightModal();
  // 没有编辑权限: 默认false
  const FORM_DISABLE_UPDATE = ref(false);
  // 主题模板类型
  const themeTemplate = ref('');

  const { getIsMobile } = useAppInject();
  const modalObject = {
    handleOpenModal: (_data) => {},
  };

  //评论区域参数
  const tableId = ref('')
  const tableName = ref('')
  const formDataId = ref('')
  const enableComment = ref(false);
  let onlineExtConfig = {}

  //弹框标题
  const title = computed(() => {
    let temp = customTitle.value;
    if(temp){
      return temp;
    }
    if (unref(disableSubmit) === true) {
      return '详情';
    }
    if (unref(isUpdate) === true) {
      return '编辑';
    }
    return '新增';
  });

  // 弹框显示 触发onlineFormCompRef---show
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    customTitle.value = '';
    topTipVisible.value = false;
    if (isBpm === true) {
      await modalObject.handleOpenModal(data);
    } else {
      await handleOpenOnlineModal(data);
    }
    // 调整modal宽高
    resetBodyStyle();
    if(callback){
      callback();
    }
  });

  /**
   * 用于控制关联记录的字段 在列表上的弹窗权限，若无编辑权限，只能打开详情页面
   */
  const loadItemSuccess = ref(false);
  async function getFormStatus(){
    await getRefPromise(loadItemSuccess);
    return FORM_DISABLE_UPDATE.value;
  }
  
  
  async function handleOpenOnlineModal(data) {
    setModalProps({ confirmLoading: false });
    isUpdate.value = data.isUpdate;
    disableSubmit.value = data.disableSubmit || false;
    // href跳转的不需要查看子表
    if(data?.hideSub===true){
      showSub.value = false;
    }
    // 设置标题，支持从传入参数读取
    if(data?.title){
      customTitle.value = data.title;
    }
    if(data?.record){
      formDataId.value = data.record.id;
    }else{
      formDataId.value = ''
    }
    await nextTick(async () => {
      await getRefPromise(formRendered);
      
      //必须等formRendered之后才能调用
      handleCommentConfig();
      await onlineFormCompRef.value.show(data?.isUpdate, data?.record, data?.param);
    });
  }

  // 渲染完成改变状态
  function renderSuccess(extConfig) {
    formRendered.value = true;
    modalMinWidth.value = extConfig.modalMinWidth;
    if (extConfig.modelFullscreen == 1) {
      //如果全屏
      setModalProps({ defaultFullscreen: true });
    } else {
      setModalProps({ defaultFullscreen: false });
    }
    onlineExtConfig = extConfig
    // update-begin--author:liaozhiyang---date:20230327---for：【QQYUN-8644】移动端效果关闭聊天窗口（详情页）
    if (getIsMobile.value) {
      onlineExtConfig['commentStatus'] = 0;
    }
    // update-end--author:liaozhiyang---date:20230327---for：【QQYUN-8644】移动端效果关闭聊天窗口（详情页）
  }

  // 评论区域相关配置
  function handleCommentConfig(){
    let dataIdValue = formDataId.value;
    //如果有评论配置 且 编辑/详情页面 才需要开启评论
    if(onlineExtConfig['commentStatus'] == 1 && dataIdValue){
      enableComment.value = true;
      setModalProps({ defaultFullscreen: true });
    }else{
      enableComment.value = false;
    }
  }

  const singleWidth = 800;
  const one2ManyWidth = 1100;
  const modalWidth = computed(() => {
    // 不同的列数展示不同的宽度
    let diff = 200 * (formTemplate.value - 1);
    // 基值加上阈值
    let width = (!unref(single) ? one2ManyWidth : singleWidth) + diff;
    // 文本太长时，会遮挡页面【issues/I44F0R】
    width = calcModalMixWidth(width);
    let minWidth = modalMinWidth.value;
    console.log({ minWidth });
    //判断计算出来的宽度 是不是比扩展配置中的宽度参数小 如果是取扩展配置的参数
    if (minWidth && width < minWidth) {
      width = minWidth;
    }
    console.log({ width });
    return width;
  });

  /** 计算弹窗最小宽度 */
  function calcModalMixWidth(width) {
    let minWidth = extConfigJson.modalMinWidth;
    if (minWidth != null && minWidth !== '') {
      try {
        minWidth = Number.parseInt(minWidth);
        if (width < minWidth) {
          return minWidth;
        }
      } catch {
        console.warn('error modalMinWidth value: ', minWidth);
      }
    }
    return width;
  }

  // 自定义按钮 增强触发事件
  function handleCgButtonClick(optType, buttonCode) {
    onlineFormCompRef.value.handleCgButtonClick(optType, buttonCode);
  }

  function handleSubmit() {
    //update-begin-author:taoyan date:2022-5-25 for:  VUEN-1105 表单 确定按钮可多次点击，保存多条数据
    submitLoading.value = true;
    setTimeout(() => {
      submitLoading.value = false;
    }, 1500);
    //update-end-author:taoyan date:2022-5-25 for:  VUEN-1105 表单 确定按钮可多次点击，保存多条数据
    onlineFormCompRef.value.handleSubmit();
  }

  function handleCancel() {
    closeModal();
  }

  function loadFormItems(id, params={}) {
    let url = `/online/cgform/api/getFormItem/${id}`;
    return new Promise((resolve, reject) => {
      defHttp
        .get({ url, params }, { isTransformResponse: false })
        .then((res) => {
          console.log('表单结果》》modal:', res);
          if (res.success) {
            resolve(res.result);
          } else {
            reject(res.message);
          }
        })
        .catch(() => {
          reject();
        });
    });
  }

  async function handleFormConfig(id, params, callBack?, taskId?, currentTableName?) {
    // -update-begin--author:liaozhiyang---date:20240613---for：【TV360X-1000】流程一对多走流程的接口
    let result: any = null;
    if (taskId && currentTableName) {
      const url = `/online/cgform/api/getFormItemBytbname/${currentTableName}`;
      const params = { taskId };
      result = await defHttp.get({ url, params });
    } else {
      result = await loadFormItems(id, params);
    }
    // -update-end--author:liaozhiyang---date:20240613---for：【TV360X-1000】流程一对多走流程的接口
    // modal页面只处理按钮、JS增强、弹框宽度
    let dataFormTemplate = result.head.formTemplate;
    formTemplate.value = dataFormTemplate ? Number(dataFormTemplate) : 1;
    cgButtonList.value = result.cgButtonList;
    isTreeForm.value = result.head.isTree === 'Y';
    pidFieldName.value = result.head.treeParentIdField || '';
    tableId.value = result.head.id;
    tableName.value = result.head.tableName;
    themeTemplate.value = result.head.themeTemplate;
    //enhanceJsObject.value = initCgEnhanceJs(result.enhanceJs)
    if(result['form_disable_update']===true){
      FORM_DISABLE_UPDATE.value = true
    }else{
      FORM_DISABLE_UPDATE.value = false;
    }
    loadItemSuccess.value = true;
    emit && emit('formConfig', result);
    // -update-begin--author:liaozhiyang---date:20230823---for：【QQYUN-6305】tab主题一对多--
    callBack && callBack(result);
    // -update-end--author:liaozhiyang---date:20230823---for：【QQYUN-6305】tab主题一对多--
    await nextTick(async () => {
      let myForm = (await getRefPromise(onlineFormCompRef)) as any;
      await myForm.createRootProperties(result);
    });
  }

  /**
   * 表单保存完后的事件
   */
  function handleSuccess(formData) {
    // 将表名设置到数据中
    formData[ONL_FORM_TABLE_NAME] = tableName.value;
    emit('success', formData);
    if(successThenClose.value == true){
      closeModal();
    }else{
      // 不关闭弹窗 提示成功
    }
    //恢复默认值
    topTipVisible.value = false;
    successThenClose.value = true;
  }

  /**
   * modal关闭事件
   */
  function onCloseEvent(){
    if(onlineFormCompRef.value){
      onlineFormCompRef.value.onCloseModal();
    }
    // update-begin--author:liaozhiyang---date:20240618---for：【TV360X-1305】打开评论编辑弹窗会全屏，关闭弹窗时把全屏去掉，否者会影响新增弹窗
    if (isUpdate.value) {
      const extConfig: any = onlineExtConfig ?? {};
      if (extConfig.commentStatus == 1) {
        setModalProps({ defaultFullscreen: false });
      }
    }
    // update-end--author:liaozhiyang---date:20240618---for：【TV360X-1305】打开评论编辑弹窗会全屏，关闭弹窗时把全屏去掉，否者会影响新增弹窗
  }
  
  return {
    // modal
    title,
    modalWidth,
    registerModal,
    closeModal,
    modalObject,
    onCloseEvent,

    // 自定义按钮
    cgButtonList,
    handleCgButtonClick,

    // 提交/关闭按钮
    disableSubmit,
    handleSubmit,
    submitLoading,
    handleCancel,
    successThenClose,
    handleSuccess,
    topTipVisible,

    //表单
    handleFormConfig,
    onlineFormCompRef,
    formTemplate,
    isTreeForm,
    pidFieldName,
    renderSuccess,
    formRendered,
    isUpdate,
    showSub,
    themeTemplate,

    // 评论区域参数
    tableId,
    tableName,
    formDataId,
    enableComment,
    popBodyStyle,
    popModalFixedWidth,
    getFormStatus
  };
}


/**
 * 使用固定高度的modal
 */
export function useFixedHeightModal() {
  const minWidth = 800;
  const popModalFixedWidth = ref(800);
  let tempWidth = window.innerWidth - 300;
  if(tempWidth < minWidth){
    tempWidth = minWidth;
  }
  popModalFixedWidth.value = tempWidth;

  // 弹窗高度控制
  const popBodyStyle = ref({});
  function resetBodyStyle(){
    let height = window.innerHeight - 210;
    popBodyStyle.value = {
      height: height+'px',
      overflowY: 'auto'
    }
  }
  
  return {
    popModalFixedWidth,
    popBodyStyle,
    resetBodyStyle
  }
}

