package org.jeecg.modules.airag.app.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.common.handler.AIChatParams;
import org.jeecg.modules.airag.common.handler.IAIChatHandler;
import org.jeecg.modules.airag.llm.consts.LLMConsts;
import org.jeecg.modules.airag.llm.entity.AiragMcp;
import org.jeecg.modules.airag.llm.mapper.AiragMcpMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI应用图片生成工具构建器。(AI应用配置的绘画模型 做成工具注入到应用中)
 *
 * @author scott
 * @since 2026-08-10 AI应用支持智能识别和图文混合生成
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageGenerationToolBuilder {

	private static final String TOOL_NAME = "generate_images";
	private static final String DRAW_ENABLED = "1";
	private static final String METADATA_DRAW_ENABLED = "izDraw";
	private static final String METADATA_DRAW_MODEL_ID = "drawModelId";
	private static final int DEFAULT_IMAGE_COUNT = 1;
	private static final int MAX_IMAGE_COUNT = 4;
	private static final String IMAGE_TYPE_REGEX = "(?:图片|图像|配图|插图|海报|照片|插画)";
	private static final String IMAGE_QUANTITY_REGEX = "(?:张(?:\\s*" + IMAGE_TYPE_REGEX + ")?|个\\s*" + IMAGE_TYPE_REGEX + ")";
	private static final Pattern EXPLICIT_IMAGE_REQUEST_PATTERN = Pattern.compile(
			"(?:生成|制作|创建|绘制|画|设计|添加|配).{0,20}" + IMAGE_TYPE_REGEX
					+ "|(?:\\d+|[一二两三四])\\s*" + IMAGE_QUANTITY_REGEX
	);
	private static final Pattern IMAGE_COUNT_PATTERN = Pattern.compile("(\\d+|[一二两三四])\\s*" + IMAGE_QUANTITY_REGEX);
	private static final Pattern IMAGE_PLUGIN_SUBJECT_PATTERN = Pattern.compile(
			"(?:^|[^a-z0-9])(?:image|images|photo|photos|picture|pictures|illustration|illustrations)(?:$|[^a-z0-9])"
					+ "|图片|图像|照片|配图|插图|海报|插画|图库",
			Pattern.CASE_INSENSITIVE
	);
	private static final Pattern IMAGE_PLUGIN_ACTION_PATTERN = Pattern.compile(
			"(?:^|[^a-z0-9])(?:search|find|generate|create|draw|render|design|produce|retrieve|fetch|get|query|list|recommend)(?:$|[^a-z0-9])"
					+ "|搜索|查询|生成|绘制|创建|设计|获取|推荐",
			Pattern.CASE_INSENSITIVE
	);
	private static final Map<String, Integer> CHINESE_IMAGE_COUNTS = Map.of(
			"一", 1,
			"二", 2,
			"两", 2,
			"三", 3,
			"四", 4
	);

	private final IAIChatHandler aiChatHandler;
	private final AiragMcpMapper airagMcpMapper;

	//update-begin---author:scott ---date:20260810  for：图片类插件与应用内置绘画能力互斥---
	/**
	 * 判断当前应用是否已启用图片搜索或图片生成类插件。
	 *
	 * @param params AI聊天参数
	 * @return 是否存在图片类插件
	 * @author scott
	 * @since 2026-08-10 图片类插件与应用内置绘画能力互斥
	 */
	public boolean hasImageRelatedPlugin(AIChatParams params) {
		if (params == null) {
			return false;
		}
		if (hasImageRelatedTool(params.getTools())) {
			return true;
		}
		List<String> pluginIds = params.getPluginIds();
		if (oConvertUtils.isObjectEmpty(pluginIds)) {
			return false;
		}
		try {
			List<String> distinctPluginIds = pluginIds.stream()
					.filter(oConvertUtils::isNotEmpty)
					.distinct()
					.collect(Collectors.toList());
			if (distinctPluginIds.isEmpty()) {
				return false;
			}
			return airagMcpMapper.selectBatchIds(distinctPluginIds).stream()
					.filter(plugin -> !LLMConsts.STATUS_DISABLE.equals(plugin.getStatus()))
					.anyMatch(this::isImageRelatedPlugin);
		} catch (Exception e) {
			log.warn("[AI-CHAT]识别图片类插件失败，保留应用内置绘画能力: {}", e.getMessage());
			return false;
		}
	}

	private boolean hasImageRelatedTool(Map<ToolSpecification, ToolExecutor> tools) {
		return tools != null && tools.keySet().stream()
				.filter(Objects::nonNull)
				.anyMatch(tool -> isImageRelatedCapability(tool.name(), tool.description()));
	}

	private boolean isImageRelatedPlugin(AiragMcp plugin) {
		if (plugin == null) {
			return false;
		}
		if (isImageRelatedCapability(plugin.getName(), plugin.getDescr())) {
			return true;
		}
		if (oConvertUtils.isEmpty(plugin.getTools())) {
			return false;
		}
		try {
			JSONArray tools = JSONArray.parseArray(plugin.getTools());
			if (tools == null) {
				return false;
			}
			return tools.stream()
					.filter(Objects::nonNull)
					.map(tool -> JSONObject.parseObject(tool.toString()))
					.anyMatch(tool -> isImageRelatedCapability(tool.getString("name"), tool.getString("description")));
		} catch (Exception e) {
			log.warn("[AI-CHAT]插件[{}]工具定义解析失败，跳过图片能力识别: {}", plugin.getName(), e.getMessage());
			return false;
		}
	}

	private boolean isImageRelatedCapability(String name, String description) {
		String capability = oConvertUtils.getString(name) + " " + oConvertUtils.getString(description);
		capability = capability.replaceAll("([a-z0-9])([A-Z])", "$1 $2");
		return IMAGE_PLUGIN_SUBJECT_PATTERN.matcher(capability).find()
				&& IMAGE_PLUGIN_ACTION_PATTERN.matcher(capability).find();
	}
	//update-end---author:scott ---date:20260810  for：图片类插件与应用内置绘画能力互斥---

	/**
	 * 判断用户是否明确要求生成图片。
	 *
	 * @param app AI应用
	 * @param content 用户消息
	 * @return 是否需要由后端确保生成图片
	 * @author scott
	 * @since 2026-08-10 AI应用支持智能识别和图文混合生成
	 */
	public boolean hasExplicitImageRequest(AiragApp app, String content) {
		return oConvertUtils.isNotEmpty(resolveDrawModelId(app))
				&& oConvertUtils.isNotEmpty(content)
				&& EXPLICIT_IMAGE_REQUEST_PATTERN.matcher(content).find();
	}

	/**
	 * 按用户消息中指定的数量生成配图。
	 *
	 * @param app AI应用
	 * @param content 用户消息
	 * @param articleContent 已生成的文章正文
	 * @param imageUploader 图片上传函数
	 * @return 已上传的图片地址
	 * @author scott
	 * @since 2026-08-10 AI应用支持智能识别和图文混合生成
	 */
	public List<String> generateForRequest(AiragApp app, String content, String articleContent,
			Function<Map<String, Object>, String> imageUploader) {
		String drawModelId = resolveDrawModelId(app);
		if (oConvertUtils.isEmpty(drawModelId) || oConvertUtils.isEmpty(content)) {
			return Collections.emptyList();
		}
		try {
			return generateImages(drawModelId, content, articleContent, resolveImageCount(content), imageUploader);
		} catch (Exception e) {
			log.error("[AI-CHAT]自动配图生成失败", e);
			return Collections.emptyList();
		}
	}

	/**
	 * 构建正文配图占位要求，由文本模型决定图片在文章中的语义位置。
	 *
	 * @author scott
	 * @since 2026-08-10 AI应用支持智能识别和图文混合生成
	 */
	public String buildPlacementInstruction(String content) {
		int count = resolveImageCount(content);
		StringBuilder placeholders = new StringBuilder();
		for (int i = 1; i <= count; i++) {
			if (i > 1) {
				placeholders.append("、");
			}
			placeholders.append("{{IMAGE_").append(i).append("}}");
		}
		return "用户已明确要求生成" + count + "张配图。请正常完成文字正文，并根据上下文在最合适的位置各插入一次占位符"
				+ placeholders + "。只允许输出这些精确占位符，禁止自行编造图片URL、Markdown图片标签、配图标题或‘此处插入图片’等其他占位文字；不要声称无法生成图片。";
	}

	/**
	 * 为已开启绘画能力的应用构建图片生成工具。
	 *
	 * @param app AI应用
	 * @param imageUploader 图片上传函数
	 * @return 图片生成工具
	 * @author scott
	 * @since 2026-08-10 AI应用支持智能识别和图文混合生成
	 */
	public Map<ToolSpecification, ToolExecutor> buildTools(AiragApp app, Function<Map<String, Object>, String> imageUploader) {
		String drawModelId = resolveDrawModelId(app);
		if (oConvertUtils.isEmpty(drawModelId)) {
			return Collections.emptyMap();
		}

		ToolSpecification specification = ToolSpecification.builder()
				.name(TOOL_NAME)
				.description("You have real image generation capability through this tool. You MUST call it when the user requests images or visual content; do not claim that you cannot generate images. For requests combining text and images, compose the requested text and include every Markdown image returned by this tool in the final answer. The count parameter must match the number of images requested by the user.")
				.parameters(JsonObjectSchema.builder()
						.addStringProperty("prompt", "A complete, detailed image-generation prompt. Required.")
						.addNumberProperty("count", "Number of distinct images requested by the user. Default 1, maximum 4.")
						.required("prompt")
						.build())
				.build();

		ToolExecutor executor = (request, memoryId) -> execute(drawModelId, request.arguments(), imageUploader);
		Map<ToolSpecification, ToolExecutor> tools = new HashMap<>(1);
		tools.put(specification, executor);
		return tools;
	}

	/**
	 * 执行模型主动调用的图片生成工具。
	 *
	 * @author scott
	 * @since 2026-08-10 AI应用支持智能识别和图文混合生成
	 */
	private String execute(String drawModelId, String arguments, Function<Map<String, Object>, String> imageUploader) {
		try {
			JSONObject args = JSONObject.parseObject(arguments);
			String prompt = args == null ? null : args.getString("prompt");
			if (oConvertUtils.isEmpty(prompt)) {
				return buildError("图片生成提示词不能为空");
			}
			int count = normalizeCount(args.getInteger("count"));
			List<String> imageUrls = generateImages(drawModelId, prompt, null, count, imageUploader);
			if (imageUrls.isEmpty()) {
				return buildError("图片生成失败，未返回有效图片");
			}
			return toMarkdown(imageUrls);
		} catch (Exception e) {
			log.error("[AI-CHAT]图片生成工具调用失败", e);
			return buildError("图片生成失败：" + e.getMessage());
		}
	}

	/**
	 * 按数量逐张生成并上传图片，单张失败时继续处理后续图片。
	 *
	 * @author scott
	 * @since 2026-08-10 AI应用支持智能识别和图文混合生成
	 */
	private List<String> generateImages(String drawModelId, String prompt, String articleContent, int count,
			Function<Map<String, Object>, String> imageUploader) {
		List<String> imageUrls = new ArrayList<>(count);
		AIChatParams params = new AIChatParams();
		for (int i = 0; i < count && imageUrls.size() < count; i++) {
			String imagePrompt = buildImagePrompt(prompt, articleContent, i, count);
			List<Map<String, Object>> generatedImages;
			try {
				generatedImages = aiChatHandler.imageGenerate(drawModelId, imagePrompt, params);
			} catch (Exception e) {
				log.warn("[AI-CHAT]第{}张配图生成失败，继续生成其他配图: {}", i + 1, e.getMessage());
				continue;
			}
			if (generatedImages == null) {
				continue;
			}
			for (Map<String, Object> generatedImage : generatedImages) {
				String imageUrl = imageUploader.apply(generatedImage);
				if (oConvertUtils.isNotEmpty(imageUrl)) {
					imageUrls.add(imageUrl);
				}
				if (imageUrls.size() >= count) {
					break;
				}
			}
		}
		return imageUrls;
	}

	/**
	 * 根据正文局部内容构造只返回图片的绘画提示词。
	 *
	 * @author scott
	 * @since 2026-08-10 AI应用支持智能识别和图文混合生成
	 */
	private String buildImagePrompt(String userRequest, String articleContent, int imageIndex, int count) {
		String visualContext = extractVisualContext(articleContent, imageIndex, count);
		StringBuilder prompt = new StringBuilder();
		prompt.append("Generate exactly one high-quality editorial illustration for an article. Return the image only. ")
				.append("Do not answer with text, do not write an article, and do not return Markdown. ")
				.append("Use a clean horizontal 16:9 composition and avoid long paragraphs of text inside the image.\n");
		if (oConvertUtils.isNotEmpty(visualContext)) {
			prompt.append("Visual context for this illustration: ").append(visualContext).append("\n");
		} else {
			prompt.append("Visual subject: ").append(userRequest).append("\n");
		}
		prompt.append("This is illustration ").append(imageIndex + 1).append(" of ").append(count)
				.append(". Make its scene and composition visibly different from the other illustrations.");
		return prompt.toString();
	}

	/**
	 * 提取图片占位符附近或文章均分位置附近的视觉上下文。
	 *
	 * @author scott
	 * @since 2026-08-10 AI应用支持智能识别和图文混合生成
	 */
	private String extractVisualContext(String articleContent, int imageIndex, int count) {
		if (oConvertUtils.isEmpty(articleContent)) {
			return "";
		}
		String placeholder = "{{IMAGE_" + (imageIndex + 1) + "}}";
		int center = articleContent.indexOf(placeholder);
		if (center < 0) {
			center = articleContent.length() * (imageIndex + 1) / (count + 1);
		}
		int start = Math.max(0, center - 500);
		int end = Math.min(articleContent.length(), center + 500);
		return articleContent.substring(start, end)
				.replaceAll("\\{\\{IMAGE_\\d+}}", " ")
				.replaceAll("[`#>*_|]", " ")
				.replaceAll("\\s+", " ")
				.trim();
	}

	/**
	 * 读取应用配置的绘画模型。
	 *
	 * @author scott
	 * @since 2026-08-10 AI应用支持智能识别和图文混合生成
	 */
	private String resolveDrawModelId(AiragApp app) {
		if (app == null || oConvertUtils.isEmpty(app.getMetadata())) {
			return null;
		}
		try {
			JSONObject metadata = JSONObject.parseObject(app.getMetadata());
			if (!DRAW_ENABLED.equals(metadata.getString(METADATA_DRAW_ENABLED))) {
				return null;
			}
			return metadata.getString(METADATA_DRAW_MODEL_ID);
		} catch (Exception e) {
			log.warn("[AI-CHAT]应用绘画配置解析失败，跳过图片生成工具: {}", e.getMessage());
			return null;
		}
	}

	/**
	 * 将图片数量限制在系统允许范围内。
	 *
	 * @author scott
	 * @since 2026-08-10 AI应用支持智能识别和图文混合生成
	 */
	private int normalizeCount(Integer count) {
		if (count == null || count < DEFAULT_IMAGE_COUNT) {
			return DEFAULT_IMAGE_COUNT;
		}
		return Math.min(count, MAX_IMAGE_COUNT);
	}

	/**
	 * 从用户消息中解析图片数量。
	 *
	 * @author scott
	 * @since 2026-08-10 AI应用支持智能识别和图文混合生成
	 */
	private int resolveImageCount(String content) {
		Matcher matcher = IMAGE_COUNT_PATTERN.matcher(content);
		if (!matcher.find()) {
			return DEFAULT_IMAGE_COUNT;
		}
		String countText = matcher.group(1);
		if (countText.matches("\\d+")) {
			return normalizeCount(Integer.parseInt(countText));
		}
		return normalizeCount(CHINESE_IMAGE_COUNTS.get(countText));
	}

	/**
	 * 将真实图片地址转换为Markdown图片标签。
	 *
	 * @author scott
	 * @since 2026-08-10 AI应用支持智能识别和图文混合生成
	 */
	private String toMarkdown(List<String> imageUrls) {
		return imageUrls.stream()
				.map(url -> "![](" + url + ")")
				.collect(Collectors.joining("\n\n"));
	}

	/**
	 * 构造图片工具的结构化错误结果。
	 *
	 * @author scott
	 * @since 2026-08-10 AI应用支持智能识别和图文混合生成
	 */
	private String buildError(String message) {
		JSONObject error = new JSONObject();
		error.put("error", message);
		return error.toJSONString();
	}
}
