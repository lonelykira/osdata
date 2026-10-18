package org.jeecg.modules.airag.voice.vo;

import lombok.Data;

/**
 * 文生语音响应VO
 */
@Data
public class VoiceResultVo {
    /**
     * 生成的音频文件相对路径
     */
    private String voiceUrl;
    /**
     * 文件名
     */
    private String fileName;
}
