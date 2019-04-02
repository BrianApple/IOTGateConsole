package IOTGateConsole.domain;

import java.io.Serializable;

public class IotGateDB implements Serializable{
	private static final long serialVersionUID = 5682133289282639734L;
	private String id;
	private int pId;
	private String pName;
	private int isBigEndian;
	private String beginHexVal;
	private int lengthFieldOffset;
	private int lengthFieldLength;
	private int isDataLenthIncludeLenthFieldLenth;
	private int exceptDataLenth;
	private int port;
	private int highControll;
	private String content;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getpId() {
		return pId;
	}
	public void setpId(int pId) {
		this.pId = pId;
	}
	public String getpName() {
		return pName;
	}
	public void setpName(String pName) {
		this.pName = pName;
	}
	public int getIsBigEndian() {
		return isBigEndian;
	}
	public void setIsBigEndian(int isBigEndian) {
		this.isBigEndian = isBigEndian;
	}
	public String getBeginHexVal() {
		return beginHexVal;
	}
	public void setBeginHexVal(String beginHexVal) {
		this.beginHexVal = beginHexVal;
	}
	public int getLengthFieldOffset() {
		return lengthFieldOffset;
	}
	public void setLengthFieldOffset(int lengthFieldOffset) {
		this.lengthFieldOffset = lengthFieldOffset;
	}
	public int getLengthFieldLength() {
		return lengthFieldLength;
	}
	public void setLengthFieldLength(int lengthFieldLength) {
		this.lengthFieldLength = lengthFieldLength;
	}
	public int getIsDataLenthIncludeLenthFieldLenth() {
		return isDataLenthIncludeLenthFieldLenth;
	}
	public void setIsDataLenthIncludeLenthFieldLenth(int isDataLenthIncludeLenthFieldLenth) {
		this.isDataLenthIncludeLenthFieldLenth = isDataLenthIncludeLenthFieldLenth;
	}
	public int getExceptDataLenth() {
		return exceptDataLenth;
	}
	public void setExceptDataLenth(int exceptDataLenth) {
		this.exceptDataLenth = exceptDataLenth;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getHighControll() {
		return highControll;
	}
	public void setHighControll(int highControll) {
		this.highControll = highControll;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	@Override
	public String toString() {
		return "IotGateDB [id=" + id + ", pId=" + pId + ", isBigEndian=" + isBigEndian + ", beginHexVal=" + beginHexVal
				+ ", lengthFieldOffset=" + lengthFieldOffset + ", lengthFieldLength=" + lengthFieldLength
				+ ", isDataLenthIncludeLenthFieldLenth=" + isDataLenthIncludeLenthFieldLenth + ", exceptDataLenth="
				+ exceptDataLenth + ", port=" + port + ", highControll=" + highControll + ", content=" + content + "]";
	}

}
