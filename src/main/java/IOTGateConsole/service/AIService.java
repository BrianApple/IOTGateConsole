package IOTGateConsole.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import IOTGateConsole.config.AIModelConfig;
import IOTGateConsole.config.AiModelManager;
import IOTGateConsole.domain.ProtocolParseResult;

/**
 * 智能体解析服务（基于 LangChain4j 动态配置）
 *
 * 通过 AiModelManager 获取当前生效的 ProtocolParser 声明式 AI 服务，
 * 解析用户提供的协议帧结构描述，自动推导出网关拆包/黏包解码参数：
 * isBigEndian/beginHexVal/lengthFieldOffset/lengthFieldLength/
 * isDataLenthIncludeLenthFieldLenth/exceptDataLenth/port 等。
 *
 * 大模型厂商无关：模型连接信息(地址/模型/Key)由用户在控制台前端动态设置，
 * 修改即时生效，无需重启。
 *
 * @author willbeahero
 * @date:   2026年8月10日
 */
@Service
public class AIService {

	private static final Logger log = LoggerFactory.getLogger(AIService.class);

	@Autowired
	private AiModelManager aiModelManager;

	/**
	 * 解析协议帧结构描述
	 *
	 * @param frameDesc 用户提供的帧结构描述（字段定义说明，含长度域位置与定义）
	 * @return 解析结果 JSON 字符串（含 error 字段表示失败）
	 */
	public String parseProtocol(String frameDesc) {
		AIModelConfig cfg = aiModelManager.getConfig();
		if (!aiModelManager.isReady()) {
			return errorJson("AI 服务未配置：请点击右下角 🤖 机器人 → 设置，填写模型地址与模型名称");
		}
		if (aiModelManager.needApiKey()) {
			return errorJson("AI 服务缺少 API Key：请点击右下角 🤖 机器人 → 设置，填写 API Key");
		}
		ProtocolParser parser = aiModelManager.getParser();
		if (parser == null) {
			return errorJson("AI 组件未初始化，请检查模型配置是否正确");
		}
		try {
			ProtocolParseResult result = parser.parse(frameDesc);
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
