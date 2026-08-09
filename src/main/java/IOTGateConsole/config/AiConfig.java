package IOTGateConsole.config;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import IOTGateConsole.service.ProtocolParser;

/**
 * AI 智能体配置（LangChain4j）
 *
 * 大模型厂商无关：通过 OpenAI 兼容协议接入任意厂家模型，
 * 用户只需在 application.properties 或环境变量中配置：
 *   ai.api-key=${DEEPSEEK_API_KEY:}      # API Key（本地模型如 Ollama 可留空）
 *   ai.base-url=https://api.deepseek.com/v1  # 兼容端点，可切换通义/智谱/Ollama/OpenAI 等
 *   ai.model=deepseek-chat               # 模型名称
 *   ai.temperature=0.1                   # 采样温度
 *   ai.timeout-seconds=60                # 请求超时
 *
 * 未配置 ai.api-key 且目标端点需要鉴权时，请求会失败并返回明确错误；
 * 应用本身照常启动，不影响控制台其他功能。
 *
 * @author willbeahero
 * @date:   2026年8月10日
 */
@Configuration
public class AiConfig {

	private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

	@Value("${ai.api-key:}")
	private String apiKey;

	@Value("${ai.base-url:https://api.deepseek.com/v1}")
	private String baseUrl;

	@Value("${ai.model:deepseek-chat}")
	private String model;

	@Value("${ai.temperature:0.1}")
	private Double temperature;

	@Value("${ai.timeout-seconds:60}")
	private Long timeoutSeconds;

	@Bean
	public ChatModel chatLanguageModel() {
		// 兼容用户配置端点不带 /v1 的情况（如 https://api.deepseek.com）
		String endpoint = normalizeBaseUrl(baseUrl);
		log.info("[AI] 初始化 ChatLanguageModel: baseUrl={} model={} temperature={}",
				endpoint, model, temperature);
		return OpenAiChatModel.builder()
				.baseUrl(endpoint)
				.apiKey(apiKey == null ? "" : apiKey.trim())
				.modelName(model)
				.temperature(temperature)
				.timeout(Duration.ofSeconds(timeoutSeconds))
				.logRequests(true)
				.logResponses(true)
				.build();
	}

	@Bean
	public ProtocolParser protocolParser(ObjectProvider<ChatModel> modelProvider) {
		ChatModel model = modelProvider.getIfAvailable();
		if (model == null) {
			log.warn("[AI] ChatLanguageModel 未就绪，跳过 ProtocolParser 装配");
			return null;
		}
		log.info("[AI] 通过 AiServices 装配 ProtocolParser");
		return AiServices.builder(ProtocolParser.class)
				.chatModel(model)
				.build();
	}

	/**
	 * 归一化兼容端点：确保以 /v1 结尾（langchain4j 内部拼接 /chat/completions）
	 */
	private String normalizeBaseUrl(String url) {
		if (url == null || url.trim().isEmpty()) {
			return "https://api.deepseek.com/v1";
		}
		String u = url.trim();
		if (u.endsWith("/")) {
			u = u.substring(0, u.length() - 1);
		}
		return u.endsWith("/v1") ? u : u + "/v1";
	}
}
