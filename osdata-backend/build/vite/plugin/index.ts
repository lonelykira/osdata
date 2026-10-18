import { PluginOption } from 'vite';
import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
import purgeIcons from 'vite-plugin-purge-icons';
// unplugin-icons 为 ESM-only，用动态 import 避免 CJS 配置加载时 require 报错
import UnoCSS from 'unocss/vite';
import { presetTypography, presetUno } from 'unocss';
import Components from 'unplugin-vue-components/vite';
import { AntDesignVueResolver } from 'unplugin-vue-components/resolvers';

// 本地调试https配置方法
import VitePluginCertificate from 'vite-plugin-mkcert';
//[issues/555]开发环境，vscode断点调试，文件或行数对不上
import vueSetupExtend from 'vite-plugin-vue-setup-extend-plus';
import { configHtmlPlugin } from './html';
import { configMockPlugin } from './mock';
import { configCompressPlugin } from './compress';
import { configVisualizerConfig } from './visualizer';
import { configThemePlugin } from './theme';
import { configSvgIconsPlugin } from './svgSprite';
import { configQiankunMicroPlugin } from './qiankunMicro';
import { configPwaPlugin } from './pwa';
// // electron plugin
// import { configElectronPlugin } from "./electron";
// //预编译加载插件(不支持vite3作废)
// import OptimizationPersist from 'vite-plugin-optimize-persist';
// import PkgConfig from 'vite-plugin-package-config';

/**
 *
 * @param viteEnv
 * @param isBuild
 * @param isQiankunMicro 是否【JEECG作为乾坤子应用】
 */
export async function createVitePlugins(
  viteEnv: ViteEnv,
  isBuild: boolean,
  isQiankunMicro: boolean,
) {
  const { VITE_USE_MOCK, VITE_BUILD_COMPRESS, VITE_BUILD_COMPRESS_DELETE_ORIGIN_FILE } = viteEnv;

  // update-begin--author:liaozhiyang---date:20260306---for:【QQYUN-14833】unplugin-icons 使用esm
  // 动态 import ESM-only，避免 CJS 配置链 require 报 ERR_REQUIRE_ESM （解决node 20启动报错）
  const [
    { default: Icons },
    { default: IconsResolver },
  ] = await Promise.all([
    import('unplugin-icons/vite'),
    import('unplugin-icons/resolver'),
  ]);
  // update-end--author:liaozhiyang---date:20260306---for:【QQYUN-14833】unplugin-icons 使用esm
  const vitePlugins: (PluginOption | PluginOption[])[] = [
    // have to
    vue(),
    // have to
    vueJsx(),
    // support name
    vueSetupExtend(),
    // @ts-ignore
    VitePluginCertificate({
      source: 'coding',
    }),
  ];

  // update-begin--author:copilot---date:20260711---for:【依赖升级排查】屏蔽被 UnoCSS 误识别为原子类的业务字符串/注释文本
  // 源码中存在 '[orderby:...]'、'[09:00]' 等业务字符串/注释文本，会被 UnoCSS 静态扫描误判为形如 [prop:value] 的
  // 任意值原子类，生成的 CSS 规则值不是合法 CSS（如包含 JS 表达式片段），旧版 esbuild/lightningcss minify 较宽松未报错，
  // vite8 lightningcss 严格校验会直接构建失败，故通过 blocklist 屏蔽这些非预期匹配。
  vitePlugins.push(
    UnoCSS({
      presets: [presetUno(), presetTypography()],
      blocklist: [/^\[orderby:.*\]$/, /^\[uploading:.*\]$/, /^\[\d{2}:\d{2}\]$/],
    })
  );
  // update-end--author:copilot---date:20260711---for:【依赖升级排查】屏蔽被 UnoCSS 误识别为原子类的业务字符串/注释文本
  // update-begin--author:liaozhiyang---date:20260302---for:【QQYUN-14806】antd采用unplugin-vue-components实现按需加载
  // unplugin-vue-components: ant-design-vue 组件自动按需导入
  vitePlugins.push(
    Components({
      resolvers: [
        AntDesignVueResolver({
          importStyle: false,
          // 排除antd的Button组件，让项目中的a-button指向BasicButton组件
          // 低版本是AButton，高版本是Button
          exclude: ['AButton', 'Button'],
        }),
        IconsResolver({
          prefix: 'iconify',
        }),
      ],
      dirs: [],
      dts: false,
    })
  );
  // update-end--author:liaozhiyang---date:20260302---for:【QQYUN-14806】antd采用unplugin-vue-components实现按需加载

  // vite-plugin-html
  vitePlugins.push(configHtmlPlugin(viteEnv, isBuild, isQiankunMicro));

  // vite-plugin-svg-icons
  vitePlugins.push(configSvgIconsPlugin(isBuild));

  // vite-plugin-mock
  VITE_USE_MOCK && vitePlugins.push(configMockPlugin(isBuild));

  // update-begin--author:liaozhiyang---date:20260304---for:【QQYUN-14802】新增unplugin-icons插件，及icon支持online和local两种模式
  if (viteEnv.VITE_GLOB_ICONIFY_USE_TYPE === 'local') {
    vitePlugins.push(purgeIcons());
  } 
  vitePlugins.push(
    Icons({
      compiler: 'vue3',
       // 自动安装图标集
      autoInstall: false,
      scale: 1,
      defaultClass: 'app-iconify anticon',
    })
  );
  vitePlugins.push(purgeIcons());
  // update-end--author:liaozhiyang---date:20260304---for:【QQYUN-14802】新增unplugin-icons插件，及icon支持online和local两种模式

  // rollup-plugin-visualizer
  vitePlugins.push(configVisualizerConfig());

  // vite-plugin-theme
  vitePlugins.push(configThemePlugin(isBuild));

  // 【JEECG作为乾坤子应用】注册乾坤子应用模式插件
  if (isQiankunMicro) {
    // vite-plugin-qiankun
    vitePlugins.push(...configQiankunMicroPlugin(viteEnv))
  }

  // // electron plugin
  const isElectron = viteEnv.VITE_GLOB_RUN_PLATFORM === 'electron';
  // if (isElectron) {
  //   vitePlugins.push(configElectronPlugin(viteEnv, isBuild))
  // }

  // The following plugins only work in the production environment
  if (isBuild) {
    
    // rollup-plugin-gzip
    vitePlugins.push(configCompressPlugin(VITE_BUILD_COMPRESS, VITE_BUILD_COMPRESS_DELETE_ORIGIN_FILE));

    // vite-plugin-pwa (PWA 插件注册)
    if (!isElectron) {
      vitePlugins.push(configPwaPlugin(isBuild));
    }
  }

  // //vite-plugin-theme【预编译加载插件，解决vite首次打开界面加载慢问题】
  // vitePlugins.push(PkgConfig());
  // vitePlugins.push(OptimizationPersist());
  return vitePlugins;
}
