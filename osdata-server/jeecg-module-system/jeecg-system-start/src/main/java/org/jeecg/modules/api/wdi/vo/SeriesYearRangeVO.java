package org.jeecg.modules.api.wdi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: 指标可用年份范围VO(选中指标年份集合交集的最小/最大年份)
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "指标可用年份范围")
public class SeriesYearRangeVO {

    /**交集最小年份(用于前端年份选择框下边界, 交集为空时为null)*/
    @Schema(description = "交集最小年份(用于前端年份选择框下边界, 交集为空时为null)", example = "1960")
    private Integer minYear;

    /**交集最大年份(用于前端年份选择框上边界, 交集为空时为null)*/
    @Schema(description = "交集最大年份(用于前端年份选择框上边界, 交集为空时为null)", example = "2025")
    private Integer maxYear;
}
