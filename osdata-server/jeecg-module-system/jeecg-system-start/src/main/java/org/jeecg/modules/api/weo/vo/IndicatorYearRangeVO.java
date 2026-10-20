package org.jeecg.modules.api.weo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WEO指标可用年份范围VO(各指标年份区间取交集)
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@Schema(description = "WEO指标可用年份范围(各指标年份区间取交集, 交集为空时minYear/maxYear返回null)")
public class IndicatorYearRangeVO {

    /**交集最小年份*/
    @Schema(description = "交集最小年份")
    private Integer minYear;

    /**交集最大年份*/
    @Schema(description = "交集最大年份")
    private Integer maxYear;
}
