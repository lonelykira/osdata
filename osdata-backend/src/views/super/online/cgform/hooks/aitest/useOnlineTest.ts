import { FormActionType, JCodeEditor } from '/@/components/Form';
import { ref } from 'vue';

export function useOnlineTest(data, methods, form: Nullable<FormActionType>) {
  // Online单元测试开关
  const aiTestMode = ref(false);
  const aiTestTable = ref([]);
  const aiTableList = ref([]);

  function initVirtualData() {
  }

  // 自定义按钮
  function genButtons(code) {
  }

  // 生成java增强
  function genEnhanceJavaData(code) {
  }

  // 生成js增强
  function genEnhanceJsData(tableName, type, codeEditor: InstanceType<typeof JCodeEditor>) {
  }

  // 自定义sql增强
  function genEnhanceSqlData(code, tableName) {
  }

  /**
   * 加载配置信息
   */
  function setTaleConfig() {
  }

  function tableJsonGetHelper(pickAfter) {
    console.log('表的配置信息', JSON.stringify(pickAfter));
    console.log('---------------------------------------');
  }

  /**
   * json 获取小助手
   * @param fields
   */
  function fieldsJsonGetHelper(fields) {
  }

  function refreshCacheTableName(oldValue, newValue) {
  }

  function getCacheTableName(name) {
  }

  // noinspection JSUnusedGlobalSymbols
  return {
    aiTestMode,
    aiTestTable,
    aiTableList,
    initVirtualData,
    genButtons,
    genEnhanceJavaData,
    genEnhanceJsData,
    genEnhanceSqlData,
    setTaleConfig,
    tableJsonGetHelper,
    fieldsJsonGetHelper,
    refreshCacheTableName,
    getCacheTableName,
  };
}
