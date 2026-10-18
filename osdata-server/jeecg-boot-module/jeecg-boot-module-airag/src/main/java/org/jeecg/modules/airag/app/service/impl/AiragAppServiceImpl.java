package org.jeecg.modules.airag.app.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.airag.app.consts.AiAppConsts;
import org.jeecg.modules.airag.app.consts.Prompts;
import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.app.mapper.AiragAppMapper;
import org.jeecg.modules.airag.app.service.IAiragAppService;
import org.jeecg.modules.airag.app.vo.AiArticleWriteVersionVo;
import org.jeecg.modules.airag.app.vo.AppVariableVo;
import org.jeecg.modules.airag.common.consts.AiragConsts;
import org.jeecg.modules.airag.common.handler.AIChatParams;
import org.jeecg.modules.airag.common.handler.IAIChatHandler;
import org.jeecg.modules.airag.common.utils.AiragLocalCache;
import org.jeecg.modules.airag.common.vo.event.EventData;
import org.jeecg.modules.airag.common.vo.event.EventFlowData;
import org.jeecg.modules.airag.common.vo.event.EventMessageData;
import org.jeecg.modules.airag.llm.entity.AiragKnowledge;
import org.jeecg.modules.airag.llm.service.IAiragKnowledgeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Description: AI应用
 * @Author: jeecg-boot
 * @Date: 2025-02-26
 * @Version: V1.0
 */
@Slf4j
@Service
public class AiragAppServiceImpl extends ServiceImpl<AiragAppMapper, AiragApp> implements IAiragAppService {

	private static final String COPY_NAME_SUFFIX = "-复制";

    @Autowired
    IAIChatHandler aiChatHandler;

    @Autowired
    private IAiragKnowledgeService airagKnowledgeService;
    
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发布/取消发布应用，并维护分享令牌。
     * 发布时生成随机 shareToken；取消发布时清空。
     *
     * @param id 应用ID
     * @param release true=发布，false=取消发布
     * @return 发布成功时返回 shareToken；取消发布返回 null
     * @author scott
     * @since 2026-07-21 【issues/9787】应用级分享令牌
     */
    @Override
    public String releaseApp(String id, boolean release) {
        AssertUtils.assertNotEmpty("id必须填写", id);
        if (release) {
            String shareToken = java.util.UUID.randomUUID().toString().replace("-", "");
            boolean updated = this.lambdaUpdate()
                    .eq(AiragApp::getId, id)
                    .set(AiragApp::getStatus, AiAppConsts.STATUS_RELEASE)
                    .set(AiragApp::getShareToken, shareToken)
                    .update();
            if (!updated) {
                throw new JeecgBootBizTipException("发布失败，应用不存在或已被删除");
            }
            return shareToken;
        }
        boolean updated = this.lambdaUpdate()
                .eq(AiragApp::getId, id)
                .set(AiragApp::getStatus, AiAppConsts.STATUS_ENABLE)
                .set(AiragApp::getShareToken, null)
                .update();
        if (!updated) {
            throw new JeecgBootBizTipException("取消发布失败，应用不存在或已被删除");
        }
        return null;
    }

	/**
	 * 复制应用
	 *
	 * @param id 原应用ID
	 * @param currentTenantId 当前租户ID
	 * @return 新应用ID
	 * @author scott
	 * @since 2026-08-06 【LHZP-1512】AI应用增加复制功能
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public String copyApp(String id, String currentTenantId) {
		AssertUtils.assertNotEmpty("id必须填写", id);
		AiragApp sourceApp = this.getById(id);
		if (sourceApp == null) {
			throw new JeecgBootBizTipException("复制失败，应用不存在或已被删除");
		}
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && (oConvertUtils.isEmpty(sourceApp.getTenantId()) || !sourceApp.getTenantId().equals(currentTenantId))) {
			throw new JeecgBootBizTipException("复制AI应用失败，不能复制其他租户的AI应用！");
		}

		AiragApp copiedApp = new AiragApp();
		BeanUtils.copyProperties(sourceApp, copiedApp);
		copiedApp.setId(null)
				.setName(sourceApp.getName() + COPY_NAME_SUFFIX)
				.setCreateBy(null)
				.setCreateTime(null)
				.setUpdateBy(null)
				.setUpdateTime(null)
				.setSysOrgCode(null)
				.setStatus(AiAppConsts.STATUS_ENABLE)
				.setShareToken(null);
		if (!this.save(copiedApp)) {
			throw new JeecgBootBizTipException("复制应用失败");
		}
		return copiedApp.getId();
	}

    @Override
    public Object generatePrompt(String prompt, boolean blocking) {
        AssertUtils.assertNotEmpty("请输入提示词", prompt);
        List<ChatMessage> messages = Arrays.asList(new SystemMessage(Prompts.GENERATE_LLM_PROMPT), new UserMessage(prompt));

        AIChatParams params = new AIChatParams();
        params.setTemperature(0.8);
        params.setTopP(0.9);
        params.setPresencePenalty(0.1);
        params.setFrequencyPenalty(0.1);
        if(blocking){
            String promptValue = aiChatHandler.completionsByDefaultModel(messages, params);
            if (promptValue == null || promptValue.isEmpty()) {
                return Result.error("生成失败");
            }
            return Result.OK("success", promptValue);
        }else{
            //update-begin---author:wangshuai---date:2026-01-08---for: 将流式输出单独抽出去，变量和记忆也需要---
            return startSseChat(messages, params);
            //update-end---author:wangshuai---date:2026-01-08---for: 将流式输出单独抽出去，变量和记忆也需要---
        }
    }

    //update-begin---author:wangshuai---date:2026-01-05---for:【QQYUN-14479】增加一个开启记忆的按钮。下面为提示词和记忆，将记忆提示词单独拆分---
    @Override
    public Object generateMemoryByAppId(String variables, String memoryId, boolean blocking) {
        if(oConvertUtils.isEmpty(variables) && oConvertUtils.isEmpty(memoryId)){
            throw new JeecgBootBizTipException("请先添加变量或者记忆后再次重试！");
        }
        // 1. 解析变量列表，本地拼接变量使用指南（无 LLM 介入，结果确定）
        String variableGuide = "";
        if (oConvertUtils.isNotEmpty(variables)) {
            List<AppVariableVo> variableList = JSONArray.parseArray(variables, AppVariableVo.class);
            variableGuide = buildVariableGuide(variableList);
        }
        boolean hasMemory = oConvertUtils.isNotEmpty(memoryId);

        // 2. 没有记忆库 → 直接返回拼接好的变量指南
        if (!hasMemory) {
            if (oConvertUtils.isEmpty(variableGuide)) {
                throw new JeecgBootBizTipException("请先添加启用的变量或者记忆后再次重试！");
            }
            if (blocking) {
                return Result.OK("success", variableGuide);
            }
            return streamLocalGuideOnly(variableGuide);
        }

        // 3. 有记忆库 → 仅记忆库部分走 LLM，变量部分作为前置本地内容
        String memoryDescr = "";
        AiragKnowledge memory = airagKnowledgeService.getById(memoryId);
        if (memory != null && oConvertUtils.isNotEmpty(memory.getDescr())) {
            memoryDescr = "记忆库描述：" + memory.getDescr();
        }
        String memoryPrompt = Prompts.GENERATE_GUIDE_HEADER + String.format(Prompts.GENERATE_MEMORY_PART, memoryDescr);
        List<ChatMessage> messages = List.of(new UserMessage(memoryPrompt));
        AIChatParams params = new AIChatParams();
        params.setTemperature(0.7);

        // 4. 阻塞模式：拼接本地变量指南 + LLM 同步生成的记忆库指南
        if (blocking) {
            String memoryPart = aiChatHandler.completionsByDefaultModel(messages, params);
            if (memoryPart == null || memoryPart.isEmpty()) {
                return Result.error("生成失败");
            }
            StringBuilder combined = new StringBuilder();
            if (oConvertUtils.isNotEmpty(variableGuide)) {
                combined.append(variableGuide).append("\n");
            }
            combined.append(memoryPart);
            return Result.OK("success", combined.toString());
        }

        // 5. SSE 流式模式：先 emit 本地变量指南，再衔接 LLM 流式输出记忆库指南
        String localPrefix = oConvertUtils.isNotEmpty(variableGuide) ? variableGuide + "\n" : "";
        return startSseChatWithLocalPrefix(localPrefix, messages, params);
    }

    /**
     * 发送聊天
     * @param messages
     * @param params
     * @return
     */
    private SseEmitter startSseChat(List<ChatMessage> messages, AIChatParams params) {
        SseEmitter emitter = new SseEmitter(-0L);
        String requestId = UUIDGenerator.generate();
        startLLMStream(emitter, requestId, messages, params);
        return emitter;
    }

    /**
     * 在已有 SseEmitter 上启动 LLM 流式响应。
     *
     * <p>从原 startSseChat 中抽出可复用的子段，使前置本地内容（变量使用指南）能与 LLM 流式记忆库指南共用同一个 emitter 与 requestId。</p>
     */
    private void startLLMStream(SseEmitter emitter, String requestId, List<ChatMessage> messages, AIChatParams params) {
        TokenStream tokenStream = aiChatHandler.chatByDefaultModel(messages, params);
        /**
         * 是否正在思考
         */
        AtomicBoolean isThinking = new AtomicBoolean(false);
        // ai聊天响应逻辑
        tokenStream.onPartialResponse((String resMessage) -> {
                    // 兼容推理模型
                    if ("<think>".equals(resMessage)) {
                        isThinking.set(true);
                        resMessage = "> ";
                    }
                    if ("</think>".equals(resMessage)) {
                        isThinking.set(false);
                        resMessage = "\n\n";
                    }
                    if (isThinking.get()) {
                        if (null != resMessage && resMessage.contains("\n")) {
                            resMessage = "\n> ";
                        }
                    }
                    EventData eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE);
                    EventMessageData messageEventData = EventMessageData.builder()
                            .message(resMessage)
                            .build();
                    eventData.setData(messageEventData);
                    try {
                        String eventStr = JSONObject.toJSONString(eventData);
                        log.debug("[AI应用]接收LLM返回消息:{}", eventStr);
                        emitter.send(SseEmitter.event().data(eventStr));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .onCompleteResponse((responseMessage) -> {
                    // 记录ai的回复
                    AiMessage aiMessage = responseMessage.aiMessage();
                    FinishReason finishReason = responseMessage.finishReason();
                    String respText = aiMessage.text();
                    if (FinishReason.STOP.equals(finishReason) || null == finishReason) {
                        // 正常结束
                        EventData eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE_END);
                        try {
                            log.debug("[AI应用]接收LLM返回消息完成:{}", respText);
                            emitter.send(SseEmitter.event().data(eventData));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        closeSSE(emitter, eventData);
                    } else {
                        // 异常结束
                        log.error("调用模型异常:" + respText);
                        if (respText.contains("insufficient Balance")) {
                            respText = "大预言模型账号余额不足!";
                        }
                        EventData eventData = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR);
                        eventData.setData(EventFlowData.builder().success(false).message(respText).build());
                        closeSSE(emitter, eventData);
                    }
                })
                .onError((Throwable error) -> {
                    // sse
                    String errMsg = "调用大模型接口失败:" + error.getMessage();
                    log.error(errMsg, error);
                    EventData eventData = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR);
                    eventData.setData(EventFlowData.builder().success(false).message(errMsg).build());
                    closeSSE(emitter, eventData);
                })
                .start();
    }
    //update-end---author:wangshuai---date:2026-01-05---for:【QQYUN-14479】增加一个开启记忆的按钮。下面为提示词和记忆，将记忆提示词单独拆分---

    private static void closeSSE(SseEmitter emitter, EventData eventData) {
        try {
            // 发送完成事件
            emitter.send(SseEmitter.event().data(eventData));
        } catch (IOException e) {
            log.error("终止会话时发生错误", e);
        } finally {
            // 从缓存中移除emitter
            AiragLocalCache.remove(AiragConsts.CACHE_TYPE_SSE, eventData.getRequestId());
            // 关闭emitter
            emitter.complete();
        }
    }

    /**
     * 拼接「变量使用指南」（无需 LLM，全部本地拼接）。
     *
     * <p>结果以 Markdown 表格展示「变量名 / 变量描述 / 当前值」，「当前值」一列使用 {{变量名}} 占位符，
     * 运行时由系统提示词渲染替换为真实值。表格下方原文保留用户自定义的 action 行为指令，
     * 最后附带固定的「变量更新协议」，强约束 LLM 在新会话中不要把已有当前值当作新信息而误调用 update_variable。</p>
     *
     * @param variableList AI 应用的变量列表
     * @return 拼接好的变量使用指南文本；若没有启用的变量则返回空字符串
     */
    private String buildVariableGuide(List<AppVariableVo> variableList) {
        if (variableList == null || variableList.isEmpty()) {
            return "";
        }
        List<AppVariableVo> enabledVars = variableList.stream()
                .filter(v -> v.getEnable() == null || v.getEnable())
                .filter(v -> oConvertUtils.isNotEmpty(v.getName()))
                .toList();
        if (enabledVars.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("## 变量使用指南\n\n");
        sb.append("> ⚠️ **重要**：下表展示了当前用户已设置的变量值。若「当前值」一列非空，表示用户在历史交互中已设置过该变量，请直接采信并使用，**严禁**假装用户从未设置过而忽略或反问。\n\n");

        // 变量状态总览表格
        sb.append("| 变量名 | 变量描述 | 当前值 |\n");
        sb.append("| --- | --- | --- |\n");
        for (AppVariableVo var : enabledVars) {
            String name = var.getName();
            String desc = oConvertUtils.isNotEmpty(var.getDescription()) ? var.getDescription() : "-";
            // 防止 | 和换行破坏 markdown 表格结构
            desc = desc.replace("|", "\\|").replace("\n", " ");
            sb.append("| ").append(name).append(" | ").append(desc)
                    .append(" | {{").append(name).append("}} |\n");
        }
        sb.append("\n");

        // 用户自定义的 action 行为指令（原文保留，不改写以避免偏离用户原意）
        List<String> actions = new ArrayList<>();
        for (AppVariableVo var : enabledVars) {
            if (oConvertUtils.isNotEmpty(var.getAction())) {
                String action = var.getAction();
                String name = var.getName();
                try {
                    // 使用正则替换未被{{}}包裹的变量名
                    String regex = "(?<!\\{\\{)\\b" + Pattern.quote(name) + "\\b(?!\\}\\})";
                    action = action.replaceAll(regex, "{{" + name + "}}");
                } catch (Exception e) {
                    log.warn("变量名替换异常: name={}", name, e);
                }
                actions.add(action);
            }
        }
        if (!actions.isEmpty()) {
            sb.append("**行为指令**：\n");
            for (String act : actions) {
                sb.append(act).append("\n");
            }
            sb.append("\n");
        }

        // 变量更新协议（写死的强约束，防止 LLM 在新会话误调用 update_variable）
        String varNames = enabledVars.stream()
                .map(AppVariableVo::getName)
                .collect(Collectors.joining("、"));
        sb.append("**变量更新协议（必读）**：\n");
        sb.append("1. **何时调用 `update_variable`**：仅当用户在**本次对话中**主动提供了上述变量（")
                .append(varNames)
                .append("）的**新值**，且该新值与「变量状态总览」中的「当前值」**不一致**时，才必须立即调用 `update_variable`。\n");
        sb.append("2. **用户质疑/纠正场景必须更新**：若用户对表中已有的「当前值」明确表达质疑、否认或纠正（例如“我不是X，是Y”、“你记错了，我叫Z”、“现在应该改成…”、“这个值不对”），并给出**新值**，则**必须立即**调用 `update_variable` 以新值覆盖旧值——这正是合法的更新场景，不要因下文「严禁误触发」而拒绝执行。\n");
        sb.append("3. **严禁误触发**：\n");
        sb.append("   - 即使是**新会话刚开启**的第一轮对话，「变量状态总览」中已有的当前值也**不是新信息**——它来自历史会话/已存在的配置，**严禁**将其当作“用户刚刚告诉我的内容”而调用 `update_variable`。\n");
        sb.append("   - 用户未主动表达变更意图、也未对当前值提出质疑/纠正时，**严禁**主动调用。\n");
        sb.append("   - 同一变量在同一轮对话中已调用过、或新值与当前值相同时，**严禁**重复调用。\n");
        sb.append("4. **调用前自检三问**：(a) 是否用户本轮主动新提供，或对当前值提出了质疑/纠正？(b) 是否与「当前值」不同？(c) 本轮是否未调过？三者同时为是才允许调用。\n");

        return sb.toString();
    }

    /**
     * 仅本地内容的 SSE 流式输出（无 LLM 调用）。
     *
     * <p>用于「只有变量、无记忆库」场景：把后端拼接好的变量指南按行 emit 出去，
     * 然后发送 MESSAGE_END 关闭 SSE。运行在异步线程中以保持 controller 即时返回 emitter。</p>
     */
    private SseEmitter streamLocalGuideOnly(String localText) {
        SseEmitter emitter = new SseEmitter(-0L);
        String requestId = UUIDGenerator.generate();
        CompletableFuture.runAsync(() -> {
            try {
                emitGuideLines(emitter, requestId, localText);
                EventData endEvent = new EventData(requestId, null, EventData.EVENT_MESSAGE_END);
                closeSSE(emitter, endEvent);
            } catch (Exception e) {
                log.error("本地变量指南流式输出失败", e);
                EventData errEvent = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR);
                errEvent.setData(EventFlowData.builder().success(false).message(e.getMessage()).build());
                closeSSE(emitter, errEvent);
            }
        });
        return emitter;
    }

    /**
     * 前置本地内容 + 后续 LLM 流式的复合 SSE 输出。
     *
     * <p>用于「变量 + 记忆库」场景：先 emit 后端拼接好的变量指南，再启动 LLM 流式生成记忆库指南，
     * 共用同一个 SseEmitter 和 requestId，前端体验为连续流式输出。</p>
     */
    private SseEmitter startSseChatWithLocalPrefix(String localPrefix, List<ChatMessage> messages, AIChatParams params) {
        SseEmitter emitter = new SseEmitter(-0L);
        String requestId = UUIDGenerator.generate();
        CompletableFuture.runAsync(() -> {
            try {
                if (oConvertUtils.isNotEmpty(localPrefix)) {
                    emitGuideLines(emitter, requestId, localPrefix);
                }
                // 衔接 LLM 流式输出（共用 emitter / requestId，由 LLM 流的 onCompleteResponse 发送 MESSAGE_END）
                startLLMStream(emitter, requestId, messages, params);
            } catch (Exception e) {
                log.error("前置本地内容 + LLM 流式输出失败", e);
                EventData errEvent = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR);
                errEvent.setData(EventFlowData.builder().success(false).message(e.getMessage()).build());
                closeSSE(emitter, errEvent);
            }
        });
        return emitter;
    }

    /**
     * 按行 emit SSE 消息（不发送 MESSAGE_END，由调用方决定何时结束）。
     */
    private void emitGuideLines(SseEmitter emitter, String requestId, String text) throws IOException {
        String[] lines = text.split("\n", -1);
        int total = lines.length;
        for (int i = 0; i < total; i++) {
            String chunk = (i == total - 1) ? lines[i] : lines[i] + "\n";
            if (chunk.isEmpty()) {
                continue;
            }
            EventData eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE);
            EventMessageData messageEventData = EventMessageData.builder().message(chunk).build();
            eventData.setData(messageEventData);
            emitter.send(SseEmitter.event().data(JSONObject.toJSONString(eventData)));
        }
    }

    /**
     * 写作列表
     */
    @Override
    public List<AiArticleWriteVersionVo> listArticleWrite() {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String redisKey = StrUtil.format(AiAppConsts.ARTICLE_WRITER_KEY, loginUser.getUsername());
        Object data = redisTemplate.opsForValue().get(redisKey);
        if (data == null) {
            return new ArrayList<>();
        }
        List<AiArticleWriteVersionVo> aiWriteViewVoList = (List<AiArticleWriteVersionVo>) data;
        Collections.reverse(aiWriteViewVoList);
        return aiWriteViewVoList;
    }

    /**
     * 写作报错
     */
    @Override
    public void saveArticleWrite(AiArticleWriteVersionVo aiWriteVersionVo) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String redisKey = StrUtil.format(AiAppConsts.ARTICLE_WRITER_KEY, loginUser.getUsername());
        //先查看redis中是否存在
        Object data = redisTemplate.opsForValue().get(redisKey);
        if(null != data){
            List<AiArticleWriteVersionVo> aiWriteVersionVos = (List<AiArticleWriteVersionVo>) data;
            aiWriteVersionVo.setVersion("V"+(aiWriteVersionVos.size() + 1));
            aiWriteVersionVos.add(aiWriteVersionVo);
            redisTemplate.opsForValue().set(redisKey, aiWriteVersionVos);
        }else{
            List<AiArticleWriteVersionVo> aiWriteVersionVos = new ArrayList<>();
            aiWriteVersionVo.setVersion("V1");
            aiWriteVersionVos.add(aiWriteVersionVo);
            redisTemplate.opsForValue().set(redisKey, aiWriteVersionVos);
        }
    }

    /**
     * 写作删除
     */
    @Override
    public void deleteArticleWrite(String version) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String redisKey = StrUtil.format(AiAppConsts.ARTICLE_WRITER_KEY, loginUser.getUsername());
        Object data = redisTemplate.opsForValue().get(redisKey);
        if (data == null) {
            return;
        }
        List<AiArticleWriteVersionVo> aiWriteVersionVos = (List<AiArticleWriteVersionVo>) data;
        if (aiWriteVersionVos.isEmpty()) {
            return;
        }
        List<AiArticleWriteVersionVo> newList = aiWriteVersionVos.stream()
                .filter(vo -> !version.equals(vo.getVersion()))
                .collect(Collectors.toList());
        if (newList.isEmpty()) {
            redisTemplate.delete(redisKey);
        } else {
            redisTemplate.opsForValue().set(redisKey, newList);
        }
    }
}
