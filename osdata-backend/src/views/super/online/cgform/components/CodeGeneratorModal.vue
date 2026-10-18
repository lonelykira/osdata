<template>
  <BasicModal
    @register="registerModal"
    :title="title"
    :width="modalWidth"
    :confirmLoading="confirmLoading"
    okText="开始生成"
    cancelText="取消"
    @ok="onSubmit"
    @cancel="onCancel"
    :wrapClassName="wrapClassName"
  >
    <a-spin :spinning="confirmLoading">
      <BasicForm @register="registerForm">
        <!-- update-begin--author:liaozhiyang---date:20240612---for：【TV360X-1057】代码生成页面代码鼠标移入给说明 -->
        <template #pageCode="{ model, field }">
          <a-radio-group v-model:value="model[field]">
            <a-tooltip placement="top">
              <template #title>
                <span>深度封装表单，用户只需定义字段json即可渲染表单，优点简单便捷，缺点扩展有难度</span>
              </template>
              <a-radio value="vue3">封装表单(BasicForm)</a-radio>
            </a-tooltip>
            <a-tooltip placement="top">
              <template #title>
                <span>antd的原生表单，所有字段都需要硬编码，缺点编码繁琐，优点扩展容易</span>
              </template>
             <a-radio value="vue3Native" v-if="!(model.jspMode == 'innerTable' || model.jspMode == 'tab')">原生表单(a-form)</a-radio>
            </a-tooltip>
          </a-radio-group>
        </template>
        <!-- update-end--author:liaozhiyang---date:20240612---for：【TV360X-1057】代码生成页面代码鼠标移入给说明 -->
      </BasicForm>
      <a-card v-if="showSubTable" title="子表信息" size="small">
        <JVxeTable ref="subTableRef" rowNumber :maxHeight="580" v-bind="subTable" />
      </a-card>
    </a-spin>
  </BasicModal>
  <FileSelectModal @register="registerFileSelectModal" @select="onFileSelect" />
  <!-- 生成代码文件后弹窗显示 -->
  <code-file-list-modal @register="registerCodeFileListModal"></code-file-list-modal>
</template>

<script lang="ts">
  import { ref, reactive, computed, nextTick, defineComponent,h } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  // TODO import { DisabledAuthFilterMixin } from '@/mixins/DisabledAuthFilterMixin'
  import { BasicForm, useForm } from '/@/components/Form';
  import { BasicModal, useModal, useModalInner } from '/@/components/Modal';
  import { JVxeTypes, JVxeColumn, JVxeTableInstance } from '/@/components/jeecg/JVxeTable/types';
  import { useCodeGeneratorFormSchemas } from '../hooks/useSchemas';
  import { underLine2CamelCase } from '/@/utils/common/compUtils';
  import CodeFileListModal from './CodeFileListModal.vue'
  import FileSelectModal from './FileSelectModal.vue';
  import {message} from "ant-design-vue";
  import { useDesign } from '/@/hooks/web/useDesign';
  import { createLocalStorage } from '/@/utils/cache';
  import { useMessage } from '@/hooks/web/useMessage';
  const $ls = createLocalStorage();
  const { notification } = useMessage();
  
  enum Api {
    tableInfo = '/online/cgform/head/tableInfo',
    codeGenerate = '/online/cgform/api/codeGenerate',
    downGenerateCode = '/online/cgform/api/downGenerateCode',
  }

  export default defineComponent({
    name: 'CodeGenerator',
    components: { BasicForm, BasicModal, FileSelectModal, CodeFileListModal },
    emits: ['register'],
    setup(props) {
      const JEECG_ONL_PROJECT_PATH = 'JEECG_ONL_PROJECT_PATH';
      const JEECG_ONL_PROJECT_NAME = 'JEECG_ONL_PROJECT_NAME';
      const single = ref(true);
      const subTableRef = ref<JVxeTableInstance>();
      const modalWidth = computed(() => (single.value ? 800 : 1200));
      const title = ref('代码生成');
      const confirmLoading = ref(false);
      const { prefixCls } = useDesign('code-generator-modal');
      const wrapClassName = prefixCls;
      const code = ref('');
      const metaModel = reactive({
        projectPath: '',
        packageStyle: 'service',
        jspMode: '',
        jformType: '1',
        tableName_tmp: '',
        ftlDescription: '',
        entityName: '',
        codeTypes: 'controller,service,dao,mapper,entity,vue',
      });
      const model = reactive<Recordable>({});
      const jspModeOptions = ref<any[]>([]);
      // 子表配置
      const subTable = reactive({
        dataSource: [] as Recordable[],
        columns: [
          {
            title: '子表名',
            key: 'tableName',
            type: JVxeTypes.input,
            disabled: true,
            validateRules: [{ required: true, message: '请输入${title}' }],
          },
          {
            title: '子表实体',
            key: 'entityName',
            type: JVxeTypes.input,
            validateRules: [{ required: true, message: '请输入${title}' }],
          },
          {
            title: '功能说明',
            key: 'ftlDescription',
            type: JVxeTypes.input,
            validateRules: [{ required: true, message: '请输入${title}' }],
          },
        ] as JVxeColumn[],
      });
      const showSubTable = computed<boolean>(() => subTable.dataSource.length > 0);

      const { formSchemas } = useCodeGeneratorFormSchemas(
        props,
        {
          onProjectPathChange,
          onProjectPathSearch,
          jspModeOptions,
        },
        single
      );

      // 表单配置
      const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        labelAlign: 'right',
      });

      const [registerModal, { closeModal }] = useModalInner(async (data) => {
        await resetFields();
        // 当前主表ID
        code.value = data.code;
        confirmLoading.value = false;
        subTable.dataSource = [];
        jspModeOptions.value = [];
        getStoreProjectPath();
        Object.assign(model, metaModel);
        loadData();
      });

      async function loadData() {
        let { main, sub, jspModeList, projectPath } = await defHttp.get({
          url: Api.tableInfo,
          params: { code: code.value },
        });
        //update-begin-author:taoyan date:2022-5-17 for: vue3没有经典风格 默认是jvxe风格
        let jspModeListForVue3: any[] = [];
        for (let mode of jspModeList) {
          const { code, note } = mode;
          if (code == 'many') {
            //经典风格不需要
          } else {
            jspModeListForVue3.push({
              label: note,
              value: code,
            });
          }
        }
        jspModeOptions.value = jspModeListForVue3;
        if (main.isTree == 'Y') {
          model.jspMode = 'tree';
        } else {
          // 获取主表风格
          if (jspModeListForVue3.find(item => item.value === main.themeTemplate)) {
            model.jspMode = main.themeTemplate
          } else {
            // 如果没有找到默认选中第一个
            model.jspMode = jspModeListForVue3[0].value;
          }
        }
        //update-end-author:taoyan date:2022-5-17 for: vue3没有经典风格 默认是jvxe风格
        single.value = main.tableType == 1;
        title.value = '代码生成【' + main.tableName + '】';
        if (!model.projectPath) {
          model.projectPath = projectPath;
          window.localStorage.setItem(JEECG_ONL_PROJECT_PATH, projectPath);
        }
        // update-begin--author:liaozhiyang---date:20240611---for：【TV360X-1054】代码生成包名缓存
        const projectName = localStorage.getItem(JEECG_ONL_PROJECT_NAME);
        if (projectName) {
          model.entityPackage = projectName;
        }
        // update-end--author:liaozhiyang---date:20240611---for：【TV360X-1054】代码生成包名缓存
        model.jformType = main.tableType + '';
        model.tableName_tmp = main.tableName;
        model.ftlDescription = main.tableTxt;

        let entityNameTemp = underLine2CamelCase(main.tableName);
        model.entityName = entityNameTemp.substring(0, 1).toUpperCase() + entityNameTemp.substring(1);
        await nextTick();
        setFieldsValue(model);
        if (sub && sub.length > 0) {
          subTable.dataSource = sub.map((item) => ({
            tableName: item.tableName,
            entityName: getCamelCase(item.tableName),
            ftlDescription: item.tableTxt,
          }));
        }
      }

      // 生成代码文件后弹窗显示
      const [registerCodeFileListModal, {openModal: openCodeFileListModal}] = useModal();

      async function onSubmit() {
        try {
          const values = await validate();
          let params = Object.assign({}, values, { code: code.value, tableName: values.tableName_tmp });
          if (showSubTable.value) {
            let errMap = await subTableRef.value!.validateTable();
            if (errMap) {
              return;
            }
            params.subList = subTableRef.value!.getTableData();
          }
          confirmLoading.value = true;
        //  let codeList = await defHttp.post({ url: Api.codeGenerate, params });
          let res:any = await codeGen(params);
          //打开文件列表弹窗
          openCodeFileListModal(true, {
            codeList: res.codeList,
            pathKey: res.pathKey,
            tableName: values.tableName_tmp
          });

          //-----------------------------------------------------------------------------------------
          // 前端代码直接生成vue3项目，会自动加载刷新，给有效期10秒的临时提示信息
          const hasViewPath = res.codeList.some(
            s => s.includes('src/views/') || s.includes('src\\views\\')
          );
          if(hasViewPath){
            $ls.set(
              'code.genenrate.success.msg',
              `表【${values.tableName_tmp}】代码生成成功！前端代码已自动刷新，Java后台需重启生效！`,
              15
            );
          }else{
            notification.success({
              message: `表【${values.tableName_tmp}】代码生成成功`,
              description: h('div', [
                '1. 前端代码请迁移到 VUE3项目', h('br'),
                '2. 菜单SQL放到jeecg-system-start的flyway目录', h('br'),
                '3. Java 后台需重启生效'
              ]),
              duration: 5,
            });
          }
          //-----------------------------------------------------------------------------------------
          
          closeModal();
          // update-begin--author:liaozhiyang---date:20240611---for：【TV360X-1054】代码生成包名缓存
          localStorage.setItem(JEECG_ONL_PROJECT_NAME, values.entityPackage);
          // update-end--author:liaozhiyang---date:20240611---for：【TV360X-1054】代码生成包名缓存
        } catch (e) {
          console.error(e);
        } finally {
          confirmLoading.value = false;
        }
      }
      
       function codeGen(params){
        return new Promise((resolve, reject) => {
          defHttp.post({ url: Api.codeGenerate, params }, {isTransformResponse: false}).then(res=>{
            if(res.success){
              let codeList = res.result;
              let pathKey = res.message;
              resolve({
                codeList,
                pathKey
              })
            }else{
              message.error(res.message);
              reject(res.message)
            }
          })
        })
      }

      function onCancel() {
        closeModal();
      }

      // 注册文件选择弹窗
      const [registerFileSelectModal, fileSelectModal] = useModal();

      function onProjectPathSearch() {
        fileSelectModal.openModal(true, {});
      }

      function onFileSelect(url) {
        window.localStorage.setItem(JEECG_ONL_PROJECT_PATH, url);
        setFieldsValue({ projectPath: url });
      }

      function getCamelCase(val) {
        let temp = underLine2CamelCase(val);
        return temp.substring(0, 1).toUpperCase() + temp.substring(1);
      }

      function getStoreProjectPath() {
        let path = window.localStorage.getItem(JEECG_ONL_PROJECT_PATH);
        if (path) {
          metaModel.projectPath = path;
        }
      }

      function onProjectPathChange(e) {
        if (e.target.value) window.localStorage.setItem(JEECG_ONL_PROJECT_PATH, e.target.value);
      }

      return {
        title,
        modalWidth,
        confirmLoading,
        subTable,
        showSubTable,
        onSubmit,
        onCancel,
        onFileSelect,
        registerFileSelectModal,
        subTableRef,
        registerForm,
        registerModal,
        registerCodeFileListModal,
        wrapClassName
      };
    },
  });
</script>

<style lang="less">
@prefix-cls: ~'@{namespace}-code-generator-modal';
.@{prefix-cls} {
  .jeecg-basic-form .ant-input {font-size: 14px;}
}
</style>
