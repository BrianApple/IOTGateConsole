package IOTGateConsole;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import IOTGateConsole.remoting.RemoteClient;
import IOTGateConsole.rpc.proxy.RPCRequestProxy;
import IOTGateConsole.rpc.service.RPCExportService;

@SpringBootApplication
public class IotGateConsoleApplication {

	public static void start(){
		RPCExportService service = new RPCRequestProxy().create(RPCExportService.class) ;
		service.test("你好rpc");
	}
	
	public static void main(String[] args) {
		SpringApplication.run(IotGateConsoleApplication.class, args);
		start();
	}

}
