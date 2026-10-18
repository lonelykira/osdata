import type { App } from 'vue';
import { Icon } from './Icon';
import AIcon from '/@/components/jeecg/AIcon.vue';
//Tinymce富文本
//  import Editor from '/@/components/Tinymce/src/Editor.vue'

import { Button, JUploadButton } from './Button';
import { Space } from 'ant-design-vue';

// ant-design-vue 组件通过 unplugin-vue-components + AntDesignVueResolver 自动按需导入，无需手动注册
const compList = [Icon, AIcon, JUploadButton];
//注册online模块的全局组件
import { registerOnlineComp } from '/@/views/super/online/cgform/auto/comp/index';
import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';


export function registerGlobComp(app: App) {
  compList.forEach((comp) => {
    app.component(comp.name || comp.displayName, comp);
  });
  // Space.Compact 是子组件，AntDesignVueResolver 无法自动解析，需手动注册
  app.component('ASpaceCompact', Space.Compact);
  
  //仪表盘依赖Tinymce，需要提前加载（没办法按需加载了）
  // app.component(Editor.name, Editor);
  // 代码逻辑说明: Tinymce异步加载
  // update-begin--author:liaozhiyang---date:20260227---for:【QQYUN-14751】tinymce富文本、JEasyCron、JLinkTableCard异步加载
  app.component(
    'Tinymce',
    createAsyncComponent(() => import('./Tinymce/src/Editor.vue'), {
      loading: true,
    })
  );
  // update-end--author:liaozhiyang---date:20260227---for:【QQYUN-14751】tinymce富文本、JEasyCron、JLinkTableCard异步加载
  app
    .use(registerOnlineComp)
    .use(Button)
  console.log("---初始化---， 全局注册Antd、仪表盘、流程设计器、online、流程等组件--------------")
}
