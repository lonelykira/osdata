package org.jeecg.modules.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: 指标可用国家VO(某指标有数据的国家清单条目)
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "指标可用国家")
public class SeriesCountryVO {

    /**国家/经济体代码(ISO3, 如 CHN)*/
    @Schema(description = "国家/经济体代码(ISO3, 如 CHN)")
    private String countryCode;

    /**国家简称(英文)*/
    @Schema(description = "国家简称(英文)")
    private String shortName;

    /**国家中文名*/
    @Schema(description = "国家中文名")
    private String countryNameCn;
}
