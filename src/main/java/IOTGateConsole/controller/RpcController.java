package IOTGateConsole.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import IOTGateConsole.domain.User;
import IOTGateConsole.service.TestService1;

@Controller
@RequestMapping("/rpc")
@PropertySource(value = {"classpath:application.properties"},encoding="utf-8")  
/**
 * 
 * Description: 
 * @author hejuanjuan
 * @date 2019年3月31日
 */
public class RpcController {
	
	@Autowired
	TestService1 testService1;
	
	@RequestMapping("/index")
	public String initPage(){
//		name,phone,create_time,age
		User user=new User();
		user.setName("326");
		user.setPhone("326");
		user.setCreate_time(new Date());
		user.setAge(1);
		int i=testService1.insert(user);
		System.out.println("添加数据，返回值：i"+i);
		return "index";
	}
	@RequestMapping("/data")
	@ResponseBody
	public User getData(){
//		name,phone,create_time,age
		User user=new User();
		user.setName("326");
		user.setPhone("326");
		user.setCreate_time(new Date());
		user.setAge(1);
		return user;
	}
}
