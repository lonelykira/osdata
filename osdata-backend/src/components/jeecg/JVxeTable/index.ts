import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
// update-begin--author:liaozhiyang---date:20260318---for:【QQYUN-14948】需要兼容vxetable引入到了页面也不报错
// 兼容页面直接引入：通过异步组件包装，确保 vxe-table 注册后再渲染，避免页面卡死
export const JVxeTable = createAsyncComponent(
  async () => {
    const app = window['JAppRootInstance'];
    if (app && !app._context.components['VxeTable']) {
      const { registerJVxeTable } = await import('./src/install');
      await registerJVxeTable(app);
      const { registerJVxeCustom } = await import('/@/components/JVxeCustom');
      await registerJVxeCustom();
    }
    const m = await import('./src/JVxeTable');
    return m.default;
  },
  { loading: true }
);
// update-end--author:liaozhiyang---date:20260318---for:【QQYUN-14948】需要兼容vxetable引入到了页面也不报错
export { registerJVxeTable } from './src/install';
export { deleteComponent } from './src/componentMap';
export { registerComponent, registerAsyncComponent, registerASyncComponentReal } from './src/utils/registerUtils';
