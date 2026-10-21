package org.jeecg.modules.api.weo.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WEO面板数据查询结果VO(按viewType填充对应视图数据, 其余视图字段为null)
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@Schema(description = "WEO面板数据查询结果(按viewType填充对应视图数据, 其余视图字段为null)")
public class WeoDataQueryVO {

    /**本次返回的视图类型: TABLE_LONG/TABLE_WIDE/TIME_SERIES/CHART*/
    @Schema(description = "本次返回的视图类型: TABLE_LONG/TABLE_WIDE/TIME_SERIES/CHART")
    private String viewType;

    /**实际返回的数据行数(底层数据点个数, 各视图通用)*/
    @Schema(description = "实际返回的数据行数(底层数据点个数, 各视图通用)")
    private Integer returnedRows;

    /**是否被截断(结果达到返回行数上限被截断, 建议缩小范围或使用异步导出)*/
    @Schema(description = "是否被截断(结果达到返回行数上限被截断, 建议缩小范围或使用异步导出)")
    private Boolean truncated;

    /**长表数据(viewType=TABLE_LONG时返回): 国家×指标×年份×数值*/
    @Schema(description = "长表数据(viewType=TABLE_LONG时返回): 国家×指标×年份×数值")
    private List<WeoDataLongRowVO> longRows;

    /**宽表数据(viewType=TABLE_WIDE时返回): 行=国家×指标, 列=年份, Java内存行转列透视*/
    @Schema(description = "宽表数据(viewType=TABLE_WIDE时返回): 行=国家×指标, 列=年份, Java内存行转列透视")
    private WeoDataWideVO wide;

    /**指标时序数据(viewType=TIME_SERIES时返回): 行=国家(每国家一行), 一级表头=指标, 二级表头=年份*/
    @Schema(description = "指标时序数据(viewType=TIME_SERIES时返回): 行=国家(每国家一行), 一级表头=指标, 二级表头=年份")
    private WeoDataTimeSeriesVO timeSeries;

    /**图表数据(viewType=CHART时返回): ECharts可直接使用的结构化数据(折线/柱状图)*/
    @Schema(description = "图表数据(viewType=CHART时返回): ECharts可直接使用的结构化数据(折线/柱状图)")
    private WeoDataChartVO chart;
}
