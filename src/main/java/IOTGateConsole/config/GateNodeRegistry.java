package IOTGateConsole.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import IOTGateConsole.chache.CommonLocalCache;
import IOTGateConsole.databridge.GateNodeInfo;

/**
 * 网关节点注册表（内存版）
 *
 * 网关启动时通过 -c -r &lt;consoleIp&gt; 主动注册到本服务：
 *  - POST /gate/register     注册(加入注册表 + RPC缓存)
 *  - POST /gate/heartbeat    心跳(刷新存活时间)
 *  - POST /gate/unregister   反注册(主动下线)
 *  - 定时扫描：超过 gate.registry.heartbeat-timeout-seconds 未心跳的节点判离线并移除
 *
 * 注册表与 application.properties 中 gate.nodes 静态配置并存：
 * 主动注册的节点优先，静态配置作为兜底。
 *
 * @author yangcheng
 * @date:   2026年8月10日
 */
@Component
public class GateNodeRegistry {

	private static final Logger log = LoggerFactory.getLogger(GateNodeRegistry.class);

	/** 心跳超时阈值(秒)，超过该时间未收到心跳判离线 */
	@Value("${gate.registry.heartbeat-timeout-seconds:30}")
	private long heartbeatTimeoutSeconds = 30;

	/** 离线节点清理扫描周期(秒) */
	@Value("${gate.registry.scan-interval-seconds:10}")
	private long scanIntervalSeconds = 10;

	/** 注册表: ip -> 节点信息 */
	private final Map<String, GateNodeInfo> nodes = new ConcurrentHashMap<>();

	private ScheduledExecutorService scheduler;

	@PostConstruct
	public void init() {
		scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread t = new Thread(r, "gate-registry-cleaner");
			t.setDaemon(true);
			return t;
		});
		scheduler.scheduleAtFixedRate(this::cleanupTimeoutNodes, scanIntervalSeconds, scanIntervalSeconds, TimeUnit.SECONDS);
		log.info("网关注册表已启动，心跳超时 {}s，扫描周期 {}s", heartbeatTimeoutSeconds, scanIntervalSeconds);
	}

	@PreDestroy
	public void destroy() {
		if (scheduler != null) {
			scheduler.shutdownNow();
		}
	}

	/**
	 * 注册或刷新节点
	 * @return 若节点为新注册返回 true，否则 false
	 */
	public boolean register(GateNodeInfo info) {
		boolean isNew = false;
		GateNodeInfo exist = nodes.get(info.getIp());
		if (exist == null) {
			nodes.put(info.getIp(), info);
			isNew = true;
		} else {
			exist.setRpcPort(info.getRpcPort() > 0 ? info.getRpcPort() : exist.getRpcPort());
			exist.setGateNum(info.getGateNum());
			exist.setOnline(true);
		}
		touch(info.getIp());
		return isNew;
	}

	/**
	 * 心跳：刷新节点最近存活时间
	 * @return 节点是否在注册表中
	 */
	public boolean heartbeat(String ip) {
		GateNodeInfo info = nodes.get(ip);
		if (info == null) {
			return false;
		}
		info.setOnline(true);
		info.setLastHeartbeat(System.currentTimeMillis());
		return true;
	}

	/**
	 * 反注册：移除节点
	 */
	public GateNodeInfo unregister(String ip) {
		GateNodeInfo removed = nodes.remove(ip);
		if (removed != null) {
			// 同步移除RPC缓存与代理
			CommonLocalCache.rpcServerCache.remove(ip);
			CommonLocalCache.rpcProxys.remove(ip);
			log.info("网关节点反注册: {}", ip);
		}
		return removed;
	}

	/**
	 * 节点快照
	 */
	public List<GateNodeInfo> snapshot() {
		return new ArrayList<>(nodes.values());
	}

	public boolean isRegistered(String ip) {
		return nodes.containsKey(ip);
	}

	/**
	 * 记录注册时间(等价于一次心跳)
	 */
	private void touch(String ip) {
		GateNodeInfo info = nodes.get(ip);
		if (info != null) {
			info.setLastHeartbeat(System.currentTimeMillis());
		}
	}

	/**
	 * 定时清理：超过心跳超时阈值未上报的节点判离线并移除
	 */
	private void cleanupTimeoutNodes() {
		long now = System.currentTimeMillis();
		long timeoutMs = heartbeatTimeoutSeconds * 1000L;
		for (GateNodeInfo info : nodes.values()) {
			if (now - info.getLastHeartbeat() > timeoutMs) {
				log.warn("网关节点心跳超时({}s)，判离线并移除: {}", heartbeatTimeoutSeconds, info.getIp());
				unregister(info.getIp());
			}
		}
	}
}
