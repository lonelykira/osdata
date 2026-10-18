package org.jeecg.modules.airag.voice.vo;

import lombok.Data;

/**
 * 文生语音请求VO
 */
@Data
public class VoiceGenerateVo {
    /**
     * 待合成文本（必填）
     */
    private String content;
    /**
     * 声色，不传用yml默认值
     */
    private String voice;
    /**
     * 倍速，范围0.25~4.0
     */
    private Double speed;
    /**
     * 音量增益(dB)
     */
    private Double volume;
}
