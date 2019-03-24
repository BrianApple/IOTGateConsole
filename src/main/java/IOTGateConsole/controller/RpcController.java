package IOTGateConsole.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rpc")
@PropertySource(value = {"classpath:application.properties"},encoding="utf-8")  
public class RpcController {
	@Value("${zkAddr}")
	private String zkAddr;
	@RequestMapping("/index")
	public String initPage(){
		System.out.println(zkAddr);
		return "aaa";
	}
	
	
}
