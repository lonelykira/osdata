import { withInstall } from '/@/utils';
import { defineAsyncComponent } from 'vue';

export { useAppProviderContext } from './src/useAppContext';

export const AppLogo = withInstall(defineAsyncComponent(() => import('./src/AppLogo.vue')));
export const AppProvider = withInstall(defineAsyncComponent(() => import('./src/AppProvider.vue')));
export const AppSearch = withInstall(defineAsyncComponent(() => import('./src/search/AppSearch.vue')));
export const AppLocalePicker = withInstall(defineAsyncComponent(() => import('./src/AppLocalePicker.vue')));
export const AppDarkModeToggle = withInstall(defineAsyncComponent(() => import('./src/AppDarkModeToggle.vue')));
