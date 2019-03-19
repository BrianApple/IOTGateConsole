package IOTGateConsole.rpc.rpcProcessor;

import IOTGateConsole.databridge.RequestData;
import IOTGateConsole.databridge.ResponseData;

/**
 * RPC服务接口
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月17日
 */
public interface RPCClientProcessor {
	
	/**
	 * 调用rpc服务
	 * @param requestData
	 * @return
	 */
	ResponseData callServiceService(RequestData requestData);
}
