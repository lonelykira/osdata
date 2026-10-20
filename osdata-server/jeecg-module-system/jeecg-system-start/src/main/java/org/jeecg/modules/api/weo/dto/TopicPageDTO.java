package org.jeecg.modules.api.weo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WEO主题分页入参
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@Schema(description = "WEO主题分页入参")
public class TopicPageDTO {

    /**页码(默认1)*/
    @Schema(description = "页码(默认1)")
    private Integer pageNo = 1;

    /**每页条数(默认10, 最大100)*/
    @Schema(description = "每页条数(默认10, 最大100)")
    private Integer pageSize = 10;
}
