package IOTGateConsole.databridge;

import java.io.Serializable;

/**
 * 网关注册节点信息
 *
 * 网关启动时通过 -c -r &lt;consoleIp&gt; 主动注册到前端管理服务(Console)，
 * 注册后 Console 定期收到心跳，超时未心跳的节点会被自动判离线并移除。
 *
 * @author yangcheng
 * @date:   2026年8月10日
 */
public class GateNodeInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 网关IP(未传时服务端取HTTP请求来源IP) */
	private String ip;

	/** 网关RPC服务端口，默认10916 */
	private int rpcPort = 10916;

	/** 网关编号(-n 参数) */
	private int gateNum;

	/** 最近一次心跳时间戳(ms) */
	private long lastHeartbeat;

	/** 注册时间戳(ms)，用于计算在线时长 */
	private long regTime;

	/** 是否在线 */
	private boolean online = true;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getRpcPort() {
		return rpcPort;
	}

	public void setRpcPort(int rpcPort) {
		this.rpcPort = rpcPort;
	}

	public int getGateNum() {
		return gateNum;
	}

	public void setGateNum(int gateNum) {
		this.gateNum = gateNum;
	}

	public long getLastHeartbeat() {
		return lastHeartbeat;
	}

	public void setLastHeartbeat(long lastHeartbeat) {
		this.lastHeartbeat = lastHeartbeat;
	}

	public long getRegTime() {
		return regTime;
	}

	public void setRegTime(long regTime) {
		this.regTime = regTime;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}
}
