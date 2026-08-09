package IOTGateConsole.config;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import IOTGateConsole.service.ProtocolParser;

/**
 * AI 智能体配置（LangChain4j）
 *
 * 组装 ChatLanguageModel（DeepSeek 走 OpenAI 兼容协议）并通过 AiServices
 * 生成 ProtocolParser 声明式 AI 服务代理。
 *
 * 未配置 ai.api-key 时应用照常启动，仅智能体功能不可用（返回明确提示），
 * 不影响控制台其他功能。
 *
 * 配置项（application.properties 或环境变量）：
 *   ai.api-key=${DEEPSEEK_API_KEY:}
 *   ai.base-url=https://api.deepseek.com
 *   ai.model=deepseek-chat
 *
 * @author willbeahero
 * @date:   2026年8月10日
 */
@Configuration
public class AiConfig {

	private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

	@Value("${ai.api-key:}")
	private String apiKey;

	@Value("${ai.base-url:https://api.deepseek.com}")
	private String baseUrl;

	@Value("${ai.model:deepseek-chat}")
	private String model;

	@Bean
	public ChatLanguageModel chatLanguageModel() {
		if (apiKey == null || apiKey.trim().isEmpty()) {
			log.warn("[AI] 未配置 ai.api-key（DEEPSEEK_API_KEY），智能体功能不可用，控制台其他功能正常");
			return null;
		}
		// DeepSeek OpenAI 兼容接口：base-url 需指向 .../v1，langchain4j 内部拼接 /chat/completions
		String endpoint = baseUrl.endsWith("/v1") ? baseUrl : baseUrl + "/v1";
		log.info("[AI] 初始化 ChatLanguageModel: baseUrl={} model={}", endpoint, model);
		return OpenAiChatModel.builder()
				.baseUrl(endpoint)
				.apiKey(apiKey)
				.modelName(model)
				.temperature(0.1)
				.timeout(Duration.ofSeconds(60))
				.logRequests(true)
				.logResponses(true)
				.build();
	}

	@Bean
	public ProtocolParser protocolParser(ObjectProvider<ChatLanguageModel> modelProvider) {
		ChatLanguageModel model = modelProvider.getIfAvailable();
		if (model == null) {
			log.warn("[AI] ChatLanguageModel 未就绪，跳过 ProtocolParser 装配");
			return null;
		}
		log.info("[AI] 通过 AiServices 装配 ProtocolParser");
		return AiServices.builder(ProtocolParser.class)
				.chatLanguageModel(model)
				.build();
	}
}
