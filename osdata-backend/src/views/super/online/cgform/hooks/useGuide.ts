import { onMounted, ref } from 'vue';
import { useDesign } from '/@/hooks/web/useDesign';
import intro from 'intro.js';
import 'intro.js/minified/introjs.min.css';

export const useGuide = () => {
  const { prefixVar } = useDesign('');
  const aiCreateTable = ref(`${prefixVar}-online-aiCreateTable`);
  const newAddBtn = ref(`${prefixVar}-online-newAddBtn`);
  const customBtn = ref(`${prefixVar}-online-customBtn`);
  const enhanceJsBtn = ref(`${prefixVar}-online-enhanceJsBtn`);
  const enhanceSqlBtn = ref(`${prefixVar}-online-enhanceSqlBtn`);
  const enhanceJavaBtn = ref(`${prefixVar}-online-enhanceJavaBtn`);
  const exportDbBtn = ref(`${prefixVar}-online-exportDbBtn`);
  const codeGeneratorBtn = ref(`${prefixVar}-online-codeGenerator`);
  const key = `${prefixVar}-online-guide`;
  const guide = () => {
    let boot = intro();
    boot.setOptions({
      nextLabel: '下一步',
      prevLabel: '上一步',
      //skipLabel: '跳过',
      doneLabel: '完成',
      disableInteraction: true,
      steps: [
        {
          title: '第一步',
          element: document.querySelector(`.${newAddBtn.value}`)!,
          intro: '点击<strong>新增</strong>按钮，新建一个表。',
        },
        {
          title: '第二步',
          intro: `在列表中找到刚才新建数据，在操作列点击<strong>"更多"</strong>，选择<strong>"同步数据库"</strong>。`,
        },
        {
          title: '第三步',
          intro: `在列表中找到刚才新建数据，在操作列点击<strong>"更多"</strong>，选择<strong>"功能测试"</strong>。`,
        },
        {
          title: 'AI建表',
          element: document.querySelector(`.${aiCreateTable.value}`)!,
          intro: `输入修饰词即可通过AI创建工作表`,
        },
        {
          title: '代码生成',
          element: document.querySelector(`.${codeGeneratorBtn.value}`)!,
          intro: `选中一条记录，通过代码生成可将已配置好的表单，一键生成前后端代码，复杂需求可在此基础上进行二次开发。`,
        },
        {
          title: '自定义按钮',
          element: document.querySelector(`.${customBtn.value}`)!,
          intro: `选中一条记录，点击自定义按钮，配置按钮相关信息即可在当前记录的<strong>"功能测试"</strong>页面新增一个按钮`,
        },
        {
          title: 'JS强增',
          element: document.querySelector(`.${enhanceJsBtn.value}`)!,
          intro: `选中一条记录，通过js增强可为<strong>"自定义按钮"</strong>添加不同操作，可操作列表和表单数据等，也可以添加表单前置事件。`,
        },
        {
          title: 'SQL增强',
          element: document.querySelector(`.${enhanceSqlBtn.value}`)!,
          intro: `选中一条记录，通过增强SQL，可以关联修改业务数据。`,
        },
        {
          title: 'java增强',
          element: document.querySelector(`.${enhanceJavaBtn.value}`)!,
          intro: `选中一条记录，通过Java增强可在表单的增加、修改、和删除数据时实现额外的功能，类似spring中的AOP切面编程。`,
        },
        {
          title: '导入数据库表',
          element: document.querySelector(`.${exportDbBtn.value}`)!,
          intro: `可将已有数据库中的表，直接导入生成表单。`,
        },
      ],
    });
    boot.start();
  };
  onMounted(() => {
    if (!localStorage.getItem(key)) {
      setTimeout(() => {
        guide();
        localStorage.setItem(key, '1');
      }, 2e3);
    }
  });
  return {
    newAddBtn,
    customBtn,
    enhanceJsBtn,
    enhanceSqlBtn,
    exportDbBtn,
    enhanceJavaBtn,
    codeGeneratorBtn,
    aiCreateTable
  };
};
