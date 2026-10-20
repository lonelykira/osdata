package org.jeecg.modules.api.weo.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WEO批量获取国家入参
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@Schema(description = "WEO批量获取国家入参")
public class CountryBatchDTO {

    /**国家/经济体编码列表(ISO3如 CHN/USA, 或聚合经济体编码)*/
    @Schema(description = "国家/经济体编码列表(ISO3如 CHN/USA, 或聚合经济体编码)", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> countryCodeList;
}
