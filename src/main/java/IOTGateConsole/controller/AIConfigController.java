package IOTGateConsole.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import IOTGateConsole.config.AIModelConfig;
import IOTGateConsole.config.AiModelManager;

/**
 * 大模型配置管理接口
 *
 * 支持前端动态设置/修改大模型连接配置（厂商无关，任意 OpenAI 兼容接口），
 * 修改后即时生效，无需重启服务。
 *
 * GET  /rpc/ai/config  获取当前配置（api-key 脱敏返回）
 * POST /rpc/ai/config  保存新配置（api-key 传掩码或留空表示不修改）
 *
 * @author willbeahero
 * @date:   2026年8月10日
 */
@Controller
@RequestMapping("/rpc/ai")
public class AIConfigController {

	@Autowired
	private AiModelManager aiModelManager;

	/**
	 * 获取当前大模型配置
	 */
	@GetMapping("/config")
	@ResponseBody
	public Map<String, Object> getConfig() {
		AIModelConfig cfg = aiModelManager.getConfig();
		Map<String, Object> data = new HashMap<>();
		data.put("baseUrl", cfg.getBaseUrl());
		data.put("model", cfg.getModel());
		data.put("apiKeyMasked", mask(cfg.getApiKey()));
		data.put("hasApiKey", cfg.getApiKey() != null && !cfg.getApiKey().isEmpty());
		data.put("temperature", cfg.getTemperature());
		data.put("timeoutSeconds", cfg.getTimeoutSeconds());
		data.put("needApiKey", aiModelManager.needApiKey());
		Map<String, Object> ret = new HashMap<>();
		ret.put("retSig", 200);
		ret.put("data", data);
		return ret;
	}

	/**
	 * 保存大模型配置（动态生效）
	 * 请求体: {"baseUrl":"...","model":"...","apiKey":"...","temperature":0.1,"timeoutSeconds":60}
	 * apiKey 传空或掩码(含****)表示不修改
	 */
	@PostMapping("/config")
	@ResponseBody
	public Map<String, Object> saveConfig(@RequestBody Map<String, Object> body) {
		Map<String, Object> ret = new HashMap<>();
		String baseUrl = str(body.get("baseUrl"));
		String model = str(body.get("model"));
		if (baseUrl.isEmpty() || model.isEmpty()) {
			ret.put("retSig", 400);
			ret.put("error", "模型地址(baseUrl)与模型名称(model)不能为空");
			return ret;
		}
		AIModelConfig cfg = new AIModelConfig();
		cfg.setBaseUrl(baseUrl);
		cfg.setModel(model);
		// apiKey：前端回显的是掩码，传掩码或留空表示不修改
		String apiKey = str(body.get("apiKey"));
		if (!apiKey.isEmpty() && !apiKey.contains("****")) {
			cfg.setApiKey(apiKey);
		}
		if (body.get("temperature") != null) {
			cfg.setTemperature(Double.parseDouble(String.valueOf(body.get("temperature"))));
		}
		if (body.get("timeoutSeconds") != null) {
			cfg.setTimeoutSeconds(Long.parseLong(String.valueOf(body.get("timeoutSeconds"))));
		}
		try {
			aiModelManager.updateConfig(cfg);
			ret.put("retSig", 200);
			ret.put("msg", "配置已更新并立即生效");
			return ret;
		} catch (Exception e) {
			ret.put("retSig", 500);
			ret.put("error", "配置更新失败: " + e.getMessage());
			return ret;
		}
	}

	private String str(Object o) {
		return o == null ? "" : String.valueOf(o).trim();
	}

	/** API Key 脱敏：sk-abc12345 → sk-***2345 */
	private String mask(String key) {
		if (key == null || key.isEmpty()) {
			return "";
		}
		if (key.length() <= 8) {
			return key.substring(0, 1) + "****";
		}
		return key.substring(0, 3) + "****" + key.substring(key.length() - 4);
	}
}
