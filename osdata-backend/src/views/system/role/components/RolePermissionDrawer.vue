<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" width="650px" destroyOnClose showFooter rootClassName="jeecg-role-auth-drawer">
    <template #title>
      角色权限配置
      <a-dropdown>
        <a-button class="more-icon">
          更多操作
          <Icon icon="ant-design:down-outlined" size="14px" style="position: relative;top: 1px;right: 5px"></Icon>
        </a-button>
        <template #overlay>
          <a-menu @click="treeMenuClick">
            <a-menu-item key="checkAll">选择全部</a-menu-item>
            <a-menu-item key="cancelCheck">取消选择</a-menu-item>
            <div class="line"></div>
            <a-menu-item key="openAll">展开全部</a-menu-item>
            <a-menu-item key="closeAll">折叠全部</a-menu-item>
            <div class="line"></div>
            <a-menu-item key="relation">层级关联</a-menu-item>
            <a-menu-item key="standAlone">层级独立</a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
      <a-input-search
        v-model:value="searchValue"
        placeholder="按名称搜索"
        allowClear
        class="title-search"
        @search="onSearch"
        @change="onSearch"
      />
    </template>
    <div ref="treeWrapRef" class="perm-tree-wrap">
    <BasicTree
      ref="treeRef"
      checkable
      highlight
      expandOnSearch
      :height="treeHeight"
      :fieldNames="{ key: 'key', title: 'slotTitle' }"
      :treeData="treeData"
      :checkedKeys="checkedKeys"
      :expandedKeys="expandedKeys"
      :selectedKeys="selectedKeys"
      :clickRowToExpand="false"
      :checkStrictly="true"
      title="所拥有的的权限"
      @check="onCheck"
      @select="onTreeNodeSelect"
    >
      <template #title="{ slotTitle, ruleFlag }">
        {{ slotTitle }}
        <Icon v-if="ruleFlag" icon="ant-design:align-left-outlined" style="margin-left: 5px; color: red"></Icon>
      </template>
    </BasicTree>
    </div>
    <!--右下角按钮-->
    <template #footer>
      <!-- <PopConfirmButton title="确定放弃编辑？" @confirm="closeDrawer" okText="确定" cancelText="取消"></PopConfirmButton> -->
      <a-button @click="closeDrawer">取消</a-button>
      <a-button @click="handleSubmit(false)" type="primary" :loading="loading" ghost style="margin-right: 0.8rem">仅保存</a-button>
      <a-button @click="handleSubmit(true)" type="primary" :loading="loading">保存并关闭</a-button>
    </template>
    <RoleDataRuleDrawer @register="registerDrawer1" />
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, computed, unref, onMounted, onUnmounted, nextTick } from 'vue';
  import { BasicDrawer, useDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicTree, TreeItem } from '/@/components/Tree';
  import { PopConfirmButton } from '/@/components/Button';
  import RoleDataRuleDrawer from './RoleDataRuleDrawer.vue';
  import { queryTreeListForRole, queryRolePermission, saveRolePermission } from '../role.api';
  import { useI18n } from "/@/hooks/web/useI18n";
  import { ROLE_AUTH_CONFIG_KEY } from '/@/enums/cacheEnum';
  const emit = defineEmits(['register']);
  //树的信息
  const treeData = ref<TreeItem[]>([]);
  //树的全部节点信息
  const allTreeKeys = ref([]);
  //树的选择节点信息
  const checkedKeys = ref<any>([]);
  const defaultCheckedKeys = ref([]);
  //树的选中的节点信息
  const selectedKeys = ref([]);
  const roleId = ref('');
  //树的实例
  const treeRef = ref(null);
  const loading = ref(false);

  // 代码逻辑说明: 虚拟滚动——给 antd Tree 一个数字 height 即开启，只渲染可见节点，解决大数据量渲染卡顿
  // 树容器的包裹层，用于实测树在视口中的实际顶部位置
  const treeWrapRef = ref<HTMLElement | null>(null);
  const treeHeight = ref(400);
  // 按真实几何位置精确计算：antd 虚拟列表顶部 → 抽屉体内容底部 的距离。
  // 这些位置都不随 treeHeight 变化（由其上方/外层固定元素决定），故无反馈死循环，测一次即可。
  function calcTreeHeight() {
    const el = unref(treeWrapRef);
    if (!el) {
      treeHeight.value = window.innerHeight - 220;
      return;
    }
    const bodyWrap = el.closest('.scrollbar__wrap') as HTMLElement | null; // 抽屉体滚动视口(带16px内边距)
    const list = el.querySelector('.ant-tree-list') as HTMLElement | null; // antd 虚拟列表容器
    if (bodyWrap && list) {
      const bottom = bodyWrap.getBoundingClientRect().bottom - 16; // 扣除底部内边距
      const listTop = list.getBoundingClientRect().top;
      treeHeight.value = Math.max(200, Math.floor(bottom - listTop - 4));
      return;
    }
    // 兜底
    const avail = bodyWrap?.clientHeight || (el.closest('.ant-drawer-body') as HTMLElement)?.clientHeight || window.innerHeight - 120;
    treeHeight.value = Math.max(200, Math.floor(avail - 90));
  }
  function handleWindowResize() {
    calcTreeHeight();
  }
  onMounted(() => {
    window.addEventListener('resize', handleWindowResize);
  });
  onUnmounted(() => {
    window.removeEventListener('resize', handleWindowResize);
  });

  //标题栏搜索关键字
  const searchValue = ref('');
  //展开折叠的key
  const expandedKeys = ref<any>([]);
  //父子节点选中状态是否关联 true不关联，false关联
  const checkStrictly = ref<boolean>(false);
  const [registerDrawer1, { openDrawer: openDataRuleDrawer }] = useDrawer();
  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await reset();
    setDrawerProps({ confirmLoading: false, loading: true });
    roleId.value = data.roleId;
    //初始化数据
    const roleResult = await queryTreeListForRole();
    // 代码逻辑说明: 【QQYUN-8355】角色权限配置的菜单翻译
    treeData.value = translateTitle(roleResult.treeList);
    allTreeKeys.value = roleResult.ids;
    const localData = localStorage.getItem(ROLE_AUTH_CONFIG_KEY);
    if (localData) {
      const obj = JSON.parse(localData);
      obj.level && treeMenuClick({ key: obj.level });
      obj.expand && treeMenuClick({ key: obj.expand });
    } else {
      expandedKeys.value = roleResult.ids;
    }
    //初始化角色菜单数据
    const permResult = await queryRolePermission({ roleId: unref(roleId) });
    checkedKeys.value = permResult;
    defaultCheckedKeys.value = permResult;
    setDrawerProps({ loading: false });
    // 抽屉渲染完成后测算一次虚拟滚动高度（高度源为固定的抽屉体视口，无需自动重算）
    nextTick(calcTreeHeight);
  });
  /**
  * 2024-02-28
  * liaozhiyang
  * 翻译菜单名称
   */
  function translateTitle(data) {
    if (data?.length) {
      data.forEach((item) => {
        if (item.slotTitle) {
          const { t } = useI18n();
          if (item.slotTitle.includes("t('") && t) {
            item.slotTitle = new Function('t', `return ${item.slotTitle}`)(t);
          }
        }
        if (item.children?.length) {
          translateTitle(item.children);
        }
      });
    }
    return data;
  }
  /**
   * 点击选中
   * 2024-04-26
   * liaozhiyang
   */
  function onCheck(o, e) {
    // checkStrictly: true=>层级独立，false=>层级关联.
    if (checkStrictly.value) {
      checkedKeys.value = o.checked ? o.checked : o;
    } else {
      const keys = getNodeAllKey(e.node, 'children', 'key');
      if (e.checked) {
        // 反复操作下可能会有重复的keys，得用new Set去重下
        checkedKeys.value = [...new Set([...checkedKeys.value, ...keys])];
      } else {
        const result = removeMatchingItems(checkedKeys.value, keys);
        checkedKeys.value = result;
      }
    }
  }
  /**
   * 2024-04-26
   * liaozhiyang
   * 删除相匹配数组的项
   */
  function removeMatchingItems(arr1, arr2) {
    // 使用哈希表记录 arr2 中的元素
    const hashTable = {};
    for (const item of arr2) {
      hashTable[item] = true;
    }
    // 使用 filter 方法遍历第一个数组，过滤出不在哈希表中存在的项
    return arr1.filter((item) => !hashTable[item]);
  }
  /**
   * 2024-04-26
   * liaozhiyang
   * 获取当前节点及以下所有子孙级的key
   */
  function getNodeAllKey(node: any, children: any, key: string) {
    const result: any = [];
    result.push(node[key]);
    const recursion = (data) => {
      data.forEach((item: any) => {
        result.push(item[key]);
        if (item[children]?.length) {
          recursion(item[children]);
        }
      });
    };
    node[children]?.length && recursion(node[children]);
    return result;
  }

  /**
   * 标题栏按名称搜索权限树
   */
  function onSearch() {
    getTree()?.setSearchValue(unref(searchValue));
  }
  /**
   * 选中节点，打开数据权限抽屉
   */
  function onTreeNodeSelect(key) {
    if (key && key.length > 0) {
      selectedKeys.value = key;
    }
    openDataRuleDrawer(true, { functionId: unref(selectedKeys)[0], roleId: unref(roleId) });
  }
  /**
   * 数据重置
   */
  function reset() {
    treeData.value = [];
    allTreeKeys.value = [];
    checkedKeys.value = [];
    defaultCheckedKeys.value = [];
    selectedKeys.value = [];
    roleId.value = '';
    searchValue.value = '';
  }
  /**
   * 获取tree实例
   */
  function getTree() {
    const tree = unref(treeRef);
    if (!tree) {
      throw new Error('tree is null!');
    }
    return tree;
  }
  /**
   * 提交
   */
  async function handleSubmit(exit) {
    let params = {
      roleId: unref(roleId),
      permissionIds: unref(getTree().getCheckedKeys()).join(','),
      lastpermissionIds: unref(defaultCheckedKeys).join(','),
    };
    // 代码逻辑说明: issues/352 VUE角色授权重复保存
    if(loading.value===false){
      await doSave(params)
    }else{
      console.log('请等待上次执行完毕!');
    }
    if(exit){
      // 如果关闭
      closeDrawer();
    }else{
      // 没有关闭需要重新获取选中数据
      const permResult = await queryRolePermission({ roleId: unref(roleId) });
      defaultCheckedKeys.value = permResult;
    }
  }

  // VUE角色授权重复保存 #352
  async function doSave(params) {
    loading.value = true;
    try {
      await saveRolePermission(params);
    } catch (e) {
      loading.value = false;
    }
    setTimeout(()=>{
      loading.value = false;
    }, 500)
  }

  /**
   * 树菜单选择
   * @param key
   */
  function treeMenuClick({ key }) {
    if (key === 'checkAll') {
      checkedKeys.value = allTreeKeys.value;
    } else if (key === 'cancelCheck') {
      checkedKeys.value = [];
    } else if (key === 'openAll') {
      expandedKeys.value = allTreeKeys.value;
      saveLocalOperation('expand', 'openAll');
    } else if (key === 'closeAll') {
      expandedKeys.value = [];
      saveLocalOperation('expand', 'closeAll');
    } else if (key === 'relation') {
      checkStrictly.value = false;
      saveLocalOperation('level', 'relation');
    } else {
      checkStrictly.value = true;
      saveLocalOperation('level', 'standAlone');
    }
  }
  /**
   * 2024-05-31
   * liaozhiyang
   * 【TV360X-590】角色授权弹窗操作缓存
   * */
  const saveLocalOperation = (key, value) => {
    const localData = localStorage.getItem(ROLE_AUTH_CONFIG_KEY);
    const obj = localData ? JSON.parse(localData) : {};
    obj[key] = value;
    localStorage.setItem(ROLE_AUTH_CONFIG_KEY, JSON.stringify(obj))
  };
</script>

<style lang="less" scoped>
  /** 树容器包裹层 */
  .perm-tree-wrap {
    /** 让 BasicTree 自带的 ScrollContainer 不参与滚动/不限高/不显示滚动条，滚动完全交给 antd 虚拟列表，
        避免重复滚动条，也避免高度链相互影响导致的卡死 */
    :deep(.scroll-container),
    :deep(.scrollbar__wrap),
    :deep(.scrollbar__view) {
      height: auto !important;
      max-height: none !important;
      overflow: visible !important;
    }
    :deep(.scrollbar__bar) {
      display: none !important;
    }
  }
  /** 固定操作按钮 */
  .jeecg-basic-tree {
    position: absolute;
    width: 618px;
  }
  // 代码逻辑说明: 抽屉弹窗标题图标下拉样式------------
  .line {
    height: 1px;
    width: 100%;
    border-bottom: 1px solid #f0f0f0;
  }
  // 标题栏搜索框：靠右、固定在标题行，不随内容滚动
  .title-search {
    float: right;
    width: 200px;
    margin-right: 12px;
  }
  .more-icon {
/*    font-size: 20px !important;
    color: black;
    display: inline-flex;*/
    float: right;
    margin-right: 2px;
    cursor: pointer;
  }
  :deep(.jeecg-tree-header) {
    border-bottom: none;
  }
</style>

<!-- 非 scoped 全局样式：抽屉经 Teleport 渲染到 body，scoped 选择器无法命中；
     用唯一 rootClassName 限定，仅隐藏本抽屉内所有自定义滚动条，滚动统一交给 antd 虚拟列表，互不影响其它弹窗 -->
<style lang="less">
  .jeecg-role-auth-drawer .scrollbar__bar {
    display: none !important;
  }
  /** 抽屉体那层禁止滚动，物理上杜绝"借用外层滚动"；滚动只由 antd 虚拟列表负责（高度已精确计算不会裁剪） */
  .jeecg-role-auth-drawer .ant-drawer-body > .scrollbar > .scrollbar__wrap {
    overflow: hidden !important;
  }
</style>
