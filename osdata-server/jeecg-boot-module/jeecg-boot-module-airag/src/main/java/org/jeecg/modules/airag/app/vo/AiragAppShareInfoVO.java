package org.jeecg.modules.airag.app.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: AI应用分享信息（/airag/chat/init 返回给聊天页的视图对象）。
 * 只暴露前端聊天页必需的字段，避免泄露 prompt、tenantId、modelId 等内部配置。
 * @author scott
 * @since 2026-07-21 【issues/9787】init接口返回最小化VO，避免泄露prompt等内部配置
 */
@Data
public class AiragAppShareInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用ID（send 时回传）
     */
    private String id;

    /**
     * 分享令牌（send 时回传，匿名访问必填）
     *
     * @author scott
     * @since 2026-07-21 【issues/9787】分享VO回传shareToken
     */
    private String shareToken;

    /**
     * 应用名称（聊天页标题）
     */
    private String name;

    /**
     * 应用描述
     */
    private String descr;

    /**
     * 应用图标
     */
    private String icon;

    /**
     * 应用类型（chat / chatFlow）
     */
    private String type;

    /**
     * 开场白
     */
    private String prologue;

    /**
     * 预设问题
     */
    private String presetQuestion;

    /**
     * 快捷指令
     */
    private String quickCommand;

    /**
     * 元数据（不原样透传，仅保留白名单 key 重新组装：
     * flowInputs、multiSession、izDraw、defaultSelect、drawModelId、modelInfo）
     */
    private String metadata;
}
