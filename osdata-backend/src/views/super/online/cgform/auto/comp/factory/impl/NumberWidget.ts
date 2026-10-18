import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * 输入框-数字
 */
export default class NumberWidget extends IFormSchema {
  dbPointLength: number;

  constructor(key, data) {
    super(key, data);
    this.dbPointLength = data.dbPointLength;
  }

  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    const safeIntRule = {
      validator: (_rule, value) => {
        if (value !== null && value !== undefined && value !== '') {
          if (value > Number.MAX_SAFE_INTEGER || value < Number.MIN_SAFE_INTEGER) {
            return Promise.reject(`数值超出安全范围（${Number.MIN_SAFE_INTEGER}~${Number.MAX_SAFE_INTEGER}），精度将丢失，请重新输入`);
          }
        }
        return Promise.resolve();
      },
    };
    const existingRules = item.rules || [];
    return Object.assign({}, item, {
      component: 'InputNumber',
      componentProps,
      // update-begin--author:liaozhiyang---date:20260413---for:【QQYUN-9790】online中数字类型超出js语言数值范围加提示
      rules: [...existingRules, safeIntRule],
      // update-end--author:liaozhiyang---date:20260413---for:【QQYUN-9790】online中数字类型超出js语言数值范围加提示
    });
  }

  getComponentProps() {
    const props = {
      style: {
        width: '100%',
      },
    };
    if (this.dbPointLength >= 0) {
      props['precision'] = this.dbPointLength;
    }
    if (this.onlyInteger) {
      props['precision'] = 0;
    }
    return props;
  }
}
