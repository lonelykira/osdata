package org.jeecg.modules.api.weo.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WEO指标可用年份范围入参
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@Schema(description = "WEO指标可用年份范围入参")
public class IndicatorYearRangeDTO {

    /**指标编码列表(如 NGDP/PCPI)*/
    @Schema(description = "指标编码列表(如 NGDP/PCPI)", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> indicatorCodeList;
}
