package com.fengx.saltedfish.config.netty;

import com.fengx.saltedfish.server.NettyHandlerServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private final NettyProperties nettyProperties;

    /**
     * 初始化通道以及配置对应管道的处理器
     */
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new ReadTimeoutHandler(nettyProperties.getReadTimout()));
        //以下三个是Http的支持
        //http解码器
        pipeline.addLast(new HttpServerCodec());
        //支持写大数据流
        pipeline.addLast(new ChunkedWriteHandler());
        //http聚合器
        pipeline.addLast(new HttpObjectAggregator(1024 * 128));
        //websocket支持,设置路由;适配 ws://ip:port/ws
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        //添加自定义的助手类
        pipeline.addLast(NettyHandlerServer.getInstance());
    }
}