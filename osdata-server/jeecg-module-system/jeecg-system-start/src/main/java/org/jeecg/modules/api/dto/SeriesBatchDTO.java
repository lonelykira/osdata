package org.jeecg.modules.api.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: 批量获取指标元信息入参DTO
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "批量获取指标元信息入参")
public class SeriesBatchDTO {

    @Schema(description = "指标编码列表")
    private List<String> seriesCodeList;
}
