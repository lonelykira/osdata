// update-begin--author:copilot---date:20260711---for:【依赖升级排查】修复 Rolldown 严格 CJS 互操作导致 tinymce 全局未挂载
// tinymce 核心包(tinymce/tinymce)是 UMD 写法：`typeof module === 'object' && module.exports ? module.exports = tinymce : window.tinymce = tinymce`。
// Rolldown 对 CJS 的互操作比 Rollup/esbuild 更严格，会给这个文件提供真实的 module 对象，
// 导致它总是走 module.exports 分支，不再顺带把 window.tinymce 设置上。
// 而 tinymce 的各个 plugin/theme 包（如 tinymce/plugins/fullscreen）在模块顶层直接引用裸变量 `tinymce`
// （例如 `tinymce.util.Tools.resolve('tinymce.PluginManager')`），依赖 window.tinymce 已提前挂载，
// 否则会抛出 `ReferenceError: tinymce is not defined`。
// 这里统一从本文件导入 tinymce 核心，保证在其余 tinymce 子模块（themes/plugins/icons）被 import 之前，
// window.tinymce 已经显式挂载好。
import tinymceCore from 'tinymce/tinymce';

if (typeof window !== 'undefined') {
  (window as any).tinymce = tinymceCore;
}

export default tinymceCore;
// update-end--author:copilot---date:20260711---for:【依赖升级排查】修复 Rolldown 严格 CJS 互操作导致 tinymce 全局未挂载
