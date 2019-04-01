package IOTGateConsole.service;


import IOTGateConsole.databridge.RetData;

public interface RpcService {
	/**
	 * 获取网关节点信息
	 * @return
	 */
	RetData getAllGateInfo();

}
