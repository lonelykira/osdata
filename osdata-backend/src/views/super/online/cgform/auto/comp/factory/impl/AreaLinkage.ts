import IFormSchema from '../IFormSchema';
import { FormSchema } from '/@/components/Form';

/**
 * 省市区
 */
export default class PcaWidget extends IFormSchema {
  getItem(): FormSchema {
    let item = super.getItem();
    // update-begin--author:liaozhiyang---date:20260204---for:【QQYUN-14694】online支持配置独立的省、市、县
    const extendData: any = this.getExtendData();
    const componentProps: any = {}
    if (extendData.displayLevel) {
      componentProps.displayLevel = extendData.displayLevel;
      componentProps.saveCode = extendData.displayLevel === 'all' ? 'region' : componentProps.displayLevel;
    }
    // update-end--author:liaozhiyang---date:20260204---for:【QQYUN-14694】online支持配置独立的省、市、县
    return Object.assign({}, item, {
      component: 'JAreaLinkage',
      componentProps: {
        saveCode: 'region',
        ...componentProps,
      },
    });
  }
}
