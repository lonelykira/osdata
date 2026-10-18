import type { JVxeVueComponent } from './types';
import { JVxeTypes } from './types/JVxeTypes';

/** 仅存 componentMap 与 clearComponent，供 qiankun unmount 等场景使用，不引用任何 Cell 组件 */
let componentMap = new Map<JVxeTypes | string, JVxeVueComponent>();
const JVxeComponents = 'JVxeComponents__';
if (import.meta.env.DEV && componentMap.size === 0 && window[JVxeComponents] && window[JVxeComponents].size > 0) {
  componentMap = window[JVxeComponents];
}

export { componentMap };

/**
 * 清空注册的组件（乾坤子应用 unmount 时调用，仅引用本文件避免加载所有 Cell）
 */
export function clearComponent() {
  componentMap.clear();
  // 代码逻辑说明: 【issues/860】生成的一对多代码，热更新之后点击新增卡死[暂时先解决]
  import.meta.env.DEV && (window[JVxeComponents] = componentMap);
}
