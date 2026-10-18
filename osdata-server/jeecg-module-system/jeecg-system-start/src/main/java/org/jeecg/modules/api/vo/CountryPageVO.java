package org.jeecg.modules.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: 国家信息VO(国家分页检索/批量获取国家信息共用)
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "国家信息")
public class CountryPageVO {

    /**国家/经济体代码(ISO3, 如 CHN)*/
    @Schema(description = "国家/经济体代码(ISO3, 如 CHN)")
    private String countryCode;

    /**国家简称(英文)*/
    @Schema(description = "国家简称(英文)")
    private String shortName;

    /**国家中文名*/
    @Schema(description = "国家中文名")
    private String countryNameCn;

    /**区域(英文)*/
    @Schema(description = "区域(英文)")
    private String region;

    /**区域(中文)*/
    @Schema(description = "区域(中文)")
    private String regionCn;

    /**收入分组(英文)*/
    @Schema(description = "收入分组(英文)")
    private String incomeGroup;

    /**收入分组(中文)*/
    @Schema(description = "收入分组(中文)")
    private String incomeGroupCn;
}
