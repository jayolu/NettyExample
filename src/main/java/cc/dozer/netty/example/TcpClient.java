package cc.dozer.netty.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Dozer @ 5/24/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class TcpClient {
    private volatile EventLoopGroup workerGroup;

    private volatile Bootstrap bootstrap;

    private volatile boolean closed = false;

    private final String remoteHost;

    private final int remotePort;
    private volatile Channel ch;

    public TcpClient(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    public void close() {
        closed = true;
        workerGroup.shutdownGracefully();
        System.out.println("Stopped Tcp Client: " + getServerInfo());
    }

    public void init() {
        closed = false;

        workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addFirst(new ChannelInboundHandlerAdapter() {
                    @Override
					public void channelActive(ChannelHandlerContext ctx) throws Exception {
						super.channelActive(ctx);
						System.out.println("client active");
					}

					@Override
					public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
						super.exceptionCaught(ctx, cause);
						System.out.println("client exception");
					}

					@Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        super.channelInactive(ctx);
                        ctx.channel().eventLoop().schedule(new Runnable() {
							
							@Override
							public void run() {
								doConnect();								
							}
						}, 1, TimeUnit.SECONDS);
                    }
                });
                
                pipeline.addLast("ping", new IdleStateHandler(60, 15, 6,TimeUnit.SECONDS));
                pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192,
                        Delimiters.lineDelimiter()));
                pipeline.addLast("decoder", new StringDecoder());
                pipeline.addLast("encoder", new StringEncoder());
                // ¿Í»§¶ËµÄÂß¼­
                pipeline.addLast("handler", new ClientHandler());

                //todo: add more handler
            }
        });

        doConnect();
    }

    private void doConnect() {
        if (closed) {
            return;
        }

        ChannelFuture future = bootstrap.connect(new InetSocketAddress(remoteHost, remotePort));
        System.out.println(Thread.currentThread().getName() + " Client start connectting Tcp Client : " + getServerInfo());

        future.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture f) throws Exception {
                if (f.isSuccess()) {
                	ch = f.channel();
                    System.out.println(Thread.currentThread().getName() + " Client connect Tcp Client Success: " + getServerInfo());
                } else {
                    System.out.println(Thread.currentThread().getName() + " Client connect Failed: " + getServerInfo());
                    f.channel().eventLoop().schedule(new Runnable() {
						
						@Override
						public void run() {
							doConnect();
						}
					}, 1, TimeUnit.SECONDS);
                }
            }
        });
    }

    private String getServerInfo() {
        return String.format("RemoteHost=%s RemotePort=%d",
                remoteHost,
                remotePort);
    }
    
    public void send(String msg) {
    	if(ch!=null && ch.isActive()) {
    		ch.writeAndFlush(msg);
    	} else {
    		System.out.println("connect error");
    	}
    }
}
