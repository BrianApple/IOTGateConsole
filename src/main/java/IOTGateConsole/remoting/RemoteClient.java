package IOTGateConsole.remoting;

import IOTGateConsole.codec.RpcDecoder;
import IOTGateConsole.codec.RpcEncoder;
import IOTGateConsole.databridge.RequestData;
import IOTGateConsole.databridge.ResponseData;
import IOTGateConsole.util.ThreadFactoryImpl;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

public class RemoteClient {
	
	private final Bootstrap bootstrap = new Bootstrap();
    private final EventLoopGroup eventLoopGroupWorker;
    private final Object obj = new Object();
    private ResponseData responseData = null;
	public RemoteClient(){
		eventLoopGroupWorker = new NioEventLoopGroup(1, new ThreadFactoryImpl("netty_rpc_client_", false));
	}

	public ResponseData start(String ip ,int port ,RequestData requestData) throws InterruptedException{
		 Bootstrap handler = this.bootstrap.group(this.eventLoopGroupWorker).channel(NioSocketChannel.class)//
		            //
		            .option(ChannelOption.TCP_NODELAY, true)
		            //
		            .option(ChannelOption.SO_KEEPALIVE, false)
		            //
		            .option(ChannelOption.SO_SNDBUF, 65535)
		            //
		            .option(ChannelOption.SO_RCVBUF, 65535)
		            //
		            .handler(new ChannelInitializer<SocketChannel>() {
		                @Override
		                public void initChannel(SocketChannel ch) throws Exception {
		                    ch.pipeline().addLast(//
		                    		 new RpcEncoder(), //
		                             new RpcDecoder(), 
		                        new IdleStateHandler(0, 0, 120),//
		                        new NettyConnetManageHandler(), //
		                        new NettyClientHandler());//获取数据
		                }
		            });
		ChannelFuture channelFuture=handler.connect(ip, port).sync();
		channelFuture.channel().writeAndFlush(requestData).sync();
		System.out.println("rpc调用成功等待数据响应......");
		synchronized (obj) {
			obj.wait();
		}
		if (responseData != null) {
			channelFuture.channel().closeFuture().sync();
		}
		return responseData;
	}
	
	
	class NettyConnetManageHandler extends ChannelDuplexHandler {
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            super.channelRegistered(ctx);
        }


        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            super.channelUnregistered(ctx);
        }


        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);

        }


        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        }


        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent evnet = (IdleStateEvent) evt;
                if (evnet.state().equals(IdleState.ALL_IDLE)) {
                    ctx.channel().close();
                }
            }

            ctx.fireUserEventTriggered(evt);
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.channel().close();
        }
    }
	
	class NettyClientHandler extends SimpleChannelInboundHandler<ResponseData> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ResponseData msg) throws Exception {
        	/**
        	 * 调用的处理响应数据的方法
        	 */
        	RemoteClient.this.responseData = msg;
        	synchronized (RemoteClient.this.obj) {
        		RemoteClient.this.obj.notifyAll();
    		}
        	System.out.println("rpc客户端收到响应数据："+msg.getReturnCode());
        }
    }
}
