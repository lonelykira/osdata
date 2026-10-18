import { ref, watch, computed, reactive, ExtractPropTypes } from 'vue';
import { router } from '/@/router';
import { cloneDeep } from 'lodash-es';
import { propTypes } from '/@/utils/propTypes';
import { printJS } from '/@/hooks/web/usePrintJS';
import { downloadByData } from '/@/utils/file/download';
import { filterDictText } from '/@/utils/dict/JDictSelectUtil';
import Bar from '/@/components/chart/Bar.vue';
import Pie from '/@/components/chart/Pie.vue';
import BarMulti from '/@/components/chart/BarMulti.vue';
import LineMulti from '/@/components/chart/LineMulti.vue';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { isFunction } from '/@/utils/is';
import { formatToDate } from '/@/utils/dateUtil';

export const ChartRenderProps = {
  // 图表标题
  title: propTypes.string,
  // 图表数据
  chartsData: propTypes.object,
  // 是否运行在组件模式
  asComponent: propTypes.bool.def(false),
};
type PropsType = ExtractPropTypes<typeof ChartRenderProps>;

export const ChartRenderEmits = ['error'];
export const ChartRenderComponents = {
  LineMulti,
  BarMulti,
  Pie,
  Bar,
};

export const ChartRenderCommon = {
  components: ChartRenderComponents,
  props: ChartRenderProps,
  emits: ChartRenderEmits,
};

const errorText = {
  jsonFormattingFailed: 'JSON字符串格式化失败',
};

export function useChartRender(props: PropsType, { emit }) {
  const {
    createMessage: $message,
    createConfirm: $confirm,
    createInfoModal: $info,
    createErrorModal: $error,
    createSuccessModal: $success,
    createWarningModal: $warning,
  } = useMessage();
  const headId = ref(null);
  // 图表的高度
  const height = ref('400px');
  // 当前显示的图表
  const activeKey = ref('bar');
  // 图表类型
  const chartTypes = ref<string[]>([]);
  // 是否开启分页
  const pageSwitch = ref(true);
  // 打印ID
  const printId = computed(() => `print-content-${headId.value}`);
  // 曲线图参数配置
  const lineParams = reactive({
    chartData: [] as Recordable[],
  });
  // 柱状图参数配置
  const barParams = reactive({
    chartData: [] as Recordable[],
  });
  // 饼图参数配置
  const pieParams = reactive({
    chartData: [] as Recordable[],
  });
  // 折柱图参数配置
  const barLineParams = reactive({
    dataSource: [] as Recordable[],
  });
  // 表格参数配置
  const tableParams = reactive({
    // 固定的列数据
    fixedColumns: [
      {
        title: '#',
        key: 'rowIndex',
        width: 60,
        align: 'center',
        customRender: function ({ record, index }) {
          if (record.isTotal === true) {
            return '总计';
          } else {
            return parseInt(index) + 1;
          }
        },
      },
    ],
    columns: [] as Recordable[],
    dataSource: [] as Recordable[],
  });
  // 用户JS增强的事件暂存处（通过headId隔离）
  const extendJsHandlerIsolation = reactive<Recordable>({});
  // 当前图表的JS增强
  const extendJsHandler = computed<Nullable<Recordable>>({
    get() {
      if (headId.value == null) {
        return null;
      } else {
        return extendJsHandlerIsolation[headId.value];
      }
    },
    set(obj) {
      if (headId.value != null) {
        extendJsHandlerIsolation[headId.value] = obj;
      }
    },
  });
  // 是否包含曲线图
  const hasLine = computed(() => chartTypes.value.includes('line'));
  // 是否包含柱状图
  const hasBar = computed(() => chartTypes.value.includes('bar'));
  // 是否包含饼图
  const hasPie = computed(() => chartTypes.value.includes('pie'));
  // 是否包含数据表格
  const hasTable = ref(false);
  // 曲线图参数
  const lineProps = computed(() => {
    return {
      type: 'line',
      height: height.value,
      chartData: lineParams.chartData,
      onClick(params) {
        // console.debug('lineProps-click: ', arguments)
        emitExtendJsEvent(params);
      },
    };
  });
  // 柱状图参数
  const barProps = computed(() => {
    return {
      height: height.value,
      chartData: barParams.chartData,
      onClick(params) {
        // console.debug('barProps-click: ', arguments)
        emitExtendJsEvent(params);
      },
    };
  });
  // 饼图参数
  const pieProps = computed(() => {
    return {
      height: height.value,
      chartData: pieParams.chartData,
      onClick(params) {
        // console.debug('pieProps-click: ', arguments)
        emitExtendJsEvent(params);
      },
    };
  });
  // 折柱图参数
  const barLineProps = computed(() => {
    return {
      height: height.value,
      dataSource: barLineParams.dataSource,
      onClick(_event, _chart) {
        console.debug('barLineProps-click: ', arguments);
      },
    };
  });
  // 图表区域 ACard 标签的固定属性
  const chartCardProps = computed(() => {
    return {
      title: props.title,
      headStyle: { paddingLeft: '20px' },
      bodyStyle: { padding: '10px' },
      bordered: !props.asComponent,
    };
  });
  // 数据表格区域 ACard 标签的固定属性
  const tableCardProps = computed(() => {
    return {
      title: '数据明细',
      headStyle: { paddingLeft: '20px' },
      bodyStyle: { padding: '0' },
      style: { marginTop: '20px' },
      bordered: !props.asComponent,
    };
  });
  // 导出按钮固定属性
  const exportButtonProps = computed(() => {
    return {
      type: 'primary',
      preIcon: 'ant-design:download',
      text: '导出',
      style: { margin: '12px' },
    };
  });
  /** 分页开关固定属性 */
  const pageSwitchProps = computed(() => {
    return {
      checkedChildren: '分页',
      unCheckedChildren: '分页',
      style: {
        position: 'absolute',
        top: '17px',
        right: '12px',
      },
    };
  });
  // 数据表格的固定属性
  const tableProps = computed(() => {
    // update-begin--author:liaozhiyang---date:20260720---for：【LHZP-1041】字段多时，表格内容挤压到一块了
    // 根据列宽总和计算横向滚动宽度，字段过多时表格可横向滚动而不是压缩换行
    let scrollX = 0;
    for (const col of tableParams.columns) {
      const w = parseInt(col.width);
      scrollX += isNaN(w) ? 150 : w;
    }
    // update-end--author:liaozhiyang---date:20260720---for：【LHZP-1041】字段多时，表格内容挤压到一块了
    return {
      size: 'middle',
      rowKey: 'id',
      // bordered: true,
      pagination: pageSwitch.value ? { pageSize: 10 } : false,
      columns: tableParams.columns,
      dataSource: tableParams.dataSource,
      scroll: { x: scrollX },
      style: { borderTop: '1px solid #e8e8e8' },
    };
  });
  // 是否显示打印按钮
  const showPrint = computed(() => !props.asComponent);
  // 是否显示详情按钮
  const showDetail = computed(() => props.asComponent);

  watch(
    () => props.chartsData,
    (data) => parseChartsData(data),
    { immediate: true }
  );

  /** 执行JS扩展 */
  function executeExtendJs(headId, jsCode) {
    if (!jsCode || !headId) {
      return;
    }
    let onClick = { line: null, bar: null, pie: null };
    // update-begin--author:liaozhiyang---date:20230904---for：【QQYUN-6390】eval替换成new Function，解决build警告
    // 执行JS增强
    new Function('onClick', 'headId', `${jsCode}`)(onClick, headId);
    // update-end--author:liaozhiyang---date:20230904---for：【QQYUN-6390】eval替换成new Function，解决build警告
    if (extendJsHandler.value == null) {
      extendJsHandler.value = { click: onClick };
    } else {
      extendJsHandler.value.click = onClick;
    }
  }

  // click 事件的 this 指向
  const onClickThis = {
    $router: router,
    $http: defHttp,
    $message,
    $confirm,
    $info,
    $error,
    $success,
    $warning,
  };

  /** 触发JS增强里定义的事件 */
  function emitExtendJsEvent(params) {
    if (extendJsHandler.value != null) {
      let clickType = params.seriesType;
      let fn: Fn = extendJsHandler.value.click[clickType];
      if (isFunction(fn)) {
        normalizeExtendJsEventParams(params);
        fn.call(onClickThis, params);
      }
    }
  }

  /** 字典字段点击时，JS增强使用原始值，显示文本通过 displayName 获取 */
  function normalizeExtendJsEventParams(params) {
    let rawName = params.data?.rawName;
    if (rawName === undefined && (params.seriesType === 'line' || params.seriesType === 'bar')) {
      const chartData = params.seriesType === 'line' ? lineParams.chartData : barParams.chartData;
      rawName = chartData.find((item) => item.type === params.seriesName && item.name === params.name)?.rawName;
    }
    if (rawName !== undefined) {
      params.displayName = params.name;
      params.rawName = rawName;
      params.name = rawName;
    }
  }

  /** 解析 chartData */
  function parseChartsData(chartsData) {
    if (chartsData == null) return null;
    let { head, data, items, dictOptions } = chartsData;
    if (head == null) return;
    let { id, xaxisField, yaxisField, dataType, cgrSql, graphType, extendJs } = head;
    headId.value = id;
    executeExtendJs(id, extendJs);
    try {
      data = dataType === 'sql' || dataType === 'api' ? data : JSON.parse(cgrSql);
    } catch {
      emit('error', errorText.jsonFormattingFailed);
      return;
    }
    let dictList = dictOptions[xaxisField];
    let graphTypes = graphType.split(',');
    activeKey.value = graphTypes[0];
    if (activeKey.value == 'table') {
      activeKey.value = graphTypes[1];
    }
    chartTypes.value = graphTypes;
    let yaxisFields: string[] = yaxisField.split(',');
    let fieldMap = new Map<string, Recordable>();
    items.forEach((item) => fieldMap.set(item.fieldName, item));
    // 判断是否定义了数据表格，如果定义了则删除该项，不参与动态渲染，只显示在最底部
    let index = graphTypes.indexOf('table');
    hasTable.value = index !== -1;
    if (hasTable.value) {
      graphTypes.splice(index, 1);
    }
    let parseOption = { graphTypes, data, items, fieldMap, xaxisField, yaxisFields, dictList, dictOptions };
    parseLineData(parseOption);
    parseBarData(parseOption);
    parsePicData(parseOption);
    parseTableData(parseOption);
  }

  type ParseDataOption = {
    graphTypes: string[];
    data: Recordable[];
    items: Recordable[];
    fieldMap: Map<string, Recordable>;
    xaxisField: string;
    yaxisFields: string[];
    dictList;
    dictOptions;
  };

  // 根据用户配置构造出通用数据
  function parseCommonData(option: ParseDataOption) {
    let { data, fieldMap, xaxisField, yaxisFields, dictList } = option;
    let chartData: Recordable[] = [];
    for (let yField of yaxisFields) {
      for (let item of data) {
        const rawName = item[xaxisField];
        let name = rawName;
        // 判断是否有字典
        if (dictList) {
          name = filterDictText(dictList, name);
        }
        chartData.push({
          name: name,
          rawName,
          value: item[yField],
          type: fieldMap.get(yField)?.fieldTxt || yField,
        });
      }
    }
    return chartData;
  }

  // 根据用户配置构造出 lineChartData
  function parseLineData(option: ParseDataOption) {
    let { graphTypes } = option;
    if (graphTypes.includes('line')) {
      lineParams.chartData = parseCommonData(option);
    }
  }

  // 根据用户配置构造出 barChartData
  function parseBarData(option: ParseDataOption) {
    let { graphTypes } = option;
    if (graphTypes.includes('bar')) {
      barParams.chartData = parseCommonData(option);
    }
  }

  // 根据用户配置构造出 pieChartData
  function parsePicData(option: ParseDataOption) {
    let { graphTypes, data, xaxisField, yaxisFields, dictList } = option;
    let yField = yaxisFields[0];
    if (graphTypes.includes('pie')) {
      let chartData: Recordable[] = [];
      for (let item of data) {
        const rawName = item[xaxisField];
        let name = rawName;
        // 判断是否有字典
        if (dictList) {
          name = filterDictText(dictList, name);
        }
        chartData.push({
          name: name,
          rawName,
          value: item[yField],
        });
      }
      pieParams.chartData = chartData;
    }
  }

  // 根据用户配置构造出 tableData
  function parseTableData(option: ParseDataOption) {
    let { data, items, xaxisField, yaxisFields, dictList, dictOptions } = option;
    if (hasTable.value) {
      tableParams.dataSource = data.map((item, index) => {
        item.id = index;
        let pieData = {
          item: item[xaxisField],
          count: item[yaxisFields[0]],
        };
        // 判断是否有字典
        if (dictList) {
          pieData.item = filterDictText(dictList, pieData.item);
        }
        return item;
      });
      // 根据用户配置构造出 tableColumns
      let tableColumns: Recordable[] = cloneDeep(tableParams.fixedColumns);
      let isTotals: string[] = [];
      items.forEach((item) => {
        if (item.isShow === 'Y') {
          let column: Recordable = {
            align: 'center',
            width: 150,
            ellipsis: true,
            title: item.fieldTxt,
            dataIndex: item.fieldName,
          };
          if (item.dictCode) {
            column.customRender = ({ text }) => filterDictText(dictOptions[item.fieldName], text);
          } else if (item.fieldType === 'Date') {
            column.customRender = ({ text, record }) => (record.isTotal || !text ? text : formatToDate(text));
          }
          tableColumns.push(column);
          // 判断是否计算总数
          if (item.isTotal === 'Y') isTotals.push(item.fieldName);
        }
      });
      tableParams.columns = tableColumns;
      // 如果有计算需要的值就进行计算
      if (isTotals.length > 0) {
        let totalRow = { id: tableParams.dataSource.length, isTotal: true };
        isTotals.forEach((column) => {
          let count = 0;
          tableParams.dataSource.forEach((row) => {
            // update-begin--author:liaozhiyang---date:20260805---for：【LHZP-1056】空字符串需过滤掉
            if (row[column]=='' || row[column]==null || isNaN(parseFloat(row[column]))) return;
            // update-end--author:liaozhiyang---date:20260805---for：【LHZP-1056】空字符串需过滤掉
            count += parseFloat(row[column]);
          });
          totalRow[column] = isNaN(count) ? '包含非数字内容' : count.toFixed(2);
        });
        tableParams.dataSource.push(totalRow);
      }
    }
  }

  // 导出Excel
  function onExportXls() {
    let fileName = props.title;
    defHttp
      .get(
        {
          url: '/online/graphreport/api/exportXlsById',
          params: {
            id: headId.value,
            name: fileName,
          },
          responseType: 'blob',
        },
        { isTransformResponse: false }
      )
      .then((data) => {
        if (!data || data.size == 0) {
          $message.warning('导出失败！');
          return;
        }
        downloadByData(data, fileName + '.xls');
      });
  }

  // 打印
  function onPrint() {
    printJS({
      type: 'html',
      printable: '#' + printId.value,
    });
  }

  /** 跳转到详情页 */
  function onGoToDetail() {
    goToInfo(props.chartsData);
  }

  function goToInfo(data) {
    let url = `/online/graphreport/chart/${data.head.id}`;
    router.push({ path: url });
  }

  return {
    headId,
    printId,
    height,
    activeKey,
    chartTypes,
    pageSwitch,
    showPrint,
    showDetail,
    hasLine,
    hasBar,
    hasPie,
    hasTable,
    lineProps,
    barProps,
    pieProps,
    tableProps,
    barLineProps,
    chartCardProps,
    tableCardProps,
    exportButtonProps,
    pageSwitchProps,
    extendJsHandlerIsolation,
    onPrint,
    onGoToDetail,
    onExportXls,
  };
}
