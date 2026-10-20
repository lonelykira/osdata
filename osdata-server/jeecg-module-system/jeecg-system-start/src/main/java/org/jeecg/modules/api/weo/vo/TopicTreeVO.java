package org.jeecg.modules.api.weo.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WEO主题树形目录VO(一级节点为Topic主题, 子节点为该主题下的指标列表)
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@Schema(description = "WEO主题树形目录节点")
public class TopicTreeVO {

    /**主题编码(即主题英文名, 与t_weo_indicator.topic_code对应)*/
    @Schema(description = "主题编码(即主题英文名, 与t_weo_indicator.topic_code对应)")
    private String topicCode;

    /**主题名称(英文)*/
    @Schema(description = "主题名称(英文)")
    private String topicEn;

    /**主题名称(中文)*/
    @Schema(description = "主题名称(中文)")
    private String topicCn;

    /**子节点: 该主题下的指标列表*/
    @Schema(description = "子节点: 该主题下的指标列表")
    private List<IndicatorNode> children;

    /**
     * 指标叶子节点
     */
    @Data
    @Schema(description = "主题下的指标节点")
    public static class IndicatorNode {

        /**指标代码(如 NGDP/PCPI)*/
        @Schema(description = "指标代码(如 NGDP/PCPI)")
        private String indicatorCode;

        /**指标名称(英文)*/
        @Schema(description = "指标名称(英文)")
        private String indicatorName;

        /**指标名称(中文)*/
        @Schema(description = "指标名称(中文)")
        private String indicatorNameCn;
    }
}
