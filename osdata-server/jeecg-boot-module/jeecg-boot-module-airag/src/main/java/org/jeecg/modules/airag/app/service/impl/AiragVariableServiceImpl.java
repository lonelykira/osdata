package org.jeecg.modules.airag.app.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.app.consts.AiAppConsts;
import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.app.service.IAiragVariableService;
import org.jeecg.modules.airag.app.vo.AppVariableVo;
import org.jeecg.modules.airag.common.handler.AIChatParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: AI应用变量服务实现
 * @Author: jeecg-boot
 * @Date: 2025-02-26
 * @Version: V1.0
 */
@Service
@Slf4j
public class AiragVariableServiceImpl implements IAiragVariableService {

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String CACHE_PREFIX = "airag:app:var:";

    /**
     * 初始化变量（仅不存在时设置）
     *
     * @param username
     * @param appId
     * @param name
     * @param defaultValue
     */
    @Override
    public void initVariable(String username, String appId, String name, String defaultValue) {
        if (oConvertUtils.isEmpty(username) || oConvertUtils.isEmpty(appId) || oConvertUtils.isEmpty(name)) {
            return;
        }
        String key = CACHE_PREFIX + appId + ":" + username;
        redisTemplate.opsForHash().putIfAbsent(key, name, defaultValue != null ? defaultValue : "");
    }

    /**
     * 获取变量值
     *
     * @param username 用户名
     * @param appId    应用ID
     * @param name     变量名
     * @return 变量值，不存在返回null
     */
    @Override
    public String getVariable(String username, String appId, String name) {
        if (oConvertUtils.isEmpty(username) || oConvertUtils.isEmpty(appId) || oConvertUtils.isEmpty(name)) {
            return null;
        }
        String key = CACHE_PREFIX + appId + ":" + username;
        Object value = redisTemplate.opsForHash().get(key, name);
        return value != null ? String.valueOf(value) : null;
    }

    /**
     * 追加提示词
     * 
     * @param username
     * @param app
     * @return
     */
    @Override
    public String additionalPrompt(String username, AiragApp app) {
        String memoryPrompt = app.getMemoryPrompt();
        String prompt = app.getPrompt();
        
        if (oConvertUtils.isEmpty(memoryPrompt)) {
            return prompt;
        }
        String variablesStr = app.getVariables();
        if (oConvertUtils.isEmpty(variablesStr)) {
            return prompt;
        }

        List<AppVariableVo> variableList = JSONArray.parseArray(variablesStr, AppVariableVo.class);
        if (variableList == null || variableList.isEmpty()) {
            return prompt;
        }

        String key = CACHE_PREFIX + app.getId() + ":" + username;
        Map<Object, Object> savedValues = redisTemplate.opsForHash().entries(key);

        for (AppVariableVo variable : variableList) {
            if (variable.getEnable() != null && !variable.getEnable()) {
                continue;
            }
            String name = variable.getName();
            String value = variable.getDefaultValue();

            // 优先使用Redis中的值
            if (savedValues.containsKey(name)) {
                Object savedVal = savedValues.get(name);
                if (savedVal != null) {
                    value = String.valueOf(savedVal);
                }
            }

            if (value == null) {
                value = "";
            }

            // 替换 {{name}}
            memoryPrompt = memoryPrompt.replace("{{" + name + "}}", value);
        }
        return prompt + "\n" + memoryPrompt;
    }

    /**
     * 更新变量值
     * 
     * @param userId
     * @param appId
     * @param name
     * @param value
     */
    @Override
    public void updateVariable(String userId, String appId, String name, String value) {
        if (oConvertUtils.isEmpty(userId) || oConvertUtils.isEmpty(appId) || oConvertUtils.isEmpty(name)) {
            return;
        }
        String key = CACHE_PREFIX + appId + ":" + userId;
        redisTemplate.opsForHash().put(key, name, value);
    }


    /**
     * 添加变量更新工具
     *
     * @param params
     * @param aiApp
     * @param username
     */
    @Override
    public void addUpdateVariableTool(AiragApp aiApp, String username, AIChatParams params) {
        if (params.getTools() == null) {
            params.setTools(new HashMap<>());
        }
        if (!AiAppConsts.IZ_OPEN_MEMORY.equals(aiApp.getIzOpenMemory())) {
            return;
        }
        // 构建变量描述信息
        String variablesStr = aiApp.getVariables();
        List<AppVariableVo> variableList = null;
        if (oConvertUtils.isNotEmpty(variablesStr)) {
            variableList = JSONArray.parseArray(variablesStr, AppVariableVo.class);
        }

        //工具描述
        //update-begin---author:wangshuai ---date:2026-04-21  for：【AI变量】支持批量更新变量，返回结构化结果避免LLM重复调用-----------
        StringBuilder descriptionBuilder = new StringBuilder("批量更新应用变量的值。请将本次对话中所有需要更新的变量一次性传入updates数组，无需多次调用。仅当变量新值与当前值确实不同时才调用本工具。");
        //update-end---author:wangshuai ---date:2026-04-21  for：【AI变量】支持批量更新变量，返回结构化结果避免LLM重复调用-----------
        if (variableList != null && !variableList.isEmpty()) {
            descriptionBuilder.append("\n\n可用变量列表：");
            for (AppVariableVo var : variableList) {
                if (var.getEnable() != null && !var.getEnable()) {
                    continue;
                }
                descriptionBuilder.append("\n- ").append(var.getName());
                if (oConvertUtils.isNotEmpty(var.getDescription())) {
                    descriptionBuilder.append(": ").append(var.getDescription());
                }
            }
            //update-begin---author:wangshuai ---date:2026-04-21  for：【AI变量】支持批量更新变量，返回结构化结果避免LLM重复调用-----------
            descriptionBuilder.append("\n\n注意：variableName必须是上述列表中的名称之一，且本工具每轮对话只需调用一次。");
            //update-end---author:wangshuai ---date:2026-04-21  for：【AI变量】支持批量更新变量，返回结构化结果避免LLM重复调用-----------
        }

        // A: 参数改为批量数组，一次可更新多个变量
        JsonObjectSchema itemSchema = JsonObjectSchema.builder()
                .addStringProperty("variableName", "变量名称（必须是可用变量列表中的名称之一）")
                .addStringProperty("value", "变量新值")
                .required("variableName", "value")
                .build();

        //构建更新变量的工具
        ToolSpecification spec = ToolSpecification.builder()
                .name("update_variable")
                .description(descriptionBuilder.toString())
                .parameters(JsonObjectSchema.builder()
                        //update-begin---author:wangshuai ---date:2026-04-21  for：【AI变量】支持批量更新变量，返回结构化结果避免LLM重复调用-----------
                        .addProperty("updates", JsonArraySchema.builder()
                                .description("需要更新的变量列表，可包含多个变量")
                                .items(itemSchema)
                                .build())
                        .required("updates")
                        //update-end---author:wangshuai ---date:2026-04-21  for：【AI变量】支持批量更新变量，返回结构化结果避免LLM重复调用-----------
                        .build())
                .build();

        //监听工具的调用
        ToolExecutor executor = (toolExecutionRequest, memoryId) -> {
            try {
                JSONObject args = JSONObject.parseObject(toolExecutionRequest.arguments());
                JSONArray updates = args.getJSONArray("updates");
                IAiragVariableService variableService = SpringContextUtils.getBean(IAiragVariableService.class);
                //update-begin---author:wangshuai ---date:2026-04-21  for：【AI变量】支持批量更新变量，返回结构化结果避免LLM重复调用-----------
                // B: 返回结构化JSON，LLM可明确感知"已全部完成"
                JSONObject updatedMap = new JSONObject();
                if (updates != null) {
                    for (int i = 0; i < updates.size(); i++) {
                        JSONObject item = updates.getJSONObject(i);
                        String name = item.getString("variableName");
                        String value = item.getString("value");
                        if (oConvertUtils.isNotEmpty(name)) {
                            variableService.updateVariable(username, aiApp.getId(), name, value);
                            updatedMap.put(name, value);
                        }
                    }
                }

                JSONObject result = new JSONObject();
                result.put("success", true);
                result.put("updated", updatedMap);
                result.put("count", updatedMap.size());
                result.put("message", "已成功更新 " + updatedMap.size() + " 个变量，无需再次调用");
                return result.toJSONString();
                //update-end---author:wangshuai ---date:2026-04-21  for：【AI变量】支持批量更新变量，返回结构化结果避免LLM重复调用-----------
            } catch (Exception e) {
                log.error("更新变量失败", e);
                //update-begin---author:wangshuai ---date:2026-04-21  for：【AI变量】支持批量更新变量，返回结构化结果避免LLM重复调用-----------
                JSONObject error = new JSONObject();
                error.put("success", false);
                error.put("message", "更新变量失败: " + e.getMessage());
                return error.toJSONString();
                //update-end---author:wangshuai ---date:2026-04-21  for：【AI变量】支持批量更新变量，返回结构化结果避免LLM重复调用-----------
            }
        };

        params.getTools().put(spec, executor);
    }
}
