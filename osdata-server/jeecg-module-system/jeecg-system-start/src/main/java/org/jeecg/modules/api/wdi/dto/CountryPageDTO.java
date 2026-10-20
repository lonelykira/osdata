package org.jeecg.modules.api.wdi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: 国家分页检索入参DTO
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "国家分页检索入参")
public class CountryPageDTO {

    /**页码, 默认1*/
    @Schema(description = "页码(默认1)", example = "1")
    private Integer pageNo = 1;

    /**每页条数, 默认10*/
    @Schema(description = "每页条数(默认10, 最大100)", example = "10")
    private Integer pageSize = 10;

    /**名称关键词(可选, 模糊匹配国家英文简称及中文名)*/
    @Schema(description = "名称关键词(可选, 模糊匹配国家英文简称及中文名)", example = "China")
    private String keyword;
}
