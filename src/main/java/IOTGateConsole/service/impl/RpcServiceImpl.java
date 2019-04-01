package IOTGateConsole.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import IOTGateConsole.chache.CommonLocalCache;
import IOTGateConsole.databridge.ReqWebData;
import IOTGateConsole.databridge.ResponseData;
import IOTGateConsole.databridge.RetData;
import IOTGateConsole.rpc.service.RPCExportService;
import IOTGateConsole.service.RpcService;
/**
 * 
 * @Description: 
 * @author  yangcheng
 * @date:   2019年4月1日
 */
@Service
public class RpcServiceImpl implements RpcService{

	@SuppressWarnings("unchecked")
	@Override
	public RetData getAllGateInfo() {
		List<String> allNodes = CommonLocalCache.rpcServerCache;
		RetData ret = new RetData();
		List<Object> retData = new ArrayList<Object>();
		
		for (String ip : allNodes) {
			Map<String,String> aStrategy = new HashMap<>();
			aStrategy.put("ip", ip);
			RPCExportService rpcExportService = CommonLocalCache.rpcProxys.get(ip);
			if(rpcExportService != null){
				aStrategy.put("stat", "ok");
				ResponseData  data= rpcExportService.getAllProtocal();
				Map retMap = (Map) data.getData().get(0);
				Iterator<Map.Entry<String, String>> it= retMap.entrySet().iterator();
				String str = "";;
				while(it.hasNext()){
					String[] arg = it.next().getValue().split("\\,");;
					System.out.println("规约类型"+arg[0]);
					str += (arg[0]+":"+arg[arg.length-1]+"/ ");
				}
				aStrategy.put("data", str.substring(0, str.length()-1));
			}else{
				aStrategy.put("stat", "error");
			}
			retData.add(aStrategy);
		}
		ret.setRetSig(200);
		ret.setData(retData);
		return ret;
	}

	@Override
	public RetData addOneStrategy(ReqWebData args) {
		RetData ret = new RetData();
		@SuppressWarnings("unchecked")
		Map<String ,String > data = args.getData();
		//{bigdian=1, leftLen=1, len2=1, lenOffset=1, lenrange=1, pid=1, port=1}
		List<Integer> strategy = new ArrayList<>();
		strategy.add(Integer.parseInt(data.get("pid")));
		strategy.add(Integer.parseInt(data.get("bigdian")));
		strategy.add(-1);//起始字符
		strategy.add(Integer.parseInt(data.get("lenOffset")));
		strategy.add(Integer.parseInt(data.get("lenrange")));
		strategy.add(Integer.parseInt(data.get("lenInfo")));
		strategy.add(Integer.parseInt(data.get("leftLen")));
		strategy.add(Integer.parseInt(data.get("port")));
		
		List<String> allNodes = CommonLocalCache.rpcServerCache;
		/**
		 * 所有节点添加新规约--不启动
		 */
		for (String ip : allNodes) {
			RPCExportService rpcExportService = CommonLocalCache.rpcProxys.get(ip);
			if(rpcExportService != null){
				rpcExportService.addNewProtocal(data.get("pid"), strategy, false);
			}
		}
		ret.setRetSig(200);
		return ret;
	}

	@Override
	public RetData getAllStrategeFromDB() {
		// TODO Auto-generated method stub
		RetData ret = new RetData();
		List<Object> retData = new ArrayList<Object>();
		Map<String, String> str= new HashMap<>();
		
		str.put("MQTT规约", "3");
		str.put("国网规约1", "1");
		str.put("国网规约2", "2");
		
		retData.add(str);
		ret.setData(retData);
		ret.setRetSig(200);
		return ret;
	}

	@Override
	public RetData updateStrategy2Node(ReqWebData args) {
		// TODO Auto-generated method stub
		List<Object> data = args.getDataList();
//		Iterator<Map.Entry<String, String>> it = data.entrySet().iterator();
//		while(it.hasNext()){
//			Map.Entry<String, String> entry = it.next();
//			String key = entry.getKey();
//			String value = entry.getKey();
//		}
		String ip = args.getStr();
		for (Object object : data) {
			Integer pid = Integer.parseInt(object.toString());
		}
		RetData ret = new RetData();
		ret.setRetSig(200);
		return ret;
	}

}
