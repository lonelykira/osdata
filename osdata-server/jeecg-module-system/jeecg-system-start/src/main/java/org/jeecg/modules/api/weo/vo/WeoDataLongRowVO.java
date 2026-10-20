package org.jeecg.modules.api.weo.vo;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WEO面板数据长表行VO(viewType=TABLE_LONG)
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@Schema(description = "WEO面板数据长表行")
public class WeoDataLongRowVO {

    /**国家/经济体代码(ISO3如 CHN, 或聚合经济体编码)*/
    @Schema(description = "国家/经济体代码(ISO3如 CHN, 或聚合经济体编码)")
    private String countryCode;

    /**国家/经济体名称(中文名优先, 无中文名时返回英文名)*/
    @Schema(description = "国家/经济体名称(中文名优先, 无中文名时返回英文名)")
    private String countryName;

    /**指标编码(如 NGDP/PCPI)*/
    @Schema(description = "指标编码(如 NGDP/PCPI)")
    private String indicatorCode;

    /**指标名称(中文名优先, 无中文名时返回英文名)*/
    @Schema(description = "指标名称(中文名优先, 无中文名时返回英文名)")
    private String indicatorName;

    /**年份*/
    @Schema(description = "年份")
    private Integer year;

    /**指标数值*/
    @Schema(description = "指标数值")
    private BigDecimal value;
}
