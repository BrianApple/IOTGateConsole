package IOTGateConsole.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import IOTGateConsole.chache.CommonLocalCache;
import IOTGateConsole.dao.IotGateMapper;
import IOTGateConsole.databridge.ReqWebData;
import IOTGateConsole.databridge.ResponseData;
import IOTGateConsole.databridge.RetData;
import IOTGateConsole.domain.IotGateDB;
import IOTGateConsole.rpc.service.RPCExportService;
import IOTGateConsole.service.RpcService;
/**
 * rpc调用类
 * @Description: 
 * @author  yangcheng
 * @date:   2019年4月1日
 */
@Service
public class RpcServiceImpl implements RpcService{
	@Autowired
	private IotGateMapper iotGateMapper;
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
				ResponseData  data= rpcExportService.getAllProtocal(true);
				Map retMap = (Map) data.getData().get(0);
				Iterator<Map.Entry<String, String>> it= retMap.entrySet().iterator();
				String str = "";;
				while(it.hasNext()){
					String[] arg = it.next().getValue().split("\\,");;
					str += (arg[0]+":"+arg[arg.length-1]+"/ ");
				}
				if(!"".equals(str)){
					aStrategy.put("data", str.substring(0, str.length()-1));
				}
			}else{
				aStrategy.put("stat", "error");
			}
			retData.add(aStrategy);
		}
		ret.setRetSig(200);
		ret.setData(retData);
		return ret;
	}

	@Transactional
	@Override
	public RetData addOneStrategy(ReqWebData args) {
		RetData ret = new RetData();
		@SuppressWarnings("unchecked")
		Map<String ,String > data = args.getData();
		//{bigdian=1, leftLen=1, len2=1, lenOffset=1, lenrange=1, pid=1, port=1}
		String strategyName = data.get("straName");
		
		//写数据库
		IotGateDB iotgate = new IotGateDB();
		iotgate.setId(UUID.randomUUID().toString());
		iotgate.setpId(Integer.parseInt(data.get("pid")));
		iotgate.setpName(strategyName);
		iotgate.setIsBigEndian(Integer.parseInt(data.get("bigdian")));
		iotgate.setBeginHexVal("-1");
		iotgate.setLengthFieldOffset(Integer.parseInt(data.get("lenOffset")));
		iotgate.setLengthFieldLength(Integer.parseInt(data.get("lenrange")));
		iotgate.setIsDataLenthIncludeLenthFieldLenth(Integer.parseInt(data.get("lenInfo")));
		iotgate.setExceptDataLenth(Integer.parseInt(data.get("leftLen")));
		iotgate.setPort(Integer.parseInt(data.get("port")));
		iotgate.setHighControll(0);//高级功能位
		iotGateMapper.insert(iotgate);
		
		
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
		 * 所有节点添加新规约--并不启动
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

	/**
	 * 只返回 规约名和规约唯一编号
	 */
	@Override
	public RetData getAllStrategeFromDB() {
		// TODO Auto-generated method stub
		RetData ret = new RetData();
		List<Object> retData = new ArrayList<Object>();
		Map<String, String> str= new HashMap<>();
		List<IotGateDB>  retList= iotGateMapper.getAllStrategy();
		for (IotGateDB iotGateDB : retList) {
			str.put(iotGateDB.getpName(), iotGateDB.getpId()+"");
		}
//		str.put("MQTT规约", "3");
		retData.add(str);
		ret.setData(retData);
		ret.setRetSig(200);
		return ret;
	}

	@Override
	public RetData updateStrategy2Node(ReqWebData args) {
		List<Object> data = args.getDataList();
		String ip = args.getStr();
		RPCExportService rpcExportService = CommonLocalCache.rpcProxys.get(ip);
		
		/**
		 * 中心思想：停运没选中的，开启选中的
		 */
		if(rpcExportService != null){
			ResponseData  rpdata= rpcExportService.getAllProtocal(true);
			Map retMap = (Map) rpdata.getData().get(0);
			Iterator<Map.Entry<String, String>> it= retMap.entrySet().iterator();
			List<String> nodeStategyPid = new ArrayList<>();//现运行的策略
			while(it.hasNext()){
				String[] arg = it.next().getValue().split("\\,");;
				nodeStategyPid.add(arg[0]);
			}
			
			
			for (Object pid : data) {
				//启动规约
				String nPid = pid.toString();
				if(nodeStategyPid.contains(nPid)){
					nodeStategyPid.remove(nPid);
				}else{
					System.out.println("启动规约"+nPid);
					rpcExportService.startProtocalServiceByPid(nPid.toString());
				}
				
				
//				IotGateDB iotGateDB = new IotGateDB();
//				iotGateDB.setpId(pid);
//				List<IotGateDB>  retList= iotGateMapper.getStrategyByPid(iotGateDB);
			}
			for (String pid : nodeStategyPid) {
				rpcExportService.stopProtocalServiceByPid(pid);
			}
		}
		
		
		RetData ret = new RetData();
		ret.setRetSig(200);
		return ret;
	}

	@Override
	public RetData getAllStrategyAllInfo() {
		
		RetData ret = new RetData();
		List<IotGateDB>  retList= iotGateMapper.getAllStrategy();
		List<Object> retData = new ArrayList<Object>();
		retData.add(retList);
		ret.setData(retData);
		ret.setRetSig(200);
		return ret;
	}

	@Transactional
	@Override
	public RetData delOneStrategyByPID(ReqWebData args) {
		RetData ret = new RetData();
		String pid = args.getStr();
		//清除节点数据
		List<String> allNodes = CommonLocalCache.rpcServerCache;
		for (String ip : allNodes) {
			RPCExportService rpcExportService = CommonLocalCache.rpcProxys.get(ip);
			if(rpcExportService != null){
				rpcExportService.delProtocalByPid(pid);
			}
		}
		//删除数据库对应数据
		IotGateDB iotgate = new IotGateDB();
		iotgate.setpId(Integer.parseInt(pid));
		iotGateMapper.delOneStrategyByPID(iotgate);
		ret.setRetSig(200);
		return ret;
	}

	@Override
	public void synchonizeStrategy(String nodeIp) {
		
		RPCExportService rpcExportService = CommonLocalCache.rpcProxys.get(nodeIp);
		if(rpcExportService != null){
			
			List<IotGateDB>  retList= iotGateMapper.getAllStrategy();
			for (IotGateDB iotGateDB : retList) {
				
				if(iotGateDB.getHighControll() == 0){
					//非高级功能
					int localPID = iotGateDB.getpId();
					//如果已经存在则不会重复添加
					rpcExportService.addNewProtocal(localPID+"", iotGateDB.toList(), false);
				}else{
					//TODO 高级功能
				}
			}
			
		}
		
	}

}
