package cc.dozer.netty.example;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class ClientHandler extends SimpleChannelInboundHandler<String> {


	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)) {
                System.out.println("Client READER_IDLE");
            } else if (event.state().equals(IdleState.WRITER_IDLE)) {
                System.out.println("Client WRITER_IDLE");
            } else if (event.state().equals(IdleState.ALL_IDLE)) {
                System.out.println("Client ALL_IDLE");
                // 发送心跳，注意要使用writeAndFlush，使用write由于包太小，可能会不直接发送
                ctx.channel().writeAndFlush("ping\n");
            }
        }
		super.userEventTriggered(ctx, evt);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		 ctx.channel().writeAndFlush("hell world \n");
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		System.out.println("Server say : " + msg);
		 
        if ("ping".equals(msg)) {
        	//服务器主动的心跳包
            ctx.channel().writeAndFlush("OK\n");
        } else if("OK".equals(msg)) {
        	//服务器心跳返回忽略
        } else {
            //业务逻辑
    		System.out.println("receiver : " + msg);
        }

	}

}
