import * as vue from 'vue';
import * as UTIL_CACHE from '/@/utils/cache';
import * as UTIL_AXIOS from '/@/utils/http/axios';
import * as HOOK_MESSAGE from '/@/hooks/web/useMessage';
import { randomString } from '/@/utils/common/compUtils';
import * as HOOK_USERINFO from '/@/store/modules/user';
import * as UTIL_AUTH from "/@/utils/auth";

// 在这里定义JS增强里可以使用的内容
const $exports = {
  vue,
  '@': {
    hooks: {
      // 调用示例：@/hooks/useMessage
      useMessage: HOOK_MESSAGE,
      useUserStore: HOOK_USERINFO
    },
    utils: {
      // 调用示例：@/utils/axios
      axios: UTIL_AXIOS,
      cache: UTIL_CACHE,
      auth: UTIL_AUTH,
    },
  },
};

/**
 * 用于处理js增强中自定义的hook代码
 * 增强定义方法：useCustomHook()，建议不要使用双引号
 * 其他导出对象
 * @param otherExports
 */
export function useCustomHook(otherExports?: Recordable, context?: any) {
  const assignExports = Object.assign({}, $exports, otherExports);

  /**
   * 自定义 import 方法
   * @param path 引用路径
   */
  function doImport(path: string) {
    if (path != null && path != '') {
      let paths = path.toString().split('/');
      let result = assignExports[paths[0]];
      for (let i = 1; i < paths.length; i++) {
        result = result[paths[i]];
      }
      return result;
    }
    return null;
  }

  function doExport() {}

  /**
   * 执行JS增强代码
   * @param code 要执行的代码
   */
  function executeJsEnhanced(code: string, row?) {
    // 为了避免方法名冲突，所以使用随机方法名
    let randomKey = randomString(6);
    // let importKey = '__import_' + randomKey
    //let importKey = 'customImport'
    let exportKey = '__export_' + randomKey;
    // 替换 import 关键字
    //code = replaceImportKey(code, importKey)
    
    //update-begin-author:taoyan date:2023-5-15 for: issues/516 自定义按钮_hook后的参数row未定义问题（参见#410） #516
    if(row){
      const executeCode = `return function (row, customImport, ${exportKey}) {"use strict"; ${code}}`;
      console.group('executeJsEnhanced');
      console.log(executeCode);
      console.groupEnd();
      const fun = new Function(executeCode)();
      fun.call(context, row, doImport, doExport);
    }else{
      const executeCode = `return function (customImport, ${exportKey}) {"use strict"; ${code}}`;
      console.group('executeJsEnhanced');
      console.log(executeCode);
      console.groupEnd();
      const fun = new Function(executeCode)();
      fun.call(context, doImport, doExport);
    }
    //update-end-author:taoyan date:2023-5-15 for: issues/516 自定义按钮_hook后的参数row未定义问题（参见#410） #516
 
  }

  /**
   * 替换 import 关键字
   * @param code
   * @param fnKey import 方法的key
   */
  /*  function replaceImportKey(code: string, fnKey: string) {
    let lines = code.split('\n')
    for (let i = 0; i < lines.length; i++) {
      let line = lines[i].trim()
      if (line.startsWith('import ')) {
        let regexp = /import (.*) from (.*)/g
        lines[i] = line.replace(regexp, `const $1 = ${fnKey}($2)`)
      }
    }
    return lines.join('\n')
  }*/

  return {
    executeJsEnhanced,
  };
}

/**获取函数体的内容作为字符串*/
export const GET_FUN_BODY_REG = /(?:\/\*[\s\S]*?\*\/|\/\/.*?\r?\n|[^{])+\{([\s\S]*)\}$/;
