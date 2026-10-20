package org.jeecg.modules.api.wdi.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: 指标可用年份范围查询入参DTO
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "指标可用年份范围查询入参")
public class SeriesYearRangeDTO {

    /**指标编码列表(选中指标的seriesCode数组)*/
    @Schema(description = "指标编码列表(选中指标的seriesCode数组)")
    private List<String> seriesCodeList;
}
