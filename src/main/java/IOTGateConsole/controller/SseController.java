package IOTGateConsole.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import IOTGateConsole.service.NodeMonitorService;

/**
 * SSE 实时推送控制器
 *
 * 前端通过 EventSource 连接 /rpc/events，
 * 实时接收网关节点状态变化、规约变更等推送事件。
 * 替代原 Zookeeper 节点事件监听机制。
 *
 * @author yangcheng
 * @date:   2026年8月9日
 */
@Controller
@RequestMapping("/rpc")
public class SseController {

	@Autowired
	private NodeMonitorService nodeMonitorService;

	/**
	 * SSE 事件流端点
	 */
	@GetMapping("/events")
	@ResponseBody
	public SseEmitter events() {
		return nodeMonitorService.registerEmitter();
	}
}
