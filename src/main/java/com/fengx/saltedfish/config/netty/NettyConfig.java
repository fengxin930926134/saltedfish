package com.fengx.saltedfish.config.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@RequiredArgsConstructor
public class NettyConfig {

    private final NettyProperties myNettyProperties;
    private final NettyServerInitializer nettyServerInitializer;

    /**
     * boss 线程池
     * 负责客户端连接
     */
    @Bean
    public NioEventLoopGroup boosGroup(){
        return new NioEventLoopGroup(myNettyProperties.getBoss());
    }

    /**
     * worker线程池
     * 负责业务处理
     */
    @Bean
    public NioEventLoopGroup workerGroup(){
        return  new NioEventLoopGroup(myNettyProperties.getWorker());
    }

    /**
     * 服务器启动器
     */
    @Bean
    public ServerBootstrap serverBootstrap(){
        ServerBootstrap serverBootstrap  = new ServerBootstrap();
        serverBootstrap
                // 指定使用的线程组
                .group(boosGroup(),workerGroup())
                // 指定使用的通道
                .channel(NioServerSocketChannel.class)
                // 指定连接超时时间
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, myNettyProperties.getTimeout())
                // 指定worker处理器
                .childHandler(nettyServerInitializer);
        return serverBootstrap;
    }


//    /**
//     * 客户端启动器
//     */
//    @Bean
//    public Bootstrap bootstrap(){
//        // 新建一组线程池
//        NioEventLoopGroup eventExecutors = new NioEventLoopGroup(myNettyProperties.getBoss());
//        Bootstrap bootstrap = new Bootstrap();
//        bootstrap
//                .group(eventExecutors)   // 指定线程组
//                .option(ChannelOption.SO_KEEPALIVE, true)
//                .channel(NioSocketChannel.class) // 指定通道
//                .handler(new ClientListenerHandler()); // 指定处理器
//        return bootstrap;
//    }
}