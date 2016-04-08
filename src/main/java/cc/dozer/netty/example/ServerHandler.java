package cc.dozer.netty.example;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class ServerHandler extends SimpleChannelInboundHandler<String> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		System.out.println(ctx.channel().remoteAddress() + " Say : " + msg);
        if ("OK".equals(msg)) {
            //客户端心跳返回忽略
        } else if("ping".equals(msg)) {
        	//客户端主动心跳
        	ctx.channel().writeAndFlush("OK\n");
        } else {
        	//业务逻辑
        }
		
	}

	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)) {
                System.out.println("Server READER_IDLE");
                // 超过时间没有收到请求，服务器主动关闭channel
                ctx.close();
            } else if (event.state().equals(IdleState.WRITER_IDLE)) {
                System.out.println("Server WRITER_IDLE");
            } else if (event.state().equals(IdleState.ALL_IDLE)) {
                System.out.println("Server ALL_IDLE");
                // 发送心跳，注意要使用writeAndFlush，使用write由于包太小，可能会不直接发送
                ctx.channel().writeAndFlush("ping\n");
            }
        }
        super.userEventTriggered(ctx, evt); 
    }
	

}
