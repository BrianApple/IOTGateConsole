package IOTGateConsole.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 智能体解析服务
 *
 * 通过大模型(DeepSeek)解析用户提供的规约帧结构描述，
 * 自动推导出网关解码所需的规约参数：
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

	@Value("${ai.base-url:https://api.deepseek.com}")
	private String baseUrl;

	@Value("${ai.model:deepseek-chat}")
	private String model;

	/** 系统提示词：规约解析专家 */
	private static final String SYSTEM_PROMPT =
			"你是一位资深的物联网通信规约解析专家，熟悉 Netty LengthFieldBasedFrameDecoder 的报文解码参数配置。\n" +
			"用户会提供某规约的报文帧结构描述（可能是十六进制报文示例、字段说明表格或文字描述）。\n" +
			"请分析并推导出以下解码参数（只输出 JSON，不要输出任何其他文字）：\n" +
			"{\n" +
			"  \"pName\": \"规约名称(根据上下文合理命名，如'自定义规约XX')\",\n" +
			"  \"isBigEndian\": 0或1,  // 长度域字节序，大端为1小端为0\n" +
			"  \"beginHexVal\": \"-1\", // 起始字符十六进制，无起始符为-1\n" +
			"  \"lengthFieldOffset\": 数字, // 长度域起始位置相对报文头的偏移字节数\n" +
			"  \"lengthFieldLength\": 数字, // 长度域占用的字节数(1/2/4)\n" +
			"  \"isDataLenthIncludeLenthFieldLenth\": 0或1, // 长度值是否包含长度域自身字节数\n" +
			"  \"exceptDataLenth\": 数字, // 长度值之外需要额外加减的字节数(如CRC/帧尾)\n" +
			"  \"port\": 数字, // 建议的监听端口，如9814\n" +
			"  \"analysis\": \"简要中文分析说明，解释推导依据\"\n" +
			"}\n" +
			"注意：\n" +
			"- lengthFieldOffset 是长度域第一个字节距离报文起始的偏移\n" +
			"- 若报文包含帧头起始符，beginHexVal 填起始符的十六进制(如68)，offset从帧头后算起\n" +
			"- 若长度域不含自身字节数，则 isDataLenthIncludeLenthFieldLenth=0\n" +
			"- exceptDataLenth 用于长度域表示的值与实际帧长的差值修正\n" +
			"- 无法确定的参数给合理默认值并在 analysis 中说明";

	/**
	 * 解析规约帧结构
	 * @param frameDesc 用户提供的帧结构描述
	 * @return 解析结果 JSON 字符串
	 */
	public String parseProtocol(String frameDesc) {
		if (apiKey == null || apiKey.isEmpty()) {
			return errorJson("AI 服务未配置：请在 application.properties 或环境变量中设置 ai.api-key（DEEPSEEK_API_KEY）");
		}
		try {
			String userPrompt = "请解析以下规约帧结构：\n\n" + frameDesc;
			JSONObject body = new JSONObject();
			body.put("model", model);
			body.put("temperature", 0.1);
			body.put("response_format", JSON.parseObject("{\"type\":\"json_object\"}"));
			JSONArray messages = new JSONArray();
			messages.add(msg("system", SYSTEM_PROMPT));
			messages.add(msg("user", userPrompt));
			body.put("messages", messages);

			String resp = postJson(baseUrl + "/v1/chat/completions", body.toJSONString());
			JSONObject json = JSON.parseObject(resp);
			JSONArray choices = json.getJSONArray("choices");
			if (choices == null || choices.isEmpty()) {
				log.error("AI 响应异常: {}", resp);
				return errorJson("AI 响应异常，请重试");
			}
			String content = choices.getJSONObject(0).getJSONObject("message").getString("content");
			// 兼容模型可能返回 markdown 代码块包裹的 JSON
			content = content.replaceAll("```json", "").replaceAll("```", "").trim();
			JSON.parseObject(content); // 校验 JSON 合法性
			return content;
		} catch (Exception e) {
			log.error("AI 解析失败", e);
			return errorJson("AI 解析失败: " + e.getMessage());
		}
	}

	private JSONObject msg(String role, String content) {
		JSONObject m = new JSONObject();
		m.put("role", role);
		m.put("content", content);
		return m;
	}

	private String postJson(String urlStr, String jsonBody) throws Exception {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(30000);
		conn.setReadTimeout(60000);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Authorization", "Bearer " + apiKey);
		conn.setDoOutput(true);
		try (OutputStream os = conn.getOutputStream()) {
			os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
		}
		int code = conn.getResponseCode();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				code >= 400 ? conn.getErrorStream() : conn.getInputStream(), StandardCharsets.UTF_8))) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			if (code >= 400) {
				throw new RuntimeException("AI API HTTP " + code + ": " + sb);
			}
			return sb.toString();
		} finally {
			conn.disconnect();
		}
	}

	private String errorJson(String msg) {
		JSONObject err = new JSONObject();
		err.put("error", msg);
		return err.toJSONString();
	}
}
