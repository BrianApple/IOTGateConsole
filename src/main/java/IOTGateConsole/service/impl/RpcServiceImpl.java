package IOTGateConsole.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import IOTGateConsole.chache.CommonLocalCache;
import IOTGateConsole.databridge.ResponseData;
import IOTGateConsole.databridge.RetData;
import IOTGateConsole.rpc.service.RPCExportService;
import IOTGateConsole.service.RpcService;
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

}
