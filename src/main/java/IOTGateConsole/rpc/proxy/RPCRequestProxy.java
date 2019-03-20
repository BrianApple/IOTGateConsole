package IOTGateConsole.rpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import IOTGateConsole.databridge.RequestData;
import IOTGateConsole.databridge.ResponseData;
import IOTGateConsole.remoting.RemoteClient;

/**
 * 请求代理类
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月19日
 */
public class RPCRequestProxy {
	
	//TODO 缓存所有rpc服务节点信息
	private String rpcServerIP;
	
	public RPCRequestProxy(String rpcServerIP) {
		super();
		this.rpcServerIP = rpcServerIP;
	}

	@SuppressWarnings("unchecked")
	public <T> T create(Class<?> clazz){
		return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class<?>[]{clazz}, new InvocationHandler() {
					
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						// TODO Auto-generated method stub
						RequestData requestData = new RequestData();
						requestData.setRequestNum(UUID.randomUUID().toString());
						requestData.setClassName(method.getDeclaringClass().getSimpleName());//获取方法所在类名称
						requestData.setMethodName(method.getName());
						requestData.setParamTyps(method.getParameterTypes());
						requestData.setArgs(args);
						
						RemoteClient remoteClient = new RemoteClient();
						//TODO  改为从zookeeper获取rpc数据
						ResponseData responseData = remoteClient.start(rpcServerIP, 10916, requestData);
						return responseData;
					}
				});
	}

}
