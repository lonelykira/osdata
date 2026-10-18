// 给使用原生vxe-table的页面注册vxe-table组件
export const useVxeTableRegister = async () => {
  const app = window['JAppRootInstance'];
  if (app._context.components.VxeTable) {
    // 已全局注册
  } else {
    const { registerJVxeTable } = await import('/@/components/jeecg/JVxeTable');
    await registerJVxeTable(app);
    const { registerJVxeCustom } = await import('/@/components/JVxeCustom');
    await registerJVxeCustom();
  }
}
