package IOTGateConsole;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@MapperScan(basePackages={"IOTGateConsole.dao"})
@EnableTransactionManagement
public class IotGateConsoleApplication {

	
	public static void main(String[] args) {
		SpringApplication.run(IotGateConsoleApplication.class, args);
 
	}
	

}
