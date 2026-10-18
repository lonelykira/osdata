package org.jeecg.modules.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: Topic主题分页查询入参DTO
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "Topic主题分页查询入参")
public class TopicPageDTO {

    /**页码, 默认1*/
    @Schema(description = "页码(默认1)", example = "1")
    private Integer pageNo = 1;

    /**每页条数, 默认10*/
    @Schema(description = "每页条数(默认10, 最大100)", example = "10")
    private Integer pageSize = 10;
}
