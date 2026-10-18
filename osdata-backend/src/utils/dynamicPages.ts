// 获取所有动态页面(views目录下的所有vue文件和tsx文件)
const allFiles = import.meta.glob(
  [
    '../views/**/*.{vue,tsx}',// 获取所有vue和tsx文件
    // 排除特定文件夹
    '!../views/system/approvalrole/compoments/**',
  ]
);
// 合并所有动态页面
export const dynamicPages = { ...allFiles };
