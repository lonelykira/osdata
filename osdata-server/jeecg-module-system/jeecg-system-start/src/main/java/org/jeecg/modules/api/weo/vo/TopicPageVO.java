package org.jeecg.modules.api.weo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WEO主题分页VO
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@Schema(description = "WEO主题分页条目")
public class TopicPageVO {

    /**主题编码(即主题英文名, 与t_weo_indicator.topic_code对应)*/
    @Schema(description = "主题编码(即主题英文名, 与t_weo_indicator.topic_code对应)")
    private String topicCode;

    /**主题名称(英文)*/
    @Schema(description = "主题名称(英文)")
    private String topicEn;

    /**主题名称(中文)*/
    @Schema(description = "主题名称(中文)")
    private String topicCn;

    /**该主题下的指标数量*/
    @Schema(description = "该主题下的指标数量")
    private Long indicatorCount;
}
