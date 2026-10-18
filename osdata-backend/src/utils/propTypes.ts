import { CSSProperties, VNodeChild } from 'vue';
import { createTypes, VueTypeValidableDef, VueTypesInterface, toValidableType } from 'vue-types';

export type VueNode = VNodeChild | JSX.Element;

type PropTypes = VueTypesInterface & {
  readonly style: VueTypeValidableDef<CSSProperties>;
  readonly VNodeChild: VueTypeValidableDef<VueNode>;
  // readonly trueBool: VueTypeValidableDef<boolean>;
};
const newPropTypes = createTypes({
  func: undefined,
  bool: undefined,
  string: undefined,
  number: undefined,
  object: undefined,
  integer: undefined,
}) as PropTypes;

// 从 vue-types v5.0 开始，extend()方法已经废弃，当前已改为官方推荐的ES6+方法 https://dwightjack.github.io/vue-types/advanced/extending-vue-types.html#the-extend-method
class _propTypes extends newPropTypes {
  // a native-like validator that supports the `.validable` method
  static get style() {
    return toValidableType('style', {
      type: [String, Object],
    });
  }

  static get VNodeChild() {
    return toValidableType('VNodeChild', {
      type: undefined,
    });
  }
}

//update-begin-author:liaozhiyang date:2026-05-12 for: 修复 class extends vue-types 实例后，TS 看不到 .string/.bool/.oneOfType 等静态成员导致大面积 "Property xxx does not exist on type 'typeof propTypes'" 报错——在导出处把类型断言成「类自身 ∪ PropTypes 静态视图」
const propTypes = _propTypes as unknown as typeof _propTypes & PropTypes;
export { propTypes };
//update-end-author:liaozhiyang date:2026-05-12
