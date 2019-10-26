package IOTGateConsole.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import IOTGateConsole.databridge.ReqWebData;
import IOTGateConsole.databridge.RetData;
import IOTGateConsole.databridge.Strategy;
import IOTGateConsole.domain.User;
import IOTGateConsole.service.RpcService;
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
	RpcService rpcService;
	@RequestMapping("/index")
	public String initPage(){
//		name,phone,create_time,age
		User user=new User();
		user.setName("326");
		user.setPhone("326");
		user.setCreate_time(new Date());
		user.setAge(1);
		return "index";
	}
	
	@RequestMapping("/gateData")
	@ResponseBody
	public RetData getData(){
		RetData  ret=rpcService.getAllGateInfo();
		return ret;
	}
	
	@RequestMapping("/addOneStrategy")
	@ResponseBody
	public RetData addOneStrategy(ReqWebData args){
		//{bigdian=1, leftLen=1, len2=1, lenOffset=1, lenrange=1, pid=1, port=1}
		RetData  ret=rpcService.addOneStrategy(args);
		return ret;
//		return new RetData();//用于演示版代码
	}
	
	@RequestMapping("/getAllStrategeFromDB")
	@ResponseBody
	public RetData getAllStrategeFromDB(){
		RetData  ret=rpcService.getAllStrategeFromDB();
		return ret;
	}
	
	@RequestMapping("/getAllStrategyAllInfo")
	@ResponseBody
	public RetData getAllStrategyAllInfo(){
		RetData  ret=rpcService.getAllStrategyAllInfo();
		return ret;
	}
	
	@RequestMapping("/updateStrategyNode")
	@ResponseBody
	public RetData updateStrategy2Node(ReqWebData args){
//		RetData  ret=rpcService.updateStrategy2Node(args);
//		return ret;
		return new RetData();//用于演示版代码
	}
	@RequestMapping("/delOneStrategyByPID")
	@ResponseBody
	public RetData delOneStrategyByPID(ReqWebData args){
		RetData  ret=rpcService.delOneStrategyByPID(args);
		return ret;
//		return new RetData();//用于演示版代码
	}
}
