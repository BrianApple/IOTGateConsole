package IOTGateConsole.config;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import IOTGateConsole.service.ProtocolParser;

/**
 * AI 模型动态配置管理器
 *
 * 支持用户在控制台前端动态设置大模型配置（厂商无关，任意 OpenAI 兼容接口），
 * 配置变更后即时重建 ChatModel 与 ProtocolParser，无需重启应用：
 * - 启动时：以 application.properties/env 默认值初始化，若存在持久化文件
 *   ai-config.json（用户上次动态设置）则优先加载
 * - 运行中：updateConfig() 校验并更新配置 → 持久化到 ai-config.json → 热重建模型
 * - api-key 保存在本地文件(权限600)，不写入任何日志与仓库
 *
 * @author willbeahero
 * @date:   2026年8月10日
 */
@Component
public class AiModelManager {

	private static final Logger log = LoggerFactory.getLogger(AiModelManager.class);

	/** 动态配置持久化文件（应用工作目录下） */
	private static final String CONFIG_FILE = "ai-config.json";

	@Value("${ai.api-key:}")
	private String defaultApiKey;

	@Value("${ai.base-url:https://api.deepseek.com/v1}")
	private String defaultBaseUrl;

	@Value("${ai.model:deepseek-chat}")
	private String defaultModel;

	@Value("${ai.temperature:0.1}")
	private Double defaultTemperature;

	@Value("${ai.timeout-seconds:60}")
	private Long defaultTimeoutSeconds;

	/** 当前生效配置（volatile 保证多线程可见） */
	private volatile AIModelConfig config = new AIModelConfig();

	/** 当前生效的模型实例 */
	private volatile ChatModel chatModel;

	/** 当前生效的 AI 服务代理 */
	private volatile ProtocolParser protocolParser;

	@PostConstruct
	public void init() {
		config.setBaseUrl(defaultBaseUrl == null || defaultBaseUrl.isEmpty()
				? "https://api.deepseek.com/v1" : defaultBaseUrl.trim());
		config.setApiKey(defaultApiKey == null ? "" : defaultApiKey.trim());
		config.setModel(defaultModel == null || defaultModel.isEmpty() ? "deepseek-chat" : defaultModel.trim());
		config.setTemperature(defaultTemperature == null ? 0.1 : defaultTemperature);
		config.setTimeoutSeconds(defaultTimeoutSeconds == null ? 60L : defaultTimeoutSeconds);
		// 用户动态设置的配置优先（持久化文件存在则覆盖默认值）
		loadFromFile();
		rebuildModel();
	}

	/**
	 * 更新配置并热生效
	 * @param newConfig 新配置（apiKey 为空表示不修改，baseUrl/model 必填）
	 */
	public synchronized void updateConfig(AIModelConfig newConfig) {
		if (newConfig.getBaseUrl() != null && !newConfig.getBaseUrl().trim().isEmpty()) {
			config.setBaseUrl(newConfig.getBaseUrl().trim());
		}
		if (newConfig.getModel() != null && !newConfig.getModel().trim().isEmpty()) {
			config.setModel(newConfig.getModel().trim());
		}
		if (newConfig.getApiKey() != null && !newConfig.getApiKey().isEmpty()) {
			config.setApiKey(newConfig.getApiKey().trim());
		}
		if (newConfig.getTemperature() != null) {
			config.setTemperature(newConfig.getTemperature());
		}
		if (newConfig.getTimeoutSeconds() != null) {
			config.setTimeoutSeconds(newConfig.getTimeoutSeconds());
		}
		saveToFile();
		rebuildModel();
	}

	/** 返回当前配置副本 */
	public AIModelConfig getConfig() {
		AIModelConfig copy = new AIModelConfig();
		copy.setBaseUrl(config.getBaseUrl());
		copy.setApiKey(config.getApiKey());
		copy.setModel(config.getModel());
		copy.setTemperature(config.getTemperature());
		copy.setTimeoutSeconds(config.getTimeoutSeconds());
		return copy;
	}

	/** 返回当前 AI 服务代理 */
	public ProtocolParser getParser() {
		return protocolParser;
	}

	/** 配置是否可用（本地模型无需 key） */
	public boolean isReady() {
		return config.getBaseUrl() != null && !config.getBaseUrl().isEmpty()
				&& config.getModel() != null && !config.getModel().isEmpty();
	}

	/** 当前模型是否需要 API Key（本地模型如 Ollama 不需要） */
	public boolean needApiKey() {
		String url = config.getBaseUrl() == null ? "" : config.getBaseUrl().toLowerCase();
		boolean local = url.contains("localhost") || url.contains("127.0.0.1") || url.contains("0.0.0.0");
		return !local && (config.getApiKey() == null || config.getApiKey().isEmpty());
	}

	private void rebuildModel() {
		String endpoint = normalizeBaseUrl(config.getBaseUrl());
		String key = config.getApiKey() == null ? "" : config.getApiKey();
		log.info("[AI] 重建 ChatModel: baseUrl={} model={} temperature={} timeout={}s hasKey={}",
				endpoint, config.getModel(), config.getTemperature(), config.getTimeoutSeconds(), !key.isEmpty());
		try {
			chatModel = OpenAiChatModel.builder()
					.baseUrl(endpoint)
					.apiKey(key)
					.modelName(config.getModel())
					.temperature(config.getTemperature() == null ? 0.1 : config.getTemperature())
					.timeout(Duration.ofSeconds(config.getTimeoutSeconds() == null ? 60 : config.getTimeoutSeconds()))
					.logRequests(true)
					.logResponses(true)
					.build();
			protocolParser = AiServices.builder(ProtocolParser.class)
					.chatModel(chatModel)
					.build();
			log.info("[AI] 模型与智能体代理重建完成");
		} catch (Exception e) {
			log.error("[AI] 模型重建失败，请检查配置", e);
			chatModel = null;
			protocolParser = null;
		}
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

	private void saveToFile() {
		try {
			File f = new File(CONFIG_FILE);
			Files.write(f.toPath(), JSON.toJSONString(config).getBytes(StandardCharsets.UTF_8));
			// 权限收紧为仅当前用户可读写(600)，避免 api-key 泄露给其他用户
			f.setReadable(true, true);
			f.setWritable(true, true);
			f.setReadable(false, false);
			f.setWritable(false, false);
			log.info("[AI] 动态配置已持久化到 {}", f.getAbsolutePath());
		} catch (Exception e) {
			log.warn("[AI] 动态配置持久化失败", e);
		}
	}

	private void loadFromFile() {
		try {
			File f = new File(CONFIG_FILE);
			if (f.exists()) {
				String json = Files.readString(f.toPath(), StandardCharsets.UTF_8);
				AIModelConfig saved = JSON.parseObject(json, AIModelConfig.class);
				if (saved != null && saved.getBaseUrl() != null && !saved.getBaseUrl().isEmpty()) {
					config = saved;
					log.info("[AI] 已加载用户动态配置: baseUrl={} model={}",
							config.getBaseUrl(), config.getModel());
				}
			}
		} catch (Exception e) {
			log.warn("[AI] 动态配置加载失败，使用默认配置", e);
		}
	}
}
