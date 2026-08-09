package IOTGateConsole.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import IOTGateConsole.chache.CommonLocalCache;
import IOTGateConsole.config.GateNodeRegistry;
import IOTGateConsole.databridge.GateNodeInfo;
import IOTGateConsole.databridge.RetData;
import IOTGateConsole.rpc.proxy.RPCRequestProxy;
import IOTGateConsole.rpc.service.RPCExportService;
import IOTGateConsole.service.RpcService;

/**
 * 网关注册接口
 *
 * 网关启动时通过 -c -r &lt;consoleIp&gt; 主动注册到本服务，替代静态 gate.nodes 配置：
 *  - POST /gate/register     注册：加入注册表 + RPC缓存 + 立即同步规约
 *  - POST /gate/heartbeat    心跳：刷新节点存活时间（未知节点返回 retSig=404，网关将重新注册）
 *  - POST /gate/unregister   反注册：移除节点（网关正常关闭时调用，尽力而为）
 *  - GET  /gate/nodes        查看当前注册表中的节点
 *
 * @author yangcheng
 * @date:   2026年8月10日
 */
@RestController
@RequestMapping("/gate")
public class GateRegistryController {

	private static final Logger log = LoggerFactory.getLogger(GateRegistryController.class);

	/** RPC 默认端口 */
	private static final int DEFAULT_RPC_PORT = 10916;

	@Autowired
	private GateNodeRegistry gateNodeRegistry;

	@Autowired
	private RpcService rpcService;

	/**
	 * 网关注册
	 */
	@PostMapping("/register")
	public RetData register(@RequestBody(required = false) GateNodeInfo info, HttpServletRequest request) {
		RetData ret = new RetData();
		String ip = resolveIp(info != null ? info.getIp() : null, request);
		int rpcPort = (info != null && info.getRpcPort() > 0) ? info.getRpcPort() : DEFAULT_RPC_PORT;
		int gateNum = info != null ? info.getGateNum() : 0;

		GateNodeInfo nodeInfo = new GateNodeInfo();
		nodeInfo.setIp(ip);
		nodeInfo.setRpcPort(rpcPort);
		nodeInfo.setGateNum(gateNum);
		boolean isNew = gateNodeRegistry.register(nodeInfo);

		// 加入 RPC 缓存并创建代理（复用原有 rpcServerCache/rpcProxys 链路，前端节点管理页自动可见）
		if (!CommonLocalCache.rpcServerCache.contains(ip)) {
			CommonLocalCache.rpcServerCache.add(ip);
		}
		if (!CommonLocalCache.rpcProxys.containsKey(ip)) {
			try {
				RPCExportService proxy = new RPCRequestProxy(ip).create(RPCExportService.class);
				CommonLocalCache.rpcProxys.put(ip, proxy);
			} catch (Exception e) {
				log.warn("创建节点 {} RPC代理失败: {}", ip, e.getMessage());
			}
		}
		// 注册成功立即同步规约（与静态配置加载行为一致）
		try {
			rpcService.synchonizeStrategy(ip);
		} catch (Exception e) {
			log.warn("节点 {} 规约同步失败(可能RPC未就绪): {}", ip, e.getMessage());
		}

		List<Object> data = new ArrayList<>();
		Map<String, Object> node = new HashMap<>();
		node.put("ip", ip);
		node.put("rpcPort", rpcPort);
		node.put("gateNum", gateNum);
		node.put("isNew", isNew);
		data.add(node);
		ret.setRetSig(200);
		ret.setData(data);
		log.info("网关节点注册成功: {} (rpcPort={}, gateNum={})", ip, rpcPort, gateNum);
		return ret;
	}

	/**
	 * 网关心跳
	 */
	@PostMapping("/heartbeat")
	public RetData heartbeat(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
		RetData ret = new RetData();
		String ip = resolveIp(body != null ? (String) body.get("ip") : null, request);
		boolean known = gateNodeRegistry.heartbeat(ip);
		if (known) {
			ret.setRetSig(200);
		} else {
			// 节点不在注册表中（如Console重启后注册表丢失），返回404提示网关重新注册
			ret.setRetSig(404);
		}
		return ret;
	}

	/**
	 * 网关反注册（正常关闭时调用）
	 */
	@PostMapping("/unregister")
	public RetData unregister(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
		RetData ret = new RetData();
		String ip = resolveIp(body != null ? (String) body.get("ip") : null, request);
		gateNodeRegistry.unregister(ip);
		ret.setRetSig(200);
		log.info("网关节点反注册: {}", ip);
		return ret;
	}

	/**
	 * 查看当前注册表中的节点
	 */
	@GetMapping("/nodes")
	public RetData nodes() {
		RetData ret = new RetData();
		List<Object> data = new ArrayList<>();
		for (GateNodeInfo info : gateNodeRegistry.snapshot()) {
			Map<String, Object> node = new HashMap<>();
			node.put("ip", info.getIp());
			node.put("rpcPort", info.getRpcPort());
			node.put("gateNum", info.getGateNum());
			node.put("online", info.isOnline());
			node.put("lastHeartbeat", info.getLastHeartbeat());
			data.add(node);
		}
		ret.setRetSig(200);
		ret.setData(data);
		return ret;
	}

	/**
	 * 解析节点IP：优先取请求体中的 ip，未传则取HTTP请求来源IP；IPv6回环归一化为127.0.0.1
	 */
	private String resolveIp(String bodyIp, HttpServletRequest request) {
		String ip = (bodyIp == null || bodyIp.trim().isEmpty()) ? request.getRemoteAddr() : bodyIp.trim();
		if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
			ip = "127.0.0.1";
		}
		return ip;
	}
}
