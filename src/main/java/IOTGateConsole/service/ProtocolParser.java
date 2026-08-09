package IOTGateConsole.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import IOTGateConsole.domain.ProtocolParseResult;

/**
 * 智能体 AI 服务接口（LangChain4j AiServices 契约）
 *
 * 声明式定义大模型对话行为：
 * - @SystemMessage：系统提示词，定义 AI 角色与输出规范
 * - @UserMessage：用户消息模板，{{frameDesc}} 为运行时注入的帧结构描述
 * - 返回类型 ProtocolParseResult：LangChain4j 自动引导模型输出 JSON 并反序列化，
 *   无需手工解析大模型返回文本
 *
 * @author willbeahero
 * @date:   2026年8月10日
 */
public interface ProtocolParser {

	/**
	 * 解析规约帧结构，推导网关解码参数
	 *
	 * @param frameDesc 用户提供的帧结构描述（十六进制报文/字段说明/文字描述）
	 * @return 结构化解析结果
	 */
	@SystemMessage("你是一位资深的物联网通信规约解析专家，熟悉 Netty LengthFieldBasedFrameDecoder 的报文解码参数配置。"
			+ "用户会提供某规约的报文帧结构描述（可能是十六进制报文示例、字段说明表格或文字描述）。"
			+ "请分析并推导出以下解码参数，只输出 JSON 对象，不要输出任何其他文字或解释：\n"
			+ "{\n"
			+ "  \"pName\": \"规约名称(根据上下文合理命名，如'自定义规约XX')\",\n"
			+ "  \"isBigEndian\": 0或1,  // 长度域字节序，大端为1小端为0\n"
			+ "  \"beginHexVal\": \"-1\", // 起始字符十六进制，无起始符为-1\n"
			+ "  \"lengthFieldOffset\": 数字, // 长度域起始位置相对报文头的偏移字节数\n"
			+ "  \"lengthFieldLength\": 数字, // 长度域占用的字节数(1/2/4)\n"
			+ "  \"isDataLenthIncludeLenthFieldLenth\": 0或1, // 长度值是否包含长度域自身字节数\n"
			+ "  \"exceptDataLenth\": 数字, // 长度值之外需要额外加减的字节数(如CRC/帧尾)\n"
			+ "  \"port\": 数字, // 建议的监听端口，如9814\n"
			+ "  \"analysis\": \"简要中文分析说明，解释推导依据\"\n"
			+ "}\n"
			+ "注意：\n"
			+ "- 必须逐字节核对示例报文：长度域偏移 = 报文头部所有固定字段（起始符/地址/控制码等）字节数之和，从报文第0字节算起\n"
			+ "- 帧头起始符可能在报文中重复出现，不可漏数。示例：DL/T645帧 '68 11 22 33 44 55 66 68 01 02 43 C3 16' 中："
			+ "起始符68占1字节(index0)，地址域6字节(index1-6)，第二个68帧头占1字节(index7)，控制码01占1字节(index8)，"
			+ "长度域02位于index9，故 lengthFieldOffset=9，lengthFieldLength=1，beginHexVal=68，exceptDataLenth=2(校验CS+结束符16)\n"
			+ "- 若报文包含帧头起始符，beginHexVal 填起始符的十六进制(如68)\n"
			+ "- 若长度域不含自身字节数，则 isDataLenthIncludeLenthFieldLenth=0\n"
			+ "- exceptDataLenth 用于长度域表示的值与实际帧长的差值修正（如含CRC/帧尾时）\n"
			+ "- 无显式长度域的规约（如Modbus RTU固定帧长）lengthFieldLength 填0并在 analysis 中说明\n"
			+ "- 无法确定的参数给合理默认值并在 analysis 中说明")
	@UserMessage("请解析以下规约帧结构：\n\n{{frameDesc}}")
	ProtocolParseResult parse(@V("frameDesc") String frameDesc);
}
