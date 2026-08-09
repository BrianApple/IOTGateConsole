package IOTGateConsole.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import IOTGateConsole.chache.CommonLocalCache;

/**
 * 节点状态监控与 SSE 推送服务
 *
 * 替代原 Zookeeper 节点事件监听：
 * 定时探测各网关节点 10916 端口的 TCP 连通性，
 * 节点状态发生变化(上线/离线)时通过 SSE 推送给前端。
 *
 * @author yangcheng
 * @date:   2026年8月9日
 */
@Service
public class NodeMonitorService {

	private static final Logger log = LoggerFactory.getLogger(NodeMonitorService.class);

	/** RPC 默认端口 */
	private static final int RPC_PORT = 10916;

	/** 探测周期(秒) */
	private static final long MONITOR_INTERVAL_SECONDS = 10;

	/** 所有已连接的 SSE 客户端 */
	private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

	/** 各节点在线状态缓存 ip -> online */
	private final Map<String, Boolean> nodeStatusCache = new ConcurrentHashMap<>();

	private ScheduledExecutorService scheduler;

	@PostConstruct
	public void init() {
		scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread t = new Thread(r, "node-monitor");
			t.setDaemon(true);
			return t;
		});
		scheduler.scheduleAtFixedRate(this::monitorNodes, 3, MONITOR_INTERVAL_SECONDS, TimeUnit.SECONDS);
		log.info("节点状态监控服务已启动，探测周期 {}s", MONITOR_INTERVAL_SECONDS);
	}

	@PreDestroy
	public void destroy() {
		if (scheduler != null) {
			scheduler.shutdownNow();
		}
	}

	/**
	 * 注册 SSE 客户端连接
	 */
	public SseEmitter registerEmitter() {
		SseEmitter emitter = new SseEmitter(0L); // 不超时
		emitters.add(emitter);
		emitter.onCompletion(() -> emitters.remove(emitter));
		emitter.onTimeout(() -> emitters.remove(emitter));
		emitter.onError(e -> emitters.remove(emitter));
		// 连接建立后立即推送一次当前节点状态快照
		try {
			emitter.send(SseEmitter.event().name("snapshot").data(buildSnapshot()));
		} catch (IOException e) {
			log.debug("SSE 快照推送失败: {}", e.getMessage());
		}
		return emitter;
	}

	/**
	 * 定时探测所有节点状态
	 */
	private void monitorNodes() {
		List<String> nodes = CommonLocalCache.rpcServerCache;
		for (String ip : nodes) {
			boolean online = isNodeOnline(ip);
			Boolean prev = nodeStatusCache.get(ip);
			if (prev == null || prev != online) {
				nodeStatusCache.put(ip, online);
				log.info("节点状态变化: {} -> {}", ip, online ? "上线" : "离线");
				broadcastNodeStatus(ip, online);
			}
		}
	}

	/**
	 * TCP 探测节点 10916 端口
	 */
	private boolean isNodeOnline(String ip) {
		String host = ip;
		int port = RPC_PORT;
		if (ip.contains(":")) {
			String[] parts = ip.split(":");
			host = parts[0];
			try {
				port = Integer.parseInt(parts[1]);
			} catch (NumberFormatException ignored) {
			}
		}
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), 2000);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * 广播节点状态变化事件
	 */
	private void broadcastNodeStatus(String ip, boolean online) {
		String data = String.format("{\"ip\":\"%s\",\"online\":%s,\"time\":%d}", ip, online, System.currentTimeMillis());
		broadcast("node-status", data);
	}

	/**
	 * 广播规约变更事件(新增/删除规约后调用)
	 */
	public void broadcastStrategyChange(String action, String pid) {
		String data = String.format("{\"action\":\"%s\",\"pid\":\"%s\",\"time\":%d}", action, pid, System.currentTimeMillis());
		broadcast("strategy-change", data);
	}

	/**
	 * 向所有 SSE 客户端广播事件
	 */
	private void broadcast(String eventName, String data) {
		for (SseEmitter emitter : emitters) {
			try {
				emitter.send(SseEmitter.event().name(eventName).data(data));
			} catch (IOException | IllegalStateException e) {
				emitters.remove(emitter);
			}
		}
	}

	/**
	 * 构建节点状态快照 JSON
	 */
	private String buildSnapshot() {
		StringBuilder sb = new StringBuilder("[");
		List<String> nodes = CommonLocalCache.rpcServerCache;
		for (int i = 0; i < nodes.size(); i++) {
			String ip = nodes.get(i);
			boolean online = nodeStatusCache.getOrDefault(ip, isNodeOnline(ip));
			nodeStatusCache.put(ip, online);
			if (i > 0) {
				sb.append(",");
			}
			sb.append(String.format("{\"ip\":\"%s\",\"online\":%s}", ip, online));
		}
		sb.append("]");
		return sb.toString();
	}
}
