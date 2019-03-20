package IOTGateConsole;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import IOTGateConsole.chache.CommonLocalCache;
import IOTGateConsole.remoting.RemoteClient;
import IOTGateConsole.rpc.proxy.RPCRequestProxy;
import IOTGateConsole.rpc.service.RPCExportService;
import IOTGateConsole.rpc.zk.ZKFramework;

@SpringBootApplication
public class IotGateConsoleApplication {

	public static void start(){
		new ZKFramework().start("192.168.18.27:2181,192.168.18.27:2182,192.168.18.27:2183");
		try {
			//zk连接比较慢
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String rpcServerIP = CommonLocalCache.rpcServerCache.get(0);
		RPCExportService service = new RPCRequestProxy(rpcServerIP).create(RPCExportService.class) ;
		service.test("你好rpc");
	}
	
	public static void main(String[] args) {
//		SpringApplication.run(IotGateConsoleApplication.class, args);
		start();
	}

}
