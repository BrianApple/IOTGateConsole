package IOTGateConsole.codec;

import IOTGateConsole.databridge.RequestData;
import IOTGateConsole.databridge.ResponseData;
import IOTGateConsole.util.MixAll;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月18日
 */
public class RpcEncoder extends MessageToByteEncoder<RequestData> {

	@Override
	protected void encode(ChannelHandlerContext ctx, RequestData msg, ByteBuf out) throws Exception {
		byte[] data = MixAll.encode(msg);
		System.out.println("rpc客户端发送数据长度="+data.length);
		out.writeShort(data.length);
		out.writeBytes(data);
	}

}
