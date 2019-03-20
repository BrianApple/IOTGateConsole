package IOTGateConsole.chache;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月20日
 */
public class CommonLocalCache {
	private CommonLocalCache(){
		throw new AssertionError();
	}
	/**
	 * 缓存所有rpcNode IP
	 */
	public static List<String> rpcServerCache = new CopyOnWriteArrayList<>();

	
}
