import { dateUtil } from '/@/utils/dateUtil';
import { duplicateCheck } from '/@/views/system/user/user.api';
import { defHttp } from '/@/utils/http/axios';

export const rules = {
  rule(type, required) {
    if (type === 'email') {
      return this.email(required);
    }
    if (type === 'phone') {
      return this.phone(required);
    }
  },
  email(required) {
    return [
      {
        required: required ? required : false,
        validator: async (_rule, value) => {
          if (required == true && !value) {
            return Promise.reject('请输入邮箱!');
          }
          if (
            value &&
            !new RegExp(
              /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
            ).test(value)
          ) {
            return Promise.reject('请输入正确邮箱格式!');
          }
          return Promise.resolve();
        },
        trigger: 'change',
      },
    ] as ArrayRule;
  },
  phone(required) {
    return [
      {
        required: required,
        validator: async (_, value) => {
          if (required && !value) {
            return Promise.reject('请输入手机号码!');
          }
          if (!/^1[3456789]\d{9}$/.test(value)) {
            return Promise.reject('手机号码格式有误');
          }
          return Promise.resolve();
        },
        trigger: 'change',
      },
    ];
  },
  startTime(endTime, required) {
    return [
      {
        required: required ? required : false,
        validator: (_, value) => {
          if (required && !value) {
            return Promise.reject('请选择开始时间');
          }
          if (endTime && value && dateUtil(endTime).isBefore(value)) {
            return Promise.reject('开始时间需小于结束时间');
          }
          return Promise.resolve();
        },
        trigger: 'change',
      },
    ];
  },
  endTime(startTime, required) {
    return [
      {
        required: required ? required : false,
        validator: (_, value) => {
          if (required && !value) {
            return Promise.reject('请选择结束时间');
          }
          if (startTime && value && dateUtil(value).isBefore(startTime)) {
            return Promise.reject('结束时间需大于开始时间');
          }
          return Promise.resolve();
        },
        trigger: 'change',
      },
    ];
  },
  confirmPassword(values, required) {
    return [
      {
        required: required ? required : false,
        validator: (_, value) => {
          if (!value) {
            return Promise.reject('密码不能为空');
          }
          if (value !== values.password) {
            return Promise.reject('两次输入的密码不一致!');
          }
          return Promise.resolve();
        },
      },
    ];
  },
  duplicateCheckRule(tableName, fieldName, model, schema, required?) {
    return [
      {
        validator: (_, value) => {
          if (!value && required) {
            return Promise.reject(`请输入${schema.label}`);
          }
          return new Promise<void>((resolve, reject) => {
            duplicateCheck({
              tableName,
              fieldName,
              fieldVal: value,
              dataId: model.id,
            })
              .then((res) => {
                res.success ? resolve() : reject(res.message || '校验失败');
              })
              .catch((err) => {
                reject(err.message || '验证失败');
              });
          });
        },
      },
    ] as ArrayRule;
  },
};

//update-begin---author:wangshuai ---date:2026-06-29  for：【QQYUN-16619】三级等保密码强度校验工具函数-----------
/**
 * 判断密码是否存在3位及以上连续递增的数字或字母
 */
function hasConsecutiveChars(password: string): boolean {
  const arr = password.split('');
  for (let i = 1; i < arr.length - 1; i++) {
    const first = arr[i - 1].charCodeAt(0);
    const second = arr[i].charCodeAt(0);
    const third = arr[i + 1].charCodeAt(0);
    if ((third - second === 1) && (second - first === 1)) {
      return true;
    }
  }
  return false;
}

/**
 * 简单密码校验：8位及以上，包含字母+数字+特殊符号
 */
export function validateSimplePassword(value: string, oldPassword?: string) {
  if (!value) {
    return Promise.resolve();
  }
  if (oldPassword && value === oldPassword) {
    return Promise.reject('不能使用系统密码作为新密码!');
  }
  const reg = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[~!@#$%^&*()_+`\-={}:";'<>?,./]).{8,}$/;
  if (!reg.test(value)) {
    return Promise.reject('密码由 8 位及以上数字、大小写字母和特殊符号组成！');
  }
  return Promise.resolve();
}

/**
 * 三级等保强密码校验：8位及以上，必须包含数字+大写+小写+特殊符号，不允许3位连续/重复字符
 */
export function validateStrongPassword(value: string, oldPassword?: string) {
  if (!value) {
    return Promise.resolve();
  }
  if (oldPassword && value === oldPassword) {
    return Promise.reject('不能使用系统密码作为新密码!');
  }
  if (value.length < 8) {
    return Promise.reject('密码长度不能少于8位');
  }
  const hasNumber = /[0-9]/.test(value);
  const hasLowercase = /[a-z]/.test(value);
  const hasUppercase = /[A-Z]/.test(value);
  const hasSpecialChar = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~`]/.test(value);
  if (!(hasNumber && hasLowercase && hasUppercase && hasSpecialChar)) {
    return Promise.reject('密码必须包含数字、大小写字母和特殊字符');
  }
  if (hasConsecutiveChars(value)) {
    return Promise.reject('密码不能出现3位及以上的连续数字或字母');
  }
  if (/(.)\1{2,}/.test(value)) {
    return Promise.reject('密码不能出现相同字符连续3位或以上');
  }
  return Promise.resolve();
}

/**
 * 缓存三级等保密码开关值（只从 API 取一次）
 */
let _enableStrongPwdCache: boolean | null = null;

async function fetchEnableStrongPwd(): Promise<boolean> {
  if (_enableStrongPwdCache !== null) {
    return _enableStrongPwdCache;
  }
  try {
    const data = await defHttp.get({ url: '/sys/user/getUserInfo' });
    _enableStrongPwdCache = data?.enableStrongPwd ?? false;
  } catch {
    _enableStrongPwdCache = false;
  }
  return _enableStrongPwdCache;
}

/**
 * 根据三级等保开关创建密码校验 validator（从 API 获取配置并缓存）
 * @param oldPassword 旧密码（可选，用于禁止与旧密码相同）
 */
export function createPasswordValidator(oldPassword?: string) {
  return async (_: any, value: string) => {
    const enableStrongPwd = await fetchEnableStrongPwd();
    if (enableStrongPwd) {
      return validateStrongPassword(value, oldPassword);
    }
    return validateSimplePassword(value, oldPassword);
  };
}
//update-end---author:wangshuai ---date:2026-06-29  for：【QQYUN-16619】三级等保密码强度校验工具函数-----------

/**
 * 唯一校验函数，给原生<a-form>使用，vben的表单校验建议使用上述rules
 * @param tableName 表名
 * @param fieldName 字段名
 * @param fieldVal 字段值
 * @param dataId 数据ID
 */
export async function duplicateValidate(tableName, fieldName, fieldVal, dataId) {
  try {
    let params = {
      tableName,
      fieldName,
      fieldVal,
      dataId: dataId,
    };
    const res = await duplicateCheck(params);
    if (res.success) {
      return Promise.resolve();
    } else {
      return Promise.reject(res.message || '校验失败');
    }
  } catch (e) {
    return Promise.reject('校验失败,可能是断网等问题导致的校验失败');
  }
}
