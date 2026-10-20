package org.jeecg.modules.api.weo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WEO指标分页检索入参
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@Schema(description = "WEO指标分页检索入参")
public class IndicatorPageDTO {

    /**页码(默认1)*/
    @Schema(description = "页码(默认1)")
    private Integer pageNo = 1;

    /**每页条数(默认10, 最大100)*/
    @Schema(description = "每页条数(默认10, 最大100)")
    private Integer pageSize = 10;

    /**主题编码筛选(对应t_weo_indicator.topic_code, 即t_weo_topic.topic_en)*/
    @Schema(description = "主题编码筛选(对应t_weo_indicator.topic_code)")
    private String topicCode;

    /**关键词(模糊匹配指标中英文名称及指标代码)*/
    @Schema(description = "关键词(模糊匹配指标中英文名称及指标代码)")
    private String keyword;
}
