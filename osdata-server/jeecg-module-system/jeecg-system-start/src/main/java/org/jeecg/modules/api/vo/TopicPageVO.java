package org.jeecg.modules.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: Topic主题分页列表VO
 * @Author: jeecg-boot
 * @Date:   2026-10-15
 * @Version: V1.0
 */
@Data
@Schema(description = "Topic主题分页记录")
public class TopicPageVO {

    /**主题名称(英文)*/
    @Schema(description = "主题名称(英文)")
    private String topic;

    /**主题名称(中文)*/
    @Schema(description = "主题名称(中文)")
    private String topicCn;

    /**该主题下的指标数量*/
    @Schema(description = "该主题下的指标数量")
    private Long indicatorCount;
}
