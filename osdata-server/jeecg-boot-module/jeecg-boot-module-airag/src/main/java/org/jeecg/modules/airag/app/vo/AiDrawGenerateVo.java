package org.jeecg.modules.airag.app.vo;

import lombok.Data;

/**
* @Description: AI绘画
*
* @author: wangshuai
* @date: 2026/2/4 18:57
*/
@Data
public class AiDrawGenerateVo {

    /**
     * 绘画模型的id
     */
    private String drawModelId;

    /**
     * 图片尺寸
     */
    private String imageSize;

    /**
     * 一张图片或者多张图片，多张图片用逗号分隔
     */
    private String imageUrl;

    /**
     * 用户输入的聊天内容
     */
    private String content;

    /**
     * 风格
     */
    private String style;

    /**
     * 视角
     */
    private String visualAngle;

    /**
     * 人物镜头
     */
    private String characterShot;

    /**
     * 灯光
     */
    private String lighting;

    /**
     * 类型 poster: 海报，draw：绘图，face 换脸，mix 混图 
     */
    private String type;
}
