import { defineComponent, h, ref, useSlots, computed, resolveComponent } from 'vue';
import { vxeEmits, vxeProps } from './vxe.data';
import { useData, useRefs } from './hooks/useData';
import { useColumns } from './hooks/useColumns';
import { useColumnsCache } from './hooks/useColumnsCache';
import { useMethods } from './hooks/useMethods';
import { useDataSource } from './hooks/useDataSource';
import { useDragSort } from './hooks/useDragSort';
import { useRenderComponents } from './hooks/useRenderComponents';
import { useFinallyProps } from './hooks/useFinallyProps';
import { JVxeTableProps } from './types';
import { Spin } from 'ant-design-vue';
import './style/index.less';

export default defineComponent({
  name: 'JVxeTable',
  inheritAttrs: false,
  props: vxeProps(),
  emits: [...vxeEmits],
  setup(props: JVxeTableProps, context) {
    const instanceRef = ref();
    const refs = useRefs();
    const slots = useSlots();
    const data = useData(props);
    const { methods, publicMethods, created } = useMethods(props, context, data, refs, instanceRef);
    created();
    useColumns(props, data, methods, slots);
    useDataSource(props, data, methods, refs);
    useDragSort(props, methods);
    // 代码逻辑说明: 【QQYUN-8566】JVXETable无法记住列设置
    const { initSetting } = useColumnsCache({ cacheColumnsKey: props.cacheColumnsKey });
    initSetting(props);
    // 最终传入到 template 里的 props
    const finallyProps = useFinallyProps(props, data, methods);
    // 渲染子组件
    const renderComponents = useRenderComponents(props, data, methods, slots);
    // update-begin--author:liaozhiyang---date:20260316---for:【QQYUN-13751】jVxetable优化
    // 在 setup 阶段缓存组件引用，避免每次 render 调用 resolveComponent 查找
    const aSpinComp = Spin;
    const vxeGridComp = resolveComponent('vxe-grid');
    // 将 vxeProps 和 data 合并为一个 computed，避免 render 每次 spread 生成新对象
    const vxeGridProps = computed(() => ({
      ...finallyProps.vxeProps.value,
      data: data.vxeDataSource.value,
    }));
    // update-end--author:liaozhiyang---date:20260316---for:【QQYUN-13751】jVxetable优化
    return {
      instanceRef,
      ...refs,
      ...publicMethods,
      ...finallyProps,
      ...renderComponents,
      vxeDataSource: data.vxeDataSource,
      aSpinComp,
      vxeGridComp,
      vxeGridProps,
    };
  },
  render() {
    return h(
      'div',
      {
        class: this.$attrs.class,
        style: this.$attrs.style,
      },
      h(
        this.aSpinComp,
        {
          spinning: this.loading,
          wrapperClassName: this.prefixCls,
        },
        {
          default: () => [
            this.renderSubPopover(),
            this.renderToolbar(),
            this.renderToolbarAfterSlot(),
            h(
              this.vxeGridComp,
              this.vxeGridProps,
              this.$slots
            ),
            this.renderPagination(),
            this.renderDetailsModal(),
          ],
        }
      )
    );
  },
  created() {
    this.instanceRef = this;
  },
});
