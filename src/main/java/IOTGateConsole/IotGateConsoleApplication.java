package IOTGateConsole;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import IOTGateConsole.remoting.RemoteClient;

@SpringBootApplication
public class IotGateConsoleApplication {

	public static void start(){
		RemoteClient client = new RemoteClient();
		try {
			client.start("127.0.0.1", 10916);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		SpringApplication.run(IotGateConsoleApplication.class, args);
		start();
	}

}
