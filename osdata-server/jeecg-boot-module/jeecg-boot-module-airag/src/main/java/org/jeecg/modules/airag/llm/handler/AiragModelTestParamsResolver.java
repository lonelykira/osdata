package org.jeecg.modules.airag.llm.handler;

import org.jeecg.ai.factory.AiModelFactory;
import org.jeecg.ai.handler.LLMHandler;
import org.jeecg.modules.airag.common.handler.AIChatParams;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * AI模型测试连接参数解析器。
 *
 * @author scott
 * @since 2026-08-10 【无号】图片模型测试连接参数迁移至Airag模块
 */
public final class AiragModelTestParamsResolver {

	private static final int TEST_IMAGE_COUNT = 1;
	private static final String QWEN_TEST_IMAGE_SIZE = "512*512";
	private static final String DALL_E_2_TEST_IMAGE_SIZE = "256x256";
	private static final String OPENAI_TEST_IMAGE_SIZE = "1024x1024";
	private static final String LOW_IMAGE_QUALITY = "low";
	private static final String PROMPT_EXTEND_PARAM = "prompt_extend";

	private AiragModelTestParamsResolver() {
	}

	/**
	 * 应用图片模型测试连接专用参数。
	 *
	 * @param params AI调用参数
	 * @param provider 供应商
	 * @param modelName 模型名称
	 * @author scott
	 * @since 2026-08-10 【无号】图片模型测试连接参数迁移至Airag模块
	 */
	public static void applyImageTestParams(AIChatParams params, String provider, String modelName) {
		Objects.requireNonNull(params, "AI调用参数不能为空");
		params.setImageCount(TEST_IMAGE_COUNT);
		params.setImageSize(resolveTestImageSize(provider, modelName));
		params.setImageQuality(resolveTestImageQuality(provider, modelName));
		params.setExtraParams(resolveTestImageExtraParams(provider, modelName));
	}

	/**
	 * 获取测试连接专用的最小图片尺寸。
	 *
	 * @param provider 供应商
	 * @param modelName 模型名称
	 * @return 测试图片尺寸，null表示使用供应商默认值
	 * @author scott
	 * @since 2026-08-10 【无号】图片模型测试连接参数迁移至Airag模块
	 */
	static String resolveTestImageSize(String provider, String modelName) {
		if (provider == null || provider.isEmpty()) {
			return null;
		}
		String model = normalizeModelName(modelName);
		if (AiModelFactory.AIMODEL_TYPE_OPENAI.equalsIgnoreCase(provider)) {
			return model.startsWith("dall-e-2") ? DALL_E_2_TEST_IMAGE_SIZE : OPENAI_TEST_IMAGE_SIZE;
		}
		if (AiModelFactory.AIMODEL_TYPE_QWEN.equalsIgnoreCase(provider)
				&& (LLMHandler.isQwenImage3Model(provider, modelName) || model.contains("turbo"))) {
			return QWEN_TEST_IMAGE_SIZE;
		}
		return null;
	}

	/**
	 * 获取测试连接专用的最低图片质量。
	 *
	 * @param provider 供应商
	 * @param modelName 模型名称
	 * @return 测试图片质量，null表示使用供应商默认值
	 * @author scott
	 * @since 2026-08-10 【无号】图片模型测试连接参数迁移至Airag模块
	 */
	static String resolveTestImageQuality(String provider, String modelName) {
		String model = normalizeModelName(modelName);
		if (AiModelFactory.AIMODEL_TYPE_OPENAI.equalsIgnoreCase(provider) && model.startsWith("gpt-image")) {
			return LOW_IMAGE_QUALITY;
		}
		return null;
	}

	/**
	 * 获取测试连接专用扩展参数。
	 *
	 * @param provider 供应商
	 * @param modelName 模型名称
	 * @return 测试扩展参数，null表示无需设置
	 * @author scott
	 * @since 2026-08-10 【无号】图片模型测试连接参数迁移至Airag模块
	 */
	static Map<String, Object> resolveTestImageExtraParams(String provider, String modelName) {
		if (LLMHandler.isQwenImage3Model(provider, modelName)) {
			return Collections.singletonMap(PROMPT_EXTEND_PARAM, false);
		}
		return null;
	}

	/**
	 * 规范化模型名称。
	 *
	 * @param modelName 模型名称
	 * @return 小写模型名称
	 * @author scott
	 * @since 2026-08-10 【无号】图片模型测试连接参数迁移至Airag模块
	 */
	private static String normalizeModelName(String modelName) {
		return modelName == null ? "" : modelName.toLowerCase(Locale.ROOT);
	}
}
