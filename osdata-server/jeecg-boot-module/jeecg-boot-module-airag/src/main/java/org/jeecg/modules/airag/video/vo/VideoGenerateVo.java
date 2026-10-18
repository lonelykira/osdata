package org.jeecg.modules.airag.video.vo;

import lombok.Data;

/**
 * AI视频生成请求VO
 */
@Data
public class VideoGenerateVo {
    /**
     * 视频描述提示词
     */
    private String prompt;

    /**
     * 场景分类（通用演示/产品营销/教育培训/创意设计）
     */
    private String category;

    /**
     * 视频任务ID（用于添加配音时传入）
     */
    private String taskId;

    /**
     * 视频尺寸，如 "1920x1080"、"720x480"
     */
    private String size;

    /**
     * 视频帧率
     */
    private Integer fps;

    /**
     * 视频时长（秒）
     */
    private Integer duration;

    /**
     * 是否为ai合成音效
     */
    private Integer izAiAudio;
}
