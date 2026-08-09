package IOTGateConsole.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import IOTGateConsole.chache.CommonLocalCache;
import IOTGateConsole.rpc.proxy.RPCRequestProxy;
import IOTGateConsole.rpc.service.RPCExportService;
import IOTGateConsole.service.RpcService;

/**
 * 网关节点静态配置加载器
 *
 * 替代原 Zookeeper 节点注册发现机制：
 * 网关节点地址通过 application.properties 中的 gate.nodes 配置，
 * 程序启动时将节点加载进本地缓存并创建 RPC 代理。
 *
 * @author yangcheng
 * @date:   2026年8月9日
 */
@Component
public class GateNodeConfig implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(GateNodeConfig.class);

	@Value("${gate.nodes:}")
	private String gateNodes;

	@Autowired
	private RpcService rpcService;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (gateNodes == null || gateNodes.trim().isEmpty()) {
			log.warn("未配置 gate.nodes，控制台将无法发现任何网关节点。请在 application.properties 中配置，如 gate.nodes=192.168.1.10,192.168.1.11");
			return;
		}
		String[] nodes = gateNodes.split(",");
		for (String node : nodes) {
			String ip = node.trim();
			if (ip.isEmpty()) {
				continue;
			}
			// 兼容 ip:port 格式，默认 RPC 端口 10916
			String rpcIp = ip;
			if (ip.contains(":")) {
				rpcIp = ip.split(":")[0];
			}
			if (!CommonLocalCache.rpcServerCache.contains(rpcIp)) {
				CommonLocalCache.rpcServerCache.add(rpcIp);
			}
			if (!CommonLocalCache.rpcProxys.containsKey(rpcIp)) {
				RPCExportService proxy = new RPCRequestProxy(rpcIp).create(RPCExportService.class);
				CommonLocalCache.rpcProxys.put(rpcIp, proxy);
			}
			log.info("已加载网关节点: {}", rpcIp);
			// 节点规约同步
			try {
				rpcService.synchonizeStrategy(rpcIp);
			} catch (Exception e) {
				log.warn("节点 {} 规约同步失败(可能未在线): {}", rpcIp, e.getMessage());
			}
		}
		log.info("网关节点加载完成，共 {} 个节点", CommonLocalCache.rpcServerCache.size());
	}
}
