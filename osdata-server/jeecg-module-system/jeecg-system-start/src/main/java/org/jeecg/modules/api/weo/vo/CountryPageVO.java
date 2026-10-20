package org.jeecg.modules.api.weo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WEO国家/经济体信息VO(国家分页检索/批量获取国家/指标可用国家清单共用)
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@Schema(description = "WEO国家/经济体信息")
public class CountryPageVO {

    /**国家/经济体代码(ISO3如 CHN, 或聚合经济体编码)*/
    @Schema(description = "国家/经济体代码(ISO3如 CHN, 或聚合经济体编码)")
    private String countryCode;

    /**名称(英文)*/
    @Schema(description = "名称(英文)")
    private String countryEn;

    /**名称(中文)*/
    @Schema(description = "名称(中文)")
    private String countryCn;

    /**是否聚合经济体: 0=单个国家 1=聚合经济体/区域分组*/
    @Schema(description = "是否聚合经济体: 0=单个国家 1=聚合经济体/区域分组")
    private Integer isAggregate;
}
