/**
 * unplugin-icons 类型声明
 * 为 ~icons 路径提供类型支持
 */

declare module '~icons/*' {
  import { FunctionalComponent, SVGAttributes } from 'vue';
  const component: FunctionalComponent<SVGAttributes>;
  export default component;
}
