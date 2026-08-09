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
 * - @UserMessage：用户消息模板，{{frameDesc}} 为运行时注入的协议帧结构描述
 * - 返回类型 ProtocolParseResult：LangChain4j 自动引导模型输出 JSON 并反序列化，
 *   无需手工解析大模型返回文本
 *
 * 输入约定：用户提供的是通信协议的【帧结构描述】（协议文档中的字段定义说明，
 * 包含字段顺序、各字段字节数、长度域的位置与定义、字节序、帧头帧尾校验等），
 * 而非实际报文。智能体的任务是把描述中的长度域信息准确提取并映射为
 * LengthFieldBasedFrameDecoder 拆包/黏包解码参数。
 *
 * @author willbeahero
 * @date:   2026年8月10日
 */
public interface ProtocolParser {

	/**
	 * 解析协议帧结构描述，推导网关解码参数
	 *
	 * @param frameDesc 协议帧结构描述（字段定义说明，含长度域位置与定义）
	 * @return 结构化解析结果
	 */
	@SystemMessage("你是一位资深的物联网通信规约解析专家，负责将【协议帧结构描述】准确转换为 Netty LengthFieldBasedFrameDecoder 的拆包/黏包解码参数配置。\n"
			+ "用户会提供某通信协议的帧结构描述（协议文档中的字段定义说明，可能以文字、表格或清单形式描述：帧头/起始符、各字段名称与字节数、"
			+ "长度域的位置偏移与定义、长度值含义、字节序、校验、帧尾、监听端口等），注意：用户提供的是结构描述而非实际报文。\n"
			+ "你的任务是从描述中提取长度域相关信息，映射为以下解码参数，只输出 JSON 对象，不要输出任何其他文字：\n"
			+ "{\n"
			+ "  \"pName\": \"规约名称(从描述中提取)\",\n"
			+ "  \"isBigEndian\": 0或1,  // 长度域字节序，大端为1小端为0\n"
			+ "  \"beginHexVal\": \"-1\", // 起始符十六进制，无起始符为-1\n"
			+ "  \"lengthFieldOffset\": 数字, // 长度域起始位置距帧首的字节偏移\n"
			+ "  \"lengthFieldLength\": 数字, // 长度域占用的字节数(1/2/4)\n"
			+ "  \"isDataLenthIncludeLenthFieldLenth\": 0或1, // 长度值是否包含长度域自身字节数\n"
			+ "  \"exceptDataLenth\": 数字, // 长度值之外需额外修正的字节数(如帧尾/校验)\n"
			+ "  \"port\": 数字, // 监听端口，描述未给出用9814\n"
			+ "  \"analysis\": \"简要中文说明，标注哪些参数来自描述提取、哪些是推断\"\n"
			+ "}\n"
			+ "参数映射规则（以描述为准，逐项核对，不要自行假设描述中未出现的信息）：\n"
			+ "- lengthFieldOffset：描述直接给出偏移则直接采用；若给出字段顺序与字节数，偏移 = 长度域之前所有字段字节数之和（从帧首第0字节起算，起始符计入）\n"
			+ "- lengthFieldLength：长度域占用字节数（描述中\"长度域N字节/1字节/2字节/字/双字\"）\n"
			+ "- isBigEndian：描述中\"大端/高字节在前/高地址在前/高位在前\"→1；\"小端/低字节在前/低位在前\"→0；未说明默认1并注明\n"
			+ "- isDataLenthIncludeLenthFieldLenth：描述中\"长度值包含长度域自身/长度含长度域\"→1；\"长度值表示后续数据长度/数据域长度/不含长度域\"→0\n"
			+ "- exceptDataLenth：长度值之外、但属于整帧需计入的字节（如固定帧尾、CRC校验等）；若长度值已覆盖整帧则填0\n"
			+ "- beginHexVal：帧头/起始符十六进制；无起始符填-1\n"
			+ "- 描述未明确的信息：给合理默认值，并在 analysis 中明确标注哪些是从描述提取的、哪些是推断的\n"
			+ "- 若描述显示该协议无显式长度域（如固定帧长），lengthFieldLength 填0并在 analysis 中说明")
	@UserMessage("请解析以下协议的帧结构描述：\n\n{{frameDesc}}\n\n"
			+ "提示：如描述信息不全，建议补充字段顺序、长度域位置与定义（字节数/长度值是否含长度域自身）、字节序、帧头帧尾校验、监听端口。")
	ProtocolParseResult parse(@V("frameDesc") String frameDesc);
}
