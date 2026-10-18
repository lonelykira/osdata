import { onMounted, onUnmounted, nextTick, watch } from 'vue';
import { JVxeTableMethods, JVxeTableProps } from '/@/components/jeecg/JVxeTable/src/types';
import Sortable from 'sortablejs';
import { isEnabledVirtualYScroll } from '/@/components/jeecg/JVxeTable/utils';

export function useDragSort(props: JVxeTableProps, methods: JVxeTableMethods) {
  if (props.dragSort) {
    let sortable2: Sortable;
    let initTime: any;

    onMounted(() => {
      // 加载完成之后再绑定拖动事件
      initTime = setTimeout(createSortable, 300);
    });

    onUnmounted(() => {
      clearTimeout(initTime);
      if (sortable2) {
        sortable2.destroy();
      }
    });
    // update-begin--author:liaozhiyang---date:20260415---for:【QQYUN-15134】修复jvxetable使用fixed固定后无法拖拽
    // 代码逻辑说明：监听 maxHeight 变化（弹窗全屏↔缩小切换时触发）。
    // 解决：maxHeight 变化时销毁旧 Sortable 并重新初始化，让其绑定到正确的 tbody。
    watch(
      () => props.maxHeight,
      () => {
        if (sortable2) {
          sortable2.destroy();
          sortable2 = null as any;
        }
        clearTimeout(initTime);
        initTime = setTimeout(createSortable, 300);
      }
    );

    function createSortable() {
      let xTable = methods.getXTable();
      // 代码逻辑说明：拖拽排序列默认 fixed:left，此时 drag-btn 在固定列 wrapper 的 tbody 内。
      // 若 dragSortFixed!='none'，优先绑定固定列 wrapper 的 tbody，确保 Sortable 能捕获拖拽事件；
      // 若 dragSortFixed='none'（拖拽列不固定），则绑定主 tbody。
      const domFixed =
        props.dragSortFixed !== 'none'
          ? xTable.$el.querySelector('.vxe-table--fixed-left-wrapper .vxe-table--body tbody')
          : null;
      const domMain = xTable.$el.querySelector('.vxe-table--body-inner-wrapper > .vxe-table--body tbody');
      const dom = domFixed || domMain;
      if (!dom) {
        console.warn('[JVxeTable] 拖拽排序初始化失败，可能是vxe-table升级导致的版本不兼容。');
        return;
      }

      // 拖拽过程中悬停目标行的 DOM 索引（由 onMove 实时更新）
      let hoverIndex = -1;
      // 拖拽起始行的 DOM 索引（用于 onMove 中判断是否悬停自身）
      let dragStartIndex = -1;

      /**
       * 为所有 tbody 中第 idx 行（0-based）添加或移除 CSS class。
       * 用于跨主体 + 固定列 wrapper 同步视觉状态，避免只改一侧 tbody 导致样式不一致。
       */
      function setRowClass(idx: number, cls: string, add: boolean) {
        xTable.$el.querySelectorAll(`.vxe-table--body tbody tr:nth-child(${idx + 1})`).forEach((tr) => {
          (tr as HTMLElement).classList[add ? 'add' : 'remove'](cls);
        });
      }
      /** 清除所有带有指定 class 的行 */
      function clearRowClass(cls: string) {
        xTable.$el.querySelectorAll(`.${cls}`).forEach((tr) => {
          (tr as HTMLElement).classList.remove(cls);
        });
      }

      sortable2 = Sortable.create(dom as HTMLElement, {
        handle: '.drag-btn',
        // 代码逻辑说明: 【QQYUN-8785】online表单列位置的id未做限制，拖动其他列到id列上面，同步数据库时报错
        filter: '.not-allow-drag',
        draggable: '.allow-drag',
        direction: 'vertical',
        animation: 0,
        onStart(e) {
          // 初始化悬停索引为起始位置，并为被拖起的行添加禁用效果
          hoverIndex = e.oldIndex!;
          dragStartIndex = e.oldIndex!;
          setRowClass(e.oldIndex!, 'j-vxe-drag-source', true);
        },
        onMove(e) {
          // 代码逻辑说明：拖拽期间只记录悬停目标的 DOM 索引，阻止 Sortable 实时交换行位置。
          // 好处：所有 tbody（主体 + 固定列 wrapper）在松手之前均保持原顺序，不会产生视觉错位；
          // 松开鼠标后统一由 vxe-table 数据驱动重渲染，fixed 列拖拽排序问题彻底解决。
          const idx = Array.from((e.from as HTMLElement).children).indexOf(e.related as HTMLElement);
          if (idx !== -1) {
            hoverIndex = idx;
          }
          // 更新悬停行指示线：先清除旧状态，悬停自身时不显示
          clearRowClass('j-vxe-drag-hover-top');
          clearRowClass('j-vxe-drag-hover-bottom');
          if (hoverIndex !== dragStartIndex) {
            // 向下拖（插入悬停行下方）→ 底部线；向上拖（插入悬停行上方）→ 顶部线
            const cls = hoverIndex > dragStartIndex ? 'j-vxe-drag-hover-bottom' : 'j-vxe-drag-hover-top';
            setRowClass(hoverIndex, cls, true);
          }
          return false; // 阻止 Sortable 移动 DOM 行
        },
        onEnd(e: any) {
          // 拖拽结束，清除所有视觉状态
          clearRowClass('j-vxe-drag-source');
          clearRowClass('j-vxe-drag-hover-top');
          clearRowClass('j-vxe-drag-hover-bottom');
          // -update-begin--author:liaozhiyang---date:20240619---for：【TV360X-585】拖动字段虚拟滚动不好使
          const isRealEnabledVirtual = isEnabledVirtualYScroll(props, xTable);
          let newIndex: number;
          let oldIndex: number;

          if (isRealEnabledVirtual) {
            // 虚拟滚动：onMove 返回 false 后 DOM 行未动，e.item 就是被拖拽行本身
            const dragNode = e.item as HTMLElement;
            const dragRowInfo = xTable.getRowNode(dragNode);
            if (!dragRowInfo) return;
            oldIndex = dragRowInfo.index;
            if (hoverIndex === e.oldIndex) return;

            // 通过 hoverIndex 对应的可视区 DOM 节点获取实际数据索引
            const hoverNode = (e.from as HTMLElement).childNodes[hoverIndex] as HTMLElement;
            if (!hoverNode) return;
            const hoverRowInfo = xTable.getRowNode(hoverNode);
            if (!hoverRowInfo) return;
            newIndex = hoverRowInfo.index;
          } else {
            // 非虚拟滚动：DOM 行从未移动，直接使用 hoverIndex 作为目标索引，无需还原 DOM
            oldIndex = e.oldIndex;
            newIndex = hoverIndex;
            if (oldIndex === newIndex) return;
          }
          // -update-end--author:liaozhiyang---date:20240619---for：【TV360X-585】拖动字段虚拟滚动不好使

          nextTick(() => {
            methods.doSort(oldIndex, newIndex);
            methods.trigger('dragged', { oldIndex: oldIndex, newIndex: newIndex });
          });
        },
      });
    }
    // update-begin--author:liaozhiyang---date:20260415---for:【QQYUN-15134】修复jvxetable使用fixed固定后无法拖拽
  }
}
