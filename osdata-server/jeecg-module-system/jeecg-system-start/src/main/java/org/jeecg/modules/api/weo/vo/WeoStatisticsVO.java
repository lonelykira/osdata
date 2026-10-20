package org.jeecg.modules.api.weo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WEO统计概览VO
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@Schema(description = "WEO统计概览")
public class WeoStatisticsVO {

    /**指标总数(t_weo_indicator)*/
    @Schema(description = "指标总数(t_weo_indicator)")
    private Long indicatorCount;

    /**国家/经济体总数(t_weo_country)*/
    @Schema(description = "国家/经济体总数(t_weo_country)")
    private Long countryCount;

    /**数据覆盖最小年份(t_weo_data聚合, 无数据时为null)*/
    @Schema(description = "数据覆盖最小年份(t_weo_data聚合, 无数据时为null)")
    private Integer minYear;

    /**数据覆盖最大年份(t_weo_data聚合, 无数据时为null)*/
    @Schema(description = "数据覆盖最大年份(t_weo_data聚合, 无数据时为null)")
    private Integer maxYear;

    /**数据记录总数(t_weo_data)*/
    @Schema(description = "数据记录总数(t_weo_data)")
    private Long dataCount;
}
