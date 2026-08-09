package IOTGateConsole.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import IOTGateConsole.service.AIService;

/**
 * 智能体模式控制器
 *
 * 提供基于大模型的规约帧结构智能解析能力：
 * 用户粘贴规约帧结构(十六进制报文/字段说明)，
 * AI 自动推导出网关解码参数并返回，前端可一键填充新增规约表单。
 *
 * @author willbeahero
 * @date:   2026年8月10日
 */
@Controller
@RequestMapping("/rpc/ai")
public class AIController {

	@Autowired
	private AIService aiService;

	/**
	 * 解析规约帧结构
	 * 请求体: {"frameDesc": "用户提供的帧结构描述"}
	 */
	@PostMapping("/parse")
	@ResponseBody
	public Map<String, Object> parse(@RequestBody Map<String, String> req) {
		String frameDesc = req.get("frameDesc");
		Map<String, Object> ret = new HashMap<>();
		if (frameDesc == null || frameDesc.trim().isEmpty()) {
			ret.put("retSig", 400);
			ret.put("error", "请提供规约帧结构描述");
			return ret;
		}
		String result = aiService.parseProtocol(frameDesc.trim());
		JSONObject json = JSON.parseObject(result);
		if (json.containsKey("error")) {
			ret.put("retSig", 500);
			ret.put("error", json.getString("error"));
			return ret;
		}
		ret.put("retSig", 200);
		ret.put("data", json);
		return ret;
	}
}
