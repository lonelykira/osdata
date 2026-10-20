package org.jeecg.modules.api.wdi.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: 批量获取国家信息入参DTO
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "批量获取国家信息入参")
public class CountryBatchDTO {

    @Schema(description = "国家编码列表")
    private List<String> countryCodeList;
}
