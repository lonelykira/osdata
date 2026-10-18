import { FormSchema } from '/@/components/Form';
import { duplicateCheckDelay } from '/@/views/system/user/user.api';
import { bindMapFormSchema } from '/@/utils/common/compUtils';
import { computed, ref } from 'vue';
import { usePermissionStore } from '/@/store/modules/permission';

// 弹窗表单
export function useFormSchemas(_, handler) {
  const permissionStore = usePermissionStore();
  // 由于需要动态改变布局，所以使用 computed
  type SpanType = 'one' | 'tow' | 'threeTow' | 'three';
  // 动态布局
  const mapFormSchema = bindMapFormSchema<SpanType>(
    {
      // 一列
      one: {
        colProps: { xs: 24, sm: 24 },
        itemProps: {
          labelCol: { xs: 24, sm: 2 },
          wrapperCol: { xs: 24, sm: 22 },
        },
      },
      // 两列
      tow: {
        colProps: { xs: 24, sm: 12 },
        itemProps: {
          labelCol: { xs: 24, sm: 4 },
          wrapperCol: { xs: 24, sm: 20 },
        },
      },
      // 三分之二列
      threeTow: {
        colProps: { xs: 24, sm: 16 },
        itemProps: {
          labelCol: { xs: 24, sm: 3 },
          wrapperCol: { xs: 24, sm: 21 },
        },
      },
      // 三列
      three: {
        colProps: { xs: 24, sm: 8 },
        itemProps: {
          labelCol: { xs: 24, sm: 6 },
          wrapperCol: { xs: 24, sm: 18 },
        },
      },
    },
    'three'
  );

  const dataType = ref('sql');
  const isCombination = ref('combination');
  // 根据不同的值展示不同的form
  const formInfo = {
    cgrSql: {
      sql: { label: '查询SQL', placeholder: '请输入查询SQL', language: 'sql' },
      json: { label: '数据JSON', placeholder: '请输入数据JSON', language: 'javascript' },
      api: { label: 'API接口', placeholder: '请输入API接口', language: 'javascript' },
    },
  };
  const cgrSqlFormInfo = computed<typeof formInfo.cgrSql.sql>(() => {
    return formInfo.cgrSql[dataType.value];
  });

  const formSchemas = computed<FormSchema[]>(() => [
    { label: 'ID', field: 'id', component: 'Input', show: false },
    mapFormSchema({
      label: '图表名称',
      field: 'name',
      component: 'Input',
      required: true,
    }),
    mapFormSchema({
      label: '编码',
      field: 'code',
      component: 'Input',
      dynamicRules({ model }) {
        return [
          { required: true, message: '请输入编码!' },
          {
            async validator({}, value) {
              if (/[\u4E00-\u9FA5]/g.test(value)) {
                return Promise.reject('编码不能为汉字');
              }
              let { success, message } = await duplicateCheckDelay({
                tableName: 'onl_graphreport_head',
                fieldName: 'code',
                fieldVal: value,
                dataId: model.id,
              });
              if (!success) {
                return Promise.reject(message);
              }
            },
          },
        ];
      },
    }),
    mapFormSchema({
      label: '展示模板',
      field: 'displayTemplate',
      component: 'Select',
      componentProps: {
        options: [
          { label: 'Tab风格', value: 'tab' },
          { label: '单排布局', value: 'single' },
          { label: '双排布局', value: 'double' },
        ],
      },
      defaultValue: 'tab',
    }),
    mapFormSchema({
      label: 'X轴字段',
      field: 'xaxisField',
      component: 'Input',
      required: true,
    }),
    mapFormSchema(
      {
        label: 'Y轴字段',
        field: 'yaxisField',
        component: 'JDictSelectTag',
        componentProps: {
          mode: 'tags',
          open: false,
          dictCode: 'online_graph_display_template',
        },
        required: true,
      },
      'threeTow'
    ),
    mapFormSchema({
      label: '数据类型',
      field: 'dataType',
      component: 'JDictSelectTag',
      componentProps: {
        dictCode: 'online_graph_data_type',
        showChooseOption: false,
        onChange: (value) => (dataType.value = value),
      },
      defaultValue: 'sql',
    }),
    mapFormSchema({
      label: '数据源',
      field: 'dbSource',
      component: 'Select',
      componentProps: {
        options: handler.dbSourceOptions.value,
      },
      rules: [{ required: permissionStore.sysSafeMode, message: '请选择数据源！' }],
      ifShow: ({ model }) => model.dataType === 'sql',
    }),
    mapFormSchema(
      {
        label: '图表类型',
        field: 'graphType',
        component: 'JDictSelectTag',
        componentProps: {
          mode: isCombination.value === 'single' ? 'default' : 'multiple',
          dictCode: 'online_graph_type',
          showChooseOption: false,
        },
        defaultValue: ['bar'],
      },
      dataType.value === 'sql' ? 'three' : 'threeTow'
    ),
    mapFormSchema(
      {
        label: '描述',
        field: 'content',
        component: 'Input',
      },
      'one'
    ),
    mapFormSchema(
      {
        label: cgrSqlFormInfo.value?.label,
        field: 'cgrSql',
        required: true,
        component: 'JCodeEditor',
        componentProps: {
          placeholder: cgrSqlFormInfo.value?.placeholder,
          language: cgrSqlFormInfo.value?.language,
          fullScreen: true,
          autoHeight: '!ie',
          height: '100px',
        },
        dynamicRules() {
          return [
            {
              required: true,
              // 根据数据类型的不同显示不同的错误信息
              message: cgrSqlFormInfo.value?.placeholder,
            },
            {
              // 自定义校验：校验JSON字符串格式是否正确
              async validator({}, value) {
                if (value && dataType.value === 'json') {
                  try {
                    JSON.parse(value);
                  } catch {
                    return Promise.reject('JSON格式不正确！');
                  }
                }
              },
            },
          ];
        },
      },
      'one'
    ),
    mapFormSchema(
      {
        label: ' ', // SQL解析
        field: 'cgrSql',
        component: 'Input',
        slot: 'SQLAnalyzeButton',
        itemProps: { colon: false },
        // ifShow: ({ model }) => model.dataType === 'sql',
      },
      'one'
    ),
    mapFormSchema(
      {
        label: 'JS增强',
        field: 'extendJs',
        component: 'JCodeEditor',
        componentProps: {
          placeholder: 'JS增强',
          language: 'javascript',
          fullScreen: true,
          autoHeight: '!ie',
          height: '100px',
        },
      },
      'one'
    ),
  ]);

  return { formSchemas, dataType, isCombination };
}
