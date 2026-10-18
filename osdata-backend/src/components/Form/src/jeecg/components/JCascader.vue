<!--
  级联选择器：只读输入框 + 挂载到 body 的下拉多列菜单
  选中叶子节点时同步 value 并关闭下拉；非叶子节点仅展开下一列
-->
<template>
  <div class="cascader" ref="rootRef">
    <AInput
      :value="displayText"
      :placeholder="inputPlaceholder"
      readonly
      :disabled="inputDisabled"
      v-bind="inputAttrs"
      :class="{ 'cascader-input-open': visible }"
      @click="toggle"
    >
      <template #suffix>
        <span v-if="displayText && !inputDisabled" class="ant-input-clear-icon ant-input-clear-icon-has-suffix"
          role="button" tabindex="-1" @click.stop="handleClear" @mousedown.prevent>
          <CloseCircleFilled />
        </span>
        <DownOutlined class="cascader-arrow" :class="{ 'is-open': visible }" />
      </template>
    </AInput>
    <!-- 下拉挂载到 body，用 fixed + 计算出的 left/top 定位 -->
    <Teleport to="body">
      <Transition name="cascader-dropdown">
        <div v-show="visible" ref="dropdownRef" class="cascader-dropdown cascader-dropdown-placement"
          :style="dropdownPlaceStyle">
          <div class="cascader-menus">
            <div v-for="(column, colIndex) in columns" :key="colIndex" class="cascader-menu">
              <div v-for="opt in column" :key="opt.value" class="cascader-menu-item"
                :class="{ 'is-active': isActive(opt, colIndex), 'is-selected': isSelected(opt, colIndex) }"
                @click="onSelectOption(opt, colIndex)">
                <span class="cascader-menu-item-content">{{ opt.label }}</span>
                <RightOutlined v-if="hasChildren(opt)" class="cascader-menu-item-arrow" />
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onBeforeUnmount, nextTick, unref } from 'vue';
import { Input as AInput } from 'ant-design-vue';
import { DownOutlined, RightOutlined, CloseCircleFilled } from '@ant-design/icons-vue';
import { useAttrs } from '/@/hooks/core/useAttrs';

/** 级联选项：value/label + 可选 children */
export interface CascaderOption {
  value: string | number;
  label: string;
  children?: CascaderOption[];
}

/** 选中路径：每级的 value 数组 */
type PathValue = (string | number)[];

const DISPLAY_SEPARATOR = ' / ';
const DROPDOWN_GAP = 4;

defineOptions({
  name: 'JCascader',
  inheritAttrs: false,
});

const props = withDefaults(
  defineProps<{
    /** 当前选中路径（每级 value 数组） */
    value?: PathValue;
    /** 级联树数据 */
    options?: CascaderOption[];
    /** 是否只在 input 中显示最后一级 */
    showLastLevelOnly?: boolean;
  }>(),
  {
    value: () => [],
    options: () => [],
    showLastLevelOnly: false,
  }
);

const emit = defineEmits<{
  (e: 'change', value: PathValue): void;
  (e: 'update:value', value: PathValue): void;
}>();

const attrs = useAttrs();
const inputPlaceholder = computed(() => String(unref(attrs as Record<string, unknown>).placeholder ?? '请选择'));
const inputDisabled = computed(() => Boolean(unref(attrs as Record<string, unknown>).disabled));
// update-begin--author:liaozhiyang---date:20260804---for：【LHZP-707】修复省市区禁用状态下还可以点击x删除
/** 排除 allowClear/disabled，清空按钮由 suffix 自定义实现 */
const inputAttrs = computed(() => {
  const { allowClear: _allowClear, disabled: _disabled, ...rest } = unref(attrs as Record<string, unknown>);
  return rest;
});
// update-end--author:liaozhiyang---date:20260804---for：【LHZP-707】修复省市区禁用状态下还可以点击x删除
const rootRef = ref<HTMLElement>();
const dropdownRef = ref<HTMLElement>();
/** 下拉是否展开 */
const visible = ref(false);
/** 展开时正在编辑的路径（未确认前）；用于多列菜单的当前高亮与子列数据 */
const editingPath = ref<PathValue>([]);
/** 下拉挂到 body 时的 fixed 定位：left、top（px 字符串） */
const dropdownPlaceStyle = ref<{ left: string; top: string }>({ left: '0', top: '0' });

/** 规范化 value 为路径数组 */
function normalizePath(val: PathValue | undefined): PathValue {
  return Array.isArray(val) ? val : [];
}

/** 是否有子节点（非叶子） */
function hasChildren(opt: CascaderOption): boolean {
  return !!(opt.children?.length);
}

/** 根据 value 路径在 options 树中逐级查找，返回对应的 label 数组 */
function getPathLabels(opts: CascaderOption[], pathValues: PathValue): string[] {
  const labels: string[] = [];
  let current = opts;
  for (const v of pathValues) {
    const opt = current.find((o) => o.value === v);
    if (!opt) break;
    labels.push(opt.label);
    current = opt.children ?? [];
  }
  return labels;
}

/** 输入框展示文案：showLastLevelOnly 时只显示最后一级 label，否则用分隔符拼接整条路径 */
const displayText = computed(() => {
  const path = normalizePath(props.value);
  if (!path.length) return '';
  const labels = getPathLabels(props.options ?? [], path);
  return props.showLastLevelOnly ? (labels.at(-1) ?? '') : labels.join(DISPLAY_SEPARATOR);
});

/** 用于菜单高亮与列数据：展开时用 editingPath，收起时用 props.value */
const pathForMenus = computed<PathValue>(() =>
  visible.value ? editingPath.value : normalizePath(props.value));

/** 根据当前 path 展开的列：第 0 列为根，第 k 列为 path[k-1] 的 children；列数 = path.length + 1，点击第几级只展示到下一级 */
const columns = computed(() => {
  const path = pathForMenus.value;
  const opts = props.options ?? [];
  const cols: CascaderOption[][] = [];
  let current: CascaderOption[] = opts;
  for (let i = 0; i <= path.length && current.length; i++) {
    cols.push(current);
    if (i < path.length && path[i] !== undefined) {
      const opt = current.find((o) => o.value === path[i]);
      current = opt?.children ?? [];
    } else {
      break;
    }
  }
  return cols;
});

/** 当前列中该项是否为“当前路径”在该列的节点（高亮并展示子列） */
function isActive(opt: CascaderOption, colIndex: number): boolean {
  const path = pathForMenus.value;
  return path[colIndex] === opt.value;
}

/** 当前列中该项是否为已选中的叶子节点（选中样式） */
function isSelected(opt: CascaderOption, colIndex: number): boolean {
  const path = pathForMenus.value;
  if (colIndex < path.length - 1) return path[colIndex] === opt.value;
  return path[colIndex] === opt.value && !hasChildren(opt);
}

/** 点击某一列选项：截断到该列并追加当前项；若为叶子则 emit 并关闭，否则更新 editingPath 展开下一列 */
function onSelectOption(opt: CascaderOption, colIndex: number) {
  const path = visible.value ? [...editingPath.value] : normalizePath(props.value).slice();
  path.length = colIndex;
  path.push(opt.value);
  if (hasChildren(opt)) {
    editingPath.value = path;
  } else {
    emit('change', path);
    emit('update:value', path);
    visible.value = false;
  }
}

function toggle() {
  if (inputDisabled.value) return;
  visible.value = !visible.value;
}

/** 清除选中：与 UI 库 readonly 下不显示清除按钮的补偿，在 suffix 中手动实现 */
function handleClear() {
  if (inputDisabled.value) return;
  emit('change', []);
  emit('update:value', []);
  visible.value = false;
}

/** 点击不在输入框、不在下拉内时关闭下拉（capture 阶段监听） */
function handleClickOutside(e: MouseEvent) {
  const target = e.target as Node;
  if (!target || !document.body.contains(target)) return;
  if (rootRef.value?.contains(target)) return;
  if (dropdownRef.value?.contains(target)) return;
  visible.value = false;
}

/** 根据触发器 rootRef 的视口位置，设置下拉的 fixed left/top（下拉在 body 下） */
function updatePopupPosition() {
  if (!rootRef.value) return;
  const rect = rootRef.value.getBoundingClientRect();
  // update-begin--author:liaozhiyang---date:20260714---for【LHZP-100】省市区在页面边缘时，最后一级没法选
  const margin = 8;
  const viewportWidth = document.documentElement.clientWidth;
  const viewportHeight = document.documentElement.clientHeight;
  // 浮层宽高（未渲染出来时退化为仅按触发器定位）
  const dropdownWidth = dropdownRef.value?.offsetWidth ?? 0;
  const dropdownHeight = dropdownRef.value?.offsetHeight ?? 0;

  // 水平：默认与触发器左对齐；右侧放不下则向左偏移使其完整显示
  let left = rect.left;
  if (dropdownWidth && left + dropdownWidth > viewportWidth - margin) {
    left = viewportWidth - dropdownWidth - margin;
  }
  if (left < margin) left = margin;

  // 垂直：默认在下方；下方放不下且上方空间足够则翻到上方
  let top = rect.bottom + DROPDOWN_GAP;
  if (dropdownHeight && top + dropdownHeight > viewportHeight - margin) {
    const aboveTop = rect.top - DROPDOWN_GAP - dropdownHeight;
    top = aboveTop >= margin ? aboveTop : Math.max(margin, viewportHeight - dropdownHeight - margin);
  }

  dropdownPlaceStyle.value = { left: `${left}px`, top: `${top}px` };
  // update-end--author:liaozhiyang---date:20260714---for：【LHZP-100】省市区在页面边缘时，最后一级没法选
}

// update-begin--author:liaozhiyang---date:20260714---for：【LHZP-100】省市区在页面边缘时，最后一级没法选
watch(columns, () => {
  if (visible.value) {
    nextTick(updatePopupPosition);
  }
});
// update-end--author:liaozhiyang---date:20260714---for：【LHZP-100】省市区在页面边缘时，最后一级没法选

/** 展开时：用当前 value 初始化 editingPath、计算定位、注册点击外部关闭；收起时移除监听 */
watch(visible, (v) => {
  if (v) {
    editingPath.value = normalizePath(props.value).slice();
    nextTick(() => {
      updatePopupPosition();
      setTimeout(() => document.addEventListener('click', handleClickOutside, true), 0);
    });
  } else {
    document.removeEventListener('click', handleClickOutside, true);
  }
});

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside, true);
});
</script>

<!-- 下拉挂 body 时用 .cascader-dropdown-placement 覆盖为 position: fixed -->
<style scoped lang="less">
@import (reference) '/@/design/config.less';

.cascader {
  position: relative;
  display: inline-block;
  width: 100%;
  min-width: 0;
}

.cascader .ant-input-affix-wrapper {
  cursor: pointer;

  .ant-input-clear-icon {
    display: none;
  }

  &:hover {
    .ant-input-clear-icon {
      display: block;
    }

    .cascader-arrow {
      opacity: 0;
    }
  }

  &.ant-input-affix-wrapper-disabled,
  &.ant-input-affix-wrapper-disabled:hover {
    cursor: not-allowed;

    .ant-input-clear-icon {
      display: none !important;
    }

    .cascader-arrow {
      opacity: 1;
    }
  }
}

.cascader-arrow {
  font-size: 10px;
  transition: transform 0.2s;
}

.cascader-arrow.is-open {
  transform: rotate(180deg);
}

:deep(.ant-input-suffix) {
  position: relative;

  .ant-input-clear-icon {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    margin: 0;
    z-index: 1;
  }
}

.cascader-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  z-index: 1050;
  margin-top: 4px;
  background: @component-background;
  border-radius: 6px;
  box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 3px 6px -4px rgba(0, 0, 0, 0.12), 0 9px 28px 8px rgba(0, 0, 0, 0.05);
}

.cascader-dropdown-placement {
  position: fixed;
  margin-top: 0;
}

.cascader-menus {
  display: flex;
  min-height: 180px;
}

.cascader-menu {
  padding: 5px;
  min-width: 112px;
  max-height: 180px;
  overflow-y: auto;
  background: @component-background;

  &:not(:last-child) {
    border-right: 1px solid @border-color-split;
  }
}

.cascader-menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 5px 12px;
  font-size: 14px;
  line-height: 22px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.cascader-menu-item-content {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cascader-menu-item-arrow {
  margin-left: 8px;
  font-size: 10px;
  color: @text-color-secondary;
  flex-shrink: 0;
}

.cascader-menu-item:hover {
  background: @item-hover-bg;
}

.cascader-menu-item.is-active .cascader-menu-item-arrow,
.cascader-menu-item.is-selected .cascader-menu-item-arrow {
  color: var(--j-global-primary-color);
}

.cascader-menu-item.is-active {
  background: @primary-1;
  color: var(--j-global-primary-color);
}

@supports (background: color-mix(in srgb, red, blue)) {
  .cascader-menu-item.is-active {
    background: color-mix(in srgb, var(--j-global-primary-color) 10%, #fff);
  }
}

.cascader-menu-item.is-selected {
  font-weight: 500;
  color: var(--j-global-primary-color);
}

.cascader-dropdown-enter-active,
.cascader-dropdown-leave-active {
  transition: opacity 0.15s;
}

.cascader-dropdown-enter-from,
.cascader-dropdown-leave-to {
  opacity: 0;
}
</style>
