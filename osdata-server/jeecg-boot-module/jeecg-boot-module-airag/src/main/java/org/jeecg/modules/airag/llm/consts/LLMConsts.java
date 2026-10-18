package org.jeecg.modules.airag.llm.consts;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @Description: airag模型常量类
 * @Author: chenrui
 * @Date: 2025/2/12 17:35
 */
public class LLMConsts {


    /**
     * 正则表达式:是否是网页
     */
    public static final Pattern WEB_PATTERN = Pattern.compile("^(http|https)://.*");

    /**
     * 状态:启用
     */
    public static final String STATUS_ENABLE = "enable";
    /**
     * 状态:禁用
     */
    public static final String STATUS_DISABLE = "disable";


    /**
     * 模型类型:向量
     */
    public static final String MODEL_TYPE_EMBED = "EMBED";

    /**
     * 模型类型:聊天
     */
    public static final String MODEL_TYPE_LLM = "LLM";

    /**
     * 模型类型: 图像生成
     */
    public static final String MODEL_TYPE_IMAGE = "IMAGE";

	/**
	 * OpenAI兼容协议供应商
	 */
	public static final String MODEL_PROVIDER_OPENAI = "OPENAI";
	public static final String MODEL_PROVIDER_KIMI = "KIMI";
	public static final String MODEL_PROVIDER_MINIMAX = "MINIMAX";
	public static final String MODEL_PROVIDER_VOLCENGINE = "VOLCENGINE";

	/**
	 * 将OpenAI兼容供应商转换为模型工厂支持的协议类型
	 *
	 * @param provider 供应商
	 * @return 模型工厂协议类型
	 */
	public static String normalizeLlmProvider(String provider) {
		if (MODEL_PROVIDER_KIMI.equalsIgnoreCase(provider)
				|| MODEL_PROVIDER_MINIMAX.equalsIgnoreCase(provider)
				|| MODEL_PROVIDER_VOLCENGINE.equalsIgnoreCase(provider)) {
			return MODEL_PROVIDER_OPENAI;
		}
		return provider;
	}

    /**
     * 向量模型：默认维度
     */
    public static final Integer EMBED_MODEL_DEFAULT_DIMENSION = 1536;

    /**
     * 知识库:文档状态:草稿
     */
    public static final String KNOWLEDGE_DOC_STATUS_DRAFT = "draft";
    /**
     * 知识库:文档状态:构建中
     */
    public static final String KNOWLEDGE_DOC_STATUS_BUILDING = "building";
    /**
     * 知识库:文档状态:构建完成
     */
    public static final String KNOWLEDGE_DOC_STATUS_COMPLETE = "complete";
    /**
     * 知识库:文档状态:构建失败
     */
    public static final String KNOWLEDGE_DOC_STATUS_FAILED = "failed";

    /**
     * 知识库:文档类型:文本
     */
    public static final String KNOWLEDGE_DOC_TYPE_TEXT = "text";
    /**
     * 知识库:文档类型:文件
     */
    public static final String KNOWLEDGE_DOC_TYPE_FILE = "file";
    /**
     * 知识库:文档类型:网页
     */
    public static final String KNOWLEDGE_DOC_TYPE_WEB = "web";

    /**
     * 知识库:文档元数据:文件路径
     */
    public static final String KNOWLEDGE_DOC_METADATA_FILEPATH = "filePath";

    /**
     * 知识库:文档元数据:资源路径
     */
    public static final String KNOWLEDGE_DOC_METADATA_SOURCES_PATH = "sourcesPath";

    /**
     * 知识库:文档元数据:网页URL
     */
    public static final String KNOWLEDGE_DOC_METADATA_WEBSITE = "website";

    /**
     * DEEPSEEK推理模型
     */
    public static final String DEEPSEEK_REASONER = "deepseek-reasoner";

    //update-begin---author:scott ---date:20260429  for：[issues/9585]DeepSeek大模型切换为新发布deepseek-v4-flash，流程中调用出现异常------------
    /**
     * DEEPSEEK 推理模型(返回 reasoning_content 字段、在多轮工具调用中要求把 reasoning_content 回传)集合，
     * 后续 DeepSeek 新增推理模型时在此追加；非推理模型(如 deepseek-chat)不要加入。
     * 触发场景：仅当对话存在工具调用导致的多轮请求时才会出现 "reasoning_content must be passed back" 错误，
     * 单轮 Q&A(如 AI 应用聊天无工具)不会触发，但开启 sendThinking 也无副作用。
     */
    public static final Set<String> DEEPSEEK_THINKING_MODELS = new HashSet<>(Arrays.asList(
            "deepseek-reasoner",
            "deepseek-v4-flash",
            "deepseek-v4-pro"
    ));

    /**
     * 判断指定模型名是否为 DeepSeek 推理模型(返回 reasoning_content 字段)
     * 匹配规则：先做大小写不敏感的精确匹配，再做关键字包含匹配(reasoner/v4-flash/v4-pro)
     * 以兼容带版本后缀的变体(如 deepseek-v4-flash-0428)
     *
     * @param modelName 模型名(大小写不敏感、首尾空白容错)
     * @return true=推理模型；false=非推理模型或空
     */
    public static boolean isDeepSeekThinkingModel(String modelName) {
        if (modelName == null || modelName.trim().isEmpty()) {
            return false;
        }
        String name = modelName.trim().toLowerCase();
        if (DEEPSEEK_THINKING_MODELS.contains(name)) {
            return true;
        }
        // 兼容带版本后缀或厂商前缀的变体
        return name.contains("reasoner")
                || name.contains("v4-flash")
                || name.contains("v4-pro");
    }
    //update-end---author:scott ---date:20260429  for：[issues/9585]DeepSeek大模型切换为新发布deepseek-v4-flash，流程中调用出现异常------------

    //update-begin---author:wangshuai ---date:2026-06-29  for：【issues/9727】不支持Tool Calling的模型（如deepseek-r1系列）发送工具调用时报错-----------
    /**
     * 判断指定模型是否不支持 Tool Calling（工具调用/函数调用）。
     * 包括：
     * - deepseek-r1 系列（Ollama 命名：deepseek-r1、deepseek-r1:14b、deepseek-r1:7b 等）
     * - deepseek-reasoner（DeepSeek API 命名）
     * - 其他已知不支持 tools 的推理模型，后续在此追加
     *
     * @param modelName 模型名（大小写不敏感、首尾空白容错，支持 Ollama 的 name:tag 格式）
     * @return true=不支持工具调用；false=支持或未知
     */
    public static boolean isToolCallingUnsupported(String modelName) {
        if (modelName == null || modelName.trim().isEmpty()) {
            return false;
        }
        String name = modelName.trim().toLowerCase();
        //去掉 :tag 部分（如 deepseek-r1:14b → deepseek-r1）
        String baseName = name.contains(":") ? name.substring(0, name.indexOf(":")) : name;
        return "deepseek-reasoner".equals(baseName)
                || "deepseek-r1".equalsIgnoreCase(baseName)
                || baseName.startsWith("deepseek-r1-");
    }
    //update-end---author:wangshuai ---date:2026-06-29  for：【issues/9727】不支持Tool Calling的模型（如Ollama的deepseek-r1系列）发送工具调用时报错-----------

    //update-begin---author:claude ---date:2026-08-07  for：Kimi k3 等模型采样参数需固定(temperature=1/topP=0.95/presencePenalty=0/frequencyPenalty=0)，传其他值报 "invalid temperature: only 1 is allowed for this model"-----------
    /**
     * 采样参数需固定的模型集合。
     * 这类模型（如 Kimi k3）请求中 temperature 不为 1 就直接 400 拒绝：
     * {"error":{"message":"invalid temperature: only 1 is allowed for this model"}}
     * 且官方推荐固定 temperature=1、topP=0.95、presencePenalty=0、frequencyPenalty=0。
     * 后续新增同类模型时在此追加。
     */
    public static final Set<String> FIXED_SAMPLING_PARAM_MODELS = new HashSet<>(Arrays.asList(
            "k3"
    ));

    /**
     * 判断指定模型的采样参数是否需要固定（temperature=1/topP=0.95/presencePenalty=0/frequencyPenalty=0）。
     * 匹配规则：大小写不敏感，先精确匹配，再兼容 "-" 后缀变体（如 k3-256、k3-256k），
     * 同时兼容厂商前缀（如 moonshot/k3）和 Ollama 的 name:tag 格式
     *
     * @param modelName 模型名（大小写不敏感、首尾空白容错）
     * @return true=采样参数需固定；false=无此限制或未知
     */
    public static boolean isFixedSamplingParamModel(String modelName) {
        if (modelName == null || modelName.trim().isEmpty()) {
            return false;
        }
        String name = modelName.trim().toLowerCase();
        // 去掉厂商前缀（如 moonshot/k3 → k3）
        if (name.contains("/")) {
            name = name.substring(name.lastIndexOf("/") + 1);
        }
        // 去掉 :tag 部分（如 k3:latest → k3）
        if (name.contains(":")) {
            name = name.substring(0, name.indexOf(":"));
        }
        if (FIXED_SAMPLING_PARAM_MODELS.contains(name)) {
            return true;
        }
        // 兼容带 "-" 后缀的变体（如 k3-256、k3-256k）
        for (String fixedModel : FIXED_SAMPLING_PARAM_MODELS) {
            if (name.startsWith(fixedModel + "-")) {
                return true;
            }
        }
        return false;
    }
    //update-end---author:claude ---date:2026-08-07  for：Kimi k3 等模型采样参数需固定(temperature=1/topP=0.95/presencePenalty=0/frequencyPenalty=0)，传其他值报 "invalid temperature: only 1 is allowed for this model"-----------

    /**
     * 知识库类型：知识库
     */
    public static final String KNOWLEDGE_TYPE_KNOWLEDGE = "knowledge";
    
    /**
     * 知识库类型：记忆库
     */
    public static final String KNOWLEDGE_TYPE_MEMORY = "memory";

    /**
     * 支持文件的后缀
     */
    public static final Set<String> CHAT_FILE_EXT_WHITELIST = new HashSet<>(Arrays.asList("txt", "pdf", "docx", "doc", "pptx", "ppt", "xlsx", "xls", "md"));

    /**
     * 文件内容最大长度
     */
    public static final int CHAT_FILE_TEXT_MAX_LENGTH = 20000;

    /**
     * 上传文件对打数量
     */
    public static final int CHAT_FILE_MAX_COUNT = 3;

    /**
     * 知识库是否开启默认分段策略
     */
    public static final String ENABLE_SEGMENT = "enableSegment";

    /**
     * 文档分段策略：使用知识库默认分段策略
     */
    public static final String USE_KNOWLEDGE_DEFAULT = "useKnowledgeDefault";

    /**
     * 分段策略
     */
    public static final String SEGMENT_STRATEGY = "segmentStrategy";
    
    /**
     * 分段策略：auto 自动分段与清洗
     */
    public static final String SEGMENT_STRATEGY_AUTO = "auto";
    
    /**
     * 分段策略：custom 自定义
     */
    public static final String SEGMENT_STRATEGY_CUSTOM = "custom";

    /**
     * 分段长度
     */
    public static final String MAX_SEGMENT = "maxSegment";

    /**
     * 重叠率 0-90%
     */
    public static final String OVERLAP = "overlap";
    
    /**
     * 分段标识符(\\n:换行，\\n\\n:2个换行，。:中文句号，！:中文叹号，？:中文问号，. :英文句号，! :英文叹号，? :英文问号，custom:自定义)
     */
    public static final String SEPARATOR = "separator";
    
    /**
     * 分段标识符自定义
     */
    public static final String CUSTOM_SEPARATOR = "customSeparator";

    /**
     * 文本预处理规则（cleanSpaces：替换掉连续的空格、换行符和制表符，removeUrlsEmails：删除所有 URL 和电子邮箱地址）
     */
    public static final String TEXT_RULES = "textRules";

    /**
     * 替换掉连续的空格、换行符和制表符
     */
    public static final String TEXT_RULES_CLEAN_SPACES = "cleanSpaces";

    /**
     * 删除所有URL和电子邮箱地址
     */
    public static final String TEXT_RULES_REMOVE_URLS_EMAILS = "removeUrlsEmails";
}
