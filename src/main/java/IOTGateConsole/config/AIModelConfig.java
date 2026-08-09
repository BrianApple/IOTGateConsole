package IOTGateConsole.config;

/**
 * 大模型连接配置（用户可动态设置）
 *
 * 对应任意 OpenAI 兼容接口：
 *   baseUrl 兼容端点（DeepSeek/通义/智谱/Ollama/OpenAI 等）
 *   apiKey  API Key（本地模型可留空）
 *   model   模型名称
 *   temperature 采样温度(0~2)
 *   timeoutSeconds 请求超时秒数
 *
 * @author willbeahero
 * @date:   2026年8月10日
 */
public class AIModelConfig {

	/** 兼容端点，默认 DeepSeek */
	private String baseUrl = "https://api.deepseek.com/v1";

	/** API Key */
	private String apiKey = "";

	/** 模型名称 */
	private String model = "deepseek-chat";

	/** 采样温度 */
	private Double temperature = 0.1;

	/** 请求超时秒数 */
	private Long timeoutSeconds = 60L;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Double getTemperature() {
		return temperature;
	}

	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}

	public Long getTimeoutSeconds() {
		return timeoutSeconds;
	}

	public void setTimeoutSeconds(Long timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	}
}
