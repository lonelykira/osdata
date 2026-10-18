import type { App } from 'vue';
import { registerJVxeTable } from '/@/components/jeecg/JVxeTable';
import { registerJVxeCustom } from '/@/components/JVxeCustom';

// 注册全局dayjs
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import customParseFormat from 'dayjs/plugin/customParseFormat';
import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';

// JVxeTable 按需加载：仅首次渲染时注册一次
let jvxeRegistered = false;

export async function registerThirdComp(app: App) {
  //---------------------------------------------------------------------
  // update-begin--author:liaozhiyang---date:20260210---for:【QQYUN-13658】Jvxetable、vxetable按需加载
  // 注册 JVxeTable 组件（按需加载：首次使用 <JVxeTable> 时才加载 vxe-table 与 JVxeTable）
  app.component(
    'JVxeTable',
    createAsyncComponent(
      () => {
        return import('/@/components/jeecg/JVxeTable/src/JVxeTable').then(async (m) => {
          if (!jvxeRegistered) {
            if (app._context.components.VxeTable) {
              // 已全局注册
            } else {
              const { registerJVxeTable } = await import('/@/components/jeecg/JVxeTable/src/install');
              await registerJVxeTable(app);
              const { registerJVxeCustom } = await import('/@/components/JVxeCustom');
              await registerJVxeCustom();
              jvxeRegistered = true;
            }
          }
          return m.default;
        });
      },
      { loading: true }
    )
  );
  // update-end--author:liaozhiyang---date:20260209---for:【QQYUN-13658】Jvxetable、vxetable按需加载
  //---------------------------------------------------------------------
  // 注册全局聊天表情包
  // 代码逻辑说明: 【QQYUN-8241】emoji-mart-vue-fast库异步加载
  app.component(
    'Picker',
    createAsyncComponent(() => {
      return new Promise((resolve, rejected) => {
        import('emoji-mart-vue-fast/src')
          .then((res) => {
            const { Picker } = res;
            resolve(Picker);
          })
          .catch((err) => {
            rejected(err);
          });
      });
    })
  );
  // update-end--author:liaozhiyang---date:20240308---for：【QQYUN-8241】emoji-mart-vue-fast库异步加载
  //---------------------------------------------------------------------
  // 注册全局dayjs
  dayjs.locale('zh-cn');
  dayjs.extend(relativeTime);
  dayjs.extend(customParseFormat);
  app.config.globalProperties.$dayjs = dayjs
  app.provide('$dayjs', dayjs)
  //---------------------------------------------------------------------
}
