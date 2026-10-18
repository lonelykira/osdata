package org.jeecg.modules.api.vo;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WDI面板图表数据VO(viewType=CHART)
 * ECharts折线/柱状图可直接使用: xAxis.data=years, 每条线/柱=一个国家×指标组合
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "WDI面板图表数据(ECharts可用: xAxis.data=years, series每条线=国家×指标组合)")
public class WdiDataChartVO {

    /**X轴年份(升序, 与各系列data按索引对齐)*/
    @Schema(description = "X轴年份(升序, 与各系列data按索引对齐)")
    private List<Integer> years;

    /**数据系列列表(每个国家×指标组合一条系列)*/
    @Schema(description = "数据系列列表(每个国家×指标组合一条系列)")
    private List<ChartSeries> seriesList;

    /**
     * 图表数据系列: 一个国家×指标组合的完整时间序列
     */
    @Data
    @Schema(description = "图表数据系列: 一个国家×指标组合的完整时间序列")
    public static class ChartSeries {

        /**系列展示名(指标名称-国家名称)*/
        @Schema(description = "系列展示名(指标名称-国家名称)")
        private String name;

        /**国家代码(ISO3, 如 CHN)*/
        @Schema(description = "国家代码(ISO3, 如 CHN)")
        private String countryCode;

        /**国家名称(中文名优先, 无中文名时返回英文简称)*/
        @Schema(description = "国家名称(中文名优先, 无中文名时返回英文简称)")
        private String countryName;

        /**指标编码(如 SP.POP.TOTL)*/
        @Schema(description = "指标编码(如 SP.POP.TOTL)")
        private String seriesCode;

        /**指标名称(中文名优先, 无中文名时返回英文名)*/
        @Schema(description = "指标名称(中文名优先, 无中文名时返回英文名)")
        private String indicatorName;

        /**系列值(与years按索引对齐, 缺失年份为null, ECharts自动断点)*/
        @Schema(description = "系列值(与years按索引对齐, 缺失年份为null, ECharts自动断点)")
        private List<BigDecimal> data;
    }
}
