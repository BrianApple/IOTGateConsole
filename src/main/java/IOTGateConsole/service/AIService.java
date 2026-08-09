package IOTGateConsole.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import IOTGateConsole.domain.ProtocolParseResult;

/**
 * 智能体解析服务（基于 LangChain4j）
 *
 * 通过 LangChain4j AiServices 生成的 ProtocolParser 声明式 AI 服务，
 * 解析用户提供的规约帧结构描述，自动推导出网关解码所需的规约参数：
 * isBigEndian/beginHexVal/lengthFieldOffset/lengthFieldLength/
 * isDataLenthIncludeLenthFieldLenth/exceptDataLenth/port 等。
 *
 * 配置(application.properties 或环境变量)：
 *   ai.api-key=${DEEPSEEK_API_KEY:}
 *   ai.base-url=https://api.deepseek.com
 *   ai.model=deepseek-chat
 *
 * @author willbeahero
 * @date:   2026年8月10日
 */
@Service
public class AIService {

	private static final Logger log = LoggerFactory.getLogger(AIService.class);

	@Value("${ai.api-key:}")
	private String apiKey;

	/** LangChain4j 声明式 AI 服务代理；未配置 key 时为 null */
	@Autowired(required = false)
	private ProtocolParser protocolParser;

	/**
	 * 解析规约帧结构
	 *
	 * @param frameDesc 用户提供的帧结构描述
	 * @return 解析结果 JSON 字符串（含 error 字段表示失败）
	 */
	public String parseProtocol(String frameDesc) {
		if (apiKey == null || apiKey.isEmpty()) {
			return errorJson("AI 服务未配置：请在 application.properties 或环境变量中设置 ai.api-key（DEEPSEEK_API_KEY）");
		}
		if (protocolParser == null) {
			return errorJson("AI 组件未初始化，请检查服务启动日志");
		}
		try {
			ProtocolParseResult result = protocolParser.parse(frameDesc);
			if (result == null) {
				return errorJson("AI 返回结果为空，请重试");
			}
			log.info("[AI] 规约解析成功: pName={} lengthFieldOffset={} lengthFieldLength={}",
					result.getPName(), result.getLengthFieldOffset(), result.getLengthFieldLength());
			return JSON.toJSONString(result);
		} catch (Exception e) {
			log.error("[AI] 规约解析失败", e);
			return errorJson("AI 解析失败: " + e.getMessage());
		}
	}

	private String errorJson(String msg) {
		JSONObject err = new JSONObject();
		err.put("error", msg);
		return err.toJSONString();
	}
}
