import { VxeTablePropTypes } from 'vxe-table';
import { isArray } from '/@/utils/is';
import { HandleArgs } from './useColumns';
import { replaceProps } from '../utils/enhancedUtils';

export function useValidateRules(args: HandleArgs) {
  const { data } = args;
  const col = args.col!;
  let rules: VxeTablePropTypes.EditRules[] = [];
  if (isArray(col.validateRules)) {
    for (let rule of col.validateRules) {
      let replace = {
        message: replaceProps(col, rule.message),
      };
      if (rule.unique || rule.pattern === 'only') {
        // 唯一校验器
        rule.validator = uniqueValidator(args);
      } else if (rule.pattern) {
        // 非空
        if (rule.pattern === fooPatterns[0].value) {
          rule.required = true;
          delete rule.pattern;
        } else {
          // 兼容Online表单的特殊规则
          let reg: any = rule.pattern;
          for (let foo of fooPatterns) {
            if (foo.value === rule.pattern) {
              reg = foo.pattern;
              break;
            }
          }
          // update-begin--author:liaozhiyang---date:20260714---for：【LHZP-172】行编辑pattern校验空值时不应触发，仅有值时校验（非空由required规则负责）
          const regExp = reg instanceof RegExp ? reg : new RegExp(reg);
          delete rule.pattern;
          rule.validator = emptySkipPatternValidator(regExp);
          // update-end--author:liaozhiyang---date:20260714---for：【LHZP-172】行编辑pattern校验空值时不应触发，仅有值时校验（非空由required规则负责）
        }
      } else if (typeof rule.handler === 'function') {
        // 自定义函数校验
        rule.validator = handlerConvertToValidator;
      }
      rules.push(Object.assign({}, rule, replace));
    }
  }
  data.innerEditRules[col.key] = rules;
}

/** 唯一校验器 */
function uniqueValidator({ methods }: HandleArgs) {
  return function (event) {
    const { cellValue, column, rule } = event;
    // 代码逻辑说明: 【TV360X-299】JVxetable组件中唯一校验过滤掉空字符串
    if (cellValue == '') return Promise.resolve();
    let tableData = methods.getTableData();
    let findCount = 0;
    for (let rowData of tableData) {
      if (rowData[column.params.key] === cellValue) {
        if (++findCount >= 2) {
          return Promise.reject(new Error(rule.message));
        }
      }
    }
    return Promise.resolve();
  };
}

/** 旧版handler转为新版Validator */
function handlerConvertToValidator(event) {
  const { column, rule } = event;
  return new Promise((resolve, reject) => {
    rule.handler(event, (flag, msg) => {
      let message = rule.message;
      if (typeof msg === 'string') {
        message = replaceProps(column.params, msg);
      }
      if (flag == null) {
        resolve(message);
      } else if (!!flag) {
        resolve(message);
      } else {
        reject(new Error(message));
      }
    });
  });
}

// update-begin--author:liaozhiyang---date:20260714---for：【LHZP-172】行编辑pattern校验空值时不应触发，仅有值时校验（非空由required规则负责）
/** pattern 校验器：空值不校验（非空交给 required 规则），仅当有值时做正则校验 */
function emptySkipPatternValidator(regExp: RegExp) {
  return function (event) {
    const { cellValue, rule } = event;
    if (cellValue === '' || cellValue === null || cellValue === undefined) {
      return Promise.resolve();
    }
    return regExp.test(String(cellValue)) ? Promise.resolve() : Promise.reject(new Error(rule.message));
  };
}
// update-end--author:liaozhiyang---date:20260714---for：【LHZP-172】行编辑pattern校验空值时不应触发，仅有值时校验（非空由required规则负责）

// 兼容 online 的规则
const fooPatterns = [
  { title: '非空', value: '*', pattern: /^.+$/ },
  { title: '6到16位数字', value: 'n6-16', pattern: /^\d{6,16}$/ },
  { title: '6到16位任意字符', value: '*6-16', pattern: /^.{6,16}$/ },
  { title: '6到18位字母', value: 's6-18', pattern: /^[a-z|A-Z]{6,18}$/ },
  // 代码逻辑说明: VUEN-1160 对多子表，网址校验不正确
  {
    title: '网址',
    value: 'url',
    pattern: /^((ht|f)tps?):\/\/[\w\-]+(\.[\w\-]+)+([\w\-.,@?^=%&:\/~+#]*[\w\-@?^=%&\/~+#])?$/,
  },
  // 代码逻辑说明: 【TV360X-466】邮箱跟一对第一校验规则一致
  { title: '电子邮件', value: 'e', pattern: /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/ },
  { title: '手机号码', value: 'm', pattern: /^1[3456789]\d{9}$/ },
  { title: '邮政编码', value: 'p', pattern: /^\d{6}$/ },
  { title: '字母', value: 's', pattern: /^[A-Z|a-z]+$/ },
  { title: '数字', value: 'n', pattern: /^-?\d+(\.?\d+|\d?)$/ },
  { title: '整数', value: 'z', pattern: /^-?\d+$/ },
  {
    title: '金额',
    value: 'money',
    pattern: /^(([1-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,5}))$/,
  },
];
