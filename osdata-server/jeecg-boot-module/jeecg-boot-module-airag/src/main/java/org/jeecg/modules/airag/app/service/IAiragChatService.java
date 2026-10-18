package org.jeecg.modules.airag.app.service;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.airag.app.vo.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * ai聊天
 *
 * @author chenrui
 * @date 2025/2/25 13:36
 */
public interface IAiragChatService {

    /**
     * 发送消息
     *
     * @param chatSendParams
     * @return
     * @author chenrui
     * @date 2025/2/25 13:39
     */
    SseEmitter send(ChatSendParams chatSendParams);


    /**
     * 调试应用
     *
     * @param appDebugParams
     * @return
     * @author chenrui
     * @date 2025/2/28 10:49
     */
    SseEmitter debugApp(AppDebugParams appDebugParams);

    /**
     * 停止响应
     *
     * @param requestId
     * @return
     * @author chenrui
     * @date 2025/2/25 17:17
     */
    Result<?> stop(String requestId);

    /**
     * 获取所有对话
     *
     * @param appId
     * @return
     * @author chenrui
     * @date 2025/2/26 14:48
     */
    Result<?> getConversations(String appId);

    /**
     * 获取对话聊天记录
     *
     * @param conversationId
     * @param sessionType 类型
     * @return
     * @author chenrui
     * @date 2025/2/26 15:16
     */
    Result<?> getMessages(String conversationId, String sessionType);

    /**
     * 删除会话
     *
     * @param conversationId
     * @param sessionType
     * @return
     * @author chenrui
     * @date 2025/3/3 16:55
     */
    Result<?> deleteConversation(String conversationId, String sessionType);

    /**
     * 更新会话标题
     * @param updateTitleParams
     * @return
     * @author chenrui
     * @date 2025/3/3 17:02
     */
    Result<?> updateConversationTitle(ChatConversation updateTitleParams);

    /**
     * 清空消息
     * @param conversationId
     * @param sessionType
     * @return
     * @author chenrui
     * @date 2025/3/3 19:49
     */
    Result<?> clearMessage(String conversationId, String sessionType);

    /**
     * 初始化聊天(忽略租户)
     * [QQYUN-12113]分享之后的聊天，应用、模型、知识库不根据租户查询
     * @param appId
     * @param shareToken 分享令牌（匿名访问必填）
     * @return
     * @author chenrui
     * @date 2025/4/21 14:17
     */
    Result<AiragAppShareInfoVO> initChat(String appId, String shareToken);

    /**
     * 匿名分享访问校验（登录用户直接放行）。
     * 用于 upload 等匿名写接口，与 send/init 共用同一套规则。
     *
     * @param appId 应用ID
     * @param shareToken 分享令牌
     * @author scott
     * @since 2026-07-21 【issues/9787】匿名上传补分享令牌校验
     */
    void validateAnonymousShareAccess(String appId, String shareToken);

    /**
     * 继续接收消息
     * @param requestId
     * @return
     * @author chenrui
     * @date 2025/8/11 17:39
     */
    SseEmitter receiveByRequestId(String requestId);

    /**
     * 根据类型获取会话列表
     * 
     * @param sessionType
     * @return
     */
    Result<?> getConversationsByType(String sessionType);

    /**
     * 生成海报图片
     * @param aiDrawGenerateVo
     * @return
     */
    String genAiPoster(AiDrawGenerateVo aiDrawGenerateVo);

    //update-begin---author:wangshuai ---date:2026-04-15  for：【QQYUN-14568】AI海报生成改为异步，支持切换菜单后重新获取结果-----------
    /**
     * 异步生成海报图片，立即返回taskId
     * @param aiDrawGenerateVo
     * @return taskId
     */
    String genAiPosterAsync(AiDrawGenerateVo aiDrawGenerateVo);

    /**
     * 查询异步海报任务结果
     * @param taskId
     * @return Result，data为图片URL（成功）或status=pending/failed
     */
    Result<?> getAiPosterResult(String taskId);
    //update-end---author:wangshuai ---date:2026-04-15  for：【QQYUN-14568】AI海报生成改为异步，支持切换菜单后重新获取结果-----------

    /**
     * 生成ai创作
     *
     * @param chatSendParams
     * @return
     */
    SseEmitter genAiWriter(AiWriteGenerateVo chatSendParams);
}
