package IOTGateConsole.service;


import IOTGateConsole.databridge.ReqWebData;
import IOTGateConsole.databridge.RetData;

public interface RpcService {
	/**
	 * 获取网关节点信息
	 * @return
	 */
	RetData getAllGateInfo();

	/**
	 * 添加一个规约
	 * @param args
	 * @return
	 */
	RetData addOneStrategy(ReqWebData args);

	/**
	 * 获取所有创建过的规约
	 * @return
	 */
	RetData getAllStrategeFromDB();

	/**
	 * 更新网关节点所所开启得规约
	 * @param args
	 * @return
	 */
	RetData updateStrategy2Node(ReqWebData args);

}
