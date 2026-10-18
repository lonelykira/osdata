package org.jeecg.modules.airag.video.vo;

import lombok.Data;

/**
 * AI视频生成任务结果VO
 */
@Data
public class VideoTaskResultVo {
    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务状态: PROCESSING, SUCCESS, FAIL
     */
    private String status;

    /**
     * 视频下载URL（SUCCESS时有值）
     */
    private String videoUrl;

    /**
     * 视频封面URL（如有）
     */
    private String coverUrl;

    /**
     * 带配音的视频URL
     */
    private String voiceoverVideoUrl;

    /**
     * 旁白文案
     */
    private String narration;

    /**
     * 错误信息（FAIL时有值）
     */
    private String message;
}
