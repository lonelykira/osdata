import dayjs from 'dayjs';
import { useUserStore } from '/@/store/modules/user';
import { defHttp } from '/@/utils/http/axios';
import { replaceAll, _eval } from '/@/utils';
import * as CustomExpression from '/@/utils/desform/customExpression';
import weekOfYear from 'dayjs/plugin/weekOfYear';
import quarterOfYear from 'dayjs/plugin/quarterOfYear';
dayjs.extend(weekOfYear);
dayjs.extend(quarterOfYear);
// 获取所有用户自定义表达式的Key
const ceKeys = Object.keys(CustomExpression);
// 将key用逗号拼接，可以拼接成方法参数，例：a,b,c --> function(a,b,c){}
const ceJoin = ceKeys.join(',');
// 将用户自定义的表达式按key的顺序放到数组中，可以使用 apply 传递给方法直接调用
const $CE$ = ceKeys.map((key) => CustomExpression[key]);

/** 普通规则表达式 #{...} */
const normalRegExp = /#{([^}]+)?}/g;
/** 用户自定义规则表达式 {{...}} */
const customRegExp = /{{([^}]+)?}}/g;
/** 填值规则表达式 ${...} */
const fillRuleRegExp = /\${([^}]+)?}/g;

/** action 类型 */
export const ACTION_TYPES = { ADD: 'add', EDIT: 'edit', DETAIL: 'detail', RELOAD: 'reload' };

/**
 * 将主表/一对一子表 默认值配置信息暂存
 * @param field
 * @param item
 * @param config
 */
export function initDefValueConfig(field, item, config) {
  if (hasEffectiveValue(item.defVal)) {
    const obj = { field: field, type: item.type, value: item.defVal, view: item.view, fieldExtendJson: item.fieldExtendJson }
    // 避免重复添加
    const index = config.findIndex((c) => c.field === field);
    if (index === -1) {
      config.push(obj);
    } else {
      config[index] = obj;
    }
  }
}

/**
 * 将一对多子表 默认值配置信息暂存
 * @param item
 * @param config
 */
export function initSubTableDefValueConfig(item, config) {
  if (hasEffectiveValue(item.fieldDefaultValue)) {
    config.push({ field: item.key, type: item.type, value: item.fieldDefaultValue });
  }
}

/**
 * 加载form组件默认值-仅用于新增页面
 * @param properties 字段配置
 * @param callback 回调传值
 * @param formData 表单值 
 */
export async function loadFormFieldsDefVal(properties, callback, formData?) {
  if (Array.isArray(properties) && properties.length > 0) {
    let formValues = {};
    for (let prop of properties) {
      let { value, type, field } = prop;
      value = await handleDefaultValue(value, ACTION_TYPES.ADD, formData||{});
      // 处理数字类型，如果type=number并且value有值
      if ('number' === type && value) {
        // parseFloat() 可以直接处理字符串、整数、小数、null和undefined，
        // 非数字类型直接返回NaN，不必担心报错
        value = Number.parseFloat(value);
      }
      // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-321】日期组件(date)中设置了年，年月，年周，年季度等格式的默认值需要转化成YYYY-MM-DD
      value = transformDefValDate(prop, value);
      // update-end--author:liaozhiyang---date:20240517---for：【TV360X-321】日期组件(date)中设置了年，年月，年周，年季度等格式的默认值需要转化成YYYY-MM-DD
      formValues[field] = value;
    }
    callback(formValues);
  }
}

/**
 * 2024-05-22
 * liaozhiyang
 * 日期组件(date)中设置了年，年月，年周，年季度等格式的默认值需要转化成YYYY-MM-DD
 */
function transformDefValDate(prop, value, mode?) {
  const { type, field, view, fieldExtendJson } = prop;
  if (view == 'date' && fieldExtendJson) {
    const extendJson = JSON.parse(fieldExtendJson);
    const { picker } = extendJson;
    if (picker && picker != 'default' && value) {
      // 范围查询默认值，形如 "2023,2024" / "2023-01,2023-12" / "2023-01-01,2023-12-31"
      if (mode === 'group' && typeof value === 'string' && value.indexOf(',') !== -1) {
        return value
          .split(',')
          .map((v) => transformPickerValue(picker, v))
          .join(',');
      }
      return transformPickerValue(picker, value);
    }
    return value;
  }
  return value;
}

/**
 * 把年 / 月 / 周 / 季度格式的字符串转换为 YYYY-MM-DD
 * @param picker year | month | week | quarter
 * @param value  例如 "2023" / "2023-07" / "2023-19周" / "2024-Q4"
 */
function transformPickerValue(picker, value) {
  try {
    if (picker === 'year') {
      // 年 (2023)
      const data = value.split('-');
      const y = data[0];
      return dayjs().year(y).format('YYYY-MM-DD');
    }
    if (picker === 'month') {
      // 年 - 月 (2023-07)，dayjs.month 是 0 索引，7 月对应索引 6
      const data = value.split('-');
      const y = data[0];
      const m = +data[1] - 1;
      return dayjs().year(y).month(m).format('YYYY-MM-DD');
    }
    if (picker === 'week') {
      // 年 - 周 (2023-19周)，兼容无"周"后缀
      const data = value.split('-');
      const y = data[0];
      const matchResult = data[1].match(/^(\d+)周?$/);
      const w = matchResult ? matchResult[1] : data[1];
      return dayjs().year(y).week(w).format('YYYY-MM-DD');
    }
    if (picker === 'quarter') {
      // 年 - 季度 (2024-Q4)
      const data = value.split('-');
      const y = data[0];
      const matchResult = data[1].match(/^[Qq](\d)$/);
      const q = matchResult ? matchResult[1] : data[1];
      return dayjs().year(y).quarter(q).format('YYYY-MM-DD');
    }
  } catch (error) {
    return value;
  }
  return value;
}

export async function loadOneFieldDefVal(field, item, formValues) {
  let { defVal, type } = item;
  if (hasEffectiveValue(defVal)) {
    let value = await handleDefaultValue(defVal, ACTION_TYPES.ADD, {});
    if ('number' === type && value) {
      // update-begin--author:liaozhiyang---date:20240618---for：online普通查询默认值范围查询不好使
      if (item.mode == 'group' && typeof value === 'string' && value.indexOf(',') != -1) {
        const arr = value.split(',');
        value = [];
        if (arr[0]) {
          value.push(Number.parseFloat(arr[0]));
        }
        if (arr[1]) {
          value.push(Number.parseFloat(arr[1]));
        }
      } else {
        value = Number.parseFloat(value);
      }
      // update-end--author:liaozhiyang---date:20240618---for：online普通查询默认值范围查询不好使
    }
    value = transformDefValDate(item, value, item.mode);
    formValues[field] = value;
  }
}

/**
 * 判断给定的值是不是有效的
 */
function hasEffectiveValue(val) {
  if (val || val === 0) {
    return true;
  }
  return false;
}

/** 加载JEditableTable组件默认值 */
export function loadFieldDefValForSubTable({ subForms, subTable, row, action, getFormData }) {
  if (subTable && Array.isArray(subTable.columns) && subTable.columns.length > 0) {
    subTable.columns.forEach(async (column) => {
      let { key, fieldDefaultValue: defVal } = column;
      eachHandler(
        defVal,
        action,
        (value) => {
          if (subForms.form) {
            subForms.form.setFieldsValue({ [key]: value });
          } else {
            // update-begin---author:sunjianlei  Date:20200725 for：online功能测试，行操作切换成新的行编辑-----------
            let v = [{ rowKey: row.id, values: { [key]: value } }];
            (subForms.jvt || subForms.jet).setValues(v);
            // update-end---author:sunjianlei    Date:20200725 for：online功能测试，行操作切换成新的行编辑------------
          }
        },
        getFormData
      );
    });
  }
}

async function eachHandler(defVal, action, callback, getFormData) {
  if (defVal != null) {
    // 检查类型，如果类型错误则不继续运行
    if (checkExpressionType(defVal)) {
      let value = await getDefaultValue(defVal, action, getFormData);
      if (value != null) {
        callback(value);
        return value;
      }
    } else {
      // 不合法的表达式直接返回不解析
      callback(defVal);
    }
  }
}

/**
 * 处理默认值
 * @param defVal
 * @param action
 * @param getFormData
 */
async function handleDefaultValue(defVal, action, getFormData) {
  if (defVal != null) {
    // 检查类型，如果类型错误则不继续运行
    if (checkExpressionType(defVal)) {
      let value = await getDefaultValue(defVal, action, getFormData);
      if (value != null) {
        return value;
      }
    }
  }
  return defVal;
}

/**
 * 检查表达式类型是否合法，规则：
 * 1、填值规则表达式不能和其他表达式混用
 * 2、每次只能填写一个填值规则表达式
 * 3、普通表达式和用户自定义表达式可以混用
 */
export function checkExpressionType(defVal) {
  // 获取各个表达式的数量
  let normalCount = 0,
    customCount = 0,
    fillRuleCount = 0;
  defVal.replace(fillRuleRegExp, () => fillRuleCount++);
  if (fillRuleCount > 1) {
    logWarn(`表达式[${defVal}]不合法：只能同时填写一个填值规则表达式！`);
    return false;
  }
  defVal.replace(normalRegExp, () => normalCount++);
  defVal.replace(customRegExp, () => customCount++);
  // 除填值规则外其他规则的数量
  let fillRuleOtherCount = normalCount + customCount;
  if (fillRuleCount > 0 && fillRuleOtherCount > 0) {
    logWarn(`表达式[${defVal}]不合法：填值规则表达式不能和其他表达式混用！`);
    return false;
  }
  return true;
}

/** 获取所有匹配的表达式 */
function getRegExpMap(text, exp) {
  let map = new Map();
  text.replace(exp, function (match, param) {
    map.set(match, param.trim());
    return match;
  });
  return map;
}

/** 获取默认值，可以执行表达式，可以执行用户自定义方法，可以异步获取用户信息等 */
async function getDefaultValue(defVal, action, getFormData) {
  // 只有在 add 和 reload 模式下才执行填值规则
  if (action === ACTION_TYPES.ADD || action === ACTION_TYPES.RELOAD) {
    // 判断是否是填值规则表达式，如果是就执行填值规则
    if (fillRuleRegExp.test(defVal)) {
      let arr: any[] = [getFormData];
      return await executeRegExp(defVal, fillRuleRegExp, executeFillRuleExpression, arr);
    }
  }
  // 只有在 add 模式下才执行其他表达式
  if (action === ACTION_TYPES.ADD) {
    // 获取并替换所有常规表达式
    defVal = await executeRegExp(defVal, normalRegExp, executeNormalExpression);
    // 获取并替换所有用户自定义表达式
    defVal = await executeRegExp(defVal, customRegExp, executeCustomExpression);
    return defVal;
  }
  return null;
}

async function executeRegExp(defVal, regExp, execFun, otherParams: any[] = []) {
  let map = getRegExpMap(defVal, regExp);
  for (let origin of map.keys()) {
    let exp = map.get(origin);
    let result = await execFun.apply(null, [exp, origin, ...otherParams]);
    // 如果只有一个表达式，那么就不替换（因为一旦替换，类型就会被转成String），直接返回执行结果，保证返回的类型不变
    if (origin === defVal) {
      return result;
    }
    defVal = replaceAll(defVal, origin, result);
  }
  return defVal;
}

/** 执行【普通表达式】#{xxx} */
async function executeNormalExpression(expression, origin) {
  switch (expression) {
    case 'date':
      return dayjs().format('YYYY-MM-DD');
    case 'time':
      return dayjs().format('HH:mm:ss');
    case 'datetime':
      return dayjs().format('YYYY-MM-DD HH:mm:ss');
    default:
      // 获取当前登录用户的信息
      let result = getUserInfoByExpression(expression);
      if (result != null) {
        return result;
      }
      // 没有符合条件的表达式，返回原始值
      return origin;
  }
}

/** 根据表达式获取相应的用户信息 */
function getUserInfoByExpression(expression) {
  const userStore = useUserStore();
  let userInfo = userStore.getUserInfo;
  if (userInfo) {
    switch (expression) {
      case 'sysUserId':
        return userInfo.id;
      // 当前登录用户登录账号
      case 'sysUserCode':
      case 'sys_user_code':
        return userInfo.username;
      // 当前登录用户真实名称
      case 'sysUserName':
        return userInfo.realname;
      // 当前登录用户部门编号
      case 'sysOrgCode':
      case 'sys_org_code':
        return userInfo.orgCode;
    }
  }
  return null;
}

/** 执行【用户自定义表达式】 {{xxx}} */
async function executeCustomExpression(expression, origin) {
  // update-begin--author:liaozhiyang---date:20230904---for：【QQYUN-6390】eval替换成new Function，解决build警告
  // 利用 eval 生成一个方法，这个方法的参数就是用户自定义的所有的表达式
  let fn = _eval(`(function (${ceJoin}){ return ${expression} })`);
  // update-end--author:liaozhiyang---date:20230904---for：【QQYUN-6390】eval替换成new Function，解决build警告
  try {
    // 然后调用这个方法，并把表达式传递进去，从而完成表达式的执行
    return fn.apply(null, $CE$);
  } catch (e) {
    // 执行失败，输出错误并返回原始值
    logError(e);
    return origin;
  }
}

/** 执行【填值规则表达式】 ${xxx} */
async function executeFillRuleExpression(expression, origin, getFormData) {
  let formData = {};
  if (typeof getFormData === 'function') {
    formData = getFormData();
  }else if(getFormData){
    formData = {...getFormData}
  }
  // 解析 url 参数
  expression = handleFillRuleQueryString(expression).exp;
  let url = `/sys/fillRule/executeRuleByCode/${expression}`;
  let { success, message, result } = await defHttp.put({ url, params: formData }, { isTransformResponse: false });
  if (success) {
    return result;
  } else {
    logError(`填值规则（${expression}）执行失败：${message}`);
    return origin;
  }
}

// 处理填值规则 queryString 参数
export function handleFillRuleQueryString(expression: string) {
  let arr = expression.split('?');
  if (arr.length > 1) {
    let queryString = '';
    let watchFields: string[] = [];
    let str = arr[1];
    let pairs = str.split('&');
    pairs.forEach((pair, idx) => {
      let [key, value] = pair.split('=');
      value = value.trim();
      // 取出监听的字段，多个用逗号分隔
      if (key === 'onl_watch') {
        watchFields = value.split(',');
      } else {
        queryString += `${key}=${value}`;
        if (idx < pairs.length - 1) {
          queryString += '&';
        }
      }
    });
    return {
      exp: arr[0] + (queryString === '' ? '' : ('?' + queryString)),
      watchFields: watchFields,
    };
  }
  return {exp: expression, watchFields: []};
}

export function handleFillRuleWatchKeysMap(properties: Recordable[]) {
  const watchKeyMap = new Map<string, string[]>();
  if (Array.isArray(properties) && properties.length > 0) {
    for (let prop of properties) {
      let {value: defVal, field} = prop;
      if (defVal == null || defVal == '') {
        continue;
      }
      // 检查类型，如果类型错误则不继续运行
      if (!checkExpressionType(defVal)) {
        continue;
      }
      // 判断是否是填值规则，如果是就解析填值规则
      if (fillRuleRegExp.test(defVal)) {
        let map = getRegExpMap(defVal, fillRuleRegExp);
        for (let origin of map.keys()) {
          let exp = map.get(origin);
          const {watchFields} = handleFillRuleQueryString(exp);
          for (const watchField of watchFields) {
            let arr = watchKeyMap.get(watchField)
            if (!Array.isArray(arr)) {
              arr = []
              watchKeyMap.set(watchField, arr)
            }
            if (arr.includes(field)) {
              continue;
            }
            arr.push(field);
          }
        }
      }
    }
  }
  return watchKeyMap;
}

function logWarn(message) {
  console.warn('[loadFieldDefVal]:', message);
}

function logError(message) {
  console.error('[loadFieldDefVal]:', message);
}
