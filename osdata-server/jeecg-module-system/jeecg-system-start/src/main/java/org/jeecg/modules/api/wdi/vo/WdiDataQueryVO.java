package org.jeecg.modules.api.wdi.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WDI面板数据查询结果VO(按viewType填充对应视图数据, 其余视图字段为null)
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "WDI面板数据查询结果(按viewType填充对应视图数据, 其余视图字段为null)")
public class WdiDataQueryVO {

    /**本次返回的视图类型: TABLE_LONG/TABLE_WIDE/CHART*/
    @Schema(description = "本次返回的视图类型: TABLE_LONG/TABLE_WIDE/CHART")
    private String viewType;

    /**实际返回的数据行数(底层数据点个数, 各视图通用)*/
    @Schema(description = "实际返回的数据行数(底层数据点个数, 各视图通用)")
    private Integer returnedRows;

    /**是否被截断(结果达到返回行数上限被截断, 建议缩小范围或使用异步导出)*/
    @Schema(description = "是否被截断(结果达到返回行数上限被截断, 建议缩小范围或使用异步导出)")
    private Boolean truncated;

    /**长表数据(viewType=TABLE_LONG时返回): 国家×指标×年份×数值*/
    @Schema(description = "长表数据(viewType=TABLE_LONG时返回): 国家×指标×年份×数值")
    private List<WdiDataLongRowVO> longRows;

    /**宽表数据(viewType=TABLE_WIDE时返回): 行=国家×指标, 列=年份, Java内存行转列透视*/
    @Schema(description = "宽表数据(viewType=TABLE_WIDE时返回): 行=国家×指标, 列=年份, Java内存行转列透视")
    private WdiDataWideVO wide;

    /**图表数据(viewType=CHART时返回): ECharts可直接使用的结构化数据(折线/柱状图)*/
    @Schema(description = "图表数据(viewType=CHART时返回): ECharts可直接使用的结构化数据(折线/柱状图)")
    private WdiDataChartVO chart;

    /**脚注列表(includeFootnote=true时返回): 来自t_wdi_footnote*/
    @Schema(description = "脚注列表(includeFootnote=true时返回): 来自t_wdi_footnote, 按国家+指标关联数据")
    private List<WdiDataFootnoteVO> footnotes;

    /**指标年份覆盖列表: 各指标的年份跨度与覆盖说明(来自t_wdi_series_time按指标聚合)*/
    @Schema(description = "指标年份覆盖列表: 各指标的年份跨度与覆盖说明(来自t_wdi_series_time按指标聚合)")
    private List<WdiSeriesTimeVO> seriesTimeList;
}
