/**
 * js增强
 */
import { reactive } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { _eval } from '/@/utils';
import { useMessage } from '/@/hooks/web/useMessage';

export function useEnhance(onlineTableContext, isList = true) {
  let EnhanceJS = reactive<any>({});

  const getAction = (url, params) => {
    return defHttp.get({ url: url, params }, { isTransformResponse: false });
  };

  const postAction = (url, params) => {
    return defHttp.post({ url: url, params }, { isTransformResponse: false });
  };

  const putAction = (url, params) => {
    return defHttp.put({ url: url, params }, { isTransformResponse: false });
  };

  const deleteAction = (url, params) => {
    return defHttp.delete({ url: url, params }, { isTransformResponse: false });
  };

  if (isList === true) {
    onlineTableContext['_getAction'] = getAction;
    onlineTableContext['_postAction'] = postAction;
    onlineTableContext['_putAction'] = putAction;
    onlineTableContext['_deleteAction'] = deleteAction;
    // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8342】js增强提供useMessage方法
    onlineTableContext['_useMessage'] = useMessage;
    // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8342】js增强提供useMessage方法
  } else {
    onlineTableContext.addObject2Context('_getAction', getAction);
    onlineTableContext.addObject2Context('_postAction', postAction);
    onlineTableContext.addObject2Context('_putAction', putAction);
    onlineTableContext.addObject2Context('_deleteAction', deleteAction);
    // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8342】js增强提供useMessage方法
    onlineTableContext.addObject2Context('_useMessage', useMessage);
    // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8342】js增强提供useMessage方法
  }

  /**
   * 初始化
   * @param str (res.result.enhanceJs)
   */
  function initCgEnhanceJs(str: string) {
    //console.log("--onlineList-js增强"+isList,str)
    if (str) {
      // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-338】js增强代码报错不能影响页面渲染
      let Obj: any;
      let result;
      try {
        // update-begin--author:liaozhiyang---date:20230904---for：【QQYUN-6390】eval替换成new Function，解决build警告
        Obj = _eval(str);
        // update-end--author:liaozhiyang---date:20230904---for：【QQYUN-6390】eval替换成new Function，解决build警告
        result = new Obj(getAction, postAction, deleteAction);
        //return new Function(str)(getAction,postAction,deleteAction);
      } catch (error) {
        result = {};
        const { createMessage } = useMessage();
        createMessage.warning(`js增强代码有语法错误，请检查代码~ ${error}`);
      }
      return result;
      // update-end--author:liaozhiyang---date:20240517---for：【TV360X-338】js增强代码报错不能影响页面渲染
    } else {
      return {};
    }
  }

  /**
   * 【】
   * 触发js增强方法
   * @param that
   * @param formData
   */
  function triggerJsFun(that, buttonCode) {
    if (EnhanceJS && EnhanceJS[buttonCode]) {
      EnhanceJS[buttonCode](that);
    }
  }

  /**
   * 【表单】
   * 处理js增强 自定义 提交前事件
   * @param that
   * @param formData
   */
  function customBeforeSubmit(that, formData) {
    if (EnhanceJS && EnhanceJS['beforeSubmit']) {
      return EnhanceJS['beforeSubmit'](that, formData);
    } else {
      return Promise.resolve();
    }
  }

  /**
   * 删除前业务处理
   * @param that
   * @param record
   */
  function beforeDelete(that, record) {
    if (EnhanceJS && EnhanceJS['beforeDelete']) {
      return EnhanceJS['beforeDelete'](that, record);
    } else {
      return Promise.resolve();
    }
  }

  if (isList === true) {
    if (onlineTableContext) {
      onlineTableContext['beforeDelete'] = (record) => {
        const onlEnhanceJS = onlineTableContext['EnhanceJS'];
        if (onlEnhanceJS && onlEnhanceJS['beforeDelete']) {
          return onlEnhanceJS['beforeDelete'](onlineTableContext, record);
        } else {
          return Promise.resolve();
        }
      };

      onlineTableContext['beforeEdit'] = (record) => {
        const onlEnhanceJS = onlineTableContext['EnhanceJS'];
        if (onlEnhanceJS && onlEnhanceJS['beforeEdit']) {
          return onlEnhanceJS['beforeEdit'](onlineTableContext, record);
        } else {
          return Promise.resolve();
        }
      };
    }
  }

  return {
    EnhanceJS,
    initCgEnhanceJs,
    customBeforeSubmit,
    beforeDelete,
    triggerJsFun,
  };
}
