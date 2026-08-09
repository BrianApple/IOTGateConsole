package IOTGateConsole.domain;

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * 规约解析结果（AI 结构化输出）
 *
 * 由 LangChain4j AiServices 自动将大模型返回的 JSON 反序列化为本对象。
 * 对应 IOTGate 网关 LengthFieldBasedFrameDecoder 的解码参数：
 * isBigEndian / beginHexVal / lengthFieldOffset / lengthFieldLength /
 * isDataLenthIncludeLenthFieldLenth / exceptDataLenth / port。
 *
 * @author willbeahero
 * @date:   2026年8月10日
 */
public class ProtocolParseResult {

	/** 规约名称（根据上下文合理命名） */
	@JSONField(name = "pName")
	private String pName;

	/** 长度域字节序：1=大端 0=小端 */
	private Integer isBigEndian;

	/** 起始字符十六进制，无起始符为 -1 */
	private String beginHexVal;

	/** 长度域起始位置相对报文头的偏移字节数 */
	private Integer lengthFieldOffset;

	/** 长度域占用的字节数（1/2/4） */
	private Integer lengthFieldLength;

	/** 长度值是否包含长度域自身字节数：1=包含 0=不包含 */
	private Integer isDataLenthIncludeLenthFieldLenth;

	/** 长度值之外需要额外加减的字节数（如 CRC/帧尾） */
	private Integer exceptDataLenth;

	/** 建议的监听端口 */
	private Integer port;

	/** 简要中文分析说明，解释推导依据 */
	private String analysis;

	public String getPName() {
		return pName;
	}

	public void setPName(String pName) {
		this.pName = pName;
	}

	public Integer getIsBigEndian() {
		return isBigEndian;
	}

	public void setIsBigEndian(Integer isBigEndian) {
		this.isBigEndian = isBigEndian;
	}

	public String getBeginHexVal() {
		return beginHexVal;
	}

	public void setBeginHexVal(String beginHexVal) {
		this.beginHexVal = beginHexVal;
	}

	public Integer getLengthFieldOffset() {
		return lengthFieldOffset;
	}

	public void setLengthFieldOffset(Integer lengthFieldOffset) {
		this.lengthFieldOffset = lengthFieldOffset;
	}

	public Integer getLengthFieldLength() {
		return lengthFieldLength;
	}

	public void setLengthFieldLength(Integer lengthFieldLength) {
		this.lengthFieldLength = lengthFieldLength;
	}

	public Integer getIsDataLenthIncludeLenthFieldLenth() {
		return isDataLenthIncludeLenthFieldLenth;
	}

	public void setIsDataLenthIncludeLenthFieldLenth(Integer isDataLenthIncludeLenthFieldLenth) {
		this.isDataLenthIncludeLenthFieldLenth = isDataLenthIncludeLenthFieldLenth;
	}

	public Integer getExceptDataLenth() {
		return exceptDataLenth;
	}

	public void setExceptDataLenth(Integer exceptDataLenth) {
		this.exceptDataLenth = exceptDataLenth;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getAnalysis() {
		return analysis;
	}

	public void setAnalysis(String analysis) {
		this.analysis = analysis;
	}
}
