package cn.iocoder.springboot.lab25.nettywebsocket.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author xingkong
 * @date 2022/12/28 15:40
 */
@Component
public class NettyServer {
    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    private static final String WEBSOCKET_PROTOCOL = "webSocket";

    /**
     * 端口号
     */
    @Value("${webSocket.netty.port}")
    private int port;

    /**
     * webSocket路径
     */
    @Value("${webSocket.netty.path}")
    private String webSocketPath;

    /**
     * 在Netty心跳检测中配置 - 读空闲超时时间设置
     */
    @Value("${webSocket.netty.readerIdleTime}")
    private long readerIdleTime;

    /**
     * 在Netty心跳检测中配置 - 写空闲超时时间设置
     */
    @Value("${webSocket.netty.writerIdleTime}")
    private long writerIdleTime;

    /**
     * 在Netty心跳检测中配置 - 读写空闲超时时间设置
     */
    @Value("${webSocket.netty.allIdleTime}")
    private long allIdleTime;

    @Autowired
    private WebSocketHandler webSocketHandler;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;

    private void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        //bossGroup辅助客户端的tcp连接请求，workGroup负责与客户端之间的读写操作
        bootstrap.group(bossGroup, workGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.localAddress(new InetSocketAddress(port));
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new IdleStateHandler(readerIdleTime, writerIdleTime, allIdleTime, TimeUnit.MINUTES));
                ch.pipeline().addLast(new HttpServerCodec());
                ch.pipeline().addLast(new ObjectEncoder());
                ch.pipeline().addLast(new ChunkedWriteHandler());

                ch.pipeline().addLast(new HttpObjectAggregator(8192));

                ch.pipeline().addLast(new WebSocketServerProtocolHandler(webSocketPath, WEBSOCKET_PROTOCOL, true, 65536 * 10));
                ch.pipeline().addLast(webSocketHandler);
            }
        });

        //配置完成，开始绑定server，通过调用sync同步方法阻塞直到绑定成功
        ChannelFuture channelFuture = bootstrap.bind().sync();
        log.info("server started and listener on :{}", channelFuture.channel().localAddress());
        //对关闭通道进行监听
        channelFuture.channel().closeFuture().sync();
    }

    @PreDestroy
    public void destory() throws InterruptedException {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().sync();
        }

        if (workGroup != null) {
            workGroup.shutdownGracefully().sync();
        }
    }

    @PostConstruct
    public void init() {
        new Thread(() -> {
            try {
                start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
