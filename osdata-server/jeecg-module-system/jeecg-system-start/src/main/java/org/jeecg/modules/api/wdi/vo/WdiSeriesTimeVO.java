package org.jeecg.modules.api.wdi.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * @Description: WDI指标年份覆盖信息VO(面板数据查询返回, 来自t_wdi_series_time按指标聚合)
 * @Author: jeecg-boot
 * @Date:   2026-10-16
 * @Version: V1.0
 */
@Data
@Schema(description = "WDI指标年份覆盖信息(来自t_wdi_series_time按指标聚合)")
public class WdiSeriesTimeVO {

    /**指标编码*/
    @Schema(description = "指标编码")
    private String indicatorCode;

    /**指标名称(中文优先, 无中文名回退英文名)*/
    @Schema(description = "指标名称(中文优先, 无中文名回退英文名)")
    private String indicatorName;

    /**最早年份*/
    @Schema(description = "最早年份")
    private Integer minYear;

    /**最晚年份*/
    @Schema(description = "最晚年份")
    private Integer maxYear;

    /**年份覆盖说明(英文)*/
    @Schema(description = "年份覆盖说明(英文)")
    private String description;

    /**年份覆盖说明中文*/
    @Schema(description = "年份覆盖说明中文")
    private String descriptionCn;
}
