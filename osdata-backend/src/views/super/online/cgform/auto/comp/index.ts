import type { App } from 'vue';
import {defineAsyncComponent} from 'vue'
const SuperQuery = defineAsyncComponent(() => import('./superquery/SuperQuery.vue'))
const JOnlineSearchSelect = defineAsyncComponent(() => import('./JOnlineSearchSelect.vue'))

export const registerOnlineComp = {
  install(app: App) {
    app.component('JOnlineSearchSelect', JOnlineSearchSelect);
    app.component('SuperQuery', SuperQuery);

    console.log("---初始化---， 全局注册Online部分组件--------------")
  },
};
