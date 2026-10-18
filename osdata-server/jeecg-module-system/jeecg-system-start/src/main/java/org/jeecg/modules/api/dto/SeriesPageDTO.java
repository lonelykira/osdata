package org.jeecg.modules.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: 指标分页检索入参DTO
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "指标分页检索入参")
public class SeriesPageDTO {

    /**页码, 默认1*/
    @Schema(description = "页码(默认1)", example = "1")
    private Integer pageNo = 1;

    /**每页条数, 默认10*/
    @Schema(description = "每页条数(默认10, 最大100)", example = "10")
    private Integer pageSize = 10;

    /**主题筛选(可选, 对应t_wdi_series.topic, 即Topic树接口返回的topic值)*/
    @Schema(description = "主题筛选编码", example = "Health")
    private String topicCode;

    /**名称关键词(可选, 模糊匹配指标中英文名称及指标代码)*/
    @Schema(description = "名称关键词(可选, 模糊匹配指标中英文名称及指标代码)", example = "population")
    private String keyword;
}
