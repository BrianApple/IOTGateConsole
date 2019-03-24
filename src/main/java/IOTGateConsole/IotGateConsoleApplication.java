package IOTGateConsole;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import IOTGateConsole.chache.CommonLocalCache;
import IOTGateConsole.databridge.ResponseData;
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
//		ResponseData data = service.stopProtocalServiceByPid("2");
		ResponseData data = service.startProtocalServiceByPid("2");
		System.out.println("！！！！！！！！！！！！1consul执行结果:"+data.getReturnCode());
	}
	
	public static void main(String[] args) {
		SpringApplication.run(IotGateConsoleApplication.class, args);
//		start();
//		updateP();
//		getallP();
	}
	
	
	/**
	 * 更新规约之后 重启规约对应的服务之后才正式生效！！！
	 */
	public static void  updateP(){
		new ZKFramework().start("192.168.18.27:2181,192.168.18.27:2182,192.168.18.27:2183");
		try {
			//zk连接比较慢
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//1,0,-1,1,2,1,1,9811
		List<Integer> args2 = new ArrayList<>();
		args2.add(2);
		args2.add(0);
		args2.add(-1);
		args2.add(1);
		args2.add(2);
		args2.add(1);
		args2.add(1);
		args2.add(9812);
		String rpcServerIP = CommonLocalCache.rpcServerCache.get(0);
		RPCExportService service = new RPCRequestProxy(rpcServerIP).create(RPCExportService.class) ;
		ResponseData data = service.updateProtocalByPid("2", args2);
		if(data.getReturnCode() == 200){
			System.out.println("执行成功！！！");
		}else{
			System.err.println(data.getErroInfo());
			data.getErroInfo().printStackTrace();
		}
		
		
	}
	

	public static void getallP(){
		new ZKFramework().start("192.168.18.27:2181,192.168.18.27:2182,192.168.18.27:2183");
		try {
			//zk连接比较慢
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String rpcServerIP = CommonLocalCache.rpcServerCache.get(0);
		RPCExportService service = new RPCRequestProxy(rpcServerIP).create(RPCExportService.class) ;
//		ResponseData data = service.stopProtocalServiceByPid("2");
		ResponseData data = service.getAllProtocal();
		Map ret = (Map) data.getData().get(0);
		Iterator<Map.Entry<String, String>> it= ret.entrySet().iterator();
		while(it.hasNext()){
			System.out.println("获取到规约=="+it.next().getValue());;
		}
		
	}

}
