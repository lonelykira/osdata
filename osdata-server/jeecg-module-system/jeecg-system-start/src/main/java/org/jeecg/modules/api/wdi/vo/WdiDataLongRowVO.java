package org.jeecg.modules.api.wdi.vo;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WDI面板数据长表行VO(viewType=TABLE_LONG)
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "WDI面板数据长表行")
public class WdiDataLongRowVO {

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

    /**年份*/
    @Schema(description = "年份")
    private Integer year;

    /**指标数值*/
    @Schema(description = "指标数值")
    private BigDecimal value;
}
