
import { nextTick } from 'vue';

/***
 * 表单上下文
 */
export function useOnlineFormDetailContext() {
  const that = {};
  const CONTEXT_DESCRIPTION = {
    setFieldsValue: '<m> 设置表单控件的值',
    getFieldsValue: '<m> 获取表单控件的值',
    sh: '<p> 表单控件的显示隐藏状态',
    isUpdate: '<p> 判断是否为编辑模式',
    isDetail: '<p> 判断是否为详情模式',
  };
  const onlineFormDetailContext = new Proxy(CONTEXT_DESCRIPTION, {
    get(_target: any, prop: string): any {
      return Reflect.get(that, prop);
    },
  });

  function addObject2Context(prop, object) {
    that[prop] = object;
  }

  function resetContext(context) {
    Object.keys(context).map((k) => {
      that[k] = context[k];
    });
  }
  addObject2Context('$nextTick', nextTick);
  addObject2Context('addObject2Context', addObject2Context);
  return { onlineFormDetailContext, addObject2Context, resetContext };
}
