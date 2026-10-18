import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/** 表单设计器联动组件高级查询。 */
export default class CategoryLinkageForQueryWidget extends IFormSchema {
  getItem(): FormSchema {
    const item = super.getItem();
    return Object.assign({}, item, {
      component: 'JCategoryLinkage',
      componentProps: {
        category: this._data.category,
        placeholder: '请选择…',
        style: { width: '100%' },
      },
    });
  }
}
